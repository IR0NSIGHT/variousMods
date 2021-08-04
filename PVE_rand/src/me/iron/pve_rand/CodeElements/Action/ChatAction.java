package me.iron.pve_rand.CodeElements.Action;

import me.iron.pve_rand.CodeElements.CustomAction;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;
import org.schema.schine.common.language.Lng;

import java.io.Serializable;
import java.util.List;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.06.2021
 * TIME: 13:14
 * an action that sends a chatmessage to all player in range of the sector.
 */
public class ChatAction extends CustomAction implements Serializable {
    private boolean showSector;
    String message;
    int range;

    /**
     * @param argument probablility 0..100
     * @param description description
     * @param message message displayed in chat
     * @param range range where mssg can be seen, -1 for everywhere
     */
    public ChatAction(int argument, String name, String description, String message, int range) {
        super(argument, name, description);
        this.message = message;
        this.range = range;
    }

    public ChatAction(int argument, String name, String description, String message, int range, boolean showSector) {
        super(argument, name ,description);
        this.message = message;
        this.range = range;
        this.showSector = true;
    }
    @Override
    protected void onExecute(int argument, Vector3i sector) {
        super.onExecute(argument, sector);
        //get all players
        for (PlayerState p: GameServerState.instance.getPlayerStatesByName().values()) {
            String message = this.message;
            Vector3i s = p.getCurrentSector();
            s.sub(sector);
            if (range != -1 && s.length() > range) return;
            if (showSector) message+=sector.toString();
            p.sendServerMessage(Lng.astr(message),0);
        }
    }

    @Override
    public String toString() {
        return "ChatAction{" +
                '"'+message +'"'+
                ((range != -1)?"-r: "+range:"")+
                (showSector?"-s":"")+
                '}';
    }

    protected void addString(List<String> ls) {
        ls.add("\t"+this.toString());
    }
}
