/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.stats;

import java.util.Map;

import javolution.util.FastMap;
import cz.nxs.events.engine.base.EventType;

public class GlobalStatsModel
{
	protected EventType event;
	protected Map<GlobalStats.GlobalStatType, Integer> stats;
	
	public GlobalStatsModel(EventType event, Map<GlobalStats.GlobalStatType, Integer> stats)
	{
		this.event = event;
		this.stats = stats;
	}
	
	public GlobalStatsModel(EventType event)
	{
		this.event = event;
		this.stats = new FastMap<>();
		for (GlobalStats.GlobalStatType t : GlobalStats.GlobalStatType.values())
		{
			this.stats.put(t, 0);
		}
	}
	
	public int get(GlobalStats.GlobalStatType type)
	{
		return this.stats.get(type);
	}
	
	public void set(GlobalStats.GlobalStatType type, int value)
	{
		this.stats.put(type, value);
	}
	
	public void raise(GlobalStats.GlobalStatType type, int value)
	{
		this.set(type, this.get(type) + value);
	}
	
	public void add(GlobalStatsModel newStats)
	{
		for (Map.Entry<GlobalStats.GlobalStatType, Integer> e : newStats.stats.entrySet())
		{
			this.raise(e.getKey(), e.getValue());
		}
	}
	
	public EventType getEvent()
	{
		return this.event;
	}
	
	public String getFavoriteEvent()
	{
		return "N/A";
	}
}
