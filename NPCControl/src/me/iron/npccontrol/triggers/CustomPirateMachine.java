package me.iron.npccontrol.triggers;

import me.iron.npccontrol.triggers.FSM.GoToRandomInSystem;
import org.schema.game.server.ai.program.common.states.WaitingTimed;
import org.schema.game.server.ai.program.simpirates.states.MovingToSector;
import org.schema.game.server.ai.program.simpirates.states.Starting;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.Transition;

class CustomPirateMachine extends FiniteStateMachine<String> {
    //Fsm variables


    public CustomPirateMachine(AiEntityStateInterface aiEntityStateInterface, MachineProgram<?> machineProgram) {
        super(aiEntityStateInterface, machineProgram, "");
    }

    private long last;

    @Override
    public void update() throws FSMException {
        super.update();
        checkForTargets();
        //TODO debug stuff
    }

    private void checkForTargets() {

    }
    @Override
    public void createFSM(String s) {
        Starting starting = new Starting(getObj());
        GoToRandomInSystem nextRandom = new GoToRandomInSystem(getObj());
        //    GoToRandomSector nextRandom = new GoToRandomSector(getObj()); //will edit the machines target sector to a nearby random location.
        MovingToSector goToSector = new MovingToSector(getObj()); //will tell ships to move to target
        WaitingTimed waitingTimed = new WaitingTimed(getObj(), 10); //wait 10 seconds in target sector

        //start->randomSector->moveTo->wait->randomSector
        starting.addTransition(Transition.PLAN, nextRandom);
        nextRandom.addTransition(Transition.MOVE_TO_SECTOR, goToSector);
        goToSector.addTransition(Transition.TARGET_SECTOR_REACHED, waitingTimed);
        waitingTimed.addTransition(Transition.WAIT_COMPLETED, nextRandom);

        setStartingState(starting);
    }

    @Override
    public void onMsg(Message message) {

    }
}
