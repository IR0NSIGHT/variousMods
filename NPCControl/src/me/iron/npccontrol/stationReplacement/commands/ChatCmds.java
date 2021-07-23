package me.iron.npccontrol.stationReplacement.commands;

import java.util.HashMap;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.07.2021
 * TIME: 23:44
 */
public enum ChatCmds {
    PREFIX("!rpc ","this prefix has to be in front of all NPC replacer chat commands.","!rpc"),
    LIST("list","list all or a factions replacers. optional: faction ID","list, list -1"),
    ADD_BP("add_bp","adds a blueprint to the factions replacer","add_bp MyCoolStation -1"),
    REMOVE_BP("rmv_bp","removes a blueprint from the factions replacer","rmv_bp MyCoolStation -1"),
    CLEAR_BP("clear_bp","clears all blueprints from the factions replacer","clear_bp -1"),
    ADD_RPC("add_rpc","adds a replacer to this faction","add_rpc -1"),
    RM_RPC("rmv_rpc","removes replacer from this faction","rmv_rpc -1"),


    SAVE("save","saves the current replacers persistently to a savefile.","save"),
    LOAD("load","loads replacers from the persistent savefile","load"),
    HELP("help","get help about this mod","help"),
    REPLACE("replace","replace the selected station with this blueprint.","replace MyNewStation");

    private final String cmd;
    private final String desc;
    private final String exampleSyntax;
    ChatCmds(String cmd, String desc, String exp) {
        this.cmd = cmd;
        this.desc = desc;
        this.exampleSyntax = exp;
    }

    public String getCmd() {
        return cmd;
    }

    public String getDesc() {
        return desc;
    }

    public String getSyntax() {
        return PREFIX.getCmd() + exampleSyntax;
    }

    private static HashMap<String,ChatCmds> by_cmd = new HashMap<>();
    static {
        for (ChatCmds c: ChatCmds.values()) {
            by_cmd.put(c.cmd,c);
        }
    }
    public static ChatCmds getByCmd(String cmd) {
        return by_cmd.get(cmd);
    }
    public static String getAllHelp() {
        StringBuilder s = new StringBuilder();
        for (ChatCmds c: ChatCmds.values()) {
            s.append(PREFIX.cmd).append(c.exampleSyntax).append(": ").append(c.desc);
        }
        return s.toString();
    }
}
