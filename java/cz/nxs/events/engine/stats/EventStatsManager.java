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
import cz.nxs.interf.PlayerEventInfo;

public class EventStatsManager
{
	private final GlobalStats _globalStats = new GlobalStats();
	private final EventSpecificStats _eventStats = new EventSpecificStats();
	
	public EventStatsManager()
	{
		_globalStats.load();
		_eventStats.load();
	}
	
	public GlobalStats getGlobalStats()
	{
		return _globalStats;
	}
	
	public EventSpecificStats getEventStats()
	{
		return _eventStats;
	}
	
	public void onBypass(PlayerEventInfo player, String command)
	{
		if (command.startsWith("global_"))
		{
			_globalStats.onCommand(player, command.substring(7));
		}
		else if (command.startsWith("eventstats_"))
		{
			_eventStats.onCommand(player, command.substring(11));
		}
		else if (command.startsWith("cbmenu"))
		{
			if (EventHtmlManager.BBS_COMMAND == null)
			{
				EventHtmlManager.BBS_COMMAND = EventConfig.getInstance().getGlobalConfigValue("cbPage");
			}
			EventManager.getInstance().getHtmlManager().onCbBypass(player, EventHtmlManager.BBS_COMMAND);
		}
	}
	
	public void onLogin(PlayerEventInfo player)
	{
		_globalStats.onLogin(player);
		_eventStats.onLogin(player);
	}
	
	public void onDisconnect(PlayerEventInfo player)
	{
		_globalStats.onDisconnect(player);
		_eventStats.onDisconnect(player);
	}
	
	public void reload()
	{
		_globalStats.loadGlobalStats();
	}
	
	public static EventStatsManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventStatsManager _instance = new EventStatsManager();
	}
}
