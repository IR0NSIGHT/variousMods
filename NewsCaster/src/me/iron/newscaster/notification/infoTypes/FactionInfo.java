package me.iron.newscaster.notification.infoTypes;

import me.iron.newscaster.notification.objectTypes.FactionObject;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.faction.Faction;

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
