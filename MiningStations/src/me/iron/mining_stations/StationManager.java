package me.iron.mining_stations;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.block.SegmentPieceModifyEvent;
import api.listener.events.block.SegmentPieceSalvageEvent;
import api.listener.events.entity.SegmentControllerFullyLoadedEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.07.2021
 * TIME: 14:40
 */
public class StationManager {
    static HashMap<String, Miner> miners = new HashMap<>();
    static HashSet<String> asteroids = new HashSet<>();
    /**
     * initializes stationmanager. adds listener and update cycle
     */
    public static void init() {
        new MiningCylce().runTimer(ModMain.instance,10);
        StarLoader.registerListener(SegmentControllerFullyLoadedEvent.class, new Listener<SegmentControllerFullyLoadedEvent>() {
            @Override
            public void onEvent(SegmentControllerFullyLoadedEvent event) {
                Miner m = miners.get(event.getController().getUniqueIdentifier());
                if (m == null) return;
                long nextUpdate = m.getNextUpdate();
                if(nextUpdate <= System.currentTimeMillis()) {
                    MiningCylce.updateMiner(System.currentTimeMillis(),m);
                }
            }
        },ModMain.instance);
        //no salvage for registered roids
        StarLoader.registerListener(SegmentPieceSalvageEvent.class, new Listener<SegmentPieceSalvageEvent>() {
            @Override
            public void onEvent(SegmentPieceSalvageEvent event) {
                String hitUID = event.getBlockInternal().getSegmentController().getUniqueIdentifier();
                if (!asteroids.contains(hitUID)) return;
                event.setCanceled(true);
            }
        },ModMain.instance);

    }

    static void removeMiner(String UID) {
        DebugFile.log("removing miner: "+UID);
        ChatUI.sendAll("removed " + UID);
        Miner m = miners.get(UID);
        m.unregisterAsteroid();
        //TODO invoke deletion of crates
        miners.remove(UID);
    }

    //TOdo pretty way to register miner (apart from chat command)0
    /**
     * attempt to make this segmentcontroller a passive miner.
     * @param sc segmentcontroller
     * @return success
     */
    static boolean makeMiner(SegmentController sc) {
        if (!validMiner(sc)) return false;
        Miner m = miners.get(sc.getUniqueIdentifier());
        if (m != null) return true;
        m = new Miner(sc);
        miners.put(sc.getUniqueIdentifier(),m);
        ChatUI.sendAll("made " + sc.getUniqueIdentifier() + " a miner");
        return true;
    }

    /**
     * return string that explains what a station has to offer to be a valid miner.
     * @return string
     */
    static String getMinerConditions() {
        return "Conditions for a valid Miner are: \n" +
                "- Spacestation \n" +
                "- not a homebase \n" +
                "- MORE STUFF"; //TODO more conditions idk
    }

    /**
     * test if station is allowed to be a miner
     * @param sc segmentcontroller
     * @return true/false
     */
    static boolean validMiner(SegmentController sc) {
        if (!(sc instanceof SpaceStation)) return false;
        if (((SpaceStation)sc).isHomeBase()) return false;
        Vector3i sector = sc.getSector(new Vector3i());
        StellarSystem sys;
        try {
            sys = GameServerState.instance.getUniverse().getStellarSystemFromSecPos(sector);
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        sys.getCenterSectorType();
        //SectorInformation.SectorType.
        return true;
    }

}
