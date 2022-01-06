package me.iron.npccontrol.pathing;

import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.LinkedList;

public class AbstractScene {
    private String name; //for soft identification
    private LinkedList<Obstacle> obstacles = new LinkedList<>();

    public AbstractScene(String name) {
        this.name = name;
    }

    public AbstractScene(String name, LinkedList<Obstacle> objects) {
        this.name = name;
        this.obstacles.addAll(objects);
    }

    public void addObjectToScene(Vector3f pos, float radius, String name) {
        obstacles.add(new Obstacle(pos,radius,name));
    }

    public void addObjectsToScene(LinkedList<Obstacle> objects) {
        obstacles.addAll(objects);
    }

    /**
     * adds all loaded objects from the sector to the raycasts obstacles (which the raycast can detect)
     * @param sector
     */
    public void addObjectsFromSector(Vector3i sector, String excludeUID) {
        try {
            for (SimpleTransformableSendableObject obj: GameServerState.instance.getUniverse().getSector(sector).getEntities()) {
                if ((obj instanceof Ship && ((Ship) obj).isDocked()) || obj.getUniqueIdentifier().equals(excludeUID))
                    continue;

                if (!(obj instanceof SimpleTransformableSendableObject)) { //playerstates have a fucked up worldtransform getter.
                    continue;
                }

                //avoid stationary objects (roids and stations)
                if (obj.getSpeedCurrent() > 5) {
                    continue;
                }

                obstacles.add(new Obstacle(obj.getWorldTransform().origin, obj.getBoundingSphereTotal().radius*5, obj.getRealName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public LinkedList<Obstacle> getObstacles() {
        return obstacles;
    }
}
