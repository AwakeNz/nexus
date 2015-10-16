/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.engine.base.EventType
 *  cz.nxs.events.engine.team.EventTeam
 */
package cz.nxs.interf.callback;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.callback.ICallback;
import java.util.Collection;

public class EventAchievments
implements ICallback {
    @Override
    public void eventStarts(int instance, EventType event, Collection<? extends EventTeam> teams) {
    }

    @Override
    public void playerKills(EventType event, PlayerEventInfo player, PlayerEventInfo target) {
    }

    @Override
    public void playerScores(EventType event, PlayerEventInfo player, int count) {
    }

    @Override
    public void playerFlagScores(EventType event, PlayerEventInfo player) {
    }

    @Override
    public void playerKillsVip(EventType event, PlayerEventInfo player, PlayerEventInfo vip) {
    }

    @Override
    public void eventEnded(int instance, EventType event, Collection<? extends EventTeam> teams) {
    }

    public static final EventAchievments getInstance() {
        if (SingletonHolder._instance == null) {
            SingletonHolder.register();
        }
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static EventAchievments _instance;

        private SingletonHolder() {
        }

        private static void register() {
            _instance = new EventAchievments();
            CallbackManager.getInstance().registerCallback(_instance);
        }
    }

}

