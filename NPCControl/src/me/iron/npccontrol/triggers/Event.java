package me.iron.npccontrol.triggers;

import me.iron.npccontrol.ModMain;
import org.luaj.vm2.ast.Str;
import org.lwjgl.Sys;
import org.lwjgl.openal.Util;
import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 29.12.2021
 * TIME: 21:30
 */
public class Event {
    private long code;
    private long last;
    private int cooldown;
    private int chance;
    private String name; //used to tell the difference between two similar ones
    public Event(long code, int chance, int cooldown, String name) {
        this.code = code;
        this.chance = chance;
        this.cooldown = cooldown*1000;
    };

    /**
     * method that gets called when the event is triggered. will evaluate cooldown and random chance.
     */
    public void trigger(long inputCode, Vector3i pos) {
        if (!triggerCheck(inputCode, pos))
            return;
        run(inputCode,pos);
    }

    /**
     * is evaluated before event is run. overwrite as you like for custom behaviour.
     * @param inputCode code that contains bitwise info about the event
     * @return
     */
    protected boolean triggerCheck(long inputCode, Vector3i pos) {
        if (Math.random()*100>=chance) {
            return false;
        }
        if (cooldown > 0 && System.currentTimeMillis()<last+cooldown) {
            return false;
        }
        for (int i = 0; i < 8; i++) {
            long in = Utility.get8bit(inputCode,i);
            long own = Utility.get8bit(code,i);
            System.out.println(Long.toBinaryString(in));
            System.out.println(Long.toBinaryString(own));
            if (own > 0 && (in&own)==0) {
                return false;
            }
        }
        return true;
    }

    /**
     * internal method that runs when the event successfully triggers
     */
    protected void run(long code, Vector3i sector) {
        last = System.currentTimeMillis();
    }

    protected String getName() {
        return name;
    }
}
