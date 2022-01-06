package me.iron.npccontrol.triggers.FSM;

import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;

import java.util.HashMap;

class CustomPirateProgram extends TargetProgram<SimulationGroup> {
    private String PROGRAM = "PROGRAM";

    public CustomPirateProgram(SimulationGroup simulationGroup, boolean b) {
        super(simulationGroup, b);
    }

    @Override
    public void onAISettingChanged(AIConfiguationElementsInterface aiConfiguationElementsInterface) throws FSMException {

    }

    @Override
    protected String getStartMachine() {
        return PROGRAM;
    }

    @Override
    protected void initializeMachines(HashMap<String, FiniteStateMachine<?>> hashMap) {
        machines.put(PROGRAM, new CustomFleetMachine(getEntityState(), this));
    }
}
