package me.iron.pve_rand;

import com.eaio.util.lang.Hex;
import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 12:11
 */
public class CustomTrigger implements Serializable {
    public int getCondition() {
        return condition;
    }

    public String getDescription() {
        return description;
    }

    private int condition; //when to activate, unique representation of possible conditions, like chmod 755
    private String description; //short description telling whats going on
    private List<CustomAction> actions = new ArrayList<CustomAction>();
    private String hex = "";
    //triggers all actions of trigger
    public void trigger(Vector3i sector) {
        int chance = (condition & 0x000000FF);
        if (chance < Math.random()*0x000000FF) return;
        for (CustomAction a: actions) {
            a.execute(sector);
        }
    }

    public CustomTrigger(int condition, String description) {
        this.condition = condition;
        this.description = description;
        hex = Integer.toHexString(condition);

    }
}
