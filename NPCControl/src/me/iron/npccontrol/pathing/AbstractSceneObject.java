package me.iron.npccontrol.pathing;

import javax.vecmath.Vector3f;

public class AbstractSceneObject {
    public Vector3f pos;
    public float bbsRadius;
    public String name;

    public AbstractSceneObject(Vector3f pos, float bbsRadius, String name) {
        this.pos = pos;
        this.bbsRadius = bbsRadius;
        this.name = name;
    }
}
