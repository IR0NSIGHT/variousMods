package me.iron.mining_stations;

import api.DebugFile;
import api.utils.StarRunnable;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.shorts.Short2FloatOpenHashMap;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.*;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.inventory.Inventory;
import org.schema.game.common.data.player.inventory.NoSlotFreeException;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Map;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.07.2021
 * TIME: 20:58
 */
public class MiningUtil {
    static int max_volume_per_crate = 1000; //TODO config
    static int passive_mining_bonus = 10; //multiplier for resources (on top of normal mining bonus)

    /**
     * gets weightmap of ECM and multippies by types volume
     */
    static float getTotalWeightedVolume(ElementCountMap ecm) {
        Short2FloatOpenHashMap weightmap = new Short2FloatOpenHashMap();
        ecm.getWeights(weightmap);
        float volume = 0;
        for (Map.Entry<Short, Float> entry : weightmap.entrySet()) {
            float weight = entry.getValue();
            if (weight == 0) continue;
            short type = entry.getKey();
            volume += ElementKeyMap.getInfo(type).volume * weight;
            //    weightmap.put(type,volume);
        }
        return volume;
    }

    /**
     * fill this crate from these resources, modifies resource map in situ.
     * will reduce map by used resources
     * @param crate target crate to fill, will use first found inventory (so dont use multi invs)
     * @param resources resource map (retrieve through getResources(roid)
     * @param maxFilledVolume maximum volume to fill (usually use static max_volume_per_crate)
     */
    static void fillCrate(SegmentController crate, ElementCountMap resources, float maxFilledVolume) {
        if (!(crate instanceof ManagedSegmentController)) return;
        if (resources.isEmpty()) return;
        ManagedSegmentController msc = (ManagedSegmentController) crate;
        //get first inventory
        Inventory inv = msc.getManagerContainer().getInventories().values().iterator().next();

        double totalV = resources.getVolume();
        //get how many resources to put
        float volumePerUnit = getTotalWeightedVolume(resources);
        float cap = (float) inv.getCapacity();
        float vol = (float) inv.getVolume();
        double freeVolume = Math.min(inv.getCapacity() - inv.getVolume(), maxFilledVolume);
        if (volumePerUnit == 0 || freeVolume == 0) return;
        float units = (float) (freeVolume / volumePerUnit);

        Short2FloatOpenHashMap weightmap = new Short2FloatOpenHashMap();
        resources.getWeights(weightmap);

        //fill inventory
        try {
            int slot = inv.getFreeSlot();
            for (short type : ElementKeyMap.keySet) {
                if (resources.get(type) == 0) continue;
                int total = resources.get(type);
                int max = (int) Math.min(weightmap.get(type) * units, inv.canPutInHowMuch(type, total, -1));
                slot = inv.incExistingOrNextFreeSlot(type, max);
                inv.sendInventoryModification(slot);
                resources.put(type, total - max);
            }
            //    inv.sendAll();
        } catch (NoSlotFreeException e) {
            e.printStackTrace();
        }
    }

