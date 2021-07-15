package me.iron.mining_stations;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.07.2021
 * TIME: 14:33
 */
public class ModMain extends StarMod {
    public ModMain() {
        super();
        this.instance = this;
    }
    public static StarMod instance;
    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent serverInitializeEvent) {
        super.onServerCreated(serverInitializeEvent);
        StationManager.init();
        ChatUI.init();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent clientInitializeEvent) {
        super.onClientCreated(clientInitializeEvent);
    //    Debug.init();
    }

    @Override
    public FileConfiguration getConfig(String s) {
        return super.getConfig(s);
    }

}
