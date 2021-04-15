package me.iron.newscaster.notification.infoTypes;

import me.iron.newscaster.notification.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 12.04.2021
 * TIME: 18:37
 */
public class ShipCreatedInfo extends EntityInfo {
    public ShipCreatedInfo(ShipObject ship, Vector3i sector) {
        super(ship, EventType.SHIP_SPAWNED, sector);
    }

    @Override
    public String getNewscast() {
        return "" + ship.getInfoStringPretty() + " was created at " + sector.toString() + " at " + getTimestampGMT();
    }
}
