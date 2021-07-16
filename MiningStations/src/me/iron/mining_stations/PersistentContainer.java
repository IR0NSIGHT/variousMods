package me.iron.mining_stations;

import java.io.Serializable;
import java.util.HashMap;

class PersistentContainer implements Serializable {
    private HashMap<String, Miner> miners = new HashMap<String, Miner>();
    private HashMap<String, Long> asteroids = new HashMap<String, Long>();

    public HashMap<String, Miner> getMiners() {
        return miners;
    }

    public void setMiners(HashMap<String, Miner> miners) {
        this.miners = miners;
    }

    public HashMap<String, Long> getAsteroids() {
        return asteroids;
    }

    public void setAsteroids(HashMap<String, Long> asteroids) {
        this.asteroids = asteroids;
    }

    public PersistentContainer() {

    }

}