package me.iron.npccontrol.stationReplacement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 19.04.2021
 * TIME: 20:50
 */
public class StationFactionContainer implements Serializable {
    ArrayList<String> replacementBlueprints;
    HashMap<String,String> stations;
    int factionID;
}
