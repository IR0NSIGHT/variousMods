package me.iron.pve_rand;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.entity.SegmentControllerFullyLoadedEvent;
import api.listener.events.entity.SegmentControllerOverheatEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 11:59
 */
public class ListenerManager {
    public static void init() {
        addLoadListener();
        addOverheatListener();
    }

    //event type 01
    private static void addLoadListener() {
        StarLoader.registerListener(SegmentControllerFullyLoadedEvent.class, new Listener<SegmentControllerFullyLoadedEvent>() {
            @Override
            public void onEvent(SegmentControllerFullyLoadedEvent event) {
                if (!event.isServer()) return;
                int condition = 0x01000000;
                SegmentController loaded = event.getController();
                int type = ActionController.getShipType(loaded);
                //debug

                if (type == 0) return;
                condition |= type;
                ActionController.fireEvent(condition, event.getController().getSector(new Vector3i()));
            }
        },ModMain.instance);

    }

    //event type 02
    private static void addOverheatListener() {
        StarLoader.registerListener(SegmentControllerOverheatEvent.class, new Listener<SegmentControllerOverheatEvent>() {
            @Override
            public void onEvent(SegmentControllerOverheatEvent event) {
                if (!event.isServer()) return;
                ActionController.fireEvent(0x020000FF, event.getEntity().getSector(new Vector3i()));
                DebugFile.log("segmentcontroller is overheating.");
            }
        }, ModMain.instance);
    }
}
