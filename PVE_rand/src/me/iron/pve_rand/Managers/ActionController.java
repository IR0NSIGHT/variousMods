package me.iron.pve_rand.Managers;

import api.mod.config.PersistentObjectUtil;
import me.iron.pve_rand.CodeElements.Action.ChatAction;
import me.iron.pve_rand.CodeElements.Action.ConditionAction;
import me.iron.pve_rand.CodeElements.CustomAction;
import me.iron.pve_rand.CodeElements.CustomCode;
import me.iron.pve_rand.CodeElements.CustomScript;
import me.iron.pve_rand.CodeElements.Action.checks.SystemCheck;
import me.iron.pve_rand.CodeElements.CustomTrigger;
import me.iron.pve_rand.ModMain;
import me.iron.pve_rand.Persistence.PersistentContainer;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.FloatingRock;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;

import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 11:59
 */
public class ActionController {
    public static ActionController instance;
    public ActionController() {
        instance = this;
    }

    //list that holds all triggers.
    private static HashMap<Integer, HashSet<CustomTrigger>> triggers = new HashMap<Integer, HashSet<CustomTrigger>>();

    public static Collection<CustomTrigger> getAllTriggers() {
        return id_to_trigger.values();
    }

    public static HashMap<Integer,CustomTrigger> id_to_trigger = new HashMap<>();
    public static void addTrigger(CustomTrigger trigger) {
        id_to_trigger.put(trigger.getID(),trigger);
    }
    public static CustomTrigger getTrigger(int id) {
        return id_to_trigger.get(id);
    }

    public static HashMap<Integer,CustomScript> id_to_script = new HashMap<>();
    public static void addScript(CustomScript script) {
        id_to_script.put(script.getID(),script);
    }
    public static CustomScript getScript(int id) {
        return id_to_script.get(id);
    }

    public static HashMap<Integer,CustomAction> id_to_action = new HashMap<>();
    public static void addAction(CustomAction action) {
        id_to_action.put(action.getID(),action);
    }
    public static CustomAction getAction(int id) {
        return id_to_action.get(id);
    }

    //initialize empty list at condition.
    private static void initEmpty(int condition) {
        if (triggers.get(condition) == null) {
            triggers.put(condition,new HashSet<CustomTrigger>());
        }
    }

    //adds trigger to triggers list
    public static void addTriggerToEvents(CustomTrigger t, int c) {
        initEmpty(c&0xFFFFFF00);
        triggers.get(c&0xFFFFFF00).add(t);
    }

    public static void removeTrigger(CustomTrigger t, int c) {
        HashSet<CustomTrigger> s = triggers.get(c);
        if (s == null) return;
        s.remove(t);

        boolean unlisted = true;
        for (int cond: t.getConditions()) {
            HashSet<CustomTrigger> set = triggers.get(cond);
            if (set == null) continue;
            if (set.contains(t)) {unlisted = false; break;}
        }
    }

    public static void clearAll() {
        triggers.clear();
        id_to_trigger.clear();
        id_to_action.clear();
        id_to_script.clear();
    }

    private static final int[] masks = new int[]{
            0xFF000000, //pure event type
            0xFFFF0000, //event + faction
            0xFF00FF00, //event + ship
            0xFFFFFF00 //event + ship + faction
    };

    //fires all triggers that have this condition. passes sector to triggers and actions.
    public static void fireEvent(int cause, Vector3i sector) {
        for (int mask: masks) {
            int maskedCause = cause&mask;
            if (triggers.get(maskedCause) == null) continue;
            //get triggers with this condition
            for (CustomTrigger t: triggers.get(maskedCause)) {
                t.trigger(sector, maskedCause);
            }
        }
    }

    //loads from modData
    public static void loadPersistent() {
        ArrayList<Object> allTs = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(), PersistentContainer.class);
        if (allTs.size() != 1) {
            System.err.println("more than one trigger container object is present for PVE_rand persistence.");
            return;
        }
        PersistentContainer container = (PersistentContainer) allTs.get(0);
        clearAll();
        container.loadIntoActionController();
        id_to_trigger = container.triggers;
        id_to_action = container.actions;
        id_to_script = container.scripts;
        CustomCode.setIdCounter(container.nextID);
    }

    public static void savePersistent() {
        PersistentContainer container;
        try {
            container = (PersistentContainer) PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(), PersistentContainer.class).get(0);
        } catch (IndexOutOfBoundsException ex) {
            container = new PersistentContainer();
            PersistentObjectUtil.addObject(ModMain.instance.getSkeleton(),container);
        }
        container.saveValues(id_to_trigger,id_to_script,id_to_action, CustomCode.nextID());
        PersistentObjectUtil.save(ModMain.instance.getSkeleton());
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

    /**
     * returns the number used to represent factions in the conditions
     * @param factionID
     * @return
     */
    public static int getFactionIndex(int factionID) {
        int f;
        switch (factionID) {
            case -1: {f = 1;break;} //pirates
            case -2: {f = 2;break;} //tradering guild
            case -9999998: {f = 3;break;} //scavengers
            case -9999999: {f = 4;break;} //outcasts
            case -10000000: {f = 5;break;} //Traders
            case 0: {f = 6; break;} //no faction
            case 10001: {f = 7;break;} //first player faction (usually admins)
            default: {f = 8;break;} //F
        }
        return f << 16;
    }

    /**
     * returns condition with added info about ship type and faction
     * @param sc
     * @return
     */
    public static int addShipInfo(SegmentController sc, int condition) {
        int faction = ActionController.getFactionIndex(sc.getFactionId());
        condition |= faction;
        int type = ActionController.getShipType(sc);
        condition |= type;
        return condition;
    }

    public static void addDebugEvent() {
    /*    CustomTrigger t = new CustomTrigger(new HashSet<Integer>(Arrays.asList(0x01000309,0x010002FF)), "spawns pirates and mssgs player in chat");
        CustomAction core = new CustomAction(100,"small pirate fleet");
        t.addAction(core);

        SpawnAction a = new SpawnAction(30,"xs pirate","pirate-ship-02",-1,3,true, -1);
        SpawnAction b = new SpawnAction(100,"s pirate","pirate-ship-01",-1,1,true, -1);
        SpawnAction c = new SpawnAction(5,"ml pirate","SnakeEye",-1,1,true, 60*3);

        ChatAction m = new ChatAction(100, "announce to nearby players that they are fucked","Time to die, hero!",3);
        t.addAction(m);

        core.addAction(c);
        core.addAction(b);
        core.addAction(a);

     */
        CustomTrigger t; CustomAction a; CustomScript s;
        t = new CustomTrigger(new HashSet<Integer>(Arrays.asList(0x010100FF,0x010003FF)),"DebugTrigger","loading any pirate or roid");

        s = new CustomScript(null,"testscript","just testing stuff");
        t.addScript(s);

        a = new ChatAction(100,"notify","post that a pirate was loaded","pirate was loaded",-1);
        s.addCodeBlock(a);

        ConditionAction ifTest = new ConditionAction(100,"trader-owned-system","test if system is owned by trader");
        s.addCodeBlock(ifTest);
        SystemCheck test = new SystemCheck(100,"trader-owned","system owned by trader?",2,false);
        ifTest.setThisBool(test);
        a = new ChatAction(100,"notify","notify that system belongs to traders","this is a trader system",-1);
        ifTest.addThenAction(a);

        a = new ChatAction(100,"notify","notify that is not trader system.","this is NOT a trader system",-1);
        ifTest.addElseAction(a);

    //    s.addCodeBlock(a);
    //    s.addCodeBlock(a);

    }

}
