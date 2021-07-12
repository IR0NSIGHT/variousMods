package me.iron.pve_rand.Action;

import org.schema.common.util.linAlg.Vector3i;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.06.2021
 * TIME: 17:07
 * represents a program being run by the trigger.
 * Holds list of codeblocks, which is executes one after the other.
 */
public class CustomScript implements Serializable {
    public List<CustomAction> getCodeBlocks() {
        return codeBlocks;
    }

    public void setCodeBlocks(List<CustomAction> codeBlocks) {
        this.codeBlocks = codeBlocks;
    }

    public void addCodeBlock(CustomAction a) {
        codeBlocks.add(a);
    }

    public void removeCodeBlock(int index) {
        codeBlocks.remove(index);
    }

    List<CustomAction> codeBlocks = new ArrayList<>();
    String name;
    String description;
    private long lastRan;
    private int timeout = -1;

    /**
     * sets how long the script will not run after being executed
     * @param timeout timeout in seconds
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout * 1000;
    }

    public CustomScript(List<CustomAction> codeBlocks, String name, String description) {
        if (codeBlocks != null) {
            this.codeBlocks = codeBlocks;
        }
        this.name = name;
        this.description = description;
    }

    public void run(Vector3i sector) { //TODO make it possible to pass more parameters
        if (timeout != -1 && System.currentTimeMillis() < lastRan + timeout) return;
        lastRan = System.currentTimeMillis();

        for (CustomAction a: codeBlocks) {
            a.execute(sector);
        }
    }

    @Override
    public String toString() {
        List<String> out = new ArrayList<>();
        out.add("CustomScript{");
        for (CustomAction a: codeBlocks) {
            a.addString(out);
        }
        out.add("}");

        StringBuilder code = new StringBuilder();
        for(int i = 0; i < out.size(); i++) {
            String line = out.get(i);
            line = line.replace("\t","   ");
            code.append(i).append("   ").append(line).append("\n");
        }
        return code.toString();
    }
}
