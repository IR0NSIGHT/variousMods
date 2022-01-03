package org.schema.game.server.ai.program.searchanddestroy;

import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.searchanddestroy.SimpleSearchAndDestroyMachine;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;

import java.util.HashMap;

public class SimpleSearchAndDestroyProgram extends TargetProgram<ShipAIEntity> {
	private String SIMPLE_SND = "SIMPLE_SND";

	public SimpleSearchAndDestroyProgram(ShipAIEntity entityState, boolean startSuspended) {
		super(entityState, startSuspended);
	}

	@Override
	public void onAISettingChanged(AIConfiguationElementsInterface setting) {

	}

	@Override
	protected String getStartMachine() {
		return SIMPLE_SND;
	}

	@Override
	protected void initializeMachines(
			HashMap<String, FiniteStateMachine<?>> machines) {
		machines.put(SIMPLE_SND, new SimpleSearchAndDestroyMachine(getEntityState(), this));
	}

}
