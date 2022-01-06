package me.iron.npccontrol.pathing;

import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.triggers.Utility;
import org.lwjgl.Sys;
import org.newdawn.slick.util.pathfinding.PathFinder;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;
import org.schema.schine.graphicsengine.forms.debug.DebugPacket;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.util.LinkedList;
import java.util.Vector;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.01.2022
 * TIME: 17:32
 * very simple raycast functionality
 */
public class Raycast {
    AbstractScene scene;
    LinkedList<AbstractSceneObject> hitObjs = new LinkedList<>();
    AbstractSceneObject hitObj;
    Vector3f point;
    Vector3f dir;

    /**
     * instantiate a raycast. give it a scene, and cast rays to use object detection in that scene.
     * @param scene abstracted scene with obstacles to cast the ray in.
     */
    public Raycast(AbstractScene scene) {
        this.scene = scene;
    }

    /**
     * determine if any entity in this sector is an obstacle on the path of "pos+x*dir".
     * entities are obstacles if their distance to the path is smaller than minDist+their bounding sphere.
     * returns the position and bounding sphere radius of the first obstacle.
     *
     * @param pos     cast ray from here
     * @param dir     in this direction
     * @param minDist minimal distance required to be away from path
     * @param firstHit abort on the first (closest) hit. if false, all hits are registered.
     * @return hit obstacle with position, boundingsphere radius and name
     */
    public void cast(Vector3f pos, Vector3f dir, float minDist, boolean firstHit) {
        hitObjs.clear();
        hitObj = null;
        dir = new Vector3f(dir);
        dir.normalize();

        this.point = new Vector3f(pos);
        this.dir = new Vector3f(dir);

        //TODO ignore if startpos is inside obstacle
        //TODO replace ownUID with a filter object
        //TODO allow constant-speed moving objects
        for (AbstractSceneObject obj : scene.getObstacles()) {
            if (isObjectBlockingLine(point, dir, obj, minDist)) {
                if (firstHit) {
                    if (hitObj != null && Utility.getDistance(point, hitObj.pos) < Utility.getDistance(point, obj.pos))
                        continue; //objects are not sorted by distance, only return the closest one.
                    hitObj = obj;
                } else {
                    hitObjs.add(obj);
                }

            }
        }
        LinkedList<DebugLine> list = new LinkedList<DebugLine>();
        list.add(toDebugLine());

        new DebugPacket(list).sendToAll();

        ModMain.log(String.format("raycast: p=%s d=%s hit %s",pos,dir, (isHit())?hitObjs.getFirst().name:"none"));
        //tested all objects in sector.
    }

    /**
     * tests if the line starting at point is blocked by this object at minimal distance.
     * only detects objects in FRONT of the point.
     * ignores hits where the point is inside of the object.
     * @param point
     * @param dir
     * @param obj
     * @param minDist
     * @return
     */
    private boolean isObjectBlockingLine(Vector3f point, Vector3f dir, AbstractSceneObject obj, float minDist) {
        Vector3f ab,ap;
        //build dot product to determine if "possible collison" point is behind ship already.
        ab = new Vector3f(dir); ab.sub(point); ab.normalize();
        ap = new Vector3f(obj.pos); ap.sub(point); ap.normalize();
        float dot = ap.dot(ab);
        if (dot<0) { //collision point is behind ship.
            return false;
        }

        Vector3f objPos = obj.pos;
        float objBBS = obj.bbsRadius;

        //is the starting pos inside a danger zone => ignore obj
        if (Utility.getDistance(point,objPos)<(minDist+objBBS))
            return false;

        //will the path come closer than bounding sphere of ship and obj combined?
        float pointToLine = Utility.distancePointLine(objPos, point, dir);
       ModMain.log("obj "+obj.name+"to line distance: " + pointToLine + " mindist: " + minDist + " bbs " + objBBS + " collides:" + (pointToLine < (minDist+objBBS)));
        return  (pointToLine < (minDist+objBBS));
    }

    /**
     * get closest/first hit object. only used when "firstHit = true".
     * @return
     */
    public AbstractSceneObject getHitObject() {
        return hitObj;
    }

    /**
     * get all hit objects. list is only non-empty if "firstHit = false".
     * list is NOT sorted.
     * @return
     */
    public LinkedList<AbstractSceneObject> getHitObjects() {
        return hitObjs;
    }

    public DebugLine toDebugLine() {
        Vector3f end = new Vector3f();
        Vector4f color = new Vector4f();
        end.set(dir);
        if (!isHit()) {
            end.scale(5000);
            color.set(0,1,0,1);
        } else {
            Vector3f endP;
            if (hitObj != null) {
                endP = hitObj.pos;
            } else {
                endP  = hitObjs.getFirst().pos;
            }
            end.scale((float) Utility.getDistance(endP,point));
            color.set(1,0,0,1);
            LinkedList<DebugLine> list = new LinkedList<>();
            list.add(new DebugLine(
                    end,
                    endP,
                    new Vector4f(1,0,1,1),
                    Pathfinder.debugLineLifeTime
                    ));
            new DebugPacket(list).sendToAll();
        //    point.set(endP);
        }
        ModMain.log("isHit:"+isHit()+" hitObj:"+hitObj+" hitObjs:"+hitObjs.size());
        end.add(point);
        return new DebugLine(
            point,
            end,
            color,
            Pathfinder.debugLineLifeTime
        );
    }

    public boolean isHit() {
        return hitObj != null || !(hitObjs.isEmpty());
    }

    /**
     * check if point is inside of any objects boundingsphere + minimal distance.
     * will overwrite hitObj and hitObjs.
     * @param point 3d point in scene
     * @param minDist minimal distance to keep to each object to not be considered "inside" (on top of objects boundingsphere)
     * @param firstHit true: abort on first hit (use hitObj for obj then, is faster), false: collect ALL objects blocking the point.
     * @return true if inside, false if not
     */
    public boolean isPointInObject(Vector3f point, float minDist, boolean firstHit) {
        hitObjs.clear();
        hitObj = null;
        for (AbstractSceneObject o: scene.getObstacles()) {
            if (Utility.getDistance(o.pos,point) < o.bbsRadius+minDist)
                if (firstHit) {
                    hitObj = o;
                    return true;
                } else {
                    hitObjs.add(o);
                }
        }
        return !hitObjs.isEmpty();
    }

    public String hitsToString() {
        StringBuilder b = new StringBuilder("hit objects:\n");
        for (AbstractSceneObject o: hitObjs) {
            b.append(String.format("%s, pos=%s, r=%sm",o.name,o.pos,o.bbsRadius)).append("\n");
        }
        return b.toString();
    }
}
