package me.iron.npccontrol;

import api.DebugFile;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.04.2021
 * TIME: 22:36
 */
public class ModMain extends StarMod {
    public static StarMod instance;
    public ModMain() {
        super();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        DebugFile.log("npc tester was activated");
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
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        Debugger.addChatListener();
        NPCStationReplacer.init();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
        Debugger.positionLogger();

    }
}
