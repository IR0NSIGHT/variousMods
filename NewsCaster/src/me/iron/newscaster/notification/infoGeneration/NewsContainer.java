package me.iron.newscaster.notification.infoGeneration;

import api.DebugFile;
import api.mod.config.PersistentObjectUtil;
import com.sun.org.apache.xpath.internal.operations.Mod;
import me.iron.newscaster.ModMain;
import me.iron.newscaster.notification.infoGeneration.infoTypes.GenericInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 23.07.2021
 * TIME: 20:30
 */
public class NewsContainer implements Serializable {
    public NewsContainer() {

    }
    private List<GenericInfo> news = new ArrayList<>();

    public void setNews(List<GenericInfo> news) {
        clean();
        this.news.addAll(news);
    }

    public List<GenericInfo> getNews() {
        assert news != null;
        return news;
    }

    public void clean() {
        this.news = new ArrayList<>();
    }
    public static NewsContainer getSaveObject() {
        List<Object> cons = PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),NewsContainer.class);
        if (cons.size()>1){
            DebugFile.log("to many savefiles detected, deleting " + (cons.size()-1), ModMain.instance);
            for (int i = 1; i < cons.size(); i++) {
                PersistentObjectUtil.removeObject(ModMain.instance.getSkeleton(),cons.get(i));
            }
            assert PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),NewsContainer.class).size() == 1:"to many savefiles";
        }

        if (cons.size() == 0) {
            NewsContainer out = new NewsContainer();
            PersistentObjectUtil.addObject(ModMain.instance.getSkeleton(), out);
            assert PersistentObjectUtil.getObjects(ModMain.instance.getSkeleton(),NewsContainer.class).size() == 1:"to many savefiles";
            return out;
        } else {
            assert cons.get(0) instanceof NewsContainer: "savefile container is of wrong type";
            return (NewsContainer) cons.get(0);
        }
    }
}
