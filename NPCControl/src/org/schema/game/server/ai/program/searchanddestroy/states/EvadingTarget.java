package org.schema.game.server.ai.program.searchanddestroy.states;

import org.schema.common.util.linAlg.Quat4fTools;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.server.ai.AIShipControllerStateUnit;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;

public class EvadingTarget extends ShipGameState {

	/**
	 *
	 */
	
	private final Vector3f movingDir = new Vector3f();
	private long start = 0;
	public EvadingTarget(AiEntityStateInterface gObj) {
		super(gObj);
	}

	/**
	 * @return the movingDir
	 */
	public Vector3f getMovingDir() {
		return movingDir;
	}

	@Override
	public boolean onEnter() {
		getMovingDir().set(0, 0, 0);
		start = System.currentTimeMillis();
		return false;
	}

	@Override
	public boolean onExit() {
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		if (System.currentTimeMillis() - start > 30000) {
			stateTransition(Transition.RESTART);
		}
		if (getEntity().getProximityObjects().size() > 0) {

			((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);

			Vector3f sum = new Vector3f();
			Vector3f dist = new Vector3f();
			boolean overlaps = false;
			for (int sId : getEntity().getProximityObjects()) {
				SegmentController s = (SegmentController)getEntity().getState().getLocalAndRemoteObjectContainer().getLocalObjects().get(sId);
				Vector3f from = getEntity().getWorldTransform().origin;
				Vector3f to = s.getWorldTransform().origin;
				dist.sub(from, to);
				sum.add(dist);

				s.calcWorldTransformRelative(getEntity().getSectorId(), ((GameServerState) getEntity().getState()).getUniverse().getSector(getEntity().getSectorId()).pos);

				overlaps = overlaps || getEntity().overlapsAABB(s, s.getClientTransform(), 30);
			}
			sum.scale(10);
			getMovingDir().set(sum);

			if (!overlaps) {
				//				System.err.println("NO OVERLAPPING");
				//				System.err.println("Clearing Proximity "+((ShipAIEntity)getGObj()).getProximityObjects().size());
				stateTransition(Transition.RESTART);
			}
		} else {
			//			System.err.println("[AI] !!!!!!!!!! EVADING FINISHED");
			stateTransition(Transition.RESTART);
		}
		return false;
	}

	@Override
	public void updateAI(AIShipControllerStateUnit unit, Timer timer,
			Ship entity, ShipAIEntity s) throws FSMException {
		getEntity().getNetworkObject().orientationDir.set(0, 0, 0,0);
		getEntity().getNetworkObject().targetPosition.set(new Vector3f(0, 0, 0));

		Vector3f moveDir = new Vector3f();
		moveDir.set(getMovingDir());
		getEntity().getNetworkObject().moveDir.set(moveDir);
		s.moveTo(timer, moveDir, false);
		Vector3f n = new Vector3f(moveDir);
		if (n.lengthSquared() > 0) {
			n.normalize();
			n.negate();
			getEntity().getNetworkObject().orientationDir.set(n, 0);
			s.orientate(timer, Quat4fTools.getNewQuat(n.x, n.y, n.z, 0));
		}		
	}

}
