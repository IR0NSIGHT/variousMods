package me.iron.npccontrol.triggers.events;

import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.Event;
import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 29.12.2021
 * TIME: 21:38
 * an event that spawns a jammed pirate scout satellite in an asteroid belt near a player.
 */
public class PirateScoutRaid extends Event {

    public PirateScoutRaid(long code, int chance, int cooldown) {
        super(code,chance,cooldown);
    }

    @Override
    protected String getName() {
        return "Pirate Scout Raid";
    }

    @Override
    protected void run(long code, Vector3i sector) {
        super.run(code, sector);
        ModMain.log("RUN PIRATE SCOUT RAID");
    }

    @Override
    protected boolean triggerCheck(long inputCode, Vector3i pos) {
        if (!super.triggerCheck(inputCode, pos))
            return false;


        return true;
    }
}
