package me.iron.pve_rand.Action;

import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.06.2021
 * TIME: 17:47
 */
public class CustomCondition extends CustomAction implements Serializable {
    public CustomCondition(int argument, String description) {
        super(argument, description);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void onExecute(int argument, Vector3i sector) {
        super.onExecute(argument, sector);
    }
}
