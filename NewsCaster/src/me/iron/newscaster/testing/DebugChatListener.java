package me.iron.newscaster.testing;

import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.notification.NewsManager;
import me.iron.newscaster.notification.infoTypes.FleetInfo;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 17:41
 */

/**
 * debug stuff like printing the newsStorage into the chat
 */
public class DebugChatListener {
    public static void addListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent playerChatEvent) {
                if (!playerChatEvent.isServer()) {
                    return;
                }
                if (playerChatEvent.getText().contains("news")) {
                    for (FleetInfo info: NewsManager.getNewsStorage()) {
                        ModPlayground.broadcastMessage(info.getNewscast());
                        DebugFile.log(info.getNewscast());
                    }
                }
                if (playerChatEvent.getText().contains("save")) {
                    NewsManager.saveToPersistenUtil();
                }
                if (playerChatEvent.getText().contains("load")) {
                    NewsManager.loadFromPersistenUtil();
                }
                if (playerChatEvent.getText().contains("clean")) {
                    NewsManager.cleanPersistentInfo();
                }
            }
        }, ModMain.instance);
    }
}
