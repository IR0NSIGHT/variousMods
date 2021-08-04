package me.iron.pve_rand.CodeElements;

import org.schema.game.common.controller.observer.DrawerObservable;

import java.io.Serializable;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.08.2021
 * TIME: 16:45
 */
public class CustomCode extends DrawerObservable implements Serializable {
    private static int idCounter = 0;
    private String name;
    private String description;
    private boolean active = true;
    private final int ID;

    public CustomCode(String name, String description) {
        this.name = name;
        this.description = description;
        this.ID = idCounter++;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyObservers(this);
    }

    public String getDescription() {
        return description;
    }

    public String getOverview() {
        return "";
    }

    public String getChildText() {
        return "no childs";
    }

    public void setDescription(String description) {
        this.description = description;
        notifyObservers(this);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        notifyObservers(this);
    }

    public int getID() {
        return ID;
    }

    public static int nextID() {
        return idCounter;
    }
    public static void setIdCounter(int next) {
        idCounter = next;
    }
}
