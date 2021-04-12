package me.iron.newscaster.notification.infoTypes;

import me.iron.newscaster.notification.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 16:49
 */
public class ShipDestroyedInfo extends EntityInfo {
    public ShipDestroyedInfo(ShipObject victim, ShipObject attacker, Vector3i sector) {
        super(victim, EventType.SHIP_LOST, sector);
        this.attacker = attacker;
    }
    private ShipObject attacker;

    @Override
    public String getNewscast() {
        return "" + ship.getInfoStringPretty() + " was lost to " + attacker.getInfoStringPretty() + " at sector " + sector.toString() + " at " + getTimestampGMT();
    }
}
