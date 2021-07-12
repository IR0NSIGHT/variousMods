package me.iron.pve_rand.Action.checks;

import me.iron.pve_rand.Action.ActionController;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.StellarSystem;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;
import java.io.Serializable;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.06.2021
 * TIME: 18:48
 * Tests if system belongs to a faction
 */
public class SystemCheck extends BooleanAction implements Serializable {
    private int faction;
    private boolean invert;
    public SystemCheck(int argument, String description, int faction, boolean invert) {
        super(argument, description);
        this.faction = faction;
        this.invert = invert;
    }

    @Override
    public boolean test(Vector3i sector) {
        StellarSystem system;
        try {
            system = GameServerState.instance.getUniverse().getStellarSystemFromSecPos(sector);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        int owner = ActionController.getFactionIndex(system.getOwnerFaction());
        return (invert?(owner!=faction):(owner==faction));
    }

    @Override
    public String toString() {
        return "SystemCheck{" +
                "faction=" + faction +
                ", invert=" + invert +
                '}';
    }
}
