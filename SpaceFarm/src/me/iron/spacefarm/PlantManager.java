package me.iron.spacefarm;

import api.DebugFile;
import api.ModPlayground;
import api.listener.Listener;
import api.listener.events.block.SegmentPieceAddEvent;
import api.listener.events.block.SegmentPieceKillEvent;
import api.listener.events.block.SegmentPieceRemoveEvent;
import api.listener.events.block.SegmentPieceSalvageEvent;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import org.schema.common.util.linAlg.Vector3b;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.world.Segment;
import org.schema.game.server.data.GameServerState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.04.2021
 * TIME: 22:38
 */
public class PlantManager {
    private List<PlantObject> plants = new ArrayList<PlantObject>();
    private HashMap<Vector3i,PlantObject> plantMap = new HashMap<>();
    private List<Short> plantTypes = new ArrayList<Short>(Arrays.<Short>asList((short) 98,(short) 99,(short) 102));
    private Vector3i tempBlock = new Vector3i();

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
                if (!event.isServer()) {
                    ModPlayground.broadcastMessage("is not server");
                    return;
                }
                ModPlayground.broadcastMessage("is server");
                //block placed event
                ModPlayground.broadcastMessage("new type: " + event.getNewType());
                if (plantTypes.contains(event.getNewType())) {    //grass 98, flower 99, berry 102
                    ModPlayground.broadcastMessage("possible plant detected.");
                    //save berry bush to list
                    SegmentPiece block = new SegmentPiece(event.getSegment(), event.getX(), event.getY(), event.getZ());
                    if (plantMap.get(block.getAbsolutePos(new Vector3i())) != null) {
                        return;
                    }
                    ModPlayground.broadcastMessage("new plant.");
                    //save: position, type, orientation, segmentcontroller, time
                //   block.setType((short) 1);
                //   block.applyToSegment(true);

                    plantMap.put(block.getAbsolutePos(new Vector3i()),new PlantObject(block));
                    //force synch (probably)
                //    long index = ElementCollection.getEncodeActivation(factorySP, true, active, false);
                //    factorySP.getSegment().getSegmentController().sendBlockActivation(index);
                }
            }
        },ModMain.instance);

        StarLoader.registerListener(SegmentPieceRemoveEvent.class, new Listener<SegmentPieceRemoveEvent>() {
            @Override
            public void onEvent(SegmentPieceRemoveEvent event) {
                if (!event.isServer()) {
                    return;
                }
                SegmentPiece block = new SegmentPiece(event.getSegment(),event.getX(),event.getY(),event.getZ());
                if (isListedPlant(block)) {
                    removeFromPlantMap(block);
                }
            }
        },ModMain.instance);

        StarLoader.registerListener(SegmentPieceSalvageEvent.class, new Listener<SegmentPieceSalvageEvent>() {
            @Override
            public void onEvent(SegmentPieceSalvageEvent event) {
                if (!event.isServer()) {
                    return;
                }
                if (isListedPlant(event.getBlockInternal())) {
                    removeFromPlantMap(event.getBlockInternal());
                };
            }
        },ModMain.instance);

        StarLoader.registerListener(SegmentPieceKillEvent.class, new Listener<SegmentPieceKillEvent>() {
            @Override
            public void onEvent(SegmentPieceKillEvent event) {
                if (!event.isServer()) {
                    return;
                }
                if (isListedPlant(event.getPiece())) {
                    removeFromPlantMap(event.getPiece());
                }
            }
        },ModMain.instance);
        //debug chat handler
        AddChatListener();
    }

    /**
     * changes the type of all stored berries
     */
    private void UpdateBerries() {
        for (PlantObject plant: plantMap.values()) {
            //abort if unloaded
            SegmentController ship = GameServerState.instance.getSegmentControllersByName().get(plant.getSegmentcontrollerUID());

            if (ship == null) {
                DebugFile.log("segmentcontroller " + plant.getSegmentcontrollerUID() + "is null ");
                continue;
            }

            //create pointer to block
            Segment segment = plant.getSegmentData().getSegment();
            SegmentPiece block = new SegmentPiece(segment, plant.getAbsPosition(),plant.getPieceData());
            tempBlock.set(block.getAbsolutePos(new Vector3i()));
            //change block type
            short type = incrementLifeStage(plant.getType());
            block.setType(type);
            block.applyToSegment(true);

            plantMap.put(block.getAbsolutePos(new Vector3i()),new PlantObject(block));
        }
    }

    private void AddChatListener() {
     StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
         @Override
         public void onEvent(PlayerChatEvent event) {
             if (event.isServer()) {
                 return;
             }

             if (event.getText().contains("berry")) {
                 UpdateBerries();
             }

             if (event.getText().contains("list")) {
                 DebugFile.log(plants.toString());
             }

             ModPlayground.broadcastMessage("i received your message, uwu!");
         }
     },ModMain.instance);
    }
    private short incrementLifeStage(short type) {
        switch(type){
            case 98:
                type = 99;
                break;
            case 99:
                type = 102;
                break;
            case 102:
                type = 98;
                break;
            default:
                System.out.println("not defined.");
                break;
        }
        return type;
    }

    private boolean isListedPlant(SegmentPiece block) {
        if (tempBlock.equals(block.getAbsolutePos(new Vector3i()))) {
            tempBlock.set(new Vector3i());
            return false;
        }
        if (plantMap.get(block.getAbsolutePos(new Vector3i())) == null) {
            return false;
        } else {
            return true;
        }
    }
    private void removeFromPlantMap(SegmentPiece block) {
        plantMap.remove(block.getAbsolutePos(new Vector3i()));
    }
}
