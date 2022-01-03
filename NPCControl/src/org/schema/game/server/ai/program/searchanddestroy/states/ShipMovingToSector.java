package org.schema.game.server.ai.program.searchanddestroy.states;

import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.Utility;
import org.lwjgl.util.vector.Vector;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameStateInterface;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.world.*;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.ai.program.searchanddestroy.SimpleSearchAndDestroyMachine;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.graphicsengine.core.Timer;

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

	Vector3f movingDir; //thrust direction for ship

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
		setMoveTarget(new Vector3i(32,80,0),new Vector3f(500,0,0), new Vector3f());
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	private boolean back;
	@Override
	public boolean onUpdate() throws FSMException {
		GameServerState state = (GameServerState) getEntity().getState();

		//get own precise position
		Vector3i ownSector = getEntity().getSector(new Vector3i());
		Vector3f ownPos = getEntity().getWorldTransform().origin;
		//Vector3f ownRot = getEntity().getWorldTransform().basis;

		//test if this waypoint is complete or no currentWP exists.
		if (currentWP == null || (Utility.getDistance(ownSector, targetSector)==0&& Utility.getDistance(ownPos,currentWP)<5)) {
			if (wpIterator == null) {
				wpIterator = waypoints.iterator();
			}
			boolean next = wpIterator.hasNext();
			if (next) {
				currentWP = new Vector3f(wpIterator.next());
				ModMain.log("new wp: target pos: " + targetPos + " target sector " + targetSector);

			} else {
				//final position reached
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
			setMoveTarget(new Vector3i(32,80,0), new Vector3f(500,0,0), new Vector3f());
		} else {
			setMoveTarget(new Vector3i(32,80,0), new Vector3f(-500,0,0), new Vector3f());
		}
		back = !back;
	}

	/**
	 * plot a multi-waypoint path to the target avoiding any obstacles/collisions.
	 * @param ownSector
	 * @param ownPos
	 * @param targetSector
	 * @param targetPos
	 */
	private void plotPath(Vector3i ownSector, Vector3f ownPos, Vector3i targetSector, Vector3f targetPos) {
		//TODO star avoidance, long distance plotting, jumping
		try {
			Vector4f obstacle = getFirstObstacle(ownSector,ownPos, Utility.getDir(ownSector,ownPos,targetSector,targetPos),getEntity().getBoundingSphereTotal().radius*2);
			if (obstacle != null) {
				float safeDistanceToObstacle = getEntity().getBoundingSphereTotal().radius*5+obstacle.z;
				Vector3f obstaclePos = new Vector3f(obstacle.w,obstacle.x,obstacle.y);
				Vector3f wp = evadeObstacle(obstaclePos, movingDir, safeDistanceToObstacle);
				waypoints.add(wp);
				ModMain.log("new evasive waypoint: " + wp + " distance to obst: " + Utility.getDistance(obstaclePos,wp));
				if (waypoints.size() < 10 && Utility.getDistance(obstaclePos,targetPos)>safeDistanceToObstacle) //only plot more if the last WP was
					//todo new wp might be outside of current sector.
					plotPath(ownSector, wp, targetSector, targetPos); //recurse with new waypoint, plot from there.
			} else {
				waypoints.add(targetPos);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * get a intermediate waypoint that avoids given obstacle at pos, evading in a random direction orthogonal to dir.
	 * returns the offset from the obstacle position for the new waypoint
	 */
	private Vector3f evadeObstacle(Vector3f pos, Vector3f dir, float distance) {
		Vector3f b = new Vector3f(dir); b.normalize();
		Vector3f off = new Vector3f(1,0,0);
		if (b.dot(off)<1/10000f) { //guarantee the "random" offset is never in line with given direction
			off.set(0,1,0);
		}

		b.add(off); //shift dir a bit so it can create a cross product
		b.cross(b,dir); //orthogonal to b and dir => new offset.
		b.normalize();b.scale(distance);
		return b;
	}

	public static void main(String[] args) throws IOException {
	}

	/**
	 * determine if any entity in this sector is an obstacle on the path of "pos+x*dir".
	 * entities are obstacles if their distance to the path is smaller than minDist+their bounding sphere.
	 * returns the position and bounding sphere radius of the first obstacle.
	 * @param sector
	 * @param pos
	 * @param dir
	 * @param minDist minimal distance required to be away from path
	 * @return (x,y,z,radius) pos & radius of bounding sphere of object
	 * @throws IOException
	 */
	private Vector4f getFirstObstacle(Vector3i sector, Vector3f pos, Vector3f dir, float minDist) throws IOException {
		Vector3f objPos, ab, ap;
		for (SimpleTransformableSendableObject obj: GameServerState.instance.getUniverse().getSector(sector).getEntities()) {
			if ((obj instanceof Ship && ((Ship) obj).isDocked()) || obj.getUniqueIdentifier().equals(getEntity().getUniqueIdentifier()))
				continue;

			if (!(obj instanceof ManagedUsableSegmentController)) { //playerstates have a fucked up worldtransform getter.
				continue;
			}
			//point to compare to a-b line
			objPos = obj.getWorldTransform().origin;

			//build dot product to determine if "possible collison" point is behind ship already.
			ab = new Vector3f(dir); ab.sub(pos); ab.normalize();
			ap = new Vector3f(objPos); ap.sub(pos); ap.normalize();
			float dot = ap.dot(ab);
			if (dot<0) { //collision point is behind ship.
				continue;
			}

			//TODO allow constant-speed moving objects

			//avoid stationary objects (roids and stations)
			if (obj.getSpeedCurrent() > 5) {
				continue;
			}
			Vector3f targetPos = new Vector3f(pos);targetPos.add(dir);
			float objBBS = obj.getBoundingSphereTotal().radius;

			//is the ship supposed to move into the bounding sphere of the object bc targetpos is inside of it?
			if (objBBS>=Utility.getDistance(objPos,targetPos)) {
				// 	continue;
			}

			//is the starting pos inside a danger zone
			if (Utility.getDistance(pos,objPos)<(minDist+objBBS))
				continue; //just ignore that //TODO find safe way out of danger zone

			//will the path come closer than bounding sphere of ship and obj combined?
			float pointToLine = Utility.distancePointLine(objPos, pos, dir);
			if (pointToLine < (minDist+objBBS)) {
				return new Vector4f(
						objPos.x,objPos.y,objPos.z,objBBS
				);
			}
		}
		//tested all objects in sector, none are obstacles, return null
		return null;
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
		ModMain.log("set move target for ship " + getEntity().getName() + "to "+targetSector + "-"+targetPos);
		plotPath(getEntity().getSector(new Vector3i()),getEntity().getWorldTransform().origin,targetSector,targetPos);

	}
}
