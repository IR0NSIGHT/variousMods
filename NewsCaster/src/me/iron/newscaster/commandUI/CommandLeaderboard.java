package me.iron.newscaster.commandUI;

import api.mod.StarMod;
import api.utils.game.chat.CommandInterface;
import com.sun.istack.internal.Nullable;
import me.iron.newscaster.DBMS.Manager;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.notification.broadcasting.Broadcaster;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 29.12.2021
 * TIME: 18:23
 */
public class CommandLeaderboard implements CommandInterface {
    @Override
    public String getCommand() {
        return "news_top10";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"top10"};
    }

    @Override
    public String getDescription() {
        return "Shows the leaderboard (if nothing is selected) or info about your selected ship.";
    }

    @Override
    public boolean isAdminOnly() {
        return false;
    }

    @Override
    public boolean onCommand(PlayerState playerState, String[] strings) {
        Sendable selected = GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(playerState.getSelectedEntityId());
        if (selected instanceof SegmentController) {
            try {
                ResultSet r = Manager.getKills(((SegmentController) selected).getUniqueIdentifier(),((SegmentController) selected).getFactionId());
                playerState.sendServerMessage(new ServerMessage(Lng.astr(Broadcaster.prettyVictims(r)),ServerMessage.MESSAGE_TYPE_INFO,playerState.getId()));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return true;
        } else {

            //nothing (proper) selected, show leaderboard.
            try {
                ResultSet r = Manager.getKillers();
                playerState.sendServerMessage(new ServerMessage(Lng.astr(Broadcaster.prettyKillers(r)),ServerMessage.MESSAGE_TYPE_INFO,playerState.getId()));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void serverAction(@Nullable PlayerState playerState, String[] strings) {

    }

    @Override
    public StarMod getMod() {
        return ModMain.instance;
    }



}
