package me.iron.mining_stations;

import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import org.luaj.vm2.ast.Stat;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import javax.validation.constraints.Min;

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
                String mssg = event.getText();
                if (!mssg.contains("!station")) return;
                mssg = mssg.replace("!station ", "");

                Sendable sel = GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(sender.getSelectedEntityId());
                SegmentController selected = sel instanceof SegmentController ? (SegmentController) sel : null;

                SimpleTransformableSendableObject con = sender.getFirstControlledTransformableWOExc();
                SegmentController controlled = (con instanceof SegmentController) ? (SegmentController) con : null;
                if (sender.isAdmin())
                    adminCmds(sender,mssg,controlled,selected);

                event.setCanceled(true);
            }
        }, ModMain.instance);
    }

    private static boolean adminCmds(PlayerState  sender, String key, SegmentController controlled, SegmentController selected) {
        Miner m;
        int code = -1;
        switch (key) {
            case "save":
                StationManager.loadFromPersistent();
                sendMssg(sender,"saving to persistent data");
                return true;

            case "load":
                StationManager.saveToPersistent();
                sendMssg(sender,"loading from persistent data");

                return true;

            case "set station":
                if (selected == null) {
                    sendMssg(sender,"must select something");
                    return false;
                }
                m = StationManager.attemptMakeMiner(selected);
                if (m != null) {
                    sendMssg(sender,"Success: " + m.toString());
                } else {
                    code = StationManager.validMiner(selected);
                    sendMssg(sender,"Failed. " + StationManager.getMinerConditions() + "error: " + code);
                }
                return true;

            case "set asteroid":
                if (selected == null || controlled == null) {
                    sendMssg(sender,"must control miner and select asteroid");
                    return false;
                }
                code =StationManager.validMiner(controlled);
                if (code != 0) {
                    sendMssg(sender,"cant make this controlled entity a miner: " + code);
                    return false;
                }
                m = StationManager.attemptMakeMiner(controlled);
                assert m != null;
                code = m.allowedAsteroid(selected);
                if (0 != code) {
                    sendMssg(sender,"asteroid can not be registered to station." + code); //TODO give asteroid conditions
                }
            case "clear station": //unregister miner
                if (selected == null) {
                    sendMssg(sender,"nothing selected");
                    return false;
                }
                m = StationManager.miners.get(selected.getUniqueIdentifier());
                if (m == null) {
                    sendMssg(sender,"not a miner");
                    return false;
                }
                StationManager.removeMiner(selected.getUniqueIdentifier());
                sendMssg(sender,"removed miner for " + selected.getName());
                return true;

            case "print":
                if (selected == null) {
                    sendMssg(sender,"nothing selected");
                    return false;
                }
                m = StationManager.miners.get(selected.getUniqueIdentifier());
                if (m != null) {
                    sendMssg(sender,"Miner: " + m.toString());
                    return true;
                }
                if (StationManager.asteroids.get(selected.getUniqueIdentifier()) != null) {
                    ElementCountMap ecm = MiningUtil.getResources(selected,config_manager.passive_mining_bonus);
                    sendMssg(sender,"Registered Asteroid: " + ecm.getAmountListString() + "total Volume: " + ecm.getVolume());
                    return true;
                }
                sendMssg(sender,"Not registered with PassiveMining.");
                return false;

            case "print all":
                StringBuilder s = new StringBuilder("MINERS ("+StationManager.miners.size()+"): \n");
                for (String UID : StationManager.miners.keySet()) {
                    s.append(StationManager.miners.get(UID).toString());
                }
                s.append("----------");
                sendMssg(sender, s.toString());
                return true;

            default:
                sendMssg(sender,"unrecognized command.");
                return false;
        }
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
