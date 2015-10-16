/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  cz.nxs.l2j.CallBack
 *  cz.nxs.l2j.IPlayerBase
 *  javolution.util.FastMap
 */
package cz.nxs.interf;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.IPlayerBase;
import javolution.util.FastMap;

public class PlayerBase
implements IPlayerBase {
    private FastMap<Integer, PlayerEventInfo> players = new FastMap().shared();

    public void load() {
        CallBack.getInstance().setPlayerBase((IPlayerBase)this);
    }

    public PlayerEventInfo getPlayer(int id) {
        return (PlayerEventInfo)this.players.get((Object)id);
    }

    public FastMap<Integer, PlayerEventInfo> getPs() {
        return this.players;
    }

    protected PlayerEventInfo getPlayer(L2PcInstance player) {
        return player.getEventInfo();
    }

    public PlayerEventInfo addInfo(PlayerEventInfo player) {
        this.players.put((Object)player.getPlayersId(), (Object)player);
        return player;
    }

    public void eventEnd(PlayerEventInfo player) {
        this.deleteInfo(player.getOwner());
    }

    public void playerDisconnected(PlayerEventInfo player) {
        this.eventEnd(player);
    }

    public void deleteInfo(int player) {
        this.players.remove((Object)player);
    }

    protected void deleteInfo(L2PcInstance player) {
        this.players.remove((Object)player.getObjectId());
    }

    public static final PlayerBase getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final PlayerBase _instance = new PlayerBase();

        private SingletonHolder() {
        }
    }

}

