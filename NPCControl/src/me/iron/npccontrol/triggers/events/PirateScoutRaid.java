package me.iron.npccontrol.triggers.events;

import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.Event;
import me.iron.npccontrol.triggers.FleetComposition;
import me.iron.npccontrol.triggers.Utility;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.simulation.groups.SimulationGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 29.12.2021
 * TIME: 21:38
 * an event that spawns a jammed pirate scout satellite in an asteroid belt near a player.
 */
public class PirateScoutRaid extends Event {
    Random r = new Random();
    FleetComposition f;
    public PirateScoutRaid(long code, int chance, int cooldown, String name) {
        super(code,chance,cooldown, name);
        f = new FleetComposition(2);
        f.addEntry(0,Utility.getBlueprintByName("SnakeEye"),1);
        f.addEntry(1,Utility.getBlueprintByName("Venom"),2);
    }




    @Override
    protected void run(long code, Vector3i sector) {
        super.run(code, sector);
        //ModMain.log("RUN PIRATE SCOUT RAID");
        ArrayList<PlayerState> players = Utility.getPlayersByDistance(sector);
        if (players.size()==0)
            return;
        PlayerState p = players.get(0); //closest

        f = new FleetComposition(2);
        f.addEntry(0,Utility.getBlueprintByName("SnakeEye"),r.nextInt(3)-1);
        f.addEntry(1,Utility.getBlueprintByName("Venom"),r.nextInt(4));

        //TODO select a better target than just players ship: slowest, biggest and weakest ship.
        SimpleTransformableSendableObject s = p.getFirstControlledTransformableWOExc();
        if (s != null) {
            Utility.spawnAdvancedHunt(sector,s,f.toFlatArray(), -2);
        }
        ModMain.log(String.format("pirate scout raid ran with code: %s",Utility.toBin(code)));
    }

    @Override
    protected boolean triggerCheck(long inputCode, Vector3i pos) {
        if (!super.triggerCheck(inputCode, pos))
            return false;


        return true;
    }
}
