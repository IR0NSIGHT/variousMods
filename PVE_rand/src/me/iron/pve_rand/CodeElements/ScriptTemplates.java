package me.iron.pve_rand.CodeElements;

import me.iron.pve_rand.CodeElements.Action.ChatAction;
import me.iron.pve_rand.CodeElements.Action.SpawnAction;

import java.util.Arrays;
import java.util.HashSet;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.06.2021
 * TIME: 19:55
 */
public class ScriptTemplates {
    //will spawn guarding mobs near derelict stations
    public static void derelictGuardian() {
        CustomTrigger t = new CustomTrigger(new HashSet<Integer>(Arrays.asList(0x010602FA)) ,"derelict mobs","derelict station loaded");
        CustomScript script = new CustomScript(null,"guardian mob spawner","spawns spawner bunch of hostile mods");
        t.addScript(script);
        SpawnAction spawner = new SpawnAction(100,"pirate script","3x small pirate","pirate-ship-01",-1,3,true,60*60);
        script.addCodeBlock(spawner);

        //add rare boss
        spawner = new SpawnAction(20,"bossmob","one big pirate ship","pirate-boss-01",-1,1,true,60*60);
        script.addCodeBlock(spawner);
        ChatAction chat = new ChatAction(100,"mob warning","warns player about mobs","Watch out for the guardian!",5);
        spawner.addCodeBlock(chat);
    }

    public static void lostCargo() {
        CustomTrigger t = new CustomTrigger(new HashSet<Integer>(Arrays.asList(0x01000301)),"lostcargo","roid loaded");
        CustomScript script = new CustomScript(null,"wreck+SOS","spawns a wreck+containers and sends an SOS");
        t.addScript(script);
        script.setTimeout(30*60);
        //spawn wreckage
        SpawnAction spawner = new SpawnAction(30,"floating wreck","spawn wrecked ship","wreck-ship-01",0,1,false,-1);
        script.addCodeBlock(spawner);

        //spawn a bunch of cargo crates floating around
        spawner = new SpawnAction(100,"3 cargo crates","spawn bunch of cargo crates","cargo-crate-01",0,3,false,-1);
        script.addCodeBlock(spawner);

        ChatAction chat = new ChatAction(100,"SOS signal","send SOS signal to nearby players","SOS SIGNAL DETECTED AT ",20,true);
        script.addCodeBlock(chat);

    }
}
