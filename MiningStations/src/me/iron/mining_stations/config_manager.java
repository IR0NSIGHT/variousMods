package me.iron.mining_stations;

import org.schema.game.common.data.world.SectorInformation;

import java.util.Arrays;
import java.util.List;

public class config_manager {
//config values that other classes use
    //how often the miningcylce checks all stations for an update (best not touch) [millis]
    public static int update_check_time = 1000;

    //basetime for a station to spawn one crate [millis]
    public static int miner_update_time =4 * 1000;

    //time increase compared to basetime in percent -> 100,90,80,70,60 etc
    public static int miner_improvement_per_level = 10;

    //maximum inventory volume per spawned crate [volume units]
    public static int max_volume_per_crate = 100;

    //multiplier for resources (on top of normal mining bonus) [factor]
    public static int passive_mining_bonus = 10;

    public static List<SectorInformation.SectorType> allowed_system_types = Arrays.asList(
            SectorInformation.SectorType.SUN,
            SectorInformation.SectorType.DOUBLE_STAR,
            SectorInformation.SectorType.GIANT);
}
//TODO load from config, save to config, edit via chat commands
