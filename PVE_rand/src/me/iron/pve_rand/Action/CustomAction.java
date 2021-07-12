package me.iron.pve_rand.Action;

import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 12:37
 * does something. listed in trigger, fired by trigger.
 */
public class CustomAction implements Serializable {

    private int argument;
    private String description;

    private List<CustomAction> actionList = new ArrayList<>();
    public CustomAction(int argument, String description) {
        this.argument = argument;
        this.description = description;
    }

    public void execute( Vector3i sector) {
        int chance = argument&0x000000FF;
        int rand = (int)(100 * Math.random());
        if (chance < rand) return;
        onExecute(argument, sector);
    }

    protected void onExecute(int argument,  Vector3i sector) {
        for (CustomAction a: actionList) {
            a.execute(sector);
        }
    }

    public List<CustomAction> getActionList() {
        return actionList;
    }

    public void addCodeBlock(CustomAction action) {
        this.actionList.add(action);
    }

    @Override
    public String toString() {
        return "CustomAction{" +
                "chance" + (argument&0x000000FF) +
                ", description='" + description + '\'' +
                '}';
    }

    protected void addString(List<String> ls) {
        ls.add("\t"+toString());
    }
}
