package me.iron.newscaster.testing;

import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.configManager;
import me.iron.newscaster.notification.broadcasting.Broadcaster;
import me.iron.newscaster.notification.infoGeneration.NewsManager;
import me.iron.newscaster.notification.infoGeneration.infoTypes.GenericInfo;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;

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
public class DebugChatListener {
    public static void addListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent playerChatEvent) {
                if (!playerChatEvent.isServer()) {
                    return;
                }
                PlayerState sender = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(playerChatEvent.getMessage().sender);
                if (sender == null || !sender.isAdmin()) {
                    return;
                }
                if (playerChatEvent.getText().contains("!news all")) {
                    ModPlayground.broadcastMessage("listing all news:");
                    for (GenericInfo info: NewsManager.getNewsStorage()) {
                        String report = info.getNewscast();
                        sender.sendServerMessage(Lng.astr(info.getNewscast() + "systemname:" + Broadcaster.getSystemName(info.getSector(),true)), 0);
                        DebugFile.log(info.getNewscast());
                    }
                    return;
                }
                if (playerChatEvent.getText().contains("!news save")) {
                    sender.sendServerMessage(Lng.astr("saving news."),0);
                    NewsManager.saveToPersistenUtil();
                    return;
                }
                if (playerChatEvent.getText().contains("!news load")) {
                    sender.sendServerMessage(Lng.astr("loading news from persistent data, overwriting runtime list"),0);
                    NewsManager.loadFromPersistenUtil();
                    return;
                }
                if (playerChatEvent.getText().contains("!news clean")) {
                    sender.sendServerMessage(Lng.astr("deleting all news, persistent and runtime"),0);
                    //TODO make confirmation input necessary
                    NewsManager.cleanPersistentInfo();
                    return;
                }
                if (playerChatEvent.getText().contains("!news flush")) {
                    Broadcaster.flushQueue();
                    return;
                }
                if (playerChatEvent.getText().contains("!news info")) {
                    sender.sendServerMessage(Lng.astr(NewsManager.getNewsStorage().size() + " news in storage. \n" +
                            " news cycle for broadcasting: " + Broadcaster.broadcastLoopTime/1000 + " seconds. \n" +
                            " threshold causing autoflush: " + Broadcaster.threshold
                    ),0);
                    return;
                }

                if (playerChatEvent.getText().contains("!news set ")) {
                    String input = playerChatEvent.getText().replace("!news set ","");
                    input = input.replace(" ","");
                    String[] vars = input.split(":");
                    String s = " setting config value: "; //TODO implement
                    if(vars.length != 2) {
                        sender.sendServerMessage(Lng.astr("invalid input, expects: path: value"),0);
                        return;
                    }
                    //test if valid entry
                    if (!configManager.exists(vars[0])) {
                        sender.sendServerMessage(Lng.astr("invalid input, entry does not exist."),0);
                        return;
                    }
                    int value;
                    try {
                        value = Integer.parseInt(vars[1]);
                    } catch (NumberFormatException ex) {
                        sender.sendServerMessage(Lng.astr("invalid input, value has to be an integer."),0);
                        return;
                    }
                    configManager.setValue(vars[0],value);
                    sender.sendServerMessage(Lng.astr("set config value: " + vars[0]  +": " + configManager.getValue(vars[0])),0);

                    configManager.updateGameValues();
                    return;
                }

                if (playerChatEvent.getText().contains("!news config default")) {
                    configManager.resetDefault();
                    sender.sendServerMessage(Lng.astr("set config to its default"),0);

                }

                if (playerChatEvent.getText().contains("!news config")) {
                    String s = "CONFIG: \n";
                    for (Map.Entry<String,Object> e: configManager.getAll().entrySet()) {
                        s += e.getKey() + ": " + e.getValue().toString() + " \n";
                    }
                    sender.sendServerMessage(Lng.astr(s),0);
                    return;
                }

                if (playerChatEvent.getText().contains("!news")) {
                    String[] s = new String[]{"!news all","!news clean","!news save","!news load","!news flush","!news info","!news config","!news set"};
                    sender.sendServerMessage(Lng.astr("unrecognized command. Available commands are: "+ Arrays.toString(s)),0);
                    return;
                }


            }
        }, ModMain.instance);
    }
}
