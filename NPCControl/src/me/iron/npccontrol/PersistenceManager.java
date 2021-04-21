package me.iron.npccontrol;

import api.DebugFile;
import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import api.mod.config.PersistentObjectUtil;
import org.luaj.vm2.ast.Str;
import org.schema.game.mod.Mod;
import org.schema.game.server.data.GameServerState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 17.04.2021
 * TIME: 18:51
 */
public class PersistenceManager {
    public static void initChatListener() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                if (!event.isServer() || !GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender).isAdmin()) {
                    return; //not admin, dirty peasants dont get to say shit here!
                }
                if (event.getMessage().text.contains("!station save persist")) {
                    SaveStrains();
                }

                if (event.getMessage().text.contains("!station load persist")) {
                    LoadStrains();
                }
            }
        }, ModMain.instance);
    }

    public static void SaveStrains() {
        StrainCollectionContainer container;

        //get reference to stored container, create new one if not existent.
        ArrayList<Object> list = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),StrainCollectionContainer.class);
        if (list.size() == 0) {
            //init container
            container = new StrainCollectionContainer();
            PersistentObjectUtil.addObject(ModMain.instance.getSkeleton(),container);
            DebugFile.log("new container was created, bc none existed",ModMain.instance);
        } else {
            container = (StrainCollectionContainer) list.get(0);
        }
        if (list.size() > 1) {
            DebugFile.log("!!MORE THAN ONE CONTAINER FOR STRAINS HAS BEEN FOUND IN POU!!",ModMain.instance);
        }

        //add new data to container
        Object[] arr =  NPCStationReplacer.getActiveStrains().toArray();
        container.strains = Arrays.copyOf(arr, arr.length, EvolutionStrain[].class);

        //save
        PersistentObjectUtil.save(ModMain.instance.getSkeleton());
        DebugFile.log("Persistence Manager saved active strains",ModMain.instance);
    }

    public static void LoadStrains() {
        ArrayList<Object> list = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),StrainCollectionContainer.class);
        StrainCollectionContainer strainCollection;
        if (list.size() == 0) {
            DebugFile.log("no saved data for station replacement (strains missing).", ModMain.instance);
        } else {
            strainCollection = (StrainCollectionContainer) list.get(0);
            EvolutionStrain[] strains = strainCollection.strains;
            NPCStationReplacer.setActiveStrains(strainCollection.strains);
        }
    }
}
