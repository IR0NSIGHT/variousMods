package me.iron.npccontrol;

import api.DebugFile;
import api.ModPlayground;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.entity.SegmentControllerInstantiateEvent;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import com.bulletphysics.linearmath.Transform;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import org.schema.common.LogUtil;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SegmentControllerHpController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.network.objects.Sendable;
import org.schema.schine.network.server.ServerMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.04.2021
 * TIME: 21:31
 */
public class NPCStationReplacer {
    public static void addStrainToMap(EvolutionStrain strain) {
        NPCStationReplacer.strainHashMap.put(strain.getOriginalBlueprint(),strain);
    }

    /**
     * maps: original, unwanted blueprint vs new, wanted blueprint.
     * new replaces original
     */
    private static HashMap<String, EvolutionStrain> strainHashMap = new HashMap<String, EvolutionStrain>() {{
    //    put("ENTITY_SPACESTATION_Refueling Platform",new EvolutionStrain("ENTITY_SPACESTATION_Refueling Platform", "REFUELING-PLATFORM"));
    //    put("Small Refueling Platform","SMALL-REFUELING-PLATFORM");
    //    put("ENTITY_SPACESTATION_trading guild temporary factory_1481325355945","KOMBINAT-100");
    //    put("temporary shop_1481328648280","TG_TRADE_HUB");
    //    put("ENTITY_SPACESTATION_Trade Post_1481338111718","TG_TRADE_HUB");
    //    //    put("ENTITY_SPACESTATION_Vault Station","TBD");
        //ENTITY_SPACESTATION_outcast temporary shop_1481328648280
        //ENTITY_SPACESTATION_Trade Post_1481338111718
        //Research Station
    }};

    /**
     * get a collection of the active evolution strains
     * @return collection of evolution strains used by the instantiation listener
     */
    public static Collection<EvolutionStrain> getActiveStrains() {
        return strainHashMap.values();
    }

    /**
     * sets strains to be used in instantiation listener. only to be used by persistence object util!!
     * @param strainCollection
     */
    public static void setActiveStrains(EvolutionStrain[] strainCollection) {
        strainHashMap.clear();
        for (EvolutionStrain strain: strainCollection) {
            strainHashMap.put(strain.getOriginalBlueprint(),strain);
        }
    }

