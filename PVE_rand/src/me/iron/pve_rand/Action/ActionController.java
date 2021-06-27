package me.iron.pve_rand;

import api.mod.config.PersistentObjectUtil;
import me.iron.pve_rand.Persistence.TriggerList;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 11:59
 */
public class ActionController {
    //list that holds all triggers.
    private static HashMap<Integer, HashSet<CustomTrigger>> triggers = new HashMap<Integer, HashSet<CustomTrigger>>();

    //initialize empty list at condition.
    private static void initEmpty(int condition) {
        if (triggers.get(condition) == null) {
            triggers.put(condition,new HashSet<CustomTrigger>());
        }
    }


    //adds trigger to triggers list
    public static void addTrigger(CustomTrigger t) {
        initEmpty(t.getCondition()>>16);
        triggers.get(t.getCondition()>>16).add(t);
    }

    //fires all triggers that have this condition. passes sector to triggers and actions.
    public static void fireEvent(int condition, Vector3i sector) {
        //get triggers with this condition
        for (CustomTrigger t: triggers.get(condition>>16)) {
            t.trigger(sector);
        }
    }

    //loads from modData
    public static void loadPersistent() {
        ArrayList<Object> allTs = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(), TriggerList.class);
        if (allTs.size() != 1) {
            System.err.println("more than one trigger list object is present for PVE_rand persistence.");
            return;
        }
        TriggerList list = (TriggerList) allTs.get(0);
        list.loadIntoActionController();
    }

    public static void savePersistent() {
        TriggerList holder;
        try {
            holder = (TriggerList) PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(), TriggerList.class).get(0);
            holder.setTriggers(triggers);
        } catch (IndexOutOfBoundsException ex) {
            holder = new TriggerList(triggers);
            PersistentObjectUtil.addObject(ModMain.instance.getSkeleton(),holder);
        }
        PersistentObjectUtil.save(ModMain.instance.getSkeleton());
    }

    public static void addDebugEvent() {
        CustomTrigger t = new CustomTrigger(0x010003FF, "debug trigger, detects SC fully loaded");
        CustomAction a = new CustomAction(0x031101000,"spawm a pirate");
        addTrigger(t);
    }

    public static int getShipType(SegmentController sc) {
        int condition = 0x00000000;
        if (sc instanceof Ship) {
            Ship s = (Ship) sc;
            if (s.isDocked()) return 0;
            condition |= 0x00000100;
        }
        if (sc instanceof SpaceStation) {
            condition |= 0x00000200;
        }
        if (sc instanceof FloatingRock) {
            condition |= 0x00000300;
        }
        return condition;
    }

}
