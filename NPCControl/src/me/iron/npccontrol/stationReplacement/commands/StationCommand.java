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
        if (arguments.length < 1)
            return;
        String cmd = arguments[0];
        ChatCmds c = ChatCmds.getByCmd(cmd);
        switch (c) {
            case LIST:
            case ADD_BP: {
                if (arguments.length < 2) {
                    echo("syntax error, expects: \n" + c.getExp());
                    return;
                }
                String blueprintName = arguments[1];
                int factionID = tryParseInt(arguments[2]);
                if (factionID == 0) {
                    echo(factionID + " is not a valid faction ID.");
                    return;
                }
                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    echo("no replacer exists for faction " + factionID);
                    return;
                }

                if (replacer.addBlueprint(blueprintName)) {
                    echo("successfully added " + blueprintName + " to faction " + factionID);
                    replacer.savePersistent();
                    return;
                } else {
                    echo(blueprintName + " is already listed for " + factionID);
                    return;
                }

                /*        if (replacer.removeBlueprint(blueprintName)) {
                            echo(  "successfully removed " + blueprintName + " from faction " + factionID);
                            replacer.savePersistent();
                            return;
                        } else {
                            echo(  blueprintName + " is not listed for " + factionID);
                            return;
                        }
                */ //FIXME remove-bp code
            }
            case REMOVE_BP: {
                if (arguments.length < 2) {
                    echo("syntax error, expects: \n" + c.getExp());
                    return;
                }
                String blueprintName = arguments[1];
                int factionID = tryParseInt(arguments[2]);
                if (factionID == 0) {
                    echo(factionID + " is not a valid faction ID.");
                    return;
                }
                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    echo("no replacer exists for faction " + factionID);
                    return;
                }

                if (replacer.removeBlueprint(blueprintName)) {
                    echo(  "successfully removed " + blueprintName + " from faction " + factionID);
                    replacer.savePersistent();
                    return;
                } else {
                    echo(  blueprintName + " is not listed for " + factionID);
                    return;
                }
            }
            case CLEAR_BP: {}

            case SAVE: {}
            case LOAD: {}
            case REPLACE: {}
            case HELP: {}
            default: {
                echo("unknown message. type \n" + ChatCmds.PREFIX.getCmd() + ChatCmds.HELP.getCmd() + "\n for help.");
            }
        }
        //station add blueprintname -1
        if (arguments.length == 3) {
                //get blueprint and faction ID



                //todo get faction related stationmanager
                return;
            }

            //station list -1 -s
            if (arguments[0].equalsIgnoreCase("list") && arguments[2].equalsIgnoreCase("-s")) {
                int factionID = tryParseInt(arguments[1]);
                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    echo(  factionID + " does not have a replacer.");
                    return;
                }
                String s = "Faction " + factionID;
                if (GameServerState.instance.getFactionManager().getFaction(factionID) != null) {
                    s += " " + GameServerState.instance.getFactionManager().getFaction(factionID).getName();
                }
                s += " manages stations: \n";
                for (Map.Entry<String, String> iterator: replacer.getManagedStations().entrySet()) {
                    s += iterator.getKey() + " || " + iterator.getValue() + "\n";
                }
                echo(  s);
                return;
            }
        }

        //station list -1 (factionID)
        if (arguments.length == 2) {
            String action = arguments[0];
            int factionID = tryParseInt(arguments[1]);

            if (action.equalsIgnoreCase("list")) {
                if (factionID == 0) {
                    echo(  arguments[3] + " is not a valid faction ID.");
                    return;
                }

                //get replacer
                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    echo(  "no replacer exists for faction " + factionID);
                    return;
                }

                String list = "";
                for (String bp : replacer.getBlueprints()) {
                    list += bp + "\n";
                }
                echo(  "faction " + factionID + " has available blueprints: \n" + list);
                return;
            }

            if (action.equalsIgnoreCase("clear")) {
                if (factionID == 0) {
                    echo(  factionID + " is not a valid faction ID.");
                    return;
                }

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
        }

        //station list
        if (arguments.length == 1 && arguments[0].equalsIgnoreCase("list")) {
            String list = "";
            for (StationReplacer replacer : StationReplacer.getList().values()) {
                int factionID = replacer.factionID;
                String name = GameServerState.instance.getFactionManager().getFactionName(factionID);
                if (name == null) {
                    name = "unknown";
                }
                list += factionID + " " + name + "\n";
            }
            echo(  "replacers exist for factions: \n " + list);

            return;
        }


        //station add_replacer -1 || remove_replacer
        if (arguments.length == 2) {
            String action = arguments[0];
            int factionID = tryParseInt(arguments[1]);

            if (factionID == 0) {
                echo(  "invalid faction id given:" + factionID);
                return;
            }

            if (action.equalsIgnoreCase("add_replacer")) {
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

            if (action.equalsIgnoreCase("remove_replacer")) {
                //test if replacer exists
                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    echo(  "Faction " + factionID + " doesn't have a replacer.");
                    return;
                }

                //remove
                StationReplacer.removeFromList(factionID);

                //return
                echo(  "replacer removed for " + factionID);
                return;
            }

        }

        //station save
        if (arguments[0].equalsIgnoreCase("save")) {
            echo(  "saving all replacers to moddata");
            StationReplacer.savePersistentAll();
            return;
        }

        //pirate station load
        if (arguments[0].equalsIgnoreCase("load")) {
            echo(  "loading replacers from moddata");
            StationReplacer.loadPersistentAll();
            return;
        }

        //debug direct replace:
        //station replace bpName
        if (arguments.length == 2 && arguments[0].equalsIgnoreCase("replace")) {
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
                return;
            }
            StationReplacer.getFromList(spaceStation.getFactionId()).addToManaged(spaceStation,blueprint);
            return;
        }
        //no command matched:
        echo(  "Command not recognized.");
        return;
    }

    //it does what its name suggests. returns zero on error
    private int tryParseInt(String s) {
        int factionID = 0;
        try {
            factionID = Integer.parseInt(s);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
        return factionID;
    }

    private boolean blueprintChange(String bp, int factionID, boolean add) {

    }

    /**
     * answers command caller with string.
     * @param mssg mssg to send only to this.sender
     */
    private void echo(String mssg) {
        sender.sendServerMessage(new ServerMessage(Lng.astr(mssg), ServerMessage.MESSAGE_TYPE_SIMPLE, sender.getId()));
    }

    @Override
    public StarMod getMod() {
        return ModMain.instance;
    }
}
