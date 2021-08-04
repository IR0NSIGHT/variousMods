package me.iron.pve_rand.CodeElements.Action.checks;

import me.iron.pve_rand.CodeElements.CustomAction;
import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.06.2021
 * TIME: 17:49
 */
public class BooleanAction extends CustomAction implements Serializable {

    public BooleanAction(int argument, String name, String description) {
        super(argument, name, description);
    }

    public boolean test(Vector3i sector) {
        return false;
    }

    protected void addString(List<String> ls) {
        ls.add("("+"false"+")");
    }
}
