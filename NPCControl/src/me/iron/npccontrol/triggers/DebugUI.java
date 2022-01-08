package me.iron.npccontrol.triggers;

import api.mod.StarMod;
import api.network.packets.PacketUtil;
import api.utils.game.chat.CommandInterface;
import com.sun.istack.internal.Nullable;
import me.iron.npccontrol.ModMain;
import me.iron.npccontrol.pathing.AbstractScene;
import me.iron.npccontrol.pathing.AbstractSceneObject;
import me.iron.npccontrol.pathing.Pathfinder;
import me.iron.npccontrol.pathing.sm.StellarPosition;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.graphicsengine.forms.debug.DebugDrawer;
import org.schema.schine.graphicsengine.forms.debug.DebugLine;
import org.schema.schine.graphicsengine.forms.debug.DebugPacket;
import org.schema.schine.graphicsengine.forms.debug.DebugSphere;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.12.2021
 * TIME: 13:04
 */
public class DebugUI implements CommandInterface {
    public static AbstractScene debugScene = new AbstractScene("debugScene");
    public DebugUI() {

    }

    @Override
    public String getCommand() {
        return "debug";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"debug"};
    }

    @Override
    public String getDescription() {
        return "debug for NPC control";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState playerState, String[] strings) {
        if (strings.length > 0 && strings[0].equals("reload")) {
            ModMain.trigger.unregister();
            ModMain.trigger = new Trigger();
            //ModMain.log("new trigger loaded");
            return true;
        }

        if (strings.length > 0 && strings[0].equals("catalog")) {
            //ModMain.log(Utility.catalogToString());
            return true;
        }


        if (strings.length > 1 && strings[0].equals("blueprint")) {
            String UID = strings[1];
            //ModMain.log("getting blueprint for: '"+UID+"'");
            CatalogPermission p = Utility.getBlueprintByName(UID);
            if (p != null) {
                //ModMain.log(p.toString());
            } else {
                //ModMain.log("no match found.");
            }
            return true;
        }

        if (strings.length>0 && strings[0].equals("ai")) {
            AIManager.debug = !AIManager.debug;
            ModMain.log("set AIManager debug to "+AIManager.debug);
        }

        if (strings.length>0 && strings[0].toLowerCase().equals("showai")) {
            Sendable s =GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(playerState.getSelectedEntityId());
            if (s instanceof Ship) {
               AIGameConfiguration aig = ( (Ship)s).getAiConfiguration();
                ModMain.log("AIG = " + aig.toString());
               AiEntityState aes = aig.getAiEntityState();
               ModMain.log("aes = "+aes.toString());
            }  else {
                ModMain.log("not a ship");
            }
        }

        if (strings.length>0 && strings[0].toLowerCase().equals("spawn")) {
            Utility.spawnHuntingGroup(playerState.getFirstControlledTransformableWOExc(),-2,1);
            return true;
        }

        if (strings.length>0 && strings[0].toLowerCase().equals("move")) {
            Sendable s =GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(playerState.getSelectedEntityId());
            if (s instanceof Ship) {
                AIGameConfiguration aig = ( (Ship)s).getAiConfiguration();

                ModMain.log("AIG = " + aig.toString());
                AiEntityState aes = aig.getAiEntityState();
                if (aes instanceof ShipAIEntity) {
                    Vector3f dir = new Vector3f(0,-10000,0);
                    ((ShipAIEntity)aes).moveTo(
                            GameServerState.instance.getController().getTimer(),
                            dir,
                            true
                    );
                    ((Ship)s).getNetworkObject().moveDir.set(dir);
                }
                ModMain.log("told ship " + ((Ship) s).getRealName() +" to move");
            }  else {
                ModMain.log("not a ship");
            }
        }

        if (strings.length>0 && strings[0].equals("pos")) {
            ModMain.log("player at pos: "+playerState.getFirstControlledTransformableWOExc().getWorldTransform().origin);
        }

        if (strings.length>0 && strings[0].equals("path")) {
            DebugDrawer.myLines.clear();

            Sendable sendable = GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get( playerState.getSelectedEntityId());
            String uid = "";
            if (sendable != null && sendable instanceof SimpleTransformableSendableObject)
                uid = ((SimpleTransformableSendableObject<?>) sendable).getUniqueIdentifier();
            generateDebugScene(debugScene);

            return true;
        }

        if (strings.length>0 && strings[0].equals("circle")) {
            Sendable obj = GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get(playerState.getSelectedEntityId());
            if (!(obj instanceof SimpleTransformableSendableObject)) {
                ModMain.log("cant select that.");
            }
            SimpleTransformableSendableObject s = (SimpleTransformableSendableObject)obj;
            Vector3f point = s.getWorldTransform().origin;
            float radius = s.getBoundingSphereTotal().radius;
            DebugSphere sp = new DebugSphere(new StellarPosition(s.getSector(new Vector3i()),point),radius,new Vector4f(0,1,0,1),120*1000);
            DebugPacket p = new DebugPacket();
            LinkedList<DebugSphere> sps = new LinkedList<>();
            sps.add(sp);
            p.addSpheres(sps);
            p.sendToAll();
            new DebugPacket().sendToAll();
            ModMain.log("circeling " + s.getRealName());
            return true;
        }

        if (strings.length>0 && strings[0].equals("circle_all")) {
            float radiusMod = 1;
            if (strings.length==2) {
                try {
                    radiusMod = Integer.parseInt(strings[1]);
                } catch (NumberFormatException ex) {
                    ModMain.log(strings[1]+ " is not a number");
                }
            }
            try {
                Sector s = GameServerState.instance.getUniverse().getSector(playerState.getCurrentSector());
                LinkedList<DebugSphere> spheres = new LinkedList<>();
                for (SimpleTransformableSendableObject obj: s.getEntities()) {
                    Vector3f point = obj.getWorldTransform().origin;
                    float radius = obj.getBoundingSphereTotal().radius * radiusMod;

                    DebugSphere sp = new DebugSphere(new StellarPosition(obj.getSector(new Vector3i()),point),radius,new Vector4f(0,1,0,1),500*1000);
                    spheres.add(sp);
                }
                DebugPacket p = new DebugPacket();
                p.addSpheres(spheres);
                p.sendToAll();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        if (strings.length==1&&strings[0].equals("clear_draw")) {
            DebugDrawer.clearLines();
            ModMain.log("cleared debug drawer lines.");
            return true;
        }
        return false;
    }
    private void generateDebugScene(AbstractScene s) {
        s.getObstacles().clear();
        s.setSector(new Vector3i(136,54,10));
        Vector3f systemOffset = new Vector3f(100,-300,55);
        Vector3f a,b,S;
        a = new Vector3f(5000,0,0);
        b = new Vector3f(0,0,0);
        S = new Vector3f(0,0,0);

        a.add(systemOffset);
        b.add(systemOffset);
        S.add(systemOffset);

        float r = 200f;
    //    s.addObjectToScene(S,r,"station_A");
        Random random = new Random(); //420
        int rangeObstacles = 1700;
        int hollow = 300;
        for (int i = 0; i < 20; i++) {
            float radius = (float) (r*Math.pow(1.2f,random.nextInt(10)));
            S = new Vector3f(
                    random.nextFloat()*(random.nextBoolean()?-1:1),
                    random.nextFloat()*(random.nextBoolean()?-1:1),
                    random.nextFloat()*(random.nextBoolean()?-1:1)
            );
            S.normalize();
            S.scale((hollow+radius+random.nextInt(rangeObstacles)));
            S.add(systemOffset);

            s.addObjectToScene(S,radius,"station_"+i);
        }

        Pathfinder.debugLineLifeTime = 120*1000;
        System.out.println(s.getSceneObjectsName());

        LinkedList<DebugLine> lines = new LinkedList<>();

        for (AbstractSceneObject obj: s.getObstacles()) {
            lines.addAll(new DebugSphere(new StellarPosition(s.getSector(),obj.pos), obj.bbsRadius ,new Vector4f(1,1,1,1),1200*1000).getLines());
        }
        lines.addAll(new DebugSphere(new StellarPosition(s.getSector(),a),10,new Vector4f(1,0,0,1),1200*1000).getLines()); //start
        lines.addAll(new DebugSphere(new StellarPosition(s.getSector(),b),10,new Vector4f(0,1,0,1),1200*1000).getLines()); //end
        //    pf.drawRaycasts();
        //DebugDrawer.myLines.addAll(lines);
        new DebugPacket(lines).sendToAll();
    }

    @Override
    public void serverAction(@Nullable PlayerState playerState, String[] strings) {
        //obsolete leftover
    }

    @Override
    public StarMod getMod() {
        return ModMain.instance;
    }
}
