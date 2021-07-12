package me.iron.pve_rand.Action.checks;

import me.iron.pve_rand.Action.CustomAction;
import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.06.2021
 * TIME: 17:49
 */
public class BooleanAction extends CustomAction implements Serializable {

    public BooleanAction(int argument, String description) {
        super(argument, description);
    }

    public boolean test(Vector3i sector) {
        return false;
    }

    @Override
    protected void onExecute(int argument, Vector3i sector) {
        super.onExecute(argument, sector);
    }

    protected void addString(List<String> ls) {
        ls.add("("+"false"+")");
    }
}
