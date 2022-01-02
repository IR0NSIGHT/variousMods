package me.iron.npccontrol.triggers.FSM;

import org.schema.game.client.data.PlayerControllable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 02.01.2022
 * TIME: 16:10
 * transitions to:
 * Transition.NO_TARGET_FOUND //no target
 * Transition.TARGET_OUT_OF_RANGE //target is trying to run away
 * Transition.WAIT_COMPLETED //time is up, didnt comply
 * Transition.CONDITION_SATISFIED //did comply
 * Transition.NPCS_ABORT //target is NPC, not player controlled.
 */
public class RequestSurrender extends CustomFleetState {
    private int timeToComply = 30*1000;
    private long timeRequestSent;
    private boolean warned;
    private float targetSpeed;
    private String surrenderMssgText = "Surrender your ship. Stop the engines. Unfaction and unfleet. Leave the ship and move 100m away. you have %s seconds to comply. Surrender or die.";
    public RequestSurrender(AiEntityStateInterface aiEntityStateInterface, int timeToComply) {
        super(aiEntityStateInterface);
        this.timeToComply = timeToComply;

    }

    @Override
    public boolean onEnter() {
        timeRequestSent = System.currentTimeMillis();
        return false;
    }

    @Override
    public boolean onExit() {
        warned = false;
        return false;
    }

    @Override
    public boolean onUpdate() throws FSMException {
        if (getFleetMachine().currentTarget == null) {
            stateTransition(Transition.NO_TARGET_FOUND);
            return false;
        }

        SegmentController sc = GameServerState.instance.getSegmentControllersByName().get(getFleetMachine().currentTarget);
        if (sc == null) {
            stateTransition(Transition.NO_TARGET_FOUND);
            return false;
        }

        if (!(sc instanceof PlayerControllable)) { //AI wont surrender.
            stateTransition(Transition.NPCS_ABORT);
            return false;
        }

        if (!((PlayerControllable)sc).getAttachedPlayers().isEmpty() && !warned) {
           warned = true;
           for (PlayerState p : ((PlayerControllable)sc).getAttachedPlayers()) {
               p.sendServerMessage(new ServerMessage(Lng.astr(surrenderMssgText,timeToComply),ServerMessage.MESSAGE_TYPE_DIALOG, p.getId()));
               p.sendServerMessage(new ServerMessage(Lng.astr(surrenderMssgText,timeToComply),ServerMessage.MESSAGE_TYPE_SIMPLE, p.getId()));
           }
           targetSpeed = sc.getSpeedCurrent();
        }

        if (warned && getTimeLeft() > timeToComply-5000) { //5 seconds after being warned
            //is target trying to escape?
            if (sc.getSpeedCurrent()>5 && !(sc.getSpeedCurrent()<targetSpeed)) {//it did not decellerate or stop
                stateTransition(Transition.TARGET_OUT_OF_RANGE);
                return false;
            }

            //is it complying?
            if (sc.getSpeedCurrent()<5 && ((PlayerControllable)sc).getAttachedPlayers().isEmpty() && sc.getFactionId() == 0 && sc.getFleet() == null) {
                stateTransition(Transition.CONDITION_SATISFIED);
                return false;
            }
        }

        //did not comply, kill him
        if (warned && getTimeLeft()<= 0) {
            stateTransition(Transition.WAIT_COMPLETED);
            return false;
        }
        return false;
    }

    private long getTimeLeft() {
        return timeToComply - (System.currentTimeMillis()-timeRequestSent);
    }
}
