package me.iron.newscaster.commandUI;

import api.DebugFile;
import api.mod.StarMod;
import api.utils.game.chat.CommandInterface;
import com.sun.istack.internal.Nullable;
import me.iron.newscaster.notification.broadcasting.Broadcaster;
import me.iron.newscaster.notification.infoGeneration.NewsManager;
import me.iron.newscaster.notification.infoGeneration.infoTypes.GenericInfo;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.data.GameServerState;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 15.09.2021
 * TIME: 15:18
 */
public class CommandBroadcast implements CommandInterface {
    @Override
    public String getCommand() {
        return CommandUI.prefix + "_broadcast";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"nbc"};
    }

    @Override
    public String getDescription() {
        return "broadcast command: \n" +
                "args: flush (flushes queue)\n" +
                "info (echos info)\n" +
                "aliases: " + Arrays.toString(getAliases());
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState sender, String[] strings) {
        if (strings.length==1&&strings[0].equals("")) {
            //no args
            CommandUI.sendMssg(sender,getDescription());
            return true;
        }

        if (strings.length>0&&strings[0].equals("flush")) {
            //flush queue
            Broadcaster.flushQueue();
            return true;
        }

        if (strings.length>0&&strings[0].equals("info")) {
            //echo info
            CommandUI.sendMssg(sender, NewsManager.getNewsStorage().size() + " news in storage. \n" +
                    " news cycle for broadcasting: " + Broadcaster.broadcastLoopTime/1000 + " seconds. \n" +
                    " threshold causing autoflush: " + Broadcaster.threshold +"\n" +
                    " news in queue: " + Broadcaster.getQueueSize()
            );
            return true;
        }

        if (strings.length>0&&strings[0].equals("list")) {
            int amount = 5;
            if (strings.length>1) {
                try {
                    amount = Integer.parseInt(strings[1]);
                } catch (NumberFormatException ex) {
                    CommandUI.sendMssg(sender,strings[1]+ " is not a number.");
                    return false;
                }
                if (amount == -1)
                    amount = NewsManager.getNewsStorage().size();

                StringBuilder out = new StringBuilder();
                out.append("Listing last").append(amount).append("infos");

                amount = NewsManager.getNewsStorage().size() - amount;
                for (int i = NewsManager.getNewsStorage().size()-1; i >= 0 && i >= amount; i--) {
                    GenericInfo info = NewsManager.getNewsStorage().get(i);
                    out.append(info.toString()).append("systemname:").append(Broadcaster.getSystemName(info.getSector(), true)).append("\n\n");
                }
                CommandUI.sendMssg(sender,out.toString());
                return true;
            }
        }
        return false;
    }

    @Override
    public void serverAction(@Nullable PlayerState playerState, String[] strings) {

    }

    @Override
    public StarMod getMod() {
        return null;
    }
}
