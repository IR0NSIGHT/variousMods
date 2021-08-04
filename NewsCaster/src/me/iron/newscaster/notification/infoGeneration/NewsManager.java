package me.iron.newscaster.notification.infoGeneration;

import api.DebugFile;
import api.ModPlayground;
import api.mod.config.PersistentObjectUtil;
import api.utils.StarRunnable;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.configManager;
import me.iron.newscaster.notification.broadcasting.Broadcaster;
import me.iron.newscaster.notification.infoGeneration.infoTypes.EntityInfo;
import me.iron.newscaster.notification.infoGeneration.infoTypes.GenericInfo;
import me.iron.newscaster.notification.infoGeneration.infoTypes.ShipDestroyedInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 22.02.2021
 * TIME: 16:56
 */
public class NewsManager {
    private static List<GenericInfo> newsStorage = new ArrayList();
    //config set values
    private static boolean info_save_persistent = false;
    private static int info_threshold = 10;
    private static int info_save_timer = 1; //in minutes
    private static Class[] newsTypes = new Class[]{EntityInfo.class,ShipDestroyedInfo.class};
    private static boolean saveTimerOn = false;
    /**
     * add a news-infoObject to the news storage
     * @param info info object
     * @return index in list.
     */
    public static int addInfo(GenericInfo info) {
        //DebugFile.log("addInfo: " + info.getNewscast());
        if (newsStorage.size() >= info_threshold) {
            for(int i = 0; i < newsStorage.size() - info_threshold; i++) {
                newsStorage.remove(0);
            }
        }
        newsStorage.add(info);
        Broadcaster.queueAdd(info);
        return newsStorage.size(); //add to end of list => size = index
    }

    public static List<GenericInfo> getNewsStorage() {
        return newsStorage;
    }

    public static void updateFromConfig() {
        info_threshold              = configManager.getValue("info_threshold");
        info_save_timer             = configManager.getValue("info_save_timer");
        info_save_persistent        = configManager.getValue("info_save_persistent")==1;
    }

    public static void saveToPersistentUtil() {
        if (!info_save_persistent) return;
    //    ModPlayground.broadcastMessage("saving persistent for newscaster");
        DebugFile.log("saving newsStorage to persistentObjectUtil");
        NewsContainer.getSaveObject().setNews(newsStorage);
        PersistentObjectUtil.save(ModMain.instance.getSkeleton());
    }

    public static void loadFromPersistentUtil() {
        //fired at onServerCreated
        createSaveTimer();
        newsStorage.clear();
        newsStorage.addAll(NewsContainer.getSaveObject().getNews());
        DebugFile.log("loading newsStorage from persistent obj Util: "+newsStorage.size()+" events.");
    }

    private static void createSaveTimer() {
        if (saveTimerOn) {
            return;
        }
        saveTimerOn = true;

        new StarRunnable() {
            long last = 0;
            @Override
            public void run() {
                if (last + info_save_timer*1000*60 > System.currentTimeMillis()) {
                    return;
                }
                last = System.currentTimeMillis();
                saveToPersistentUtil();
            }
        }.runTimer(ModMain.instance,10);
    }

    /** deletes all loaded infos
     *
     */
    public static void cleanLoadedInfo() {
        Broadcaster.flushQueue();
        newsStorage.clear();
    }

    /**
     * deletes the savefiles. use with caution. doesnt touch runtime loaded infos.
     */
    public static void cleanPersistentInfo() {
        //this is legacy code for pre 1.1 versions that brute forced their objects.
        for (Class cl: newsTypes) {
            ArrayList<Object> list = PersistentObjectUtil.getCopyOfObjects(ModMain.instance.getSkeleton(),cl);
            for (Object obj: list) {
                newsStorage.remove(obj);
                PersistentObjectUtil.removeObject(ModMain.instance.getSkeleton(),obj);
            }
        }
        //new way
        PersistentObjectUtil.removeObject(ModMain.instance.getSkeleton(), NewsContainer.getSaveObject());
        PersistentObjectUtil.save(ModMain.instance.getSkeleton());
    }
}
