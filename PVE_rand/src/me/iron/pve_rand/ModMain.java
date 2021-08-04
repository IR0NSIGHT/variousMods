package me.iron.pve_rand;

import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.ModSkeleton;
import api.mod.StarMod;
import api.utils.StarRunnable;
import api.utils.gui.ModGUIHandler;
import me.iron.pve_rand.CodeElements.ScriptTemplates;
import me.iron.pve_rand.Managers.ActionController;
import me.iron.pve_rand.Managers.ListenerManager;
import me.iron.pve_rand.GUI.ScriptControlManager;
import me.iron.pve_rand.Managers.PlayerInterface;
import org.schema.game.client.data.GameClientState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 12:01
 */
public class ModMain extends StarMod {
    public static StarMod instance;
    public ModMain() {
        super();
    }

    @Override
    public ModSkeleton getSkeleton() {
        return super.getSkeleton();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        new ActionController(); //create an instance for observers

    }

    @Override
    public void onDisable() {
        super.onDisable();
    //    ActionController.savePersistent();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        ListenerManager.init();
        PlayerInterface.init();
        ScriptTemplates.derelictGuardian();
        ScriptTemplates.lostCargo();
    //     ActionController.addDebugEvent();

    //      ActionController.loadPersistent();

    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
        new StarRunnable() {
            @Override
            public void run() {
                ScriptControlManager controlManager = new ScriptControlManager(GameClientState.instance);
                ModGUIHandler.registerNewControlManager(getSkeleton(), controlManager);
            }
        }.runLater(instance,100);

    }

}
