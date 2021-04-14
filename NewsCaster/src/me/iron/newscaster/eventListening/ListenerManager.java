package me.iron.newscaster.eventListening;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 16:05
 */

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.entity.SegmentControllerOverheatEvent;
import api.listener.events.entity.SegmentControllerSpawnEvent;
import api.listener.events.entity.SegmentHitByProjectileEvent;
import api.listener.events.faction.FactionRelationChangeEvent;
import api.listener.events.faction.SystemClaimEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import me.iron.newscaster.*;
import me.iron.newscaster.notification.NewsManager;
import me.iron.newscaster.notification.infoTypes.FactionRelationInfo;
import me.iron.newscaster.notification.infoTypes.ShipCreatedInfo;
import me.iron.newscaster.notification.infoTypes.ShipDestroyedInfo;
import me.iron.newscaster.notification.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SegmentControllerHpController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.damage.Damager;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.PlayerControlledTransformableNotFound;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.mod.Mod;

import java.util.HashMap;
import java.util.List;

/**
 * manages various event listeners: damage EH, destruction EH, etc etc
 * creates and destroyes them.
 */
public class ListenerManager {
    HashMap<String, Integer> massLimits = new HashMap<String, Integer>() {{
        put("destructionLower",2500);
        put("creationLower",2500);
    }};
    /**
     * will create a new manager and auto create all relevant eventhandlers.
     */
    public ListenerManager() {
        //Create new manager, will auto init wanted EHs.
        InitEHs();
    }

    private void InitEHs() {
        DebugFile.log("initializing eventhandlers.");
        StarLoader.registerListener(SegmentControllerOverheatEvent.class, new Listener<SegmentControllerOverheatEvent>() {
            @Override
            public void onEvent(SegmentControllerOverheatEvent event) {
                if (!event.isServer())
                {
                    return;
                }
                DebugFile.log("segmentcontroller is overheating.");
                //make ship object
                SegmentController ship = event.getEntity();
                float mass = ship.getMassWithDocks();
                float min = massLimits.get("destructionLower");
                if (ship.getMassWithDocks() < massLimits.get("destructionLower")) {
                    return;
                }

                //make attacker object
                Damager lastDamager = event.getLastDamager();

                if (lastDamager.getFactionId() == ship.getFactionId() && ship.getFactionId() != 0) {
                    return; //same faction == boring.
                }

                boolean isPlayer = (lastDamager.getOwnerState() instanceof PlayerState);
                SimpleTransformableSendableObject shooter = lastDamager.getShootingEntity();
                if (isPlayer) {
                    try {
                        shooter = ((PlayerState) lastDamager.getOwnerState()).getFirstControlledTransformable();
                    } catch (PlayerControlledTransformableNotFound playerControlledTransformableNotFound) {
                        playerControlledTransformableNotFound.printStackTrace();
                    }
                }

                ShipObject victim = new ShipObject(ship);
                ShipObject attacker;
                if (event.getLastDamager().getShootingEntity() instanceof SegmentController) {
                    attacker = new ShipObject((SegmentController) event.getLastDamager().getShootingEntity());
                } else {
                    attacker = new ShipObject((SegmentController) shooter);
                }
                ShipDestroyedInfo info = new ShipDestroyedInfo(victim,attacker,ship.getSector(new Vector3i()));
                NewsManager.addInfo(info);

            }
        }, ModMain.instance);

        StarLoader.registerListener(SegmentControllerSpawnEvent.class, new Listener<SegmentControllerSpawnEvent>() {
            @Override
            public void onEvent(SegmentControllerSpawnEvent event) {
                QueueForCreationReport(event.getController());
            }
        },ModMain.instance);

        StarLoader.registerListener(FactionRelationChangeEvent.class, new Listener<FactionRelationChangeEvent>() {
            @Override
            public void onEvent(FactionRelationChangeEvent event) {
                if (event.getTo().isNPC() && event.getFrom().isNPC()) {
                    return;//ignore npc spamming each other with peace offers.
                }
                FactionRelationInfo info = new FactionRelationInfo(event.getFrom(), event.getTo(),event.getOldRelation(),event.getNewRelation());
                NewsManager.addInfo(info);
                DebugFile.log("Faction relation change event: " + info.getNewscast());
                DebugFile.log(info.toString());
            }
        },ModMain.instance);

        StarLoader.registerListener(SystemClaimEvent.class, new Listener<SystemClaimEvent>() {
            @Override
            public void onEvent(SystemClaimEvent event) {
               
            }
        }, ModMain.instance);
    }

    private void QueueForCreationReport(final SegmentController sc) {
        DebugFile.log("queued segmentcontroller for creation report");
        new StarRunnable() {
            @Override
            public void run() {
                if (sc.getMassWithDocks() < massLimits.get("creationLower") || sc.getFaction().isNPC()) {
                    return;
                }
                DebugFile.log("delayed log running for " + sc.getName());
                ShipObject ship = new ShipObject(sc);
                ShipCreatedInfo report = new ShipCreatedInfo(ship, sc.getSector(new Vector3i()));
                NewsManager.addInfo(report);
            }
        }.runLater(ModMain.instance,10);
    }
    /**
     * will overheat any docked entities.
     * @param sc mothership
     * @param exemptReactors true to not overheat any entities with reactors, false to overheat all.
     */
    public static void overheatDock(SegmentController sc, boolean exemptReactors) {
        if (sc == null) {
            return;
        }
        List<RailRelation> next = sc.railController.next;
        for (int i = 0; i < next.size();i++) {
            SegmentController dockedEntity = next.get(i).docked.getSegmentController();
            SegmentControllerHpController hpController = (SegmentControllerHpController) dockedEntity.getHpController();
            float mass = hpController.getSegmentController().getMassWithDocks();
            SendableSegmentController ssc = (SendableSegmentController) dockedEntity;
            long hp = ssc.getReactorHp();
            long hpMax = ssc.getReactorHpMax();

            if (ssc.hasAnyReactors()) { //TODO check for reactor size instead.
                continue; //no chain reaction for bigger ships. dont wanna overheat a battleship thats docked to a shuttle.
            };
            forceOverheatSC(dockedEntity);
        }
    }

    public static void forceOverheatSC(SegmentController sc) {
        if ((sc.getHpController() == null)) {
            return;
        }
        if (sc.isCoreOverheating()) {
            return;
        }
        if (sc.hasActiveReactors()) {
            SegmentControllerHpController hpController = (SegmentControllerHpController) sc.getHpController();
            hpController.forceDamage(hpController.getHp() + 1); //apply damage to reactor (otherwise overheat will not work)
            ((SegmentControllerHpController) sc.getHpController()).triggerOverheating(); //is hpController, safe cast (recursively trigger next dock level overheat)
        } else {
            sc.startCoreOverheating(sc);
        }

    }

    /**
     * kill a sc. will not overheat: docked || non-managed SC || reactor > 500 blocks
     * @param sc
     */
    public static void killTiny(SegmentController sc) {
        if (sc.getDockingController().isDocked()) { //dont kill docked elements
            return;
        }
        if (!(sc instanceof ManagedSegmentController))
        {
            return;
        }
        ManagerContainer mc = ((ManagedSegmentController) sc).getManagerContainer();
        if (!sc.hasActiveReactors()) { //no reactor => instant overheat
            forceOverheatSC(sc);
            return;
        }
        int rctSize = mc.getPowerInterface().getActiveReactor().getActualSize();
        if (rctSize > 500) {
            return;
        }
        forceOverheatSC(sc);
    }
}
