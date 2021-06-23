package me.iron.npccontrol;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerCustomCommandEvent;
import api.mod.StarLoader;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 09.06.2021
 * TIME: 13:11
 */
public class CommandListener {
    public static void addListener() {
        StarLoader.registerListener(PlayerCustomCommandEvent.class, new Listener<PlayerCustomCommandEvent>() {
            @Override
            public void onEvent(PlayerCustomCommandEvent event) {
                System.err.println("custom player command was called: "+ event.toString());
                ModPlayground.broadcastMessage("custom command: " + event.toString());
            }
        },ModMain.instance);
    }
}
