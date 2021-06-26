package me.iron.newscaster.notification.infoGeneration.infoTypes;

import me.iron.newscaster.notification.infoGeneration.objectTypes.FactionObject;
import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 12.04.2021
 * TIME: 20:01
 */
public class FactionInfo extends GenericInfo {
    FactionObject faction;
    public FactionInfo(FactionObject faction, EventType type) {
        super(new Vector3i(), type);
        this.faction = faction;
    }
}
