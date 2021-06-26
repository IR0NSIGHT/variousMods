package me.iron.newscaster.notification.infoGeneration.infoTypes;

import org.schema.common.util.linAlg.Vector3i;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 12.04.2021
 * TIME: 20:02
 */
public class GenericInfo {
    public enum EventType {
        GENERIC,

        //ENTITY STUFF
        SHIP_LOST,
        ENEMY_ENCOUNTER,
        ENEMY_DESTROYED,
        SHIP_DAMAGED,
        SHIP_SPAWNED,

        //faction stuff
        WAR_DECLARATION,
        PEACE_DECLARATION,
        PEACE_OFFER,
        ALLIANCE_DECLARATION,
        ALLIANCE_OFFER,
        SYSTEM_CONQUERED,
        SYSTEM_LOST,
    }
    //where
    Vector3i sector;
    //what
    EventType eventType;
    //when
    long time;

    public GenericInfo(Vector3i sector, EventType type) {
        this.sector = sector;
        this.eventType = type;
        this.time = System.currentTimeMillis();
    }

    public EventType getType() {
        return eventType;
    }

    /**
     * return timestamp of info creation.
     * @return
     */
    public long getTime() {
        return time;
    }

    /**
     * @return sector of event
     */
    public Vector3i getSector() {
        return sector;
    }
    /**
     * creates humanely readable string.
     * @return string
     */
    public String getNewscast() {
        return eventType.name() + " reported " + " at sector " + sector.toString() + " at " + getTimestampGMT();
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
}
