package me.iron.newscaster.notification;

import api.DebugFile;
import api.common.GameServer;
import api.utils.StarRunnable;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.notification.infoTypes.GenericInfo;
import me.iron.newscaster.notification.infoTypes.ShipCreatedInfo;
import me.iron.newscaster.notification.infoTypes.ShipDestroyedInfo;
import me.iron.newscaster.notification.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.RegisteredClientOnServer;
import org.schema.schine.network.server.ServerMessage;

import java.io.IOException;
import java.util.ArrayList;

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
    public static int broadcastLoopTime = 1000*60*5;
    public static int threshold = 5;
    private static long lastBroadcast = System.currentTimeMillis() + 1000;

    private static ArrayList<GenericInfo> queue = new ArrayList<>();
    //removes and returns 0 idx (oldest) entry.
    private static GenericInfo queueShift() {
        return queue.remove(0);
    }

    /**
     * adds info to queue. auto aborts on outdated info.
     * @param info info object to add to queue
     */
    public static void queueAdd(GenericInfo info) {
        if (queue.size() != 0 && queue.get(queue.size()-1).getTime() > info.getTime()) {
            //only allow newer entries than newest.
            return;
        }
        if(info.getTime() < lastBroadcast) {
            //no events older than last broadcast.
            return;
        }
        queue.add(info);


    }
    /**
     * if true, system will automatically broadcast new events
     */
    public static boolean autobroadcast = true;

    /**
     * Initializes a loop that prints interesting news to the chat.
     */
    public static void initBroadcastingCycle() {
        new StarRunnable() {
            private boolean timeToBroadcast = true;
            @Override
            public void run() {
                if (!autobroadcast) {
                    return;
                }
                if (System.currentTimeMillis() > lastBroadcast + broadcastLoopTime) {
                    timeToBroadcast = true;
                }
                if (queue.size() >= threshold || timeToBroadcast) {
                    timeToBroadcast = false;
                    lastBroadcast = System.currentTimeMillis();
                    //was created after last broadcast.
                    flushQueue();
                }
            }
        }.runTimer(ModMain.instance, 10);
    }

    /**
     * flushes broadcasters queue of events out to chat.
     */
    public static void flushQueue() {
        //bundle events
        int length = queue.size();
        String s = "";
        for (int i = 0; i<length; i++) {
            s += broadcastToChat(queueShift()) + "\n";
        }
        if (s.equals("")) return;
        s = "--------- ENN BREAKING NEWS ---------\n " + s +
                "\n-------------------------------------\n";
        sendToAll(s);
    }

    public static String broadcastToChat(GenericInfo info) {
        DebugFile.log("broadcasting message for event" + info.getTime());
        if (info instanceof ShipDestroyedInfo) {
            ShipDestroyedInfo dInfo = (ShipDestroyedInfo) info;
            if (dInfo.getAttacker().getFaction().equals("Pirates")) {
                return "Pirates claim yet another victim ("+dInfo.getShip().getMass()+"k) from ["+dInfo.getShip().getFaction()+"] in "+getSystemName(dInfo.getSector(),true)+"!";

            }
            return("["+dInfo.getShip().getFaction() + "] lost a " + dInfo.getShip().getMass() + "k ship to [" + dInfo.getAttacker().getFaction()+"] in "+getSystemName(dInfo.getSector(),true)+".");
        }
        if (info instanceof ShipCreatedInfo) {
            ShipCreatedInfo sinfo = (ShipCreatedInfo) info;
            ShipObject s = sinfo.getShip();
            if (sinfo.getShip().getFaction().equals("Pirates") || s.getFaction().equals("")) {

            }
            String string = "["+s.getFaction()+"] has deployed a "+getShipType(s.getMass())+" (" + s.getMass() + "k) in " + getSystemName(sinfo.getSector(),true);
            return(string            );

        }
        return(info.getNewscast());
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

    public static String getShipType(int mass) {
        if(mass <=  15) return "Fast Attack Craft";
        if(mass <=  30) return "Escort";
        if(mass <=  50) return "Corvette";
        if(mass <= 100) return "Frigate";
        if(mass <= 200) return "Destroyer";
        if(mass <= 300) return "Cruiser";
        if(mass <= 500) return "Battleship";
        return "Titan";
    }

    private static void sendToAll(String mssg) {
        for(RegisteredClientOnServer client : GameServerState.instance.getClients().values()) {
            PlayerState player = GameServerState.instance.getPlayerStatesByName().get(client.getPlayerName());
            player.sendServerMessage(Lng.astr(mssg),ServerMessage.MESSAGE_TYPE_SIMPLE);
        }
    }
}
