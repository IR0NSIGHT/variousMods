package me.iron.npccontrol;

import api.DebugFile;
import api.ModPlayground;
import api.mod.StarMod;
import api.utils.game.chat.CommandInterface;
import org.schema.game.common.data.player.PlayerState;

import java.util.Arrays;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 09.06.2021
 * TIME: 12:43
 */
public class TestCommand implements CommandInterface {
    @Override
    public String getCommand() {
        return "test";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "testing if commands are broken";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] args) {
        String name = (sender == null)? "null-sender": sender.getName();
        System.err.println("onCommand was called by "+name + " with args: "+ Arrays.toString(args));
        ModPlayground.broadcastMessage("onCommand called.");
        return true;
    }

    @Override
    public void serverAction(PlayerState sender, String[] args) {
        String name = (sender == null)? "null-sender": sender.getName();
        System.err.println("serverAction was called by "+name+ " with args: "+ Arrays.toString(args));
        ModPlayground.broadcastMessage("serverAction called.");
    }

    @Override
    public StarMod getMod() {
        return ModMain.instance;
    }
}
