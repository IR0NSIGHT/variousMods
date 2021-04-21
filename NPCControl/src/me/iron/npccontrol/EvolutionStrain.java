package me.iron.npccontrol;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 17.04.2021
 * TIME: 17:03
 */

import api.DebugFile;

import java.io.Serializable;
import java.util.HashMap;

/**
 * represents the evolutionary strain/wrapper of a version-zeor blueprint:
 * every trader station has its own evolutionStrain object. It holds the originals BPs name, the current replacement blueprint, and the current version to compare against found stations.
 */
public class EvolutionStrain implements Serializable {
    private int version;
    private String originalBlueprint;
    private String currentBlueprint;
    //maps station UID vs stations bp version
    private HashMap<String,Integer> stations = new HashMap<>();

    /**
     * get the version of a station of this strain. returns zero for gen zero
     * @param stationUID station uid
     * @return version number
     */
    public int getStationVersion(String stationUID) {
        Integer version = stations.get(stationUID);
        if (version == null) {
            return 0;
        }
        return version;
    }

    public void UpdateStation(String oldStation, String newStation) {
        stations.put(newStation,version);
        stations.remove(oldStation);
    }
    //TODO log all blueprint updates as legacy
    //TODO revert method
    public void RemoveStation(String stationUID) {
        stations.remove(stationUID);
    }
    /**
     * creates a strain for this blueprint. stations with this blueprint will from now on be auto updated with replacements.
     * @param originalBlueprint name of original
     * @param currentBlueprint new blueprint to use as a replacement
     */
    public EvolutionStrain(String originalBlueprint, String currentBlueprint) {
        this.originalBlueprint = originalBlueprint;
        this.currentBlueprint = currentBlueprint;
        this.version = 1;
        RegisterStrain();
        DebugFile.log("creating evolution strain: " + toString());
    }

    /**
     * updates this strain to use given blueprint, returns new version number.
     * @param newBlueprint
     * @return new version number
     */
    public int updateStrain(String newBlueprint) {
        //check for existence of blueprint.
        this.currentBlueprint = newBlueprint;
        return (++version);
    }

    /**
     * get version of this strain
     * @return version number
     */
    public int getVersion() {
        return version;
    }

    /**
     * get original blueprint (strain defining ancestor)
     * @return
     */
    public String getOriginalBlueprint() {
        return originalBlueprint;
    }

    /**
     *
     * @return
     */
    public String getCurrentBlueprint() {
        return currentBlueprint;
    }

    /**
     * registers strain to autocheck for station replacement
     */
    private void RegisterStrain() {
        NPCStationReplacer.addStrainToMap(this);
    }

    @Override
    public String toString() {
        return "EvolutionStrain{" +
                "version=" + version +
                ", originalBlueprint='" + originalBlueprint + '\'' +
                ", currentBlueprint='" + currentBlueprint + '\'' +
                '}';
    }
}