    /**
     * retrieves blocks and ore-shards from given segmentcontroller. access with map.get(type)
     * @param roid roid to map
     * @param multiplier multiplier added on top of mining bonus
     * @return elementcountmap of asteroid
     */
    static ElementCountMap getResources(SegmentController roid, int multiplier) {
        //get map of resources and ores
        ElementCountMap ecm = new ElementCountMap();
        roid.railController.fillElementCountMapRecursive(ecm);
        int miningbonus = roid.getMiningBonus(roid);

        //multiply resources with miningbonus and multiplier
        for (short type : ElementKeyMap.keySet) {
            if (ecm.get(type) == 0) continue;
            ecm.put(type, ecm.get(type) * multiplier * miningbonus);
        }

        //get oreCounts from countmap through reflection
        int[] oreCounts = new int[0];
        //try getting ore counts
        try {
            Class aClass = ecm.getClass();
            Field field = aClass.getDeclaredField("oreCounts");
            field.setAccessible(true);
            Object value = field.get(ecm);
            oreCounts = (int[]) value;
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (NoSuchFieldException ex) {
            ex.printStackTrace();
        }

        //translate the resource types into raw shards, put into countmap
        for (int i = 0; i < oreCounts.length; i++) {
            int ore = i;
            int count = oreCounts[i];
            if (count == 0) continue;
            short shardType = ElementKeyMap.resources[ore];
            int amount = count * miningbonus * multiplier;
            ecm.put(shardType, amount);
        }

        return ecm;
    }

    /**
     * spawn a crate for this miner, invoke filling with resources on spawning
     * @param thisMiner miner for which to spawn and use resources
     * @param sector sector in whcih to spawn
     */
    static void spawnCrate(final Miner thisMiner, Vector3i sector) {
        //test for blueprint validitiy

        String blueprint = thisMiner.getCrateBlueprint();
        //get transform = worldposition wrapper
        Transform transform = new Transform();
        transform.setIdentity();
        final SegmentController miner = GameServerState.instance.getSegmentControllersByName().get(thisMiner.getUID());
        if (miner == null) return;
        float radius = miner.getBoundingSphereTotal().radius;
        Vector3f dir = new Vector3f(
                -0.5f + (float) Math.random(),
                -0.5f + (float) Math.random(),
                -0.5f + (float) Math.random());

        dir.scale(radius * 2);
        dir.add(miner.getWorldTransform().origin);
        transform.origin.set(dir);
        //    final Vector3f offset = dir;
        //ModPlayground.broadcastMessage("spawning at: " + transform.origin);
        //TODO give array of bps, choose randomly
        //TODO delayed deleting also from unloaded + across server restarts, abort on "is docked or piloted"
        //create outline = loaded but not yet spawned entity
        SegmentControllerOutline scOutline = null;
        try {
            scOutline = BluePrintController.active.loadBluePrint(
                    GameServerState.instance,
                    blueprint, //catalog entry name
                    "CONTAINER " + System.currentTimeMillis(), //ship name //TODO
                    transform, //transform position
                    -1, //credits to spend
                    0, //faction ID
                    sector, //sector
                    "uwuBoy8000", //spawner
                    PlayerState.buffer, //buffer (?) no idea what that is, worked fine for me as is
                    null,   //segmentpiece, no idea either
                    false, //active ai -> basically fleet AI ship. attacks enemies.
                    new ChildStats(false)); //childstats, no idea what it does
        } catch (EntityNotFountException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EntityAlreadyExistsException e) {
            e.printStackTrace();
        }

        if (scOutline != null) {
            DebugFile.log("outline was loaded, not null!");
            //spawn the outline
            try {
                scOutline.spawn(
                        sector, //dont know what happens if you put loadsector != spawnsector
                        false, //check block counts (?) no idea
                        new ChildStats(false), //no idea again
                        new SegmentControllerSpawnCallbackDirect(GameServerState.instance, sector) { //idk what that is
                            @Override
                            public void onSpawn(final SegmentController segmentController) {
                                final long last = System.currentTimeMillis();
                                super.onSpawn(segmentController);
                                new StarRunnable() {
                                    @Override
                                    public void run() {
                                        if (!segmentController.isFullyLoadedWithDock()) return;
                                        if (last + 1000 > System.currentTimeMillis()) return;
                                        thisMiner.getCrates().add(segmentController.dbId);
                                        MiningUtil.fillCrate(segmentController, thisMiner.getResources(),MiningUtil.max_volume_per_crate);
                                        cancel();
                                    }
                                }.runTimer(ModMain.instance, 1);
                            }

                            @Override
                            public void onNoDocker() { //in vanilla used to write a debug line.
                            }
                        });
            } catch (EntityAlreadyExistsException e) {
                e.printStackTrace();
            } catch (StateParameterNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * will lock roid in place, cant be mined, destroyed or moved
     * @param roid Asteroid to lock down
     */
    public static void lockAsteroid(SegmentController roid) {
        if (roid instanceof FloatingRock) {
            ((FloatingRock)roid).setMoved(true);
            ((FloatingRock)roid).setTouched(true, false);
        }

        //make invulnerable to weapon damage
        roid.setVulnerable(false);

        //unminable relies on StationManager salvage eventhandler and internal hashmap. added to map in registerAsteroid.

        //make immovable but allow rotating
        RigidBody rb = roid.getPhysicsObject();
        rb.setDamping(1.0f,rb.getAngularDamping());

        //make not editable
        //set faction != 0
        roid.setFactionId(-2004);
    }

    /**
     * test if segmentcontroller exists.
     * @param databaseID  sc.getDbId();
     * @param UID sc.getUniqueIdentifier();
     * @return true if exists (loaded or unloaded)
     */
    static boolean existsInDB(long databaseID, String UID) { //TODO first test seems unreliable for roid?
        if(null == GameServerState.instance.getSegmentControllersByName().get(UID)){
            return true;
        }
        try {
            return (null != GameServerState.instance.getDatabaseIndex().getTableManager().getEntityTable().getById(databaseID));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
