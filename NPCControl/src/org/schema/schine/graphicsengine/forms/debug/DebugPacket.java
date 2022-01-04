package org.schema.schine.graphicsengine.forms.debug;

import api.network.Packet;
import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import api.network.packets.PacketUtil;
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

    public DebugPacket(LinkedList<DebugLine> lines) {
        this.lines.addAll(lines);
    }

    public DebugPacket() { //defautl empty constructor for packet util
    }

    @Override
    public void readPacketData(PacketReadBuffer packetReadBuffer) throws IOException {
       int size = packetReadBuffer.readInt();
       DebugDrawer.clear();
       for (int i = 0; i < size; i++) {
           DebugLine l =new DebugLine(
                   packetReadBuffer.readVector3f(),
                   packetReadBuffer.readVector3f(),
                   packetReadBuffer.readVector4f()
           );
           l.LIFETIME = packetReadBuffer.readLong();
           lines.add(l);

       }


    }

    @Override
    public void writePacketData(PacketWriteBuffer packetWriteBuffer) throws IOException {
        packetWriteBuffer.writeInt(lines.size());
        for (DebugLine l: lines) {
            packetWriteBuffer.writeVector3f(l.pointA);
            packetWriteBuffer.writeVector3f(l.pointB);
            packetWriteBuffer.writeVector4f(l.color);
            packetWriteBuffer.writeLong(l.LIFETIME);
        }
    }

    @Override
    public void processPacketOnClient() {
     //   DebugDrawer.myLines.clear();
        synchronized (DebugDrawer.myLines) {
            DebugDrawer.myLines.addAll(lines);
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
}
