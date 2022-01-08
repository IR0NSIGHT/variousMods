package me.iron.npccontrol.pathing.sm;

import me.iron.npccontrol.pathing.AbstractScene;
import me.iron.npccontrol.pathing.AbstractSceneObject;
import me.iron.npccontrol.pathing.Pathfinder;
import me.iron.npccontrol.triggers.Utility;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;
import java.util.Vector;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 08.01.2022
 * TIME: 12:59
 */
public class StarPathFinder extends Pathfinder {
    /**
     * instantiats a pathfinder with a scene that constains this sectors and (loaded)objects in its proximity.
     * @param sector
     */
    public StarPathFinder(Vector3i sector) {
        super();
        //generate scene from sector and its surrounding neighbours.
        AbstractScene scene = new AbstractScene("sector"+sector);
        Vector3i sPos = new Vector3i();
        for (Sendable s : GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalObjects().values()) {
            if (s instanceof SimpleTransformableSendableObject) {
                //only use "static" objects.
                if (((SimpleTransformableSendableObject<?>) s).getSpeedCurrent()>5)
                    continue;

                double d = Utility.getDistance(((SimpleTransformableSendableObject<?>) s).getSector(sPos), sector);
                if (d>1.5) //ignore objects that are not in/proximit of sector.
                    continue;

                scene.addObjectToScene(  ((SimpleTransformableSendableObject<?>) s).getWorldTransform().origin,
                        ((SimpleTransformableSendableObject<?>) s).getBoundingSphere().radius,
                        ((SimpleTransformableSendableObject<?>) s).getUniqueIdentifier());
            }
        }

    }
}
