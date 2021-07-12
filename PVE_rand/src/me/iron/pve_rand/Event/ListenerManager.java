package me.iron.pve_rand.Event;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.entity.SegmentControllerFullyLoadedEvent;
import api.listener.events.entity.SegmentControllerOverheatEvent;
import api.listener.events.entity.SegmentControllerSpawnEvent;
import api.mod.StarLoader;
import me.iron.pve_rand.Action.ActionController;
import me.iron.pve_rand.ModMain;
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
        addSpawnListener();
    }

    //event type 01
    private static void addLoadListener() {
        StarLoader.registerListener(SegmentControllerFullyLoadedEvent.class, new Listener<SegmentControllerFullyLoadedEvent>() {
            @Override
            public void onEvent(SegmentControllerFullyLoadedEvent event) {
                if (!event.isServer()) return;
                if (event.getController().isDocked()) return;

                int condition = 0x01000000;
                SegmentController loaded = event.getController();
                condition = ActionController.addShipInfo(loaded,condition);
                ActionController.fireEvent(condition, event.getController().getSector(new Vector3i()));
            }
        }, ModMain.instance);
    }

    //event type 02
    private static void addOverheatListener() {
        StarLoader.registerListener(SegmentControllerOverheatEvent.class, new Listener<SegmentControllerOverheatEvent>() {
            @Override
            public void onEvent(SegmentControllerOverheatEvent event) {
                if (!event.isServer()) return;
                if (event.getEntity().isDocked()) return;
                int condition = 0x02000000;
                SegmentController loaded = event.getEntity();
                condition = ActionController.addShipInfo(loaded,condition);

                ActionController.fireEvent(condition, event.getEntity().getSector(new Vector3i()));
                DebugFile.log("segmentcontroller is overheating.");
            }
        }, ModMain.instance);
    }

    //event type 03
    //TODO is SCSpawnEvent the correct one?
    private static void addSpawnListener() {
        StarLoader.registerListener(SegmentControllerSpawnEvent.class, new Listener<SegmentControllerSpawnEvent>() {
            @Override
            public void onEvent(SegmentControllerSpawnEvent event) {
                if (event.isServer()) return;
                if (event.getController().isDocked()) return;

                int cause = 0x03000000;
                cause = ActionController.addShipInfo(event.getController(), cause);
                ActionController.fireEvent(cause,event.getSector());
            }
        },ModMain.instance);
    }
}
