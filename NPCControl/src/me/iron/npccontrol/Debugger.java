package me.iron.npccontrol;

import api.ModPlayground;
import api.common.GameClient;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import com.bulletphysics.linearmath.Transform;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;

import javax.vecmath.Vector3f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.04.2021
 * TIME: 19:34
 */
public class Debugger {
    public static void addChatListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                ModPlayground.broadcastMessage("received chat event.");
                if (!event.isServer()) {
                    return;
                }
            }
        },ModMain.instance);
    }

    public static void positionLogger() {
        final Transform playerTransform = new Transform();
        playerTransform.setIdentity();
        final Vector3f pos = new Vector3f();
        new StarRunnable() {
            long lastTime = 0;
            @Override
            public void run() {
                if (!(System.currentTimeMillis() > lastTime + 5000)) {
                    return;
                }
                lastTime = System.currentTimeMillis();

                PlayerState player = GameClient.getClientPlayerState();
                if (player == null) {
                    return;
                }
        //        player.getWordTransform(playerTransform);
                playerTransform.setIdentity();
                player.getWordTransform(playerTransform);
                if (GameClientState.instance.getCurrentPlayerObject() == null) {
                    return;
                }
                playerTransform.origin.set(GameClientState.instance.getCurrentPlayerObject().getWorldTransform().origin);
                pos.set(
                        Math.round(playerTransform.origin.x),
                        Math.round(playerTransform.origin.y),
                        Math.round(playerTransform.origin.z)
                        );
                ModPlayground.broadcastMessage("transform pos: " + pos);
            }
        }.runTimer(ModMain.instance, 50);
    }
}
