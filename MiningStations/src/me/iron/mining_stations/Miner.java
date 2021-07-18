package me.iron.mining_stations;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.GameServerState;

import javax.vecmath.Vector3f;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.07.2021
 * TIME: 17:09
 * wrapper class for the mining vessel
 */
public class Miner implements Serializable {

    //internal technic stuff
    private String UID;
    private long dbID;

    String roidUID; //registered asteroid
    long roid_db_ID;

    private List<Long> crates = new ArrayList<Long>();
    private HashMap<Long,String> crateUIDs = new HashMap<>();
    private int queuedCrates = 0;
    private String crateBlueprint = "cargo-crate-01";

    private transient ElementCountMap resources;
    private transient long lastUpdate = 0;

    //upgradeable values that define how "good" a station is
    private int maxCrates = 5; //how many crates can exist, without being deleted when a new one is spawned
    private int time_level = 0; //update time improved with each level

    public Miner(SegmentController sc) {
        this.dbID = sc.getDbId();
        this.UID = sc.getUniqueIdentifier();
    }

    /**
     * will increase the queued crates.
     * If miner is loaded:
     * will spawn all queued crates.
     * will delete the old crates.
     */
    public void update() {
        ChatUI.sendAll("updating: " + UID);
        lastUpdate = System.currentTimeMillis();
        removeDeadFromCrates();
        deleteOldest();

        //no asteroid, no need to update.
        if (!hasAsteroid()) {
            resources = null;
            return;
        }

        //increment (potential) crates
        if (maxCrates > queuedCrates) queuedCrates++;

        //no resource map and unloaded roid => cant get resources, return
        if (resources == null && GameServerState.instance.getSegmentControllersByName().get(roidUID)==null)
            return;

        //test if station and roid are loaded -> can spawn crates etc
        SegmentController station = GameServerState.instance.getSegmentControllersByName().get(UID);
        if (station == null || !station.isFullyLoadedWithDock()) { //TODO figure out how long unloading takes on dedicated
            ChatUI.sendAll("unloaded update: " + this.UID + " queued: " + queuedCrates);
            return;
        }
        SegmentController roid = GameServerState.instance.getSegmentControllersByName().get(roidUID);
        if (roid == null || !roid.isFullyLoadedWithDock())
            return; //TODO can this cause bugs?

        assert station != null && roid != null;

        loadedUpdate(station,roid);
    }

    /**
     * triggered by internal update and when the station is loaded in. doesnt change values just spawns crates
     * @param station
     * @param roid
     */
    public void loadedUpdate(SegmentController station, SegmentController roid) {
        if (station == null || roid == null)
            return;
        if (!station.isFullyLoadedWithDock() || !roid.isFullyLoaded())
            return;
        //fill element map
        if (resources == null) { //assigning new resources resets crate queue
            resources = MiningUtil.getResources(roid, MiningConfig.passive_mining_bonus.getValue());
            queuedCrates = 0;
        }


        assert resources != null;

        //TODO safeguards so resources == null after persistent load can be handled.
        int code = allowedAsteroid(roid);
        if (0 != code) {
            ChatUI.sendAll("roid is invalid " + code);
            unregisterAsteroid();
            return;
        }

        while (queuedCrates > 0 && !resources.isEmpty()) {
            queuedCrates--;
            MiningUtil.spawnCrate(this, station.getSector(new Vector3i()));
        }

        if (resources.isEmpty()) {
            unregisterAsteroid();
            ChatUI.sendAll("resources empty.");
        }
        ChatUI.sendAll("resources remaining: " + (int) resources.getVolume());
    }

