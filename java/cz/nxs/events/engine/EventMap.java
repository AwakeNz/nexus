/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import cz.nxs.events.Configurable;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.interf.PlayerEventInfo;

public class EventMap
{
	private final int _globalId;
	private boolean _saved = true;
	private String _mapName;
	private String _configs;
	private int _highestSpawnId;
	private List<EventType> _events;
	private final List<EventSpawn> _spawns;
	private final Map<Integer, Map<SpawnType, Integer>> _history;
	private final Map<Integer, EventSpawn> _lastSpawns;
	private List<EventSpawn> _doorsSpawn;
	private boolean _hasDoors;
	private final Map<EventType, Map<String, ConfigModel>> _configModels;
	public static Comparator<EventSpawn> compareByIdAsc = (s1, s2) ->
	{
		int id2;
		int id1 = s1.getSpawnId();
		return id1 == (id2 = s2.getSpawnId()) ? 0 : (id1 < id2 ? -1 : 1);
	};
	public static Comparator<EventSpawn> compareByIdDesc = (s1, s2) ->
	{
		int id2;
		int id1 = s1.getSpawnId();
		return id1 == (id2 = s2.getSpawnId()) ? 0 : (id1 > id2 ? -1 : 1);
	};
	public static Comparator<EventSpawn> compareByType = (s1, s2) ->
	{
		SpawnType t1 = s1.getSpawnType();
		SpawnType t2 = s2.getSpawnType();
		return t1.compareTo(t2);
	};
	
	public EventMap(int mapId, String mapName, List<EventType> events, List<EventSpawn> spawns, String configs)
	{
		_globalId = mapId;
		_mapName = mapName;
		_configs = configs;
		_spawns = new FastList<>();
		_history = new FastMap<>();
		_lastSpawns = new FastMap<>();
		_events = events;
		if (_events == null)
		{
			NexusLoader.debug("_events null in EventMap constructor");
			_events = new FastList<>();
		}
		_configModels = new FastMap<>();
		addSpawns(spawns);
		initDoors();
	}
	
	public void loadConfigs()
	{
		for (EventType event : _events)
		{
			initEventsConfigs(event);
		}
		EventConfig.getInstance().loadMapConfigs(this, _configs);
	}
	
	private void initEventsConfigs(EventType event)
	{
		_configModels.put(event, new FastMap<>());
		Configurable conf = EventManager.getInstance().getEvent(event);
		if ((conf == null) || (conf.getMapConfigs() == null))
		{
			return;
		}
		for (ConfigModel config : conf.getMapConfigs().values())
		{
			_configModels.get(event).put(config.getKey(), new ConfigModel(config.getKey(), config.getValue(), config.getDesc(), config.getInput()));
		}
	}
	
	private void deleteEventsConfigs(EventType event)
	{
		this._configModels.remove(event);
	}
	
	public void setConfigValue(EventType event, String key, String value, boolean addToValue)
	{
		try
		{
			if (!this._configModels.containsKey(event))
			{
				NexusLoader.debug("Trying to set MapConfig's: map ID " + this.getGlobalId() + " event " + event.getAltTitle() + ", config's key = " + key + ". The map doesn't have such event.");
				return;
			}
			if (this._configModels.get(event).get(key) == null)
			{
				NexusLoader.debug("Trying to set MapConfig's: map ID " + this.getGlobalId() + " event " + event.getAltTitle() + ", config's key = " + key + ", but this config doesn't exist for that map! Skipping...");
				return;
			}
			if (!addToValue)
			{
				this._configModels.get(event).get(key).setValue(value);
			}
			else
			{
				this._configModels.get(event).get(key).addToValue(value);
			}
		}
		catch (Exception e)
		{
			NexusLoader.debug("Error setting map config's value to " + value + ", config's key = " + key + ", map ID = " + this.getGlobalId() + " and event = " + event.getAltTitle(), Level.WARNING);
			e.printStackTrace();
		}
	}
	
	public Map<EventType, Map<String, ConfigModel>> getConfigModels()
	{
		return this._configModels;
	}
	
