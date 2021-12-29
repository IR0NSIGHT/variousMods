package me.iron.npccontrol.triggers;

import me.iron.npccontrol.ModMain;
import org.luaj.vm2.ast.Str;
import org.lwjgl.Sys;
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

    public Event(long code, int chance, int cooldown) {
        this.code = code;
        this.chance = chance;
        this.cooldown = cooldown;
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
            ModMain.log("abort random chance");
            return false;
        }
        if (cooldown > 0 && System.currentTimeMillis()<last+cooldown) {
            ModMain.log("abort cooldown, wait: " + (last+cooldown- System.currentTimeMillis()));
            return false;
        }
        ModMain.log(String.format("%s input\n%s code\n%s differece\n\n",Trigger.toBin(inputCode),Trigger.toBin(code),Trigger.toBin(inputCode&code)));
        for (long i = 0; i < 8; i++) {
            long mask =255L<<(8L*i);
            //ModMain.log(String.format("%s mask, %s value",Trigger.toBin(mask),mask));
            ModMain.log(String.format("%s mask \n%s input\n%s code\n%s differece\n\n",Trigger.toBin(mask),Trigger.toBin(mask&inputCode),Trigger.toBin(mask&code),Trigger.toBin((mask&(inputCode&code)))));

            if (((mask&code) != 0) && (mask&(inputCode&code)) == 0) {
                ModMain.log("ABORT");
                return false;
            }
        }
        return true;
    }

    /**
     * internal method that runs when the event successfully triggers
     */
    protected void run(long code, Vector3i sector) {
        ModMain.log(String.format("event %s was run with c=%s, pos=%s",getName(),Long.toBinaryString(code),sector.toStringPure()));
        last = System.currentTimeMillis();
    }

    protected String getName() {
        return "?";
    }
}
