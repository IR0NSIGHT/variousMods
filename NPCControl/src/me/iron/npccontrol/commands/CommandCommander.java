package me.iron.npccontrol.commands;

import api.DebugFile;
import api.mod.StarLoader;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 20.04.2021
 * TIME: 21:27
 */
public class CommandCommander {
    public static void init() {
        DebugFile.log("command commander init");
        StarLoader.registerCommand(new StationCommand());
    }
}
