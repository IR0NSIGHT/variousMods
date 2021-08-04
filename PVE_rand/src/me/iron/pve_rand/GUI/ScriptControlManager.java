package me.iron.pve_rand.GUI;

import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.gui.GUIControlManager;
import api.utils.gui.GUIMenuPanel;
import api.utils.gui.ModGUIHandler;
import me.iron.pve_rand.ModMain;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.08.2021
 * TIME: 15:00
 * 100% clientside
 */
public class ScriptControlManager extends GUIControlManager {
    public static GUIMenuPanel p;
    public static ScriptControlManager instance;
    public ScriptControlManager (GameClientState state) {
        super(state);
        instance = this;
        initListener();
    }

    @Override
    public GUIMenuPanel createMenuPanel() {
        p = new ScriptMenuPanel(getState());
        p.onInit();
        p.recreateTabs();
        return p;
    }

    private void initListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                PlayerState p = GameClientState.instance.getPlayer();
                if (!p.isAdmin())
                    return;
                if (event.getText().contains("!menu")) {
                    for (GUIControlManager manager: ModGUIHandler.getAllModControlManagers()) {
                        manager.setActive(false);
                    }
                    //TODO disable all others
                    setActive(true);
                    if (ScriptControlManager.p != null)
                        ScriptControlManager.p.recreateTabs();
                    event.setCanceled(true);
                }
            }
        }, ModMain.instance);
    }
}
