package me.iron.npccontrol;

import api.DebugFile;
import api.ModPlayground;
import api.common.GameClient;
import api.common.GameServer;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.utils.StarRunnable;
import com.bulletphysics.linearmath.Transform;
import org.schema.game.client.data.GameClientState;
import org.schema.game.common.data.player.PlayerCharacter;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogManager;
import org.schema.game.common.data.player.faction.FactionManager;
import org.schema.game.server.controller.BluePrintController;
import org.schema.game.server.controller.EntityAlreadyExistsException;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.GameServerState;
import org.schema.game.server.data.blueprint.ChildStats;
import org.schema.game.server.data.blueprint.SegmentControllerOutline;
import org.schema.game.server.data.blueprint.SegmentControllerSpawnCallbackDirect;
import org.schema.schine.common.language.Lng;
import org.schema.schine.graphicsengine.core.settings.StateParameterNotFoundException;

import javax.vecmath.Vector3f;
import java.io.IOException;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 14.04.2021
 * TIME: 19:34
 */
public class Debugger {
    public static void addChatListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                ModPlayground.broadcastMessage("received chat event.");
                if (!event.isServer()) {
                    return;
                }
                //get player who sent message
                PlayerState sender = GameServer.getServerState().getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender);
                //get transform = worldposition wrapper
                Transform transform = new Transform();
                transform.setIdentity();
                sender.getWordTransform(transform);
                transform.origin.set(sender.getFirstControlledTransformableWOExc().getWorldTransform().origin);

                ModPlayground.broadcastMessage("spawning at: " + transform.origin);

                //create outline = loaded but not yet spawned entity
                SegmentControllerOutline scOutline = null;
                try {
                    scOutline = BluePrintController.active.loadBluePrint(
                            GameServerState.instance,
                            "pirate-drone-01", //catalog entry name
                            "uwuBoy9000", //ship name
                            transform, //transform position
                            -1, //credits to spend
                            10001, //faction ID
                            sender.getCurrentSector(), //sector
                            "uwuBoy8000", //spawner
                            PlayerState.buffer, //buffer (?) no idea what that is, worked fine for me as is
                            null,   //segmentpiece, no idea either
                            true, //active ai -> basically fleet AI ship. attacks enemies.
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
                                sender.getCurrentSector(), //dont know what happens if you put loadsector != spawnsector
                                false, //check block counts (?) no idea
                                new ChildStats(false), //no idea again
                        new SegmentControllerSpawnCallbackDirect(GameServerState.instance, sender.getCurrentSector()) { //idk what that is
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
        },ModMain.instance);
    }

    public static void positionLogger() {
        final Transform playerTransform = new Transform();
        playerTransform.setIdentity();
        final Vector3f pos = new Vector3f();
        new StarRunnable() {
            long lastTime = 0;
            @Override
            public void run() {
                if (!(System.currentTimeMillis() > lastTime + 5000)) {
                    return;
                }
                lastTime = System.currentTimeMillis();

                PlayerState player = GameClient.getClientPlayerState();
                if (player == null) {
                    return;
                }
        //        player.getWordTransform(playerTransform);
                playerTransform.setIdentity();
                player.getWordTransform(playerTransform);
                if (GameClientState.instance.getCurrentPlayerObject() == null) {
                    return;
                }
                playerTransform.origin.set(GameClientState.instance.getCurrentPlayerObject().getWorldTransform().origin);
                pos.set(
                        Math.round(playerTransform.origin.x),
                        Math.round(playerTransform.origin.y),
                        Math.round(playerTransform.origin.z)
                        );
                ModPlayground.broadcastMessage("transform pos: " + pos);
            }
        }.runTimer(ModMain.instance, 50);
    }
}
