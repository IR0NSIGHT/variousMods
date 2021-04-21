package me.iron.npccontrol.commands;

import api.mod.StarMod;
import api.utils.game.PlayerUtils;
import api.utils.game.chat.CommandInterface;
import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.NPCStationReplacer;
import me.iron.npccontrol.StationReplacer;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import javax.annotation.Nullable;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 20.04.2021
 * TIME: 21:30
 */
public class PirateCommand implements CommandInterface {
    @Override
    public String getCommand() {
        return "pirate";
    }

    @Override
    public String[] getAliases() {
        return new String[] {"piratecontrol"};
    }

    @Override
    public String getDescription() {
        return "command interface for replacing pirate stations";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] arguments) {

        //pirate station add blueprintname -1
        if (arguments.length == 4 && arguments[0].equalsIgnoreCase("station")) {
            if (arguments[1].equalsIgnoreCase("add") || arguments[1].equalsIgnoreCase("remove")) {
                //get blueprint and faction ID
                String blueprintName = arguments[2];
                int factionID = tryParseInt(arguments[3]);
                if (factionID == 0) {
                    PlayerUtils.sendMessage(sender,  arguments[3] + " is not a valid faction ID.");
                    return false;
                }

                //test blueprint validity
                if (!NPCStationReplacer.isValidBlueprint(blueprintName)) {
                    PlayerUtils.sendMessage(sender,  blueprintName + " is not a valid blueprint in the catalogmanager.");

                    return false;
                }

                StationReplacer replacer = StationReplacer.replacerList.get(factionID);
                if (replacer == null) {
                    PlayerUtils.sendMessage(sender,  "no replacer exists for faction " + factionID);
                    return false;
                }

                switch (arguments[1]) {
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

        //pirate station list -1
        if (arguments.length == 3 && arguments[0].equalsIgnoreCase("station")) {
            if (arguments[1].equalsIgnoreCase("list")) {
                int factionID = tryParseInt(arguments[2]);
                if (factionID == 0) {
                    PlayerUtils.sendMessage(sender,  arguments[3] + " is not a valid faction ID.");
                    return false;
                }

                //get replacer
                StationReplacer replacer = StationReplacer.replacerList.get(factionID);
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

            if (arguments[1].equalsIgnoreCase("clear")) {
                int factionID = tryParseInt(arguments[2]);
                if (factionID == 0) {
                    PlayerUtils.sendMessage(sender,  arguments[3] + " is not a valid faction ID.");
                    return false;
                }

                StationReplacer replacer = StationReplacer.replacerList.get(factionID);
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

        //pirate station list
        if (arguments.length == 2 && arguments[0].equalsIgnoreCase("station")) {
            String list = "";
            for (int factionID : StationReplacer.replacerList.keySet()) {
                String name = GameServerState.instance.getFactionManager().getFactionName(factionID);
                if (name == null) {
                    name = "unknown";
                }
                list += factionID + " " + name + "\n";
            }
            PlayerUtils.sendMessage(sender,  "replacers exist for factions: \n " + list);
            return true;
        }

        //pirate station add_replacer -1 || remove_replacer
        if (arguments.length == 3 && arguments[0].equalsIgnoreCase("station")) {
            int factionID = tryParseInt(arguments[2]);
            if (factionID == 0) {
                PlayerUtils.sendMessage(sender,  "invalid faction id given:" + arguments[2]);
                return false;
            }

            if (arguments[1].equalsIgnoreCase("add_replacer")) {
                //test if replacer already exists
                if (StationReplacer.replacerList.get(factionID) != null) {
                    PlayerUtils.sendMessage(sender,  "Faction " + factionID + " already has a replacer.");
                    return false;
                }

                //create new replacer
                StationReplacer replacer = new StationReplacer(factionID);
                StationReplacer.replacerList.put(factionID,replacer);
                replacer.savePersistent();

                String name = GameServerState.instance.getFactionManager().getFactionName(factionID);
                if (name == null) {
                    name = "unknown faction";
                }

                PlayerUtils.sendMessage(sender,  "Created replacer for faction " + name + "("+factionID+")" );
                return true;
            }

            if (arguments[1].equalsIgnoreCase("remove_replacer")) {
                //test if replacer exists
                StationReplacer replacer = StationReplacer.replacerList.get(factionID);
                if (replacer == null) {
                    PlayerUtils.sendMessage(sender,  "Faction " + factionID + " doesn't have a replacer.");
                    return false;
                }

                //remove
                StationReplacer.replacerList.remove(factionID);

                //return
                PlayerUtils.sendMessage(sender,  "replacer removed for " + factionID);
                return true;
            }

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
