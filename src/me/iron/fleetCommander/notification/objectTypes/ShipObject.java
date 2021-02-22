package me.iron.fleetCommander.notification.objectTypes;

import api.DebugFile;
import org.schema.game.common.controller.SegmentController;
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
    private String faction = unknownString;
    private String pilot = unknownString; //TODO add pilot getter, setter, getStringPretty etc.
    private int mass = unknownNumber;
    private int cargoMass = unknownNumber;
    //TODO more info? type? "drive signature" for identification = UID + turning into a number?

    /**
     * creates a ship Object (pure abstract information container!) with given params.
     * by default the ship is created with "unknown" for strings and -1 for numbers. pass null and zero to keep it that way.
     * @param UID UID of ship
     * @param name name of ship
     * @param faction belonging faction
     * @param mass mass, gets rounded.
     * @param cargoMass total cargo mass (gets rounded)
     */
    public ShipObject(String UID, String name, String faction, int mass, int cargoMass) {
        this.setUID(UID);
        this.setName(name);
        this.setFaction(faction);
        this.setMass(mass);
        this.setCargoMass(mass);
        //DebugFile.log("created ship object" + toString());
    }
    public ShipObject(SegmentController ship) {

        setUID(ship.getUniqueIdentifier());
        setName(ship.getName());
        //faction
        Faction shipFaction = ship.getFaction();
        String factionName = "unfactioned";
        if (shipFaction != null) {
            factionName = shipFaction.getName();
        }

        setMass((int) ship.getMass());
        //DebugFile.log("created ship object" + toString());
        //TODO get cargo mass
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
        return faction;
    }

    public void setFaction(String faction) {
        if (faction == null) {
            return;
        }
        this.faction = faction;
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

    @Override
    public String toString() {
        return "ShipObject{" +
                "UID='" + UID + '\'' +
                ", name='" + name + '\'' +
                ", faction='" + faction + '\'' +
                ", pilot='" + pilot + '\'' +
                ", mass=" + mass +
                ", cargoMass=" + cargoMass +
                '}';
    }

    public String getInfoStringPretty() {
        String s = "Ship '" + name + "' (";
        if (mass == unknownNumber) {
            s += "unknown ";
        } else {
            s += mass + "k ";
        };
        s += "mass) " + "[" + faction + "]";
        return s;
    }
}
