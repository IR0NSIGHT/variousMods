package me.iron.pve_rand.Event;

import me.iron.pve_rand.Action.ActionController;
import me.iron.pve_rand.Action.CustomAction;
import me.iron.pve_rand.Action.CustomScript;
import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;
import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 12:11
 */
public class CustomTrigger implements Serializable {

    private HashMap<Integer, Integer> conditions = new HashMap<Integer, Integer>(); //maps trigger condition vs chance

    private HashSet<Integer> conditionsPure = new HashSet<>();

    private String description; //short description telling whats going on

    public String getDescription() {
        return description;
    }

    public HashSet<Integer> getConditions() {
        return conditionsPure;
    }

    public List<CustomScript> getActions() {
        return scripts;
    }

    public CustomTrigger(HashSet<Integer> conditions, String description) {
        for (Integer c : conditions) {
            addCondition(c);
        }
        this.description = description;
    }

    //triggers all actions of trigger
    public void trigger(Vector3i sector, int cause) {
        //only if precise match on cause type OR non discrimination (FF)

        int rand = (int) (Math.random() * 100);
        int chance = conditions.get(cause & 0xFFFFFF00);

        if (chance < rand) return;
        for (CustomScript script : scripts) {
            script.run(sector);
        }
    }

    private List<CustomScript> scripts = new ArrayList<CustomScript>();

    public void addScript(CustomScript s) {
        this.scripts.add(s);
    }

    public void removeScript(CustomAction a) {

    }

    /**
     * adds condition to this trigger, autoassigns to be fired through AC
     *
     * @param c pure condition
     */
    public void addCondition(int c) {
        this.conditions.put(c & 0xFFFFFF00, c & 0x000000FF);
        conditionsPure.add(c);
        ActionController.addTrigger(this, c);
    }

    /**
     * removes condition from trigger, wont fire anymore through AC
     *
     * @param c pure condition
     */
    public void removeCondition(int c) {
        ActionController.removeTrigger(this, c);
    }

    @Override
    public String toString() {
        return "CustomTrigger{" +
                "conditions=" +  conditions +
                ", conditionsPure=" + conditionsPure +
                ", description='" + description + '\'' +
                ", scripts=" + scripts +
                '}';
    }

    /**
     * gets string of condition + descirption
     * @return
     */
    public String getOverview() {
        StringBuilder out = new StringBuilder("Trigger{\n\tdescription: " + description + "\n\tconditions: ");
        Iterator<Integer> it = conditionsPure.iterator();
        while (it.hasNext()) {
            int c = it.next();
            for (int i = 3; i >= 0; i--) {
                out.append(String.format("%2S",Integer.toHexString((c>>8*i&255))).replace(" ","0"));
                if (i > 0)
                  out.append(".");
            }
            if (it.hasNext())
                out.append("|| ");
        }
        out.append("\n}\n");
        return out.toString();
    }
}
