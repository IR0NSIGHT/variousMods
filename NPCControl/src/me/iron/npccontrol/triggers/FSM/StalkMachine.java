package me.iron.npccontrol.triggers.FSM;

import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.cloaking.StealthAddOn;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.State;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.01.2022
 * TIME: 19:30
 */
public class StalkMachine extends FiniteStateMachine<String> {
    public StalkMachine(AiEntityStateInterface aiEntityStateInterface, MachineProgram<?> machineProgram, State state) {
        super(aiEntityStateInterface, machineProgram, "s", state);
    }

    @Override
    public void update() throws FSMException {
        for ( String s :   ((SimulationGroup)getObj()).getMembers()) {
            //use jammer
            useJammer(s);
       //     GameServerState.instance.getLo
            //move towards target
       //     ((SimulationGroup)getObj()).moveToTarget()
        }

    }

    private void useJammer(String UID) {
        SegmentController sc = GameServerState.instance.getSegmentControllersByName().get(UID);
        if (sc instanceof ManagedUsableSegmentController) {
            StealthAddOn sa = ((ManagedUsableSegmentController)sc).getManagerContainer().getStealthAddOn();
            if (sa.isActive())
                return;
            sa.executeModule();
        }
    }
    public StalkMachine(AiEntityStateInterface aiEntityStateInterface, MachineProgram<?> machineProgram) {
        super(aiEntityStateInterface, machineProgram, "");
    }

    @Override
    public void createFSM(String s) {

    }

    @Override
    public void onMsg(Message message) {

    }
}
