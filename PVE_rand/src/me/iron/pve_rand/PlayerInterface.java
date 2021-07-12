package me.iron.pve_rand;

import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import me.iron.pve_rand.Action.ActionController;
import me.iron.pve_rand.Action.CustomAction;
import me.iron.pve_rand.Action.CustomScript;
import me.iron.pve_rand.Event.CustomTrigger;
import org.dom4j.rule.Action;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;

import java.util.Arrays;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 12:23
 * basically a chatcommand class
 */
public class PlayerInterface {
    public static void init() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                if (!event.isServer()) return;
                PlayerState sender = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender);
                if (sender == null || !sender.isAdmin()) return;
                String mssg = event.getText();
                if (!mssg.contains("!pve ")) return;
                mssg = mssg.replace("!pve ","");
                //commands
                if (mssg.contains("list --all")) {
                    StringBuilder s = new StringBuilder("Triggers: \n");
                    for (CustomTrigger t: ActionController.getAllTriggers()) {
                        s.append("des: ").append(t.getDescription()).append("\n[");
                        for (int c : t.getConditions()) {
                            s.append(Integer.toHexString(c)).append(", ");
                        }
                        s.append("]\n");
                        for (CustomScript script: t.getActions()) {
                            s.append(script.toString()).append("\n");
                        }
                        s.append("\n");

                    }
                    sender.sendServerMessage(Lng.astr(s.toString()),0);
                    return;
                }

                if (mssg.contains("save persistent")) {
                    ActionController.savePersistent();
                    sender.sendServerMessage(Lng.astr("saving"),0);
                    return;
                }

                if (mssg.contains("load persistent")) {
                    ActionController.loadPersistent();
                    sender.sendServerMessage(Lng.astr("loading"),0);
                    return;
                }

                if (mssg.contains("add default")) {
                    mssg = mssg.replace("new trigger default","");
                    ActionController.addDebugEvent();
                    sender.sendServerMessage(Lng.astr("added default trigger"),0);

                    return;
                }

                if(mssg.contains("clear triggers")) {
                    ActionController.clearAll();
                    sender.sendServerMessage(Lng.astr("deleted all triggers/scripts/actions"),0);
                    return;
                }
                sender.sendServerMessage(Lng.astr("no match for commands!"),0);
            }
        },ModMain.instance);
    }
}
