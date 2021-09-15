package me.iron.newscaster.notification.broadcasting;

import api.DebugFile;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import me.iron.newscaster.commandUI.CommandUI;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.configManager;
import me.iron.newscaster.notification.infoGeneration.infoTypes.FactionSystemClaimInfo;
import me.iron.newscaster.notification.infoGeneration.infoTypes.GenericInfo;
import me.iron.newscaster.notification.infoGeneration.infoTypes.ShipCreatedInfo;
import me.iron.newscaster.notification.infoGeneration.infoTypes.ShipDestroyedInfo;
import me.iron.newscaster.notification.infoGeneration.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.network.objects.ChatMessage;
import org.schema.game.server.data.Galaxy;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.RegisteredClientOnServer;

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
    public static int broadcastLoopTime;

    public static int threshold;

    private static long lastBroadcast = System.currentTimeMillis() + 1000;

    private static boolean  broadcast_show_shipname;

    private static boolean  broadcast_show_shiptype;

    private static boolean  broadcast_show_shipmass;

    private static boolean  broadcast_show_shipfaction;

    private static boolean  broadcast_show_pilot;

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
    public static boolean autobroadcast;

    /**
     * Initializes a loop that prints interesting news to the chat.
     * Reads values from config
     * TODO: adds chatcommand interface for broadcaster
     */
    public static void init() {
        //initialize config reading
        configManager.init();
        updateFromConfig();
        //start cycle
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
     * pulls config values, updates variables.
     */
    public static void updateFromConfig()
    {
        try {
            autobroadcast               = configManager.getValue("broadcast_active")==1;
            threshold                   = configManager.getValue("broadcast_threshold");
            broadcastLoopTime           = 1000*configManager.getValue("broadcast_timeout");
            broadcast_show_shipname     = configManager.getValue("broadcast_show_shipname")==1;
            broadcast_show_shiptype     = configManager.getValue("broadcast_show_shiptype")==1;
            broadcast_show_shipmass     = configManager.getValue("broadcast_show_shipmass")==1;
            broadcast_show_shipfaction  = configManager.getValue("broadcast_show_shipfaction")==1;
            broadcast_show_pilot        = configManager.getValue("broadcast_show_pilot")==1;
        } catch (Exception e) {
            DebugFile.log("something went wrong with the config.");
        }

    }

    /**
     * flushes broadcasters queue of events out to chat.
     */
    public static void flushQueue() {
        //bundle events
        int length = queue.size();
        if (length == 0)
            return;

        String s = "";
        for (int i = 0; i<length; i++) {
            s += getReportString(queueShift()) + "\n";
        }
        if (s.equals("")) return;
        s = "--------- ENN BREAKING NEWS ---------\n " + s +
                "-------------------------------------\n";
        sendToAll(s);
    }

    /**
     * generates cool news-string from given info
     * @param info
     * @return
     */
    public static String getReportString(GenericInfo info) {
        DebugFile.log("broadcasting message for event" + info.getTime());
        if (info instanceof ShipDestroyedInfo) {
            ShipDestroyedInfo dInfo = (ShipDestroyedInfo) info;
            String report = "";
            if (dInfo.getAttacker().getFaction().equals("Pirates")) {
                return "Pirates claim yet another victim ("+dInfo.getShip().getMass()+"k"+(dInfo.getShip().isStation?" station":"")+") from ["+dInfo.getShip().getFaction()+"] in "+getSystemName(dInfo.getSector(),true)+"!";
            }
            String shipA = "";
            String type = getShipType(dInfo.getShip().getMass());
            if (!broadcast_show_shipname || dInfo.getShip().getName().equals("")|| dInfo.getShip().getName().length() > 14) {
                shipA = "a " + type+ " ";
            } else {
                shipA = dInfo.getShip().getName()+"("+type+") ";
            }

            String factionA = broadcast_show_shipfaction?"["+dInfo.getShip().getFaction() + "] ":"";
            if (dInfo.getShip().getFaction().equals("")) {
                factionA = "";
            }

            String factionB = dInfo.getAttacker().getFaction();
            if (factionB.equals("[]")) {
                report = shipA + factionA+ " was destroyed ";
            } else {
                report = "["+dInfo.getAttacker().getFaction() + "] destroyed " + shipA + factionA;
            }
            return(report+" in "+getSystemName(dInfo.getSector(),true)+".");
        }
        if (info instanceof ShipCreatedInfo) {
            ShipCreatedInfo sinfo = (ShipCreatedInfo) info;
            ShipObject s = sinfo.getShip();
            String string = "["+s.getFaction()+"] has deployed a "+((s.isStation)?"space station":getShipType(s.getMass())+" (" + s.getMass() + "k)")+" in " + getSystemName(sinfo.getSector(),true);
            return(string);

        }
        if (info.getType().equals(GenericInfo.EventType.SYSTEM_CONQUERED)) {
            String s = "";
            FactionSystemClaimInfo fInfo = (FactionSystemClaimInfo) info;
            s += "[" + fInfo.getFaction().getFactionName() + "] has conquered " + getSystemName(fInfo.getSystem(),true);
            return s;
        }
        if (info.getType().equals(GenericInfo.EventType.SYSTEM_LOST)) {
            String s = "";
            FactionSystemClaimInfo fInfo = (FactionSystemClaimInfo) info;
            s += "[" + fInfo.getOldOwner().getFactionName() + "] no longer controls " + getSystemName(fInfo.getSystem(),true);
            return s;
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

    /**
     * gets a hardcoded shiptype(string) based on the reactorsize of the ship.
     * @param reactorsize reactorsize of ship in k
     * @return
     */
    public static String getShipType(int reactorsize) {
        if(reactorsize <=  15000) return "Fast Attack Craft";
        if(reactorsize <=  30000) return "Escort";
        if(reactorsize <=  50000) return "Corvette";
        if(reactorsize <= 100000) return "Frigate";
        if(reactorsize <= 200000) return "Destroyer";
        if(reactorsize <= 300000) return "Cruiser";
        if(reactorsize <= 500000) return "Battleship";
        return "Titan";
    }

    /**
     * sends string directly to all clients to display as simple chat message, no [SERVER] prefix or similar.
     * @param mssg
     */
    private static void sendToAll(String mssg){
        if (mssg.equals(""))
            return;

        for(RegisteredClientOnServer client : GameServerState.instance.getClients().values()) {
            PlayerState player = GameServerState.instance.getPlayerStatesByName().get(client.getPlayerName());
            CommandUI.sendMssg(player,mssg);
            ChatMessage m = new ChatMessage();
            m.text = mssg;
            m.sender = "Newscaster";
            StarLoader.fireEvent(new PlayerChatEvent(m,null),true);
        }
    }
}
