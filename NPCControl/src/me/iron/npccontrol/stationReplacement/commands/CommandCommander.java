package me.iron.npccontrol.stationReplacement.commands;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import me.iron.npccontrol.ModMain;
import org.schema.game.common.data.chat.prefixprocessors.PrefixProcessorAdminCommand;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 20.04.2021
 * TIME: 21:27
 */
public class CommandCommander {
    static StationCommand stationCommand;
    public static void init() {
        stationCommand = new StationCommand();
        DebugFile.log("command commander init");
    //    StarLoader.registerCommand(new StationCommand()); //FIXME command interface is broken since april 2021
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                PlayerState sender = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender);
                if (sender == null || !sender.isAdmin())
                    return;
                String mssg = event.getText();
                String prefix = ChatCmds.PREFIX.getCmd();
                if (!mssg.contains(prefix)) return;
                mssg = mssg.replace(prefix,"");

                String[] args = mssg.split(" ");
                stationCommand.serverAction(sender,args);
                event.setCanceled(true);
            }
        }, ModMain.instance);
    }
}
