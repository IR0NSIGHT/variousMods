package org.schema.game.server.ai.program.searchanddestroy.states;

import api.network.packets.PacketUtil;
import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.Pathfinder;
import me.iron.npccontrol.triggers.Utility;
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

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class ShipMovingToSector extends ShipGameState {

	//get movement values from the FSM
	Vector3i targetSector = new Vector3i();
	Vector3f targetPos = new Vector3f();
	Vector3f targetRot = new Vector3f();

	Vector3f movingDir = new Vector3f(); //thrust direction for ship

	private LinkedList<Vector3f> waypoints = new LinkedList<>(); //internal path waypoints for collision avoidance
	private Vector3f currentWP;
	private Iterator<Vector3f> wpIterator;

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
		onTargetReached();
	//	setMoveTarget(new Vector3i(32,80,0),new Vector3f(500,0,0), new Vector3f());
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	private boolean back;
	@Override
	public boolean onUpdate() throws FSMException {
		//TODO make sure the distance tolerances make sense
		GameServerState state = (GameServerState) getEntity().getState();

		//get own precise position
		Vector3i ownSector = getEntity().getSector(new Vector3i());
		Vector3f ownPos = getEntity().getWorldTransform().origin;

		//test if this waypoint is complete or no currentWP exists. //TODO ships seem to have trouble precisely hitting a waypoint.
		if (currentWP == null || (Utility.getDistance(ownSector, targetSector)==0&& Utility.getDistance(ownPos,currentWP)<20)) {
			if (wpIterator == null) {
				wpIterator = waypoints.iterator();
			}
			boolean next = wpIterator.hasNext();
			if (next) {
				currentWP = new Vector3f(wpIterator.next());
			//	ModMain.log("new wp: target pos: " + targetPos + " target sector " + targetSector);

			} else {
				//final position reached
				assert Utility.getDistance(getEntity().getWorldTransform().origin, targetPos) < 5;
				onTargetReached();
				return false;
			}
		}
		targetPos.set(currentWP);

		//get direction to target
		Vector3i dir = Utility.getDir(ownSector, new Vector3i(targetSector));
		float sectorSize = ((GameStateInterface) getEntity().getState()).getSectorSize();
		dir.scale((int)sectorSize); //scale from sector to meters

		//add in-sector-targetPos, sub own-sector-pos (precision might get lost but not important on long journeys)
		Vector3f thrustDir = dir.toVector3f();
		if (ownSector.equals(targetSector)) {
			thrustDir.sub(ownPos);
			thrustDir.add(targetPos);
		}

		if (thrustDir.length() < 1) //ignore tiny movements
			return false;

		setMovingDir(thrustDir); //dir the ship will move in (after the AI got updated)
		return false;
	}

	private void onTargetReached() {
		if (back) {
			setMoveTarget(new Vector3i(32,80,0), new Vector3f(2000,1000,1000), new Vector3f());
		} else {
			setMoveTarget(new Vector3i(32,80,0), new Vector3f(-2000,1000,1000), new Vector3f());
		}
		back = !back;
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
		currentWP = null;
		wpIterator = null;
		Pathfinder pf = new Pathfinder(getEntity().getUniqueIdentifier());
		waypoints.addAll(pf.findPath(
				getEntity().getSector(new Vector3i()),
				getEntity().getWorldTransform().origin,
				targetSector,
				targetPos,
				Math.max(getEntity().getBoundingSphereTotal().radius*2,300)
		));

		LinkedList<DebugLine> lines = new LinkedList<>();
		lines.add(new DebugLine(getEntity().getWorldTransform().origin,targetPos, new Vector4f(1,0,0,1),120*1000));
		Vector3f previous = new Vector3f(getEntity().getWorldTransform().origin);
		for (Vector3f wp: waypoints) {
			lines.add(new DebugLine(
					new Vector3f(previous),
					new Vector3f(wp),
					new Vector4f(0,1,0,1),
					45*1000
			));
			previous.set(wp);
		}
		new DebugPacket(lines).sendToAll();



	//	ModMain.log("set move target for ship " + getEntity().getName() + "to "+targetSector + "-"+targetPos);
		//plotPath(getEntity().getSector(new Vector3i()),getEntity().getWorldTransform().origin,targetSector,targetPos);
	}
}
