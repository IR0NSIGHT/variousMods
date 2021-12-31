package me.iron.npccontrol.triggers;

import api.mod.StarMod;
import api.utils.game.chat.CommandInterface;
import com.sun.istack.internal.Nullable;
import me.iron.npccontrol.ModMain;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.12.2021
 * TIME: 13:04
 */
public class DebugUI implements CommandInterface {
    @Override
    public String getCommand() {
        return "debug";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"debug"};
    }

    @Override
    public String getDescription() {
        return "debug for NPC control";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState playerState, String[] strings) {
        if (strings.length > 0 && strings[0].equals("reload")) {
            ModMain.trigger.unregister();
            ModMain.trigger = new Trigger();
            //ModMain.log("new trigger loaded");
            return true;
        }

        if (strings.length > 0 && strings[0].equals("catalog")) {
            //ModMain.log(Utility.catalogToString());
            return true;
        }


        if (strings.length > 1 && strings[0].equals("blueprint")) {
            String UID = strings[1];
            //ModMain.log("getting blueprint for: '"+UID+"'");
            CatalogPermission p = Utility.getBlueprintByName(UID);
            if (p != null) {
                //ModMain.log(p.toString());
            } else {
                //ModMain.log("no match found.");
            }
            return true;
        }

        if (strings.length>0 && strings[0].equals("ai")) {
            AIManager.debug = !AIManager.debug;
            ModMain.log("set AIManager debug to "+AIManager.debug);
        }

        return false;
    }

    @Override
    public void serverAction(@Nullable PlayerState playerState, String[] strings) {
        //obsolete leftover
    }

    @Override
    public StarMod getMod() {
        return ModMain.instance;
    }
}
