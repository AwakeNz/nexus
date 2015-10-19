/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.ItemData
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import javolution.util.FastList;
import javolution.util.FastMap;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;

public class EventRewardSystem
{
	private final Map<EventType, FastMap<Integer, EventRewards>> _rewards = new FastMap<>();
	private int count = 0;
	private int notEnoughtScore = 0;
	
	public EventRewardSystem()
	{
		for (EventType t : EventType.values())
		{
			_rewards.put(t, new FastMap<>());
		}
		loadRewards();
	}
	
	private EventType getType(String s)
	{
		for (EventType t : EventType.values())
		{
			if (!t.getAltTitle().equalsIgnoreCase(s))
			{
				continue;
			}
			return t;
		}
		return null;
	}
	
	public EventRewards getAllRewardsFor(EventType event, int modeId)
	{
		if (_rewards.get(event).get(modeId) == null)
		{
			_rewards.get(event).put(modeId, new EventRewards());
		}
		EventRewards er = _rewards.get(event).get(modeId);
		return er;
	}
	
	public void loadRewards()
	{
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT eventType, modeId, position, parameter, item_id, min, max, chance FROM nexus_rewards");
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				EventRewards rewards = null;
				EventType type = getType(rset.getString("eventType"));
				int modeId = rset.getInt("modeId");
				if (!_rewards.get(type).containsKey(modeId))
				{
					rewards = new EventRewards();
					_rewards.get(type).put(modeId, rewards);
				}
				else
				{
					rewards = _rewards.get(type).get(modeId);
				}
				rewards.addItem(RewardPosition.getPosition(rset.getString("position")), rset.getString("parameter"), rset.getInt("item_id"), rset.getInt("min"), rset.getInt("max"), rset.getInt("chance"));
			}
			rset.close();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		NexusLoader.debug("Nexus Engine: Reward System Loaded.");
	}
	
	public int addRewardToDb(EventType type, RewardPosition position, String parameter, int modeId, int id, int minAmmount, int maxAmmount, int chance, boolean updateOnly)
	{
		if (_rewards.get(type).get(modeId) == null)
		{
			_rewards.get(type).put(modeId, new EventRewards());
		}
		EventRewards rewards = _rewards.get(type).get(modeId);
		int newId = 0;
		if (!updateOnly)
		{
			newId = rewards.addItem(position, parameter, id, minAmmount, maxAmmount, chance);
		}
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_rewards VALUES (?,?,?,?,?,?,?,?)");
			statement.setString(1, type.getAltTitle());
			statement.setInt(2, modeId);
			statement.setString(3, position.toString());
			statement.setString(4, parameter == null ? "" : parameter);
			statement.setInt(5, id);
			statement.setInt(6, minAmmount);
			statement.setInt(7, maxAmmount);
			statement.setInt(8, chance);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return newId;
	}
	
	public int createReward(EventType type, RewardPosition position, String parameter, int modeId)
	{
		return addRewardToDb(type, position, parameter, modeId, 57, 1, 1, 100, false);
	}
	
	public boolean setPositionRewarded(EventType type, int modeId, RewardPosition position, String parameter)
	{
		if (_rewards.get(type).get(modeId) == null)
		{
			return false;
		}
		if (_rewards.get(type).get(modeId).getContainer(position, parameter) != null)
		{
			return false;
		}
		_rewards.get(type).get(modeId).getOrCreateContainer(position, parameter);
		return true;
	}
	
	public boolean removePositionRewarded(EventType type, int modeId, RewardPosition position, String parameter)
	{
		if (_rewards.get(type).get(modeId) == null)
		{
			return false;
		}
		if (_rewards.get(type).get(modeId).getContainer(position, parameter) == null)
		{
			return false;
		}
		PositionContainer container = _rewards.get(type).get(modeId).getContainer(position, parameter);
		Map<Integer, RewardItem> map = _rewards.get(type).get(modeId).getAllRewards().get(container);
		for (Map.Entry<Integer, RewardItem> e : map.entrySet())
		{
			removeRewardFromDb(type, e.getKey(), modeId);
		}
		_rewards.get(type).get(modeId).getAllRewards().remove(container);
		return true;
	}
	
	public void updateRewardInDb(EventType type, int rewardId, int modeId)
	{
		RewardItem item;
		if (_rewards.get(type).get(modeId) == null)
		{
			_rewards.get(type).put(modeId, new EventRewards());
		}
		if ((item = (_rewards.get(type).get(modeId)).getItem(rewardId)) == null)
		{
			return;
		}
		PositionContainer position = this.getRewardPosition(type, modeId, rewardId);
		addRewardToDb(type, position.position, position.parameter, modeId, item.id, item.minAmmount, item.maxAmmount, item.chance, true);
	}
	
	public void removeFromDb(EventType type, RewardPosition position, String parameter, int modeId, int itemId, int min, int max, int chance)
	{
		try
		{
			Connection con = CallBack.getInstance().getOut().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_rewards WHERE eventType = '" + type.getAltTitle() + "' AND position = '" + position.toString() + "' AND parameter = '" + (parameter == null ? "" : parameter) + "' AND modeId = " + modeId + " AND item_id = " + itemId + " AND min = " + min + " AND max = " + max + " AND chance = " + chance);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void removeRewardFromDb(EventType type, int rewardId, int modeId)
	{
		PositionContainer container = this.getRewardPosition(type, modeId, rewardId);
		if (_rewards.get(type).get(modeId) == null)
		{
			_rewards.get(type).put(modeId, new EventRewards());
		}
		EventRewards rewards = this._rewards.get(type).get(modeId);
		RewardItem item = rewards.getItem(rewardId);
		rewards.removeItem(container.position, container.parameter, rewardId);
		removeFromDb(type, container.position, container.parameter, modeId, item.id, item.minAmmount, item.maxAmmount, item.chance);
	}
	
	public Map<Integer, RewardItem> getRewards(EventType type, int modeId, RewardPosition position, String parameter)
	{
		Map<Integer, RewardItem> map;
		if (_rewards.get(type).get(modeId) == null)
		{
			_rewards.get(type).put(modeId, new EventRewards());
		}
		if ((map = _rewards.get(type).get(modeId).getRewards(position, parameter)) != null)
		{
			return map;
		}
		return new FastMap<>();
	}
	
	public RewardItem getReward(EventType type, int modeId, int rewardId)
	{
		if (_rewards.get(type).get(modeId) == null)
		{
			_rewards.get(type).put(modeId, new EventRewards());
		}
		return _rewards.get(type).get(modeId).getItem(rewardId);
	}
	
	public PositionContainer getRewardPosition(EventType type, int modeId, int rewardId)
	{
		if (_rewards.get(type).get(modeId) == null)
		{
			_rewards.get(type).put(modeId, new EventRewards());
		}
		for (Entry<PositionContainer, Map<Integer, RewardItem>> e : _rewards.get(type).get(modeId)._rewards.entrySet())
		{
			for (int i : e.getValue().keySet())
			{
				if (i != rewardId)
				{
					e.getKey();
				}
				
			}
		}
		return new PositionContainer(RewardPosition.None, null);
	}
	
	public Map<Integer, List<EventTeam>> rewardTeams(Map<EventTeam, Integer> teams, EventType event, int modeId, int minScore, int halfRewardAfkTime, int noRewardAfkTime)
	{
		int score;
		count = 0;
		notEnoughtScore = 0;
		int totalCount = teams.size();
		FastMap<Integer, List<EventTeam>> scores = new FastMap<>();
		for (Map.Entry<EventTeam, Integer> e : teams.entrySet())
		{
			EventTeam team = e.getKey();
			score = e.getValue();
			if (!scores.containsKey(score))
			{
				scores.put(score, new FastList<>());
			}
			scores.get(score).add(team);
		}
		int position = 1;
		for (Entry<Integer, List<EventTeam>> e2 : scores.entrySet())
		{
			PositionContainer temp;
			score = e2.getKey();
			int count = e2.getValue().size();
			if (position == 1)
			{
				if (count == 1)
				{
					temp = existsReward(event, modeId, RewardPosition.Numbered, "1");
					if (temp != null)
					{
						giveRewardsToTeams(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
					}
					else
					{
						temp = existsRangeReward(event, modeId, position);
						if (temp != null)
						{
							giveRewardsToTeams(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
						}
						else
						{
							temp = existsReward(event, modeId, RewardPosition.Winner, null);
							if (temp != null)
							{
								giveRewardsToTeams(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
							}
						}
					}
				}
				else if (totalCount > count)
				{
					temp = existsReward(event, modeId, RewardPosition.Numbered, "1");
					if (temp != null)
					{
						giveRewardsToTeams(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
					}
					else
					{
						temp = existsRangeReward(event, modeId, position);
						if (temp != null)
						{
							giveRewardsToTeams(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
						}
						else
						{
							temp = existsReward(event, modeId, RewardPosition.Winner, null);
							if (temp != null)
							{
								giveRewardsToTeams(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
							}
						}
					}
				}
				else
				{
					temp = existsReward(event, modeId, RewardPosition.Tie, null);
					if (temp != null)
					{
						giveRewardsToTeams(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
					}
				}
			}
			else
			{
				temp = existsReward(event, modeId, RewardPosition.Numbered, String.valueOf(position));
				if (temp != null)
				{
					giveRewardsToTeams(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
				}
				else
				{
					temp = existsRangeReward(event, modeId, position);
					if (temp != null)
					{
						giveRewardsToTeams(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
					}
					else
					{
						temp = existsReward(event, modeId, RewardPosition.Looser, null);
						if (temp != null)
						{
							giveRewardsToTeams(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
						}
					}
				}
			}
			++position;
		}
		try
		{
			AbstractMainEvent ev;
			if (event.isRegularEvent() && ((ev = EventManager.getInstance().getMainEvent(event)) != null))
			{
				dump(ev.getPlayers(0).size());
			}
		}
		catch (Exception e2)
		{
			// empty catch block
		}
		return scores;
	}
	
	private void dump(int total)
	{
		NexusLoader.debug("" + total + " was the count of players in the event.");
		NexusLoader.debug("" + count + " players were rewarded.");
		NexusLoader.debug("" + notEnoughtScore + " players were not rewarded because they didn't have enought score.");
		if (NexusLoader.detailedDebug)
		{
			NexusLoader.detailedDebug("" + total + " was the count of players in the event.");
		}
		if (NexusLoader.detailedDebug)
		{
			NexusLoader.detailedDebug("" + count + " players were rewarded.");
		}
		if (NexusLoader.detailedDebug)
		{
			NexusLoader.detailedDebug("" + notEnoughtScore + " players were not rewarded because they didn't have enought score.");
		}
	}
	
	public Map<Integer, List<PlayerEventInfo>> rewardPlayers(Map<PlayerEventInfo, Integer> players, EventType event, int modeId, int minScore, int halfRewardAfkTime, int noRewardAfkTime)
	{
		int score;
		count = 0;
		notEnoughtScore = 0;
		int totalCount = players.size();
		FastMap<Integer, List<PlayerEventInfo>> scores = new FastMap<>();
		for (Map.Entry<PlayerEventInfo, Integer> e : players.entrySet())
		{
			PlayerEventInfo player = e.getKey();
			score = e.getValue();
			if (!scores.containsKey(score))
			{
				scores.put(score, new FastList<>());
			}
			scores.get(score).add(player);
		}
		int position = 1;
		for (Entry<Integer, List<PlayerEventInfo>> e2 : scores.entrySet())
		{
			PositionContainer temp;
			score = e2.getKey();
			int count = e2.getValue().size();
			if (position == 1)
			{
				if (count == 1)
				{
					temp = existsReward(event, modeId, RewardPosition.Numbered, "1");
					if (temp != null)
					{
						giveRewardsToPlayers(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
					}
					else
					{
						temp = existsRangeReward(event, modeId, position);
						if (temp != null)
						{
							giveRewardsToPlayers(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
						}
						else
						{
							temp = existsReward(event, modeId, RewardPosition.Winner, null);
							if (temp != null)
							{
								giveRewardsToPlayers(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
							}
						}
					}
				}
				else if (totalCount > count)
				{
					temp = existsReward(event, modeId, RewardPosition.Numbered, "1");
					if (temp != null)
					{
						giveRewardsToPlayers(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
					}
					else
					{
						temp = existsRangeReward(event, modeId, position);
						if (temp != null)
						{
							giveRewardsToPlayers(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
						}
						else
						{
							temp = existsReward(event, modeId, RewardPosition.Winner, null);
							if (temp != null)
							{
								giveRewardsToPlayers(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
							}
						}
					}
				}
				else
				{
					temp = existsReward(event, modeId, RewardPosition.Tie, null);
					if (temp != null)
					{
						giveRewardsToPlayers(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
					}
				}
			}
			else
			{
				temp = existsReward(event, modeId, RewardPosition.Numbered, String.valueOf(position));
				if (temp != null)
				{
					giveRewardsToPlayers(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
				}
				else
				{
					temp = existsRangeReward(event, modeId, position);
					if (temp != null)
					{
						giveRewardsToPlayers(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
					}
					else
					{
						temp = existsReward(event, modeId, RewardPosition.Looser, null);
						if (temp != null)
						{
							giveRewardsToPlayers(temp, e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
						}
					}
				}
			}
			++position;
		}
		return scores;
	}
	
	private void giveRewardsToPlayers(PositionContainer container, List<PlayerEventInfo> players, EventType event, int modeId, int minScore, int halfRewardAfkTime, int noRewardAfkTime)
	{
		for (PlayerEventInfo player : players)
		{
			if (player.isOnline())
			{
				if (player.getEventData().getScore() >= minScore)
				{
					++count;
					rewardPlayer(event, modeId, player, container.position, container.parameter, player.getTotalTimeAfk(), halfRewardAfkTime, noRewardAfkTime);
					continue;
				}
				++notEnoughtScore;
				if ((minScore <= 0) || (player.getScore() >= minScore))
				{
					continue;
				}
				player.sendMessage(LanguageEngine.getMsg("event_notEnoughtScore", minScore));
				continue;
			}
			NexusLoader.debug("trying to reward player " + player.getPlayersName() + " (player) which is not online()", Level.WARNING);
		}
	}
	
	private void giveRewardsToTeams(PositionContainer container, List<EventTeam> teams, EventType event, int modeId, int minScore, int halfRewardAfkTime, int noRewardAfkTime)
	{
		for (EventTeam team : teams)
		{
			for (PlayerEventInfo player : team.getPlayers())
			{
				if (player.isOnline())
				{
					if (player.getEventData().getScore() >= minScore)
					{
						++count;
						rewardPlayer(event, modeId, player, container.position, container.parameter, player.getTotalTimeAfk(), halfRewardAfkTime, noRewardAfkTime);
						continue;
					}
					++notEnoughtScore;
					if ((minScore <= 0) || (player.getScore() >= minScore))
					{
						continue;
					}
					player.sendMessage(LanguageEngine.getMsg("event_notEnoughtScore", minScore));
					continue;
				}
				NexusLoader.debug("trying to reward player " + player.getPlayersName() + " (team) which is not online()", Level.WARNING);
			}
		}
	}
	
	private PositionContainer existsReward(EventType event, int modeId, RewardPosition pos, String parameter)
	{
		if (_rewards.get(event).get(modeId) == null)
		{
			return null;
		}
		PositionContainer c = _rewards.get(event).get(modeId).getContainer(pos, parameter);
		if ((c == null) || _rewards.get(event).get(modeId).getAllRewards().get(c).isEmpty())
		{
			return null;
		}
		return c;
	}
	
	private PositionContainer existsRangeReward(EventType event, int modeId, int position)
	{
		if (_rewards.get(event).get(modeId) == null)
		{
			return null;
		}
		for (Map.Entry<PositionContainer, Map<Integer, RewardItem>> e : _rewards.get(event).get(modeId).getAllRewards().entrySet())
		{
			if ((e.getValue() == null) || e.getValue().isEmpty() || (e.getKey().position.posType == null) || (e.getKey().position.posType != RewardPosition.PositionType.Range))
			{
				continue;
			}
			int from = Integer.parseInt(e.getKey().parameter.split("-")[0]);
			int to = Integer.parseInt(e.getKey().parameter.split("-")[1]);
			if ((position < from) || (position > to))
			{
				continue;
			}
			return e.getKey();
		}
		return null;
	}
	
	public boolean rewardPlayer(EventType event, int modeId, PlayerEventInfo player, RewardPosition position, String parameter, int afkTime, int halfRewardAfkTime, int noRewardAfkTime)
	{
		if (player == null)
		{
			return false;
		}
		if (_rewards.get(event).get(modeId) == null)
		{
			_rewards.get(event).put(modeId, new EventRewards());
		}
		if (_rewards.get(event).get(modeId).getRewards(position, parameter) == null)
		{
			return false;
		}
		if ((noRewardAfkTime > 0) && (afkTime >= noRewardAfkTime))
		{
			player.sendMessage("You receive no reward because you were afk too much.");
			return false;
		}
		if ((halfRewardAfkTime > 0) && (afkTime >= halfRewardAfkTime))
		{
			player.sendMessage("You receive half reward because you were afk too much.");
		}
		boolean given = false;
		for (RewardItem item : _rewards.get(event).get(modeId).getRewards(position, parameter).values())
		{
			int ammount = item.getAmmount(player);
			if (ammount <= 0)
			{
				continue;
			}
			if ((ammount > 1) && (halfRewardAfkTime > 0) && (afkTime >= halfRewardAfkTime))
			{
				ammount /= 2;
			}
			if (item.id == -1)
			{
				player.addExpAndSp(ammount, 0);
			}
			else if (item.id == -2)
			{
				player.addExpAndSp(0, ammount);
			}
			else if (item.id == -3)
			{
				player.setFame(player.getFame() + ammount);
			}
			else
			{
				player.addItem(item.id, ammount, true);
			}
			given = true;
		}
		return given;
	}
	
	public static final EventRewardSystem getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventRewardSystem _instance = new EventRewardSystem();
		
		private SingletonHolder()
		{
		}
	}
	
	public class RewardItem
	{
		public int id;
		public int minAmmount;
		public int maxAmmount;
		public int chance;
		public int pvpRequired;
		public int levelRequired;
		
		public RewardItem(int id, int minAmmount, int maxAmmount, int chance, int pvpRequired, int levelRequired)
		{
			this.id = id;
			this.minAmmount = minAmmount;
			this.maxAmmount = maxAmmount;
			this.chance = chance;
			this.pvpRequired = pvpRequired;
			this.levelRequired = levelRequired;
		}
		
		public int getAmmount(PlayerEventInfo player)
		{
			if (CallBack.getInstance().getOut().random(100) < chance)
			{
				return CallBack.getInstance().getOut().random(minAmmount, maxAmmount);
			}
			NexusLoader.debug("chance check for reward failed for player " + player.getPlayersName() + ", reward item " + id);
			return 0;
		}
	}
	
	public class EventRewards
	{
		private int _lastId;
		protected final Map<PositionContainer, Map<Integer, RewardItem>> _rewards;
		
		public EventRewards()
		{
			_lastId = 0;
			_rewards = new FastMap<>();
		}
		
		public PositionContainer getOrCreateContainer(RewardPosition position, String posParameter)
		{
			PositionContainer container = null;
			container = getContainer(position, posParameter);
			if (container == null)
			{
				container = new PositionContainer(position, posParameter);
			}
			if (!_rewards.containsKey(container))
			{
				_rewards.put(container, new FastMap<>());
			}
			return container;
		}
		
		public int addItem(RewardPosition position, String posParameter, int id, int minAmmount, int maxAmmount, int chance)
		{
			if (position == null)
			{
				NexusLoader.debug("Null RewardPosition for item ID " + id + ", minAmmount " + minAmmount + " maxAmmount " + maxAmmount + " chance " + chance, Level.WARNING);
				return _lastId++;
			}
			if ("".equals(posParameter))
			{
				posParameter = null;
			}
			PositionContainer container = getOrCreateContainer(position, posParameter);
			++_lastId;
			RewardItem item = new RewardItem(id, minAmmount, maxAmmount, chance, 0, 0);
			_rewards.get(container).put(_lastId, item);
			return _lastId;
		}
		
		public PositionContainer getContainer(RewardPosition position, String parameter)
		{
			for (PositionContainer ps : _rewards.keySet())
			{
				if ((ps.position == null) || !ps.position.toString().equals(position.toString()) || ((parameter != null) && !parameter.equals("null") && !parameter.equals(ps.parameter)))
				{
					continue;
				}
				return ps;
			}
			return null;
		}
		
		public void removeItem(RewardPosition position, String parameter, int rewardId)
		{
			PositionContainer ps = getContainer(position, parameter);
			if ((ps != null) && _rewards.containsKey(ps))
			{
				_rewards.get(ps).remove(rewardId);
			}
		}
		
		public Map<Integer, RewardItem> getRewards(RewardPosition position, String parameter)
		{
			PositionContainer ps = getContainer(position, parameter);
			if (ps != null)
			{
				return _rewards.get(ps);
			}
			return null;
		}
		
		public Map<PositionContainer, Map<Integer, RewardItem>> getAllRewards()
		{
			return _rewards;
		}
		
		public RewardItem getItem(int rewardId)
		{
			for (Map<Integer, RewardItem> i : _rewards.values())
			{
				for (Map.Entry<Integer, RewardItem> e : i.entrySet())
				{
					if (e.getKey() != rewardId)
					{
						continue;
					}
					return e.getValue();
				}
			}
			return null;
		}
	}
	
	public class PositionContainer
	{
		public RewardPosition position;
		public String parameter;
		public boolean rewarded;
		
		PositionContainer(RewardPosition position, String parameter)
		{
			this.position = position;
			this.parameter = parameter;
		}
		
		public void setRewarded(boolean b)
		{
			rewarded = b;
		}
		
		public boolean isRewarded()
		{
			return rewarded;
		}
	}
	
}
