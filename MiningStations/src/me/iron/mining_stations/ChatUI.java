package me.iron.mining_stations;

import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
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
        int[] args = getArgs(key);
        try {
            if (key.contains(" ")) {
                String[] split = key.split(" ");
                key = split.length>0?split[0]:key;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    //    key = key.split(" ")[0];
        switch (key) {
            case "save":
                StationManager.loadFromPersistent();
                sendMssg(sender,"saving to persistent data");
                return true;

            case "load":
                StationManager.saveToPersistent();
                sendMssg(sender,"loading from persistent data");

                return true;

            case "set_station":
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

            case "set_asteroid":
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
            case "clear_station": //unregister miner
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
                if (StationManager.isRegistered(selected.getUniqueIdentifier())) {
                    ElementCountMap ecm = MiningUtil.getResources(selected, MiningConfig.passive_mining_bonus.getValue());
                    sendMssg(sender,"Registered Asteroid: " + ecm.getAmountListString() + "total Volume: " + ecm.getVolume());
                    return true;
                }
                sendMssg(sender,"Not registered with PassiveMining.");
                return false;
            case "clear_all":
                StationManager.miners.clear();
                StationManager.roidsByMiner.clear();
                sendMssg(sender,"deleting miners and roids in heap");
                return true;

            case "print_all":
                StringBuilder s = new StringBuilder("MINERS ("+StationManager.miners.size()+"): \n");
                for (String UID : StationManager.miners.keySet()) {
                    s.append(StationManager.miners.get(UID).toString());
                }
                s.append("----------");
                sendMssg(sender, s.toString());
                return true;

            case "print_config":
                StringBuilder string = new StringBuilder("MINING CONFIG : \n");
                boolean desc = false;
                if (args.length==1)
                    desc = args[0]==1;
                for (MiningConfig entry: MiningConfig.values()) {
                    string.append(entry.name()).append("(key: ").append(entry.getKey()).append("): ").append(entry.getValue()).append("\n ");
                    if (desc)
                        string.append(entry.getDescription()).append("\n");
                }
                sendMssg(sender, string.toString());
                return true;

            case "set_config":
                if (args==null||args.length!=2) {
                    sendMssg(sender,"wrong input format. must be 'set_config key value'");
                    return false;
                }

                int out = MiningConfig.setValue(args[0],args[1]);
                if (out != 0) {
                    sendMssg(sender,"could not set config value. unknown key.");
                } else {
                    sendMssg(sender,"set value to " + args[1] + "for " + MiningConfig.getName(args[0]));
                }
                return true;

            case "delete_save":
                sendMssg(sender,"deleting persistent savefile.");
                StationManager.destroySaveFile();
                return true;

            default:
                sendMssg(sender,"unrecognized command.");
                return false;
        }
    }

    private static int[] getArgs(String input) {
        //input = input.replace(" ","");
        if (!input.contains(" "))
            return  new int[0];
        String[] vars = input.split(" ");
        if(vars.length == 0) {
            return new int[0];
        }
        int[] args = new int[vars.length-1];
        for (int i = 0; i < vars.length-1; i++) {
            try {
                args[i] = Integer.parseInt(vars[i+1]);
            } catch (NumberFormatException ex) {
            }
        }
        return args;
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
