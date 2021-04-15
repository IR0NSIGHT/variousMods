package me.iron.newscaster.notification.objectTypes;

import org.schema.game.common.data.player.faction.Faction;

import java.io.Serializable;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 12.04.2021
 * TIME: 20:23
 */

/**
 * wrapper class for a faction. stores ID and name.
 */
public class FactionObject implements Serializable {
    int factionID = 0;
    String factionName = "";

    public FactionObject(Faction f) {
        this.factionID = f.getIdFaction();
        this.factionName = f.getName();
    }

    public FactionObject() {
    }

    public int getFactionID() {
        return factionID;
    }

    public String getFactionName() {
        return factionName;
    }

}
