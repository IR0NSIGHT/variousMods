package org.schema.game.server.ai.program.searchanddestroy.states;

import api.network.packets.PacketUtil;
import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.Pathfinder;
import me.iron.npccontrol.triggers.Utility;
import org.lwjgl.openal.Util;
import org.lwjgl.util.vector.Vector;
import org.newdawn.slick.util.pathfinding.PathFinder;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.*;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.ai.program.searchanddestroy.SimpleSearchAndDestroyMachine;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.graphicsengine.core.Timer;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;
import org.schema.schine.graphicsengine.forms.debug.DebugPacket;
import org.schema.schine.graphicsengine.forms.debug.DebugSphere;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

public class ShipMovingToSector extends ShipGameState {
	Vector4f color = new Vector4f();
	//get movement values from the FSM
	Vector3i targetSector = new Vector3i();
	Vector3f targetPos = new Vector3f();
	Vector3f targetRot = new Vector3f();

	Vector3f movingDir = new Vector3f(); //thrust direction for ship

	private LinkedList<Vector3f> waypoints = new LinkedList<>(); //internal path waypoints for collision avoidance
	private Vector3f currentWP;
	private Iterator<Vector3f> wpIterator;
	private int wpIdx;

	private long lastLog;
	public ShipMovingToSector(AiEntityStateInterface gObj) {
		super(gObj);
	}

	/**
	 * @return the movingDir
	 */
	public Vector3f getMovingDir() {
		return movingDir;
	}

	/**
	 * @param movingDir the movingDir to set
	 */
	public void setMovingDir(Vector3f movingDir) {
		this.movingDir = movingDir;
	}

