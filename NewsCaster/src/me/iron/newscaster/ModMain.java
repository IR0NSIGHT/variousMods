package me.iron.newscaster;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import me.iron.newscaster.eventListening.ListenerManager;
import me.iron.newscaster.notification.Broadcaster;
import me.iron.newscaster.notification.NewsManager;
import me.iron.newscaster.testing.DebugChatListener;

import java.security.ProtectionDomain;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 15:41
 */
public class ModMain extends StarMod {
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
        Broadcaster.initBroadcastingCycle(1000 * 30);
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
    }

}
