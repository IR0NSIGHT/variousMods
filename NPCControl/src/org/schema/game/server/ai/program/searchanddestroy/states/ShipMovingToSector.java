package org.schema.game.server.ai.program.searchanddestroy.states;

import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.pathing.AbstractScene;
import me.iron.npccontrol.pathing.Pathfinder;
import me.iron.npccontrol.pathing.sm.StarPathFinder;
import me.iron.npccontrol.pathing.sm.StellarPosition;
import me.iron.npccontrol.triggers.DebugUI;
import me.iron.npccontrol.triggers.Utility;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.states.ShipGameState;
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
import java.util.Vector;

public class ShipMovingToSector extends ShipGameState {
	Vector4f color = new Vector4f();

	//get movement values from the FSM
	Vector3i targetSector = new Vector3i();
	Vector3f targetPos = new Vector3f();
	Vector3f movingDir = new Vector3f(); //thrust direction for ship

	private LinkedList<StellarPosition> waypoints = new LinkedList<>(); //internal path waypoints for collision avoidance
	private StellarPosition currentWP;
	private Iterator<StellarPosition> wpIterator;
	private int wpIdx;
	private Pathfinder pf;

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
		if (getEntity().isDocked() || getEntity().getUniqueIdentifier().contains("GFLT"))
			return false;
		pf = new StarPathFinder(getEntity().getSector(new Vector3i()));
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
			setMovingDir(new Vector3f(0,0,0));
			onTargetReached();
			return false;
		}
		assert wpIterator!=null&&waypoints!=null;

		//get own precise position
		Vector3i ownSector = getEntity().getSector(new Vector3i());
		Vector3f ownPos = getEntity().getWorldTransform().origin;

		//test if this waypoint is complete or no currentWP exists. //TODO ships seem to have trouble precisely hitting a waypoint.
		if ((Utility.getDistance(ownSector, targetSector)==0&& Utility.getDistance(ownPos,currentWP.getPosition())<20)) {
			onNextWaypoint();
			return false;
		}
		targetPos.set(currentWP.getPosition());

		//get direction to target (factoring in different sectors of ship and target.
		Vector3f thrustDir =Utility.getDir(ownSector,ownPos,targetSector,currentWP.getPosition());

		//ignore tiny movements
		if (thrustDir.length() < 0.1f)
			return false;

		setMovingDir(thrustDir); //dir the ship will move in (after the AI got updated)

		//debug
		if (lastLog + 1000 < System.currentTimeMillis()) {
			lastLog = System.currentTimeMillis();
			drawDebug(waypoints,thrustDir,1000);
		}
		return false;
	}

	private void onNextWaypoint() {
		ModMain.log("ship reached waypoint. remaining: " + (waypoints.size()-wpIdx));
		wpIdx++;
		if (wpIterator.hasNext()) {
			currentWP = wpIterator.next();
		} else {
			//final position reached
			assert Utility.getDistance(getEntity().getWorldTransform().origin, targetPos) < 5;
			ModMain.log(getEntity().getRealName()+" reached target");
			onTargetReached();
		}
	}

	private void onTargetReached() { //crashes server
		if (!getEntity().isFullyLoadedWithDock()) {
			return;
		}
		if (lastLog + 2000> System.currentTimeMillis()) {
			return;
		}
		lastLog = System.currentTimeMillis();
		Vector3i sector = new Vector3i(35,59,-5);
		back = !back;
		Vector3f nextPos;
		if (back) {
			nextPos = getRandomPointNearObject(sector, getEntity().getBoundingSphereTotal().radius);
		} else  {
			nextPos = getRandomSafePoint(sector,2000, getEntity().getBoundingSphereTotal().radius);
		}
		if (nextPos == null)
			throw new NullPointerException("nextpos is null");
		setMoveTarget(new StellarPosition(sector,nextPos));
	}

	/**
	 * gets a random waypoint that is guaranteed to not be inside an obstacle + offset
	 * @param sector
	 * @param offset
	 * @return
	 */
	private Vector3f getRandomPointNearObject(Vector3i sector, float offset) {
		Vector3f ownPos = getEntity().getWorldTransform().origin;
		Random r = new Random();
		boolean blocked;
		Vector3f nextPos = null;

		for (SegmentController s: GameServerState.instance.getSegmentControllersByName().values()) {
			Vector3i objSector = s.getSector(new Vector3i());
			if (!objSector.equals(sector))
				continue;

			if (s.getUniqueIdentifier().equals(getEntity().getUniqueIdentifier()) || s.isDocked())
				continue;

			nextPos = Utility.getDir(s.getSector(new Vector3i()),s.getWorldTransform().origin,getEntity().getSector(new Vector3i()), ownPos);
			if (nextPos.length()<1000)
				continue;
			nextPos.normalize();
			nextPos.scale(2000); //nextpos is now vector from ownPos, through obj, behind object
			nextPos.add(ownPos);
			blocked = pf.isPointInObstacle(sector,nextPos,offset);
			if (!blocked)
				return nextPos;
		}
		return nextPos;

	}

	/**
	 * requires internal pathfinder to be initialized with objects.
	 * will use pathfinder to find a position thats guaranteed to not be inside an object.
	 * @param sector
	 * @param radius
	 * @param safeDistance meters of minimum distance required to each object
	 * @return
	 */
	private Vector3f getRandomSafePoint(Vector3i sector, float radius, float safeDistance) {
		boolean safe = false;
		Vector3f nextPos = null;
		int i = 0;
		while (!safe) {		Random r = new Random();

			nextPos = new Vector3f(
					r.nextFloat()*(r.nextBoolean()?-1:1),
					r.nextFloat()*(r.nextBoolean()?-1:1),
					r.nextFloat()*(r.nextBoolean()?-1:1)
			);
			nextPos.normalize();
			nextPos.scale(radius*=1.2);
			safe = !pf.isPointInObstacle(sector,nextPos,safeDistance);
			i++;
			assert i <100:"infinite loop, cant find safe pos in sector";
		}
		return nextPos;
	}

	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer, Ship entity, ShipAIEntity s) {

		getEntity().getNetworkObject().orientationDir.set(0,0,0,0);
		getEntity().getNetworkObject().targetPosition.set(0, 0, 0);

		Vector3f moveDir = new Vector3f();
		moveDir.set(getMovingDir());
		getEntity().getNetworkObject().moveDir.set(moveDir);
		s.moveTo(timer, moveDir, true);
	}

	/**
	 * set where the ship should go
	 */
	public void setMoveTarget(StellarPosition target) {
		this.targetSector.set(target.getSector());
		this.targetPos.set(target.getPosition());
		waypoints.clear();

		pf.setScene(DebugUI.debugScene);
		waypoints.addAll(pf.relativeToStellar(pf.findPath(

				DebugUI.debugScene.getScenePos(
					getEntity().getSector(new Vector3i()),
					getEntity().getWorldTransform().origin),
				DebugUI.debugScene.getScenePos(targetSector,targetPos),
				getEntity().getBoundingSphereTotal().radius
				)));

		wpIterator = waypoints.iterator();
		currentWP = wpIterator.next();
		wpIdx=0;
		assert Utility.getDistance(waypoints.getLast().getPosition(),targetPos)<0.001f;
		pf.drawRaycasts();

		ModMain.log("set move target for ship " + getEntity().getName() + "to "+targetSector + "-"+targetPos);
	}

	/**
	 * draw waypoints and thrust direction
	 */
	private void drawDebug(LinkedList<StellarPosition> waypoints, Vector3f thrustDir, long lifetime) {
		LinkedList<DebugLine> points = new LinkedList<>();
		Vector3f previous = null;
		if (back) {
			color.set(0,0,1,1);
		}else {
			color.set(0,1,1,1);
		}
		//Drawwaypoints
		for (StellarPosition wp: waypoints) {
			points.addAll(new DebugSphere(
					wp,
					100,
					color,
					lifetime
			).getLines());
			if (wp.equals(currentWP)||wp.getPosition().equals(targetPos)) {
				points.addAll(new DebugSphere(
						wp,
						150,
						color,
						lifetime
				).getLines());
			}
			if (previous != null) {
				points.add(new DebugLine(
						new Vector3f(previous),
						new Vector3f(wp.getPosition()),
						new Vector4f(color),
						lifetime
				));
			}
			previous = wp.getPosition();
		}

		new DebugPacket(points).sendToAll();
	}
}