	public ConfigModel getConfigModel(EventType event, String key)
	{
		try
		{
			if (!this._configModels.containsKey(event))
			{
				NexusLoader.debug("Trying to set MapConfig's value: map ID " + this.getGlobalId() + " event " + event.getAltTitle() + ", config's key = " + key + ". The map doesn't have such event.");
				return null;
			}
			return this._configModels.get(event).get(key);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public void addSpawns(List<EventSpawn> spawns)
	{
		if (spawns == null)
		{
			return;
		}
		this._spawns.addAll(spawns);
		for (EventSpawn spawn : spawns)
		{
			if (!this._history.containsKey(spawn.getSpawnTeam()))
			{
				this._history.put(spawn.getSpawnTeam(), new FastMap());
			}
			if (this._history.get(spawn.getSpawnTeam()).containsKey(spawn.getSpawnType()))
			{
				continue;
			}
			this._history.get(spawn.getSpawnTeam()).put(spawn.getSpawnType(), 0);
		}
		this.recalcLastSpawnId();
	}
	
	public FastList<EventSpawn> getSpawns(int teamId, SpawnType type)
	{
		FastList temp = new FastList();
		for (EventSpawn spawn : this._spawns)
		{
			if (((spawn.getSpawnTeam() != teamId) && (teamId != -1)) || (spawn.getSpawnType() != type))
			{
				continue;
			}
			temp.add(spawn);
		}
		return temp;
	}
	
	public void clearHistory(int teamId, SpawnType type)
	{
		if (teamId == -1)
		{
			for (Map.Entry<Integer, Map<SpawnType, Integer>> e : this._history.entrySet())
			{
				this._history.get(e.getKey()).put(type, 0);
			}
		}
		else
		{
			this._history.get(teamId).put(type, 0);
		}
	}
	
	public EventSpawn getNextSpawn(int teamId, SpawnType type)
	{
		FastList<EventSpawn> spawns = this.getSpawns(teamId, type);
		if ((spawns == null) || spawns.isEmpty())
		{
			return null;
		}
		if (teamId == -1)
		{
			teamId = 0;
		}
		int lastId = 0;
		try
		{
			lastId = this._history.get(teamId).get(type);
		}
		catch (NullPointerException e)
		{
			lastId = 0;
		}
		EventSpawn nextSpawn = null;
		for (EventSpawn spawn : spawns)
		{
			if (spawn.getSpawnId() <= lastId)
			{
				continue;
			}
			nextSpawn = spawn;
			break;
		}
		if (nextSpawn == null)
		{
			nextSpawn = spawns.getFirst();
		}
		lastId = nextSpawn.getSpawnId();
		if (!this._history.containsKey(teamId))
		{
			this._history.put(teamId, new FastMap());
		}
		this._history.get(teamId).put(type, lastId);
		return nextSpawn;
	}
	
	public List<EventSpawn> getSpawns()
	{
		return this._spawns;
	}
	
	public EventSpawn getSpawn(int spawnId)
	{
		for (EventSpawn spawn : this._spawns)
		{
			if (spawn.getSpawnId() != spawnId)
			{
				continue;
			}
			return spawn;
		}
		return null;
	}
	
	public boolean removeSpawn(int spawnId, boolean db)
	{
		for (EventSpawn spawn : this._spawns)
		{
			if (spawn.getSpawnId() != spawnId)
			{
				continue;
			}
			this._spawns.remove(spawn);
			if (this.getSpawns(spawn.getSpawnTeam(), spawn.getSpawnType()).isEmpty())
			{
				this._history.remove(spawn.getSpawnType());
			}
			if (db)
			{
				EventMapSystem.getInstance().removeSpawnFromDb(spawn);
			}
			this.recalcLastSpawnId();
			return true;
		}
		return false;
	}
	
	private void recalcLastSpawnId()
	{
		int highestId = 0;
		for (EventSpawn spawn : this._spawns)
		{
			if (spawn.getSpawnId() <= highestId)
			{
				continue;
			}
			highestId = spawn.getSpawnId();
		}
		this._highestSpawnId = highestId;
	}
	
	private void initDoors()
	{
		for (EventSpawn spawn : this._spawns)
		{
			if (spawn.getSpawnType() != SpawnType.Door)
			{
				continue;
			}
			if (this._doorsSpawn == null)
			{
				this._doorsSpawn = new FastList();
			}
			this._doorsSpawn.add(spawn);
			this._hasDoors = true;
		}
	}
	
	public String[] getAviableConfigs(EventType type)
	{
		if ((type != EventType.Unassigned) && this._events.contains(type))
		{
			Configurable event = EventManager.getInstance().getEvent(type, 1);
			if (event == null)
			{
				System.out.println("null event at getAviableConfigs(EventType)");
				return null;
			}
			return event.getMapConfigs().keySet().toArray(new String[event.getMapConfigs().size()]);
		}
		System.out.println("getAviableConfigs - type " + type.getAltTitle() + " returned null.");
		return null;
	}
	
	public boolean hasDoor()
	{
		return this._hasDoors;
	}
	
	public List<EventSpawn> getDoors()
	{
		return this._doorsSpawn;
	}
	
	public EventSpawn getLastSpawn(int teamId)
	{
		return this._lastSpawns.get(teamId);
	}
	
	public String getMapName()
	{
		return this._mapName;
	}
	
	public int getGlobalId()
	{
		return this._globalId;
	}
	
	public List<EventType> getEvents()
	{
		return this._events;
	}
	
	public String getConfigs()
	{
		return this._configs;
	}
	
	public void setConfigs(String s)
	{
		this._configs = s;
	}
	
	public void setMapName(String name)
	{
		this._mapName = name;
		this._saved = false;
	}
	
	public int getNewSpawnId()
	{
		return this._highestSpawnId + 1;
	}
	
	public void addEvent(EventType type)
	{
		this._events.add(type);
		EventMapSystem.getInstance().addMapToEvent(this, type);
		this.initEventsConfigs(type);
		this._saved = false;
	}
	
	public void removeEvent(EventType type)
	{
		if (this._events.remove(type))
		{
			EventMapSystem.getInstance().removeMapFromEvent(this, type);
			this.deleteEventsConfigs(type);
			if (this._events.isEmpty())
			{
				this._events.add(EventType.Unassigned);
				EventMapSystem.getInstance().addMapToEvent(this, EventType.Unassigned);
			}
			this._saved = false;
		}
	}
	
	public boolean isSaved()
	{
		return this._saved;
	}
	
	public void setSaved(boolean b)
	{
		this._saved = b;
		if (this._saved)
		{
			this.initDoors();
		}
	}
	
	public boolean checkForSpawns(SpawnType type, int teamId, int count)
	{
		try
		{
			return this.getSpawns(teamId, type).size() >= count;
		}
		catch (NullPointerException npe)
		{
			return false;
		}
	}
	
	public String getMissingSpawns()
	{
		TextBuilder tb = new TextBuilder();
		for (EventType type : this.getEvents())
		{
			if ((type == EventType.Classic_1v1) || (type == EventType.PartyvsParty) || (type == EventType.TvT) || (type == EventType.TVTv) || (type == EventType.MiniTvT))
			{
				if (!this.checkForSpawns(SpawnType.Regular, 1, 1))
				{
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>REGULAR</font> <font color=9f9f9f>spawn team 1 count 1 (or more)</font><br1>");
				}
				if (!this.checkForSpawns(SpawnType.Regular, 2, 1))
				{
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>REGULAR</font> <font color=9f9f9f>spawn team 2 count 1 (or more)</font><br1>");
				}
				if (type != EventType.TVTv)
				{
					continue;
				}
				if (!this.checkForSpawns(SpawnType.VIP, 1, 1))
				{
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>VIP</font> <font color=9f9f9f>spawn team 1 count 1 (or more)</font><br1>");
				}
				if (this.checkForSpawns(SpawnType.VIP, 2, 1))
				{
					continue;
				}
				tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>VIP</font> <font color=9f9f9f>spawn team 2 count 1 (or more)</font><br1>");
				continue;
			}
			if (type == EventType.Domination)
			{
				if (!this.checkForSpawns(SpawnType.Regular, 1, 1))
				{
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>REGULAR</font> <font color=9f9f9f>spawn team 1 count 1 (or more)</font><br1>");
				}
				if (!this.checkForSpawns(SpawnType.Regular, 2, 1))
				{
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>REGULAR</font> <font color=9f9f9f>spawn team 2 count 1 (or more)</font><br1>");
				}
				if (this.checkForSpawns(SpawnType.Zone, -1, 1))
				{
					continue;
				}
				tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>ZONE</font> <font color=9f9f9f>spawn team -1 (team does not matter) count 1</font><br1>");
				continue;
			}
			if (type == EventType.MassDomination)
			{
				if (!this.checkForSpawns(SpawnType.Regular, 1, 1))
				{
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>REGULAR</font> <font color=9f9f9f>spawn team 1 count 1 (or more)</font><br1>");
				}
				if (!this.checkForSpawns(SpawnType.Regular, 2, 1))
				{
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>REGULAR</font> <font color=9f9f9f>spawn team 2 count 1 (or more)</font><br1>");
				}
				try
				{
					Configurable massDomEvent = EventManager.getInstance().getEvent(EventType.MassDomination);
					int count = massDomEvent.getConfigs().get("countOfZones").getValueInt();
					if (this.checkForSpawns(SpawnType.Zone, -1, count))
					{
						continue;
					}
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>ZONE</font> <font color=9f9f9f>spawn team -1 (team does not matter) count " + count + "</font><br1>");
				}
				catch (Exception e)
				{
					e.printStackTrace();
					if (this.checkForSpawns(SpawnType.Zone, -1, 2))
					{
						continue;
					}
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>ZONE</font> <font color=9f9f9f>spawn team -1 (team does not matter) count at least 2 (depends on what you have in your configs)</font><br1>");
				}
				continue;
			}
			if (type == EventType.Korean)
			{
				if (!this.checkForSpawns(SpawnType.Safe, 1, 1))
				{
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>SAFE</font> <font color=9f9f9f>spawn, team 1 count 1.</font> <font color=54585C>This is initial spawn for Players.</font><br1>");
				}
				if (!this.checkForSpawns(SpawnType.Safe, 2, 1))
				{
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>SAFE</font> <font color=9f9f9f>spawn, team 2 count 1.</font> <font color=54585C>This is initial spawn for Players.</font><br1>");
				}
				if (!this.checkForSpawns(SpawnType.Regular, 1, 4))
				{
					tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>REGULAR</font> <font color=9f9f9f>spawn, team 1 count 4.</font> <font color=54585C>(for each player one spot)</font><br1>");
				}
				if (this.checkForSpawns(SpawnType.Regular, 2, 4))
				{
					continue;
				}
				tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>REGULAR</font> <font color=9f9f9f>spawn, team 2 count 4.</font> <font color=54585C>(for each player one spot)</font><br1>");
				continue;
			}
			if (type != EventType.CTF)
			{
				continue;
			}
			if (!this.checkForSpawns(SpawnType.Flag, 1, 1))
			{
				tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>FLAG</font> <font color=9f9f9f>spawn, team 1 count 1.</font> <font color=54585C>This defines the place where Team 1 flag will appear.</font><br1>");
			}
			if (!this.checkForSpawns(SpawnType.Flag, 2, 1))
			{
				tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>FLAG</font> <font color=9f9f9f>spawn, team 2 count 1.</font> <font color=54585C>This defines the place where Team 2 flag will appear.</font><br1>");
			}
			if (!this.checkForSpawns(SpawnType.Regular, 1, 1))
			{
				tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>REGULAR</font> <font color=9f9f9f>spawn team 2 count 1 (or more)</font><br1>");
			}
			if (this.checkForSpawns(SpawnType.Regular, 2, 1))
			{
				continue;
			}
			tb.append("<font color=B46F6B>" + type.getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>REGULAR</font> <font color=9f9f9f>spawn team 2 count 1 (or more)</font><br1>");
		}
		return tb.toString();
	}
	
	public String getMissingConfigs()
	{
		return "";
	}
	
	public void checkMap(PlayerEventInfo gm)
	{
		for (EventType type : this.getEvents())
		{
			if ((type == EventType.Classic_1v1) || (type == EventType.Classic_2v2) || (type == EventType.PartyvsParty) || (type == EventType.TvT) || (type == EventType.TVTv) || (type == EventType.MiniTvT))
			{
				if (!this.checkForSpawns(SpawnType.Regular, 1, 1))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1, count 1");
				}
				if (this.checkForSpawns(SpawnType.Regular, 2, 1))
				{
					continue;
				}
				gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 2, count 1");
				continue;
			}
			if (type == EventType.CTF)
			{
				if (!this.checkForSpawns(SpawnType.Flag, 1, 1))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type FLAG, team 1, count 1.");
				}
				if (!this.checkForSpawns(SpawnType.Flag, 2, 1))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type FLAG, team 2, count 1.");
				}
				if (!this.checkForSpawns(SpawnType.Regular, 1, 1))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1, count 1 or more.");
				}
				if (this.checkForSpawns(SpawnType.Regular, 2, 1))
				{
					continue;
				}
				gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 2, count 1 or more.");
				continue;
			}
			if ((type == EventType.DM) || (type == EventType.LMS))
			{
				if (this.checkForSpawns(SpawnType.Regular, 1, -1))
				{
					continue;
				}
				gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, count 1 or more. (team doesn't matter)");
				continue;
			}
			if ((type == EventType.Mutant) || (type == EventType.Zombies))
			{
				if (!this.checkForSpawns(SpawnType.Regular, 1, -1))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR,count 1 or more. (team doesn't matter");
				}
				if (this.checkForSpawns(SpawnType.Zombie, 1, -1))
				{
					continue;
				}
				gm.sendMessage(type.getAltTitle() + ": Missing spawn type ZOMBIE, count 1 or more.");
				continue;
			}
			if (type == EventType.Korean)
			{
				if (!this.checkForSpawns(SpawnType.Safe, 1, 1))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type SAFE, team 1, count 1. This is initial spawn for Players.");
				}
				if (!this.checkForSpawns(SpawnType.Safe, 2, 1))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type SAFE, team 2, count 1. This is initial spawn for Players.");
				}
				if (!this.checkForSpawns(SpawnType.Regular, 1, 4))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1, count 4 (for each player one spot)");
				}
				if (this.checkForSpawns(SpawnType.Regular, 2, 4))
				{
					continue;
				}
				gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 2, count 4 (for each player one spot)");
				continue;
			}
			if (type == EventType.Underground_Coliseum)
			{
				if (!this.checkForSpawns(SpawnType.Regular, 1, 4))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1. count 1. This is initial spawn used to teleport players before event starts.");
				}
				if (!this.checkForSpawns(SpawnType.Regular, 2, 4))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 2, count 1. This is initial spawn used to teleport players before event starts.");
				}
				if (!this.checkForSpawns(SpawnType.Safe, 1, 1))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type SAFE, team 1, count 1. This is respawn spot.");
				}
				if (this.checkForSpawns(SpawnType.Safe, 2, 1))
				{
					continue;
				}
				gm.sendMessage(type.getAltTitle() + ": Missing spawn type SAFE, team 2, count 1. This is respawn spot.");
				continue;
			}
			if (type == EventType.RBHunt)
			{
				if (!this.checkForSpawns(SpawnType.Boss, -1, 1))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type BOSS count 1.");
				}
				if (!this.checkForSpawns(SpawnType.Regular, 1, 1))
				{
					gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1, count 1 or more.");
				}
				if (this.checkForSpawns(SpawnType.Regular, 2, 1))
				{
					continue;
				}
				gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 2, count 1 or more.");
				continue;
			}
			if (type != EventType.SurvivalArena)
			{
				continue;
			}
			boolean round1 = false;
			boolean round2 = false;
			boolean round3 = false;
			if (!round1)
			{
				gm.sendMessage(type.getAltTitle() + ": Missing spawn type MONSTER for FIRST round!");
			}
			if (!round2)
			{
				gm.sendMessage(type.getAltTitle() + ": Missing spawn type MONSTER for SECOND round!");
			}
			if (!round3)
			{
				gm.sendMessage(type.getAltTitle() + ": Missing spawn type MONSTER for FINAL round!");
			}
			if (this.checkForSpawns(SpawnType.Regular, 1, 1))
			{
				continue;
			}
			gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1, count 1.");
		}
	}
	
}
