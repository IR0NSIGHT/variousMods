package me.iron.mining_stations;

import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.07.2021
 * TIME: 14:40
 */
public class ChatUI {
    public static void init() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                PlayerState sender = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender);
                if (!event.isServer()) return;
                if (!sender.isAdmin()) return;
                String mssg = event.getText();
                if (!mssg.contains("!station")) return;
                mssg = mssg.replace("!station ", "");
                event.setCanceled(true);

                //make miner
                if (mssg.contains("set")) {
                    SegmentController sc = null;
                    try {
                        sc = (SegmentController) sender.getFirstControlledTransformableWOExc();
                        if (sc == null) return;
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendMssg(sender, "FAILED: \n" + StationManager.getMinerConditions());
                    }
                    if (StationManager.makeMiner(sc) == null) {
                        sendMssg(sender, "SUCCESSFULLY REGISTERED AS MINER: " + sc.getName());
                    } else {
                        sendMssg(sender, "FAILED: \n" + StationManager.getMinerConditions());
                    }
                    return;
                }

                if (mssg.contains("list")) {
                    StringBuilder s = new StringBuilder("MINERS: \n");
                    for (String UID : StationManager.miners.keySet()) {
                        s.append(UID).append("\n");
                    }
                    s.append("----------");
                    sendMssg(sender, s.toString());
                    return;
                }

                if (mssg.contains("assign")) {
                    Sendable selected = GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(sender.getSelectedEntityId());
                    if (selected == null || !(selected instanceof SegmentController)) {
                        sendMssg(sender, "not floating rock managed: " + ((selected == null) ? "null" : selected.getClass().toString()));
                        return;
                    }
                    if (StationManager.miners.size() == 0) {
                        sendMssg(sender,"no miners are registered");
                        return;
                    }
                    Miner m = StationManager.miners.values().iterator().next();
                    m.roidUID = null;
                    StationManager.asteroids.clear();
                    boolean success = m.registerAsteroid((SegmentController) selected);
                    if (success) {
                        sendMssg(sender, "assigned roid , total volume: " + Math.round(m.resources.getVolume()));
                    } else {
                        sendMssg(sender,"could not assign roid.");
                    }
                    return;
                }

                //default expection
                sendMssg(sender, "not a mining valid command");
            }
        }, ModMain.instance);
    }

    public static void sendMssg(PlayerState receiver, String mssg) {
        receiver.sendServerMessage(new ServerMessage(Lng.astr(mssg), ServerMessage.MESSAGE_TYPE_SIMPLE, receiver.getId()));
    }

    public static void sendAll(String mssg) {
        for (PlayerState p : GameServerState.instance.getPlayerStatesByName().values()) {
            sendMssg(p, mssg);
        }
    }
}
