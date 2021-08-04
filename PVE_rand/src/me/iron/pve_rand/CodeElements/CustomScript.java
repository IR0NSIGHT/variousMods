package me.iron.pve_rand.CodeElements;

import me.iron.pve_rand.Managers.ActionController;
import org.hsqldb.Trigger;
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
public class CustomScript extends CustomCode {
    public List<Integer> getCodeBlocks() {
        return codeBlocks;
    }

    public void setCodeBlocks(List<CustomAction> codeBlocks) {
        if (codeBlocks == null)
            return;
        for (CustomAction c: codeBlocks) {
            this.codeBlocks.add(c.getID());
        }
    }

    public void addCodeBlock(CustomAction a) {
        codeBlocks.add(a.getID());
    }

    public void removeCodeBlock(int index) {
        codeBlocks.remove(index);
    }

    List<Integer> codeBlocks = new ArrayList<>();
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
        super(name,description);
        ActionController.addScript(this);
        setCodeBlocks(codeBlocks);
    }

    public void run(Vector3i sector) { //TODO make it possible to pass more parameters
        if (timeout != -1 && System.currentTimeMillis() < lastRan + timeout)
            return;
        if (!isActive())
            return;

        lastRan = System.currentTimeMillis();
        for (Integer a: codeBlocks) {
            ActionController.getAction(a).execute(sector);
        }
    }

    @Override
    public String getChildText() {
        StringBuilder out = new StringBuilder();
        for (Integer id: codeBlocks)
        {
            CustomAction a = ActionController.getAction(id);
            if (a != null)
                out.append(a.toString()); //TODO getOverview
        }
        return out.toString();
    }

    @Override
    public String toString() {
        List<String> out = new ArrayList<>();
        out.add("CustomScript{");
        for (Integer a: codeBlocks) {
            ActionController.getAction(a).addString(out);
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
