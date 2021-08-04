package me.iron.pve_rand.Persistence;

import me.iron.pve_rand.CodeElements.CustomAction;
import me.iron.pve_rand.CodeElements.CustomScript;
import me.iron.pve_rand.CodeElements.CustomTrigger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 13:13
 * persistance holder
 */
public class PersistentContainer implements Serializable {
    public HashMap<Integer,CustomTrigger> triggers;
    public HashMap<Integer, CustomScript> scripts;
    public HashMap<Integer, CustomAction> actions;
    public int nextID;
    //creates triggerlist object holding all triggers in the map
    public PersistentContainer() {
    }

    public void saveValues(HashMap<Integer,CustomTrigger> triggers, HashMap<Integer, CustomScript> scripts, HashMap<Integer, CustomAction> actions, int nextID) {
        this.triggers = triggers;
        this.scripts = scripts;
        this.actions = actions;
        this.nextID = nextID;
    }

    //loads the objects triggers into the actioncontroller map.
    public void loadIntoActionController() {
        for (CustomTrigger trigger: triggers.values()) {
            for (int c: trigger.getConditions()) {
                trigger.addCondition(c); //will as sideeffect add itself into ActionController
            }
        }
    }
}
