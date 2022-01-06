package me.iron.npccontrol.triggers.FSM;

import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.01.2022
 * TIME: 17:35
 * Transition.TARGET_AQUIRED
 */
public class CaptureTarget extends CustomFleetState  {
    public CaptureTarget(AiEntityStateInterface aiEntityStateInterface) {
        super(aiEntityStateInterface);
    }

    @Override
    public boolean onEnter() {
        return false;
    }

    @Override
    public boolean onExit() {
        return false;
    }

    @Override
    public boolean onUpdate() throws FSMException {
        if (getFleetMachine().currentTarget == null) {
            stateTransition(Transition.NO_TARGET_FOUND);
            return false;
        }
        SegmentController target = GameServerState.instance.getSegmentControllersByName().get(getFleetMachine().currentTarget);
        target.setFactionId(getFleetMachine().ownFaction);
        getSimGroup().getMembers().add(target.getUniqueIdentifier());
        stateTransition(Transition.TARGET_AQUIRED);
        return false;
    }
}
