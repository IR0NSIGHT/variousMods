package me.iron.newscaster.notification.infoGeneration.infoTypes;

import me.iron.newscaster.notification.infoGeneration.objectTypes.FactionObject;
import org.schema.common.util.linAlg.Vector3i;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 26.06.2021
 * TIME: 17:17
 */
public class FactionSystemClaimInfo extends FactionInfo {
    public FactionObject getOldOwner() {
        return oldOwner;
    }

    public Vector3i getSystem() {
        return system;
    }

    FactionObject oldOwner;
    Vector3i system;
    public FactionSystemClaimInfo(FactionObject newOwner, FactionObject oldOwner, EventType type, Vector3i system) {
        super(newOwner, type);
        this.oldOwner = oldOwner;
        this.system = system;
    }
}
