/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.l2j.CallBack;

public class EventMapSystem
{
	private final Map<EventType, Map<Integer, EventMap>> _maps = new FastMap<>();
	private int _lastMapId = 0;
	
	public EventMapSystem()
	{
		for (EventType type : EventType.values())
		{
			_maps.put(type, new FastMap<>());
		}
	}
	
	private EventType[] getTypes(String s)
	{
		String[] splits = s.split(";");
		FastList<EventType> types = new FastList<>();
		for (String typeString : splits)
		{
			EventType t = EventType.getType(typeString);
			if (t == null)
			{
				continue;
			}
			types.add(t);
		}
		return types.toArray(new EventType[types.size()]);
	}
	
	public String convertToString(List<EventType> types)
	{
		TextBuilder tb = new TextBuilder();
		int i = 1;
		for (EventType t : types)
		{
			tb.append(t.toString());
			if (i < types.size())
			{
				tb.append(";");
			}
			++i;
		}
		return tb.toString();
	}
	
	public void loadMaps()
	{
		int count = 0;
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT mapId, mapName, eventType, configs, description FROM nexus_maps");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				String type = rset.getString("eventType");
				List<EventType> types = new FastList<>();
				for (EventType t : getTypes(type))
				{
					types.add(t);
				}
				EventMap map = new EventMap(rset.getInt("mapId"), rset.getString("mapName"), rset.getString("description"), types, this.loadSpawns(rset.getInt("mapId")), rset.getString("configs"));
				if (map.getMapDesc() == null)
				{
					map.setMapDesc("");
					map.setSaved(true);
				}
				map.loadConfigs();
				if (map.getGlobalId() > _lastMapId)
				{
					_lastMapId = map.getGlobalId();
				}
				for (EventType t2 : types)
				{
					_maps.get(t2).put(_maps.get(t2).size() + 1, map);
				}
				++count;
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		NexusLoader.debug("Nexus Engine: Loaded " + count + " EventMaps.");
	}
	
	public List<EventSpawn> loadSpawns(int arenaId)
	{
		List<EventSpawn> spawns = new FastList<>();
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT mapId, spawnId, x, y, z, teamId, type, note FROM nexus_spawns WHERE mapId = " + arenaId);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				EventSpawn spawn = new EventSpawn(rset.getInt("mapId"), rset.getInt("spawnId"), new Loc(rset.getInt("x"), rset.getInt("y"), rset.getInt("z")), rset.getInt("teamId"), rset.getString("type"));
				String note = rset.getString("note");
				if (note != null)
				{
					spawn.setNote(note);
				}
				spawn.setSaved(true);
				spawns.add(spawn);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return spawns;
	}
	
	public void addSpawnToDb(EventSpawn spawn)
	{
		if (spawn.isSaved())
		{
			return;
		}
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_spawns VALUES (" + spawn.getMapId() + ", " + spawn.getSpawnId() + ", " + spawn.getLoc().getX() + ", " + spawn.getLoc().getY() + ", " + spawn.getLoc().getZ() + ", " + spawn.getSpawnTeam() + ", '" + spawn.getSpawnType().toString() + "', " + (spawn.getNote() == null ? "''" : new StringBuilder().append("'").append(spawn.getNote()).append("'").toString()) + ")");
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		spawn.setSaved(true);
	}
	
