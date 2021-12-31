package me.iron.npccontrol.triggers;

import me.iron.npccontrol.ModMain;
import org.luaj.vm2.ast.Str;
import org.lwjgl.Sys;
import org.lwjgl.openal.Util;
import org.schema.common.util.linAlg.Vector3i;

import java.util.HashMap;
import java.util.Set;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 29.12.2021
 * TIME: 21:30
 */
public class Event {
    final private HashMap<Long,Long> code_to_last = new HashMap<>();

    /**
     *
     */
    public Event() {
    };

    public void setCode(long code) {
        code_to_last.put(code,-1L);
    }

    public boolean removeCode(long code) {
        if (code_to_last.containsKey(code)) {
            code_to_last.remove(code);
            return true;
        }
        return false;
    }

    public Set<Long> getCodes() {
        return code_to_last.keySet();
    }

    private long getLastByCode(long code) {
        if (!code_to_last.containsKey(code))
            return -1L;
        return code_to_last.get(code);
    }

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
       if (Utility.getRand().nextInt(255) >= Utility.get8bit(inputCode,7)) {
           //ModMain.log("abort random chance");
           return false;
       }
       long cooldown = Utility.get8bit(inputCode,6);
        if (cooldown > 0 && System.currentTimeMillis()< getLastByCode(c) +cooldown) {
            //ModMain.log("abort cooldown, wait: " + (last+cooldown- System.currentTimeMillis()));
            return false;
        }
        //ModMain.log(String.format("%s input\n%s code\n%s differece\n\n",Trigger.toBin(inputCode),Trigger.toBin(code),Trigger.toBin(inputCode&code)));
        for (long i = 0; i < 8; i++) {
            long mask =255L<<(8L*i);
            ////ModMain.log(String.format("%s mask, %s value",Trigger.toBin(mask),mask));
        //    //ModMain.log(String.format("%s mask \n%s input\n%s code\n%s differece\n\n",Trigger.toBin(mask),Trigger.toBin(mask&inputCode),Trigger.toBin(mask&code),Trigger.toBin((mask&(inputCode&code)))));

      //     if (((mask&code) != 0) && (mask&(inputCode&code)) == 0) {
      //         //ModMain.log("ABORT code mismatch");
      //         return false;
      //     }
        }
        return true;
    }

    /**
     * internal method that runs when the event successfully triggers
     */
    protected void run(long code, Vector3i sector) {
        //ModMain.log(String.format("event %s was run with c=%s, pos=%s",getName(),Long.toBinaryString(code),sector.toStringPure()));
      //  last = System.currentTimeMillis();
    }

    protected String getName() {
        return "?";
    }

    public static void main(String[] args) {
        long code = 0;
        code = Utility.add8bit(code,0b00001111,7);
        code = Utility.add8bit(code,0b00001111,1);

        System.out.println(Trigger.toBin(code));
        code = Utility.add8bit(code,0b10101010,1);

        System.out.println(Trigger.toBin(code));
        System.out.println(Utility.get8bit(code,7));
        System.out.println(Utility.get8bit(code,1));

      //  System.out.println(Integer.toBinaryString(Utility.get8bit(code,1)));

    }
}
