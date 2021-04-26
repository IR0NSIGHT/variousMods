package me.iron.npccontrol;

import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.entity.SegmentControllerInstantiateEvent;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 19.04.2021
 * TIME: 20:23
 */
public class StationReplacer {
    private static boolean hasListener = false;
    private static HashMap<Integer,StationReplacer> replacerList = new HashMap<>();
    public static ArrayList<StationReplacer> allReplacersDEBUG = new ArrayList<>();
    public static StationReplacer getFromList(int factionID) {
        return replacerList.get(factionID);
    }

    public static void addToList(StationReplacer replacer) {
        replacerList.put(replacer.factionID,replacer);
    }

    public static void removeFromList(int factionID) {
        replacerList.remove(factionID);
    }

    public static HashMap<Integer,StationReplacer> getList() {
        HashMap<Integer, StationReplacer> map = new HashMap<>(replacerList);
        return map;
    }

    /**
     * creates a listener that will replace any pirate station upon loading the station with a random available blueprint, if it wasnt already replaced. replace stations get saved persistently.
     */
    public static void deployListener() {
        if (hasListener) {
            return;
        }
        DebugFile.log("deploying instantiation listener for StationReplacer.class");
        StarLoader.registerListener(SegmentControllerInstantiateEvent.class, new Listener<SegmentControllerInstantiateEvent>() {
            @Override
            public void onEvent(SegmentControllerInstantiateEvent event) {
            if (!event.isServer()) {
                return;
            }
            if (!(event.getController() instanceof SpaceStation)) {
                return;
            }
            String name = " idk some bullshit probably ";
            if (event.getController() != null && event.getController().getName() != null) {
                name = event.getController().getName();
            }
            DebugFile.log("firing onInstantiaton method for replacers for " + event.getController().getName());

          for (StationReplacer replacer: StationReplacer.replacerList.values()) {
              if (replacer.factionID == event.getController().getFactionId()) {
                  replacer.onInstantiation(event);
              }
          }
            }
        },ModMain.instance);
    }
    //----------------------------- non static stuff

    public int factionID;

    private ArrayList<String> replacementBlueprints = new ArrayList<String>();

    //stores stations that were replaced, to avoid double replacement operations
    //stores UID vs blueprint of station
    private HashMap<String,String> managedStations = new HashMap<>();

    public void addToManaged(SpaceStation station, String blueprint) {
        managedStations.put(station.getUniqueIdentifier(),blueprint);
    }

    public boolean isManaged(String UID) {
        return (managedStations.get(UID)==null);
    }
    public StationReplacer(int factionID) {
        this.factionID = factionID;
        replacerList.put(factionID,this);
        allReplacersDEBUG.add(this);
    }

    /**
     * gets random string of available pirate stations.
     * @return
     */
    public String GetRandomBlueprint() {
        return replacementBlueprints.get((int) (Math.random() * replacementBlueprints.size()));
    }



    /**
     * fires if a segmentcontroller of that faction is instantiated.
     * @param event
     */
    public void onInstantiation(SegmentControllerInstantiateEvent event) {
        if (event.getController() == null) {
            return;
        }

        //sort out non-stations and unwanted faction
        if (!(event.getController() instanceof SpaceStation) || (event.getController().getFaction() == null) || ((event.getController().getFaction() != null && event.getController().getFactionId() != factionID))) {
            return;
        }
        SpaceStation station = (SpaceStation) event.getController();

        //sort out already managed, up to date stations
        //fall through: unmanaged stations, managed stations with a blueprint that isn't in the list anymore
        String stationsBP = managedStations.get(station.getUniqueIdentifier());
        if (stationsBP != null) {
            DebugFile.log("station " + station.getRealName() + "is in managed list");
            if (replacementBlueprints.contains(stationsBP)) {
                DebugFile.log("stations blueprint is up to date.");
                return;
            }
            return;
        }

        //get new blueprint
        String newBlueprint = GetRandomBlueprint();

        //test if blueprint exists
        if (!NPCStationReplacer.isValidBlueprint(newBlueprint)) {
            return;
        }

        //delete old docking system turrets that are not registered in sc.getDockedOnThis
        //TODO make safer method
        String stationUID = station.getUniqueIdentifier();
        managedStations.put(stationUID,"uwu");
        stationUID = stationUID.replace("ENTITY_SPACESTATION_","");
        DebugFile.log("delete pirate ships by UID: " + stationUID);
        NPCStationReplacer.deleteByName(stationUID,station.getSector(new Vector3i()), factionID);
        //delete new system docks
        NPCStationReplacer.deleteDocked(station);

        //create new station
        SpaceStation newStation = NPCStationReplacer.replaceFromBlueprint(station,newBlueprint);

        //write to persistence map
        managedStations.put(newStation.getUniqueIdentifier(),newBlueprint);
        DebugFile.log("adding " + newStation.getRealName() + " to managed stations");
        ModPlayground.broadcastMessage("station " + stationUID + " was replaced with " + newStation.getRealName());
    }

