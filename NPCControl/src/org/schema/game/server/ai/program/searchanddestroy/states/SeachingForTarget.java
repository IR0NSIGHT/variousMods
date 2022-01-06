package org.schema.game.server.ai.program.searchanddestroy.states;

import org.schema.game.common.data.world.Universe;
import org.schema.game.server.ai.SegmentControllerAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

public class SeachingForTarget extends ShipGameState {

	/**
	 *
	 */
	
	private long lastNoTargetFound;

	public SeachingForTarget(AiEntityStateInterface gObj) {
		super(gObj);
	}

	

	@Override
	public boolean onEnter() {

		((TargetProgram<?>) getEntityState().getCurrentProgram()).setTarget(null);

		return false;
	}

	@Override
	public boolean onExit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onUpdate() throws FSMException {
		//		if(getEntity().toString().contains("SIM")){
		//			System.err.println(getEntity()+" SEARCHING FOR TARGET "+((TargetProgram<?>)getEntityState().getCurrentProgram()).getSectorTarget());
		//		}
		if (((SegmentControllerAIEntity<?>)getEntityState()).hadTimeout()) {
			//timeout setter makes new desicion
			((SegmentControllerAIEntity<?>)getEntityState()).onTimeout(this);
			return false;
		}
		if (System.currentTimeMillis() - lastNoTargetFound < 3000) {
			//last attempt had no result. wait 5 sec to try again
			return false;
		}
		if (getEntityState().isActive()) {

			findTarget(getEntity().getFactionId() < 0);
			if (((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget() == null) {
				//no target found with player.
				//				findTarget(false, enemyTeams);
				lastNoTargetFound = System.currentTimeMillis() + Universe.getRandom().nextInt(1000);
			}

			if (((TargetProgram<?>) getEntityState().getCurrentProgram()).getTarget() != null) {
				stateTransition(Transition.TARGET_AQUIRED);
			} else {
				if (((TargetProgram<?>) getEntityState().getCurrentProgram()).getSectorTarget() != null) {
					stateTransition(Transition.MOVE_TO_SECTOR);
					((SegmentControllerAIEntity<?>)getEntityState()).onNoTargetFound(this);
				}
			}
		}
		((SegmentControllerAIEntity<?>)getEntityState()).engageTimeout();
		return false;
	}

	

}
