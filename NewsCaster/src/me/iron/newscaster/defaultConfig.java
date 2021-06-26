package me.iron.newscaster;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 26.06.2021
 * TIME: 14:37
 */
public class defaultConfig {
    private static final String[] values = new String[]{
            //broadcaster related stuff
            "broadcast_active                   : 1", //auto broadcaster
            "broadcast_timeout                  : 300", //time between autobroadcasts
            "broadcast_threshold                : 5", //max queue length before autoflush
            "broadcast_show_shipname            : 0",
            "broadcast_show_shiptype            : 1",
            "broadcast_show_shipmass            : 0",
            "broadcast_show_shipfaction         : 1",
            "broadcast_show_pilot               : 0",

            "info_ship_minMass                  : 2000", //minimal mass ship needs otherwise news report is not logged
            "info_threshold                     : 50", //max info objects saved, -1 for unlimited
            "info_log_ship_destruct             : 1",
            "info_log_ship_spawn                : 1",
            "info_log_faction_relationchange    : 1",
            "info_log_faction_systemclaim       : 1"

    };
    public static String[] get() {
        String[] v = values;
        for (int i = 0; i < v.length; i++) {
            v[i] = v[i].replace(" ","");
            v[i] = v[i].replace(":",": ");
        }
        return values;
    }
}
