package me.iron.npccontrol.stationReplacement;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.entity.SegmentControllerInstantiateEvent;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import me.iron.npccontrol.ModMain;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation;
import java.util.ArrayList;
import java.util.HashMap;

//TODO automatic cleaing fully deleted UIDs from "managed stations"

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

    /**
     * removes a factions replacer from the list of active replacers.
     * @param factionID
     */
    public static void removeFromList(int factionID) {
        replacerList.remove(factionID);
    }

    /**
     * returns value copy of the list of active replacer objects.
     * @return
     */
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
        }, ModMain.instance);
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

    /**
     * removes double entries with the same faction ID from the permanent data
     */
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

    /**
     * saves all existing replacers to persisntent data
     */
    public static void savePersistentAll() {
        DebugFile.log("attempting to save all replacers to POU.");
        clearOldSaves();
        removeDoubleSaves();
        //--------------------------------------------------------------
        for (StationReplacer replacer: replacerList.values()) {
            replacer.savePersistent();
        }
    }

    /**
     * replaces all replacers with new, loaded ones from persistnent data
     */
    public static void loadPersistentAll() {
        DebugFile.log("attempting to load station replacers from persistent data");
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
    //----------------------------- non static stuff

    public int factionID;

    /**
     * list of available replacement blueprints for this faction. names of the entries in catalogmanager.
     */
    private ArrayList<String> replacementBlueprints = new ArrayList<String>();

    //stores stations that were replaced, to avoid double replacement operations
    //stores UID vs blueprint of station
    private HashMap<String,String> managedStations = new HashMap<>();

    /**
     * get value copy of managed stations: UID vs blueprintname
     * @return
     */
    public HashMap<String,String> getManagedStations() {
        return new HashMap<>(managedStations);
    }

    /**
     * add this station to the replacers list of managed stations, citing its construction blueprint (for future updates).
     * will stop the replacer in the future from touching that station.
     * @param station
     * @param blueprint
     */
    public void addToManaged(SpaceStation station, String blueprint) {
        managedStations.put(station.getUniqueIdentifier(),blueprint);
    }

    /**
     * test if the given station UID is listed as an already replaced station.
     * @param UID
     * @return
     */
    public String getManaged(String UID) {
        return managedStations.get(UID);
    }

    /**
     * test if a blueprint is listed for this repalcers allowed blueprints.
     * @param blueprintName
     * @return
     */
    public boolean existsInList(String blueprintName) {
        return (this.replacementBlueprints.contains(blueprintName));
    }

    /**
     * construct a replacer for this faction. adds it to the static map for listening, saving, loading.
     * Does not contain any replacement blueprints by default.
     * @param factionID
     */
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
        String stationBlueprintName = getManaged(station.getUniqueIdentifier());
        if (stationBlueprintName != null && existsInList(stationBlueprintName)) {
            DebugFile.log("station " + station.getRealName() + "is in managed list with known blueprint");
            return;
        }

        //get new blueprint
        String newBlueprint = GetRandomBlueprint();

        //test if blueprint exists
        if (!StationHelper.isValidBlueprint(newBlueprint)) {
            return;
        }

        //delete old docking system turrets that are not registered in sc.getDockedOnThis
        //TODO make safer method
        String stationUID = station.getUniqueIdentifier();
        managedStations.put(stationUID,"vanilla spawned");
        stationUID = stationUID.replace("ENTITY_SPACESTATION_","");
        DebugFile.log("delete pirate ships by UID: " + stationUID);
        StationHelper.deleteByName(stationUID,station.getSector(new Vector3i()), factionID);
        //delete new system docks
        StationHelper.deleteDocked(station);

        //create new station
        SpaceStation newStation = StationHelper.replaceFromBlueprint(station,newBlueprint);

        //write to persistence map
        managedStations.put(newStation.getUniqueIdentifier(),newBlueprint);
        DebugFile.log("adding " + newStation.getRealName() + " to managed stations");
    }

    /**
     * saves this instance of replacer to persistent data
     */
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
        DebugFile.log("saved station replacer for " + factionID + " to POU");
    }

    /**
     * loads + overwrites managed stations and replacementblueprints into this instance.
     * @param factionID
     */
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
        DebugFile.log("Loaded station replacer for " + factionID + " from POU");
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

    /**
     * get list of available blueprints
     * @return
     */
    public ArrayList<String> getBlueprints() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(replacementBlueprints);
        return list;
    }
 }
