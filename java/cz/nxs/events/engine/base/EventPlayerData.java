/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 */
package cz.nxs.events.engine.base;

import cz.nxs.events.EventGame;
import cz.nxs.events.engine.stats.GlobalStats;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.interf.PlayerEventInfo;

public class EventPlayerData {
    private PlayerEventInfo _owner;
    protected GlobalStatsModel _globalStats;
    private int _score;

    public EventPlayerData(PlayerEventInfo owner, EventGame event, GlobalStatsModel stats) {
        this._owner = owner;
        this._globalStats = stats;
    }

    public PlayerEventInfo getOwner() {
        return this._owner;
    }

    public int getScore() {
        return this._score;
    }

    public int raiseScore(int i) {
        this._score+=i;
        this._globalStats.raise(GlobalStats.GlobalStatType.SCORE, i);
        return this._score;
    }

    public void setScore(int i) {
        this._score = i;
        this._globalStats.set(GlobalStats.GlobalStatType.SCORE, i);
    }

    public GlobalStatsModel getGlobalStats() {
        return this._globalStats;
    }
}

