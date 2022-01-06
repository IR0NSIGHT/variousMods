package me.iron.npccontrol.pathing;

import javax.vecmath.Vector3f;

class Obstacle {
    Vector3f pos;
    float bbsRadius;
    String name;

    public Obstacle(Vector3f pos, float bbsRadius, String name) {
        this.pos = pos;
        this.bbsRadius = bbsRadius;
        this.name = name;
    }
}
