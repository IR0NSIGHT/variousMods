package me.iron.newscaster.testing;

import me.iron.newscaster.notification.infoTypes.FleetInfo;
import me.iron.newscaster.notification.infoTypes.ShipDestroyedInfo;
import me.iron.newscaster.notification.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 17:15
 */
public class main {
    public static void main(String[] args) {
       FleetInfo info = new FleetInfo(new ShipObject(null,null,null,-1,-1),FleetInfo.EventType.ENEMY_ENCOUNTER,new Vector3i(0,420,69));
       System.out.println(info.getNewscast());

       ShipObject victim = new ShipObject("InnocentMiner01","Innocence","BlackWork Mechanics",2459,300000);
       ShipObject attacker = new ShipObject("EvilWarlordPirateShip","RED FLOOD II","bloodcrows",15420,0);
        ShipDestroyedInfo report = new ShipDestroyedInfo(victim,attacker,new Vector3i(0,420,69));

       System.out.println(report.getNewscast());
       System.out.println(victim.toString());
    }
}
