package me.iron.spacefarm;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.04.2021
 * TIME: 23:36
 */

import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.SegmentData;

import java.io.Serializable;

/**
 * represents the plant block/information. is persistent
 */
public class PlantObject implements Serializable {
    private Vector3b absPosition;
    private short type;
    private String segmentcontrollerUID;
    private int lifeStage;  //how big the plant is in its lifecycle, does nothing atm

    private int pieceData;
    private SegmentData segmentData;

    public PlantObject(SegmentPiece block) {
        Vector3i absPosition = block.getAbsolutePos(new Vector3i());
        this.absPosition = new Vector3b(absPosition.x,absPosition.y,absPosition.z);
        this.type = block.getType();
        this.segmentData = block.getSegment().getSegmentData();
        this.pieceData = block.getData();
        this.segmentcontrollerUID = block.getSegmentController().getUniqueIdentifier();
        this.lifeStage = 0;
    }
    public void setPieceData(int data) {
        this.pieceData = data;
    }

    public void setSegmentData(SegmentData segmentData) {
        this.segmentData = segmentData;
    }

    public void setType(short type) {
        this.type = type;
    }

    public Vector3b getAbsPosition() {
        return absPosition;
    }

    public short getType() {
        return type;
    }

    public String getSegmentcontrollerUID() {
        return segmentcontrollerUID;
    }

    public int getLifeStage() {
        return lifeStage;
    }

    public int getPieceData() {
        return pieceData;
    }

    public SegmentData getSegmentData() {
        return segmentData;
    }
}
