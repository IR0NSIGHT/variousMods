package me.iron.newscaster.eventListening;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 16:05
 */

import api.listener.Listener;
import api.listener.events.entity.SegmentControllerOverheatEvent;
import api.listener.events.entity.SegmentHitByProjectileEvent;
import api.mod.StarLoader;
import me.iron.newscaster.*;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.SegmentControllerHpController;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.controller.rails.RailRelation;
import org.schema.game.common.data.ManagedSegmentController;

import java.util.List;

/**
 * manages various event listeners: damage EH, destruction EH, etc etc
 * creates and destroyes them.
 */
public class ListenerManager {
    /**
     * will create a new manager and auto create all relevant eventhandlers.
     */
    public ListenerManager() {
        //Create new manager, will auto init wanted EHs.
        InitEHs();
    }

    private void InitEHs() {
        StarLoader.registerListener(SegmentControllerOverheatEvent.class, new Listener<SegmentControllerOverheatEvent>() {
            @Override
            public void onEvent(SegmentControllerOverheatEvent event) {
                if (event.isServer())
                {
                    return;
                }
                //DebugFile.log("Overheat event detected for: " + event.getEntity().getName());
                if (!event.isServer()) {
                    return;
                }
                overheatDock(event.getEntity(),true);

                float mass = event.getEntity().getMassWithDocks();
                if (false) { //TODO make configurable
                    return;
                }
                //make ship object
        //        SegmentController ship = event.getEntity();
        //        ShipObject victim = new ShipObject(ship);
        //        //make attacker object
        //        ShipObject attacker = null;
        //        Damager dmg = event.getLastDamager();
        //        boolean isSC = dmg.isSegmentController();
        //        boolean isPlayer = (dmg.getOwnerState() instanceof PlayerState);
//
        //        String name = dmg.getName();
        //        SimpleTransformableSendableObject shooter = dmg.getShootingEntity();
        //        if (isPlayer) {
        //            try {
        //                shooter = ((PlayerState) dmg.getOwnerState()).getFirstControlledTransformable();
        //            } catch (PlayerControlledTransformableNotFound playerControlledTransformableNotFound) {
        //                playerControlledTransformableNotFound.printStackTrace();
        //            }
        //        }
        //        String UID = shooter.getUniqueIdentifier();
        //        if (event.getLastDamager().getShootingEntity() instanceof SegmentController) {
        //            attacker = new ShipObject((SegmentController) event.getLastDamager().getShootingEntity());
        //        } else {
//
        //            attacker = new ShipObject((SegmentController) shooter);
        //        }
        //        ShipDestroyedInfo info = new ShipDestroyedInfo(victim,attacker,ship.getSector(new Vector3i()));
        //        NewsManager.addInfo(info);
            }
        }, ModMain.instance);

      StarLoader.registerListener(SegmentHitByProjectileEvent.class, new Listener<SegmentHitByProjectileEvent>() {
          @Override
          public void onEvent(SegmentHitByProjectileEvent event) {
              SegmentController victim = event.getShotHandler().hitSegController;
              killTiny(victim);
          }
      }, ModMain.instance);
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
