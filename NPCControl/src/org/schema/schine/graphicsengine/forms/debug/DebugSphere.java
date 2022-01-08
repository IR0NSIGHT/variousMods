package org.schema.schine.graphicsengine.forms.debug;

import api.network.PacketReadBuffer;
import api.network.PacketWriteBuffer;
import me.iron.npccontrol.pathing.sm.StellarPosition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.client.data.GameClientState;
import org.schema.game.server.data.GameServerState;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.01.2022
 * TIME: 12:04
 */
public class DebugSphere extends DebugGeometry {
    private StellarPosition point = new StellarPosition();
    private float radius;
    private LinkedList<DebugLine> lines;
    public DebugSphere() {}
    public DebugSphere(PacketReadBuffer buffer) throws IOException {
        readFromBuffer(buffer);
    }
    public DebugSphere(StellarPosition point, float radius) {
        setPoint(point);
        this.radius = radius;
        this.color = new Vector4f(1,1,1,1);
        generateLines();
    }

    public DebugSphere(StellarPosition point, float radius, Vector4f color, long lifetime) {
        this.color = color;
        setPoint(point);
        this.radius = radius;
        this.LIFETIME = lifetime;
        if (GameServerState.instance != null)
            generateLines();
    }

    private void setPoint(StellarPosition point) {
        this.point.setSector(point.getSector());
        this.point.setPosition(point.getPosition());
    }

    private void generateLines() {
        lines = new LinkedList<>();
        Vector3i playerSector = GameClientState.instance.getPlayer().getCurrentSector();
        Vector3f[] starPlane = new Vector3f[]{new Vector3f(0,0,0),new Vector3f(1,0,0), new Vector3f(0,0,1)};
        Vector3f[] drawPlane = new Vector3f[]{new Vector3f(0,0,0),new Vector3f(0,1,0),new Vector3f()};
        drawPlane[1].scale(radius);
        drawPlane[0].set(point.getPosition());
        Vector3f[] starDir = generateStar(starPlane,16, radius);
        for (int i = 0; i < starDir.length; i++) {
            
            drawPlane[2]=starDir[i];
            Vector3f[] points = generateStar(drawPlane,16, radius);
            drawCircle(points, drawPlane[0]);
        }



    //   for (float i = -1; i <= 1; i+= 0.5f) {
    //       xyPlane[1].x  = 1;
    //       xyPlane[1].z = i;
    //       xyPlane[1].normalize();
    //       drawCircle(xyPlane);
    //   }
    //   drawCircle(xzPlane);
    //   drawCircle(yzPlane);
    }

    public static void main(String[] args) {
        DebugSphere s = new DebugSphere();
        Vector3f[] dirs = s.generateStar(
                new Vector3f[]{new Vector3f(0,0,0),new Vector3f(1,0,0), new Vector3f(0,0,1)},
                16,
                1
                );
        System.out.println(Arrays.toString(dirs));

    }

    public LinkedList<DebugLine> getLines() {
        return lines;
    }

    /**
     * expects normalized plane vectors that are orthogonal to eachother. adds debuglines that outline a circle around point with given radius on this plane.
     * @param points points on circle to connect, gotta be in the right order.
     */
    private void drawCircle(Vector3f[] points, Vector3f center) {
        for (Vector3f p: points) {
            p.add(center);
        }
        //connect the dots
        for (int i=0; i < points.length; i++) {
            lines.add(new DebugLine(
                    new StellarPosition(this.point.getSector(),points[i]),
                    new StellarPosition(this.point.getSector(),points[(i+1)%points.length]),
                    new Vector4f(color),getLifeTime()
            ));
        }
    }

    /** directions from 0 0 0 to point
     *
     * @param plane with point, dir1, dir2
     * @param pointAmount
     * @return
     */
    private Vector3f[] generateStar(Vector3f[] plane, int pointAmount, float radius) {
        //rotate pointer by 45° each iteration
        RealMatrix pointer = MatrixUtils.createRealMatrix(new double[][]{{1},{0}});
        double rads = Math.toRadians(360f/pointAmount);
        double sinAlpha = Math.sin(rads);// 0.7071067d;
        double cosAlpha = Math.cos(rads);
        RealMatrix rotMatrix45 = MatrixUtils.createRealMatrix(new double[][]{
                {cosAlpha,-sinAlpha},
                {sinAlpha,cosAlpha}});
        System.out.println(rotMatrix45);
        Vector3f[] points = new Vector3f[pointAmount]; //vectors from center pointing outwards
        Vector3f x = new Vector3f(),y = new Vector3f(), point;
        //make x dots
        for (int i = 0; i < pointAmount; i++) {
            x.set(plane[1]);
            y.set(plane[2]);
            x.scale((float) pointer.getEntry(0,0));
            y.scale((float) pointer.getEntry(1,0));
            point = new Vector3f();
            point.add(x);point.add(y);
            point.scale(radius);
        //    point.add(plane[0]);
            points[i] = point;
            pointer = rotMatrix45.multiply(pointer);
        }
        return points;
    }

    public void draw() {
        for (DebugLine l: lines) {
            draw();
        }
    }

    /**
     * write info to the buffer
     * @param buffer
     * @throws IOException
     */
    public void writeToBuffer(PacketWriteBuffer buffer) throws IOException {
        buffer.writeVector(point.getSector());
        buffer.writeVector3f(point.getPosition());
        buffer.writeFloat(radius);
        buffer.writeVector4f(color);
        buffer.writeLong(LIFETIME);
    }

    /**
     * will create spehere and generate lines (if on client) from buffer.
     * @param buffer
     * @throws IOException
     */
    public void readFromBuffer(PacketReadBuffer buffer) throws IOException {
        this.point.setSector(buffer.readVector());
        this.point.setPosition(buffer.readVector3f());
        this.radius = buffer.readFloat();
        this.color = buffer.readVector4f();
        this.LIFETIME = buffer.readLong();
        if (GameClientState.instance != null)
            generateLines();
    }
}
