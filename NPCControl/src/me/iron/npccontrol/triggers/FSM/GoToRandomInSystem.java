package me.iron.npccontrol.triggers.FSM;

import me.iron.npccontrol.triggers.Utility;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Universe;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.simpirates.states.SimulationGroupState;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.stateMachines.FSMException;
import org.schema.schine.ai.stateMachines.Transition;

import java.sql.SQLException;
import java.util.Random;
import java.util.Vector;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.12.2021
 * TIME: 19:39
 * go to a random sector in the current targetsectors system.
 */
public class GoToRandomInSystem extends SimulationGroupState<SimulationGroup> {
    public GoToRandomInSystem(AiEntityStateInterface aiEntityStateInterface) {
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
        SimulationGroup group = getSimGroup();

        if (!(group.getCurrentProgram() instanceof TargetProgram)) {
            stateTransition(Transition.RESTART);
        }
        Vector3i target = null;
        TargetProgram t = ((TargetProgram)group.getCurrentProgram());
        if (t.getSectorTarget() == null) {
            for (String s: group.getMembers()) {
                try {
                    target = group.getSector(s,new Vector3i());
                    break;
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                } catch (EntityNotFountException e) {
                    e.printStackTrace();
                }
            }
            if (target == null) {
                stateTransition(Transition.RESTART);
                return false;
            }

        } else {
            target = new Vector3i(t.getSectorTarget());
        }
        target.x=(target.x/VoidSystem.SYSTEM_SIZE)*VoidSystem.SYSTEM_SIZE + Universe.getRandom().nextInt(VoidSystem.SYSTEM_SIZE*2)-VoidSystem.SYSTEM_SIZE;
        target.y=(target.y/VoidSystem.SYSTEM_SIZE)*VoidSystem.SYSTEM_SIZE + Universe.getRandom().nextInt(VoidSystem.SYSTEM_SIZE*2)-VoidSystem.SYSTEM_SIZE;
        target.z=(target.z/VoidSystem.SYSTEM_SIZE)*VoidSystem.SYSTEM_SIZE + Universe.getRandom().nextInt(VoidSystem.SYSTEM_SIZE*2)-VoidSystem.SYSTEM_SIZE;

        Vector3i system = target;
        system.x = system.x/VoidSystem.SYSTEM_SIZE;
        system.y = system.y/VoidSystem.SYSTEM_SIZE;
        system.z = system.z/VoidSystem.SYSTEM_SIZE;
        Vector3i systemCenter = new Vector3i(VoidSystem.SYSTEM_SIZE_HALF,VoidSystem.SYSTEM_SIZE_HALF,VoidSystem.SYSTEM_SIZE_HALF);
        double dist = Utility.getDistance(target,systemCenter);
        if (dist>=3) {
            //more than 3 sectors away from system center (where stars are)
            t.setSectorTarget(target);

            stateTransition(Transition.MOVE_TO_SECTOR);
            return false;
        }

        int systemType = GameServerState.instance.getUniverse().getGalaxyFromSystemPos(system).getSystemType(system);
        boolean isStar = systemType == Galaxy.TYPE_DOUBLE_STAR || systemType == Galaxy.TYPE_GIANT || systemType == Galaxy.TYPE_SUN;
        if (!isStar) { //target is near center, but its not a star. free to move.
            t.getSectorTarget().set(target);
            stateTransition(Transition.MOVE_TO_SECTOR);
        }
        //repeat until safe position is found.
        return false;
    }
    public static void main(String[] args) {
        Vector3i target = new Vector3i(15,1,-55);
        Random r = new Random(3);
        target.x=(target.x/VoidSystem.SYSTEM_SIZE)*VoidSystem.SYSTEM_SIZE;//+ r.nextInt(VoidSystem.SYSTEM_SIZE*2)-VoidSystem.SYSTEM_SIZE;
        target.y=(target.y/VoidSystem.SYSTEM_SIZE)*VoidSystem.SYSTEM_SIZE;//+ r.nextInt(VoidSystem.SYSTEM_SIZE*2)-VoidSystem.SYSTEM_SIZE;
        target.z=(target.z/VoidSystem.SYSTEM_SIZE)*VoidSystem.SYSTEM_SIZE;//+ r.nextInt(VoidSystem.SYSTEM_SIZE*2)-VoidSystem.SYSTEM_SIZE;

        System.out.println(target);
    }

}
