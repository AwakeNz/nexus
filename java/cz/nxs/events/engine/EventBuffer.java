/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 *  javolution.util.FastSet
 */
package cz.nxs.events.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;
import cz.nxs.events.NexusLoader;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;

public class EventBuffer
{
	private final Map<String, Map<Integer, Integer>> _aviableBuffs = new FastMap<>();
	private final FastMap<Integer, Map<String, List<Integer>>> _buffs = new FastMap<>();
	private final Map<Integer, String> _activeSchemes = new FastMap<>();
	private final Map<Integer, String> _activePetSchemes = new FastMap<>();
	private final Map<Integer, List<String>> _modified = new FastMap<>();
	@SuppressWarnings("unused")
	private final DataUpdater _dataUpdater;
	
	public EventBuffer()
	{
		_dataUpdater = new DataUpdater();
		loadAviableBuffs(true);
	}
	
	public void reloadBuffer()
	{
		loadAviableBuffs(false);
	}
	
	public void loadPlayer(PlayerEventInfo player)
	{
		loadData(player.getPlayersId());
	}
	
	public void buffPlayer(PlayerEventInfo player, boolean heal)
	{
		for (int buffId : getBuffs(player))
		{
			player.getSkillEffects(buffId, this.getLevelFor(buffId));
		}
		if (heal)
		{
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}
	
	public void buffPlayer(PlayerEventInfo player)
	{
		for (int buffId : getBuffs(player))
		{
			player.getSkillEffects(buffId, getLevelFor(buffId));
		}
		if (EventConfig.getInstance().getGlobalConfigBoolean("bufferHealsPlayer"))
		{
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
		}
	}
	
	public void buffPet(PlayerEventInfo player)
	{
		if (player.hasPet())
		{
			if (getPlayersCurrentPetScheme(player.getPlayersId()) == null)
			{
				return;
			}
			
			for (int buffId : getBuffs(player, getPlayersCurrentPetScheme(player.getPlayersId())))
			{
				player.getPetSkillEffects(buffId, getLevelFor(buffId));
			}
		}
	}
	
	public void addModifiedBuffs(PlayerEventInfo player, String schemeName)
	{
		if (!_modified.containsKey(player.getPlayersId()))
		{
			_modified.put(player.getPlayersId(), new FastList<>());
		}
		if (!_modified.get(player.getPlayersId()).contains(schemeName))
		{
			_modified.get(player.getPlayersId()).add(schemeName);
		}
	}
	
	public void addModifiedBuffs(int player, String schemeName)
	{
		if (!_modified.containsKey(player))
		{
			_modified.put(player, new FastList<>());
		}
		if (!_modified.get(player).contains(schemeName))
		{
			_modified.get(player).add(schemeName);
		}
	}
	
	public boolean hasBuffs(PlayerEventInfo player)
	{
		try
		{
			return !(_buffs.get(player.getPlayersId())).isEmpty();
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public boolean addScheme(PlayerEventInfo player, String schemeName)
	{
		if (_buffs.get(player.getPlayersId()).containsKey(schemeName))
		{
			return false;
		}
		if (_buffs.get(player.getPlayersId()).size() >= 6)
		{
			
			player.sendMessage("You can't have more than 6 schemes.");
			return false;
		}
		(_buffs.get(player.getPlayersId())).put(schemeName, new FastList<>());
		setPlayersCurrentScheme(player.getPlayersId(), schemeName);
		addModifiedBuffs(player, schemeName);
		return true;
	}
	
	public boolean removeScheme(PlayerEventInfo player, String schemeName)
	{
		if (!(_buffs.get(player.getPlayersId())).containsKey(schemeName))
		{
			return false;
		}
		(_buffs.get(player.getPlayersId())).remove(schemeName);
		if (schemeName.equals(getPlayersCurrentScheme(player.getPlayersId())))
		{
			setPlayersCurrentScheme(player.getPlayersId(), getFirstScheme(player.getPlayersId()));
		}
		addModifiedBuffs(player, schemeName);
		return true;
	}
	
	public String getPlayersCurrentScheme(int player)
	{
		String current = _activeSchemes.get(player);
		if (current == null)
		{
			current = setPlayersCurrentScheme(player, getFirstScheme(player));
		}
		return current;
	}
	
	public String getPlayersCurrentPetScheme(int player)
	{
		String current = _activePetSchemes.get(player);
		return current;
	}
	
	public String setPlayersCurrentScheme(int player, String schemeName)
	{
		return setPlayersCurrentScheme(player, schemeName, true);
	}
	
	public String setPlayersCurrentScheme(int player, String schemeName, boolean updateInDb)
	{
		if (schemeName == null)
		{
			_activeSchemes.remove(player);
			return null;
		}
		if (!(_buffs.get(player)).containsKey(schemeName))
		{
			(_buffs.get(player)).put(schemeName, new FastList<>());
		}
		if (updateInDb)
		{
			if (_activeSchemes.containsKey(player))
			{
				addModifiedBuffs(player, _activeSchemes.get(player));
			}
			addModifiedBuffs(player, schemeName);
		}
		_activeSchemes.put(player, schemeName);
		return schemeName;
	}
	
	public String setPlayersCurrentPetScheme(int player, String schemeName)
	{
		if (schemeName == null)
		{
			_activePetSchemes.remove(player);
			return null;
		}
		if (!(_buffs.get(player)).containsKey(schemeName))
		{
			(_buffs.get(player)).put(schemeName, new FastList<>());
		}
		_activePetSchemes.put(player, schemeName);
		return schemeName;
	}
	
	public String getFirstScheme(int player)
	{
		if (_buffs.containsKey(player))
		{
			for (Map.Entry<String, List<Integer>> e : _buffs.get(player).entrySet())
			{
				return e.getKey();
			}
		}
		return null;
	}
	
	public Set<Map.Entry<String, List<Integer>>> getSchemes(PlayerEventInfo player)
	{
		if (_buffs.containsKey(player.getPlayersId()))
		{
			return (_buffs.get(player.getPlayersId())).entrySet();
		}
		return new FastSet<>();
	}
	
	public boolean addBuff(int buffId, PlayerEventInfo player)
	{
		String scheme = getPlayersCurrentScheme(player.getPlayersId());
		if (scheme == null)
		{
			return false;
		}
		if (!(_buffs.get(player.getPlayersId())).containsKey(scheme))
		{
			return false;
		}
		if (_buffs.get(player.getPlayersId()).get(scheme).contains(buffId))
		{
			return false;
		}
		_buffs.get(player.getPlayersId()).get(scheme).add(buffId);
		addModifiedBuffs(player, scheme);
		return true;
	}
	
	public void removeBuff(int buffId, PlayerEventInfo player)
	{
		String scheme = getPlayersCurrentScheme(player.getPlayersId());
		if (scheme == null)
		{
			return;
		}
		_buffs.get(player.getPlayersId()).get(scheme).remove(new Integer(buffId));
		addModifiedBuffs(player, scheme);
	}
	
	public boolean containsSkill(int buffId, PlayerEventInfo player)
	{
		String scheme = getPlayersCurrentScheme(player.getPlayersId());
		if (scheme == null)
		{
			return false;
		}
		return _buffs.get(player.getPlayersId()).get(scheme).contains(buffId);
	}
	
	public List<Integer> getBuffs(PlayerEventInfo player)
	{
		String scheme = getPlayersCurrentScheme(player.getPlayersId());
		if (scheme == null)
		{
			return new FastList<>();
		}
		return _buffs.get(player.getPlayersId()).get(scheme);
	}
	
	public List<Integer> getBuffs(PlayerEventInfo player, String scheme)
	{
		return _buffs.get(player.getPlayersId()).get(scheme);
	}
	
	@SuppressWarnings("null")
	private void loadData(int playerId)
	{
		FastMap<Integer, Map<String, List<Integer>>> fastMap = _buffs;
		synchronized (fastMap)
		{
			_buffs.put(playerId, new FastMap<>());
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = CallBack.getInstance().getOut().getConnection();
				statement = con.prepareStatement("SELECT * FROM nexus_playerbuffs WHERE playerId = " + playerId);
				ResultSet rset = statement.executeQuery();
				while (rset.next())
				{
					String scheme = rset.getString("scheme");
					int active = rset.getInt("active");
					(_buffs.get(playerId)).put(scheme, new FastList<>());
					for (String buffId : rset.getString("buffs").split(","))
					{
						try
						{
							(_buffs.get(playerId)).get(scheme).add(Integer.parseInt(buffId));
							continue;
						}
						catch (Exception e)
						{
							// empty catch block
						}
					}
					if (active != 1)
					{
						continue;
					}
					setPlayersCurrentScheme(playerId, scheme, false);
				}
				rset.close();
				statement.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				try
				{
					con.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@SuppressWarnings("null")
	protected synchronized void storeData()
	{
		if (_modified.isEmpty())
		{
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			for (Map.Entry<Integer, List<String>> modified : _modified.entrySet())
			{
				for (String modifiedScheme : modified.getValue())
				{
					statement = con.prepareStatement("DELETE FROM nexus_playerbuffs WHERE playerId = " + modified.getKey() + " AND scheme = '" + modifiedScheme + "'");
					statement.execute();
					if (!_buffs.get(modified.getKey()).containsKey(modifiedScheme))
					{
						continue;
					}
					TextBuilder tb = new TextBuilder();
					for (int buffId : _buffs.get(modified.getKey()).get(modifiedScheme))
					{
						tb.append("" + buffId + ",");
					}
					String buffs = tb.toString();
					if (buffs.length() > 0)
					{
						buffs = buffs.substring(0, buffs.length() - 1);
					}
					statement = con.prepareStatement("REPLACE INTO nexus_playerbuffs VALUES (?,?,?,?)");
					statement.setInt(1, modified.getKey());
					statement.setString(2, modifiedScheme);
					statement.setString(3, buffs);
					statement.setInt(4, modifiedScheme.equals(getPlayersCurrentScheme(modified.getKey())) ? 1 : 0);
					statement.executeUpdate();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		_modified.clear();
	}
	
	public Map<String, Map<Integer, Integer>> getAviableBuffs()
	{
		return _aviableBuffs;
	}
	
	public int getLevelFor(int skillId)
	{
		for (Map<Integer, Integer> e : _aviableBuffs.values())
		{
			for (Map.Entry<Integer, Integer> entry : e.entrySet())
			{
				if (entry.getKey() != skillId)
				{
					continue;
				}
				return entry.getValue();
			}
		}
		return -1;
	}
	
	@SuppressWarnings("null")
	private void loadAviableBuffs(boolean test)
	{
		if (!_aviableBuffs.isEmpty())
		{
			_aviableBuffs.clear();
		}
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			statement = con.prepareStatement("SELECT * FROM nexus_buffs");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				String name;
				String category = rset.getString("category");
				int buffId = rset.getInt("buffId");
				int level = rset.getInt("level");
				if (test && (((name = rset.getString("name")) == null) || (name.length() == 0)))
				{
					try
					{
						name = new SkillData(buffId, level).getName();
						if (name != null)
						{
							PreparedStatement statement2 = con.prepareStatement("UPDATE nexus_buffs SET name = '" + name + "' WHERE buffId = " + buffId + " AND level = " + level + "");
							statement2.execute();
							statement2.close();
						}
					}
					catch (Exception e)
					{
						// empty catch block
					}
				}
				if (!_aviableBuffs.containsKey(category))
				{
					_aviableBuffs.put(category, new FastMap<>());
				}
				_aviableBuffs.get(category).put(buffId, level);
				++count;
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		NexusLoader.debug("Loaded " + count + " buffs for Event Buffer.", Level.INFO);
	}
	
	public static final EventBuffer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventBuffer _instance = new EventBuffer();
		
		private SingletonHolder()
		{
		}
	}
	
	private class DataUpdater implements Runnable
	{
		protected DataUpdater()
		{
			CallBack.getInstance().getOut().scheduleGeneralAtFixedRate(this, 10000, 10000);
		}
		
		@Override
		public void run()
		{
			storeData();
		}
	}
	
}