    /**
     * initialises eventhandlers that detect the instantiation of spacestations.
     */
    public static void init() {
        //add listeners for creation and loading of npc stations
        StarLoader.registerListener(SegmentControllerInstantiateEvent.class, new Listener<SegmentControllerInstantiateEvent>() {
            @Override
            public void onEvent(SegmentControllerInstantiateEvent event) {
                if (!event.isServer()) {
                    return;
                }
                //sort out non-stations and non-npc's
                if (!(event.getController() instanceof SpaceStation) || ((event.getController().getFaction() != null && !event.getController().getFaction().isNPC()))) {
                    return;
                }
                ModPlayground.broadcastMessage("Instantiation: " + event.getController().getName() + " of : " + event.getController().getFactionId());
                SpaceStation station = (SpaceStation) event.getController();
                DebugFile.log("Logging NPC station:/n UID: " + station.getUniqueIdentifier() + "/n blueprint: " +station.blueprintIdentifier + "/npath: " + station.blueprintSegmentDataPath );
                replaceFromList(station);

            }
        },ModMain.instance);

        //add chat listener for "admin commands"
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                if (!event.isServer()) {
                    return;
                }
                PlayerState player = GameServer.getServerState().getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender);
                if (player == null) {
                    return;
                }
                if (!GameServerState.instance.isAdmin(player.getName())) {
        //            ModPlayground.broadcastMessage("rejected.");
                    return;
                }
                String text = event.getText();
                if (text.contains("!station")) {
                    //get selected object
                    int selectedID = player.getSelectedEntityId();
                    Sendable selected = (Sendable)GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(selectedID);

                    SpaceStation station = null;
                    if (selected instanceof  SpaceStation) {
                        station = (SpaceStation) selected;
                    }

                    if (text.contains("!station info")) {
                        String bp = station.blueprintIdentifier;
                        EvolutionStrain strain = strainHashMap.get(bp);
                        if (bp == null) {
                            bp = "null";
                        }
                        if (strain == null) {
                            ModPlayground.broadcastMessage("station BP: " + bp + " strain: null");
                            return;
                        }
                         ModPlayground.broadcastMessage("station BP: " + bp + " strain: " + strain.toString());
                        return;
                    }

                    if (text.contains("!station set ")) {
                        text = text.replace("!station set ","");
                        if (station != null) {
                            station.blueprintIdentifier = text;
                            ModPlayground.broadcastMessage("set blueprint identifier to " + text);
                            return;
                        }
                    }
                    if (text.contains("!station add ")) {
                        if (station == null) { //TODO handle pirates and other "nulL" stations
                            if (station.blueprintIdentifier == null) {
                                ModPlayground.broadcastMessage("null ancestor");
                                return;
                            }
                            ModPlayground.broadcastMessage("not a station!");
                            return;
                        }

                        //check if already listed
                        if (strainHashMap.get(station.blueprintIdentifier) != null) {
                            ModPlayground.broadcastMessage("blueprint " + station.blueprintIdentifier + " already exists in replacement list: " + strainHashMap.get(station.blueprintIdentifier));
                            return;
                        }

                        //get replacement blueprint
                        text = text.replace("!station add ","");

                        //check validity of blueprint
                        boolean validBlueprint = isValidBlueprint(text);
                        if (!validBlueprint) {
                            ModPlayground.broadcastMessage("no blueprint of name '" + text + "' was found.");
                            return;
                        }

                        //create strain object                         //add to replacement list (automatic)
                        EvolutionStrain strain = new EvolutionStrain(station.blueprintIdentifier,text);
                        EvolutionStrain control = strainHashMap.get(station.blueprintIdentifier);
                        if (strain.equals(control)) {
                            ModPlayground.broadcastMessage("new evolution strain created: " + control.toString());
                        } else {
                            ModPlayground.broadcastMessage("something went wrong creating the evolution strain.");
                            return;
                        }
                        //replace selected station
                        replaceFromList(station);
                        return;
                    }

                    if (text.contains("!station clear all")) {
                        strainHashMap.clear();
                        ModPlayground.broadcastMessage("deleted all evolution strains.");
                    }

                    //one time replacement operation
                    if (text.contains("!station replace ")) {
                        if (!(selected instanceof SpaceStation)) {
                            ModPlayground.broadcastMessage("not a station!");
                            return;
                        }

                        //get replacement blueprint
                        text = text.replace("!station replace ","");
                        //check validity of blueprint
                        boolean validBlueprint = isValidBlueprint(text);
                        if (!validBlueprint) {
                            ModPlayground.broadcastMessage("no blueprint of name '" + text + "' was found.");
                            return;
                        }

                        replaceFromBlueprint(station,text);
                    }

                    //update selected station
                    if (text.contains("!station update")) {
                        if (station != null) {
                            replaceFromList(station);
                            return;
                        }
                        //Fallthrough
                    }

                    //update an evolution strain to a new blueprint
                    if (text.contains("!station update ")) {//strainname newBP OR selected
                        text = text.replace("!station update ","");
                        String oldBP, newBP;
                        if (!(selected instanceof SpaceStation)) {
                            String[] inputValues = text.split(" ");
                            if (inputValues.length != 2) {
                                ModPlayground.broadcastMessage("not station selected/ expects 2 inputs");
                                return;
                            }
                            oldBP = inputValues[0]; newBP = inputValues[1];
                        } else {
                            oldBP = ((SpaceStation) selected).blueprintIdentifier;
                            newBP = text;
                        }
                        //get replacement blueprint
                        if (!isValidBlueprint(newBP)) {
                            ModPlayground.broadcastMessage("replacement is not a valid blueprint: '" + text + "'");
                            return;
                        }
                        EvolutionStrain strain = strainHashMap.get(oldBP);
                        if (strain == null) {
                            ModPlayground.broadcastMessage(oldBP + " not listed in evolution map");
                            return;
                        }
                        strain.updateStrain(newBP);
                        ModPlayground.broadcastMessage("updated strain to " + strain.toString());
                        if (selected != null) {
                            replaceFromList((SpaceStation) selected);
                        }
                        return;
                    }

                    if (text.contains("!station list")) {
                        for (EvolutionStrain strain: strainHashMap.values()) {
                            ModPlayground.broadcastMessage(strain.toString());
                        }
                        return;
                    }

                    //default
                    ModPlayground.broadcastMessage("command not recognized");
                }
            }
        },ModMain.instance);
    }

    /**
     * will try to replace the given station with a blueprint from its internal list. will do nothing if station original blueprint is not in list.
     * @param original
     * @return
     */
    private static SpaceStation replaceFromList(SpaceStation original) {
        if (original.blueprintIdentifier != null) {
            //test if this blueprint is part of the evolution map
            EvolutionStrain strain =  strainHashMap.get(original.blueprintIdentifier);
            if (strain == null) {
                //not part of evolution.
                return original;
            }

            //get stationVersion of this station
            Integer stationVersion = strain.getStationVersion(original.getUniqueIdentifier());

            //its versioned, get current stationVersion, compare to most recent available blueprint
            int currentVersion = strain.getVersion();
            if (stationVersion >= currentVersion) { //station is up to date, dont replace
                return original;
            }

            //is this one on the "to be replaced list"?
            //get name of new blueprint
            String replacementBlueprint = strainHashMap.get(original.blueprintIdentifier).getCurrentBlueprint();
            if (replacementBlueprint != null) {
                //TODO test if replacementBlueprint is valid blueprint entry
                //replace original with new station
                SpaceStation newStation = replaceFromBlueprint(original, replacementBlueprint);
                strain.UpdateStation(original.getUniqueIdentifier(),newStation.getUniqueIdentifier());
                return newStation;
            }
        }
        return original;
    }

    public static void deleteDocked(SegmentController entity) {
        List<RailRelation> next = entity.railController.next;
        for (RailRelation railRelation : next) {
            SegmentController dockedEntity = railRelation.docked.getSegmentController();

            //recurse
            deleteDocked(dockedEntity);
        }
        entity.markForPermanentDelete(true);
        entity.setMarkedForDeleteVolatile(true);
        entity.setMarkedForDeletePermanentIncludingDocks(true);
    }

    /**
     * loops over all entities in a sector and deletes the ones that contain the given string in their UID.
     * this is a very jank solution in hopes of getting a reference to old docking system turrets on pirate stations.
     * Does NOT delete the given UID itself.
     * @param name
     * @param sector
     * @param factionID
     */
    public static void deleteByName(String name, Vector3i sector, int factionID) {
        ObjectCollection<Sendable> list = GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().values();
        for (Sendable entity: list) {

            if (entity instanceof SegmentController && ((SegmentController) entity).getUniqueIdentifier().contains(name)) {
                if (((SegmentController) entity).getName().equals(name)) {
                    continue; //dont destroy original.
                }
                //only destroy ship in this sector
                if (!((SegmentController) entity).getSector(new Vector3i()).equals(sector)) {
                    continue;
                };
                //dont destroy ships that dont match the given faction.
                if (((SegmentController) entity).getFactionId() != factionID) {
                    continue;
                }
                //kill entity
                DebugFile.log("DESTROY segmentcontroller: " + ((SegmentController) entity).getName() + " matches on: " + name);
                SegmentController thingy = (SegmentController) entity;
                entity.markForPermanentDelete(true);
                entity.setMarkedForDeleteVolatile(true);
            }
        }
    }

    /**
     * replace spacestation with new station of this blueprint.
     * inherits faction, name, position, but not complex attributes like inventory or docks.
     * station is always placed at world 0,0,0.
     * @param original
     * @param newBlueprint
     * @return
     */
    public static SpaceStation replaceFromBlueprint(SpaceStation original, String newBlueprint) {
        //get transform = worldposition wrapper
        Transform transform = new Transform();
        transform.setIdentity();
        transform = original.getWorldTransform();
        transform.origin.set(original.getWorldTransform().origin);

        //log sector and blueprint
        Vector3i sector = original.getSector(new Vector3i());
        ModPlayground.broadcastMessage("spawning at: " + transform.origin);
        String originalBlueprint = original.blueprintIdentifier;

        //create outline = loaded but not yet spawned entity
        SegmentControllerOutline scOutline = null;
        try {
            scOutline = BluePrintController.active.loadBluePrint(
                    GameServerState.instance,
                    newBlueprint, //catalog entry name
                    original.getRealName(), //ship name
                    transform, //transform position
                    -1, //credits to spend
                    original.getFactionId(), //faction ID
                    sector, //sector
                    "Station VCS", //spawner
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
        SpaceStation newStation = null;
        if (scOutline != null) {
            DebugFile.log("outline was loaded, not null!");
            //delete originals docked entities
            original.railController.destroyDockedRecursive();
            for (ElementDocking dock : original.getDockingController().getDockedOnThis()) {
                dock.from.getSegment().getSegmentController().markForPermanentDelete(true);
                dock.from.getSegment().getSegmentController().setMarkedForDeleteVolatile(true);
            }
            //delete original
            original.markForPermanentDelete(true);
            original.setMarkedForDeleteVolatile(true);

            //spawn the outline
            try {
                newStation = (SpaceStation) scOutline.spawn(
                        sector, //dont know what happens if you put loadsector != spawnsector
                        false, //check block counts (?) no idea
                        new ChildStats(false), //no idea again
                        new SegmentControllerSpawnCallbackDirect(GameServerState.instance, sector) { //idk what that is
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
        newStation.blueprintIdentifier = originalBlueprint;
        final SpaceStation s = newStation;
        new StarRunnable() {
            @Override
            public void run() {
                s.warpTransformable(0,0,0, true, null);
                ModPlayground.broadcastMessage("station blueprint: " + s.blueprintIdentifier);
            }
        }.runLater(ModMain.instance,10);
        return newStation;
    }

    /**
     * checks blueprint catalog for given entry. tests if entry with that name exists.
     * @param blueprintName
     * @return
     */
    public static boolean isValidBlueprint(String blueprintName) {
        //TODO find better way than bruteforce search
        Collection<CatalogPermission> allEntries = GameServer.getServerState().getCatalogManager().getCatalog();
        for (CatalogPermission entry: allEntries) {
            if(entry.getUid().equals(blueprintName)) {
                return true;
            }
        }
        return false;
    }
}
