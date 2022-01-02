package me.iron.npccontrol.triggers;

import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.FSM.GoToRandomInSystem;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.ai.program.common.states.WaitingTimed;
import org.schema.game.server.ai.program.simpirates.PirateSimulationProgram;
import org.schema.game.server.ai.program.simpirates.SimulationProgramInterface;
import org.schema.game.server.ai.program.simpirates.TradingRouteSimulationMachine;
import org.schema.game.server.ai.program.simpirates.states.GoToRandomSector;
import org.schema.game.server.ai.program.simpirates.states.MovingToSector;
import org.schema.game.server.ai.program.simpirates.states.Starting;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.simulation.SimPrograms;
import org.schema.game.server.data.simulation.SimulationManager;
import org.schema.game.server.data.simulation.groups.AttackSingleEntitySimulationGroup;
import org.schema.game.server.data.simulation.groups.ShipSimulationGroup;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
import org.schema.game.server.data.simulation.jobs.SimulationJob;
import org.schema.schine.ai.AiEntityStateInterface;
import org.schema.schine.ai.MachineProgram;
import org.schema.schine.ai.stateMachines.*;

import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.12.2021
 * TIME: 13:54
 */
public class Utility {
    public static ArrayList<PlayerState> getPlayersByDistance(final Vector3i sector) {
        ArrayList<PlayerState> ps = new ArrayList(GameServerState.instance.getPlayerStatesByName().values());
        Collections.sort(ps, new Comparator<PlayerState>() {
            @Override
            public int compare(PlayerState o1, PlayerState o2) {
                return (int)(getDistance(o1.getCurrentSector(),sector)*10 - getDistance(o2.getCurrentSector(),sector)*10);
            }
        });
        return ps;
    }

    /**
     * euklidean standard norm for vectors = distance between a and b
     * @param a
     * @param b
     * @return distance
     */
    public static double getDistance(Vector3i a, Vector3i b) {
        return Math.sqrt((a.x-b.x)*(a.x-b.x) + (a.y-b.y)*(a.y-b.y) + (a.z-b.z)*(a.z-b.z));
    }

    public static SimulationGroup spawnHuntingGroup(SimpleTransformableSendableObject target, int factionID , int ships) {
        return GameServerState.instance.getSimulationManager().sendToAttackSpecific(target,factionID,ships);
    }

    /**
     *
     * @param spawnPos intitial postion for spawning, will get shifted to closest unloaded sector nearby.
     * @param target target which is to be hunted
     * @return NULL //TODO return AI group
     */
    public static ShipSimulationGroup spawnAdvancedHunt(final Vector3i spawnPos, SimpleTransformableSendableObject target, CatalogPermission[] bps, final int factionID) {
        //final Vector3i spawnPos = new Vector3i(p.getCurrentSector());
        final GameServerState state = GameServerState.instance;
        final String targetUID = target.getUniqueIdentifier();

        //create a job for the simulationManager to execute
        final SimulationJob simJob = new SimulationJob() {
            @Override
            public void execute(SimulationManager simMan) {
                Vector3i unloadedPos = simMan.getUnloadedSectorAround(spawnPos,new Vector3i());
                //create group
                final ShipSimulationGroup group = new AttackSingleEntitySimulationGroup(state,unloadedPos, targetUID);;

                simMan.addGroup(group);
                //spawn members
                CatalogPermission[] bps = simMan.getBlueprintList(3,1,factionID); //TODO advanced blueprint
                if (bps.length == 0) {
                    new NullPointerException("no blueprints avaialbe for faction " + factionID).printStackTrace();
                    return;
                }
                group.createFromBlueprints(unloadedPos,simMan.getUniqueGroupUId(),factionID,bps); //seems to try and work but doesnt spawn stuff?
                //add program to group
                TargetProgram<SimulationGroup> p = new PirateSimulationProgram(group,false);//new CustomPirateProgram(group,false);
                group.setCurrentProgram(p);
                AIManager.groups.add(group);
                ModMain.log("spawned advanced hunt:"+group.getDebugString());

            }

        };
        state.getSimulationManager().addJob(simJob); //adds job, is synchronized.
        return null;
    }

    public static CatalogPermission getBlueprintByName(String UID) {  CatalogPermission p = new CatalogPermission();
        p.setUid(UID);
        ArrayList<CatalogPermission> pms = new ArrayList<>(GameServerState.instance.getCatalogManager().getCatalog());
        int idx = pms.indexOf(p);
        if (idx == -1)
        return null;
        return pms.get(idx);
    }

    public static String catalogToString() {
        StringBuilder b = new StringBuilder("Catalog:\n");
        for (CatalogPermission p: GameServerState.instance.getCatalogManager().getCatalog()) {
            b.append(p.toString()).append("\n");
        }
        return b.toString();
    }

    /**
     *
     * @param code
     * @param part 0..7
     * @return
     */
    public static int get8bit(long code, int part) {
            //last 8 bits
            return (int)((code>>(8*part))&255L);
    }

    /**
     * overwrites 8 bits starting at 8*part bit in the code with given bits.
     * @param code code
     * @param bits bits with new info at 0..7
     * @param part
     * @return
     */
    public static long add8bit(long code, long bits, int part) {
        bits = bits&255; //only allow last 8 bits of new info
        bits = bits<<(8*part); //shift to correct position
        code = code&~(255<<part); //clear old info, mask is 11111...00000000..1111
        code = code|bits; //add new info to code
        return code;
    }

    private static Random r = new Random();

    public static Random getRand() {
        return r;
    }

    public static long addFaction(long in, int factionID) {
        switch (factionID) {
            case 0: //neutral
                return in|1;
            case -1: //pirates
                return in|(1<<1);
        }
        if (factionID < 0) { //is NPC
            return in|(1<<2);
        } else if (factionID > 10000){ //is player
            return in|(1<<3);
        }
        return in;
    }

    public static long addEntityType(long in, SimpleTransformableSendableObject.EntityType type) {
        int s = 8;
        switch (type) {
            case SHIP:                    break;
            case SPACE_STATION:           s += 1;break;
            case ASTEROID_MANAGED:
            case ASTEROID:
                s += 2; break;
            case ASTRONAUT:               s += 3; break;
            case SUN:                     s += 4; break;
            case BLACK_HOLE:              s += 5;break;
            case SHOP:                    s += 6; break;
            case PLANET_CORE:
            case PLANET_ICO:
            case PLANET_SEGMENT:
                s += 7;break;
            default:return in;
        }
        return in|1<<s;

    }

    public static String toBin(long code) {
        String o = Long.toBinaryString(code);
        return String.format("%1$" + 64 + "s", o).replace(' ', '0');
    }

}
