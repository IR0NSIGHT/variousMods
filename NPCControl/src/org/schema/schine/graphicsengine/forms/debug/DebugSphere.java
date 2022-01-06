package org.schema.schine.graphicsengine.forms.debug;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.schema.common.util.linAlg.Vector3i;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.LinkedList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 05.01.2022
 * TIME: 12:04
 */
public class DebugSphere extends DebugGeometry {
    private Vector3f point;
    private float radius;
    private LinkedList<DebugLine> lines = new LinkedList<>();
    public DebugSphere(Vector3f point, float radius) {
        this.point = point;
        this.radius = radius;
        this.color = new Vector4f(1,1,1,1);
        generateLines();
    }

    public DebugSphere(Vector3f point, float radius, Vector4f color, long lifetime) {
        this.color = color;
        this.point = point;
        this.radius = radius;
        this.LIFETIME = lifetime;
        generateLines();
    }

    private void generateLines() {

        Vector3f[] xyPlane = new Vector3f[]{point, new Vector3f(1,0,0),new Vector3f(0,1,0)};
        Vector3f[] xzPlane = new Vector3f[]{point, new Vector3f(1,0,0),new Vector3f(0,0,1)};
        Vector3f[] yzPlane = new Vector3f[]{point, new Vector3f(0,1,0),new Vector3f(0,0,1)};
        generateCircle(xyPlane);
        generateCircle(xzPlane);
        generateCircle(yzPlane);

    }

    public LinkedList<DebugLine> getLines() {
        return lines;
    }

    /**
     * expects normalized plane vectors that are orthogonal to eachother. adds debuglines that outline a circle around point with given radius on this plane.
     * @param plane
     */
    private void generateCircle(Vector3f[] plane) {
        //rotate pointer by 45° each iteration
        RealMatrix pointer = MatrixUtils.createRealMatrix(new double[][]{{1},{0}});
        double sin45 = 0.7071067d;
        RealMatrix rotMatrix45 = MatrixUtils.createRealMatrix(new double[][]{{sin45,-sin45},{sin45,sin45}});
        Vector3f[] points = new Vector3f[8]; //vectors from center pointing outwards
        Vector3f x = new Vector3f(),y = new Vector3f(), point;
        //make 8 dots
        for (int i = 0; i < 8; i++) {
            x.set(plane[1]);
            y.set(plane[2]);
            x.scale((float) pointer.getEntry(0,0));
            y.scale((float) pointer.getEntry(1,0));
            point = new Vector3f();
            point.add(x);point.add(y);
            point.scale(radius);
            point.add(plane[0]);
            points[i] = point;
            pointer = rotMatrix45.multiply(pointer);
        }
        //connect the dots
        for (int i = 0; i < 8; i++) {
            lines.add(new DebugLine(
                points[i],points[(i+1)%8],new Vector4f(color),getLifeTime()
            ));
        }
    }

    public void draw() {
        for (DebugLine l: lines) {
            draw();
        }
    }


}
