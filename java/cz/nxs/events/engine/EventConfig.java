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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import cz.nxs.events.Configurable;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.GlobalConfigModel;
import cz.nxs.events.engine.html.EventHtmlManager;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.mini.ScheduleInfo;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.l2j.CallBack;

public class EventConfig
{
	private final Map<EventType, Config> _eventConfigs = new FastMap<>();
	private final Map<String, List<GlobalConfigModel>> _globalConfigs = new FastMap<>();
	
	protected EventConfig()
	{
	}
	
	public void loadEventConfigs()
	{
		loadMiniEventModes();
		loadEventConfigsFromDb();
	}
	
	public void loadGlobalConfigs()
	{
		this.loadGlobalConfigsFromDb();
		EventSQLManager.addMissingGlobalConfigs();
		NexusLoader.debug("Nexus Engine: Loaded GlobalConfigs engine.");
	}
	
	private void loadMiniEventModes()
	{
		int count = 0;
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM nexus_modes");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				EventType type = EventType.getType(rset.getString("event"));
				if (type == null)
				{
					continue;
				}
				int modeId = rset.getInt("modeId");
				String modeName = rset.getString("name");
				String visibleName = rset.getString("visible_name");
				String parameters = rset.getString("params");
				boolean allowed = Boolean.parseBoolean(rset.getString("allowed"));
				String maps = rset.getString("disallowedMaps");
				String times = rset.getString("times");
				int npcId = rset.getInt("npcId");
				this.loadMode(type, modeId, modeName, visibleName, parameters, maps, times, allowed, npcId);
				++count;
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
		}
		NexusLoader.debug("Nexus Engine: Loaded " + count + " mini event modes.");
	}
	
	private void loadMode(EventType type, int modeId, String modeName, String visibleName, String parameters, String maps, String time, boolean allowed, int npcId)
	{
		MiniEventManager manager = EventManager.getInstance().createManager(type, modeId, modeName, visibleName, false);
		if (manager == null)
		{
			NexusLoader.debug("manager's null after it was created!", Level.WARNING);
			return;
		}
		manager.getMode().setAllowed(allowed);
		manager.getMode().getScheduleInfo().encrypt(time);
		if (!(maps.equals("") || maps.equals(" ")))
		{
			for (String s : maps.split(";"))
			{
				manager.getMode().getDisMaps().add(Integer.parseInt(s));
			}
		}
		EventMode.FeatureType featureType = null;
		String[] featuresAndConfigs = parameters.split(";");
		if ((featuresAndConfigs.length > 0) && (featuresAndConfigs[0] != "") && (featuresAndConfigs[0] != " "))
		{
			for (String features : featuresAndConfigs)
			{
				String[] splitted = features.split(":");
				for (EventMode.FeatureType t : EventMode.FeatureType.values())
				{
					if (!t.toString().equals(splitted[0]))
					{
						continue;
					}
					featureType = t;
					break;
				}
				if (featureType == null)
				{
					NexusLoader.debug("feature type - " + splitted[0] + " doesn't exist. " + "(event " + type.getAltTitle() + ", modeId " + modeId + ")", Level.WARNING);
					continue;
				}
				manager.getMode().addFeature(null, featureType, splitted[1]);
			}
		}
		manager.getMode().refreshScheduler();
		manager.getMode().setNpcId(npcId);
	}
	
	public MiniEventManager createDefaultMode(EventType type)
	{
		MiniEventManager manager = EventManager.getInstance().createManager(type, 1, "Default", "Default", false);
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_modes WHERE event = '" + type.getAltTitle() + "' AND modeId = " + 1);
			statement.execute();
			statement.close();
			MiniEventManager event = EventManager.getInstance().getMiniEvents().get(type).get(1);
			statement = con.prepareStatement("INSERT INTO nexus_modes VALUES (?,?,?,?,?,?,?,?,?)");
			statement.setString(1, type.getAltTitle());
			statement.setInt(2, 1);
			statement.setString(3, event.getMode().getModeName().replaceAll("'", ""));
			statement.setString(4, event.getMode().getVisibleName().replaceAll("'", ""));
			statement.setString(5, String.valueOf(event.getMode().isAllowed()));
			statement.setString(6, this.getParams(event));
			statement.setString(7, this.getDisMaps(event));
			statement.setString(8, this.getTimesAviable(event));
			statement.setInt(9, 0);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
		}
		return manager;
	}
	
	public void updateEventModes(EventType type, int modeId)
	{
		block14:
		{
			try
			{
				Connection con = CallBack.getInstance().getOut().getConnection();
				if (modeId <= 0)
				{
					PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_modes WHERE event = '" + type.getAltTitle() + "'");
					statement.execute();
					statement.close();
					for (Map.Entry<Integer, MiniEventManager> e : EventManager.getInstance().getMiniEvents().get(type).entrySet())
					{
						statement = con.prepareStatement("INSERT INTO nexus_modes VALUES (?,?,?,?,?,?,?,?,?)");
						statement.setString(1, type.getAltTitle());
						statement.setInt(2, e.getKey());
						statement.setString(3, e.getValue().getMode().getModeName().replaceAll("'", ""));
						statement.setString(4, e.getValue().getMode().getVisibleName().replaceAll("'", ""));
						statement.setString(5, String.valueOf(e.getValue().getMode().isAllowed()));
						statement.setString(6, this.getParams(e.getValue()));
						statement.setString(7, this.getDisMaps(e.getValue()));
						statement.setString(8, this.getTimesAviable(e.getValue()));
						statement.setInt(9, e.getValue().getMode().getNpcId());
						statement.execute();
						statement.close();
					}
					break block14;
				}
				PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_modes WHERE event = '" + type.getAltTitle() + "' AND modeId = " + modeId);
				statement.execute();
				statement.close();
				MiniEventManager event = EventManager.getInstance().getMiniEvent(type, modeId);
				if (event != null)
				{
					statement = con.prepareStatement("INSERT INTO nexus_modes VALUES (?,?,?,?,?,?,?,?,?)");
					statement.setString(1, type.getAltTitle());
					statement.setInt(2, modeId);
					statement.setString(3, event.getMode().getModeName().replaceAll("'", ""));
					statement.setString(4, event.getMode().getVisibleName().replaceAll("'", ""));
					statement.setString(5, String.valueOf(event.getMode().isAllowed()));
					statement.setString(6, this.getParams(event));
					statement.setString(7, this.getDisMaps(event));
					statement.setString(8, this.getTimesAviable(event));
					statement.setInt(9, event.getMode().getNpcId());
					statement.execute();
					statement.close();
					break block14;
				}
				NexusLoader.debug("Tried to save unexisting event mode - " + type.getAltTitle() + ", mode " + modeId, Level.WARNING);
			}
			catch (Exception e)
			{
			}
		}
	}
	
	private String getTimesAviable(MiniEventManager manager)
	{
		ScheduleInfo info = manager.getMode().getScheduleInfo();
		return info.decrypt();
	}
	
	private String getDisMaps(MiniEventManager manager)
	{
		TextBuilder tb = new TextBuilder();
		for (int mapId : manager.getMode().getDisMaps())
		{
			tb.append("" + mapId + ";");
		}
		String result = tb.toString();
		if (result.length() > 0)
		{
			return result.substring(0, result.length() - 1);
		}
		return result;
	}
	
	private String getParams(MiniEventManager manager)
	{
		TextBuilder tb = new TextBuilder();
		for (AbstractFeature feature : manager.getMode().getFeatures())
		{
			tb.append(feature.getType().toString() + ":" + feature.getParams() + ";");
		}
		String result = tb.toString();
		if (result.length() == 0)
		{
			return result;
		}
		return result.substring(0, result.length() - 1);
	}
	
	private void loadEventConfigsFromDb()
	{
		int count = 0;
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM nexus_configs");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				EventType type = EventType.getType(rset.getString("event"));
				if (type == null)
				{
					continue;
				}
				_eventConfigs.put(type, new Config(type, Boolean.parseBoolean(rset.getString("allowed"))));
				count += this.deconvert(type, rset.getString("params"));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
		}
		for (EventType t : EventType.values())
		{
			if (!t.allowEdits() || (t == EventType.Unassigned) || _eventConfigs.containsKey(t))
			{
				_eventConfigs.put(t, new Config(t, true));
			}
			addNew(t);
		}
		NexusLoader.debug("Nexus Engine: Loaded " + count + " configs for events.");
	}
	
	private String convert(EventType type)
	{
		TextBuilder tb = new TextBuilder();
		Configurable event = EventManager.getInstance().getEvent(type);
		if (event == null)
		{
			NexusLoader.debug("null event on EventConfig.convert, event type " + type.getAltTitle(), Level.SEVERE);
			return "";
		}
		for (Map.Entry<String, ConfigModel> e : event.getConfigs().entrySet())
		{
			tb.append(e.getKey() + ":" + e.getValue().getValue());
			tb.append(";");
		}
		String result = tb.toString();
		if (result.length() > 0)
		{
			return result.substring(0, result.length() - 1);
		}
		return "";
	}
	
	private int deconvert(EventType type, String params)
	{
		try
		{
			int count = 0;
			for (String config : params.split(";"))
			{
				String key = config.split(":")[0];
				String value = config.split(":").length > 1 ? config.split(":")[1] : "";
				EventManager.getInstance().getEvent(type).setConfig(key, value, false);
				++count;
			}
			return count;
		}
		catch (Exception e)
		{
			return 0;
		}
	}
	
	public boolean isEventAllowed(EventType type)
	{
		return this._eventConfigs.get(type) == null ? false : this._eventConfigs.get(type).allowed;
	}
	
	public void setEventAllowed(EventType type, boolean b)
	{
		this._eventConfigs.get(type).allowed = b;
		this.updateInDb(type);
	}
	
	public void updateInDb(EventType type)
	{
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_configs WHERE event = '" + type.getAltTitle() + "'");
			statement.execute();
			statement.close();
			statement = con.prepareStatement("INSERT INTO nexus_configs VALUES ('" + type.getAltTitle() + "', '" + Boolean.toString(this._eventConfigs.get(type).allowed) + "', ?)");
			statement.setString(1, this.convert(type));
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
		}
	}
	
	private void addNew(EventType type)
	{
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO nexus_configs VALUES ('" + type.getAltTitle() + "', 'true', ?)");
			statement.setString(1, this.convert(type));
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
		}
	}
	
	public void addConfig(EventType event, String param, String value, boolean addToValue)
	{
		if (event.isMiniEvent())
		{
			for (MiniEventManager mgr : EventManager.getInstance().getMiniEvents().get(event).values())
			{
				if (mgr == null)
				{
					continue;
				}
				mgr.setConfig(param, value, addToValue);
			}
		}
		else
		{
			EventManager.getInstance().getEvent(event).setConfig(param, value, addToValue);
		}
		this.updateInDb(event);
	}
	
	public void removeConfigMultiAddValue(EventType event, String key, int index)
	{
		if (event.isMiniEvent())
		{
			for (MiniEventManager mgr : EventManager.getInstance().getMiniEvents().get(event).values())
			{
				if (mgr == null)
				{
					continue;
				}
				mgr.getConfigs().get(key).removeMultiAddValueIndex(index);
			}
		}
		else
		{
			EventManager.getInstance().getEvent(event).getConfigs().get(key).removeMultiAddValueIndex(index);
		}
		this.updateInDb(event);
	}
	
	public String convertMapConfigs(EventMap map)
	{
		TextBuilder tb = new TextBuilder();
		int totalEvents = map.getConfigModels().size();
		int count = 1;
		for (Map.Entry<EventType, Map<String, ConfigModel>> e : map.getConfigModels().entrySet())
		{
			if (!e.getValue().values().isEmpty())
			{
				tb.append(e.getKey().getAltTitle() + ":");
				int size = e.getValue().values().size();
				int i = 1;
				for (ConfigModel config : e.getValue().values())
				{
					tb.append(config.getKey() + "-" + config.getValue());
					if (i < size)
					{
						tb.append(",");
					}
					++i;
				}
				if (count < totalEvents)
				{
					tb.append(";");
				}
			}
			++count;
		}
		return tb.toString();
	}
	
	public void saveMapConfigs(EventMap map)
	{
		EventMapSystem.getInstance().addMapToDb(map, true);
	}
	
	public void loadMapConfigs(EventMap map, String params)
	{
		try
		{
			if ((params == null) || params.isEmpty() || params.equals(" "))
			{
				return;
			}
			for (String event : params.split(";"))
			{
				if (event.split(":").length <= 1)
				{
					continue;
				}
				EventType eventType = EventType.getType(event.split(":")[0]);
				String[] configs = event.split(":")[1].split(",");
				if (eventType == null)
				{
					NexusLoader.debug("error while mapConfigs loading - event: " + event + " does not exist, map ID = " + map.getGlobalId(), Level.WARNING);
					continue;
				}
				for (String config : configs)
				{
					String key = config.split("-")[0];
					String value = config.split("-").length > 1 ? config.split("-")[1] : "";
					map.setConfigValue(eventType, key, value, false);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void setMapConfig(EventMap map, EventType event, String param, String value, boolean addToValue)
	{
		map.setConfigValue(event, param, value, addToValue);
		this.saveMapConfigs(map);
	}
	
	public void removeMapConfigMultiAddValue(EventMap map, EventType event, String key, int index)
	{
		map.getConfigModel(event, key).removeMultiAddValueIndex(index);
		this.saveMapConfigs(map);
	}
	
	public String getMapConfig(EventMap map, EventType type, String key)
	{
		try
		{
			return map.getConfigModel(type, key).getValue();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public int getMapConfigInt(EventMap map, EventType type, String key)
	{
		try
		{
			return map.getConfigModel(type, key).getValueInt();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return 0;
		}
	}
	
	public boolean getMapConfigBoolean(EventMap map, EventType type, String key)
	{
		try
		{
			return map.getConfigModel(type, key).getValueBoolean();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	private void loadGlobalConfigsFromDb()
	{
		this._globalConfigs.clear();
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM nexus_globalconfigs");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				String type = rset.getString("configType");
				String key = rset.getString("key");
				String desc = rset.getString("desc");
				String value = rset.getString("value");
				int input = rset.getInt("inputType");
				this.addGlobalConfig(type, key, desc, value, input);
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
		}
	}
	
	public GlobalConfigModel addGlobalConfig(String type, String key, String desc, String value, int inputType)
	{
		if (!this._globalConfigs.containsKey(type))
		{
			this._globalConfigs.put(type, new FastList<>());
		}
		GlobalConfigModel gc = new GlobalConfigModel(type, key, value, desc, inputType);
		this._globalConfigs.get(type).add(gc);
		return gc;
	}
	
	public void removeGlobalConfig(String type, String key)
	{
		for (GlobalConfigModel c : this._globalConfigs.get(type))
		{
			if (!c.getKey().equals(key))
			{
				continue;
			}
			this._globalConfigs.get(type).remove(c);
			break;
		}
	}
	
	public List<GlobalConfigModel> getGlobalConfigs(String type)
	{
		return this._globalConfigs.get(type);
	}
	
	public String getGlobalConfigValue(String type, String key)
	{
		if (NexusLoader.loadedOrBeingLoaded())
		{
			GlobalConfigModel gc = this.getGlobalConfig(type, key);
			if (gc != null)
			{
				return gc.getValue();
			}
			NexusLoader.debug("GlobalConfig '" + key + "' has not been found.", Level.WARNING);
		}
		return null;
	}
	
	public int getGlobalConfigInt(String type, String key)
	{
		if (NexusLoader.loadedOrBeingLoaded())
		{
			String val = this.getGlobalConfigValue(type, key);
			try
			{
				return Integer.parseInt(val);
			}
			catch (Exception e)
			{
				NexusLoader.debug("GlobalConfig '" + key + "' int cast error.", Level.WARNING);
				return 0;
			}
		}
		return 0;
	}
	
	public boolean getGlobalConfigBoolean(String type, String key)
	{
		if (NexusLoader.loadedOrBeingLoaded())
		{
			String val = this.getGlobalConfigValue(type, key);
			try
			{
				return Boolean.parseBoolean(val);
			}
			catch (Exception e)
			{
				NexusLoader.debug("GlobalConfig '" + key + "' boolean cast error.", Level.WARNING);
				return false;
			}
		}
		return false;
	}
	
	public int getGlobalConfigInt(String key)
	{
		return this.getGlobalConfigInt(null, key);
	}
	
	public String getGlobalConfigValue(String key)
	{
		return this.getGlobalConfigValue(null, key);
	}
	
	public boolean getGlobalConfigBoolean(String key)
	{
		return this.getGlobalConfigBoolean(null, key);
	}
	
	public String getGlobalConfigDesc(String type, String key)
	{
		GlobalConfigModel gc = this.getGlobalConfig(type, key);
		if (gc != null)
		{
			return gc.getDesc();
		}
		return null;
	}
	
	public String getGlobalConfigType(GlobalConfigModel config)
	{
		for (Map.Entry<String, List<GlobalConfigModel>> list : this._globalConfigs.entrySet())
		{
			for (GlobalConfigModel c : list.getValue())
			{
				if (!config.getKey().equals(c.getKey()))
				{
					continue;
				}
				return list.getKey();
			}
		}
		return null;
	}
	
	public boolean globalConfigExists(String key)
	{
		GlobalConfigModel gc = this.getGlobalConfig(null, key);
		return gc != null;
	}
	
	public void setGlobalConfigValue(GlobalConfigModel config, String key, String value)
	{
		String prev = config.getValue();
		config.setValue(value);
		try
		{
			if (key.equals("debug") && value.equals("true") && prev.equals("false"))
			{
				NexusLoader.loadDebugConsole(false);
			}
			else if (key.equals("detailedDebug"))
			{
				NexusLoader.detailedDebug = Boolean.parseBoolean(value);
			}
			else if (key.equals("detailedDebugToConsole"))
			{
				NexusLoader.detailedDebugToConsole = Boolean.parseBoolean(value);
			}
			else if (key.equals("logToFile"))
			{
				NexusLoader.logToFile = Boolean.parseBoolean(value);
			}
			else if (key.equals("maxWarnings"))
			{
				EventWarnings.MAX_WARNINGS = Integer.parseInt(value);
			}
			else if (key.equals("maxBuffsPerPage"))
			{
				EventHtmlManager.BUFFS_PER_PAGE = Integer.parseInt(value);
			}
			else if (key.equals("cbPage"))
			{
				EventHtmlManager.BBS_COMMAND = value;
			}
			else if (key.equals("allowVoicedCommands"))
			{
				EventManager.ALLOW_VOICE_COMMANDS = Boolean.parseBoolean(value);
			}
			else if (key.equals("registerVoicedCommand"))
			{
				EventManager.REGISTER_VOICE_COMMAND = value;
			}
			else if (key.equals("unregisterVoicedCommand"))
			{
				EventManager.UNREGISTER_VOICE_COMMAND = value;
			}
		}
		catch (Exception e)
		{
			NexusLoader.debug("Wrong value set for config " + key + " (value = " + value + ")");
		}
		this.saveGlobalConfig(config);
	}
	
	public void saveGlobalConfig(GlobalConfigModel config)
	{
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_globalconfigs VALUES (?,?,?,?,?)");
			statement.setString(1, config.getCategory());
			statement.setString(2, config.getKey());
			statement.setString(3, config.getDesc());
			statement.setString(4, config.getValue());
			statement.setInt(5, config.getInputType());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
		}
	}
	
	public GlobalConfigModel getGlobalConfig(String type, String key)
	{
		if (type == null)
		{
			for (List<GlobalConfigModel> list : this._globalConfigs.values())
			{
				for (GlobalConfigModel gc : list)
				{
					if (!gc.getKey().equals(key))
					{
						continue;
					}
					return gc;
				}
			}
			return null;
		}
		for (GlobalConfigModel gc : this._globalConfigs.get(type))
		{
			if (!gc.getKey().equals(key))
			{
				continue;
			}
			return gc;
		}
		return null;
	}
	
	public static final EventConfig getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventConfig _instance = new EventConfig();
		
		private SingletonHolder()
		{
		}
	}
	
	private class Config
	{
		protected boolean allowed;
		
		public Config(EventType type, boolean allowed)
		{
			this.allowed = allowed;
		}
	}
	
}
