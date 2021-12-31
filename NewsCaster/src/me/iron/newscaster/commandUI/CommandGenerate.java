package me.iron.newscaster.commandUI;

import api.ModPlayground;
import api.mod.StarMod;
import api.utils.game.chat.CommandInterface;
import com.sun.istack.internal.Nullable;
import me.iron.newscaster.notification.broadcasting.Broadcaster;
import me.iron.newscaster.notification.infoGeneration.NewsManager;
import me.iron.newscaster.notification.infoGeneration.infoTypes.*;
import me.iron.newscaster.notification.infoGeneration.objectTypes.FactionObject;
import me.iron.newscaster.notification.infoGeneration.objectTypes.ShipObject;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.data.player.PlayerState;
import org.schema.game.common.data.player.faction.FactionRelation;
import org.schema.game.server.data.GameServerState;

import java.util.List;
import java.util.Random;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 15.09.2021
 * TIME: 12:48
 */
public class CommandGenerate implements CommandInterface {
    @Override
    public String getCommand() {
        return CommandUI.prefix+"_generate";
    }

    @Override
    public String[] getAliases() {
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "will generate random infos. params: seed(default 420), amount(default 3, max 20)";
    }

    @Override
    public boolean isAdminOnly() {
        return true;
    }

    @Override
    public boolean onCommand(PlayerState playerState, String[] strings) {
        if (GameServerState.instance == null) {
            //ModPlayground.broadcastMessage("IS NOT SERVERSIDE COMMAND");
            return false;
        }

        int amount = 3;
        int seed = 420;
        if (strings.length>0&& !strings[0].equals("")) {
            seed = Integer.parseInt(strings[0]);
        }
        if (strings.length>1) {
            amount = Math.min(20,Integer.parseInt(strings[1]));
        }

        Random rand = new Random(seed);
        GenericInfo[] infos = new GenericInfo[amount];
        for (int i = 0; i < amount; i++) {
            //generate random info
            GenericInfo info = generateRandInfo(rand);
            NewsManager.addInfo(info);
            infos[i] = info;
        }
        List<? extends GenericInfo> l = NewsManager.getNewsStorage();
        return true;
    }

    @Override
    public void serverAction(@Nullable PlayerState playerState, String[] strings) {

    }

    @Override
    public StarMod getMod() {
        return null;
    }

    private <T extends GenericInfo> T generateRandInfo(Random rand) {
        GenericInfo info = null;
        switch (Math.abs(rand.nextInt())%4) {
            case 0:
                info = new ShipCreatedInfo(generateRandShip(rand.nextInt()), new Vector3i(rand.nextInt(),rand.nextInt(),rand.nextInt()));
                break;
            case 1:
                info = new ShipDestroyedInfo(generateRandShip(rand.nextInt()),
                        generateRandShip(rand.nextInt()),
                        new Vector3i(rand.nextInt(),
                                rand.nextInt(),
                                rand.nextInt()));
                break;
            case 2:
                info = new FactionRelationInfo(generateRandFaction(rand.nextInt()),generateRandFaction(rand.nextInt()),generateRandRtype(rand.nextInt()), generateRandRtype(rand.nextInt()));
                break;
            case 3:
                info = new FactionSystemClaimInfo(generateRandFaction(rand.nextInt()),
                        generateRandFaction(rand.nextInt()),
                        generateRandEventType(rand.nextInt()),
                        new Vector3i(rand.nextInt(),rand.nextInt(),rand.nextInt()));
                break;
        }
        return (T) info;
    }

    private ShipObject generateRandShip(int seed) {
        Random rand = new Random(seed);
        String name = "exampleship"+Math.abs(rand.nextInt());
        ShipObject ship = new ShipObject(name,name,rand.nextInt(),
                Math.abs(rand.nextInt())%1000000,
                Math.abs(rand.nextInt())%1000000,
                Math.abs(rand.nextInt())%500000
        );
        ship.setFaction(generateRandFaction(rand.nextInt()));
        return ship;
    }

    private FactionObject generateRandFaction(int seed) {
        Random rand = new Random(seed);
        FactionObject f = new FactionObject();
        f.setFactionID(rand.nextInt());
        f.setFactionName("ExampleFaction"+rand.nextInt());
        return f;
    }

    private FactionRelation.RType generateRandRtype(int seed) {
        Random r = new Random(seed);
        int idx = Math.abs(r.nextInt())% FactionRelation.RType.values().length;
        return FactionRelation.RType.values()[idx];
    }

    private GenericInfo.EventType generateRandEventType(int seed) {
        Random r = new Random(seed);
        int idx = Math.abs(r.nextInt())% GenericInfo.EventType.values().length;
        return GenericInfo.EventType.values()[idx];
    }
}
