package me.iron.mining_stations;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.block.SegmentPieceSalvageEvent;
import api.listener.events.entity.SegmentControllerFullyLoadedEvent;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.world.SectorInformation;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.07.2021
 * TIME: 14:40
 */
public class StationManager {
    static HashMap<String, Miner> miners = new HashMap<String, Miner>();
    transient static HashMap<String,String> roidsByMiner = new HashMap<String,String>();
    /**
     * initializes stationmanager. adds listener and update cycle
     */
    public static void init() {
        DebugFile.log("INIT FOR STATION MANAGER",ModMain.instance);
        new MiningCylce().runTimer(ModMain.instance,10);
        StarLoader.registerListener(SegmentControllerFullyLoadedEvent.class, new Listener<SegmentControllerFullyLoadedEvent>() {
            @Override
            public void onEvent(SegmentControllerFullyLoadedEvent event) {
                Miner m = miners.get(event.getController().getUniqueIdentifier());
                if (m != null) {
                    ChatUI.sendAll("miner was loaded in, updating.");
                    SegmentController station = event.getController();
                    SegmentController roid = m.getAsteroid();
                    //auto checks that roid and station are loaded
                    m.loadedUpdate(station,roid);
                }

                if (StationManager.isRegistered(event.getController().getUniqueIdentifier())){
                    ChatUI.sendAll("asteroid was loaded in, locking.");
                    //registered roid
                    MiningUtil.lockAsteroid(event.getController());
                }

            }
        },ModMain.instance);
        //no salvage for registered roids
        StarLoader.registerListener(SegmentPieceSalvageEvent.class, new Listener<SegmentPieceSalvageEvent>() {
            @Override
            public void onEvent(SegmentPieceSalvageEvent event) {
                String hitUID = event.getBlockInternal().getSegmentController().getUniqueIdentifier();
                if (StationManager.isRegistered(hitUID)) {
                    ChatUI.sendAll("is a registered roid");
                    event.setCanceled(true);
                }
                SegmentController hitter = event.getSegmentController();
                if (hitter.railController.isDocked()) {
                    RailRelation parent = hitter.railController.previous;
                    while (parent.getRailRController().previous != null) {
                        parent = parent.getRailRController().previous;
                    }
                    hitter = parent.rail.getSegmentController();
                }
                if (validMiner(hitter)==0) {
                    //register as miner if not already one
                    Miner m = miners.get(hitter.getUniqueIdentifier());
                    if (m == null) {
                        m = attemptMakeMiner(hitter);
                    }
                    //Todo send nearby players the error message
                    assert m != null : "miner is null";

                    int code = m.allowedAsteroid(event.getBlockInternal().getSegmentController());
                    if (code == 0) {  //has no registered roid
                        if (!m.hasAsteroid()) {
                            boolean succ = m.registerAsteroid(event.getBlockInternal().getSegmentController());
                            ChatUI.sendAll("registered roid" + succ);
                        } else {
                            ChatUI.sendAll("dude its already registered, leave me alone!");
                        }

                    } else {
                        ChatUI.sendAll("invalid roid:" + code);
                    }
                }
                    //mining beam comes from a station,
            }
        },ModMain.instance);

    }

    /**
     * tests if asteroid is registered, fixes bugged roids by unregistering them as a sideeffect
     * @param UID
     * @return
     */
    static boolean isRegistered(String UID) {
        String miner = roidsByMiner.get(UID);
        Miner m = miners.get(miner);
        if (m == null)
            return false;
        if (!m.hasAsteroid() || !m.getRoidUID().equals(UID)) {
            roidsByMiner.remove(UID);
            return false;
        }
        return true;
    }
    static void removeMiner(String UID) {
        DebugFile.log("removing miner: "+UID);
        ChatUI.sendAll("removed " + UID);
        Miner m = miners.get(UID);
        m.unregisterAsteroid();
        //TODO invoke deletion of crates
        miners.remove(UID);
    }

    /**
     * attempt to make this segmentcontroller a passive miner.
     * returns already existing or new miner
     * @param sc segmentcontroller
     * @return created or already existing miner
     */
    static Miner attemptMakeMiner(SegmentController sc) {
        if (sc == null)
            return null;
        if (0 != validMiner(sc))
            return null;
        Miner m = miners.get(sc.getUniqueIdentifier());
        if (m != null)
            return m;
        m = new Miner(sc);
        miners.put(sc.getUniqueIdentifier(),m);
        ChatUI.sendAll("made " + sc.getName() + " a miner");
        return m;
    }

    /**
     * return string that explains what a station has to offer to be a valid miner.
     * @return string
     */
    static String getMinerConditions() {
        return "Conditions for a valid Miner are: \n" +
                "- Spacestation \n" +
                "- not a homebase \n" +
                "- MORE STUFF"; //TODO more conditions idk
    }

    /**
     * test if station is allowed to be a miner
     * @param sc segmentcontroller
     * @return 0 for allowed, 1..x for error code. use getError for hurt condition.
     */
    static int validMiner(SegmentController sc) {
        if (!(sc instanceof SpaceStation))
            return 1;
        if (((SpaceStation)sc).isHomeBase())
            return 2;
        Vector3i sector = sc.getSector(new Vector3i());
        StellarSystem sys;
        try {
            sys = GameServerState.instance.getUniverse().getStellarSystemFromSecPos(sector);
        }catch (IOException e) {
            e.printStackTrace();
            return -1; //internal error contant mod author
        }
        SectorInformation.SectorType t = sys.getCenterSectorType();
    //    if (!MiningConfig.allowed_system_type.getValue().contains(t))
    //        return 2; //no star in system

        return 0;
    }

    /**
     * will load asteroids and miners from persistence.
     */
    static void loadFromPersistent() {
        PersistentContainer container = getOrNewContainer();
        miners.putAll(container.getMiners());
        roidsByMiner.putAll(container.getAsteroids());
    }

    /**
     * gets first container object, will delete the rest of them.
     * will create and add new one if needed
     * @return
     */
    private static PersistentContainer getOrNewContainer() {
        List containers = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(), PersistentContainer.class);
        PersistentContainer container;
        if (containers.size() == 0) {
            //make new container
            container = new PersistentContainer();
            PersistentObjectUtil.addObject(ModMain.instance.getSkeleton(),container);
        } else {
            container = (PersistentContainer) containers.get(0);
        }
        if (containers.size() > 1) {
            //to many containers
            for (int i = containers.size(); i > 1; i--) {
                PersistentObjectUtil.removeObject(ModMain.instance.getSkeleton(), containers.get(i));
            }
            PersistentObjectUtil.save(ModMain.instance.getSkeleton());
            assert  PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(), PersistentContainer.class).size() == 1;
        }
        return container;
    }

    /**
     * will save asteroids and miners persistently.
     */
    static void saveToPersistent() {
        PersistentContainer container = getOrNewContainer();
        container.setMiners(miners);

        PersistentObjectUtil.save(ModMain.instance.getSkeleton());
    }

    static void destroySaveFile() {
        PersistentContainer container = getOrNewContainer();
        PersistentObjectUtil.removeObject(ModMain.instance.getSkeleton(),container);
        PersistentObjectUtil.save(ModMain.instance.getSkeleton() );
    }
}
