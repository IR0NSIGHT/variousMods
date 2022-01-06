package me.iron.npccontrol.triggers.FSM;

import org.schema.game.server.ai.program.simpirates.states.SimulationGroupState;
import org.schema.schine.ai.AiEntityStateInterface;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.01.2022
 * TIME: 16:46
 * FSM state used for fleet FSMs
 */
abstract public  class CustomFleetState extends SimulationGroupState {
    public CustomFleetState(AiEntityStateInterface aiEntityStateInterface) {
        super(aiEntityStateInterface);
    }

    public CustomFleetMachine getFleetMachine() {
        return (CustomFleetMachine) getMachine();
    }
}
