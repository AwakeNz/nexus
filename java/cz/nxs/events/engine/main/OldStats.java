package cz.nxs.events.engine.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import javolution.text.TextBuilder;
import javolution.util.FastMap;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.ShowBoardData;
import cz.nxs.l2j.CallBack;

public class OldStats
{
	private final FastMap<Integer, FastMap<Integer, StatModell>> stats;
	public FastMap<Integer, int[]> tempTable;
	private final FastMap<Integer, ShowBoardData> htmls;
	private final FastMap<Integer, int[]> statSums;
	
	private static class SingletonHolder
	{
		public static final OldStats _instance = new OldStats();
	}
	
	public class StatModell
	{
		public int num;
		public int wins;
		public int losses;
		public int kills;
		public int deaths;
		public int scores;
		
		public StatModell(int num, int wins, int losses, int kills, int deaths, int scores)
		{
			this.num = num;
			this.wins = wins;
			this.losses = losses;
			this.kills = kills;
			this.deaths = deaths;
			this.scores = scores;
		}
	}
	
	public static OldStats getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private final boolean enabled = false;
	
	protected OldStats()
	{
		this.stats = new FastMap<>();
		this.tempTable = new FastMap<>();
		this.htmls = new FastMap<>();
		this.statSums = new FastMap<>();
		loadSQL();
	}
	
