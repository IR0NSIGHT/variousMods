package me.iron.npccontrol.pathing;

import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.Utility;
import org.apache.commons.math3.linear.*;
import org.lwjgl.Sys;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

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
     * @param targetSector
     * @param targetPos
     * @param corridorRadius radius of safety corridor where no obstacles are.
     */
    public LinkedList<Vector3f> findPath(SimpleTransformableSendableObject ship, Vector3i targetSector, Vector3f targetPos, float corridorRadius) {
        return findPath(ship.getSector(new Vector3i()), ship.getWorldTransform().origin, targetSector,targetPos, corridorRadius, ship.getUniqueIdentifier());
    }

    public LinkedList<Vector3f> findPath(Vector3i sectorStart, Vector3f startPos, Vector3i sectorEnd, Vector3f end, float corridorRadius, String excludeUID) {
        AbstractScene scene = new AbstractScene(sectorStart);
        scene.addObjectsFromSector(scene.getSector(), excludeUID);
        Vector3f scenePosA = scene.getScenePos(sectorStart,startPos);
        Vector3f scenePosB = scene.getScenePos(sectorEnd, end);

        return findPath(scene,scenePosA,scenePosB, corridorRadius); //positions in scene, revert back to sector+sectorpos
        //TODO star avoidance, long distance plotting, jumping
        //TODO allow filter, take into account neighbour-sector objs that might be big enough to obstruct into this sector
        //TODO take into account that evasive wp might be in different sector
        //TODO assert that evasive wp is not inside another obstacle
    }

    public LinkedList<Vector3f> findPath(AbstractScene scene, Vector3f start, Vector3f end, float corridorRadius) {
        //System.out.println("find path from "+start + " to " +end);
        LinkedList<Vector3f> waypoints = new LinkedList<>();
        AbstractSceneObject hitObj;
        Vector3f currentWP = new Vector3f(start);
        waypoints.add(currentWP);

        //plot until target is reached
        while (!currentWP.equals(end) && waypoints.size() < 100) {
            //get obstacle if exists
            Vector3f rayDir = Utility.getDir(currentWP,end);
            rayDir.normalize();
            System.out.println("raycast from current waypoint towards target.");
            Raycast r = new Raycast(scene);
            r.cast(currentWP, rayDir, corridorRadius, false);

            debugLines.add(r.toDebugLine());
            if (r.isHit()) {
                hitObj = r.hitObjs.getFirst();
                System.out.println("raycast hit obj " + hitObj.name + ", try pathing around.");
                //path is blocked
                Vector3f obstaclePos = new Vector3f(hitObj.pos);
                Vector3f planeNormal = new Vector3f(rayDir); planeNormal.negate();
                Vector3f[] obstaclePlane = getPlaneDirFromNormal(obstaclePos,planeNormal);

                assert Math.abs(obstaclePlane[1].dot(obstaclePlane[2]))<0.01f; //plane vectors are orthogonal to eachother
                assert Math.abs(rayDir.dot(obstaclePlane[1]))<0.99f &&  Math.abs(rayDir.dot(obstaclePlane[2]))<0.99f:"line is parallel to plane.";

                //find where the casted ray interects the plane defined by object and ray direction.
                Vector3f solution = solveLinePlaneIntersection(currentWP,rayDir,obstaclePlane[0],obstaclePlane[1],obstaclePlane[2]);
                Vector3f closestPointToObj = new Vector3f(rayDir); closestPointToObj.scale(solution.x); closestPointToObj.add(currentWP);
                Vector3f off = new Vector3f(obstaclePos); off.sub(closestPointToObj);

                //find a evasive WP near the obstacle to go around it.
                Vector3f evasiveWP = this.findPointOnPlaneWithoutObstacle(scene,currentWP,closestPointToObj, planeNormal,hitObj.bbsRadius,corridorRadius);
                if (evasiveWP == null) {
                    drawRaycasts();
                    throw new NullPointerException("was not able to find a path.");
                }

                //get an intermediate waypoint that avoids the obstacle
                ModMain.log(" next waypoint: "+evasiveWP);
                currentWP = new Vector3f(evasiveWP);
                assert !Float.isNaN(currentWP.x) : "currentWP is NaN:"+currentWP;
            } else {
                currentWP = new Vector3f(end);
            }
            assert !r.isPointInObject(currentWP,corridorRadius,true);
        //    assert currentWP.equals(end) || !isPointInObstacle(s,currentWP, corridorRadius):"waypoint inside of obstacle";
            waypoints.add(currentWP);

        }

        assert waypoints.size()>=2; //start and end at least.
        assert Utility.getDistance(waypoints.getLast(),end)>0.01f :"last waypoint is not target pos." ;
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
     * @param corridorRadius
     * @return
     */
    private Vector3f findPointOnPlaneWithoutObstacle(AbstractScene scene, Vector3f pointPos, Vector3f planePoint, Vector3f planeNormal, float obstacleSize, float corridorRadius) {
        assert planeNormal.length()>0.999f && planeNormal.length()<1.001f; //is it normalized?
        //while raycast failed
        Vector3f[] plane = getPlaneDirFromNormal(planePoint,planeNormal);
        System.out.println("find evade point on plane: "+Arrays.toString(plane));
        //rotate pointer by 45° each iteration
        RealMatrix pointer = MatrixUtils.createRealMatrix(new double[][]{{1},{0}});
        double sin45 = 0.7071067d;
        RealMatrix rotMatrix45 = MatrixUtils.createRealMatrix(new double[][]{{sin45,-sin45},{sin45,sin45}});
        for (int i = 0; i < 8; i++) { //8 rotations = 1 full rotiation
            //get offset vector in plane based on rotated pointer
            Vector3f d1 = new Vector3f(plane[1]);
            Vector3f d2 =  new Vector3f(plane[2]);

            d1.scale((float) pointer.getEntry(0,0));
            d2.scale((float) pointer.getEntry(1,0));
            d1.add(d2);
            d1.normalize();
            d1.scale(obstacleSize*2); //d1 is direction from planePoint to evasive point
            //here d1 is the star building vector inside the plane.

            //offset towards raypoint by radius (cheap tangent alternativ) to ensure not hitting the same obj again.
           // d2.set(planeNormal);d2.scale(obstacleSize); d1.add(d2);
            d1.add(plane[0]);

            Vector3f evasivePoint = new Vector3f(d1); //evasiveP is now the position in the plane offset from the plane center.
            //System.out.println("evasive point: " +evasivePoint);
            Vector3f newCastDir = Utility.getDir(pointPos, evasivePoint); //connection vector between pointPos and evasivePoint

            Raycast r = new Raycast(scene);
            ModMain.log("raycast to evade object with dir: " + newCastDir);
            r.cast(pointPos, newCastDir, corridorRadius, false);
            debugLines.add(r.toDebugLine());
            if (!r.isHit()) {
                //unobstructed path, return point
                ModMain.log("no hit, returning evasive point: " +evasivePoint);
                return evasivePoint;
            }
            pointer = rotMatrix45.multiply(pointer);

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
        //System.out.println("sol:"+sol+" point:"+lp + "vec to planecenter:"+toPlaneCenter);

        //rotate pointer by 45° each iteration
        RealMatrix pointer = MatrixUtils.createRealMatrix(new double[][]{{1},{0}});
        double sin45 = 0.7071067d;
        RealMatrix rotMatrix45 = MatrixUtils.createRealMatrix(new double[][]{{sin45,-sin45},{sin45,sin45}});
        //System.out.println("Rotation matrix (45°):"+rotMatrix45.toString());
        for (int i = 0; i < 10; i++) {
            //System.out.println("pointer: " + pointer.toString());
            pointer = rotMatrix45.multiply(pointer);

            //get offset vector in plane based on rotated pointer
            Vector3f d1 = new Vector3f(plane[1]), d2 =  new Vector3f(plane[2]);
            d1.scale((float) pointer.getEntry(0,0));
            d2.scale((float) pointer.getEntry(1,0));
            d1.add(d2);
            Vector3f off = d1;
            //System.out.println(off);
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
        assert Math.abs(planeVec1.dot(planeVec2))>0.001f:"plane vectors are not orthogonal to eachother:"+planeVec1+","+planeVec2;
        ////System.out.println(String.format("lengths lD: %s, pV1: %s, pV2: %s",lineDir.length(),planeVec1.length(),planeVec2.length()) );

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
        ////System.out.println("matrix:" + coefficients.toString());
        ////System.out.println("constants: " + constants.toString());
        RealVector solution = solver.solve(constants); //throws error "singularmatrix exc"
        ////System.out.println("solution: " + solution);
        return new Vector3f((float)solution.getEntry(0),(float)solution.getEntry(1),(float)solution.getEntry(2));
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
        if (GameServerState.instance==null)
            return;
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
