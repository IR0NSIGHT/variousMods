package me.iron.npccontrol.triggers;

import me.iron.npccontrol.ModMain;
import org.schema.game.common.data.player.catalog.CatalogPermission;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.12.2021
 * TIME: 14:28
 * container class for mapping blueprints and amounts of wanted ships, plus a single flagship.
 * used for easier spawning of AI/NPC ships/fleets.
 */
public class FleetComposition {
    final private CatalogPermission[] blueprints;
    final private int[] count;
   // private String[] names; //TODO build utility for naming ships
    private int flagship = 0;

    private int total = 0;

    public FleetComposition(int size) {
        this.blueprints = new CatalogPermission[size];
        this.count      = new int[size];
    }

    public void addFlagship(int i) {
        flagship = i;
    }
    /**
     * how many ships of blueprints[i] should spawn
     * @param index
     * @param amount
     */
    public void addEntry(int index, CatalogPermission bp, int amount) {
        blueprints[index] = bp;
        count[index] = amount;
        total += amount;
    }

    public int getAmount(int index) {
        return count[index];
    }

    public CatalogPermission getBlueprint(int index) {
        return blueprints[index];
    }

    public int getFlagship() {
        return flagship;
    }

    public String toPrettyString() {
        StringBuilder b = new StringBuilder("FleetComposition\n");
        for (int i = 0; i < blueprints.length; i++) {
            b.append(String.format("#%s - BP:%s(mass:%s,type:%s-%s)\n",count[i],blueprints[i].getUid(),blueprints[i].mass,blueprints[i].getEntry(),blueprints[i].getClassification()));
        }
        return b.toString();
    }

    public CatalogPermission[] toFlatArray() {
        CatalogPermission[] out = new CatalogPermission[total];
        int idx = 0;
        for (int i = 0; i < blueprints.length; i++) {
            for (int j = 0; j < count[i]; j++) {
                out[idx++] = blueprints[i];
            }
        }

        StringBuilder b = new StringBuilder("fleet comp:");
        for (CatalogPermission p: out) {
            b.append(p.getUid()).append(",");
        }
        ModMain.log(b.toString());

        return out;
    }
}
