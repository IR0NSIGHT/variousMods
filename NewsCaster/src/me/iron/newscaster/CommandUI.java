package me.iron.newscaster;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import me.iron.newscaster.notification.broadcasting.Broadcaster;
import me.iron.newscaster.notification.infoGeneration.NewsManager;
import me.iron.newscaster.notification.infoGeneration.infoTypes.GenericInfo;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.server.ServerMessage;

import java.util.Arrays;
import java.util.Map;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 17:41
 */

/**
 * debug stuff like printing the newsStorage into the chat, runs 100% serverside
 */
public class CommandUI {
    public static void addListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent playerChatEvent) {
                if (!playerChatEvent.isServer()) { //shouldnt happen bc EH is serverside
                    return;
                }

                PlayerState sender = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(playerChatEvent.getMessage().sender);
                if (sender == null || !sender.isAdmin()) {
                    return;
                }

                if (playerChatEvent.getText().contains("!news all")) {
                    sendMssg(sender,"listing all news:");
                    for (GenericInfo info: NewsManager.getNewsStorage()) {
                        String report = info.getNewscast();
                        sendMssg(sender,info.getNewscast() + "systemname:" + Broadcaster.getSystemName(info.getSector(),true));
                        DebugFile.log(info.getNewscast());
                    }
                    playerChatEvent.setCanceled(true);return;
                }
                if (playerChatEvent.getText().contains("!news save")) {
                    sendMssg(sender,"saving news.");
                    NewsManager.saveToPersistentUtil();
                    playerChatEvent.setCanceled(true);return;
                }
                if (playerChatEvent.getText().contains("!news load")) {
                    sendMssg(sender,"loading news from persistent data, overwriting runtime list");
                    NewsManager.loadFromPersistentUtil();
                    playerChatEvent.setCanceled(true);return;
                }
                if (playerChatEvent.getText().contains("!news clean")) {
                    String mssg = playerChatEvent.getText().replace("!news clean","");
                    String answer = "Deleting: ";
                    if (mssg.contains("all")) {
                        NewsManager.cleanPersistentInfo();
                        answer+= "persistent info, ";
                    }
                    answer += "runtime info";
                    NewsManager.cleanLoadedInfo();
                    sendMssg(sender,answer);
                    playerChatEvent.setCanceled(true);
                    playerChatEvent.setCanceled(true);return;
                }
                if (playerChatEvent.getText().contains("!news flush")) {
                    Broadcaster.flushQueue();
                    playerChatEvent.setCanceled(true);return;
                }
                if (playerChatEvent.getText().contains("!news info")) {
                    sendMssg(sender,NewsManager.getNewsStorage().size() + " news in storage. \n" +
                            " news cycle for broadcasting: " + Broadcaster.broadcastLoopTime/1000 + " seconds. \n" +
                            " threshold causing autoflush: " + Broadcaster.threshold
                    );
                    playerChatEvent.setCanceled(true);return;
                }

                if (playerChatEvent.getText().contains("!news set ")) {
                    String input = playerChatEvent.getText().replace("!news set ","");
                    input = input.replace(" ","");
                    String[] vars = input.split(":");
                    String s = " setting config value: "; //TODO implement
                    if(vars.length != 2) {
                        sendMssg(sender,"invalid input, expects: path: value");
                        playerChatEvent.setCanceled(true);return;
                    }
                    //test if valid entry
                    if (!configManager.exists(vars[0])) {
                        sendMssg(sender,"invalid input, entry does not exist. \n" +
                                "Try setting config to default if an update brought new values.");
                        playerChatEvent.setCanceled(true);return;
                    }
                    int value;
                    try {
                        value = Integer.parseInt(vars[1]);
                    } catch (NumberFormatException ex) {
                        sendMssg(sender,"invalid input, value has to be an integer.");
                        playerChatEvent.setCanceled(true);return;
                    }
                    configManager.setValue(vars[0],value);
                    sendMssg(sender,"setting config value to: " + vars[0]  +": " + configManager.getValue(vars[0]));

                    configManager.updateGameValues();
                    playerChatEvent.setCanceled(true);return;
                }

                if (playerChatEvent.getText().contains("!news config default")) {
                    configManager.resetDefault();
                    sendMssg(sender,"set config to its default");
                    playerChatEvent.setCanceled(true);return;
                }

                if (playerChatEvent.getText().contains("!news config")) {
                    String s = "CONFIG: \n";
                    for (Map.Entry<String,Object> e: configManager.getAll().entrySet()) {
                        s += e.getKey() + ": " + e.getValue().toString() + " \n";
                    }
                    sendMssg(sender,s);
                    playerChatEvent.setCanceled(true);return;
                }

                if (playerChatEvent.getText().contains("!news")) {
                    String[] s = new String[]{"!news all","!news clean (optional) all","!news save","!news load","!news flush","!news info","!news config default","!news config","!news set"};
                    sendMssg(sender,"unrecognized command. Available commands are: "+ Arrays.toString(s));
                    playerChatEvent.setCanceled(true);return;
                }


            }
        }, ModMain.instance);
    }
    public static void sendMssg(PlayerState receiver,String mssg) {
        receiver.sendServerMessage(new ServerMessage(Lng.astr(mssg), ServerMessage.MESSAGE_TYPE_SIMPLE, receiver.getId()));
    }
}
