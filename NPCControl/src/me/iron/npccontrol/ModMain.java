package me.iron.npccontrol;

import api.DebugFile;
import api.ModPlayground;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import me.iron.npccontrol.stationReplacement.*;
import me.iron.npccontrol.stationReplacement.commands.CommandCommander;
import org.junit.Test;
import org.schema.game.server.data.GameServerState;


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
    public void onDisable() {
        super.onDisable();
        StationReplacer.savePersistentAll();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        DebugFile.log("server created",this);

    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
        DebugFile.log("on client",this);

    }

    public static void log(String mssg) {
        System.out.println(mssg);
        if (GameServerState.instance != null) {
            DebugFile.log(mssg);
            ModPlayground.broadcastMessage(mssg);
        }
    }
}
