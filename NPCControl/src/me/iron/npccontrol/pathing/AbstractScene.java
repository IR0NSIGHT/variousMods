package me.iron.npccontrol.pathing;

import me.iron.npccontrol.triggers.Utility;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.common.data.world.VoidSystem;
import org.schema.game.server.data.GameServerState;

import javax.vecmath.Vector3f;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

public class AbstractScene {
    private String name; //for soft identification
    private LinkedList<AbstractSceneObject> abstractSceneObjects = new LinkedList<>();
    private Vector3i sector = new Vector3i(0,0,0);

    public AbstractScene(String name) {
        this.name = name;
    }

    public AbstractScene(String name, LinkedList<AbstractSceneObject> objects) {
        this.name = name;
        this.abstractSceneObjects.addAll(objects);
    }

    public AbstractScene(Vector3i sector) {
        this.name = "Sector"+sector;
        this.sector.set(sector);
    }

    public void addObjectToScene(Vector3f pos, float radius, String name) {
        abstractSceneObjects.add(new AbstractSceneObject(pos,radius,name));
    }

    public void addObjectsToScene(LinkedList<AbstractSceneObject> objects) {
        abstractSceneObjects.addAll(objects);
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

                abstractSceneObjects.add(new AbstractSceneObject(obj.getWorldTransform().origin, obj.getBoundingSphereTotal().radius*5, obj.getRealName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public LinkedList<AbstractSceneObject> getObstacles() {
        return abstractSceneObjects;
    }

    /**
     * formatted string with list of objects in scene+pos+size
     * @return
     */
    public String getSceneObjectsName() {
        StringBuilder b = new StringBuilder("AbstractScene "+name+"\n");
        int i = 0;
        for (AbstractSceneObject o: abstractSceneObjects) {
            b.append(i).append(": ").append(String.format("%s, pos=%s, r=%sm",o.name,o.pos,o.bbsRadius)).append("\n");
            i++;
        }
        return b.toString();
    }

    /**
     * convert starmade sector+insector pos into scene pos.
     * @param sector
     * @param sectorPos
     * @return
     */
    public Vector3f getScenePos(Vector3i sector, Vector3f sectorPos) {
        return Utility.getDir(this.sector,new Vector3f(0,0,0),sector,sectorPos);
    }

    //GETTERS AND SETTERS

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<AbstractSceneObject> getAbstractSceneObjects() {
        return abstractSceneObjects;
    }

    public void setAbstractSceneObjects(LinkedList<AbstractSceneObject> abstractSceneObjects) {
        this.abstractSceneObjects = abstractSceneObjects;
    }

    public Vector3i getSector() {
        return sector;
    }

    public void setSector(Vector3i sector) {
        this.sector = sector;
    }

    public static void main(String[] args) {
        AbstractScene s = new AbstractScene("scene01");
        Vector3f a,b,S;
        a = new Vector3f(5,0,0);
        b = new Vector3f(-5,0,0);
        S = new Vector3f(0,-1,0);
        float r = 2;
        s.addObjectToScene(S,r,"station_A");

        Pathfinder f = new Pathfinder();
        f.setScene(s);
        LinkedList<Vector3f> wps = f.findPath(a,b,0);

        System.out.println(s.getSceneObjectsName());
        System.out.println(Utility.vecsToString(wps));
    }
}
