package me.iron.npccontrol.commands;

import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.StationHelper;
import me.iron.npccontrol.StationReplacer;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

import javax.annotation.Nullable;

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
    public boolean onCommand(PlayerState sender, String[] arguments) {

        //station add blueprintname -1
        if (arguments.length == 3) {
            if (arguments[0].equalsIgnoreCase("add") || arguments[0].equalsIgnoreCase("remove")) {
                //get blueprint and faction ID
                String action = arguments[0];
                String blueprintName = arguments[1];
                int factionID = tryParseInt(arguments[2]);
                if (factionID == 0) {
                    PlayerUtils.sendMessage(sender,  arguments[3] + " is not a valid faction ID.");
                    return false;
                }

                //test blueprint validity
                if (!StationHelper.isValidBlueprint(blueprintName)) {
                    PlayerUtils.sendMessage(sender,  blueprintName + " is not a valid blueprint in the catalogmanager.");

                    return false;
                }

                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    PlayerUtils.sendMessage(sender,  "no replacer exists for faction " + factionID);
                    return false;
                }

                switch (action) {
                    case "add":
                    {
                        if (replacer.addBlueprint(blueprintName)) {
                            PlayerUtils.sendMessage(sender,  "successfully added " + blueprintName + " to faction " + factionID);
                            replacer.savePersistent();
                            return true;
                        } else {
                            PlayerUtils.sendMessage(sender,  blueprintName + " is already listed for " + factionID);
                            return false;
                        }
                    }
                    case "remove":
                        if (replacer.removeBlueprint(blueprintName)) {
                            PlayerUtils.sendMessage(sender,  "successfully removed " + blueprintName + " from faction " + factionID);
                            replacer.savePersistent();
                            return true;
                        } else {
                            PlayerUtils.sendMessage(sender,  blueprintName + " is not listed for " + factionID);
                            return false;
                        }
                }
                //todo get faction related stationmanager
                return true;
            }

        }

        //station list -1
        if (arguments.length == 2) {
            String action = arguments[0];
            int factionID = tryParseInt(arguments[1]);

            if (action.equalsIgnoreCase("list")) {
                if (factionID == 0) {
                    PlayerUtils.sendMessage(sender,  arguments[3] + " is not a valid faction ID.");
                    return false;
                }

                //get replacer
                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    PlayerUtils.sendMessage(sender,  "no replacer exists for faction " + factionID);
                    return false;
                }

                String list = "";
                for (String bp : replacer.getBlueprints()) {
                    list += bp + "\n";
                }
                PlayerUtils.sendMessage(sender,  "faction " + factionID + " has available blueprints: \n" + list);
                return true;
            }

            if (action.equalsIgnoreCase("clear")) {
                if (factionID == 0) {
                    PlayerUtils.sendMessage(sender,  arguments[3] + " is not a valid faction ID.");
                    return false;
                }

                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    PlayerUtils.sendMessage(sender,  "no replacer exists for faction " + factionID);
                    return false;
                }

                String list = "";
                for (String bp : replacer.getBlueprints()) {
                    list += bp + "\n";
                    replacer.removeBlueprint(bp);
                }
                PlayerUtils.sendMessage(sender,  "faction " + factionID + " had removed blueprints: \n" + list);
                replacer.savePersistent();
                return true;
            }
        }

        //station list
        if (arguments.length == 1 && arguments[0].equalsIgnoreCase("list")) {
            String list = "";
            for (StationReplacer replacer : StationReplacer.allReplacersDEBUG) {
                int factionID = replacer.factionID;
                String name = GameServerState.instance.getFactionManager().getFactionName(factionID);
                if (name == null) {
                    name = "unknown";
                }
                list += factionID + " " + name + "\n";
            }
            PlayerUtils.sendMessage(sender,  "replacers exist for factions: \n " + list);

            return true;
        }

        //station add_replacer -1 || remove_replacer
        if (arguments.length == 3) {
            String action = arguments[0];
            int factionID = tryParseInt(arguments[2]);

            if (factionID == 0) {
                PlayerUtils.sendMessage(sender,  "invalid faction id given:" + arguments[2]);
                return false;
            }

            if (action.equalsIgnoreCase("add_replacer")) {
                //test if replacer already exists
                if (StationReplacer.getFromList(factionID) != null) {
                    PlayerUtils.sendMessage(sender,  "Faction " + factionID + " already has a replacer.");
                    return false;
                }

                //create new replacer
                StationReplacer replacer = new StationReplacer(factionID);
                StationReplacer.addToList(replacer);
                replacer.savePersistent();

                String name = GameServerState.instance.getFactionManager().getFactionName(factionID);
                if (name == null) {
                    name = "unknown faction";
                }

                PlayerUtils.sendMessage(sender,  "Created replacer for faction " + name + "("+factionID+")" );
                return true;
            }

            if (action.equalsIgnoreCase("remove_replacer")) {
                //test if replacer exists
                StationReplacer replacer = StationReplacer.getFromList(factionID);
                if (replacer == null) {
                    PlayerUtils.sendMessage(sender,  "Faction " + factionID + " doesn't have a replacer.");
                    return false;
                }

                //remove
                StationReplacer.removeFromList(factionID);

                //return
                PlayerUtils.sendMessage(sender,  "replacer removed for " + factionID);
                return true;
            }

        }

        //station save
        if (arguments[0].equalsIgnoreCase("save")) {
            PlayerUtils.sendMessage(sender,  "saving all replacers to moddata");
            StationReplacer.savePersistentAll();
            return true;
        }

        //pirate station load
        if (arguments[0].equalsIgnoreCase("load")) {
            PlayerUtils.sendMessage(sender,  "loading replacers from moddata");
            StationReplacer.loadPersistentAll();
            return true;
        }

        //debug direct replace:
        //station replace bpName
        if (arguments.length == 2 && arguments[0].equalsIgnoreCase("replace")) {
            String blueprint = arguments[1];

            if (!StationHelper.isValidBlueprint(blueprint)) {
                PlayerUtils.sendMessage(sender,  "Not a valid blueprint");
                return false;
            }

            //get selected object
            int selectedID = sender.getSelectedEntityId();
            Sendable selected = (Sendable)GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(selectedID);

            SpaceStation station = null;
            if (selected instanceof  SpaceStation) {
                station = (SpaceStation) selected;
            } else {
                PlayerUtils.sendMessage(sender,  "Selected object not a station.");
                return false;
            }

            SpaceStation spaceStation = StationHelper.replaceFromBlueprint(station,blueprint);
            if (spaceStation == null) {
                return false;
            }
            StationReplacer.getFromList(spaceStation.getFactionId()).addToManaged(spaceStation,blueprint);
            return true;
        }
        //no command matched:
        PlayerUtils.sendMessage(sender,  "Command not recognized.");
        return false;
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

    @Override
    public void serverAction(@Nullable PlayerState playerState, String[] strings) {

    }

    @Override
    public StarMod getMod() {
        return ModMain.instance;
    }
}
