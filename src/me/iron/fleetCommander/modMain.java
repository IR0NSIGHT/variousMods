package me.iron.fleetCommander;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import me.iron.fleetCommander.eventListening.ListenerManager;
import me.iron.fleetCommander.notification.NewsManager;
import me.iron.fleetCommander.testing.DebugChatListener;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 15:41
 */
public class modMain extends StarMod {
    public static StarMod instance;
    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        NewsManager.saveToPersistenUtil();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        new ListenerManager();
        DebugChatListener.addListener();
        NewsManager.loadFromPersistenUtil();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
    }
}
