package me.iron.pve_rand;

import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;

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

    public CustomAction(int argument, String description) {
        this.argument = argument;
        this.description = description;
    }

    public void execute( Vector3i sector) {
        onExecute(argument, sector);
    }

    private void onExecute(int argument,  Vector3i sector) {
        //action class (spawn, despawn, etc)
        int prefix = argument >> 24;
        switch (prefix) {
            case 0:
                //spawn a pirate nearby

                return;
            default:
                return;
        }

    }
}
