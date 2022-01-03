package me.iron.npccontrol.triggers;

import api.mod.StarMod;
import api.utils.game.chat.CommandInterface;
import com.bulletphysics.linearmath.Transform;
import com.sun.istack.internal.Nullable;
import me.iron.npccontrol.ModMain;
import org.schema.game.common.controller.Ship;
import org.schema.game.common.controller.ai.AIGameConfiguration;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.catalog.CatalogPermission;
import org.schema.game.mod.Mod;
import org.schema.game.server.ai.ShipAIEntity;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.ai.stateMachines.AiEntityState;
import org.schema.schine.network.objects.Sendable;

import javax.vecmath.Vector3f;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 30.12.2021
 * TIME: 13:04
 */
public class DebugUI implements CommandInterface {
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
