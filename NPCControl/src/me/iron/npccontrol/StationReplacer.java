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
    public static HashMap<Integer,StationReplacer> replacerList = new HashMap<>();

    public int factionID;

    public StationReplacer(int factionID) {
        this.factionID = factionID;
        replacerList.put(factionID,this);
    }

    private ArrayList<String> replacementBlueprints = new ArrayList<String>(){{
        add("UwU Station");
    }};

    //stores stations that were replaced, to avoid double replacement operations
    //stores UID vs blueprint of station
    private HashMap<String,String> managedStations = new HashMap<>();

    /**
     * gets random string of available pirate stations.
     * @return
     */
    public String GetRandomBlueprint() {
        return replacementBlueprints.get((int) (Math.random() * replacementBlueprints.size()));
    }

    /**
     * creates a listener that will replace any pirate station upon loading the station with a random available blueprint, if it wasnt already replaced. replace stations get saved persistently.
     */
    public static void deployListener() {
        StarLoader.registerListener(SegmentControllerInstantiateEvent.class, new Listener<SegmentControllerInstantiateEvent>() {
            @Override
            public void onEvent(SegmentControllerInstantiateEvent event) {
            if (!event.isServer()) {
                return;
            }

            for (StationReplacer replacer: StationReplacer.replacerList.values()) {
                replacer.onInstantiation(event);
            }
            }
        },ModMain.instance);
    }

    public void onInstantiation(SegmentControllerInstantiateEvent event) {
        //sort out non-stations and non-pirates
        if (!(event.getController() instanceof SpaceStation) || (event.getController().getFaction() == null) || ((event.getController().getFaction() != null && event.getController().getFactionId() != factionID))) {
            return;
        }

        ModPlayground.broadcastMessage("Instantiation: " + event.getController().getName() + " of pirate: " + event.getController().getFactionId());
        SpaceStation station = (SpaceStation) event.getController();
        DebugFile.log("Pirate station: UID: " + station.getUniqueIdentifier());

        //sort out already managed, up to date stations
        //fall through: unmanaged stations, managed stations with a blueprint that isn't in the list anymore
        String stationsBP =managedStations.get(station.getUniqueIdentifier());
        if (stationsBP != null && replacementBlueprints.contains(stationsBP)) {
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
        stationUID = stationUID.replace("ENTITY_SPACESTATION_","");
        DebugFile.log("delete pirate ships by UID: " + stationUID);
        NPCStationReplacer.deleteByName(stationUID,station.getSector(new Vector3i()), factionID);

        //create new station
        SpaceStation newStation = NPCStationReplacer.replaceFromBlueprint(station,newBlueprint);


        //write to persistence map
        managedStations.put(newStation.getUniqueIdentifier(),newBlueprint);
    }

    private static void removeDoubleSaves() {
        ArrayList<Object> containers = new ArrayList<>();
        containers.addAll(PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),StationFactionContainer.class));
        ArrayList<Integer> blackList = new ArrayList<>();
        for (Object obj: containers) {
            StationFactionContainer container = (StationFactionContainer) obj;
            //find old saves that dont exist in game anymore and double entries.
            if (StationReplacer.replacerList.get(container.factionID) == null || blackList.contains(container.factionID)) {
                PersistentObjectUtil.removeObject(ModMain.instance.getSkeleton(),obj);
                continue;
            }
            blackList.add(container.factionID);
        }
    }

    public static void savePersistentAll() {
        removeDoubleSaves();

        //--------------------------------------------------------------
        for (StationReplacer replacer: replacerList.values()) {
            replacer.savePersistent();
        }
    }

    public static void loadPersistentAll() {
        removeDoubleSaves();

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
        //get container
        StationFactionContainer myContainer = null;
        ArrayList<Object> containers = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),StationFactionContainer.class);
        StationFactionContainer iteratorContainer = null;
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

        //save
        PersistentObjectUtil.save(ModMain.instance.getSkeleton());
    }

    public void loadPersistent(int factionID) {
        //get container
        StationFactionContainer myContainer = null;
        ArrayList<Object> containers = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),StationFactionContainer.class);
        StationFactionContainer iteratorContainer = null;
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

    public void destroyListener() {

    }
 }
