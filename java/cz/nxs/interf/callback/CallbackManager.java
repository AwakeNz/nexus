/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.engine.base.EventType
 *  cz.nxs.events.engine.team.EventTeam
 *  javolution.util.FastList
 */
package cz.nxs.interf.callback;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.ICallback;
import java.util.Collection;
import java.util.List;
import javolution.util.FastList;

public class CallbackManager
implements ICallback {
    public List<ICallback> _list = new FastList();

    public void registerCallback(ICallback c) {
        this._list.add(c);
    }

    @Override
    public void eventStarts(int instance, EventType event, Collection<? extends EventTeam> teams) {
        for (ICallback cb : this._list) {
            try {
                cb.eventStarts(instance, event, teams);
            }
            catch (Exception e) {}
        }
    }

    @Override
    public void playerKills(EventType event, PlayerEventInfo player, PlayerEventInfo target) {
        for (ICallback cb : this._list) {
            try {
                cb.playerKills(event, player, target);
            }
            catch (Exception e) {}
        }
    }

    @Override
    public void playerScores(EventType event, PlayerEventInfo player, int count) {
        for (ICallback cb : this._list) {
            try {
                cb.playerScores(event, player, count);
            }
            catch (Exception e) {}
        }
    }

    @Override
    public void playerFlagScores(EventType event, PlayerEventInfo player) {
        for (ICallback cb : this._list) {
            try {
                cb.playerFlagScores(event, player);
            }
            catch (Exception e) {}
        }
    }

    @Override
    public void playerKillsVip(EventType event, PlayerEventInfo player, PlayerEventInfo vip) {
        for (ICallback cb : this._list) {
            try {
                cb.playerKillsVip(event, player, vip);
            }
            catch (Exception e) {}
        }
    }

    @Override
    public void eventEnded(int instance, EventType event, Collection<? extends EventTeam> teams) {
        for (ICallback cb : this._list) {
            try {
                cb.eventEnded(instance, event, teams);
            }
            catch (Exception e) {}
        }
    }

    public static final CallbackManager getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final CallbackManager _instance = new CallbackManager();

        private SingletonHolder() {
        }
    }

}

