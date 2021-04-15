package me.iron.newscaster.notification.infoTypes;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 15:46
 */

import me.iron.newscaster.notification.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * an object that stores information about an event happening to a fleet:
 * f.e. "fleet x lost ship y to enemy ship z at sector abc".
 */
public class EntityInfo extends GenericInfo {
    //who (this fleetmember)
    ShipObject ship;

    public EntityInfo(ShipObject ship, EventType event, Vector3i sector) {
        super(sector,event);
        this.ship = ship; //who
    }

}
