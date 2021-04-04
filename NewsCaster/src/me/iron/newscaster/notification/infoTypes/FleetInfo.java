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
public class FleetInfo implements Serializable {
    public enum EventType {
        SHIP_LOST,
        ENEMY_ENCOUNTER,
        ENEMY_DESTROYED,
        SHIP_DAMAGED
    }
    //where
    Vector3i sector;
    //who (this fleetmember)
    ShipObject ship;
    EventType eventType;

    public long getTime() {
        return time;
    }

    long time;


    public FleetInfo(ShipObject ship, EventType event, Vector3i sector) {
        this.ship = ship; //who
        this.eventType = event; //what
        this.sector = sector;   //where
        this.time = System.currentTimeMillis(); //when
    }

    /**
     * creates humanely readable string
     * @return string
     */
    public String getNewscast() {
       return ship.getInfoStringPretty() + " reported " + eventType.name() + " at sector " + sector.toString() + " at " + getTimestampGMT();
    }

    /**
     * return timestamp in GMT format (string)
     * @return timestamp string
     */
    public String getTimestampGMT() {
        String s = "";
        final Date currentTime = new Date();
        currentTime.setTime(time);
        final SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, hh:mm a z");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        s += sdf.format(currentTime);
        return s;
    }

    //TODO create means to write object to persistent data

}