    public void deleteOldest() { //TODO make me 100% reliable, bc sometimes >maxCrates coexist for a short time

        if (crates.size() <= maxCrates)
            return;
        for (int i = 0; i < crates.size() - maxCrates; i++) {
            long db_id = crates.get(i);
            String crate_uid = crateUIDs.get(db_id);
            if (!MiningUtil.existsInDB(db_id,crate_uid))
                continue;
            SegmentController crate = GameServerState.instance.getSegmentControllersByName().get(crate_uid);
            if (crate != null) {
                crate.markForPermanentDelete(true);
                crate.setMarkedForDeleteVolatile(true);
            } else {

                try {
                    GameServerState.instance.destroyEntity(db_id);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void removeDeadFromCrates() {
        for (int i = 0; i < crates.size(); i++) {
            long db_id = crates.get(i);
            String crate_uid = crateUIDs.get(db_id);
            if (!MiningUtil.existsInDB(db_id,crate_uid)) {
                crates.remove(db_id);
                crateUIDs.remove(db_id);
            }
        }
    }
    public ElementCountMap getResources() {
        return resources;
    }

    public String getRoidUID() {
        return roidUID;
    }

    public List<Long> getCrates() {
        return crates;
    }

    public int getMaxCrates() {
        return maxCrates;
    }

    public int getQueuedCrates() {
        return queuedCrates;
    }

    public String getCrateBlueprint() {
        return crateBlueprint;
    }

    public String getUID() {
        return UID;
    }

    public long getDbID() {
        return dbID;
    }

    public long getNextUpdate() {
        int timeToNext = MiningConfig.miner_update_time.getValue()*(100-this.time_level* MiningConfig.miner_improvement_per_level.getValue())/100;
        return lastUpdate + timeToNext;
    }

    /**
     * attempts to get the asteroid. might be null if non-existent or unlaoded.
     * @return
     */
    public SegmentController getAsteroid() {
        if (!hasAsteroid()) return null;
        return GameServerState.instance.getSegmentControllersByName().get(roidUID);
    }
    public boolean hasAsteroid() {
        if (roidUID != null && roid_db_ID != -1) {
            if (!MiningUtil.existsInDB(roid_db_ID,roidUID)) {
                //roid doesnt exist anymore
                StationManager.roidsByMiner.remove(roidUID);
                roid_db_ID = -1;
                roidUID = null;
                return false;
            }
            return true;
        } else {
            roid_db_ID = -1;
            roidUID = null;
            return false;
        }
    }

    public boolean registerAsteroid(SegmentController roid) {
        //test if allowed
        int code = allowedAsteroid(roid);
        if (0 != code) {
            ChatUI.sendAll("roidcode:"+code);
            return false;
        }
        roid.setAllTouched(true);
        if (roid.dbId == -1) {
            ChatUI.sendAll("db id == -1");
            return false;
        }

        //register globally
        StationManager.roidsByMiner.put(roid.getUniqueIdentifier(),this.UID);
        //register locally
        this.roidUID = roid.getUniqueIdentifier();
        this.roid_db_ID = roid.dbId;
        resources = MiningUtil.getResources(roid, MiningConfig.passive_mining_bonus.getValue());
        MiningUtil.lockAsteroid(roid);
        return true;
    }

    /**
     * will unregister and destroy the registered asteroid
     */
    public void unregisterAsteroid() {
        if (roidUID == null || roid_db_ID == -1)  return;
        ChatUI.sendAll("removing roid for miner: " + UID);
        StationManager.roidsByMiner.remove(roidUID);
        SegmentController roid = GameServerState.instance.getSegmentControllersByName().get(roidUID);
        if (roid != null) {
            roid.markForPermanentDelete(true);
            roid.setMarkedForDeleteVolatile(true);
        } else {
            GameServerState.instance.destroyEntity(roid_db_ID);
        }
    }

    /**
     * tests if this roid is allowed to be registered to this miner.
     * DOES NOT TEST FOR DOUBLE REGISTEREING!
     * @param roid
     * @return 0 for okay, 1..x for error code
     */
    public int allowedAsteroid(SegmentController roid) {
        //type roid
        if (!(roid instanceof FloatingRock))
            return 1;

        //miner already has another roid
        if (this.hasAsteroid() && !this.roidUID.equals(roid.getUniqueIdentifier()))
            return 2;

        //already registered
        if (StationManager.isRegistered(roid.getUniqueIdentifier()) && this.hasAsteroid() && !this.roidUID.equals(roid.getUniqueIdentifier()))
            return 3;



        //station unloaded, cant be close
        SegmentController sc = getSc();
        if (sc == null)
            return -1;

        //not same sector
        if (roid.getSectorId() != sc.getSectorId())
            return 4;

        //to far away?
        Vector3f worldOffset = roid.getWorldTransform().origin; worldOffset.sub(sc.getWorldTransform().origin);
        if (worldOffset.length() > (sc.getBoundingSphereTotal().radius*3))
            return 5;
        return 0;
    }

    /**
     * get this miners segmentcontroller. might be null!
     * @return sc if exists, else null
     */
    private SegmentController getSc() {
        return GameServerState.instance.getSegmentControllersByName().get(UID);
    }

    public void registerCrate(SegmentController crate) {
        this.crates.add(crate.dbId);
        this.crateUIDs.put(crate.dbId,crate.getUniqueIdentifier());
    }
    @Override
    public String toString() {
        return "Miner{" +
                "UID='" + UID + '\'' + "\n" +
                "roidUID='" + roidUID + '\'' + "\n" +
                "maxCrates=" + maxCrates + "\n" +
                "existing Crates=" + crates.toString() + "\n" +
                "queuedCrates=" + queuedCrates + "\n" +
                "resources=" + ((resources != null)? resources.getAmountListString():"null resources") + "\n" +
                "update in: =" + (getNextUpdate() - System.currentTimeMillis())/1000 + "\n" +
                "loaded: " + (getSc()!=null)+
                '}';
    }
}
