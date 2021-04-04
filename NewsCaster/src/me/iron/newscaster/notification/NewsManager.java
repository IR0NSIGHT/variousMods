package me.iron.newscaster.notification;

import api.DebugFile;
import api.mod.config.PersistentObjectUtil;
import api.utils.StarRunnable;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.notification.infoTypes.FleetInfo;
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
    private static List<FleetInfo> newsStorage = new ArrayList();
    private static Class[] newsTypes = new Class[]{FleetInfo.class,ShipDestroyedInfo.class};
    private static boolean saveTimerOn = false;
    /**
     * add a news-infoObject to the news storage
     * @param info info object
     * @return index in list.
     */
    public static int addInfo(FleetInfo info) {
        //DebugFile.log("addInfo: " + info.getNewscast());
        newsStorage.add(info);
        return newsStorage.size(); //add to end of list => size = index
    }

    public static FleetInfo getInfo(int index) {
        try {
            return newsStorage.get(index);
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static List<FleetInfo> getNewsStorage() {
        return newsStorage;
    }

    public static void saveToPersistenUtil() {
        DebugFile.log("saving newsStorage to persistenObjectUtil");
        for (FleetInfo info: newsStorage) {
            PersistentObjectUtil.addObject(ModMain.instance.getSkeleton(),info);
        }
        PersistentObjectUtil.save(ModMain.instance.getSkeleton());
    }

    public static void loadFromPersistenUtil() {
        //fired at onServerCreated
        createSaveTimer();
        for (Class type: newsTypes) {
            ArrayList<FleetInfo> list = PersistentObjectUtil.getCopyOfObjects(ModMain.instance.getSkeleton(),type); //TODO does this work: list fleetinfo with other type obj?
            DebugFile.log("loading newsStorage from persistent obj Util: class: "+ type.toString() + ".size: " + list.size());

            for (FleetInfo info: list) {
                boolean isClone = false;
                for (FleetInfo clone: newsStorage) {
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
                PersistentObjectUtil.removeObject(ModMain.instance.getSkeleton(),obj);
                DebugFile.log("removing object");
            }
        }
    }
}
