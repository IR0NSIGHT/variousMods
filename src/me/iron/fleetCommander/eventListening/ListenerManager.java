package me.iron.fleetCommander.eventListening;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 16:05
 */

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.entity.SegmentControllerOverheatEvent;
import api.mod.StarLoader;
import me.iron.fleetCommander.*;
import me.iron.fleetCommander.notification.NewsManager;
import me.iron.fleetCommander.notification.infoTypes.ShipDestroyedInfo;
import me.iron.fleetCommander.notification.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;

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
                //DebugFile.log("Overheat event detected for: " + event.getEntity().getName());
                if (event.getEntity().getMassWithDocks() < 1000 || !event.isServer()) { //TODO make configurable
                    return;
                }
                //make ship object
                SegmentController ship = event.getEntity();
                ShipObject victim = new ShipObject(ship);
                //make attacker object
                ShipObject attacker;
                try {
                    attacker = new ShipObject((SegmentController) event.getLastDamager());
                } catch (Exception e) {
                    e.printStackTrace();
                    attacker = new ShipObject(event.getLastDamager().getShootingEntity().getUniqueIdentifier(), event.getLastDamager().getName(),null,-1,-1);
                }
                ShipDestroyedInfo info = new ShipDestroyedInfo(victim,attacker,ship.getSector(new Vector3i()));
                NewsManager.addInfo(info);
            }
        },modMain.instance);
    }
}
