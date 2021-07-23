package me.iron.npccontrol.stationReplacement.commands;

import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.stationReplacement.StationHelper;
import me.iron.npccontrol.stationReplacement.StationReplacer;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import java.util.Map;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 20.04.2021
 * TIME: 21:30
 */
public class StationCommand implements CommandInterface {
    @Override
    public String getCommand() {
        return "stcn";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"stationcontrol","station"};
    }

    @Override
    public String getDescription() {
        return "command interface for automated station replacement. \n" +
                "subcommands are: station \n" +
                "station add blueprintname factionID \n" +
                "station list \n" +
                "station list factionID \n" +
                "station add_replacer factionID \n" +
                "station save \n" +
                "station load \n" +
                "replace blueprintname \n" +
                "";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState playerState, String[] strings) {
        PlayerUtils.sendMessage(playerState,  "station command was fired, server exists:" + (GameServerState.instance == null));
   //    ModPlayground.broadcastMessage("station command was used!");
   //    echo("station command was used: " + this.toString());
        return true;
    }
    PlayerState sender;
    @Override
    public void serverAction(PlayerState sender, String[] arguments) {
        assert sender != null;
        this.sender = sender;
    //    System.err.println("station command was used by " + sender.getName() + " with args: " + Arrays.toString(arguments));
        if (arguments.length < 1) //[0] is subcommand
            return;
        String cmd = arguments[0];
        ChatCmds c = ChatCmds.getByCmd(cmd);
        if (c == null) {
            echo(  "Command not recognized. Type \n" + ChatCmds.HELP.getSyntax() + "\n for help");
            return;
        }
        switch (c) {
            case ADD_BP: {
                //TODO move to own method, is identiucal with remove_bp
                if (arguments.length < 2) {
                    error(c);
                    return;
                }
                String blueprintName = arguments[1];
                int factionID = getValidFactionID(arguments[2],c);
                if (factionID == 0) return;
                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    echo("no replacer exists for faction " + factionID);
                    return;
                }

                if (replacer.addBlueprint(blueprintName)) {
                    echo("successfully added " + blueprintName + " to faction " + factionID);
                    replacer.savePersistent();
                } else {
                    echo(blueprintName + " is already listed for " + factionID);
                }
                return;
            }
            case REMOVE_BP: {
                if (arguments.length < 2) {
                    echo("syntax error, expects: \n" + c.getSyntax());
                    return;
                }
                String blueprintName = arguments[1];
                int factionID = getValidFactionID(arguments[2],c);
                if (factionID == 0) return;
                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    echo("no replacer exists for faction " + factionID);
                    return;
                }

                if (replacer.removeBlueprint(blueprintName)) {
                    echo(  "successfully removed " + blueprintName + " from faction " + factionID);
                    replacer.savePersistent();
                } else {
                    echo(  blueprintName + " is not listed for " + factionID);
                }
                return;
            }
            case CLEAR_BP: {
                if (arguments.length < 3) { //TODO move into own "getFactionID" method, lots of copy paste here
                    error(c);
                    return;
                }
                String blueprintName = arguments[1];
                int factionID = getValidFactionID(arguments[2],c);
                if (factionID == 0) return;

                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    echo(  "no replacer exists for faction " + factionID);
                    return;
                }

                String list = "";
                for (String bp : replacer.getBlueprints()) {
                    list += bp + "\n";
                    replacer.removeBlueprint(bp);
                }
                echo(  "faction " + factionID + " had removed blueprints: \n" + list);
                replacer.savePersistent();
                return;
            }

            case ADD_RPC: {
                int factionID = getValidFactionID(arguments[1],c);
                if (factionID == 0) return;

                if (null == GameServerState.instance.getFactionManager().getFaction(factionID)) {
                    echo("this faction ID does not belong to any existing station");
                    return;
                }
                //test if replacer already exists
                if (StationReplacer.getFromList(factionID) != null) {
                    echo(  "Faction " + factionID + " already has a replacer.");
                    return;
                }

                //create new replacer
                StationReplacer replacer = new StationReplacer(factionID);
                StationReplacer.addToList(replacer);
                replacer.savePersistent();

                String name = GameServerState.instance.getFactionManager().getFactionName(factionID);
                if (name == null) {
                    name = "unknown faction";
                }

                echo(  "Created replacer for faction " + name + "("+factionID+")" );
                return;
            }
            case RM_RPC:{
                int factionID = getValidFactionID(arguments[1],c);
                if (factionID == 0) return;
                //test if replacer already exists
                StationReplacer rpc = StationReplacer.getFromList(factionID);
                if (rpc == null) {
                    echo(  "Faction " + factionID + " does not have a replacer.");
                    return;
                }
                StationReplacer.removeFromList(factionID);
                echo("removed replacer for faction " + factionID);
            }

            case SAVE: {
                echo(  "saving all replacers to moddata");
                StationReplacer.savePersistentAll();
                return;
            }
            case LOAD: {
                echo(  "loading replacers from moddata");
                StationReplacer.loadPersistentAll();
                return;
            }
            case LIST: {
                StringBuilder s = new StringBuilder();
                if (arguments.length < 2) {
                    //print all
                    StringBuilder list = new StringBuilder();
                    for (StationReplacer replacer : StationReplacer.getList().values()) {
                        int factionID = replacer.factionID;
                        String name = GameServerState.instance.getFactionManager().getFactionName(factionID);
                        if (name == null) {
                            name = "unknown";
                        }
                        list.append(factionID).append(" ").append(name);
                        list.append("(").append(replacer.getBlueprints().size()).append(" bps)");
                        list.append("\n");
                    }
                    echo(  "replacers exist for factions: \n " + list);
                } else {
                    //print single faction detailed
                    int factionID = getValidFactionID(arguments[1],c);
                    if (factionID == 0) return;
                    StationReplacer replacer = StationReplacer.getFromList(factionID);
                    if (replacer == null) {
                        echo(  factionID + " does not have a replacer.");
                        return;
                    }

                    s.append("Faction ").append(factionID).append(" ");
                    if (GameServerState.instance.getFactionManager().getFaction(factionID) == null) {
                        echo("faction does not exist for: "+factionID);
                        return;
                    }

                    s.append(GameServerState.instance.getFactionManager().getFaction(factionID).getName());
                    s.append(" manages stations: (").append(replacer.getManagedStations().values().size()).append(") \n");
                    for (Map.Entry<String, String> iterator: replacer.getManagedStations().entrySet()) {
                        s.append(iterator.getKey()).append(" || ").append(iterator.getValue()).append("\n");
                    }
                    s.append("has blueprints: (").append(replacer.getBlueprints().size()).append(") \n");
                    for (String bp :replacer.getBlueprints()) {
                        s.append(bp);
                    }
                }
                echo(s.toString());
                return;
            }

            case REPLACE: {
                if (arguments.length < 2) {
                    error(c);
                    return;
                }
                String blueprint = arguments[1];

                if (!StationHelper.isValidBlueprint(blueprint)) {
                    echo(  "Not a valid blueprint");
                    return;
                }

                //get selected object
                int selectedID = sender.getSelectedEntityId();
                Sendable selected = (Sendable)GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(selectedID);

                SpaceStation station = null;
                if (selected instanceof  SpaceStation) {
                    station = (SpaceStation) selected;
                } else {
                    echo(  "Selected object not a station.");
                    return;
                }

                SpaceStation spaceStation = StationHelper.replaceFromBlueprint(station,blueprint);
                if (spaceStation == null) {
                    echo("new spacestation null, error!");
                    return;
                }
                StationReplacer replacer = StationReplacer.getFromList(spaceStation.getFactionId());
                if (replacer == null) {
                    echo("no replacer for this faction exists, can not log station: " + spaceStation.getFactionId());
                } else {
                    replacer.addToManaged(spaceStation,blueprint);
                }
                echo("replaced station with blueprint: " + blueprint);
                return;
            }

            case HELP: {
                StringBuilder out = new StringBuilder("--------------------- \nList of commands are: \n");
                for (ChatCmds x: ChatCmds.values()) {
                    if (x.equals(ChatCmds.PREFIX)) continue;
                    out.append(x.getDesc()).append(":\n").append(x.getSyntax()).append("\n+++++++\n");
                }
                out.append("--------------------------");
                echo(out.toString());
                return;
            }

            default: {
                echo(  "Command not recognized. Type \n" + ChatCmds.HELP.getSyntax() + "\n for help");
                return;
            }
        }
    }

    /**
     * will parse string into id, will echo an error mssg if cant be parsed/zero given.
     * @param s argument to be parsed
     * @return
     */
    private int getValidFactionID(String s, ChatCmds c) {
        int factionID;
        try {
            factionID = Integer.parseInt(s);
        }
        catch (NumberFormatException e)
        {
            echo("invalid faction ID: " +s + "expects: \n" + c.getSyntax());
            return 0;
        }
        if (factionID == 0) {
            echo("faction 0 (unfactioned) is not allowed.");
        }
        return factionID;
    }

    /**
     * answers command caller with string.
     * @param mssg mssg to send only to this.sender
     */
    private void echo(String mssg) {
        sender.sendServerMessage(new ServerMessage(Lng.astr(mssg), ServerMessage.MESSAGE_TYPE_SIMPLE, sender.getId()));
    }

    /**
     * echoes an error message with correct syntax example
     * @param c
     */
    private void error(ChatCmds c) {
        echo("incorrect syntax. expects: \n" + c.getSyntax());
    }

    @Override
    public StarMod getMod() {
        return ModMain.instance;
    }
}
