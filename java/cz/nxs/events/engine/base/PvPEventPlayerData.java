/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 */
package cz.nxs.events.engine.base;

import cz.nxs.events.EventGame;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.stats.GlobalStats;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.interf.PlayerEventInfo;

public class PvPEventPlayerData
extends EventPlayerData {
    private int _kills = 0;
    private int _deaths = 0;
    private int _spree = 0;

    public PvPEventPlayerData(PlayerEventInfo owner, EventGame event, GlobalStatsModel stats) {
        super(owner, event, stats);
    }

    public int getKills() {
        return this._kills;
    }

    public int raiseKills(int i) {
        this._kills+=i;
        this._globalStats.raise(GlobalStats.GlobalStatType.KILLS, i);
        return this._kills;
    }

    public void setKills(int i) {
        this._kills = i;
        this._globalStats.set(GlobalStats.GlobalStatType.KILLS, i);
    }

    public int getDeaths() {
        return this._deaths;
    }

    public int raiseDeaths(int i) {
        this._deaths+=i;
        this._globalStats.raise(GlobalStats.GlobalStatType.DEATHS, i);
        return this._deaths;
    }

    public void setDeaths(int i) {
        this._deaths = i;
        this._globalStats.set(GlobalStats.GlobalStatType.DEATHS, i);
    }

    public int getSpree() {
        return this._spree;
    }

    public int raiseSpree(int i) {
        this._spree+=i;
        return this._spree;
    }

    public void setSpree(int i) {
        this._spree = i;
    }
}

