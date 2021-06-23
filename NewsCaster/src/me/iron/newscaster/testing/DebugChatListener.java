package me.iron.newscaster.testing;

import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.notification.Broadcaster;
import me.iron.newscaster.notification.NewsManager;
import me.iron.newscaster.notification.infoTypes.EntityInfo;
import me.iron.newscaster.notification.infoTypes.GenericInfo;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

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
                        ModPlayground.broadcastMessage(info.getNewscast() + "systemname:" + Broadcaster.getSystemName(info.getSector(),true));
                    //    ModPlayground.broadcastMessage();
                        DebugFile.log(info.getNewscast());
                    }
                }
                if (playerChatEvent.getText().contains("!news save")) {
                    NewsManager.saveToPersistenUtil();
                }
                if (playerChatEvent.getText().contains("!news load")) {
                    ModPlayground.broadcastMessage("loading news from persistent data, overwriting runtime list");
                    NewsManager.loadFromPersistenUtil();
                }
                if (playerChatEvent.getText().contains("!news clean")) {
                    ModPlayground.broadcastMessage("deleting all news, persistent and runtime");
                    //TODO make confirmation input necessary
                    NewsManager.cleanPersistentInfo();
                }
                //TODO allow selecting specific info
            }
        }, ModMain.instance);
    }
}
