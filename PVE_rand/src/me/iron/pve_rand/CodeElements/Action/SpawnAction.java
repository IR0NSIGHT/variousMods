package me.iron.pve_rand.CodeElements.Action;

import api.DebugFile;
import api.utils.StarRunnable;
import com.bulletphysics.linearmath.Transform;
import me.iron.pve_rand.CodeElements.CustomAction;
import me.iron.pve_rand.ModMain;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.SegmentController;
import org.schema.game.common.data.element.ElementDocking;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 15:13
 */
public class SpawnAction extends CustomAction implements Serializable {
    //TODO make all params null safe and type safe
    public SpawnAction(int argument, String name, String description, String blueprint, int factionID, int amount, boolean AI, int lifetime) {
        super(argument, name, description);
        params.put("factionID",factionID);
        params.put("blueprint",blueprint);
        params.put("amount",amount);
        params.put("AI",AI);
        params.put("lifetime",lifetime);
    }

    @Override
    protected void onExecute(int argument, Vector3i sector) {
        super.onExecute(argument, sector);
        for (int i = 0; i < (int)params.get("amount"); i++) {
            spawnObject(sector);
        }
    }

    private void spawnObject(Vector3i sector) {
        //get transform = worldposition wrapper
        Transform transform = new Transform();
        transform.setIdentity();
        transform.origin.set(
                -3000+(float)Math.random()*6000,
                -3000+(float)Math.random()*6000,
                -3000+(float)Math.random()*6000
        );
        //ModPlayground.broadcastMessage("spawning at: " + transform.origin);
        //TODO give array of bps, choose randomly
        //TODO delayed deleting also from unloaded + across server restarts, abort on "is docked or piloted"
        //create outline = loaded but not yet spawned entity
        SegmentControllerOutline scOutline = null;
        String blueprint = (String)params.get("blueprint");
        if (blueprint == null)
            return;
        int factionID = (int)params.get("factionID");
        boolean AI = (boolean)params.get("AI");
        final int lifetime = (int)params.get("lifetime");
        try {

            scOutline = BluePrintController.active.loadBluePrint(
                    GameServerState.instance,
                    blueprint, //catalog entry name
                    "PVE RAND " + blueprint, //ship name //TODO
                    transform, //transform position
                    -1, //credits to spend
                    factionID, //faction ID
                    sector, //sector
                    "uwuBoy8000", //spawner
                    PlayerState.buffer, //buffer (?) no idea what that is, worked fine for me as is
                    null,   //segmentpiece, no idea either
                    AI, //active ai -> basically fleet AI ship. attacks enemies.
                    new ChildStats(false)); //childstats, no idea what it does
        } catch (EntityNotFountException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EntityAlreadyExistsException e) {
            e.printStackTrace();
        }

        if (scOutline != null) {
            DebugFile.log("outline was loaded, not null!");
            //spawn the outline
            try {
                scOutline.spawn(
                        sector, //dont know what happens if you put loadsector != spawnsector
                        false, //check block counts (?) no idea
                        new ChildStats(false), //no idea again
                        new SegmentControllerSpawnCallbackDirect(GameServerState.instance, sector) { //idk what that is
                            @Override
                            public void onSpawn(final SegmentController segmentController) {
                                super.onSpawn(segmentController);
                                //delayed deleting after lifetime
                                if (lifetime == -1) return;
                                new StarRunnable() {
                                    long start = System.currentTimeMillis();
                                    @Override
                                    public void run() {
                                    //    ModPlayground.broadcastMessage("Lifetime at " + (100*System.currentTimeMillis()/(start+lifetime*1000)) +" % for " + segmentController.getName());
                                        if (start + 1000*lifetime < System.currentTimeMillis()) {
                                            //delete ship
                                            segmentController.railController.destroyDockedRecursive();
                                            for (ElementDocking dock : segmentController.getDockingController().getDockedOnThis()) {
                                                dock.from.getSegment().getSegmentController().markForPermanentDelete(true);
                                                dock.from.getSegment().getSegmentController().setMarkedForDeleteVolatile(true);
                                            }
                                            //delete original
                                            segmentController.markForPermanentDelete(true);
                                            //TODO delete when unloaded too
                                            segmentController.setMarkedForDeleteVolatile(true);
                                            cancel();
                                        }
                                    }
                                }.runTimer(ModMain.instance, 1000);
                            }

                            @Override
                            public void onNoDocker() { //in vanilla used to write a debug line.
                            }
                        });
            } catch (EntityAlreadyExistsException e) {
                e.printStackTrace();
            } catch (StateParameterNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public HashMap<String,Object> getParams() {
        HashMap<String,Object> map = new HashMap<>();

        return map;
    }

    @Override
    public String toString() {
        return "SpawnAction{" +
                "params"+params.toString()+
                '}';
    }

    protected void addString(List<String> ls) {
        ls.add("\t"+this.toString());
    }
}
