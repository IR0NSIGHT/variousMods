package me.iron.newscaster.notification.infoGeneration.objectTypes;

import api.common.GameServer;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.SpaceStation;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.Faction;

import java.io.Serializable;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 15:59
 */
public class ShipObject implements Serializable {
    public static String unknownString = "unknown";
    public static int unknownNumber = -1;
    private String UID = unknownString;
    private String name = unknownString;
    private FactionObject faction;
    private String pilot = unknownString; //TODO add pilot getter, setter, getStringPretty etc.
    private int mass = unknownNumber;



    private int reactor = unknownNumber;
    private int cargoMass = unknownNumber;
    public boolean isStation = false;
    //TODO more info? type? "drive signature" for identification = UID + turning into a number?

    /**
     * creates a ship Object (pure abstract information container!) with given params.
     * by default the ship is created with "unknown" for strings and -1 for numbers. pass null and zero to keep it that way.
     * @param UID UID of ship
     * @param name name of ship
     * @param factionID belonging faction
     * @param mass mass, gets rounded.
     * @param cargoMass total cargo mass (gets rounded)
     */
    public ShipObject(String UID, String name, int factionID, int mass, int cargoMass, int reactor) {
        this.setUID(UID);
        this.setName(name);
        this.setFaction(factionID);
        this.setMass(mass);
        this.setCargoMass(mass);
        this.setReactor(reactor);
        //DebugFile.log("created ship object" + toString());
    }
    public ShipObject(SegmentController ship) {
        setUID(ship.getUniqueIdentifier());
        setName(ship.getName());

        //faction
        Faction shipFaction = ship.getFaction();
        if (shipFaction != null) {
            this.faction = new FactionObject(shipFaction);
        } else {
            this.faction = new FactionObject();
        };


        setMass((int) ship.getMass());
        //DebugFile.log("created ship object" + toString());
        //TODO get cargo mass
        double invMass = (this instanceof ManagedSegmentController<?> ? ((ManagedSegmentController<?>) this).getManagerContainer().getMassFromInventories() : 0);
        this.cargoMass = (int) invMass;
        this.isStation = (ship instanceof SpaceStation);
        this.reactor = ((ManagedSegmentController<?>)ship).getManagerContainer().getPowerInterface().getActiveReactor().getActualSize();

        ManagedUsableSegmentController<?> msc = (ManagedUsableSegmentController<?>)ship;
        for (PlayerState p: msc.getAttachedPlayers()) {
            if (p.getFirstControlledTransformableWOExc().equals(ship)) {
                pilot = p.getName();
                break;
            }
        }
    }
    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        if (UID == null) {
            return;
        }
        this.UID = UID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            return;
        }
        this.name = name;
    }

    public String getFaction() {
        return faction.getFactionName();
    }

    public void setFaction(int factionID) {
        Faction faction = GameServer.getServerState().getFactionManager().getFaction(factionID);
        if (faction == null) {
            return;
        }
       this.faction = new FactionObject(faction);
    }

    public void setFaction(FactionObject f) {
        this.faction = f;
    }

    /**
     * returns mass in k (1000)
     * @return
     */
    public int getMass() {
        return mass;
    }

    public void setMass(int mass) {
        if (mass < 0) {
            return;
        }
        mass = Math.round((float) mass/1000);
        this.mass = mass;
    }

    public int getCargoMass() {
        return cargoMass;
    }

    public void setCargoMass(int cargoMass) {
        if (cargoMass <= 0) {
            return;
        }
        this.cargoMass = cargoMass;
    }

    public int getReactor() {
        return reactor;
    }

    public void setReactor(int reactor) {
        this.reactor = reactor;
    }

    @Override
    public String toString() {
        return "ShipObject{" +
                "UID='" + UID + '\'' +
                ", name='" + name + '\'' +
                ", faction=" + faction +
                ", pilot='" + pilot + '\'' +
                ", mass=" + mass +
                ", reactor=" + reactor +
                ", cargoMass=" + cargoMass +
                ", isStation=" + isStation +
                '}';
    }

    public String getInfoStringPretty() {
        String s = "Ship '" + name + "' (";
        if (mass == unknownNumber) {
            s += "unknown ";
        } else {
            s += mass + "k ";
        };
        s += "mass) " + "[" + getFaction() + "]";
        return s;
    }
}
