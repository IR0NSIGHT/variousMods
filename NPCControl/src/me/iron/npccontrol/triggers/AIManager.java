package me.iron.npccontrol.triggers;

import api.utils.StarRunnable;
import me.iron.npccontrol.ModMain;
import org.luaj.vm2.ast.Str;
import org.lwjgl.util.vector.Vector;
import org.lwjgl.util.vector.Vector3f;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.server.ai.program.common.TargetProgram;
import org.schema.game.server.controller.EntityNotFountException;
import org.schema.game.server.data.simulation.groups.SimulationGroup;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.12.2021
 * TIME: 15:50
 */
public class AIManager {
    private static AIManager instance;
    public static AIManager getInstance() {
        return instance;
    }

    public AIManager() {
        instance = this;
        initDebugLoop();
    }



    public static ArrayList<SimulationGroup> groups = new ArrayList<>();

    public static boolean debug;

    public static void initDebugLoop() {
        new StarRunnable(){
            long last = 0;

            @Override
            public void run() {
                if (System.currentTimeMillis() >= last + 2000) {
                    last = System.currentTimeMillis();
                    if (AIManager.getInstance() != null) {
                        AIManager.getInstance().update();
                    }
                }
            }
        }.runTimer(ModMain.instance,10);
    };

    private void update() {
        //Delete groups that dont exist anymore
        //TODO is currentProgram == null a safe way to determine if a simgroup was deleted?
        ArrayList<SimulationGroup> nullProgram = new ArrayList<>();
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).getCurrentProgram() == null || groups.get(i).getMembers().size() == 0)
                nullProgram.add(groups.get(i));
        }
        for (SimulationGroup g: nullProgram) {
            g.deleteMembers(); //just to be safe
            groups.remove(g);
        }

        if (debug) {
            try {
                StringBuilder b = new StringBuilder("AIDEBUG:\n");
                for (SimulationGroup grp : groups) {
                    b.append(debugGroup(grp)).append("\n");
                }
                ModMain.log(b.toString());
            } catch (Exception e) {
                e.printStackTrace();
                ModMain.log(e.toString());
            }

        }
    }

    private String debugGroup(SimulationGroup grp) throws EntityNotFountException, SQLException {
        int members = grp.getMembers().size();
        Vector3i pos = new Vector3i();
        if (members > 0)
            pos = grp.getSector(grp.getMembers().get(0),pos);
        Vector3i target = null;

        if (grp.getCurrentProgram() instanceof TargetProgram) {
            target = ((TargetProgram)grp.getCurrentProgram()).getSectorTarget();
        }
        return String.format("members: %s, pos: %s, target:%s, state: %s",members,pos,target,grp.getCurrentProgram()!=null?grp.getStateCurrent():"PROGRAM NULL");
    }
}
