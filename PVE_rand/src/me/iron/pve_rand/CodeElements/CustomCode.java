package me.iron.pve_rand.CodeElements;

import org.schema.game.common.controller.observer.DrawerObservable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 04.08.2021
 * TIME: 16:45
 */
public class CustomCode extends DrawerObservable implements Serializable {
    protected final HashMap<String,Object> params = new HashMap<String,Object>(){
        @Override
        public Object put(String key, Object value) {
            if (this.get(key) != null && (value.getClass() != this.get(key).getClass())) {
                return null;
            }
            return super.put(key, value);
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            for (Map.Entry<String,Object> entry: this.entrySet()) {
                out.append(entry.getKey()).append(": ");
                out.append("(").append(entry.getValue().getClass().getSimpleName()).append(")");
                out.append(entry.getValue().toString());
            }
            return out.toString();
        }
    };
    //TODO safe way to set and get the wanted types
    //TODO default value map or similar

    private static int idCounter = 0;
    private String name;
    private String description;
    private boolean active = true;
    private final int ID;

    public CustomCode(String name, String description) {
        params.put("name",name);
        params.put("description",description);
        params.put("active",active);
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

    public void setDescription(String description) {
        this.description = description;
        notifyObservers(this);
    }

    public void setActive(boolean active) {
        this.active = active;
        notifyObservers(this);
    }

    public static void setIdCounter(int next) {
        idCounter = next;
    }

    public String getDescription() {
        return description;
    }

    public HashMap<String,Object> getParams() {
        return params;
    }

    public String getOverview() {
        return "";
    }

    public String getChildText() {
        return "no childs";
    }

    public boolean isActive() {
        return active;
    }

    public int getID() {
        return ID;
    }

    public static int nextID() {
        return idCounter;
    }

}
