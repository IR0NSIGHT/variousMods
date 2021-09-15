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
import api.listener.events.faction.FactionRelationChangeEvent;
import api.listener.events.faction.SystemClaimEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import me.iron.newscaster.*;
import me.iron.newscaster.notification.infoGeneration.NewsManager;
import me.iron.newscaster.notification.infoGeneration.infoTypes.*;
import me.iron.newscaster.notification.infoGeneration.objectTypes.FactionObject;
import me.iron.newscaster.notification.infoGeneration.objectTypes.ShipObject;
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
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;

import java.util.HashMap;
import java.util.List;

/**
 * manages various event listeners: damage EH, destruction EH, etc etc
 * creates and destroyes them.
 */
public class ListenerManager {

    private static int info_ship_minMass;
    private static boolean info_threshold ;
    private static boolean info_log_ship_destruct;
    private static boolean info_log_ship_spawn;
    private static boolean info_log_faction_relationchange;
    private static boolean info_log_faction_systemclaim;

    public static void updateFromConfig() {
        info_ship_minMass                      = configManager.getValue("info_ship_minMass");
        info_threshold                         = configManager.getValue("info_threshold")==1;
        info_log_ship_destruct                 = configManager.getValue("info_log_ship_destruct")==1;
        info_log_ship_spawn                    = configManager.getValue("info_log_ship_spawn")==1;
        info_log_faction_relationchange        = configManager.getValue("info_log_faction_relationchange")==1;
        info_log_faction_systemclaim           = configManager.getValue("info_log_faction_systemclaim")==1;
    }

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
                if (!event.isServer() || !info_log_ship_destruct)
                {
                    return;
                }
                DebugFile.log("segmentcontroller is overheating.");
                //make ship object
                SegmentController ship = event.getEntity();
                float mass = ship.getMassWithDocks();
                float min = info_ship_minMass;
                if (ship.getMassWithDocks() < info_ship_minMass) {
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
                //account for overheating, ship is at 40% reactor
                victim.setReactor((int) (victim.getReactor()/0.4f)); //TODO use reactor overheat percent
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
                if(!info_log_ship_spawn) return;
                QueueForCreationReport(event.getController());
            }
        },ModMain.instance);

        //FIXME doesnt seem to run on dedicated at all
        StarLoader.registerListener(FactionRelationChangeEvent.class, new Listener<FactionRelationChangeEvent>() {
            @Override
            public void onEvent(FactionRelationChangeEvent event) {
                if (event.getTo().isNPC() && event.getFrom().isNPC()) {
                    return;//ignore npc spamming each other with peace offers.
                }
                if(!info_log_faction_relationchange)
                    return;
                FactionRelationInfo info = new FactionRelationInfo(event.getFrom(), event.getTo(),event.getOldRelation(),event.getNewRelation());
                NewsManager.addInfo(info);
                DebugFile.log("Faction relation change event: " + info.getNewscast());
                DebugFile.log(info.toString());
            }
        },ModMain.instance);

        StarLoader.registerListener(SystemClaimEvent.class, new Listener<SystemClaimEvent>() {
            @Override
            public void onEvent(SystemClaimEvent event) {
                //FIXME traders conquering system sometimes gets attributed to a player who passed the system
                if(!info_log_faction_systemclaim || !event.isServer()) return;
                //new faction = event.change.factionID
                //old = event.change.intiator.getFaction
                FactionObject newF = new FactionObject(event.getFaction());
                PlayerState initiator = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(event.getOwnershipChange().initiator);
                FactionObject oldF = new FactionObject(GameServerState.instance.getFactionManager().getFaction(initiator.getFactionId()));
                GenericInfo.EventType type = newF.getFactionID()!=oldF.getFactionID()? GenericInfo.EventType.SYSTEM_LOST:GenericInfo.EventType.SYSTEM_CONQUERED;
                FactionSystemClaimInfo info = new FactionSystemClaimInfo(newF,oldF, type, event.getSystem().getPos());
                NewsManager.addInfo(info);
            }
        }, ModMain.instance);
    }

    /**
     * queues SC for creation of report, to give game time to fully initialize SC on spawning.
     * @param sc
     */
    private void QueueForCreationReport(final SegmentController sc) {
        DebugFile.log("queued segmentcontroller for creation report");
        new StarRunnable() {
            int i = 0;
            @Override
            public void run() {
                i++;
                if (i > 5) cancel(); //absolute safeguard should everything else fail
                if (!sc.isFullyLoadedWithDock()) {
                    return;
                }
                //kick out small ships
                if (sc.getMassWithDocks() < info_ship_minMass) {
                    cancel();
                    return;
                }
                //dont allow npcs
                if ((sc.getFaction() != null) && (sc.getFaction().isNPC() || sc.getFactionId() <= 0)) {
                    cancel();
                    return;
                }
                ShipObject ship = new ShipObject(sc);
                ShipCreatedInfo report = new ShipCreatedInfo(ship, sc.getSector(new Vector3i()));
                NewsManager.addInfo(report);
                cancel();
            }
        }.runTimer(ModMain.instance,50);
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

    //TODO export me into my own mod
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
