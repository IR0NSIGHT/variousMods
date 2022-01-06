package me.iron.npccontrol.pathing;

import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.Utility;
import org.apache.commons.math3.linear.*;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import org.schema.schine.graphicsengine.forms.debug.DebugLine;
import org.schema.schine.graphicsengine.forms.debug.DebugPacket;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 03.01.2022
 * TIME: 21:49
 * finds a flyable corridor from a to b for a ship
 */
public class Pathfinder {
    public static long debugLineLifeTime = 15*1000;
    private String shipUID;
    private LinkedList<Vector3f[]> raycasts = new LinkedList<>();
    private LinkedList<DebugLine> debugLines = new LinkedList<>();

    //TODO find better and more generic way to filter out specific ships/edit the buindingsphere size of objects
    public Pathfinder(String shipUID) {
        this.shipUID = shipUID;
    }

    /**
     * plot a multi-waypoint path to the target avoiding any obstacles/collisions, with a corridor that has the desired radius.
     * @param ownSector
     * @param ownPos
     * @param targetSector
     * @param targetPos
     * @param corridorRadius radius of safety corridor where no obstacles are.
     */
    public LinkedList<Vector3f> findPath(Vector3i ownSector, Vector3f ownPos, Vector3i targetSector, Vector3f targetPos, float corridorRadius) {
        //TODO star avoidance, long distance plotting, jumping
        //TODO allow filter, take into account neighbour-sector objs that might be big enough to obstruct into this sector
        //TODO take into account that evasive wp might be in different sector
        //TODO assert that evasive wp is not inside another obstacle
        LinkedList<Vector3f> waypoints = new LinkedList<>();
        Obstacle obstacle;
        Vector3f currentWP = new Vector3f(ownPos);
        waypoints.add(currentWP);
        try {
            //plot until target is reached
            while (!currentWP.equals(targetPos) && waypoints.size() < 10) {
                //get obstacle if exists
                Vector3f rayDir = Utility.getDir(ownSector,currentWP,targetSector,targetPos);
                rayDir.normalize();
                AbstractScene scene = new AbstractScene("Sector_"+ownSector.toStringPure());
                scene.addObjectsFromSector(ownSector,shipUID);
                Raycast r = new Raycast(scene);
           //    new DebugLine(
           //            currentWP,targetPos,
           //    )
                r.cast(currentWP, rayDir, corridorRadius);
                obstacle = r.hitObj;
                debugLines.add(r.toDebugLine());
                if (obstacle != null) {
                    //path is blocked
                    Vector3f obstaclePos = new Vector3f(obstacle.pos);
                    Vector3f[] obstaclePlane = getPlaneDirFromNormal(obstaclePos,rayDir);

                    System.out.println("obstacle plane: " + Arrays.toString(obstaclePlane));
                //    assert Math.abs(obstaclePlane[1].dot(obstaclePlane[2]))<0.01f; //plane vectors are orthogonal to eachother
                    assert Math.abs(rayDir.dot(obstaclePlane[1]))<0.99f &&  Math.abs(rayDir.dot(obstaclePlane[2]))<0.99f:"line is parallel to plane.";
                    System.out.println(String.format("raycast: p:%s, d:%s",currentWP, rayDir));

                                        Vector3f closestPointToObj =solveLinePlaneIntersection(currentWP,rayDir,obstaclePlane[0],obstaclePlane[1],obstaclePlane[2]);



                    Vector3f off = new Vector3f(obstaclePos); off.sub(closestPointToObj);
                    float dist = off.length();
                    //find a evasive WP near the obstacle to go around it.
                    Vector3f evasiveWP = this.findPointOnPlaneWithoutObstacle(ownSector,currentWP,closestPointToObj, rayDir, corridorRadius, shipUID);
                    if (evasiveWP == null) {
                        drawRaycasts();
                        throw new NullPointerException("was not able to find a path.");
                    }

                    //get an intermediate waypoint that avoids the obstacle
                    currentWP = new Vector3f(evasiveWP);
                    assert !Float.isNaN(currentWP.x) : "currentWP is NaN:"+currentWP;
                } else {
                    currentWP = new Vector3f(targetPos);
                }

                //assert currentWP.equals(targetPos) || !isPointInObstacle(ownSector,currentWP, corridorRadius):"waypoint inside of obstacle";
                waypoints.add(currentWP);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (Utility.getDistance(waypoints.getLast(),targetPos)>0.01f) {
            ModMain.log("last waypoint is not target pos.");
        }
       // assert Utility.getDistance(waypoints.getLast(),targetPos)>0.01f :"last waypoint is not target pos." ;
        return waypoints;
    }

    /**
     * get a intermediate waypoint that avoids given obstacle at pos, evading in x axis direction.
     * if x axis is in line with dir, use y axis
     * returns the offset from the obstacle position for the new waypoint
     * @param dir path direction that is now blocked by obstacle
     * @param distance distance from obstacle
     * @return offset direction vector, orthogonal to dir
     */
    private Vector3f getEvasiveOffset(Vector3f dir, float distance) {
        Vector3f dirN = new Vector3f(dir); dirN.normalize();
        Vector3f off = new Vector3f(1,0,0);
        if (Math.abs(off.dot(dirN))>=9/10f) { //guarantee the "random" offset is never in line with given direction
            off.set(0,1,0);
        }
        assert Math.abs(off.dot(dirN))<9/10f:"dot on non-inline vector is: "+off.dot(dirN);
        Vector3f out = new Vector3f();
        out.cross(off,dirN); //orthogonal to off and dir => new offset.

        assert out.length()>1/10000f;
        out.normalize();
        out.scale(distance);
        assert out.dot(off)<1/10000f && out.dot(dir)<1/10000f;
        return out;
    }

    /**
     * will raycast on given plane until a non-obstacle point on the plane is found.
     * @param pointPos position from where to raycast
     * @param planePoint any point on the plane
     * @param planeNormal plane normal
     * @param minDist
     * @param ownUID
     * @return
     */
    private Vector3f findPointOnPlaneWithoutObstacle(Vector3i pointSector, Vector3f pointPos, Vector3f planePoint, Vector3f planeNormal, float minDist, String ownUID) throws IOException {
        //while raycast failed
        Vector3f[] plane = getPlaneDirFromNormal(planePoint,planeNormal);

        //rotate pointer by 45° each iteration
        RealMatrix pointer = MatrixUtils.createRealMatrix(new double[][]{{1},{0}});
        double sin45 = 0.7071067d;
        RealMatrix rotMatrix45 = MatrixUtils.createRealMatrix(new double[][]{{sin45,-sin45},{sin45,sin45}});
        for (int i = 0; i < 8; i++) { //8 rotations = 1 full rotiation
            pointer = rotMatrix45.multiply(pointer);

            //get offset vector in plane based on rotated pointer
            Vector3f d1 = new Vector3f(plane[1]), d2 =  new Vector3f(plane[2]);
            d1.scale((float) pointer.getEntry(0,0));
            d2.scale((float) pointer.getEntry(1,0));
            d1.add(d2);
            d1.scale(2*minDist); //d1 is direction from planePoint to evasive point
            Vector3f evasiveP = new Vector3f(d1);
            evasiveP.add(plane[0]); //evasiveP is now the position in the plane offset from the plane center.

            Vector3f newCastDir =new Vector3f(evasiveP); //connection vector between pointPos and evasivePoint
            newCastDir.sub(pointPos);
            newCastDir.normalize();
            //    d1.sub(pointPos); //d1 is now raycast direction from pointPos to new evasive point
            AbstractScene scene = new AbstractScene("evade");
            scene.addObjectsFromSector(pointSector, ownUID);
            Raycast r = new Raycast(scene);
            r.cast(pointPos, newCastDir, minDist);
            Obstacle obj = r.hitObj;
            debugLines.add(r.toDebugLine());
            if (obj == null) {
                //unobstructed path, return point
                return evasiveP;
            }
        }
        return null; //all raycasts around the center have failed, cant find evasive point
    }

    /**
     * predicate that guarantees that the pos is more than obj.boundingsphereradius+corridorradius away from any object in the sector
     * if false is returned.
     * @param sector
     * @param pos
     * @param corridorRadius
     * @return
     */
    public boolean isPointInObstacle(Vector3i sector, Vector3f pos, float corridorRadius) {
        if (!GameServerState.instance.getUniverse().isSectorLoaded(sector))
            return false; //TODO do unloaded DB access for check
        //test the distance to every single object inside the sector, if its to close, return true
        try {
            Sector s = GameServerState.instance.getUniverse().getSector(sector);
            Vector3f objPos = new Vector3f();
            for (SimpleTransformableSendableObject obj: s.getEntities()) {
                objPos.set(obj.getWorldTransform().origin);
                if (Utility.getDistance(objPos,pos)<(obj.getBoundingSphereTotal().radius+corridorRadius)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    public static void main(String args[]) {
        Pathfinder pf = new Pathfinder("");

        Vector3f[] plane = pf.getPlaneDirFromNormal(new Vector3f(3,3,3),new Vector3f(1,0,0));

        Vector3f lp,ld,pp,pd1,pd2;
        Vector3f sol = pf.solveLinePlaneIntersection(
               lp = new Vector3f(0,0,0), //ray
               ld = new Vector3f(1,0,0),

               plane[0],plane[1],plane[2]
                );
        ld.scale(sol.x);
        lp.add(ld);

        Vector3f toPlaneCenter = new Vector3f();
        plane[1].scale(sol.y);
        plane[2].scale(sol.z);
        toPlaneCenter.add(plane[1]);
        toPlaneCenter.add(plane[2]);
        System.out.println("sol:"+sol+" point:"+lp + "vec to planecenter:"+toPlaneCenter);

        //rotate pointer by 45° each iteration
        RealMatrix pointer = MatrixUtils.createRealMatrix(new double[][]{{1},{0}});
        double sin45 = 0.7071067d;
        RealMatrix rotMatrix45 = MatrixUtils.createRealMatrix(new double[][]{{sin45,-sin45},{sin45,sin45}});
        System.out.println("Rotation matrix (45°):"+rotMatrix45.toString());
        for (int i = 0; i < 10; i++) {
            System.out.println("pointer: " + pointer.toString());
            pointer = rotMatrix45.multiply(pointer);

            //get offset vector in plane based on rotated pointer
            Vector3f d1 = new Vector3f(plane[1]), d2 =  new Vector3f(plane[2]);
            d1.scale((float) pointer.getEntry(0,0));
            d2.scale((float) pointer.getEntry(1,0));
            d1.add(d2);
            Vector3f off = d1;
            System.out.println(off);
        }


    }

    /**
     * will solve the intersection equation for lP+x*lD = pP+y*pV1+z*pv2
     * requires that line is not parallel to plane.
     * @param linePoint
     * @param lineDir
     * @param planePoint
     * @param planeVec1
     * @param planeVec2
     * @return (x,y,z) solution
     */
    private Vector3f solveLinePlaneIntersection(Vector3f linePoint, Vector3f lineDir, Vector3f planePoint, Vector3f planeVec1, Vector3f planeVec2) {
        if (Math.abs(planeVec1.dot(planeVec2))<0.001f) {
            System.out.println("plane vectors are not orthogonal to eachother:"+planeVec1+","+planeVec2);
        }
        System.out.println(String.format("lengths lD: %s, pV1: %s, pV2: %s",lineDir.length(),planeVec1.length(),planeVec2.length()) );

        assert lineDir.length()<1.001f&&lineDir.length()>0.999f;
        assert planeVec1.length()<1.001f&&planeVec1.length()>0.999f;
        assert planeVec2.length()<1.001f&&planeVec2.length()>0.999f;

        //assert planeVec1.dot(planeVec2)<0.001f:"plane vectors are not orthogonal to eachother:"+planeVec1+","+planeVec2;
        //matrix with Linear equation system
        double[][]matrixData = {
                {-lineDir.x,planeVec1.x,planeVec2.x},
                {-lineDir.y,planeVec1.y,planeVec2.y},
                {-lineDir.z,planeVec1.z,planeVec2.z}
        };
        RealMatrix coefficients = MatrixUtils.createRealMatrix(matrixData);
        //Vector to solve for
        RealVector constants = new ArrayRealVector(new double[]{
                linePoint.x-planePoint.x,
                linePoint.y-planePoint.y,
                linePoint.z-planePoint.z},false);
        DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
        System.out.println("matrix:" + coefficients.toString());
        System.out.println("constants: " + constants.toString());
        RealVector solution = solver.solve(constants); //throws error "singularmatrix exc"
        Vector3f out = new Vector3f((float)solution.getEntry(0),(float)solution.getEntry(1),(float)solution.getEntry(2));
        return out;
    }

    /**
     * get plane in format point+x*dir1+y*dir2
     * @param point point on plane
     * @param normal normal of the plane
     * @return [point,dir1,dir2]
     */
    private Vector3f[] getPlaneDirFromNormal(Vector3f point, Vector3f normal) {
        normal = new Vector3f(normal);
        normal.normalize();
        Vector3f[] plane = new Vector3f[3];
        plane[0] = point;
        plane[1] = getEvasiveOffset(normal,1); //guaranteed orthogonal to normal -> in plane
        plane[2] = new Vector3f(); plane[2].cross(plane[1],normal); //orthogonal to normal and dir1 -> in plane
        //both directions are orthogonal to normal
        assert plane[1].length()>0.999f && plane[2].length()>0.999f:"plane vectors are not normalized:"+plane[1]+","+plane[2];
        assert Math.abs(normal.dot(plane[1]))<0.001f && Math.abs(normal.dot(plane[2]))<0.001f: "plane vectors are not orthogonal to normal:"+plane[1]+","+plane[2];

        Vector3f p1 = new Vector3f(plane[1]), p2 = new Vector3f(plane[2]);
        p1.scale(500); p2.scale(500);
        p1.add(point); p2.add(point);
        debugLines.add(new DebugLine(
                new Vector3f(point),
                p1,
                new Vector4f(0,0,1,1),
                debugLineLifeTime
        ));
        debugLines.add(new DebugLine(
                new Vector3f(point),
                p2,
                new Vector4f(0,0,1,1),
                debugLineLifeTime
        ));
        return plane;
    }

    public void drawRaycasts() {
        LinkedList<DebugLine> lines = debugLines;
        Vector3f from, to, hit;
        for (Vector3f[] raycast: raycasts) {
            from = raycast[0];
            to = new Vector3f(raycast[0]); raycast[1].normalize(); raycast[1].scale(3000);to.add(raycast[1]);
            hit = raycast[2];
            lines.add(new DebugLine(
                    from,
                    to,
                    (hit!=null?new Vector4f(1,0,0,1):new Vector4f(0,1,0,1)),
                    debugLineLifeTime
            ));
        }
        new DebugPacket(lines).sendToAll();
        debugLines.clear();

    }
}
