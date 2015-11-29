package cz.nxs.events.engine.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.interf.NexusOut;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.SkillData;

public class Buffer
{
	private final FastMap<String, FastList<Integer>> buffTemplates;
	private final FastMap<String, Boolean> changes;
	private final UpdateTask updateTask;
	
	private static class SingletonHolder
	{
		public static final Buffer _instance = new Buffer();
	}
	
	protected class UpdateTask implements Runnable
	{
		
		@Override
		public void run()
		{
			updateSQL();
		}
	}
	
	public static Buffer getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public Buffer()
	{
		updateTask = new UpdateTask();
		changes = new FastMap<>();
		buffTemplates = new FastMap<>();
		loadSQL();
		NexusOut.scheduleGeneralAtFixedRate(updateTask, 600000, 600000);
	}
	
	public void buffPlayer(PlayerEventInfo player)
	{
		String playerId = "" + player.getPlayersId() + player.getClassIndex();
		if (!buffTemplates.containsKey(playerId))
		{
			NexusLoader.debug("The player : " + player.getPlayersName() + " (" + playerId + ") without template");
			return;
		}
		for (int skillId : buffTemplates.get(playerId))
		{
			if (player.isInOlympiadMode())
			{
				return;
			}
			player.getSkillEffects(skillId, 99);
		}
	}
	
	public void changeList(PlayerEventInfo player, int buff, boolean action)
	{
		String playerId = "" + player.getPlayersId() + player.getClassIndex();
		if (!buffTemplates.containsKey(playerId))
		{
			buffTemplates.put(playerId, new FastList<>());
			changes.put(playerId, true);
		}
		else
		{
			if (!changes.containsKey(playerId))
			{
				changes.put(playerId, false);
			}
			if (action)
			{
				buffTemplates.get(playerId).add(buff);
			}
			else
			{
				buffTemplates.get(playerId).remove(buffTemplates.get(playerId).indexOf(buff));
			}
		}
	}
	
	private void loadSQL()
	{
		if (!EventConfig.getInstance().getGlobalConfigBoolean("eventBufferEnabled"))
		{
			return;
		}
		// PreparedStatement statement = null;
		try
		{
			Connection con = ConnectionFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM event_buffs");
			ResultSet rset = statement.executeQuery();
			int count = 0;
			while (rset.next())
			{
				count++;
				
				buffTemplates.put(rset.getString("player"), new FastList<>());
				
				StringTokenizer st = new StringTokenizer(rset.getString("buffs"), ",");
				
				FastList<Integer> templist = new FastList<>();
				while (st.hasMoreTokens())
				{
					templist.add(Integer.parseInt(st.nextToken()));
				}
				buffTemplates.getEntry(rset.getString("player")).setValue(templist);
			}
			rset.close();
			statement.close();
			
			NexusLoader.debug("Buffer loaded: " + count + " players template.");
			return;
		}
		catch (Exception e)
		{
			System.out.println("EventBuffs SQL catch");
		}
	}
	
	public boolean playerHaveTemplate(PlayerEventInfo player)
	{
		String playerId = "" + player.getPlayersId() + player.getClassIndex();
		if (buffTemplates.containsKey(playerId))
		{
			return true;
		}
		return false;
	}
	
