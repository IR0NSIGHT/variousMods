package me.iron.npccontrol.triggers;

import api.listener.Listener;
import api.listener.events.entity.SegmentControllerFullyLoadedEvent;
import api.mod.StarLoader;
import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.events.PirateScoutRaid;
import org.apache.poi.ss.formula.functions.T;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 29.12.2021
 * TIME: 21:22
 */
public class Trigger {
    public ArrayList<Event> scLoadedEvents = new ArrayList<>();
    private final ArrayList<Listener> listeners = new ArrayList<>();
    public Trigger() {
        if (GameServerState.instance != null)
            segConLoaded();

        addEvents();
    }

    /**
     * unregisters this trigger and all its event listeners (in case of reload or similar)
     */
    public void unregister() {
        for (Listener l: listeners) {
            try {
            //    StarLoader.unregisterListener(l.getClass(),l); //TODO unregister listeners
            } catch (Exception ex) {
                //ModMain.log(ex.toString());
            }
        }
        listeners.clear();
        scLoadedEvents.clear();
    }

    public void addEvents() {
        long code = 0;
        //spawn pirates in asteroid belt
        code = Utility.add8bit(code,0b1,0); //neutral faction
        code = Utility.add8bit(code,0b10000100,1); //roid or planet
    //    scLoadedEvents.add(new PirateScoutRaid(code,100,120, "belter pirates"));

        //spawn pirates at pirate stations and neutral stations (derelicts)
        code = Utility.add8bit(code,0b1,0); //neutral faction
        code = Utility.add8bit(code,0b00000010,1); //roid or planet
    //    scLoadedEvents.add(new PirateScoutRaid(code,100,120,"station lurkers"));
    }

    private void segConLoaded() {
        Listener l = new Listener<SegmentControllerFullyLoadedEvent>() {
            @Override
            public void onEvent(SegmentControllerFullyLoadedEvent event) {
                //ModMain.log("segcon loaded");
                long code = 0;
                code = Utility.addFaction(code,event.getController().getFactionId());
                code = Utility.addEntityType(code,event.getController().getType());

                triggerList(scLoadedEvents,code,event.getController().getSector(new Vector3i()));
            }
        };
        StarLoader.registerListener(SegmentControllerFullyLoadedEvent.class, l, ModMain.instance);
    }

    private void triggerList(ArrayList<Event> events, long code, Vector3i position) {
        for (Event e: events) {
            e.trigger(code,position);
        }
    }

    public static void main(String[] e) {
        Trigger t = new Trigger();

        long l = 0;
        System.out.println("spawn neutral roid");

        l = Utility.addFaction(l,0);
        l = Utility.addEntityType(l, SimpleTransformableSendableObject.EntityType.ASTEROID);
        t.triggerList(t.scLoadedEvents, l, new Vector3i(2,2,2));

        System.out.println("spawn pirate ship");
        l = 0;
        l = Utility.addFaction(l,-1);
        l = Utility.addEntityType(l, SimpleTransformableSendableObject.EntityType.SHIP);

        t.triggerList(t.scLoadedEvents, l, new Vector3i(3,3,3));


    }


}
