package me.iron.npccontrol.pathing.sm;

import me.iron.npccontrol.triggers.Utility;
import org.schema.common.util.linAlg.Vector3i;

import javax.vecmath.Vector3f;
import java.util.Objects;
import java.util.Vector;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 08.01.2022
 * TIME: 12:11
 */
public class StellarPosition {
    private Vector3i sector = new Vector3i();
    private Vector3f position = new Vector3f();
    public StellarPosition(Vector3i sector, Vector3f position) {
        setPosition(position);
        setSector(sector);
    }
    public StellarPosition() {}
    public Vector3i getSector() {
        return sector;
    }

    public void setSector(Vector3i sector) {
        this.sector.set(sector);
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    /**
     * get relative to this sector
     * @param sector
     * @return
     */
    public Vector3f getRelativePosition(Vector3i sector) {
        return Utility.getDir(sector,new Vector3f(0,0,0),this.sector,this.position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StellarPosition that = (StellarPosition) o;
        return sector.equals(that.sector) &&
                position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sector, position);
    }
}
