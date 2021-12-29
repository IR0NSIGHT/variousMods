package me.iron.npccontrol.triggers;

import api.listener.Listener;
import api.listener.events.entity.SegmentControllerFullyLoadedEvent;
import api.mod.StarLoader;
import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.events.PirateScoutRaid;
import org.newdawn.slick.tests.xml.Entity;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;

import java.util.ArrayList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 29.12.2021
 * TIME: 21:22
 */
public class Trigger {
    public ArrayList<Event> scLoadedEvents = new ArrayList<>();

    public Trigger() {
        if (GameServerState.instance != null)
            segConLoaded();
        addEvents();
        ModMain.log("trigger init");


    }

    public void addEvents() {
        scLoadedEvents.add(new PirateScoutRaid(0x8401,100,0));
    }

    private void segConLoaded() {
        StarLoader.registerListener(SegmentControllerFullyLoadedEvent.class, new Listener<SegmentControllerFullyLoadedEvent>() {
            @Override
            public void onEvent(SegmentControllerFullyLoadedEvent event) {
                ModMain.log("segcon loaded");
                long code = 0;
                code = addFaction(code,event.getController().getFactionId());
                code = addEntityType(code,event.getController().getType());

                triggerList(scLoadedEvents,code,event.getController().getSector(new Vector3i()));
            }
        }, ModMain.instance);
    }

    private void triggerList(ArrayList<Event> events, long code, Vector3i position) {
        ModMain.log(String.format("trigger list with input %s\npos %s",toBin(code),position.toStringPure()));
        for (Event e: events) {
            e.trigger(code,position);
        }
    }

    public static void main(String[] e) {
        Trigger t = new Trigger();

        long l = 0;
        System.out.println("spawn neutral roid");

        l = addFaction(l,0);
        l = addEntityType(l, SimpleTransformableSendableObject.EntityType.ASTEROID);
        t.triggerList(t.scLoadedEvents, l, new Vector3i(2,2,2));

        System.out.println("spawn pirate ship");
        l = 0;
        l = addFaction(l,-1);
        l = addEntityType(l, SimpleTransformableSendableObject.EntityType.SHIP);

        t.triggerList(t.scLoadedEvents, l, new Vector3i(3,3,3));


    }

    private static long addFaction(long in, int factionID) {
        switch (factionID) {
            case 0: //neutral
                return in|1;
            case -1: //pirates
                return in|(1<<1);
        }
        if (factionID < 0) { //is NPC
            return in|(1<<2);
        } else if (factionID > 10000){ //is player
            return in|(1<<3);
        }
        return in;
    }

    private static long addEntityType(long in, SimpleTransformableSendableObject.EntityType type) {
        int s = 8;
        switch (type) {
            case SHIP:                    break;
            case SPACE_STATION:           s += 1;break;
            case ASTEROID_MANAGED:
            case ASTEROID:
                s += 2; break;
            case ASTRONAUT:               s += 3; break;
            case SUN:                     s += 4; break;
            case BLACK_HOLE:              s += 5;break;
            case SHOP:                    s += 6; break;
            case PLANET_CORE:
            case PLANET_ICO:
            case PLANET_SEGMENT:
                s += 7;break;
            default:return in;
        }
        return in|1<<s;

    }

    public static String toBin(long code) {
        String o = Long.toBinaryString(code);
        return String.format("%1$" + 64 + "s", o).replace(' ', '0');
    }
}
