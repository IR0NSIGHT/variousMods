package me.iron.npccontrol.pathing;

import me.iron.npccontrol.triggers.Utility;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.01.2022
 * TIME: 17:32
 * very simple raycast functionality
 */
public class Raycast {
    AbstractScene scene;
    Obstacle hitObj;
    Vector3f point;
    Vector3f dir;

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
     * @return hit obstacle with position, boundingsphere radius and name
     */
    public Raycast cast(Vector3f pos, Vector3f dir, float minDist) {

        hitObj = null;
        dir = new Vector3f(dir);
        dir.normalize();

        this.point = new Vector3f(pos);
        this.dir = new Vector3f(dir);

        //TODO ignore if startpos is inside obstacle
        //TODO replace ownUID with a filter object
        //TODO allow constant-speed moving objects
        for (Obstacle obj : scene.getObstacles()) {
            if (isObjectBlockingLine(point, dir, obj, minDist)) {
                if (hitObj != null && Utility.getDistance(point, hitObj.pos) < Utility.getDistance(point, obj.pos))
                    continue; //objects are not sorted by distance, only return the closest one.
                hitObj = obj;
            }
        }
        //tested all objects in sector, none are obstacles, return null
        return this;
    }

    private boolean isObjectBlockingLine(Vector3f point, Vector3f dir, Obstacle obj, float minDist) {
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

        //is the starting pos inside a danger zone
        if (Utility.getDistance(point,objPos)<(minDist+objBBS))
            return false; //just ignore that //TODO find safe way out of danger zone

        //will the path come closer than bounding sphere of ship and obj combined?
        float pointToLine = Utility.distancePointLine(objPos, point, dir);
        return  (pointToLine < (minDist+objBBS));
    }

    public Obstacle getHitObject() {
        return hitObj;
    }

    public DebugLine toDebugLine() {
        Vector3f end = new Vector3f();
        Vector4f color = new Vector4f();
        end.set(dir);
        if (hitObj == null) {
            end.scale(5000);
            color.set(0,1,0,1);
        } else {
            end.scale((float) Utility.getDistance(hitObj.pos,point));
            color.set(1,0,0,1);
        }
        end.add(point);
        return new DebugLine(
            point,
            end,
            color,
                Pathfinder.debugLineLifeTime
        );
    }

}