	protected void applyChanges()
	{
		if (!this.enabled)
		{
			return;
		}
		int eventId = EventManager.getInstance().getCurrentMainEvent().getEventType().getMainEventId();
		for (PlayerEventInfo player : EventManager.getInstance().getCurrentMainEvent().getPlayers(0))
		{
			int playerId = player.getPlayersId();
			if (!this.stats.containsKey(Integer.valueOf(playerId)))
			{
				this.stats.put(Integer.valueOf(playerId), new FastMap<>());
			}
			if (!(this.stats.get(Integer.valueOf(playerId))).containsKey(Integer.valueOf(eventId)))
			{
				(this.stats.get(Integer.valueOf(playerId))).put(Integer.valueOf(eventId), new StatModell(0, 0, 0, 0, 0, 0));
			}
			if (this.tempTable.get(Integer.valueOf(playerId))[0] == 1)
			{
				(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).wins = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).wins + 1);
			}
			else
			{
				(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).losses = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).losses + 1);
			}
			(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).num = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).num + 1);
			(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).kills = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).kills + this.tempTable.get(Integer.valueOf(playerId))[1]);
			(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).deaths = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).deaths + this.tempTable.get(Integer.valueOf(playerId))[2]);
			(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).scores = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).scores + this.tempTable.get(Integer.valueOf(playerId))[3]);
		}
		NexusLoader.debug("applyChanges finished");
	}
	
	public void applyMiniEventStatsChanges(int eventId, FastMap<Integer, int[]> statsTable)
	{
		if (!this.enabled)
		{
			return;
		}
		for (Map.Entry<Integer, int[]> e : statsTable.entrySet())
		{
			int playerId = e.getKey().intValue();
			if (!this.stats.containsKey(Integer.valueOf(playerId)))
			{
				this.stats.put(Integer.valueOf(playerId), new FastMap<>());
			}
			if (!(this.stats.get(Integer.valueOf(playerId))).containsKey(Integer.valueOf(eventId)))
			{
				(this.stats.get(Integer.valueOf(playerId))).put(Integer.valueOf(eventId), new StatModell(0, 0, 0, 0, 0, 0));
			}
			if (statsTable.get(Integer.valueOf(playerId))[0] != -1)
			{
				if (statsTable.get(Integer.valueOf(playerId))[0] == 1)
				{
					(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).wins = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).wins + 1);
				}
				else
				{
					(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).losses = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).losses + 1);
				}
			}
			(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).num = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).num + 1);
			(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).kills = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).kills + statsTable.get(Integer.valueOf(playerId))[1]);
			(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).deaths = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).deaths + statsTable.get(Integer.valueOf(playerId))[2]);
			(this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).scores = ((this.stats.get(Integer.valueOf(playerId))).get(Integer.valueOf(eventId)).scores + statsTable.get(Integer.valueOf(playerId))[3]);
		}
		NexusLoader.debug("applyChanges finished for mini events");
	}
	
	@SuppressWarnings("null")
	private void createHtmls()
	{
		this.htmls.clear();
		TextBuilder sb = new TextBuilder();
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			statement = con.prepareStatement("SELECT characters.char_name, nexus_stats_full.* FROM nexus_stats_full INNER JOIN characters ON characters.charId = nexus_stats_full.player ORDER BY nexus_stats_full.wins DESC");
			ResultSet rset = statement.executeQuery();
			rset.last();
			int size = rset.getRow();
			rset.beforeFirst();
			int count = 0;
			while (rset.next())
			{
				count++;
				if ((count % 10) == 1)
				{
					sb.append("<html><body><br><br><center><table width=150><tr><td width=50><center>" + (((count - 1) / 10) != 0 ? "<a action=\"bypass -h eventstats " + ((count - 1) / 10) + "\">Prev</a>" : "Prev") + "</td><td width=50><center>" + (((count - 1) / 10) + 1) + "</td><td width=50><center>" + (((count - 1) / 10) != (size / 10) ? "<a action=\"bypass -h eventstats " + (((count - 1) / 10) + 2) + "\">Next</a>" : "Next") + "</td></tr></table><br><br><center><table width=700 bgcolor=5A5A5A><tr><td width=30><center>Rank</td><td width=100><center>Name</td><td width=65><center>Events</td><td width=65><center>Win%</td><td width=65><center>K:D</td><td width=65><center>Wins</td><td width=65><center>Losses</td><td width=65><center>Kills</td><td width=65><center>Deaths</td><td width=100><center>Favourite Event</td></tr></table><br>" + "<center><table width=720>");
				}
				sb.append("<tr><td width=30><center>" + count + ".</td><td width=100><a action=\"bypass -h eventstats_show " + rset.getInt("player") + "\">" + rset.getString("char_name") + "</a></td><td width=65><center>" + rset.getInt("num") + "</td><td width=65><center>" + rset.getDouble("winpercent") + "%</td><td width=65><center>" + rset.getDouble("kdratio") + "</td><td width=65><center>" + rset.getInt("wins") + "</td><td width=65><center>" + rset.getInt("losses") + "</td><td width=65><center>" + rset.getInt("kills") + "</td>" + "<td width=65><center>" + rset.getInt("deaths") + "</td><td width=120><center>" + EventType.getEventByMainId(rset.getInt("favevent")).getHtmlTitle() + "</td></tr>");
				if ((count % 10) == 0)
				{
					sb.append("</table></body></html>");
					this.htmls.put(Integer.valueOf(count / 10), new ShowBoardData(sb.toString(), "101"));
					sb.clear();
				}
			}
			if (((count % 10) != 0) && (!this.htmls.containsKey(Integer.valueOf((count / 10) + 1))))
			{
				sb.append("</table></body></html>");
				this.htmls.put(Integer.valueOf((count / 10) + 1), new ShowBoardData(sb.toString(), "101"));
				sb.clear();
			}
			rset.close();
			statement.close();
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			NexusLoader.debug("createHtmls finished");
		}
		catch (Exception e)
		{
			System.out.println("create SQL exception.");
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
	
	@SuppressWarnings("null")
	private void loadSQL()
	{
		if (!this.enabled)
		{
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = CallBack.getInstance().getOut().getConnection();
			statement = con.prepareStatement("SELECT * FROM nexus_stats");
			ResultSet rset = statement.executeQuery();
			int count = 0;
			while (rset.next())
			{
				count++;
				if (!this.stats.containsKey(Integer.valueOf(rset.getInt("player"))))
				{
					this.stats.put(Integer.valueOf(rset.getInt("player")), new FastMap<>());
				}
				(this.stats.get(Integer.valueOf(rset.getInt("player")))).put(Integer.valueOf(rset.getInt("event")), new StatModell(rset.getInt("num"), rset.getInt("wins"), rset.getInt("losses"), rset.getInt("kills"), rset.getInt("deaths"), rset.getInt("scores")));
			}
			rset.close();
			statement.close();
			
			NexusLoader.debug("Stats loaded: " + count + " records.");
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			createHtmls();
		}
		catch (Exception e)
		{
			System.out.println("EventStats SQL catch");
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
	
	public void showHtml(int id, PlayerEventInfo player)
	{
		if (!this.enabled)
		{
			player.sendMessage("The stat tracking is disabled.");
			return;
		}
		if (!this.htmls.containsKey(Integer.valueOf(id)))
		{
			return;
		}
		ShowBoardData sb = this.htmls.get(Integer.valueOf(id));
		sb.sendToPlayer(player);
		
		sb = new ShowBoardData(null, "102");
		sb.sendToPlayer(player);
		
		sb = new ShowBoardData(null, "103");
		sb.sendToPlayer(player);
	}
	
	public void showPlayerStats(int playerId, PlayerEventInfo player)
	{
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><body><br><br><center><table width=640 bgcolor=5A5A5A><tr><td width=120><center>Event</td><td width=65><center>Count</td><td width=65><center>Win%</td><td width=65><center>K:D</td><td width=65><center>Wins</td><td width=65><center>Losses</td><td width=65><center>Kills</td><td width=65><center>Deaths</td><td width=65><center>Scores</td></tr></table><br><center><table width=640>");
		if (this.stats.containsKey(Integer.valueOf(playerId)))
		{
			for (Map.Entry<Integer, StatModell> event : (this.stats.get(Integer.valueOf(playerId))).entrySet())
			{
				StatModell stats = event.getValue();
				if (EventType.getEventByMainId(event.getKey().intValue()) != null)
				{
					String kdRatio = String.valueOf(stats.deaths == 0 ? stats.kills : stats.kills / stats.deaths);
					String winPercent = String.valueOf((stats.wins / stats.num) * 100.0D);
					
					kdRatio = kdRatio.substring(0, Math.min(3, kdRatio.length()));
					winPercent = winPercent.substring(0, Math.min(5, winPercent.length()));
					
					tb.append("<tr><td width=120>" + EventType.getEventByMainId(event.getKey().intValue()).getHtmlTitle() + "</td><td width=65><center>" + stats.num + "</td><td width=65><center>" + winPercent + "%</td><td width=65><center>" + kdRatio + "</td><td width=65><center>" + stats.wins + "</td><td width=65><center>" + stats.losses + "</td><td width=65><center>" + stats.kills + "</td><td width=65><center>" + stats.deaths + "</td><td width=65><center>" + stats.scores + "</td></tr>");
				}
			}
		}
		tb.append("</table></body></html>");
		
		ShowBoardData sb = new ShowBoardData(tb.toString(), "101");
		sb.sendToPlayer(player);
		
		sb = new ShowBoardData(null, "102");
		sb.sendToPlayer(player);
		
		sb = new ShowBoardData(null, "103");
		sb.sendToPlayer(player);
	}
	
	private void sumPlayerStats()
	{
		if (!this.enabled)
		{
			return;
		}
		this.statSums.clear();
		// TODO: Smotocel
		for (Integer integer : this.stats.keySet())
		{
			int playerId = integer.intValue();
			
			int num = 0;
			int wins = 0;
			int losses = 0;
			int kills = 0;
			int deaths = 0;
			int faveventid = 0;
			int faveventamm = 0;
			for (Map.Entry<Integer, StatModell> statmodell : (this.stats.get(Integer.valueOf(playerId))).entrySet())
			{
				num += statmodell.getValue().num;
				wins += statmodell.getValue().wins;
				losses += statmodell.getValue().losses;
				kills += statmodell.getValue().kills;
				deaths += statmodell.getValue().deaths;
				if (statmodell.getValue().num > faveventamm)
				{
					faveventamm = statmodell.getValue().num;
					faveventid = statmodell.getKey().intValue();
				}
			}
			this.statSums.put(Integer.valueOf(playerId), new int[]
			{
				num,
				wins,
				losses,
				kills,
				deaths,
				faveventid
			});
		}
		NexusLoader.debug("sumPlayerStats finished");
	}
	
	@SuppressWarnings(
	{
		"resource",
		"null"
	})
	public void updateSQL(Set<PlayerEventInfo> players, int eventId)
	{
		if (!this.enabled)
		{
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			sumPlayerStats();
			con = CallBack.getInstance().getOut().getConnection();
			for (PlayerEventInfo player : players)
			{
				int id = player.getPlayersId();
				if (this.statSums.get(Integer.valueOf(id))[0] != 1)
				{
					statement = con.prepareStatement("UPDATE nexus_stats_full SET num=?, winpercent=?, kdratio=?, wins=?, losses=?, kills=?, deaths=?, favevent=? WHERE player=?");
					statement.setInt(1, this.statSums.get(Integer.valueOf(id))[0]);
					statement.setDouble(2, (this.statSums.get(Integer.valueOf(id))[0] == 0 ? 1.0D : this.statSums.get(Integer.valueOf(id))[1] / this.statSums.get(Integer.valueOf(id))[0]) * 100.0D);
					statement.setDouble(3, this.statSums.get(Integer.valueOf(id))[4] == 0 ? this.statSums.get(Integer.valueOf(id))[3] : this.statSums.get(Integer.valueOf(id))[3] / this.statSums.get(Integer.valueOf(id))[4]);
					statement.setInt(4, this.statSums.get(Integer.valueOf(id))[1]);
					statement.setInt(5, this.statSums.get(Integer.valueOf(id))[2]);
					statement.setInt(6, this.statSums.get(Integer.valueOf(id))[3]);
					statement.setInt(7, this.statSums.get(Integer.valueOf(id))[4]);
					statement.setInt(8, this.statSums.get(Integer.valueOf(id))[5]);
					statement.setInt(9, id);
					
					statement.executeUpdate();
					statement.close();
				}
				else
				{
					statement = con.prepareStatement("INSERT INTO nexus_stats_full(player,num,winpercent,kdratio,wins,losses,kills,deaths,favevent) VALUES (?,?,?,?,?,?,?,?,?)");
					statement.setInt(1, id);
					statement.setInt(2, this.statSums.get(Integer.valueOf(id))[0]);
					statement.setDouble(3, (this.statSums.get(Integer.valueOf(id))[0] == 0 ? 1.0D : this.statSums.get(Integer.valueOf(id))[1] / this.statSums.get(Integer.valueOf(id))[0]) * 100.0D);
					statement.setDouble(4, this.statSums.get(Integer.valueOf(id))[4] == 0 ? this.statSums.get(Integer.valueOf(id))[3] : this.statSums.get(Integer.valueOf(id))[3] / this.statSums.get(Integer.valueOf(id))[4]);
					statement.setInt(5, this.statSums.get(Integer.valueOf(id))[1]);
					statement.setInt(6, this.statSums.get(Integer.valueOf(id))[2]);
					statement.setInt(7, this.statSums.get(Integer.valueOf(id))[3]);
					statement.setInt(8, this.statSums.get(Integer.valueOf(id))[4]);
					statement.setInt(9, this.statSums.get(Integer.valueOf(id))[5]);
					statement.executeUpdate();
					statement.close();
				}
				if ((this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).num != 1)
				{
					statement = con.prepareStatement("UPDATE nexus_stats SET num=?, wins=?, losses=?, kills=?, deaths=?, scores=? WHERE player=? AND event=?");
					statement.setInt(1, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).num);
					statement.setInt(2, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).wins);
					statement.setInt(3, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).losses);
					statement.setInt(4, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).kills);
					statement.setInt(5, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).deaths);
					statement.setInt(6, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).scores);
					statement.setInt(7, id);
					statement.setInt(8, eventId);
					statement.executeUpdate();
					statement.close();
				}
				else
				{
					statement = con.prepareStatement("INSERT INTO nexus_stats(player,event,num,wins,losses,kills,deaths,scores) VALUES (?,?,?,?,?,?,?,?)");
					statement.setInt(1, id);
					statement.setInt(2, eventId);
					statement.setInt(3, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).num);
					statement.setInt(4, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).wins);
					statement.setInt(5, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).losses);
					statement.setInt(6, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).kills);
					statement.setInt(7, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).deaths);
					statement.setInt(8, (this.stats.get(Integer.valueOf(id))).get(Integer.valueOf(eventId)).scores);
					statement.executeUpdate();
					statement.close();
				}
			}
			try
			{
				con.close();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			NexusLoader.debug("updateSQL finished");
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
		createHtmls();
	}
}
