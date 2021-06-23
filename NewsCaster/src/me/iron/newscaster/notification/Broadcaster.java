package me.iron.newscaster.notification;

import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.notification.infoTypes.GenericInfo;
import me.iron.newscaster.notification.infoTypes.ShipDestroyedInfo;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 23.06.2021
 * TIME: 13:08
 */
public class Broadcaster {
    /**
     * time between broadcasts in millis
     */
    public static int broadcastLoopTime;

    /**
     * if true, system will automatically broadcast new events
     */
    public static boolean autobroadcast = true;

    /**
     * Initializes a loop that prints interesting news to the chat.
     */
    public static void initBroadcastingCycle(int time) {
        broadcastLoopTime = time;
        new StarRunnable() {
            private long lastBroadcast = System.currentTimeMillis() + 30000;
            private GenericInfo info;
            private boolean timeToBroadcast = true;
            @Override
            public void run() {
                if (!autobroadcast) {
                    return;
                }
                //TODO bundle multiple events into one broadcast
                //get latest event, broadcast to chat.
                info = NewsManager.getInfo(-1);
                if (info == null) {
                    return;
                }
                assert info != null;
                if (System.currentTimeMillis() > lastBroadcast + broadcastLoopTime) {
                    timeToBroadcast = true;
                }
                if (info.getTime() > lastBroadcast + broadcastLoopTime && timeToBroadcast) {
                    timeToBroadcast = false;
                    lastBroadcast = System.currentTimeMillis();
                    //was created after last broadcast.
                    broadcastToChat(info);
                }
            }
        }.runTimer(ModMain.instance, 10);
    }

    public static void broadcastToChat(GenericInfo info) {
        DebugFile.log("broadcasting message for event" + info.getTime());
        if (info instanceof ShipDestroyedInfo) {
            ShipDestroyedInfo dInfo = (ShipDestroyedInfo) info;
            if (dInfo.getAttacker().getFaction().equals("Pirates")) {
                ModPlayground.broadcastMessage("Pirates claim yet another victim ("+dInfo.getShip().getMass()+"k) from ["+dInfo.getShip().getFaction()+"] in "+getSystemName(dInfo.getSector(),true)+"!");
                return;
            }
            ModPlayground.broadcastMessage("["+dInfo.getShip().getFaction() + "] lost a " + dInfo.getShip().getMass() + "k ship to [" + dInfo.getAttacker().getFaction()+"] in "+getSystemName(dInfo.getSector(),true)+".");
            return;
        }
        ModPlayground.broadcastMessage(info.getNewscast(),false);
    }

    /**
     * returns the name of the starsystem the sector is in
     * @param sector
     * @return
     */
    public static String getSystemName(Vector3i sector, boolean includePos) {
        String s = "";
        try {
            //get system coords
            Vector3i sysCoord = GameServerState.instance.getUniverse()
                    .getStellarSystemFromSecPos(sector).getPos();

            //convert to relative pos
            Vector3i relPos = new Vector3i();
            Galaxy.getLocalCoordinatesFromSystem(sysCoord,relPos);

            Galaxy g = GameServerState.instance.getUniverse().getGalaxyFromSystemPos(sysCoord);
            //get name from universe
            s += g.getName(relPos);
            if (includePos) {
                s += " " +sysCoord.toString();
            }
            return s;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return "unknown system";
    }
}
