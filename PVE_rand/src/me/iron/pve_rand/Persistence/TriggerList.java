package me.iron.pve_rand.Persistence;

import me.iron.pve_rand.Action.ActionController;
import me.iron.pve_rand.Event.CustomTrigger;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 13:13
 * persistance holder
 */
public class TriggerList implements Serializable {
    private ArrayList<CustomTrigger> triggers = new ArrayList<CustomTrigger>();

    //creates triggerlist object holding all triggers in the map
    public TriggerList(HashSet<CustomTrigger> ts) {
        setTriggers(ts);
    }

    public void setTriggers(HashSet<CustomTrigger> mapTs) {
        if (mapTs == null) {
            return;
        }
        for (CustomTrigger t : mapTs) {
            triggers.clear();
            triggers.add(t);
        }
    }

    //loads the objects triggers into the actioncontroller map.
    public void loadIntoActionController() {
        for (CustomTrigger trigger: triggers) {
            for (int c: trigger.getConditions()) {
                trigger.addCondition(c); //will as sideeffect add itself into ActionController
            }
        }
    }
}