    /**
     * gets rid of all saves that dont have a loaded counterpart ingame (used when server is shutting down to kill obsolete replacers)
     */
    private static void clearOldSaves() {
        ArrayList<Object> containers = new ArrayList<>();
        containers.addAll(PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),StationFactionContainer.class));
        for (Object obj: containers) {
            StationFactionContainer container = (StationFactionContainer) obj;
            //find old saves that dont exist in game anymore and double entries.
            if (StationReplacer.replacerList.get(container.factionID) == null) {
                DebugFile.log("[DELETE] killing obsolete replacer: " + container.toString());
                PersistentObjectUtil.removeObject(ModMain.instance.getSkeleton(),container);
            }
        }
    }
    private static void removeDoubleSaves() {
        ArrayList<Object> containers = new ArrayList<>();
        containers.addAll(PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),StationFactionContainer.class));
        ArrayList<Integer> blackList = new ArrayList<>();
        for (Object obj: containers) {
            StationFactionContainer container = (StationFactionContainer) obj;
            //find old saves that dont exist in game anymore and double entries.
            if (blackList.contains(container.factionID)) {
                PersistentObjectUtil.removeObject(ModMain.instance.getSkeleton(),container);
                continue;
            }
            blackList.add(container.factionID);
        }
    }

    public static void savePersistentAll() {
        clearOldSaves();
        removeDoubleSaves();
        //--------------------------------------------------------------
        for (StationReplacer replacer: replacerList.values()) {
            replacer.savePersistent();
        }
    }

    public static void loadPersistentAll() {
        removeDoubleSaves();
        ArrayList<Object> l1 = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),Object.class);
        ArrayList<Object> containers = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),StationFactionContainer.class);
        for (Object container: containers) {
            StationFactionContainer dataContainer = (StationFactionContainer) container;
            StationReplacer replacer = StationReplacer.replacerList.get(dataContainer.factionID);
            if (replacer == null) {
                replacer = new StationReplacer(dataContainer.factionID);
            }
            replacer.loadPersistent(dataContainer.factionID);
        }
    }

    public void savePersistent() {
        DebugFile.log("[SAVE] stationreplacer for: " + factionID);
        //get container
        StationFactionContainer myContainer = null, iteratorContainer = null;

        ArrayList<Object> containers = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),StationFactionContainer.class);

        for (Object x : containers) { //TODO autocast whole arraylist
            iteratorContainer = (StationFactionContainer) x;
            if (iteratorContainer.factionID == factionID) {
                myContainer = iteratorContainer;
                break;
            }
        }

        //create new one if needed
        if (myContainer == null) {
            myContainer = new StationFactionContainer();
            PersistentObjectUtil.addObject(ModMain.instance.getSkeleton(),myContainer);
        }

        //write data
        myContainer.factionID = factionID;
        myContainer.stations = managedStations;
        myContainer.replacementBlueprints = replacementBlueprints;

        //save
        PersistentObjectUtil.save(ModMain.instance.getSkeleton());
    }

    public void loadPersistent(int factionID) {
        //get container
        StationFactionContainer myContainer = null, iteratorContainer;
        ArrayList<Object> containers = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),StationFactionContainer.class);
        for (Object x : containers) { //TODO autocast whole arraylist
            iteratorContainer = (StationFactionContainer) x;
            if (iteratorContainer.factionID == factionID) {
                myContainer = iteratorContainer;
                break;
            }
        }
        if (myContainer == null) {
            DebugFile.log("could not find data container with managed pirate stations.",ModMain.instance);
            return;
        }

        managedStations = myContainer.stations;
        replacementBlueprints = myContainer.replacementBlueprints;

    }

    /**
     * adds blueprint to list of usable blueprints for this faction.
     * @return false if already in list, true if added.
     */
    public boolean addBlueprint(String blueprintName) {
        if (replacementBlueprints.contains(blueprintName)) {
            return false;
        }
        replacementBlueprints.add(blueprintName);
        savePersistent();
        return true;
    }

    /**
     * removes the blueprint from available list.
     * @param blueprintName
     * @return fasle if not in list, true if removed
     */
    public boolean removeBlueprint(String blueprintName) {
        if (!replacementBlueprints.contains(blueprintName)) {
            return false;
        }
        replacementBlueprints.remove(blueprintName);
        return true;
    }

    public ArrayList<String> getBlueprints() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(replacementBlueprints);
        return list;
    }
 }
