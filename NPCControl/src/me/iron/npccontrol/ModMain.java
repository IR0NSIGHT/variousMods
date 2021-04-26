package me.iron.npccontrol;

import api.DebugFile;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarMod;
import me.iron.npccontrol.stationReplacement.*;
import me.iron.npccontrol.stationReplacement.commands.CommandCommander;


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
        DebugFile.log("on enable",this);

        StationReplacer.loadPersistentAll(); //load persistent data for pirates
        CommandCommander.init();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        StationReplacer.savePersistentAll();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        DebugFile.log("server created",this);
        StationReplacer.deployListener();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
        DebugFile.log("on client",this);

    }
}
