package me.iron.npccontrol.stationReplacement;

import api.DebugFile;

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
import org.schema.game.common.controller.*;
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
import org.schema.game.server.data.simulation.SimulationManager;
import org.schema.game.server.data.simulation.groups.SimulationGroup;
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
public class StationHelper {
    /**
     * deletes new-system docked entities. doesnt work for old docked.
     * @param entity
     */
    public static void deleteDocked(SegmentController entity) {
        List<RailRelation> next = entity.railController.next;
        for (RailRelation railRelation : next) {
            SegmentController dockedEntity = railRelation.docked.getSegmentController();

            //recurse
            deleteDocked(dockedEntity);
            dockedEntity.markForPermanentDelete(true);
            dockedEntity.setMarkedForDeleteVolatile(true);
        }
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
        DebugFile.log("replacing " + original.getName() + " with " + newBlueprint);
        //get transform = worldposition wrapper
        Transform transform = new Transform();
        transform.setIdentity();
        transform = original.getWorldTransform();
        transform.origin.set(original.getWorldTransform().origin);

        //log sector and blueprint
        Vector3i sector = original.getSector(new Vector3i());
        String originalBlueprint = original.blueprintIdentifier;

        //create outline = loaded but not yet spawned entity
        SegmentControllerOutline scOutline = null;
        try {
            scOutline = BluePrintController.active.loadBluePrint(
                    GameServerState.instance,
                    newBlueprint, //catalog entry name
                    newBlueprint, //ship name
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
    //    final SpaceStation s = newStation;
    //    new StarRunnable() {
    //        @Override
    //        public void run() {
    //            s.warpTransformable(0,0,0, true, null);
    //            ModPlayground.broadcastMessage("station blueprint: " + s.blueprintIdentifier);
    //        }
    //    }.runLater(ModMain.instance,10);
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