	public void showHtml(PlayerEventInfo player)
	{
		try
		{
			String playerId = "" + player.getPlayersId() + player.getClassIndex();
			if (!buffTemplates.containsKey(playerId))
			{
				buffTemplates.put(playerId, new FastList<>());
				changes.put(playerId, true);
			}
			StringTokenizer st = new StringTokenizer(Config.getInstance().getString(0, "allowedBuffsList"), ",");
			
			FastList<Integer> skillList = new FastList<>();
			while (st.hasMoreTokens())
			{
				skillList.add(Integer.parseInt(st.nextToken()));
			}
			TextBuilder sb = new TextBuilder();
			
			sb.append("<html><title>Event Manager - Buffer</title><body>");
			
			sb.append("<table width=270 border=0 bgcolor=666666><tr>");
			sb.append("<td><button value=\"Mini Events\" action=\"bypass -h minievents_mini_menu\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td><button value=\"Buffer\" action=\"bypass -h eventbuffershow\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("<td><button value=\"Statistics\" action=\"bypass -h eventstats 1\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			sb.append("</tr></table>");
			
			sb.append("<br>");
			
			sb.append("<center><table width=270 bgcolor=4f4f4f><tr><td width=70><font color=ac9775>Edit Buffs</font></td><td width=80></td><td width=120><font color=9f9f9f>Remaining slots:</font> <font color=ac9775>" + (Config.getInstance().getInt(0, "maxBuffNum") - (buffTemplates.get(playerId)).size()) + "</font></td></tr></table><br><br>");
			sb.append("<center><table width=270 bgcolor=4f4f4f><tr><td><font color=ac9775>Added buffs:</font></td></tr></table><br>");
			sb.append("<center><table width=270>");
			
			int c = 0;
			for (int skillId : buffTemplates.get(playerId))
			{
				c++;
				String skillStr = "0000";
				if (skillId < 100)
				{
					skillStr = "00" + skillId;
				}
				else if ((skillId > 99) && (skillId < 1000))
				{
					skillStr = "0" + skillId;
				}
				else if ((skillId > 4698) && (skillId < 4701))
				{
					skillStr = "1331";
				}
				else if ((skillId > 4701) && (skillId < 4704))
				{
					skillStr = "1332";
				}
				else
				{
					skillStr = "" + skillId;
				}
				SkillData skill = new SkillData(skillId, 1);
				if ((c % 2) == 1)
				{
					sb.append("<tr><td width=33><img src=\"Icon.skill" + skillStr + "\" width=32 height=32></td><td width=100><a action=\"bypass -h eventbuffer " + skillId + " 0\"><font color=9f9f9f>" + skill.getName() + "</font></a></td>");
				}
				if ((c % 2) == 0)
				{
					sb.append("<td width=33><img src=\"Icon.skill" + skillStr + "\" width=32 height=32></td><td width=100><a action=\"bypass -h eventbuffer " + skillId + " 0\"><font color=9f9f9f>" + skill.getName() + "</font></a></td></tr>");
				}
			}
			if ((c % 2) == 1)
			{
				sb.append("<td width=33></td><td width=100></td></tr>");
			}
			sb.append("</table><br>");
			
			sb.append("<br><br><center><table width=270 bgcolor=5A5A5A><tr><td><font color=ac9775>Available buffs:</font></td></tr></table><br>");
			sb.append("<center><table width=270>");
			
			c = 0;
			for (int skillId : skillList)
			{
				String skillStr = "0000";
				if (skillId < 100)
				{
					skillStr = "00" + skillId;
				}
				else if ((skillId > 99) && (skillId < 1000))
				{
					skillStr = "0" + skillId;
				}
				else if ((skillId > 4698) && (skillId < 4701))
				{
					skillStr = "1331";
				}
				else if ((skillId > 4701) && (skillId < 4704))
				{
					skillStr = "1332";
				}
				else
				{
					skillStr = "" + skillId;
				}
				SkillData skill = new SkillData(skillId, 1);
				if (!(buffTemplates.get(playerId)).contains(skillId))
				{
					c++;
					if ((c % 2) == 1)
					{
						sb.append("<tr><td width=32><img src=\"Icon.skill" + skillStr + "\" width=32 height=32></td><td width=100>" + ((Config.getInstance().getInt(0, "maxBuffNum") - (buffTemplates.get(playerId)).size()) != 0 ? "<a action=\"bypass -h eventbuffer " + skillId + " 1\"><font color=9f9f9f>" : "") + skill.getName() + ((Config.getInstance().getInt(0, "maxBuffNum") - (buffTemplates.get(playerId)).size()) != 0 ? "</font></a>" : "") + "</td>");
					}
					if ((c % 2) == 0)
					{
						sb.append("<td width=32><img src=\"Icon.skill" + skillStr + "\" width=32 height=32></td><td width=100>" + ((Config.getInstance().getInt(0, "maxBuffNum") - (buffTemplates.get(playerId)).size()) != 0 ? "<a action=\"bypass -h eventbuffer " + skillId + " 1\"><font color=9f9f9f>" : "") + skill.getName() + ((Config.getInstance().getInt(0, "maxBuffNum") - (buffTemplates.get(playerId)).size()) != 0 ? "</font></a>" : "") + "</td></tr>");
					}
				}
			}
			if ((c % 2) == 1)
			{
				sb.append("<td width=33></td><td width=100></td></tr>");
			}
			sb.append("</table>");
			
			sb.append("</body></html>");
			String html = sb.toString();
			
			player.sendHtmlText(html);
			player.sendStaticPacket();
		}
		catch (Throwable e)
		{
			e.printStackTrace();
		}
	}
	
	public void updateSQL()
	{
		try
		{
			Connection con = ConnectionFactory.getInstance().getConnection();
			for (Map.Entry<String, Boolean> player : changes.entrySet())
			{
				TextBuilder sb = new TextBuilder();
				
				int c = 0;
				for (int buffid : buffTemplates.get(player.getKey()))
				{
					if (c == 0)
					{
						sb.append(buffid);
						c++;
					}
					else
					{
						sb.append("," + buffid);
					}
				}
				if (player.getValue())
				{
					PreparedStatement statement = con.prepareStatement("INSERT INTO event_buffs(player,buffs) VALUES (?,?)");
					statement.setString(1, player.getKey());
					statement.setString(2, sb.toString());
					
					statement.executeUpdate();
					statement.close();
				}
				else
				{
					PreparedStatement statement = con.prepareStatement("UPDATE event_buffs SET buffs=? WHERE player=?");
					statement.setString(1, sb.toString());
					statement.setString(2, player.getKey());
					
					statement.executeUpdate();
					statement.close();
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("EventBuffs SQL catch");
		}
		changes.clear();
		
	}
}
