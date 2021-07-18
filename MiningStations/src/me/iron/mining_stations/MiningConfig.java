package me.iron.mining_stations;

import api.mod.config.FileConfiguration;
import api.mod.config.PersistentObjectUtil;

import javax.jdo.annotations.Persistent;
import java.util.HashMap;

public enum MiningConfig {

    //config values that other classes use
    update_check_time(1000,
            0,
            "update check time",
            "how often the miningcylce checks all stations for an update (best not touch) [millis]"),

    miner_update_time(4 * 1000,
            1,
            "miner update time",
            "basetime for a station to spawn one crate [millis, 4000]"),

    miner_improvement_per_level(10,
            2,
            "time improvement per level",
            "the miner produces crates X*level percent faster than the basetime [percent, 10]"),

    max_volume_per_crate(1000,
            3,
            "maximum volume per crate",
            "a crate is filled with X volume of resources when its spawned at max. [number, 1000]"),

    passive_mining_bonus(10,
            4,
            "passive mining bonus",
        "an asteroids resources are multiplied with X*vanilla mining bonus [factor,10]");

    /*  public static List<SectorInformation.SectorType> allowed_system_types = Arrays.asList(
            SectorInformation.SectorType.SUN,
            SectorInformation.SectorType.DOUBLE_STAR,
            SectorInformation.SectorType.GIANT);
    */
    public int getValue() {
        return value;
    }
    private int value; //value that the game uses
    private int key; //an integer that identifies the enum for lazy, fast chat input
    private String description;
    private String name;
    private static HashMap<Integer, MiningConfig> by_key = new HashMap<>();

    static {
        for (MiningConfig entry : values()) {
            by_key.put(entry.key, entry);
        }
    }

    MiningConfig(int value, int key, String name, String description) {
        this.key = key;
        this.value = value;
        this.name = name;
        this.description = description;
    }

    public static int setValue(int key, int value) {
        MiningConfig entry = by_key.get(key);
        if (entry == null)
            return -1; //not found
        entry.value = value;
        saveConfig();
        return 0;
    }

    public static void loadConfig() {
        FileConfiguration config = ModMain.instance.getConfig("MiningConfig");
        for (MiningConfig entry: values()) {
            String keyString = Integer.toString(entry.key);
            if (config.getKeys().contains(keyString)) {
                System.out.print("config doesnt contain entry for " + entry.name);
                continue;
            }
            entry.value = config.getInt(keyString);
        }
    }

    public static void saveConfig() {
        FileConfiguration config = ModMain.instance.getConfig("MiningConfig");
        for (MiningConfig entry: values()) {
            String keyString = Integer.toString(entry.key);
            config.set(keyString,entry.value);
        }
        config.saveConfig();
    }
    public int getKey() {
        return key;
    }
    public String getName() {
        return description;
    }
    public static String getName(int key) {
        MiningConfig entry = by_key.get(key);
        if (entry != null)
            return entry.getName();
        return null;
    }
    public String getDescription() {
        return description;
    }
}
//TODO load from config, save to config, edit via chat commands