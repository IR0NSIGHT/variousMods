package me.iron.pve_rand.Action;

import me.iron.pve_rand.Action.checks.BooleanAction;
import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.06.2021
 * TIME: 17:47
 */
public class ConditionAction extends CustomAction implements Serializable {
    public ConditionAction(int argument, String description) {
        super(argument, description);
    }

    public void setThisBool(BooleanAction thisBool) {
        this.thisBool = thisBool;
    }

    public void addThenAction(CustomAction a) {
        thenActions.add(a);
    }

    public void addElseAction(CustomAction a) {
        elseActions.add(a);
    }

    private BooleanAction thisBool;
    private List<CustomAction> thenActions = new ArrayList<>();
    private List<CustomAction> elseActions = new ArrayList<>();

    @Override
    protected void addString(List<String> ls) {
        ls.add("ConditionAction{if");
        ls.add(this.toString());
        ls.add("then {");
        for (CustomAction a : thenActions) {
            ls.add("\t"+a.toString());
        }
        ls.add("} else {");
        for (CustomAction a : elseActions) {
            ls.add("\t"+a.toString());
        }
        ls.add("}");
    }

    @Override
    protected void onExecute(int argument, Vector3i sector) {
        super.onExecute(argument, sector);
        if (thisBool.test(sector)) {
            for (CustomAction a: thenActions) {
                a.execute(sector);
            }
        } else {
            for (CustomAction a: elseActions) {
                a.execute(sector);
            }
        }
    }
}