	public void removeSpawnFromDb(EventSpawn spawn)
	{
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_spawns WHERE mapId = " + spawn.getMapId() + " AND spawnId = " + spawn.getSpawnId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void removeMapFromDb(EventMap map)
	{
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_maps WHERE mapId = " + map.getGlobalId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void addMapToDb(EventMap map, boolean force)
	{
		if (map.isSaved() && !force)
		{
			return;
		}
		map.setConfigs(EventConfig.getInstance().convertMapConfigs(map));
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_maps VALUES (" + map.getGlobalId() + ", '" + map.getMapName().replaceAll("'", "") + "', '" + this.convertToString(map.getEvents()) + "', '" + map.getConfigs() + "', '" + map.getMapDesc() + "')");
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		map.setSaved(true);
	}
	
	@SuppressWarnings("null")
	public EventMap getNextMap(MiniEventManager manager, int lastId, EventMode mode)
	{
		EventType type = manager.getEventType();
		int nextMapId = lastId;
		EventMap map = null;
		for (int limit = 0; limit < (_maps.get(type).size() + 99); ++limit)
		{
			map = this._maps.get(type).get(++nextMapId);
			if (map == null)
			{
				nextMapId = 0;
				continue;
			}
			if (!manager.canRun(map) || mode.getDisMaps().contains(map.getGlobalId()))
			{
				map = null;
				continue;
			}
			return map;
		}
		if (map == null)
		{
			NexusLoader.debug("No map aviable for event " + type.getAltTitle() + " and mode " + mode.getModeName(), Level.WARNING);
		}
		return map;
	}
	
	public int getMapIndex(EventType event, EventMap map)
	{
		for (Map.Entry<Integer, EventMap> e : _maps.get(event).entrySet())
		{
			if (e.getValue().getGlobalId() != map.getGlobalId())
			{
				continue;
			}
			return e.getKey();
		}
		return 0;
	}
	
	public EventMap getMapById(int id)
	{
		for (Map<Integer, EventMap> map : _maps.values())
		{
			for (Map.Entry<Integer, EventMap> m : map.entrySet())
			{
				if (m.getValue().getGlobalId() != id)
				{
					continue;
				}
				return m.getValue();
			}
		}
		return null;
	}
	
	public int getNewMapId()
	{
		return ++_lastMapId;
	}
	
	public int getMapsCount(EventType type)
	{
		return _maps.get(type).size();
	}
	
	public Map<Integer, EventMap> getMaps(EventType type)
	{
		return _maps.get(type);
	}
	
	public boolean removeMap(int id)
	{
		EventMap map = getMapById(id);
		if (map == null)
		{
			return false;
		}
		this.removeMapFromDb(map);
		if (map.getGlobalId() >= _lastMapId)
		{
			--_lastMapId;
		}
		for (EventType type : map.getEvents())
		{
			for (Map.Entry<Integer, EventMap> e : _maps.get(type).entrySet())
			{
				if (e.getValue().getGlobalId() != id)
				{
					continue;
				}
				_maps.get(type).remove(e.getKey());
				reorganizeMaps(type);
			}
		}
		for (EventSpawn spawn : map.getSpawns())
		{
			removeSpawnFromDb(spawn);
		}
		return true;
	}
	
	private void reorganizeMaps(EventType type)
	{
		Collection<EventMap> maps = _maps.get(type).values();
		Map<Integer, EventMap> mapping = new FastMap<>();
		for (EventMap map : maps)
		{
			mapping.put(mapping.size() + 1, map);
		}
		_maps.put(type, mapping);
	}
	
	public void addMap(EventMap map)
	{
		for (EventType type : map.getEvents())
		{
			_maps.get(type).put(_maps.get(type).size() + 1, map);
		}
	}
	
	public void addMapToEvent(EventMap map, EventType type)
	{
		List<EventMap> maps = new FastList<>();
		maps.addAll(_maps.get(type).values());
		maps.add(map);
		_maps.get(type).clear();
		int i = 0;
		for (EventMap m : maps)
		{
			_maps.get(type).put(i, m);
			++i;
		}
	}
	
	public void removeMapFromEvent(EventMap map, EventType type)
	{
		for (Map.Entry<Integer, EventMap> e : _maps.get(type).entrySet())
		{
			if (e.getValue().getGlobalId() != map.getGlobalId())
			{
				continue;
			}
			_maps.get(type).remove(e.getKey());
		}
	}
	
	public List<EventMap> getMainEventMaps(EventType type)
	{
		if (!type.isRegularEvent())
		{
			return null;
		}
		List<EventMap> maps = new FastList<>();
		maps.addAll(this._maps.get(type).values());
		return maps;
	}
	
	public EventMap getMap(EventType type, String mapName)
	{
		for (EventMap map : _maps.get(type).values())
		{
			if (!map.getMapName().toString().equals(mapName))
			{
				continue;
			}
			return map;
		}
		return null;
	}
	
	public static final EventMapSystem getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventMapSystem _instance = new EventMapSystem();
		
		private SingletonHolder()
		{
		}
	}
	
}
