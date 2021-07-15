package me.iron.mining_stations;

import api.utils.StarRunnable;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;

import java.util.Map;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.07.2021
 * TIME: 16:28
 * runs on a cycle, holds and uses methods to update miners
 */
public class MiningCylce extends StarRunnable {
    long last;
    static int checkFrequency = 1000;
    @Override
    public void run() {
        long diff = System.currentTimeMillis() - last;
        if (diff < checkFrequency) return;
        last = System.currentTimeMillis();
        updateAllMiners(System.currentTimeMillis());
    }

    /**
     * updates all miners, skips unloaded ones and those that havent had enough downtime yet since last update
     * @param time time now
     */
    private static void updateAllMiners(long time) {
        Miner.miner_update_time = 5*1000;
        for (Map.Entry<String,Miner> entry: StationManager.miners.entrySet()) {
            SegmentController sc = GameServerState.instance.getSegmentControllersByName().get(entry.getKey());
            Miner m = entry.getValue();
            if (m.getNextUpdate() > time) continue;

            //test if miner is still an existing entity, remove from list otherwise
            if (sc == null && !MiningUtil.existsInDB(m.getDbID(),m.getUID())) {
                ChatUI.sendAll("doesnt exist: " + entry.getKey());
                StationManager.removeMiner(entry.getKey());
                continue;
            }

            //is loaded?
            if (sc == null) continue;
            updateMiner(time, m);
        }
    }

    /**
     * updates this miner
     * @param time time now
     * @param miner miner to update
     */
    static void updateMiner(long time, Miner miner) {
        SegmentController sc = GameServerState.instance.getSegmentControllersByName().get(miner.getUID());
        if (sc == null) {
            try {
                throw new EntityNotFountException("tried updating miner, cant find SegmentCOntroller");
            } catch (EntityNotFountException e) {
                e.printStackTrace();
            }
            return;
        }
        //check if still valid
        if (!StationManager.validMiner(sc)) {
            StationManager.removeMiner(sc.getUniqueIdentifier());
            ChatUI.sendAll("not a valid miner anymore: " + sc.getUniqueIdentifier());
        }
        miner.update();
        miner.setNextUpdate(time+ Miner.miner_update_time);
    }

}
