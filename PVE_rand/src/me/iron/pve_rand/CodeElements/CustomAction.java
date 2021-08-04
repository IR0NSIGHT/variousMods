package me.iron.pve_rand.CodeElements;

import me.iron.pve_rand.Managers.ActionController;
import org.schema.common.util.linAlg.Vector3i;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 12:37
 * does something. listed in trigger, fired by trigger.
 */
public class CustomAction extends CustomCode {

    private int argument;

    private List<Integer> actionList = new ArrayList<>(); //TODO get rid of subactions, move all into scripts
    public CustomAction(int argument, String name, String description) {
        super(name, description);
        this.argument = argument;
        ActionController.addAction(this);
    }

    public void execute( Vector3i sector) {
        if (!isActive())
            return;
        int chance = argument&0x000000FF;
        int rand = (int)(100 * Math.random());
        if (chance < rand)
            return;
        onExecute(argument, sector);
    }

    protected void onExecute(int argument,  Vector3i sector) {
        for (Integer id: actionList) {
            ActionController.getAction(id).execute(sector);
        }
    }

    public void addCodeBlock(CustomAction action) {
        this.actionList.add(action.getID());
    }

    @Override
    public String toString() {
        return "CustomAction{" +
                "chance" + (argument&0x000000FF) +
                ", description='" + getDescription() + '\'' +
                '}';
    }

    protected void addString(List<String> ls) {
        ls.add("\t"+toString());
    }
}
