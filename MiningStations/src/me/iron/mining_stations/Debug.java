package me.iron.mining_stations;

import api.ModPlayground;
import api.utils.StarRunnable;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.schine.network.server.ServerMessage;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.07.2021
 * TIME: 14:55
 */
public class Debug {
    public static void init() {
        new StarRunnable() {
            int i = 0;
            long lastBroadcast = 0;
            @Override
            public void run() {
                try {
                    PlayerState p = GameClientState.instance.getPlayer();
                    if (p == null) return;
                    long diff = System.currentTimeMillis()-lastBroadcast;
                    if (diff < 3000) return;
                    lastBroadcast = System.currentTimeMillis(); i++;
                    SectorInformation.SectorType type = GameClientState.instance.getCurrentRemoteSector().getType();
                    //  = GameClientState.instance.getCurrentClientSystem().getSectorType(p.getCurrentSector());
                    ChatUI.sendMssg(p,"hallo");

                    ModPlayground.broadcastMessage("["+i+"] SECTOR TYPE: " + type.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.runTimer(ModMain.instance,10);
    }
}
