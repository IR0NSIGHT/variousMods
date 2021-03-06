package me.iron.newscaster.testing;

import me.iron.newscaster.notification.infoGeneration.infoTypes.EntityInfo;
import me.iron.newscaster.notification.infoGeneration.infoTypes.ShipCreatedInfo;
import me.iron.newscaster.notification.infoGeneration.infoTypes.ShipDestroyedInfo;
import me.iron.newscaster.notification.infoGeneration.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 17:15
 */
public class main {
    //TODO make debug info generator
    public static void main(String[] args) {
       EntityInfo info = new EntityInfo(new ShipObject(null,null,0,-1,-1,10), EntityInfo.EventType.ENEMY_ENCOUNTER,new Vector3i(0,420,69));
       System.out.println(info.getNewscast());

       ShipObject victim = new ShipObject("InnocentMiner01","Innocence",10001,2459,300000,19);
       ShipObject attacker = new ShipObject("EvilWarlordPirateShip","RED FLOOD II",10006,15420,0,10);
        ShipDestroyedInfo report = new ShipDestroyedInfo(victim,attacker,new Vector3i(0,420,69));

       System.out.println(report.getNewscast());
       System.out.println(victim.toString());

        ShipCreatedInfo report1 = new ShipCreatedInfo(attacker,new Vector3i(0, 800, 222222));
        System.out.println(report1.getNewscast());
    }
}
