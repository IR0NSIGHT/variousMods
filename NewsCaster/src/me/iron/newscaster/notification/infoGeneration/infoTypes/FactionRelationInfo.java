package me.iron.newscaster.notification.infoGeneration.infoTypes;

import me.iron.newscaster.notification.infoGeneration.objectTypes.FactionObject;
import org.schema.game.common.data.player.faction.Faction;
import org.schema.game.common.data.player.faction.FactionRelation;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 12.04.2021
 * TIME: 20:22
 */
public class FactionRelationInfo extends FactionInfo {
    FactionObject faction1;
    FactionObject faction2;
    public FactionRelationInfo(Faction f1, Faction f2, FactionRelation.RType oldRelation, FactionRelation.RType newRelation) {
        super(new FactionObject(f1),EventType.GENERIC);
        switch (newRelation.getName()) {
            case "NEUTRAL":
                this.eventType = EventType.PEACE_DECLARATION;
                break;
            case "FRIEND":
                this.eventType = EventType.ALLIANCE_DECLARATION;
                break;
            case "ENEMY":
                this.eventType = EventType.WAR_DECLARATION;
                break;
        };
        faction1 = new FactionObject(f1);
        faction2 = new FactionObject(f2);
    }

    @Override
    public String getNewscast() {
        String s = "";
        switch (eventType) {
            case PEACE_DECLARATION:
                s += "Faction " + faction1.getFactionName() + " and " + faction2.getFactionName() + " have declared peace";
                break;
            case ALLIANCE_DECLARATION:
                s += "Faction " + faction1.getFactionName() + " and " + faction2.getFactionName() + " are now allied";
                break;
            case WAR_DECLARATION:
                s += "Faction " + faction1.getFactionName() + " and " + faction2.getFactionName() + " have declared war on eachother";
                break;
        };
        s += " at " + getTimestampGMT();
        return s;
    }
}
