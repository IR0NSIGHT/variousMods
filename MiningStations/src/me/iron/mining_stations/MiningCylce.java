package me.iron.mining_stations;

import api.DebugFile;
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

    @Override
    public void run() {
        long diff = System.currentTimeMillis() - last;
        if (diff < MiningConfig.update_check_time.getValue()) return;
        last = System.currentTimeMillis();
        try {
            updateAllMiners(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            DebugFile.log(e.getMessage(),ModMain.instance);
        }
    }

    /**
     * updates all miners, skips unloaded ones and those that havent had enough downtime yet since last update
     * @param time time now
     */
    private static void updateAllMiners(long time) {
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
            m.update();
        }
    }


}
