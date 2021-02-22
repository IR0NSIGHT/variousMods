package me.iron.fleetCommander.notification.infoTypes;

import me.iron.fleetCommander.notification.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 16:49
 */
public class ShipDestroyedInfo extends FleetInfo {
    public ShipDestroyedInfo(ShipObject victim, ShipObject attacker, Vector3i sector) {
        super(victim, EventType.SHIP_LOST, sector);
        this.attacker = attacker;
    }
    private ShipObject attacker;

    @Override
    public String getNewscast() {
        return "Ship " + ship.getInfoStringPretty() + " was lost to " + attacker.getInfoStringPretty() + " at sector " + sector.toString() + " at " + getTimestampGMT();
    }
}
