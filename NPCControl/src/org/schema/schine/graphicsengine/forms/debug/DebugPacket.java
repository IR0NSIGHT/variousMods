package org.schema.schine.graphicsengine.forms.debug;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
import me.iron.npccontrol.ModMain;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

import java.io.IOException;
import java.util.LinkedList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.01.2022
 * TIME: 00:25
 */
public class DebugPacket extends Packet {
    private LinkedList<DebugLine> lines = new LinkedList<>();
    private LinkedList<DebugSphere> spheres = new LinkedList<>();
    private boolean clear;

    public void setClear(boolean clear) {
        this.clear = clear;
    }

    public DebugPacket() { //defautl empty constructor for packet util
    }

    public DebugPacket(LinkedList<DebugLine> lines) {
        this.lines.addAll(lines);
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
        clear = packetReadBuffer.readBoolean();

        int sphereCount = packetReadBuffer.readInt();
        for (int i = 0; i < sphereCount; i++) {
            DebugSphere s = new DebugSphere(packetReadBuffer);
            spheres.add(s);
        }

       int size = packetReadBuffer.readInt();
       DebugDrawer.clear();
       for (int i = 0; i < size; i++) {
           DebugLine l =new DebugLine(packetReadBuffer);
           lines.add(l);
       }
    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        packetWriteBuffer.writeBoolean(clear);

        packetWriteBuffer.writeInt(spheres.size());
        for (DebugSphere sphere: spheres) {
            sphere.writeToBuffer(packetWriteBuffer);
        }

        packetWriteBuffer.writeInt(lines.size());
        for (DebugLine l: lines) {
            l.writeToBuffer(packetWriteBuffer);
        }
    }

    @Override
    public void processPacketOnClient() {
        if (clear)
            DebugDrawer.myLines.clear();
        synchronized (DebugDrawer.myLines) {
            DebugDrawer.myLines.addAll(lines);
            for (DebugSphere s: spheres) {
                DebugDrawer.myLines.addAll(s.getLines());
            }
        }
    }

    @Override
    public void processPacketOnServer(PlayerState playerState) {

    }

    public void sendToAll() {
        for (PlayerState p: GameServerState.instance.getPlayerStatesByName().values()) {
            PacketUtil.sendPacket(p,this);
        }
    }

    public void addLines(LinkedList<DebugLine> lines) {
        this.lines.addAll(lines);
    }

    public void addSpheres(LinkedList<DebugSphere> spheres) {
        this.spheres.addAll(spheres);
    }
}
