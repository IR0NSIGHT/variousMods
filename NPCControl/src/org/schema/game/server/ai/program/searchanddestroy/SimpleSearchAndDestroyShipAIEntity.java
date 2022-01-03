package org.schema.game.server.ai.program.searchanddestroy;

import org.schema.game.common.controller.Ship;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.ShootingStateInterface;
import org.schema.game.server.ai.program.common.states.ShipGameState;
import org.schema.game.server.ai.program.searchanddestroy.SimpleSearchAndDestroyProgram;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipEngagingTarget;
import org.schema.game.server.ai.program.searchanddestroy.states.ShipGettingToTarget;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.graphicsengine.core.Timer;

import javax.vecmath.Vector3f;

public class SimpleSearchAndDestroyShipAIEntity extends ShipAIEntity {

	private static final long CHECK_PROXIMITY_DELAY = 10000;
	private long lastProximityTest;

	private Vector3f dist = new Vector3f();

	//	private Ship aggroTarget;
	public SimpleSearchAndDestroyShipAIEntity(Ship s, boolean startSuspended) {
		super("S&D_ENT", s);
		if (s.isOnServer()) {
			setCurrentProgram(new SimpleSearchAndDestroyProgram(this, startSuspended));
		}
		lastProximityTest = (long) (System.currentTimeMillis() + Math.random() * CHECK_PROXIMITY_DELAY);

	}
	//	/* (non-Javadoc)
	//	 * @see org.schema.game.server.ai.SegmentControllerAIEntity#handleHitBy(int, org.schema.schine.network.objects.Sendable)
	//	 */
	//	@Override
	//	public void handleHitBy(int actualDamage, Damager from) {
	//		if(System.currentTimeMillis() - lastTargetChangeDueToAttack < TARGET_ATTACKED_CHANGE_DELAY){
	//			return;
	//		}
	//		if(from instanceof Ship){
	//			Ship ship = (Ship)from;
	//			RType relation = ((FactionState)getState()).getFactionManager().getRelation(ship, getEntity());
	//			System.err.println("Relation: "+relation);
	//			if(relation == RType.ENEMY || relation == RType.NEUTRAL){
	//				System.err.println("[AI] Ship has been attacked and will now respond aggression "+getEntity()+" -> "+ship);
	//				lastTargetChangeDueToAttack = System.currentTimeMillis();
	//				((TargetProgram<?>)getCurrentProgram()).setTarget(ship);
	//				this.aggroTarget = (ship);
	//				try {
	//					((TargetProgram<?>)getCurrentProgram()).getMachine().getFsm().getCurrentState().stateTransition(Transition.RESTART);
	//				} catch (FSMException e) {
	//					e.printStackTrace();
	//				}
	//			}
	//		}
	//	}

	/* (non-Javadoc)
	 * @see org.schema.game.server.ai.ShipAIEntity#updateAIServer(org.schema.schine.graphicsengine.core.Timer)
	 */
	@Override
	public void updateAIServer(Timer timer) throws FSMException {
		super.updateAIServer(timer);

		
		if(getStateCurrent() instanceof ShipGameState){
			((ShipGameState)getStateCurrent()).updateAI(unit, timer, getEntity(), this);
		}
		
		

		if (getEntity().getProximityObjects().size() > 0 && (getStateCurrent() instanceof ShipEngagingTarget || getStateCurrent() instanceof ShipGettingToTarget || getStateCurrent() instanceof ShootingStateInterface)) {
			try {
				//				System.err.println(getSendable()+" IS NOW EVADING OBJECTS: "+getProximityObjects().size());
				if (!getEntity().getDockingController().isDocked()) {
					getStateCurrent().stateTransition(Transition.ENEMY_PROXIMITY);
				}
			} catch (FSMException e) {
				e.printStackTrace();
			}
		} else {

		}

	
	}

}
