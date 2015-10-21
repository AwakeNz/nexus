/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastMap;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;

public class EventWarnings
{
	protected Map<Integer, Integer> _warnings = new FastMap<>();
	private ScheduledFuture<?> _decTask = null;
	public static int MAX_WARNINGS = 3;
	
	public EventWarnings()
	{
		new SaveScheduler();
		loadData();
		decreasePointsTask();
		NexusLoader.debug("Nexus Engine: Loaded EventWarnings engine.");
	}
	
	protected void decreasePointsTask()
	{
		if (_decTask != null)
		{
			_decTask.cancel(false);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(11, 23);
		cal.set(12, 59);
		cal.set(13, 30);
		long delay = cal.getTimeInMillis() - System.currentTimeMillis();
		_decTask = CallBack.getInstance().getOut().scheduleGeneral(() ->
		{
			for (int id : _warnings.keySet())
			{
				decreasePoints(id, 1);
				PlayerEventInfo pi = CallBack.getInstance().getOut().getPlayer(id);
				// if (pi == null)
				{
					pi.sendMessage(LanguageEngine.getMsg("system_warningsDecreased", getPoints(id)));
				}
			}
			saveData();
			decreasePointsTask();
		}, delay);
	}
	
	public int getPoints(PlayerEventInfo player)
	{
		if (player == null)
		{
			return -1;
		}
		return _warnings.containsKey(player.getPlayersId()) ? _warnings.get(player.getPlayersId()) : 0;
	}
	
	public int getPoints(int player)
	{
		return _warnings.containsKey(player) ? _warnings.get(player) : 0;
	}
	
	public void addWarning(PlayerEventInfo player, int ammount)
	{
		if (player == null)
		{
			return;
		}
		addPoints(player.getPlayersId(), ammount);
		if (ammount > 0)
		{
			player.sendMessage(LanguageEngine.getMsg("system_warning", MAX_WARNINGS - getPoints(player)));
		}
	}
	
	public void addPoints(int player, int ammount)
	{
		int points = 0;
		if (_warnings.containsKey(player))
		{
			points = _warnings.get(player);
		}
		if ((points += ammount) < 0)
		{
			points = 0;
		}
		if (points > 0)
		{
			_warnings.put(player, points);
		}
		else
		{
			_warnings.remove(player);
		}
	}
	
	public void removeWarning(PlayerEventInfo player, int ammount)
	{
		addWarning(player, -ammount);
	}
	
	public void decreasePoints(int player, int ammount)
	{
		addPoints(player, -ammount);
	}
	
	private void loadData()
	{
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT id, points FROM nexus_warnings");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				_warnings.put(rset.getInt("id"), rset.getInt("points"));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void saveData()
	{
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_warnings");
			statement.execute();
			statement.close();
			for (Map.Entry<Integer, Integer> e : _warnings.entrySet())
			{
				statement = con.prepareStatement("INSERT INTO nexus_warnings VALUES (" + e.getKey() + "," + e.getValue() + ")");
				statement.execute();
				statement.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static final EventWarnings getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventWarnings _instance = new EventWarnings();
		
		private SingletonHolder()
		{
		}
	}
	
	private class SaveScheduler implements Runnable
	{
		public SaveScheduler()
		{
			schedule();
		}
		
		private void schedule()
		{
			CallBack.getInstance().getOut().scheduleGeneral(this, 1800000);
		}
		
		@Override
		public void run()
		{
			saveData();
			schedule();
		}
	}
	
}
