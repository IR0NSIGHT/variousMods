package me.iron.mining_stations;

import java.io.Serializable;
import java.util.HashMap;

class PersistentContainer implements Serializable {
    private HashMap<String, Miner> miners = new HashMap<String, Miner>();

    public HashMap<String, Miner> getMiners() {
        return miners;
    }

    public void setMiners(HashMap<String, Miner> miners) {
        this.miners = miners;
    }

    public HashMap<String, String> getAsteroids() {
        HashMap<String, String> asteroids = new HashMap<>();
        for (Miner m: miners.values()) {
            if (m.hasAsteroid()) {
                asteroids.put(m.roidUID, m.getUID());
            }
        }
        return asteroids;
    }

    public PersistentContainer() {

    }

}
