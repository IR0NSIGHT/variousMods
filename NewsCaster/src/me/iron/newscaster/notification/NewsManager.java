package me.iron.newscaster.notification;

import api.DebugFile;
import api.mod.config.PersistentObjectUtil;
import api.utils.StarRunnable;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.notification.infoTypes.EntityInfo;
import me.iron.newscaster.notification.infoTypes.GenericInfo;
import me.iron.newscaster.notification.infoTypes.ShipDestroyedInfo;

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
    private static Class[] newsTypes = new Class[]{EntityInfo.class,ShipDestroyedInfo.class};
    private static boolean saveTimerOn = false;
    /**
     * add a news-infoObject to the news storage
     * @param info info object
     * @return index in list.
     */
    public static int addInfo(GenericInfo info) {
        //DebugFile.log("addInfo: " + info.getNewscast());
        if (newsStorage.size() >= 50) {
            for(int i = 0; i < newsStorage.size() - 50; i++) {
                newsStorage.remove(0);
            }
        }
        newsStorage.add(info);
        Broadcaster.queueAdd(info);
        return newsStorage.size(); //add to end of list => size = index
    }

    /**
     * returns info at index. use -1 for last entry.
     * @param index
     * @return
     */
    public static GenericInfo getInfo(int index) {
        if (index == -1) {
            if (newsStorage.isEmpty()) {
                return null;
            }
            index = newsStorage.size()-1;
        }
        try {
            return newsStorage.get(index);
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<GenericInfo> getNewsStorage() {
        return newsStorage;
    }

    public static void saveToPersistenUtil() {
        DebugFile.log("saving newsStorage to persistenObjectUtil");
        for (GenericInfo info: newsStorage) {
            PersistentObjectUtil.addObject(ModMain.instance.getSkeleton(),info);
        }
        PersistentObjectUtil.save(ModMain.instance.getSkeleton());
    }

    public static void loadFromPersistenUtil() {
        //fired at onServerCreated
        createSaveTimer();
        for (Class type: newsTypes) {
            ArrayList<EntityInfo> list = PersistentObjectUtil.getCopyOfObjects(ModMain.instance.getSkeleton(),type);
            DebugFile.log("loading newsStorage from persistent obj Util: class: "+ type.toString() + ".size: " + list.size());
            //TODO dont loop a bruteforce test on all existing infos.
            for (EntityInfo info: list) {
                boolean isClone = false;
                for (GenericInfo clone: newsStorage) {
                    if (clone.getTime() == info.getTime()) {
                        isClone = true;
                        break;
                    }
                }
                if (isClone) {
                    continue;
                }
                newsStorage.add(info);
            }
        }
    }

    private static void createSaveTimer() {
        if (saveTimerOn) {
            return;
        }
        saveTimerOn = true;
        new StarRunnable() {
            @Override
            public void run() {
                saveToPersistenUtil();
            }
        }.runTimer(ModMain.instance,12 * 60 * 5);
    }

    public static void cleanPersistentInfo() {
        //TODO
        for (Class cl: newsTypes) {
            ArrayList<Object> list = PersistentObjectUtil.getCopyOfObjects(ModMain.instance.getSkeleton(),cl);
            for (Object obj: list) {
                newsStorage.remove(obj);
                PersistentObjectUtil.removeObject(ModMain.instance.getSkeleton(),obj);
                DebugFile.log("removing object");
            }
        }
    }

}
