package me.iron.npccontrol;

import api.DebugFile;
import api.ModPlayground;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.SegmentDrawListener;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.network.packets.PacketUtil;
import me.iron.npccontrol.stationReplacement.*;
import me.iron.npccontrol.stationReplacement.commands.CommandCommander;
import me.iron.npccontrol.triggers.AIManager;
import me.iron.npccontrol.triggers.DebugUI;
import me.iron.npccontrol.triggers.Trigger;
import org.junit.Test;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;
import org.schema.schine.graphicsengine.forms.debug.DebugPacket;

import javax.vecmath.Vector3f;


/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.04.2021
 * TIME: 22:36
 */
public class ModMain extends StarMod {
    public static StarMod instance;
    public static Trigger trigger;
    public ModMain() {
        super();
        instance = this;
    }

    @Override
    public void onEnable() {
        super.onEnable();
        StarLoader.registerCommand(new DebugUI());
        PacketUtil.registerPacket(DebugPacket.class);
        ModMain.log("DebugPacket registered");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        StationReplacer.savePersistentAll();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
        DebugFile.log("server created",this);
        new AIManager();
        trigger = new Trigger();
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
        DebugFile.log("on client",this);
        DebugDrawer.lines.add(new DebugLine(new Vector3f(0,0,0),new Vector3f(100,100,100)));
        FastListenerCommon.segmentDrawListeners.add(new SegmentDrawListener() {
            @Override
            public void preDrawSegment(DrawableRemoteSegment drawableRemoteSegment) {

            }

            @Override
            public void postDrawSegment(DrawableRemoteSegment drawableRemoteSegment) {
                DebugDrawer.drawLines();
            }
        });
    }

    public static void log(String mssg) {
        System.out.println(mssg);
        if (GameServerState.instance != null) {
            DebugFile.log(mssg);
            ModPlayground.broadcastMessage(mssg);
        }
    }
}
