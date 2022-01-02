package me.iron.npccontrol.triggers.FSM;

import me.iron.npccontrol.triggers.Utility;
import org.lwjgl.Sys;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.server.ai.program.common.states.WaitingTimed;
import org.schema.game.server.ai.program.simpirates.states.*;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.FiniteStateMachine;
import org.schema.schine.ai.stateMachines.Message;
import org.schema.schine.ai.stateMachines.Transition;
import org.schema.schine.network.objects.Sendable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

class CustomFleetMachine extends FiniteStateMachine<String> {
    //Fsm variables
    private ArrayList<ManagedUsableSegmentController> targets = new ArrayList<>();
    private String flagShip;
    private HashMap<String, Vector3i> members_to_sector = new HashMap<>();

    //feuerstatus
    FireMode fleetFireMode = FireMode.return_fire;

    //combat mode
    CombatMode fleetCombatMode = CombatMode.evade;

    String currentTarget;
    int ownFaction;

    public CustomFleetMachine(SimulationGroup simulationGroup, MachineProgram<?> machineProgram) {
        super(simulationGroup, machineProgram, "");
    }

    private long lastHeavyUpdate;

    @Override
    public void update() throws FSMException {
        super.update();
        updatePositions();
        if (lastHeavyUpdate + 10000 > System.currentTimeMillis()) {
            lastHeavyUpdate = System.currentTimeMillis();
            updateTargets();
        }
    }

    private void updateTargets() throws FSMException {
        //check if any members can see the entity
        for (Sendable s : GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().values()) { //TODO this is a garbage-bruteforce system, build spacial hashgrid
            if (!(s instanceof ManagedUsableSegmentController))
                continue;
            ManagedUsableSegmentController target = (ManagedUsableSegmentController)s;
            if (!target.isDocked() && canGroupSee(target))
                targets.add(target);
        }

    }

    /**
     * is target visible to any craft of the group
     * @param target
     * @return
     * @throws FSMException
     */
    private boolean canGroupSee(ManagedUsableSegmentController target) throws FSMException {
        for (String member: members_to_sector.keySet()) {
            if (canSee(member,target))
                return true;
        }
        return false;
    }

    /**
     * can this groupmember see the target. checks sector distance and target.isHidden
     * @param member
     * @param target
     * @return
     * @throws FSMException
     */
    private boolean canSee(String member, ManagedUsableSegmentController target) throws FSMException {
        if (!members_to_sector.containsKey(member))
            throw new FSMException("invalid member in FSM group");

        //TODO make scan tests and jammer tests etc
        if (target.isHidden())
            return false;

        if (Utility.getDistance(members_to_sector.get(member),target.getSector(new Vector3i())) > 1.5f) //!direct neighbour sector
            return false;

        return true;
    }

    /**
     * updates the hashmap of group members and their sectors.
     * @throws FSMException
     */
    private void updatePositions() throws FSMException {
        if (!(getObj() instanceof SimulationGroup))
            throw new FSMException("object for custom pirate machine is not a simulation group.");
        SimulationGroup group = (SimulationGroup) getObj();
        members_to_sector.clear();
        for (int i = 0; i < group.getMembers().size(); i++) {
            Vector3i shipPos = new Vector3i();
            try {
                shipPos = group.getSector(group.getMembers().get(i), shipPos);
                members_to_sector.put(group.getMembers().get(i),shipPos);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (EntityNotFountException e) {
                throw new FSMException("member not found in FSM");
            }
        }
    }

    @Override
    public void createFSM(String s) {
        Transition t_moveToSector = Transition.MOVE_TO_SECTOR;
        Transition t_restart = Transition.RESTART;
        Transition t_plan = Transition.PLAN;
        Transition t_disband = Transition.DISBAND;
        Transition t_waitCompleted = Transition.WAIT_COMPLETED;
        Transition t_targetSectorReached = Transition.TARGET_SECTOR_REACHED;

        AiEntityStateInterface gObj = getObj();

        Starting starting = new Starting(gObj);
        MovingToSector movingToSectorPlayer = new MovingToSector(gObj); //move to closest player
        MovingToSector movingToSectorHome = new MovingToSector(gObj);
        Disbanding disbanding = new Disbanding(gObj);
        CheckingForPlayers checkingForPlayers = new CheckingForPlayers(gObj); //set closest player sector as target
        ReturningHome returningHome = new ReturningHome(gObj);
        GoToRandomSector goToRandomSector = new GoToRandomSector(gObj);
        WaitingTimed waitingInTargetSector = new WaitingTimed(gObj, 60);
        RequestSurrender requestSurrender = new RequestSurrender(gObj,30000);


        starting.addTransition(t_restart, starting);
        starting.addTransition(t_plan, checkingForPlayers);

        checkingForPlayers.addTransition(t_restart, starting);
        checkingForPlayers.addTransition(t_disband, disbanding);
        checkingForPlayers.addTransition(t_moveToSector, movingToSectorPlayer);

        movingToSectorPlayer.addTransition(t_restart, starting);
    //    movingToSectorPlayer.addTransition(t_targetSectorReached, waitingInTargetSector);
        movingToSectorPlayer.addTransition(t_targetSectorReached, requestSurrender);
        requestSurrender.addTransition(Transition.NO_TARGET_FOUND,goToRandomSector);
 //       requestSurrender.addTransition(Transition.TARGET_OUT_OF_RANGE, )

        waitingInTargetSector.addTransition(t_restart, starting);
        waitingInTargetSector.addTransition(t_waitCompleted, returningHome);

        returningHome.addTransition(t_restart, starting);
        returningHome.addTransition(t_moveToSector, movingToSectorHome);

        movingToSectorHome.addTransition(t_restart, starting);
        movingToSectorHome.addTransition(t_targetSectorReached, disbanding);

        disbanding.addTransition(t_restart, starting);
        disbanding.addTransition(t_waitCompleted, goToRandomSector);

        goToRandomSector.addTransition(t_restart, starting);
        goToRandomSector.addTransition(t_moveToSector, movingToSectorHome);
        goToRandomSector.addTransition(t_disband, disbanding);

        setStartingState(starting);
    }

    @Override
    public void onMsg(Message message) {

    }

    enum FireMode{ //how to react when encountering enemy
        open_fire,
        return_fire,
        hold_fire
    }

    enum CombatMode{ //TODO nochmal überdenken was genau hier wichtig ist. //controls fleet movement when enemies are detected
        ignore,
        evade, //find route thats free of enemies
        attack, //
        stealth
    }
}
