package me.iron.newscaster.notification.infoGeneration.objectTypes;

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
        if (f == null) {
            return;
        }
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

    public void setFactionID(int factionID) {
        this.factionID = factionID;
    }

    public void setFactionName(String factionName) {
        this.factionName = factionName;
    }

    @Override
    public String toString() {
        return "FactionObject{" +
                "factionID=" + factionID +
                ", factionName='" + factionName + '\'' +
                '}';
    }
}
