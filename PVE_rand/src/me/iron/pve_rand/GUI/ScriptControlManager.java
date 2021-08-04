package me.iron.pve_rand.GUI;

import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.input.KeyPressEvent;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.gui.GUIControlManager;
import api.utils.gui.GUIMenuPanel;
import api.utils.gui.ModGUIHandler;
import me.iron.pve_rand.ModMain;
import org.lwjgl.input.Keyboard;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerState;
import org.schema.schine.input.KeyboardEvent;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.08.2021
 * TIME: 15:00
 * 100% clientside
 */
public class ScriptControlManager extends GUIControlManager {
    public static ScriptControlManager instance;
    public ScriptControlManager (GameClientState state) {
        super(state);
        instance = this;
        initListener();
    }

    @Override
    public GUIMenuPanel createMenuPanel() {
        MyMenuPanel p = new MyMenuPanel(getState());
        p.onInit();
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
                    event.setCanceled(true);
                }
            }
        }, ModMain.instance);

        StarLoader.registerListener(KeyPressEvent.class, new Listener<KeyPressEvent>() {
            @Override
            public void onEvent(KeyPressEvent keyPressEvent) {
                KeyboardEvent e = keyPressEvent.getRawEvent();
                DebugFile.log("keydown: "  + e.toString());
            }
        },ModMain.instance);
    }
}
