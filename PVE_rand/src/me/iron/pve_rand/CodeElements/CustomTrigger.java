package me.iron.pve_rand.CodeElements;

import me.iron.pve_rand.Managers.ActionController;
import org.schema.common.util.linAlg.Vector3i;

import java.util.*;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 12:11
 */
public class CustomTrigger extends CustomCode {

    private HashMap<Integer, Integer> conditions = new HashMap<Integer, Integer>(); //maps trigger condition vs chance

    private HashSet<Integer> conditionsPure = new HashSet<>();

    public HashSet<Integer> getConditions() {
        return conditionsPure;
    }

    public List<CustomScript> getScripts() {
        List<CustomScript> out = new ArrayList<>();
        for (Integer id: scripts) {
            out.add(ActionController.getScript(id));
        }
        return out;
    }

    public CustomTrigger(HashSet<Integer> conditions, String name, String description) {
        super(name,description);
        ActionController.addTrigger(this);
        for (Integer c : conditions) {
            addCondition(c);
        }
    }

    //triggers all actions of trigger
    public void trigger(Vector3i sector, int cause) {
        //only if precise match on cause type OR non discrimination (FF)
        if (!isActive())
            return;

        int rand = (int) (Math.random() * 100);
        int chance = conditions.get(cause & 0xFFFFFF00);

        if (chance < rand) return;
        for (Integer script : scripts) {
            ActionController.getScript(script).run(sector);
        }
    }

    private List<Integer> scripts = new ArrayList<Integer>();

    public void addScript(CustomScript s) {
        this.scripts.add(s.getID());
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
        ActionController.addTriggerToEvents(this, c);
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
                ", description='" + getDescription() + '\'' +
                ", scripts=" + scripts +
                '}';
    }

    @Override
    public String getChildText() {
        StringBuilder b = new StringBuilder();
        b.append("Scripts: \n");
        Iterator<Integer> i = scripts.iterator();
        while (i.hasNext()) {
            int id = i.next();
            b.append("---").append(ActionController.getScript(id).getName()).append("\n");
            b.append("------").append(ActionController.getScript(id).getOverview());
            CustomScript s = ActionController.getScript(id);
            b.append("actions:").append(s.getChildText());
            if (i.hasNext())
                b.append("\n");
        }
        return b.toString();
    }

    /**
     * gets string of condition + descirption
     * @return //TODO make useful overview
     */
    @Override
    public String getOverview() {
        StringBuilder out = new StringBuilder("Trigger{\n\tdescription: " + getDescription() + "\n\tconditions: ");
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
        out.append("\n}");
        return out.toString();
    }
}