	@Override
	public boolean onEnter() {
		//TODO debug why freshly loaded entites get wierd pathing behavior
		if (getEntity().isDocked() || getEntity().getUniqueIdentifier().contains("GFLT"))
			return false;

		onTargetReached();
		Random r = new Random(getEntity().dbId);
		color.set(r.nextFloat(),r.nextFloat(),r.nextFloat(),1);
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	private boolean back;
	@Override
	public boolean onUpdate() throws FSMException {


		if (waypoints.isEmpty()) {
			ModMain.log("wps is empty.");
			onTargetReached();
			return false;
		}
		assert wpIterator!=null&&waypoints!=null;


		//get own precise position
		Vector3i ownSector = getEntity().getSector(new Vector3i());
		Vector3f ownPos = getEntity().getWorldTransform().origin;

		//test if this waypoint is complete or no currentWP exists. //TODO ships seem to have trouble precisely hitting a waypoint.
		if ((Utility.getDistance(ownSector, targetSector)==0&& Utility.getDistance(ownPos,currentWP)<20)) {
			onNextWaypoint();
			return false;
		}
		targetPos.set(currentWP);

		//get direction to target (factoring in different sectors of ship and target.

		Vector3f thrustDir =Utility.getDir(ownSector,ownPos,targetSector,currentWP);

		if (thrustDir.length() < 1) //ignore tiny movements
			return false;


		setMovingDir(thrustDir); //dir the ship will move in (after the AI got updated)

		LinkedList<DebugLine> lines = new LinkedList<>();
		Vector3f end = new Vector3f(ownPos);
		end.add(thrustDir);
		lines.add(new DebugLine(
				new Vector3f(ownPos),end,new Vector4f(1,1,0,1),10)
		);
		lines.add(new DebugLine(new Vector3f(ownPos),new Vector3f(currentWP),new Vector4f(1,0,1,1),10));
		new DebugPacket(lines).sendToAll();
	//	ModMain.log("length thrust dir: " + thrustDir.length() + " : " + thrustDir);

		return false;
	}

	private void onNextWaypoint() {
		ModMain.log("ship reached waypoint. remaining: " + (waypoints.size()-wpIdx));
		wpIdx++;
		if (wpIterator.hasNext()) {
			currentWP = new Vector3f(wpIterator.next());
		} else {
			//final position reached
			assert Utility.getDistance(getEntity().getWorldTransform().origin, targetPos) < 5;
			ModMain.log(getEntity().getRealName()+" reached target");
			onTargetReached();
		}
	}
	private void onTargetReached() {
		if (!getEntity().isFullyLoadedWithDock()) {
			return;
		}
		Vector3i sector = new Vector3i(35,59,-5);
		Random r = new Random();
		Vector3f nextPos = new Vector3f(
				r.nextFloat()*(r.nextBoolean()?-1:1),
				r.nextFloat()*(r.nextBoolean()?-1:1),
				r.nextFloat()*(r.nextBoolean()?-1:1)
		);
		nextPos.normalize();
		nextPos.scale(2000);
		setMoveTarget(sector,nextPos, new Vector3f());
	}

	/**
	 * gets a random waypoint that is guaranteed to not be inside an obstacle + offset
	 * @param sector
	 * @param offset
	 * @return
	 */
	private Vector3f getRandomPointNearObject(Vector3i sector, float offset) {

		Random r = new Random();
		boolean blocked = true;
		Vector3f nextPos = null;
		while (blocked) {
			//select wp near random object
			try {
				Set<SimpleTransformableSendableObject<?>> objs = GameServerState.instance.getUniverse().getSector(getEntity().getSector(new Vector3i())).getEntities();
				int size = objs.size();
				int idx = r.nextInt(size)-1;
				Iterator<SimpleTransformableSendableObject<?>> it = objs.iterator();
				for (int i = 0; i < idx && it.hasNext(); i++) {
					it.next();
				}
				nextPos = new Vector3f(it.next().getWorldTransform().origin);
				nextPos.add(new Vector3f(0,offset,0));
				blocked = new Pathfinder("").isPointInObstacle(sector,nextPos,offset);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return nextPos;

	}



	public static void main(String[] args) throws IOException {
	}

	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer, Ship entity, ShipAIEntity s) throws FSMException {

		getEntity().getNetworkObject().orientationDir.set(0,0,0,0);
		getEntity().getNetworkObject().targetPosition.set(0, 0, 0);

		Vector3f moveDir = new Vector3f();
		moveDir.set(getMovingDir());
		getEntity().getNetworkObject().moveDir.set(moveDir);
		s.moveTo(timer, moveDir, true);

	}

	/**
	 * set where the ship should go
	 * @param targetSector sector
	 * @param targetPos in-sector position
	 * @param targetRot rotation to assume in the end (unused)
	 */
	public void setMoveTarget(Vector3i targetSector, Vector3f targetPos, Vector3f targetRot) {
		this.targetSector.set(targetSector);
		this.targetPos.set(targetPos);
		this.targetRot.set(targetRot);
		waypoints.clear();
		Pathfinder pf = new Pathfinder(getEntity().getUniqueIdentifier());
		waypoints.addAll(pf.findPath(
				getEntity().getSector(new Vector3i()),
				getEntity().getWorldTransform().origin,
				targetSector,
				targetPos,
				Math.max(getEntity().getBoundingSphereTotal().radius*2,300)
		));
		wpIterator = waypoints.iterator();
		currentWP = wpIterator.next();
		wpIdx=0;
		assert Utility.getDistance(waypoints.getLast(),targetPos)<0.001f;
		pf.drawRaycasts();

		Random r = new Random();
		LinkedList<DebugLine> points = new LinkedList<>();
		for (Vector3f wp: waypoints) {
			points.addAll(new DebugSphere(
					wp,
					100,
					new Vector4f(0,r.nextFloat(),r.nextFloat(),1),
					120*1000
			).getLines());
		}
		new DebugPacket(points).sendToAll();
		ModMain.log("set move target for ship " + getEntity().getName() + "to "+targetSector + "-"+targetPos);
		//plotPath(getEntity().getSector(new Vector3i()),getEntity().getWorldTransform().origin,targetSector,targetPos);
	}
}
