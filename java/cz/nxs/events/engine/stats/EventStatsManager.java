/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 */
package cz.nxs.events.engine.stats;

import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.html.EventHtmlManager;
import cz.nxs.events.engine.stats.EventSpecificStats;
import cz.nxs.events.engine.stats.GlobalStats;
import cz.nxs.interf.PlayerEventInfo;

public class EventStatsManager {
    private GlobalStats _globalStats = new GlobalStats();
    private EventSpecificStats _eventStats = new EventSpecificStats();

    public EventStatsManager() {
        this._globalStats.load();
        this._eventStats.load();
    }

    public GlobalStats getGlobalStats() {
        return this._globalStats;
    }

    public EventSpecificStats getEventStats() {
        return this._eventStats;
    }

    public void onBypass(PlayerEventInfo player, String command) {
        if (command.startsWith("global_")) {
            this._globalStats.onCommand(player, command.substring(7));
        } else if (command.startsWith("eventstats_")) {
            this._eventStats.onCommand(player, command.substring(11));
        } else if (command.startsWith("cbmenu")) {
            if (EventHtmlManager.BBS_COMMAND == null) {
                EventHtmlManager.BBS_COMMAND = EventConfig.getInstance().getGlobalConfigValue("cbPage");
            }
            EventManager.getInstance().getHtmlManager().onCbBypass(player, EventHtmlManager.BBS_COMMAND);
        }
    }

    public void onLogin(PlayerEventInfo player) {
        this._globalStats.onLogin(player);
        this._eventStats.onLogin(player);
    }

    public void onDisconnect(PlayerEventInfo player) {
        this._globalStats.onDisconnect(player);
        this._eventStats.onDisconnect(player);
    }

    public void reload() {
        this._globalStats.loadGlobalStats();
    }

    public static EventStatsManager getInstance() {
        return _instance;
    }

    private static class SingletonHolder {
        private static final EventStatsManager _instance = new EventStatsManager();

        private SingletonHolder() {
        }
    }

}

