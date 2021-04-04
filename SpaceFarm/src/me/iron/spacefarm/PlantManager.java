package me.iron.spacefarm;

import api.DebugFile;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.Event;
import api.listener.events.block.SegmentPieceAddEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.ManagedUsableSegmentController;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.controller.elements.ManagerContainer;
import org.schema.game.common.data.ManagedSegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.game.server.data.GameServerState;

import javax.swing.text.Segment;
import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.04.2021
 * TIME: 22:38
 */
public class PlantManager {
    private List<PlantObject> plants = new ArrayList<PlantObject>();

    /**
     * create new plantmanager that collects and stores all plants and manages their lifecycle.
     */
    public PlantManager() {
        DebugFile.log("plantmanager created.");
        addListeners();
    }

    /**
     * event listeners for:
     * block place event
     */
    private void addListeners() {
        StarLoader.registerListener(SegmentPieceAddEvent.class, new Listener<SegmentPieceAddEvent>() {
            @Override
            public void onEvent(SegmentPieceAddEvent event) {
                //block placed event
                event.getNewType(); //berry = 102
                if (event.getNewType() == 102) {
                    //save berry bush to list
                    SegmentPiece block = new SegmentPiece(event.getSegment(), event.getX(), event.getY(), event.getZ());
                    //save: position, type, orientation, segmentcontroller, time
                    block.setType((short) 1);
                    block.applyToSegment(true);

                    PlantObject plant = new PlantObject(block.getAbsolutePos(new Vector3i()),block.getType(),block.getSegmentController().getUniqueIdentifierFull(),block.getSegment().absPos,0);
                    plant.setData(block.getData());
                    plants.add(plant);

                    //force synch (probably)
                //    long index = ElementCollection.getEncodeActivation(factorySP, true, active, false);
                //    factorySP.getSegment().getSegmentController().sendBlockActivation(index);
                }
            }
        },ModMain.instance);
    }

    /**
     * changes the type of all stored berries
     */
    private void UpdateBerries() {
        for (PlantObject plant: plants) {
            SegmentController ship = GameServerState.instance.getSegmentControllersByName().get(plant.getSegmentcontrollerUID());
       //     ship.getSegmentProvider().
            SegmentPiece block = new SegmentPiece(plant.getSegmentcontrollerUID())
        }
    }

}
