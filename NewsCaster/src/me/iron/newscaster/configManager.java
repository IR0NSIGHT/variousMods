package me.iron.newscaster;

import api.DebugFile;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;
import com.sun.deploy.config.DefaultConfig;
import me.iron.newscaster.eventListening.ListenerManager;
import me.iron.newscaster.notification.broadcasting.Broadcaster;
import me.iron.newscaster.notification.infoGeneration.NewsManager;

import java.util.HashMap;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 26.06.2021
 * TIME: 14:20
 * handles config stuff related to the broadcaster.
 */
public class configManager {
    static StarMod mod = ModMain.instance;
    static String name = "newscaster_settings";
    static FileConfiguration config;
    public static int getValue(String entryName) {
        //make sure value is valid path
        boolean exists = config.getKeys().contains(entryName);
        if (!exists) {
            System.out.println("Newscaster config entry doesnt exist: " + entryName);
            return 0;
        }
        int value = config.getInt(entryName);

        return value;
    }

    /**
     * returns clone of config internal hashmap
     * @return
     */
    public static HashMap<String, Object> getAll() {
        HashMap<String, Object> map = new HashMap<>();
        for (String s: config.getKeys()) {
            map.put(s,config.getInt(s));
        }
        return map;
    }
    /**
     * sets config value.
     * @return
     */
    public static void setValue(String path, Object value) {
        assert config != null;
        config.set(path, value);
        config.saveConfig();
    }

    public static void init() {
        config = mod.getConfig(name);
        String[] defaultV = defaultConfig.get();
        config.saveDefault(defaultV);
        updateGameValues();
        DebugFile.log("set default config.",mod);
    }

    /**
     * tests if entry is existent in config.
     * @param path
     * @return
     */
    public static boolean exists(String path) {
        return config.getKeys().contains(path);
    }

    /**
     * resets config to default.
     */
    public static void resetDefault() {
        for (String x: defaultConfig.get()) {
            String[] xs = x.split(": ");
            config.set(xs[0],Integer.parseInt(xs[1]));
        };
        config.saveConfig();
        updateGameValues();
    }
    public static void updateGameValues() {
        Broadcaster.updateFromConfig();
        ListenerManager.updateFromConfig();
        NewsManager.updateFromConfig();
    }
}
