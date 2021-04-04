package me.iron.spacefarm;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.04.2021
 * TIME: 23:36
 */

import org.schema.common.util.linAlg.Vector3i;

import javax.vecmath.Vector3f;
import java.io.Serializable;

/**
 * represents the plant block/information. is persistent
 */
public class PlantObject implements Serializable {
    private Vector3i absPosition;
    private short type;
    private String segmentcontrollerUID;
    private Vector3i segmentAbsPos;
    private int lifeStage;  //how big the plant is in its lifecycle, does nothing atm

    private int data;

    public PlantObject(Vector3i absPosition, short type, String segmentcontrollerUID, Vector3i segmentAbsPos, int lifeStage) {
        this.absPosition = absPosition;
        this.type = type;
        this.segmentcontrollerUID = segmentcontrollerUID;
        this.segmentAbsPos = segmentAbsPos;
        this.lifeStage = lifeStage;
    }

    public void setData(int data) {
        this.data = data;
    }

    public Vector3i getAbsPosition() {
        return absPosition;
    }

    public short getType() {
        return type;
    }

    public String getSegmentcontrollerUID() {
        return segmentcontrollerUID;
    }

    public Vector3i getSegmentAbsPos() {
        return segmentAbsPos;
    }

    public int getLifeStage() {
        return lifeStage;
    }

    public int getData() {
        return data;
    }
}
