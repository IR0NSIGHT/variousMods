package me.iron.npccontrol.triggers;

import api.listener.fastevents.FastListenerCommon;
import api.listener.fastevents.SegmentDrawListener;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.network.packets.PacketUtil;
import api.utils.game.chat.CommandInterface;
import com.bulletphysics.linearmath.Transform;
import com.sun.istack.internal.Nullable;
import me.iron.npccontrol.ModMain;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.common.data.world.DrawableRemoteSegment;
import org.schema.game.common.data.world.Sector;
import org.schema.game.common.data.world.SimpleTransformableSendableObject;
import org.schema.game.mod.Mod;
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

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.12.2021
 * TIME: 13:04
 */
public class DebugUI implements CommandInterface {
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

            Sendable s = GameServerState.instance.getLocalAndRemoteObjectContainer().getLocalUpdatableObjects().get( playerState.getSelectedEntityId());
            String uid = "";
            if (s != null && s instanceof SimpleTransformableSendableObject)
                uid = ((SimpleTransformableSendableObject<?>) s).getUniqueIdentifier();

            Pathfinder pf = new Pathfinder(uid);
            LinkedList<Vector3f> wps = pf.findPath(playerState.getCurrentSector(),new Vector3f(2000,1000,1000),playerState.getCurrentSector(),new Vector3f(-2000,1000,1000),200f);
            Iterator<Vector3f> it = wps.iterator();
            Vector3f previous = null;
            LinkedList<DebugLine> lines = new LinkedList<>();
            while (it.hasNext()) {
                Vector3f wp = it.next();
                if (previous != null) {
                    lines.add(new DebugLine(previous,wp,new Vector4f(1,0,0,1),60*1000));
                }
                ModMain.log("wP:" + wp);

                previous = wp;
            }
        //    pf.drawRaycasts();
            PacketUtil.sendPacket(playerState,new DebugPacket(lines));
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
            DebugSphere sp = new DebugSphere(point,radius,new Vector4f(0,1,0,1),120*1000);
            new DebugPacket(sp.getLines()).sendToAll();
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
                LinkedList<DebugLine> lines = new LinkedList<>();
                for (SimpleTransformableSendableObject obj: s.getEntities()) {
                    Vector3f point = obj.getWorldTransform().origin;
                    float radius = obj.getBoundingSphereTotal().radius * radiusMod;
                    DebugSphere sp = new DebugSphere(point,radius,new Vector4f(0,1,0,1),120*1000);
                    lines.addAll(sp.getLines());
                }
                new DebugPacket(lines).sendToAll();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (strings.length==1&&strings[0].equals("clear_draw")) {
            DebugDrawer.clearLines();
            ModMain.log("cleared debug drawer lines.");
            return true;
        }
        return false;
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
