package me.iron.newscaster;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.game.PlayerUtils;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.09.2021
 * TIME: 13:57
 */
public class debugUI {
    public static void chatListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                ModPlayground.broadcastMessage("chat event fired");

                if (event.getText().contains("ping")) {
                    for (PlayerState p: GameServerState.instance.getPlayerStatesByName().values()) {
                        PlayerUtils.sendMessage(p,"pong");
                    }
                }
            }
        }, ModMain.instance);
    }
}
