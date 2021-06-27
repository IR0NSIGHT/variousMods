import api.listener.Listener;
import api.listener.events.player.PlayerChatEvent;
import api.mod.StarLoader;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.server.data.GameServerState;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 12:23
 * basically a chatcommand class
 */
public class PlayerInterface {
    public static void init() {
        StarLoader.registerListener(PlayerChatEvent.class, new Listener<PlayerChatEvent>() {
            @Override
            public void onEvent(PlayerChatEvent event) {
                if (!event.isServer()) return;
                PlayerState sender = GameServerState.instance.getPlayerFromNameIgnoreCaseWOException(event.getMessage().sender);
                if (sender == null || !sender.isAdmin()) return;
                String mssg = event.getText();
                if (!mssg.contains("!pve")) return;

                //commands
                if (mssg.contains("!pve new trigger ")) {
                    mssg = mssg.replace("!pve new trigger ","");

                    return;
                }
            }
        },ModMain.instance);
    }
}
