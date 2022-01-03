package me.iron.npccontrol.triggers.FSM;

import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.stateMachines.AIConfiguationElementsInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;

import java.util.HashMap;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.01.2022
 * TIME: 19:28
 * makes a simulation group stalk a target, using jammers
 */
public class StalkProgram extends TargetProgram<SimulationGroup> {
    private String PROGRAM = "PROGRAM";
    public StalkProgram(SimulationGroup group, boolean b, int targetID) {
        super(group, b);
        setSpecificTargetId(targetID);
    }

    @Override
    public void onAISettingChanged(AIConfiguationElementsInterface aiConfiguationElementsInterface) throws FSMException {

    }

    @Override
    protected String getStartMachine() {
        return this.PROGRAM;
    }

    @Override
    protected void initializeMachines(HashMap<String, FiniteStateMachine<?>> hashMap) {
        hashMap.put(this.PROGRAM,new StalkMachine(this.getEntityState(),this));
    }
}
