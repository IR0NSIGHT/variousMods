package me.iron.mining_stations;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ElementCountMap;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.FloatingRockManaged;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.server.data.GameServerState;

import javax.validation.constraints.Min;
import javax.vecmath.Vector3f;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.07.2021
 * TIME: 17:09
 * wrapper class for the mining vessel
 */
public class Miner implements Serializable {

    public static int miner_update_time = 10 * 1000; //time for one mining cycle in millis //todo non static

    private String UID;
    public String roidUID; //registered asteroid
    private long roid_db_ID;
    private List<Long> crates = new ArrayList<Long>();
    private int maxCrates = 5;
    private int queuedCrates = 0;
    private String crateBlueprint = "cargo-crate-01";
    private long dbID = -1;
    transient public ElementCountMap resources;

    private transient long nextUpdate = 0;
    private transient SegmentController sc;

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
        ChatUI.sendAll("updating: " + toString());

        //no asteroid, no need to update.
        if (!hasAsteroid())
            return;

        //increment (potential) crates
        if (maxCrates > queuedCrates) queuedCrates++;
        //get rid of to many crates
        while (crates.size() > maxCrates) deleteOldest();

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
        if (roid == null || !roid.isFullyLoadedWithDock()) return; //TODO can this cause bugs?

        //fill element map
        if (resources == null)
            resources = MiningUtil.getResources(roid,MiningUtil.passive_mining_bonus);
        //TODO safeguards so resources == null after persistent load can be handled.
        if (hasAsteroid() && !allowedAsteroid(roid,false)) {
            ChatUI.sendAll("roid is invalid");
            unregisterAsteroid();
        }

        for (int i; queuedCrates > 0; queuedCrates--) {
            if (resources.isEmpty()) break;
            MiningUtil.spawnCrate(this, station.getSector(new Vector3i()));
        }
        if (hasAsteroid() && resources.isEmpty()) {
            unregisterAsteroid();
            ChatUI.sendAll("resources empty.");
        }
        ChatUI.sendAll("resources remaining: " + (int) resources.getVolume());
    }

    private void deleteOldest() {
        if (crates.size() == 0) return;

        GameServerState.instance.destroyEntity(crates.remove(0));
    }

    public ElementCountMap getResources() {
        return resources;
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
        return nextUpdate;
    }

    public void setNextUpdate(long nextUpdate) {
        this.nextUpdate = nextUpdate;
    }

    public boolean hasAsteroid() {
        if (roidUID != null && roid_db_ID != -1) {
            if (!MiningUtil.existsInDB(roid_db_ID,roidUID)) {
                //roid doesnt exist anymore
                StationManager.asteroids.remove(roidUID);
                roid_db_ID = -1;
                roidUID = null;
                return false;
            }
            return true;
        } else {
            return false;
        }
    }
    public boolean registerAsteroid(SegmentController roid) {
        //test if allowed
        if (!allowedAsteroid(roid, true)) return false;
        //register globally
        StationManager.asteroids.put(roid.getUniqueIdentifier(),roid.dbId);
        //register locally
        this.roidUID = roid.getUniqueIdentifier();
        this.roid_db_ID = roid.dbId;
        resources = MiningUtil.getResources(roid, MiningUtil.passive_mining_bonus);
        MiningUtil.lockAsteroid(roid);
        return true;
    }

    /**
     * will unregister and destroy the registered asteroid
     */
    public void unregisterAsteroid() {
        if (roidUID == null || roid_db_ID == -1)  return;
        ChatUI.sendAll("removing roid for miner: " + UID);
        StationManager.asteroids.remove(roidUID);
        SegmentController roid = GameServerState.instance.getSegmentControllersByName().get(roidUID);
        if (roid != null) {
            roid.markForPermanentDelete(true);
            roid.setMarkedForDeleteVolatile(true);
        } else {
            GameServerState.instance.destroyEntity(roid_db_ID);
        }
    }

    private boolean allowedAsteroid(SegmentController roid, boolean assign) {
        //type roid
        if (!(roid instanceof FloatingRock))
            return false;

        //already registered
        if (assign && StationManager.asteroids.get(roid.getUniqueIdentifier())!=null)
            return false;

        //miner already has another roid
        if (assign && this.hasAsteroid())
            return false;

        //station unloaded, cant be close
        SegmentController sc = getSc();
        if (sc == null)
            return false;

        //not same sector
        if (roid.getSectorId() != sc.getSectorId())
            return false;

        //to far away?
        Vector3f worldOffset = roid.getWorldTransform().origin; worldOffset.sub(sc.getWorldTransform().origin);
        if (worldOffset.length() > (sc.getBoundingSphereTotal().radius*3))
            return false;
        return true;
    }

    /**
     * get this miners segmentcontroller. might be null!
     * @return sc if exists, else null
     */
    private SegmentController getSc() {
        if (sc == null)
            sc = GameServerState.instance.getSegmentControllersByName().get(UID);
        return sc;
    }

    @Override
    public String toString() {
        return "Miner{" +
                "UID='" + UID + '\'' +
                ", roidUID='" + roidUID + '\'' +
                ", roid_db_ID=" + roid_db_ID +
                ", crates=" + crates +
                ", maxCrates=" + maxCrates +
                ", queuedCrates=" + queuedCrates +
                ", crateBlueprint='" + crateBlueprint + '\'' +
                ", dbID=" + dbID +
                ", resources=" + resources.getAmountListString() +
                ", nextUpdate=" + nextUpdate +
                '}';
    }
}
