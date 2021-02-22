package me.iron.fleetCommander.notification;

import api.DebugFile;
import api.mod.config.PersistentObjectUtil;
import me.iron.fleetCommander.modMain;
import me.iron.fleetCommander.notification.infoTypes.FleetInfo;

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
        PersistentObjectUtil.addObject(modMain.instance.getSkeleton(),newsStorage);
        PersistentObjectUtil.save(modMain.instance.getSkeleton());
        DebugFile.log("saving newsStorage to persistenObjectUtil");
    }

    public static void loadFromPersistenUtil() {

        ArrayList<Object> objects = PersistentObjectUtil.getObjects(modMain.instance.getSkeleton(),newsStorage.getClass());
        DebugFile.log("loading newsStorage from persistent obj Util: objs.size: " + objects.size());
        if (objects.size() == 0) {
            return;
        }
        Object list =  objects.get(0);
        newsStorage = (List<FleetInfo>) list;
    }
}
