package me.iron.pve_rand;

import api.config.BlockConfig;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.ModSkeleton;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;
import api.utils.StarRunnable;
import api.utils.gui.ModGUIHandler;
import api.utils.particle.ModParticleUtil;
import me.iron.pve_rand.Action.ActionController;
import me.iron.pve_rand.Event.ListenerManager;
import me.iron.pve_rand.GUI.ScriptControlManager;
import org.schema.game.client.data.GameClientState;
import org.schema.schine.resource.ResourceLoader;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

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
        ActionController.savePersistent();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        ListenerManager.init();
  //      ActionController.loadPersistent();
        PlayerInterface.init();
        ScriptTemplates.derelictGuardian();
        ScriptTemplates.lostCargo();
   //     ActionController.addDebugEvent();
    //    ActionController.savePersistent();
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
