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
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;
import org.schema.schine.network.objects.Sendable;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.04.2021
 * TIME: 21:31
 */
public class NPCStationReplacer {
    /**
     * maps: original, unwanted blueprint vs new, wanted blueprint.
     * new replaces original
     */
    private static HashMap<String,String> blueprintReplacementList = new HashMap<String,String>() {{
        put("ENTITY_SPACESTATION_Refueling Platform","REFUELING-PLATFORM");
        put("Small Refueling Platform","SMALL-REFUELING-PLATFORM");
        put("ENTITY_SPACESTATION_trading guild temporary factory_1481325355945","KOMBINAT-100");
        put("temporary shop_1481328648280","TG_TRADE_HUB");
        put("ENTITY_SPACESTATION_Trade Post_1481338111718","TG_TRADE_HUB");
        //    put("ENTITY_SPACESTATION_Vault Station","TBD");
        //ENTITY_SPACESTATION_outcast temporary shop_1481328648280
        //ENTITY_SPACESTATION_Trade Post_1481338111718
        //Research Station
    }};

    /**
     * stores the UIDs of the clones which replaced unwanted stations. is used to test if a station was already replaced.
     */
    private static HashSet<String> clones = new HashSet<String>(){{
            add("uwuEmpirePrimeStation");
        }};

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
                ModPlayground.broadcastMessage("Instantiation: " + event.getController().getName() + " of : " + event.getController().getFaction().getName());
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
                    ModPlayground.broadcastMessage("rejected.");
                    return;
                }
                ModPlayground.broadcastMessage("accepted");
                String text = event.getText();
                if (text.contains("!station")) {
                    //get selected object
                    int selectedID = player.getSelectedEntityId();
                    Sendable selected = (Sendable)GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().get(selectedID);
                    if (!(selected instanceof SpaceStation)) {
                        ModPlayground.broadcastMessage("not a station!");
                        return;
                    }
                    SpaceStation station = (SpaceStation) selected;

                    if (text.contains("!station add ")) {
                        //check if already listed
                        if (blueprintReplacementList.get(station.blueprintIdentifier) != null) {
                            ModPlayground.broadcastMessage("blueprint " + station.blueprintIdentifier + " already exists in replacement list: " + blueprintReplacementList.get(station.blueprintIdentifier));
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
                        //add to replacement list
                        blueprintReplacementList.put(station.blueprintIdentifier,text);

                        //replace selected station
                        replaceFromList(station);
                        return;
                    }

                    //one time replacement operation
                    if (text.contains("!station replace ")) {
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
            //test if original is actually a new station
            if (clones.contains(original.getUniqueIdentifier())) {
                return original;
            }

            //is this one on the "to be replaced list"?
            //get name of new blueprint
            String replacement = blueprintReplacementList.get(original.blueprintIdentifier);
            if (replacement != null) {
                //TODO test if replacement is valid blueprint entry

                //replace original with new station
                return replaceFromBlueprint(original, replacement);
            }
        }
        return original;
    }

    /**
     * replace spacestation with new station of this blueprint.
     * inherits faction, name, position, but not complex attributes like inventory or docks.
     * station is always placed at world 0,0,0.
     * @param original
     * @param newBlueprint
     * @return
     */
    private static SpaceStation replaceFromBlueprint(SpaceStation original, String newBlueprint) {
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
                    "REPLACEMENT" + original.getRealName(), //ship name
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
    private static boolean isValidBlueprint(String blueprintName) {
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
