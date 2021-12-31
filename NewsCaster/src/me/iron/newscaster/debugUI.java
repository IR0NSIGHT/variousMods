package me.iron.newscaster;

import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.game.PlayerUtils;
import me.iron.newscaster.DBMS.Manager;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

import java.sql.SQLException;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.09.2021
 * TIME: 13:57
 */
public class debugUI {
    public static void chatListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                ModPlayground.broadcastMessage("chat event fired");

                if (event.getText().contains("ping")) {
                    for (PlayerState p: GameServerState.instance.getPlayerStatesByName().values()) {
                        PlayerUtils.sendMessage(p,"pong");
                    }
                }
                if (event.getText().contains("print")) {
                    try {
                        System.out.println(Manager.resultToString(Manager.getAttacksPretty()));
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }

                if (event.getText().contains("!objects")) {
                    try {
                        ModPlayground.broadcastMessage(Manager.resultToString(Manager.getObjectsSimple()));
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
                if (event.getText().contains("!killer")) {
                    try {
                        ModPlayground.broadcastMessage("killers:\n"+Manager.resultToString(Manager.getKillers()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (event.getText().contains("!victims")) {
                    //try get player
                    PlayerState p = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender);
                    if (p == null)
                        return;
                    Sendable s =GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(p.getSelectedEntityId());
                    if (s instanceof SegmentController) {
                        try {
                            ModPlayground.broadcastMessage("get kills for " + ((SegmentController) s).getRealName() + " type " + ((SegmentController) s).getType().getName());
                            ModPlayground.broadcastMessage(Manager.resultToString(Manager.getKills(((SegmentController) s).getUniqueIdentifier(),null)));
                            return;
                        } catch (Exception e) {
                            ModPlayground.broadcastMessage("error: " + e.toString());
                            e.printStackTrace();
                        }
                    }
                    ModPlayground.broadcastMessage("error: selected object not a ship/station");
                }
                if (event.getText().contains("!delete")) {
                    try {
                        Manager.deleteTables();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }

                if(event.getText().contains("!reset")) {
                    try {
                        Manager.deleteTables();
                        Manager.initTable();
                        ModPlayground.broadcastMessage("reset table:");
                        ModPlayground.broadcastMessage(Manager.resultToString(Manager.getAttacksPretty()));
                        ModPlayground.broadcastMessage(Manager.resultToString(Manager.getObjectsSimple()));

                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                        ModPlayground.broadcastMessage(throwables.toString());
                    }

                }
                if (event.getText().contains("!attacks")) {
                    try {
                        String out = "attacks:\n"+Manager.resultToString(Manager.getAttacksPretty());
                        System.out.println(out);
                        ModPlayground.broadcastMessage(out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, ModMain.instance);
    }
}
