package cz.nxs.events.engine.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

import javolution.text.TextBuilder;
import javolution.util.FastMap;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventBuffer;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.EventMapSystem;
import cz.nxs.events.engine.EventRewardSystem;
import cz.nxs.events.engine.EventWarnings;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.mini.MiniEventGame;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.events.engine.mini.features.LevelFeature;
import cz.nxs.events.engine.stats.EventStatsManager;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.ShowBoardData;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;

public class EventHtmlManager implements IHtmlManager
{
	private final Map<String, String> _cache;
	private String name;
	private boolean bg;
	public static int BUFFS_PER_PAGE = -1;
	public static String BBS_COMMAND = null;
	
	public EventHtmlManager()
	{
		this._cache = new FastMap<>();
		loadAdminHtmls();
		
		NexusLoader.debug("Nexus Events: Loaded HTML manager.");
	}
	
	@SuppressWarnings("null")
	public void loadAdminHtmls()
	{
		if (!this._cache.isEmpty())
		{
			this._cache.clear();
		}
		NexusLoader.debug("Nexus Events: Loading Admin htmls from the jar file.", Level.INFO);
		JarFile jarFile = null;
		try
		{
			jarFile = new JarFile(NexusLoader.getLibsFolderName() + "nexus_events.jar");
			
			InputStream in = null;
			BufferedReader reader = null;
			String line = null;
			String text = null;
			String name = null;
			
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements())
			{
				try
				{
					text = "";
					
					JarEntry entry = entries.nextElement();
					if ((!entry.getName().endsWith(".htm")) && (!entry.getName().endsWith(".html")))
					{
						if (in != null)
						{
							in.close();
						}
						if (reader != null)
						{
							reader.close();
						}
					}
					else
					{
						in = jarFile.getInputStream(entry);
						reader = new BufferedReader(new InputStreamReader(in));
						while ((line = reader.readLine()) != null)
						{
							text = text + line;
						}
						text = text.replaceAll("\r\n", "\n");
						
						name = entry.getName().split("/")[(entry.getName().split("/").length - 1)];
						this._cache.put(name, text);
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					if (in != null)
					{
						in.close();
					}
					if (reader != null)
					{
						reader.close();
					}
				}
			}
			try
			{
				jarFile.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			NexusLoader.debug("Loaded " + this._cache.size() + " HTML files from nexus_events.jar", Level.INFO);
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		finally
		{
			try
			{
				jarFile.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public String getAdminHtml(String name)
	{
		if (EventConfig.getInstance().getGlobalConfigBoolean("devMode"))
		{
			return CallBack.getInstance().getOut().getHtml(name);
		}
		if ((name == null) || (name.isEmpty()))
		{
			return "This page doesn't exist.";
		}
		name = name.split("/")[(name.split("/").length - 1)];
		return this._cache.get(name);
	}
	
	@Override
	public boolean showNpcHtml(PlayerEventInfo player, NpcData npc)
	{
		if (npc.getNpcId() == EventConfig.getInstance().getGlobalConfigInt("miniEventsManagerId"))
		{
			showMiniEventsMenu(player);
			return true;
		}
		if (npc.getNpcId() == EventConfig.getInstance().getGlobalConfigInt("mainEventManagerId"))
		{
			showMainEventsMenu(player, npc);
			return true;
		}
		for (Map<Integer, MiniEventManager> coll : EventManager.getInstance().getMiniEvents().values())
		{
			for (MiniEventManager mgr : coll.values())
			{
				if ((mgr.getMode() != null) && (mgr.getMode().getNpcId() == npc.getNpcId()))
				{
					showMiniEventMenu(player, mgr.getEventType(), mgr.getMode().getModeId(), false, null, "nxs_mini_view " + mgr.getMode().getModeId() + " " + mgr.getEventType().getAltTitle());
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onCbBypass(PlayerEventInfo player, String bypass)
	{
		if (BBS_COMMAND == null)
		{
			BBS_COMMAND = EventConfig.getInstance().getGlobalConfigValue("cbPage");
		}
		try
		{
			if (bypass.startsWith(BBS_COMMAND))
			{
				StringTokenizer st = new StringTokenizer(bypass);
				st.nextToken();
				if (st.hasMoreTokens())
				{
					String command = st.nextToken();
					if (command.startsWith("showplayers_page"))
					{
						int page = 1;
						try
						{
							page = Integer.parseInt(st.nextToken());
						}
						catch (Exception e)
						{
						}
						showHomePage(player, Integer.valueOf(page), null, (st.hasMoreTokens()) && (st.nextToken().equals("desc")));
						return true;
					}
					if (command.startsWith("showinstance"))
					{
						int instance = 0;
						try
						{
							instance = Integer.parseInt(st.nextToken());
						}
						catch (Exception e)
						{
						}
						showHomePage(player, Integer.valueOf(instance), null, false);
						return true;
					}
					if (command.startsWith("nextpageteam"))
					{
						int page = 1;
						int instance = -1;
						try
						{
							page = Integer.parseInt(st.nextToken());
							if (st.hasMoreTokens())
							{
								instance = Integer.parseInt(st.nextToken());
							}
						}
						catch (Exception e)
						{
						}
						showHomePage(player, instance > 0 ? Integer.valueOf(instance) : null, Integer.valueOf(page), false);
						return true;
					}
				}
				else
				{
					return showHomePage(player, null, null, false);
				}
			}
		}
		catch (Exception e)
		{
			if (BBS_COMMAND == null)
			{
				BBS_COMMAND = EventConfig.getInstance().getGlobalConfigValue("cbPage");
			}
		}
		return false;
	}
	
	protected boolean showHomePage(PlayerEventInfo player, Object param, Object param2, boolean fullInfo)
	{
		String text = null;
		if (EventManager.getInstance().getCurrentMainEvent() != null)
		{
			switch (EventManager.getInstance().getMainEventManager().getState())
			{
				case REGISTERING:
					text = CallBack.getInstance().getOut().getHtml("data/html/mods/nexus/cb_registering.htm");
					if (text == null)
					{
						NexusLoader.debug("Missing cb_registering.htm - html file for community board in mods/nexus/ folder.", Level.WARNING);
						return false;
					}
					AbstractMainEvent event = EventManager.getInstance().getCurrentMainEvent();
					
					TextBuilder tb = new TextBuilder();
					
					tb.append("<br><center><font color=8f8f8f>Event <font color=BB9777>" + event.getEventType().getHtmlTitle() + "</font> is in the <font color=86AA6F>registration phase!</font></font></center><br>");
					
					tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
					tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
					
					tb.append("<table width=510 bgcolor=2E2E2E>");
					
					tb.append("<tr><td width=65></td><td width=110><font color=ac9887>Event name:</font></td><td width=140><font color=7f7f7f>" + event.getEventType().getAltTitle() + "</font></td>");
					tb.append("<td width=110 align=right><font color=ac9887>Event map:</font></td><td width=140 align=right><font color=7f7f7f>" + EventManager.getInstance().getMainEventManager().getMapName() + "</font></td><td width=65></td></tr><tr></tr>");
					
					tb.append("<tr><td width=65></td><td width=110><font color=ac9887>Time left:</font></td><td width=140><font color=7f7f7f>" + EventManager.getInstance().getMainEventManager().getTimeLeft(false) + "</font></td>");
					tb.append("<td width=110 align=right><font color=ac9887>Registered:</font></td><td width=140 align=right><font color=7f7f7f>" + EventManager.getInstance().getMainEventManager().getPlayersCount() + " players</font></td><td width=65></td></tr><tr></tr>");
					
					tb.append("</table>");
					
					tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
					tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
					
					int page = (param != null) && ((param instanceof Integer)) ? ((Integer) param).intValue() : 1;
					String desc = event.getHtmlDescription();
					if (!fullInfo)
					{
						if (desc.length() > 280)
						{
							desc = desc.substring(0, 277) + "...&nbsp;<font color=A0A996><a action=\"bypass -h " + BBS_COMMAND + " showplayers_page " + page + " desc\">[Expand info]</a></font>";
						}
					}
					else
					{
						desc = desc + "&nbsp;&nbsp;<font color=A0A996><a action=\"bypass -h " + BBS_COMMAND + " showplayers_page " + page + "\">[Hide info]</a></font>";
					}
					tb.append("<br><br><center><font color=ac9887>Event description:</font><br1><table width=430><tr><td width=430><font color=7f7f7f>" + desc + "</font></td></tr></table></center>");
					tb.append("<br1><font color=8f8f8f>Registration is possible on the registration NPC.</font>");
					
					RegisteredPlayerList playerList = getRegisteredPlayerList((param != null) && ((param instanceof Integer)) ? ((Integer) param).intValue() : 1);
					
					tb.append("<br><img src=\"L2UI.SquareGray\" width=512 height=2>");
					tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
					
					int maxPages = getMaxPages();
					
					tb.append("<center><table bgcolor=2E2E2E width=510><tr><td width=510 align=center><font color=ac9887>Registered players</font> " + (page > 1 ? "<font color=5f5f5f>(page " + page + "/" + maxPages + ")</font>" : "") + "</td></tr></table></center><br1>");
					tb.append(playerList.list);
					
					tb.append("<img src=\"L2UI.SquareBlank\" width=512 height=3>");
					tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
					
					text = text.replaceAll("%regInfo%", tb.toString());
					
					int countBr = 15 - ((playerList.rows - 1) * 2);
					
					String br = "";
					for (int i = 0; i < countBr; i++)
					{
						br = br + "<br>";
					}
					text = text.replaceAll("%br%", br);
					
					break;
				case RUNNING:
				case TELE_BACK:
					text = CallBack.getInstance().getOut().getHtml("data/html/mods/nexus/cb_eventactive.htm");
					if (text == null)
					{
						NexusLoader.debug("Missing cb_eventactive.htm - html file for community board in mods/nexus/ folder.", Level.WARNING);
						return false;
					}
					event = EventManager.getInstance().getCurrentMainEvent();
					
					tb = new TextBuilder();
					
					tb.append("<br><center><font color=8f8f8f>Event <font color=BB9777>" + event.getEventType().getHtmlTitle() + "</font> is running.</font></center><br>");
					
					tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
					tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
					
					tb.append("<table width=510 bgcolor=2E2E2E>");
					
					tb.append("<tr><td width=65></td><td width=110><font color=ac9887>Event name:</font></td><td width=140><font color=7f7f7f>" + event.getEventType().getAltTitle() + "</font></td>");
					tb.append("<td width=110 align=right><font color=ac9887>Event map:</font></td><td width=140 align=right><font color=7f7f7f>" + EventManager.getInstance().getMainEventManager().getMapName() + "</font></td><td width=65></td></tr><tr></tr>");
					
					tb.append("<tr><td width=65></td><td width=110><font color=ac9887>Time left:</font></td><td width=140><font color=7f7f7f>" + EventManager.getInstance().getMainEventManager().getTimeLeft(false) + "</font></td>");
					tb.append("<td width=110 align=right><font color=ac9887>Registered:</font></td><td width=140 align=right><font color=7f7f7f>" + EventManager.getInstance().getMainEventManager().getPlayersCount() + " players</font></td><td width=65></td></tr><tr></tr>");
					
					tb.append("</table>");
					
					tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
					tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
					tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
					
					boolean moreInstances = event.getInstances().length > 1;
					InstanceData shownInstance = (param != null) && ((param instanceof Integer)) ? event.getInstances()[((Integer) param).intValue()] : !moreInstances ? event.getInstances()[0] : event.getInstances()[0];
					
					tb.append("<br>");
					if (moreInstances)
					{
						String instances = "";
						for (int i = 0; i < Math.min(event.getInstances().length, 4); i++)
						{
							instances = instances + "<td align=center width=" + (510 / Math.min(event.getInstances().length, 4)) + "><a action=\"bypass -h " + BBS_COMMAND + " showinstance " + i + "\"><font color=" + (event.getInstances()[i].getId() == shownInstance.getId() ? "BB9777" : "9f9f9f") + ">[Instance " + event.getInstances()[i].getName() + "]</font></a></td>, ";
						}
						if (!instances.isEmpty())
						{
							instances = instances.substring(0, instances.length() - 3);
						}
						tb.append("<table width=510 bgcolor=2E2E2E><tr>" + instances + "</tr></table><br>");
					}
					String scoreBar = event.getScorebarCb(shownInstance.getId());
					
					tb.append(scoreBar + "<img src=\"L2UI.SquareBlank\" width=510 height=2>");
					
					String eventInfo = event.getEventInfoCb(shownInstance.getId(), param2);
					
					tb.append(eventInfo);
					
					tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=4>");
					
					text = text.replaceAll("%eventInfo%", tb.toString());
					
					countBr = 4;
					
					br = "";
					for (int i = 0; i < countBr; i++)
					{
						br = br + "<br>";
					}
					text = text.replaceAll("%br%", br);
					
					break;
			}
		}
		else
		{
			text = CallBack.getInstance().getOut().getHtml("data/html/mods/nexus/cb_noevent.htm");
			if (text == null)
			{
				NexusLoader.debug("Missing cb_noevent.htm - html file for community board in mods/nexus/ folder.", Level.WARNING);
				return false;
			}
			String nextEventInfo = null;
			TextBuilder tb = new TextBuilder();
			
			boolean showNextEventTime = true;
			boolean showNextEventName = true;
			
			String nextDelay = EventManager.getInstance().getMainEventManager().getAutoSchedulerDelay();
			if (nextDelay.equals("N/A"))
			{
				showNextEventTime = false;
			}
			String nextEvent = null;
			EventType next = EventManager.getInstance().getMainEventManager().getGuessedNextEvent();
			if (next == null)
			{
				showNextEventName = false;
			}
			else
			{
				nextEvent = next.getHtmlTitle();
			}
			tb.append("<img src=\"L2UI.SquareBlank\" width=500 height=6>");
			tb.append("<img src=\"L2UI.SquareGray\" width=500 height=2>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=500 height=3>");
			
			tb.append("<center><table bgcolor=2E2E2E width=510><tr><td align=center><font color=8A5653>No event is active at the moment.</font></td></tr></table></center>");
			if (showNextEventTime)
			{
				if (nextDelay.endsWith("sec"))
				{
					nextDelay = "<font color=97AF8F>" + nextDelay;
					nextDelay = nextDelay + "</font>";
				}
				else if (nextDelay.endsWith("min"))
				{
					nextDelay = "<font color=9f9f9f>" + nextDelay;
					nextDelay = nextDelay + "</font>";
				}
				tb.append("<center><table bgcolor=2E2E2E width=510><tr><td align=center><font color=7f7f7f>The next event starts in " + nextDelay + ".</font></td></tr></table></center>");
			}
			if (showNextEventName)
			{
				tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=0>");
				tb.append("<center><table bgcolor=2E2E2E width=510><tr><td align=center><font color=7f7f7f>The next event will be " + nextEvent + ".</font></td></tr></table></center>");
			}
			tb.append("<img src=\"L2UI.SquareBlank\" width=500 height=3>");
			tb.append("<img src=\"L2UI.SquareGray\" width=500 height=2>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=500 height=10>");
			
			nextEventInfo = tb.toString();
			
			text = text.replaceAll("%nextEventInfo%", nextEventInfo);
			if ((showNextEventName) && (showNextEventTime))
			{
				text = text.replaceAll("%br1%", "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>");
				text = text.replaceAll("%br2%", "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>");
			}
			else if ((showNextEventName) || (showNextEventTime))
			{
				text = text.replaceAll("%br1%", "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>");
				text = text.replaceAll("%br2%", "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>");
			}
			else
			{
				text = text.replaceAll("%br1%", "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>");
				text = text.replaceAll("%br2%", "<br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br><br>");
			}
		}
		if (text != null)
		{
			showCBHtml(player, text);
			return true;
		}
		return false;
	}
	
	protected int getMaxPages()
	{
		
		return (int) Math.ceil(EventManager.getInstance().getMainEventManager().getPlayers().size() / 15.0D);
	}
	
	protected RegisteredPlayerList getRegisteredPlayerList(int page)
	{
		int size = EventManager.getInstance().getMainEventManager().getPlayers().size();
		
		TextBuilder tb = new TextBuilder();
		
		tb.append("<table bgcolor=2E2E2E width=511>");
		
		int counter = 0;
		int rows = 0;
		
		int startFrom = (page - 1) * 15;
		int to = page * 15;
		for (PlayerEventInfo pl : EventManager.getInstance().getMainEventManager().getPlayers())
		{
			if (counter < startFrom)
			{
				counter++;
			}
			else
			{
				if (counter == to)
				{
					break;
				}
				counter++;
				if (((counter + 2) % 3) == 0)
				{
					rows++;
					tb.append("<tr>");
				}
				tb.append("<td width=170 align=center><font color=7f7f7f>" + pl.getPlayersName() + " (" + pl.getLevel() + ")</font></td>");
				if ((counter % 3) == 0)
				{
					tb.append("</tr>");
				}
			}
		}
		if (!tb.toString().endsWith("</tr>"))
		{
			tb.append("</tr>");
		}
		tb.append("</table><br1>");
		
		boolean previousButton = false;
		boolean nextButton = false;
		if (page > 1)
		{
			previousButton = true;
		}
		if (counter < size)
		{
			nextButton = true;
		}
		if ((previousButton) && (nextButton))
		{
			tb.append("<table bgcolor=2E2E2E width=511><tr><td width=250 align=right><font color=B3AA9D><a action=\"bypass -h " + BBS_COMMAND + " showplayers_page " + (page - 1) + "\">Previous page</a></font></td><td width=45></td><td width=250 align=left><font color=B3AA9D><a action=\"bypass -h " + BBS_COMMAND + " showplayers_page " + (page + 1) + "\">Next page</a></font></td></tr></table>");
		}
		else if (previousButton)
		{
			tb.append("<table bgcolor=2E2E2E width=511><tr><td width=510 align=center><font color=B3AA9D><a action=\"bypass -h " + BBS_COMMAND + " showplayers_page " + (page - 1) + "\">Previous page</a></font></td></tr></table>");
		}
		else if (nextButton)
		{
			tb.append("<table bgcolor=2E2E2E width=511><tr><td width=510 align=center><font color=B3AA9D><a action=\"bypass -h " + BBS_COMMAND + " showplayers_page " + (page + 1) + "\">Next page</a></font></td></tr></table>");
		}
		RegisteredPlayerList data = new RegisteredPlayerList();
		data.list = tb.toString();
		data.rows = (rows + ((previousButton) || (nextButton) ? 1 : 0));
		return data;
	}
	
	protected void showCBHtml(PlayerEventInfo player, String text)
	{
		if (text.length() < 4090)
		{
			ShowBoardData sb = new ShowBoardData(text, "101");
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(null, "102");
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(null, "103");
			sb.sendToPlayer(player);
		}
		else if (text.length() < 8180)
		{
			ShowBoardData sb = new ShowBoardData(text.substring(0, 4090), "101");
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(text.substring(4090, text.length()), "102");
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(null, "103");
			sb.sendToPlayer(player);
		}
		else if (text.length() < 12270)
		{
			ShowBoardData sb = new ShowBoardData(text.substring(0, 4090), "101");
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(text.substring(4090, 8180), "102");
			sb.sendToPlayer(player);
			
			sb = new ShowBoardData(text.substring(8180, text.length()), "103");
			sb.sendToPlayer(player);
		}
	}
	
	@Override
	public boolean onBypass(PlayerEventInfo player, String bypass)
	{
		if (bypass.startsWith("mini_"))
		{
			String action = bypass.substring(5);
			if (action.startsWith("menu"))
			{
				showMiniEventsMenu(player);
				return true;
			}
			if (action.startsWith("register"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				int modeId = Integer.parseInt(st.nextToken());
				EventType event = EventType.getType(st.nextToken());
				if (!event.isRegularEvent())
				{
					EventManager.getInstance().getMiniEvent(event, modeId).registerTeam(player);
				}
				return true;
			}
			if (action.startsWith("unregister"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				int modeId = Integer.parseInt(st.nextToken());
				EventType event = EventType.getType(st.nextToken());
				if (!event.isRegularEvent())
				{
					if (EventManager.getInstance().getMiniEvent(event, modeId).unregisterTeam(player))
					{
						showWindowUnregistered(player);
					}
				}
				return true;
			}
			if (action.startsWith("view"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				int modeId = Integer.parseInt(st.nextToken());
				EventType event = EventType.getType(st.nextToken());
				
				String returnPage = "nxs_mini_menu";
				if (st.hasMoreTokens())
				{
					returnPage = st.nextToken();
				}
				if (!event.isRegularEvent())
				{
					showMiniEventMenu(player, event, modeId, false, null, returnPage);
				}
				return true;
			}
			if (action.startsWith("expand_eventinfo"))
			{
				StringTokenizer st = new StringTokenizer(bypass);
				st.nextToken();
				if (st.hasMoreTokens())
				{
					int modeId = Integer.parseInt(st.nextToken());
					EventType event = EventType.getType(st.nextToken());
					String returnPage = "nxs_mini_menu";
					if (st.hasMoreTokens())
					{
						returnPage = st.nextToken();
					}
					showMiniEventMenu(player, event, modeId, true, null, returnPage);
					
					return true;
				}
				player.sendMessage("Wrong event.");
				return true;
			}
			if (action.startsWith("showrewards"))
			{
				StringTokenizer st = new StringTokenizer(bypass);
				st.nextToken();
				if (st.hasMoreTokens())
				{
					int modeId = Integer.parseInt(st.nextToken());
					EventType event = EventType.getType(st.nextToken());
					
					String pos = "";
					while (st.hasMoreTokens())
					{
						pos = pos + st.nextToken();
					}
					showMiniEventMenu(player, event, modeId, false, pos, "nxs_mini_menu");
					
					return true;
				}
				player.sendMessage("Wrong event.");
				return true;
			}
			if (action.startsWith("matches_menu"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				int modeId = Integer.parseInt(st.nextToken());
				EventType event = EventType.getType(st.nextToken());
				
				boolean returnPage = true;
				if (st.hasMoreTokens())
				{
					returnPage = false;
				}
				if (!event.isRegularEvent())
				{
					showSpectateableGames(player, event, modeId, returnPage);
				}
				return true;
			}
			if (action.startsWith("spectate_game"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				int modeId = Integer.parseInt(st.nextToken());
				EventType event = EventType.getType(st.nextToken());
				int gameId = Integer.parseInt(st.nextToken());
				if (!event.isRegularEvent())
				{
					EventManager.getInstance().spectateGame(player, event, modeId, gameId);
				}
				return true;
			}
			if (action.startsWith("maps"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				int modeId = Integer.parseInt(st.nextToken());
				EventType event = EventType.getType(st.nextToken());
				boolean returnPage = true;
				if (st.hasMoreTokens())
				{
					returnPage = false;
				}
				if (!event.isRegularEvent())
				{
					showEventMaps(player, event, modeId, returnPage);
				}
				return true;
			}
		}
		else if (bypass.startsWith("main_"))
		{
			String action = bypass.substring(5);
			if (action.startsWith("register"))
			{
				EventManager.getInstance().getMainEventManager().registerPlayer(player);
				
				return true;
			}
			if (action.startsWith("unregister"))
			{
				EventManager.getInstance().getMainEventManager().unregisterPlayer(player, false);
				showMainEventsMenu(player, null);
				return true;
			}
			if (action.startsWith("menu"))
			{
				showMainEventsMenu(player, null);
				return true;
			}
		}
		else if (bypass.startsWith("buffer"))
		{
			String action = bypass.substring(7);
			if (action.startsWith("menu"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				
				int page = 0;
				if (st.hasMoreTokens())
				{
					page = Integer.parseInt(st.nextToken());
				}
				showBufferHome(player, returnPage, page, null);
				return true;
			}
			if (action.startsWith("select_scheme_menu"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				
				showSelectSchemeMenu(player, returnPage, 0, null);
				return true;
			}
			if (action.startsWith("select_eventscheme_menu"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				String event = st.nextToken();
				
				showSelectSchemeForEventWindow(player, returnPage, event);
				return true;
			}
			if (action.startsWith("select_eventscheme_pet_menu"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				String event = st.nextToken();
				
				showSelectSchemeForPetInEventWindow(player, returnPage, event);
				return true;
			}
			if (action.startsWith("select_scheme"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String scheme = st.nextToken();
				String returnPage = st.nextToken();
				
				EventBuffer.getInstance().setPlayersCurrentScheme(player.getPlayersId(), scheme);
				showBufferHome(player, returnPage, 0, null);
				return true;
			}
			if (action.startsWith("edit_eventscheme"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String scheme = st.nextToken();
				String returnPage = st.nextToken();
				String menuReturnPage = st.nextToken();
				String event = st.nextToken();
				
				int page = 0;
				if (st.hasMoreTokens())
				{
					page = Integer.parseInt(st.nextToken());
				}
				EventBuffer.getInstance().setPlayersCurrentScheme(player.getPlayersId(), scheme);
				showEditEventScheme(player, returnPage, menuReturnPage, page, null, event);
				return true;
			}
			if (action.startsWith("select_eventscheme"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String scheme = st.nextToken();
				String returnPage = st.nextToken();
				String event = st.nextToken();
				
				EventBuffer.getInstance().setPlayersCurrentScheme(player.getPlayersId(), scheme);
				
				showSelectSchemeForEventWindow(player, returnPage, event);
				return true;
			}
			if (action.startsWith("select_pet_eventscheme"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String scheme = st.nextToken();
				String returnPage = st.nextToken();
				String event = st.nextToken();
				
				EventBuffer.getInstance().setPlayersCurrentPetScheme(player.getPlayersId(), scheme);
				
				showSelectSchemeForPetInEventWindow(player, returnPage, event);
				return true;
			}
			if (action.startsWith("create_scheme"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				if (!st.hasMoreTokens())
				{
					player.sendMessage("You need to specify the name of the scheme.");
					return true;
				}
				String scheme = st.nextToken();
				if (st.hasMoreTokens())
				{
					while (st.hasMoreTokens())
					{
						scheme = scheme + "_" + st.nextToken();
					}
				}
				if (scheme.length() >= 12)
				{
					player.sendMessage("Scheme name must be shorter than 12 chars.");
					showSelectSchemeMenu(player, returnPage, 0, null);
					return true;
				}
				EventBuffer.getInstance().addScheme(player, scheme);
				showSelectSchemeMenu(player, returnPage, 0, null);
				return true;
			}
			if (action.startsWith("create_eventscheme"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				String event = st.nextToken();
				if (!st.hasMoreTokens())
				{
					player.sendMessage("You need to specify scheme's name.");
					return true;
				}
				String scheme = st.nextToken();
				if (st.hasMoreTokens())
				{
					while (st.hasMoreTokens())
					{
						scheme = scheme + "_" + st.nextToken();
					}
				}
				if (scheme.length() >= 12)
				{
					player.sendMessage("Scheme name must be shorter than 12 chars.");
					showSelectSchemeMenu(player, returnPage, 0, null);
					return true;
				}
				EventBuffer.getInstance().addScheme(player, scheme);
				showSelectSchemeForEventWindow(player, returnPage, event);
				return true;
			}
			if (action.startsWith("delete_scheme"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String scheme = st.nextToken();
				String returnPage = st.nextToken();
				
				EventBuffer.getInstance().removeScheme(player, scheme);
				showSelectSchemeMenu(player, returnPage, 0, null);
				return true;
			}
			if (action.startsWith("select_category"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				String category = st.nextToken();
				int page = Integer.parseInt(st.nextToken());
				String oldCat = st.nextToken();
				if ((oldCat != null) && (oldCat.equals(category)))
				{
					category = null;
				}
				showBufferHome(player, returnPage, page, category);
				return true;
			}
			if (action.startsWith("addskill"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				String category = st.nextToken();
				int page = Integer.parseInt(st.nextToken()) + 1;
				int buffId = Integer.parseInt(st.nextToken());
				
				addBuffToTemplate(player, buffId);
				
				showBufferHome(player, returnPage, page, category);
				
				return true;
			}
			if (action.startsWith("removeskill"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				String category = st.nextToken();
				int page = Integer.parseInt(st.nextToken());
				int buffId = Integer.parseInt(st.nextToken());
				
				removeBuffFromTemplate(player, buffId);
				showBufferHome(player, returnPage, page, category);
				return true;
			}
			if (action.startsWith("eventscheme_select_category"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				String menuReturnPage = st.nextToken();
				String category = st.nextToken();
				int page = Integer.parseInt(st.nextToken());
				String oldCat = st.nextToken();
				String event = st.nextToken();
				if ((oldCat != null) && (oldCat.equals(category)))
				{
					category = null;
				}
				showEditEventScheme(player, returnPage, menuReturnPage, page, category, event);
				return true;
			}
			if (action.startsWith("eventscheme_addskill"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				String menuReturnPage = st.nextToken();
				String category = st.nextToken();
				int page = Integer.parseInt(st.nextToken()) + 1;
				int buffId = Integer.parseInt(st.nextToken());
				String event = st.nextToken();
				
				addBuffToTemplate(player, buffId);
				
				showEditEventScheme(player, returnPage, menuReturnPage, page, category, event);
				return true;
			}
			if (action.startsWith("eventscheme_removeskill"))
			{
				StringTokenizer st = new StringTokenizer(action);
				st.nextToken();
				
				String returnPage = st.nextToken();
				String menuReturnPage = st.nextToken();
				String category = st.nextToken();
				int page = Integer.parseInt(st.nextToken());
				int buffId = Integer.parseInt(st.nextToken());
				String event = st.nextToken();
				
				removeBuffFromTemplate(player, buffId);
				showEditEventScheme(player, returnPage, menuReturnPage, page, category, event);
				return true;
			}
		}
		else
		{
			if (bypass.startsWith("showstats_"))
			{
				EventStatsManager.getInstance().onBypass(player, bypass.substring(10));
				return true;
			}
			if (bypass.startsWith("engine_info"))
			{
				String returnPage = bypass.substring(12);
				showEngineInfoPage(player, returnPage);
				return true;
			}
			if (bypass.startsWith("features_menu"))
			{
				String returnPage = "features";
				try
				{
					returnPage = bypass.substring(14);
				}
				catch (Exception e)
				{
				}
				showEngineFeatures(player, returnPage);
				return true;
			}
			if (bypass.startsWith("statistics_menu"))
			{
				String returnPage = "statistics";
				try
				{
					returnPage = bypass.substring(16);
				}
				catch (Exception e)
				{
				}
				showStatisticsMenu(player, returnPage);
				return true;
			}
			if (bypass.startsWith("warnings_info"))
			{
				String returnPage = "main";
				try
				{
					returnPage = bypass.substring(14);
				}
				catch (Exception e)
				{
				}
				showWarningInfo(player, returnPage);
				return true;
			}
			if (bypass.startsWith("showrewards"))
			{
				StringTokenizer st = new StringTokenizer(bypass);
				st.nextToken();
				if (st.hasMoreTokens())
				{
					EventType event = EventType.getType(st.nextToken());
					
					String pos = "";
					while (st.hasMoreTokens())
					{
						pos = pos + st.nextToken();
					}
					if ((EventManager.getInstance().getMainEventManager().getCurrent() == null) || (EventManager.getInstance().getMainEventManager().getCurrent().getEventType() != event))
					{
						player.sendMessage("Wrong event.");
					}
					else
					{
						showMainEventsMenu(player, null, pos, false, false);
					}
					return true;
				}
				player.sendMessage("Wrong event.");
				return true;
			}
			if (bypass.startsWith("showplayers"))
			{
				player.sendMessage("Sorry, this feature isn't implemented yet.");
				showMainEventsMenu(player, null);
				return true;
			}
			if (bypass.startsWith("mapinfo"))
			{
				StringTokenizer st = new StringTokenizer(bypass);
				st.nextToken();
				if (st.hasMoreTokens())
				{
					player.sendMessage("================================");
					player.sendMessage("   Map " + EventManager.getInstance().getMainEventManager().getMapName() + ": ");
					player.sendMessage(EventManager.getInstance().getMainEventManager().getMapDesc());
					player.sendMessage("================================");
					
					showMainEventsMenu(player, null, null, false, true);
				}
				else
				{
					player.sendMessage("Wrong mapId.");
				}
				return true;
			}
			if (bypass.startsWith("expand_eventinfo"))
			{
				showMainEventsMenu(player, null, null, true, false);
			}
		}
		return false;
	}
	
	public void showSelectSchemeForPetInEventWindow(PlayerEventInfo player, String returnPage, String event)
	{
		if (!EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer"))
		{
			return;
		}
		String scheme = EventBuffer.getInstance().getPlayersCurrentPetScheme(player.getPlayersId());
		
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Event Buffer</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=220 align=center> <font color=7f7f7f>Select buffs for your summon/pet</font></td>");
		if (returnPage.equals("none"))
		{
			tb.append("<td width=65 align=right><button value=\"Close\" action=\"bypass -h close\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		else
		{
			tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_" + returnPage + "_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=5>");
		
		tb.append("<br>");
		
		tb.append("<center><font color=B06F26>If you don't need any buffs for your <br1>summon/pet, just close this window.</center>");
		
		tb.append("<br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
		
		boolean chosen = false;
		if (!EventBuffer.getInstance().getSchemes(player).isEmpty())
		{
			tb.append("<table width=283 bgcolor=2E2E2E>");
			for (Map.Entry<String, List<Integer>> schemes : EventBuffer.getInstance().getSchemes(player))
			{
				try
				{
					chosen = false;
					if (schemes.getKey().equals(EventBuffer.getInstance().getPlayersCurrentPetScheme(player.getPlayersId())))
					{
						chosen = true;
					}
				}
				catch (Exception e)
				{
				}
				tb.append("<tr>");
				if (chosen)
				{
					tb.append("<td width=150 align=left><font color=6B75C9>" + ((scheme != null) && (scheme.equals(schemes.getKey())) ? "*" : "") + " " + schemes.getKey() + " </font><font color=7f7f7f>(" + (schemes.getValue()).size() + " buffs)</font></td>");
				}
				else
				{
					tb.append("<td width=150 align=left><font color=ac9887>" + ((scheme != null) && (scheme.equals(schemes.getKey())) ? "*" : "") + " " + schemes.getKey() + " </font><font color=7f7f7f>(" + (schemes.getValue()).size() + " buffs)</font></td>");
				}
				if (chosen)
				{
					tb.append("<td width=65 align=center><font color=6B75C9>(selected)</font></td>");
				}
				else
				{
					tb.append("<td width=65 align=center><font color=9f9f9f><a action=\"bypass -h nxs_buffer_select_pet_eventscheme " + schemes.getKey() + " " + returnPage + " " + event + "\">Select</a></font></td>");
				}
				tb.append("</tr><tr></tr>");
			}
			tb.append("</table>");
		}
		else
		{
			tb.append("<table width=283 bgcolor=2E2E2E>");
			tb.append("<tr><td width=280 align=center><font color=ac9887>You don't have any buff scheme.</font></td></tr>");
			tb.append("</table>");
		}
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=283 bgcolor=2E2E2E");
		tb.append("<tr><td width=115><edit var=\"name\" width=115 height=15></td><td width=150 align=right><button value=\"Create scheme\" action=\"bypass -h nxs_buffer_create_eventscheme " + returnPage + " " + event + " $name\" width=105 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		tb.append("</table>");
		
		tb.append("<br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
		
		tb.append("<center><button value=\"<- Select scheme for Player\" action=\"bypass -h nxs_buffer_select_eventscheme_menu " + returnPage + " " + event + "\" width=230 height=28 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
		
		tb.append("<center><table bgcolor=36611D>");
		tb.append("<tr><td align=center><button value=\"Close\" action=\"bypass -h nxs_close \" width=100 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		tb.append("</table></center>");
		
		tb.append("</body></html>");
		
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	public void showSelectSchemeForEventWindow(PlayerEventInfo player, String returnPage, String event)
	{
		if (!EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer"))
		{
			return;
		}
		String scheme = EventBuffer.getInstance().getPlayersCurrentScheme(player.getPlayersId());
		
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Event Buffer</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=220 align=center> <font color=7f7f7f>Select which buffs you want in event</font></td>");
		if (returnPage.equals("none"))
		{
			tb.append("<td width=65 align=right><button value=\"Close\" action=\"bypass -h close\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		else
		{
			tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_" + returnPage + "_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=5>");
		
		tb.append("<br>");
		
		tb.append("<center><font color=848A53>Scheme can be changed during the event<br1> by typing </font><font color=LEVEL>.scheme</font></center>");
		
		tb.append("<br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
		
		boolean chosen = false;
		if (!EventBuffer.getInstance().getSchemes(player).isEmpty())
		{
			tb.append("<table width=283 bgcolor=2E2E2E>");
			for (Map.Entry<String, List<Integer>> schemes : EventBuffer.getInstance().getSchemes(player))
			{
				try
				{
					chosen = false;
					if (schemes.getKey().equals(EventBuffer.getInstance().getPlayersCurrentScheme(player.getPlayersId())))
					{
						chosen = true;
					}
				}
				catch (Exception e)
				{
				}
				tb.append("<tr>");
				if (chosen)
				{
					tb.append("<td width=150 align=left><font color=8FC570>" + ((scheme != null) && (scheme.equals(schemes.getKey())) ? "*" : "") + " " + schemes.getKey() + " </font><font color=7f7f7f>(" + (schemes.getValue()).size() + " buffs)</font></td>");
				}
				else
				{
					tb.append("<td width=150 align=left><font color=ac9887>" + ((scheme != null) && (scheme.equals(schemes.getKey())) ? "*" : "") + " " + schemes.getKey() + " </font><font color=7f7f7f>(" + (schemes.getValue()).size() + " buffs)</font></td>");
				}
				tb.append("<td width=75 align=right><font color=9f9f9f><a action=\"bypass -h nxs_buffer_edit_eventscheme " + schemes.getKey() + " buffer_select_eventscheme_menu " + returnPage + " " + event + "\">Edit</a></font></td>");
				if (chosen)
				{
					tb.append("<td width=65 align=center><font color=8FC570>(selected)</font></td>");
				}
				else
				{
					tb.append("<td width=65 align=center><font color=9f9f9f><a action=\"bypass -h nxs_buffer_select_eventscheme " + schemes.getKey() + " " + returnPage + " " + event + "\">Select</a></font></td>");
				}
				tb.append("</tr><tr></tr>");
			}
			tb.append("</table>");
		}
		else
		{
			tb.append("<table width=283 bgcolor=2E2E2E>");
			tb.append("<tr><td width=280 align=center><font color=ac9887>You don't have any buff scheme.</font></td></tr>");
			tb.append("</table>");
		}
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=283 bgcolor=2E2E2E");
		tb.append("<tr><td width=115><edit var=\"name\" width=115 height=15></td><td width=150 align=right><button value=\"Create scheme\" action=\"bypass -h nxs_buffer_create_eventscheme " + returnPage + " " + event + " $name\" width=105 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		tb.append("</table>");
		
		tb.append("<br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
		
		tb.append("<center><button value=\"Select scheme for Pet/Summon ->\" action=\"bypass -h nxs_buffer_select_eventscheme_pet_menu " + returnPage + " " + event + "\" width=230 height=28 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
		
		tb.append("<center><table bgcolor=36611D>");
		tb.append("<tr><td align=center><button value=\"Close\" action=\"bypass -h nxs_close \" width=100 height=24 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		tb.append("</table></center>");
		
		tb.append("</body></html>");
		
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	protected void showEditEventScheme(PlayerEventInfo player, String returnPage, String menuReturnPage, int page, String selectedCategory, String event)
	{
		String scheme = EventBuffer.getInstance().getPlayersCurrentScheme(player.getPlayersId());
		if (scheme == null)
		{
			showSelectSchemeMenu(player, returnPage, page, selectedCategory);
			return;
		}
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Event Buffer</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=70 align=left> <font color=696969> Scheme:</font></td>");
		tb.append("<td width=150 align=left>" + scheme + "</td>");
		tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_" + returnPage + " " + menuReturnPage + " " + event + "\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=1>");
		
		tb.append("<table width=283 bgcolor=2E2E2E");
		
		int i = 0;
		for (String category : EventBuffer.getInstance().getAviableBuffs().keySet())
		{
			if (i == 0)
			{
				tb.append("<tr>");
			}
			if ((selectedCategory != null) && (selectedCategory.equals(category)))
			{
				tb.append("<td align=center width=93><button value=\"" + category + "\" action=\"bypass -h nxs_buffer_eventscheme_select_category " + returnPage + " " + menuReturnPage + " " + category + " " + page + " " + selectedCategory + " " + event + "\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_CT1.Button_DF_Down\"></td>");
			}
			else
			{
				tb.append("<td align=center width=93><button value=\"" + category + "\" action=\"bypass -h nxs_buffer_eventscheme_select_category " + returnPage + " " + menuReturnPage + " " + category + " " + page + " " + selectedCategory + " " + event + "\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			}
			i++;
			if (i == 3)
			{
				tb.append("</tr>");
				i = 0;
			}
		}
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=6>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=281 bgcolor=2E2E2E>");
		tb.append("<tr>");
		if (selectedCategory != null)
		{
			tb.append("<td width=281><font color=696969>Selected category: " + selectedCategory + "</font></td>");
		}
		else
		{
			tb.append("<td><font color=805553>No category selected</td>");
		}
		tb.append("</tr>");
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=6>");
		
		tb.append("<table width=281 bgcolor=2E2E2E");
		
		i = 0;
		for (Map.Entry<String, Map<Integer, Integer>> e : EventBuffer.getInstance().getAviableBuffs().entrySet())
		{
			String category = e.getKey();
			if (category.equals(selectedCategory))
			{
				for (Map.Entry<Integer, Integer> buff : (e.getValue()).entrySet())
				{
					int id = buff.getKey().intValue();
					int level = buff.getValue().intValue();
					String name = new SkillData(id, level).getName();
					
					name = trimName(name);
					if (!EventBuffer.getInstance().containsSkill(id, player))
					{
						if (i == 0)
						{
							tb.append("<tr>");
						}
						String icon = formatSkillIcon("0000", id);
						
						tb.append("<td width=33 align=left><img src=\"icon.skill" + icon + "\" width=32 height=32></td>");
						tb.append("<td width=95 align=left><button action=\"bypass -h nxs_buffer_eventscheme_addskill " + returnPage + " " + menuReturnPage + " " + category + " " + page + " " + id + " " + event + "\" value=\"" + name + "\" width=95 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
						
						i++;
						if (i == 2)
						{
							tb.append("</tr>");
							i = 0;
						}
					}
				}
			}
		}
		tb.append("</table>");
		
		tb.append("<br>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=6>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		int maxBuffs = getMaxBuffs(player) + Math.max(0, getMaxDances(player));
		int currentBuffs = EventBuffer.getInstance().getBuffs(player).size();
		
		tb.append("<table width=281 bgcolor=2E2E2E>");
		tb.append("<tr>");
		tb.append("<td width=150 align=left><font color=696969> Already added buffs:</font></td>");
		tb.append("<td width=130 align=right><font color=696969>count: " + currentBuffs + "/" + maxBuffs + " </font></td>");
		tb.append("</tr>");
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=6>");
		
		tb.append("<table width=281 bgcolor=2E2E2E");
		
		int count = EventBuffer.getInstance().getBuffs(player).size();
		int maxPages = (int) Math.ceil(count / BUFFS_PER_PAGE);
		if (page > (maxPages - 1))
		{
			page = maxPages - 1;
		}
		BUFFS_PER_PAGE = EventConfig.getInstance().getGlobalConfigInt("maxBuffsPerPage");
		
		int from = BUFFS_PER_PAGE * page;
		int to = (BUFFS_PER_PAGE * (page + 1)) - 1;
		
		i = 0;
		int counter = 0;
		for (Integer integer : EventBuffer.getInstance().getBuffs(player))
		{
			int buffId = integer.intValue();
			if ((counter < from) || (counter > to))
			{
				counter++;
			}
			else
			{
				counter++;
				if (i == 0)
				{
					tb.append("<tr>");
				}
				String name = new SkillData(buffId, 1).getName();
				String icon = formatSkillIcon("0000", buffId);
				
				name = trimName(name);
				
				tb.append("<td width=33 align=left><img src=\"icon.skill" + icon + "\" width=32 height=32></td>");
				tb.append("<td width=95 align=left><button action=\"bypass -h nxs_buffer_eventscheme_removeskill " + returnPage + " " + menuReturnPage + " " + selectedCategory + " " + page + " " + buffId + " " + event + "\" value=\"" + name + "\" width=95 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				
				i++;
				if (i == 2)
				{
					tb.append("</tr>");
					i = 0;
				}
			}
		}
		tb.append("</table>");
		
		tb.append("<br>");
		if (maxPages > 1)
		{
			tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=6>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
			
			tb.append("<table width=281 bgcolor=484848>");
			tb.append("<tr>");
			tb.append("<td width=65 align=left><font color=696969> Page: </font></td>");
			
			int width = (int) (220.0D / maxPages);
			for (int c = 0; c < maxPages; c++)
			{
				tb.append("<td width=" + width + " align=left><a action=\"bypass -h nxs_buffer_edit_eventscheme " + scheme + " " + returnPage + " " + menuReturnPage + " " + event + " " + c + "\"><font color=" + (c == page ? "849D68" : "9f9f9f") + ">Page " + (c + 1) + "</font></a></td>");
			}
			tb.append("</tr>");
			tb.append("</table>");
			
			tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=6>");
		}
		else
		{
			tb.append("<br>");
		}
		tb.append("</body></html>");
		
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	private void showSelectSchemeMenu(PlayerEventInfo player, String returnPage, int page, String selectedCategory)
	{
		String scheme = EventBuffer.getInstance().getPlayersCurrentScheme(player.getPlayersId());
		
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Event Buffer</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=220 align=center> <font color=8f8f8f>Scheme management menu</font></td>");
		tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_" + returnPage + "_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=5>");
		
		tb.append("<br>");
		
		tb.append("<br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
		if (!EventBuffer.getInstance().getSchemes(player).isEmpty())
		{
			tb.append("<table width=283 bgcolor=2E2E2E>");
			for (Map.Entry<String, List<Integer>> schemes : EventBuffer.getInstance().getSchemes(player))
			{
				tb.append("<tr>");
				tb.append("<td width=150 align=left><font color=ac9887>" + ((scheme != null) && (scheme.equals(schemes.getKey())) ? "*" : "") + " " + schemes.getKey() + " </font><font color=7f7f7f>(" + (schemes.getValue()).size() + " buffs)</font></td>");
				tb.append("<td width=65 align=center><font color=B04F51><a action=\"bypass -h nxs_buffer_delete_scheme " + schemes.getKey() + " " + returnPage + "\">Delete</a></font></td>");
				tb.append("<td width=75 align=right><font color=9f9f9f><a action=\"bypass -h nxs_buffer_select_scheme " + schemes.getKey() + " " + returnPage + "\">Edit scheme</a></font></td>");
				tb.append("</tr>");
			}
			tb.append("</table>");
		}
		else
		{
			tb.append("<table width=283 bgcolor=2E2E2E>");
			tb.append("<tr><td width=280 align=center><font color=ac9887>You don't have any scheme.</font></td></tr>");
			tb.append("</table>");
		}
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=283 bgcolor=2E2E2E");
		tb.append("<tr><td width=115><edit var=\"name\" width=115 height=15></td><td width=150 align=right><button value=\"Create scheme\" action=\"bypass -h nxs_buffer_create_scheme " + returnPage + " $name\" width=105 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		tb.append("</table>");
		
		tb.append("<br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
		
		tb.append("</body></html>");
		
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	protected void showBufferHome(PlayerEventInfo player, String returnPage, int page, String selectedCategory)
	{
		String scheme = EventBuffer.getInstance().getPlayersCurrentScheme(player.getPlayersId());
		if (scheme == null)
		{
			showSelectSchemeMenu(player, returnPage, page, selectedCategory);
			return;
		}
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Event Buffer</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=70 align=left> <font color=696969> Scheme:</font></td>");
		tb.append("<td width=150 align=left>" + scheme + "&nbsp;&nbsp;<a action=\"bypass -h nxs_buffer_select_scheme_menu " + returnPage + "\">(change)</a></td>");
		tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_" + returnPage + "_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=1>");
		
		tb.append("<table width=283 bgcolor=2E2E2E");
		
		int i = 0;
		for (String category : EventBuffer.getInstance().getAviableBuffs().keySet())
		{
			if (i == 0)
			{
				tb.append("<tr>");
			}
			if ((selectedCategory != null) && (selectedCategory.equals(category)))
			{
				tb.append("<td align=center width=93><button value=\"" + category + "\" action=\"bypass -h nxs_buffer_select_category " + returnPage + " " + category + " " + page + " " + selectedCategory + "\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_CT1.Button_DF_Down\"></td>");
			}
			else
			{
				tb.append("<td align=center width=93><button value=\"" + category + "\" action=\"bypass -h nxs_buffer_select_category " + returnPage + " " + category + " " + page + " " + selectedCategory + "\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
			}
			i++;
			if (i == 3)
			{
				tb.append("</tr>");
				i = 0;
			}
		}
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=6>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=281 bgcolor=2E2E2E>");
		tb.append("<tr>");
		if (selectedCategory != null)
		{
			tb.append("<td width=281><font color=696969>Selected category: " + selectedCategory + "</font></td>");
		}
		else
		{
			tb.append("<td><font color=805553>No category selected</td>");
		}
		tb.append("</tr>");
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=6>");
		
		tb.append("<table width=281 bgcolor=2E2E2E");
		
		i = 0;
		for (Map.Entry<String, Map<Integer, Integer>> e : EventBuffer.getInstance().getAviableBuffs().entrySet())
		{
			String category = e.getKey();
			if (category.equals(selectedCategory))
			{
				for (Map.Entry<Integer, Integer> buff : (e.getValue()).entrySet())
				{
					int id = buff.getKey().intValue();
					int level = buff.getValue().intValue();
					String name = new SkillData(id, level).getName();
					
					name = trimName(name);
					if (!EventBuffer.getInstance().containsSkill(id, player))
					{
						if (i == 0)
						{
							tb.append("<tr>");
						}
						String icon = formatSkillIcon("0000", id);
						
						tb.append("<td width=33 align=left><img src=\"icon.skill" + icon + "\" width=32 height=32></td>");
						tb.append("<td width=95 align=left><button action=\"bypass -h nxs_buffer_addskill " + returnPage + " " + category + " " + page + " " + id + "\" value=\"" + name + "\" width=95 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
						
						i++;
						if (i == 2)
						{
							tb.append("</tr>");
							i = 0;
						}
					}
				}
			}
		}
		tb.append("</table>");
		
		tb.append("<br>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=6>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		int maxBuffs = getMaxBuffs(player) + Math.max(0, getMaxDances(player));
		int currentBuffs = EventBuffer.getInstance().getBuffs(player).size();
		
		tb.append("<table width=281 bgcolor=2E2E2E>");
		tb.append("<tr>");
		tb.append("<td width=150 align=left><font color=696969> Already added buffs:</font></td>");
		tb.append("<td width=130 align=right><font color=696969>count: " + currentBuffs + "/" + maxBuffs + " </font></td>");
		tb.append("</tr>");
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=6>");
		
		tb.append("<table width=281 bgcolor=2E2E2E");
		
		int count = EventBuffer.getInstance().getBuffs(player).size();
		int maxPages = (int) Math.ceil(count / BUFFS_PER_PAGE);
		if (page > (maxPages - 1))
		{
			page = maxPages - 1;
		}
		BUFFS_PER_PAGE = EventConfig.getInstance().getGlobalConfigInt("maxBuffsPerPage");
		
		int from = BUFFS_PER_PAGE * page;
		int to = (BUFFS_PER_PAGE * (page + 1)) - 1;
		
		i = 0;
		int counter = 0;
		for (Integer integer : EventBuffer.getInstance().getBuffs(player))
		{
			int buffId = integer.intValue();
			if ((counter < from) || (counter > to))
			{
				counter++;
			}
			else
			{
				counter++;
				if (i == 0)
				{
					tb.append("<tr>");
				}
				String name = new SkillData(buffId, 1).getName();
				String icon = formatSkillIcon("0000", buffId);
				
				name = trimName(name);
				
				tb.append("<td width=33 align=left><img src=\"icon.skill" + icon + "\" width=32 height=32></td>");
				tb.append("<td width=95 align=left><button action=\"bypass -h nxs_buffer_removeskill " + returnPage + " " + selectedCategory + " " + page + " " + buffId + "\" value=\"" + name + "\" width=95 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				
				i++;
				if (i == 2)
				{
					tb.append("</tr>");
					i = 0;
				}
			}
		}
		tb.append("</table>");
		
		tb.append("<br>");
		if (maxPages > 1)
		{
			tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=6>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
			
			tb.append("<table width=281 bgcolor=484848>");
			tb.append("<tr>");
			tb.append("<td width=65 align=left><font color=696969> Page: </font></td>");
			
			int width = (int) (220.0D / maxPages);
			for (int c = 0; c < maxPages; c++)
			{
				tb.append("<td width=" + width + " align=left><a action=\"bypass -h nxs_buffer_menu " + returnPage + " " + c + "\"><font color=" + (c == page ? "849D68" : "9f9f9f") + ">Page " + (c + 1) + "</font></a></td>");
			}
			tb.append("</tr>");
			tb.append("</table>");
			
			tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=6>");
		}
		else
		{
			tb.append("<br>");
		}
		tb.append("</body></html>");
		
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	protected String trimName(String name)
	{
		String[] split = name.split(" ");
		if (split.length == 1)
		{
			return split[0];
		}
		if (split[0].equals("Song"))
		{
			if (split.length == 3)
			{
				return split[2];
			}
			if (split.length == 4)
			{
				return split[2] + " " + split[3];
			}
		}
		if (name.startsWith("Siren"))
		{
			return name;
		}
		if (split[0].equals("Dance"))
		{
			if (split.length == 3)
			{
				return split[2];
			}
			if (split.length == 4)
			{
				if (split[2].equals("the"))
				{
					return split[3];
				}
				return split[2] + " " + split[3];
			}
		}
		if (split[0].equals("Chant"))
		{
			for (String s : split)
			{
				if ((!s.equalsIgnoreCase("Chant")) && (!s.equals("of")))
				{
					return s;
				}
			}
		}
		if ((split.length == 2) && (split[1].equals("Protection")))
		{
			return split[0] + " Prot.";
		}
		if ((split.length == 3) && (split[0].equals("Prophecy")))
		{
			return "Proph. " + split[1] + " " + split[2];
		}
		if (name.equals("Siren's Dance"))
		{
			return split[1];
		}
		if (name.equals("Unholy Resistance"))
		{
			return "Unholy Resist.";
		}
		if (name.equals("Holy Resistance"))
		{
			return "Holy Resist.";
		}
		if (name.equals("Blessing of Queen"))
		{
			return "Bless. Queen";
		}
		if (name.equals("Gift of Queen"))
		{
			return "Gift of Queen";
		}
		if (name.equals("Blessing of Seraphim"))
		{
			return "Bless. Seraph.";
		}
		if (name.equals("Gift of Seraphim"))
		{
			return "Gift of Seraph.";
		}
		if (name.equals("Mana Regeneration"))
		{
			return "Mana Regen.";
		}
		return name;
	}
	
	protected int getMaxBuffs(PlayerEventInfo player)
	{
		return 32;
	}
	
	protected int getMaxDances(PlayerEventInfo player)
	{
		return 0;
	}
	
	protected void addBuffToTemplate(PlayerEventInfo player, int buffId)
	{
		int maxBuffs = getMaxBuffs(player) + Math.max(0, getMaxDances(player));
		int currentBuffs = EventBuffer.getInstance().getBuffs(player).size();
		if (currentBuffs >= maxBuffs)
		{
			player.sendMessage("The maximum buffs count is " + maxBuffs + ". You can't add any buffs anymore.");
			return;
		}
		if (EventBuffer.getInstance().addBuff(buffId, player))
		{
			player.sendMessage("The buff was added to your template.");
		}
		else
		{
			player.sendMessage("This buff has already been added!");
		}
	}
	
	protected void removeBuffFromTemplate(PlayerEventInfo player, int buffId)
	{
		EventBuffer.getInstance().removeBuff(buffId, player);
		player.sendMessage("The buff was removed from your template.");
	}
	
	protected String formatSkillIcon(String vzor, int hodnota)
	{
		if ((hodnota == 4699) || (hodnota == 4700))
		{
			return "1331";
		}
		if ((hodnota == 4702) || (hodnota == 4703))
		{
			return "1332";
		}
		DecimalFormat format = new DecimalFormat(vzor);
		String vystup = format.format(hodnota);
		return vystup;
	}
	
	public void showMainEventsMenu(PlayerEventInfo player, NpcData npc)
	{
		showMainEventsMenu(player, npc, null, false, false);
	}
	
	@SuppressWarnings("null")
	public void showMainEventsMenu(PlayerEventInfo player, NpcData npc, String position, boolean fullDesc, boolean mapInfo)
	{
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Main Events</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=90 align=left> <font color=696969> Powered by:</font></td>");
		tb.append("<td width=130 align=left><font color=63AA1C><a action=\"bypass -h nxs_engine_info main\">Nexus Event Engine</a></font></td>");
		tb.append("<td width=65 align=right><button value=\"Features\" action=\"bypass -h nxs_features_menu main\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=10>");
		RewardPosition.PositionType posType;
		int i;
		boolean bg;
		String name;
		if (EventManager.getInstance().getMainEventManager().getState() == MainEventManager.State.REGISTERING)
		{
			AbstractMainEvent currentEvent = EventManager.getInstance().getMainEventManager().getCurrent();
			
			tb.append("<table width=281 bgcolor=2E2E2E>");
			tb.append("<tr>");
			tb.append("<td width=90><font color=9f9f9f>&nbsp;Current event:</font></td>");
			tb.append("<td width=115 align=center><font color=ac9887>" + currentEvent.getEventType().getHtmlTitle() + "</font></td>");
			if (EventManager.getInstance().getMainEventManager().getPlayers().contains(player))
			{
				tb.append("<td width=75 align=right><a action=\"bypass -h nxs_main_unregister\"><font color=A26D64>Unregister</font></a></td>");
			}
			else
			{
				tb.append("<td width=75 align=right><a action=\"bypass -h nxs_main_register\"><font color=849D68>Register!</font></a></td>");
			}
			tb.append("</tr>");
			tb.append("</table>");
			
			tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=6>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
			
			String desc = currentEvent.getHtmlDescription();
			if ((!fullDesc) && (desc.length() > 170))
			{
				desc = desc.substring(0, 155) + "... &nbsp;<a action=\"bypass -h nxs_expand_eventinfo\">[view full info]</a>";
			}
			if (fullDesc)
			{
				desc = desc + "&nbsp;<font color=696969><a action=\"bypass -h nxs_main_menu\">[hide full info]</a></font>";
			}
			tb.append("<table width=281 bgcolor=2E2E2E>");
			tb.append("<tr>");
			tb.append("<td width=281><font color=" + (fullDesc ? "898989" : "696969") + ">" + desc + "</font></td>");
			tb.append("</tr>");
			tb.append("</table>");
			
			tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=6>");
			
			tb.append("<table width=281 bgcolor=2E2E2E>");
			
			tb.append("<tr>");
			tb.append("<td width=90><font color=9f9f9f>&nbsp;Event state:</font></td>");
			
			String state = "Idle";
			if (EventManager.getInstance().getMainEventManager().getState() == MainEventManager.State.REGISTERING)
			{
				state = "Registration";
			}
			else if (EventManager.getInstance().getMainEventManager().getState() == MainEventManager.State.RUNNING)
			{
				state = "Running";
			}
			else if ((EventManager.getInstance().getMainEventManager().getState() == MainEventManager.State.END) || (EventManager.getInstance().getMainEventManager().getState() == MainEventManager.State.TELE_BACK))
			{
				state = "Ending";
			}
			tb.append("<td width=115 align=center><font color=ac9887>" + state + "</font></td>");
			
			String timeLeft = EventManager.getInstance().getMainEventManager().getTimeLeft(true);
			int minutes = Integer.valueOf(timeLeft.split(":")[0]).intValue();
			int seconds = Integer.valueOf(timeLeft.split(":")[1]).intValue();
			if (minutes > 0)
			{
				tb.append("<td width=75 align=right><font color=696969>" + minutes + " min left</font></td>");
			}
			else
			{
				tb.append("<td width=75 align=right><font color=805353>" + seconds + " sec left</font></td>");
			}
			tb.append("</tr>");
			if (!mapInfo)
			{
				tb.append("<tr>");
				tb.append("<td width=90><font color=9f9f9f>&nbsp;Current map:</font></td>");
				tb.append("<td width=115 align=center><font color=ac9887>" + EventManager.getInstance().getMainEventManager().getMapName() + "</font></td>");
				tb.append("<td width=75 align=right><font color=696969><a action=\"bypass -h nxs_mapinfo " + EventManager.getInstance().getMainEventManager().getMap().getGlobalId() + "\"> Map info</a></font></td>");
				tb.append("</tr>");
			}
			else
			{
				tb.append("<tr>");
				tb.append("<td width=90><font color=9f9f9f>&nbsp;Current map:</font></td>");
				tb.append("<td width=115 align=center><font color=ac9887>" + EventManager.getInstance().getMainEventManager().getMapName() + "</font></td>");
				tb.append("<td width=75 align=right></td>");
				tb.append("</tr>");
			}
			tb.append("<tr>");
			tb.append("<td width=90><font color=9f9f9f>&nbsp;Players:</font></td>");
			tb.append("<td width=115 align=center><font color=99816C>" + EventManager.getInstance().getMainEventManager().getPlayersCount() + " </font><font color=9f9f9f>registered</font></td>");
			
			tb.append("<td width=75 align=right><font color=696969><a action=\"bypass -h nxs_showplayers\"></a></font></td>");
			tb.append("</tr>");
			
			tb.append("<tr>");
			tb.append("<td width=90><font color=9f9f9f>&nbsp;Statistics:</font></td>");
			tb.append("<td width=115 align=center></td>");
			tb.append("<td width=75 align=right><font color=696969><a action=\"bypass -h nxs_statistics_menu main \">Show</a></font></td>");
			tb.append("</tr>");
			if ((EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer")) && (player.isRegistered()) && (player.getRegisteredMainEvent() != null) && (player.getRegisteredMainEvent() == currentEvent.getEventType()))
			{
				tb.append("<tr>");
				tb.append("<td width=110><font color=9f9f9f>&nbsp;Buffer scheme:</font></td>");
				tb.append("<td width=105 align=center><font color=CAA26A>" + EventBuffer.getInstance().getPlayersCurrentScheme(player.getPlayersId()) + "</font></td>");
				tb.append("<td width=75 align=right><font color=729768><a action=\"bypass -h nxs_buffer_select_eventscheme_menu main " + currentEvent.getEventType().getAltTitle() + "\">Change</a></font></td>");
				tb.append("</tr>");
			}
			tb.append("</table>");
			
			tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=6>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
			
			tb.append("<table width=281 bgcolor=2E2E2E>");
			tb.append("<tr>");
			
			TextBuilder positions = new TextBuilder();
			
			EventRewardSystem.EventRewards rewards = EventRewardSystem.getInstance().getAllRewardsFor(currentEvent.getEventType(), 1);
			RewardPosition pos = null;
			if (position != null)
			{
				if (position.split("-").length > 1)
				{
					String from = position.split("-")[0];
					String to = position.split("-")[1];
					
					positions.append(from + " - " + to + " ;");
				}
				else
				{
					positions.append(position + ";");
				}
			}
			for (Map.Entry<EventRewardSystem.PositionContainer, Map<Integer, EventRewardSystem.RewardItem>> e : rewards.getAllRewards().entrySet())
			{
				if ((e.getValue() != null) && (!(e.getValue()).isEmpty()) && (e.getKey().position.posType != null))
				{
					pos = e.getKey().position;
					if (pos.posType == RewardPosition.PositionType.General)
					{
						if ((position == null) || (!position.equals(pos.toString())))
						{
							if ((pos == RewardPosition.Winner) || (pos == RewardPosition.Looser))
							{
								positions.append("" + pos.toString() + ";");
							}
						}
					}
					else if ((pos.posType == RewardPosition.PositionType.Numbered) && (pos != RewardPosition.KillingSpree))
					{
						if ((position == null) || (!(e.getKey().parameter + ".").equals(position)))
						{
							positions.append("" + e.getKey().parameter + ". ;");
						}
					}
					else if (pos.posType == RewardPosition.PositionType.Range)
					{
						try
						{
							int from = Integer.parseInt(e.getKey().parameter.split("-")[0]);
							int to = Integer.parseInt(e.getKey().parameter.split("-")[1]);
							if ((position != null) && ((from + ".-" + to + ".").equals(position)))
							{
								continue;
							}
							positions.append("" + from + ". - " + to + ".;");
						}
						catch (Exception e2)
						{
						}
					}
				}
			}
			boolean hasRewards = false;
			String positionsString = positions.toString();
			if (positionsString.length() > 0)
			{
				hasRewards = true;
				positionsString = positionsString.substring(0, positionsString.length() - 1);
			}
			if (hasRewards)
			{
				tb.append("<td width=110 align=left><font color=ac9887>");
				tb.append("&nbsp;Rewards:");
				tb.append("</font></td>");
				
				tb.append("<td width=75 align=right><font color=696969>");
				tb.append("<combobox width=72 height=17 var=pos list=\"" + positionsString + "\">");
				tb.append("</font></td>");
				
				tb.append("<td width=100 align=right><font color=696969>");
				tb.append("<button value=\"Show Rewards\" action=\"bypass -h nxs_showrewards " + currentEvent.getEventType().getAltTitle() + " $pos\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				tb.append("</font></td>");
			}
			else
			{
				tb.append("<td width=280 align=center><font color=9f9f9f>This event has no or only secret rewards.</font></td>");
			}
			tb.append("</tr>");
			tb.append("</table>");
			
			tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=7>");
			if (hasRewards)
			{
				if (position == null)
				{
					tb.append("<table width=281 bgcolor=2E2E2E>");
					tb.append("<tr>");
					
					tb.append("<td align=center width=281><font color=595959>Select a position to see the rewards.</font></td>");
					
					tb.append("</tr>");
					tb.append("</table>");
				}
				else
				{
					if (position.split("-").length > 1)
					{
						posType = RewardPosition.PositionType.Range;
					}
					else
					{
						if (position.endsWith("."))
						{
							posType = RewardPosition.PositionType.Numbered;
						}
						else
						{
							posType = RewardPosition.PositionType.General;
						}
					}
					for (Map.Entry<EventRewardSystem.PositionContainer, Map<Integer, EventRewardSystem.RewardItem>> e : rewards.getAllRewards().entrySet())
					{
						if ((e.getKey().position != null) && (e.getKey().position.posType == posType))
						{
							if (posType == RewardPosition.PositionType.General)
							{
								if (e.getKey().position.toString().equals(position))
								{
								}
							}
							else if (posType == RewardPosition.PositionType.Numbered)
							{
								if ((e.getKey().parameter + ".").equals(position))
								{
								}
							}
							else
							{
								try
								{
									String param = e.getKey().parameter;
									String from = param.split("-")[0];
									String to = param.split("-")[1];
									
									param = from + ".-" + to + ".";
									if (!param.equals(position))
									{
										continue;
									}
								}
								catch (Exception e2)
								{
								}
								continue;
							}
							i = 0;
							bg = false;
							name = null;
							for (Map.Entry<Integer, EventRewardSystem.RewardItem> item : (e.getValue()).entrySet())
							{
								if (bg)
								{
									tb.append("<table width=281 bgcolor=2E2E2E>");
								}
								else
								{
									tb.append("<table width=281 bgcolor=3B3B3B>");
								}
								bg = !bg;
								
								tb.append("<tr>");
								if (item.getValue().id == -1)
								{
									name = "XP";
								}
								else if (item.getValue().id == -2)
								{
									name = "SP";
								}
								else if (item.getValue().id == -3)
								{
									name = "Fame";
								}
								else
								{
									name = CallBack.getInstance().getOut().getItemName(item.getValue().id);
								}
								if (name.length() > 27)
								{
									name = name.substring(0, 24) + "...";
								}
								int width = 170;
								if (name.length() < 5)
								{
									width = 100;
								}
								else if (name.length() < 12)
								{
									width = 130;
								}
								tb.append("<td width=" + width + " align=left> <font color=ac9887>" + ++i + ".</font> <font color=9f9f9f>" + name + "</font></td>");
								
								String min = null;
								String max = null;
								if (item.getValue().minAmmount >= 1000000000)
								{
									min = (int) Math.round(item.getValue().minAmmount / 1.0E9D) + "kkk";
								}
								else if (item.getValue().minAmmount >= 1000000)
								{
									min = (int) Math.round(item.getValue().minAmmount / 1000000.0D) + "kk";
								}
								else if (item.getValue().minAmmount >= 1000)
								{
									min = (int) Math.round(item.getValue().minAmmount / 1000.0D) + "k";
								}
								else
								{
									min = "" + item.getValue().minAmmount + "x";
								}
								if (item.getValue().maxAmmount >= 1000000000)
								{
									max = (int) Math.round(item.getValue().maxAmmount / 1.0E9D) + "kkk";
								}
								else if (item.getValue().maxAmmount >= 1000000)
								{
									max = (int) Math.round(item.getValue().maxAmmount / 1000000.0D) + "kk";
								}
								else if (item.getValue().maxAmmount >= 1000)
								{
									max = (int) Math.round(item.getValue().maxAmmount / 1000.0D) + "k";
								}
								else
								{
									max = "" + item.getValue().maxAmmount + "x";
								}
								if (item.getValue().minAmmount == item.getValue().maxAmmount)
								{
									tb.append("<td width=40 align=right><font color=595959>" + min + "</font></td>");
								}
								else
								{
									tb.append("<td width=50 align=right><font color=595959>" + min + "-" + max + "</font></td>");
								}
								tb.append("<td align=right><font color=666666>" + item.getValue().chance + "%</font></td>");
								tb.append("</tr>");
								
								tb.append("</table>");
								
								tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
							}
						}
					}
				}
			}
		}
		else if ((EventManager.getInstance().getMainEventManager().getState() == MainEventManager.State.RUNNING) || (EventManager.getInstance().getMainEventManager().getState() == MainEventManager.State.TELE_BACK))
		{
			tb.append("<center><br><font color=8A5653>Event " + EventManager.getInstance().getMainEventManager().getCurrent().getEventName() + " is running in map " + EventManager.getInstance().getMainEventManager().getMapName() + ".");
			tb.append("<br><br><button value=\"Event status\" action=\"bypass -h " + BBS_COMMAND + "\" width=95 height=23 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center>");
		}
		else
		{
			boolean showNextEventTime = EventConfig.getInstance().getGlobalConfigBoolean("showNextEventTime");
			boolean showNextEventName = EventConfig.getInstance().getGlobalConfigBoolean("showNextEventName");
			String nextDelay = null;
			if (showNextEventTime)
			{
				nextDelay = EventManager.getInstance().getMainEventManager().getAutoSchedulerDelay();
				if (nextDelay.equals("N/A"))
				{
					showNextEventTime = false;
				}
			}
			String nextEvent = null;
			if (showNextEventName)
			{
				EventType next = EventManager.getInstance().getMainEventManager().getGuessedNextEvent();
				if (next == null)
				{
					showNextEventName = false;
				}
				else
				{
					nextEvent = next.getHtmlTitle();
				}
			}
			if ((showNextEventTime) && (showNextEventName))
			{
				tb.append("<br><br><br><br><br><br><br><br><br>");
			}
			else if ((showNextEventName) || (showNextEventTime))
			{
				tb.append("<br><br><br><br><br><br><br><br><br><br>");
			}
			else
			{
				tb.append("<br><br><br><br><br><br><br><br><br><br><br><br>");
			}
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=6>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
			
			tb.append("<center><table bgcolor=2E2E2E width=282><tr><td align=center><font color=8A5653>No event is active at the moment.</font></td></tr></table></center>");
			if (showNextEventTime)
			{
				if (nextDelay.endsWith("sec"))
				{
					nextDelay = "<font color=97AF8F>" + nextDelay;
					nextDelay = nextDelay + "</font>";
				}
				else if (nextDelay.endsWith("min"))
				{
					nextDelay = "<font color=9f9f9f>" + nextDelay;
					nextDelay = nextDelay + "</font>";
				}
				tb.append("<center><table bgcolor=2E2E2E width=282><tr><td align=center><font color=7f7f7f>The next event starts in " + nextDelay + ".</font></td></tr></table></center>");
			}
			if (showNextEventName)
			{
				tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=0>");
				tb.append("<center><table bgcolor=2E2E2E width=282><tr><td align=center><font color=7f7f7f>The next event will be " + nextEvent + ".</font></td></tr></table></center>");
			}
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
			tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=10>");
		}
		tb.append("</body></html>");
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	protected void showEngineFeatures(PlayerEventInfo player, String returnPage)
	{
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Features</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=90 align=left> <font color=696969> Powered by:</font></td>");
		tb.append("<td width=130 align=left><font color=63AA1C><a action=\"bypass -h nxs_engine_info features\">Nexus Event Engine</a></font></td>");
		tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_" + returnPage + "_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=1>");
		
		tb.append("<br><br>");
		if (EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer"))
		{
			tb.append("<table bgcolor=2E2E2E width=270>");
			tb.append("<tr><td width=180 align=left><font color=8f8f8f>The nexus event scheme buffer can buff you during the event.</font></td><td width=90 align=left><button value=\"Buffer\" action=\"bypass -h nxs_buffer_menu " + returnPage + "\" width=70 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			tb.append("</table>");
			
			tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=5>");
		}
		if (EventConfig.getInstance().getGlobalConfigBoolean("enableStatistics"))
		{
			tb.append("<table width=295 bgcolor=2E2E2E>");
			tb.append("<tr><td with=200 align=left><font color=8f8f8f>View your and server's event statistics here.</font></td><td width=90 align=left><button value=\"Statistics\" action=\"bypass -h nxs_statistics_menu " + returnPage + "\" width=70 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
			tb.append("</table>");
			
			tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=5>");
		}
		tb.append("<table width=295 bgcolor=2E2E2E>");
		tb.append("<tr><td with=200 align=left><font color=8f8f8f>Get informations about your warnings count</font></td><td width=90 align=left><button value=\"Warnings\" action=\"bypass -h nxs_warnings_info " + returnPage + "\" width=70 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
		tb.append("</table>");
		
		tb.append("<br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left>");
		
		tb.append("<br>");
		
		tb.append("<center>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=1>");
		tb.append("<img src=\"L2UI.SquareGray\" width=295 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=3>");
		
		tb.append("<table width=100% bgcolor=3f3f3f>");
		tb.append("<tr><td width=100% align=center><font color=9f9f9f>This server is using <font color=9FBF80>Nexus Event engine</font></font></td></tr>");
		tb.append("<tr><td width=100% align=center><font color=9f9f9f>of version <font color=797979>2.21</font>, developed by <font color=BEA481>hNoke</font>.</font><br></td></tr>");
		tb.append("<tr><td width=100% align=center><font color=9f9f9f>For more informations visit<br1> <font color=BEA481>www.nexus-engine.net</font></font></td></tr>");
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=295 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=1>");
		
		tb.append("</center>");
		
		tb.append("</body></html>");
		
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	protected void showWarningInfo(PlayerEventInfo player, String returnPage)
	{
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Event Warning system</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=90 align=left> <font color=696969> Powered by:</font></td>");
		tb.append("<td width=130 align=left><font color=63AA1C><a action=\"bypass -h nxs_engine_info features\">Nexus Event Engine</a></font></td>");
		tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_" + returnPage + "_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=1>");
		
		tb.append("<center>");
		
		tb.append("<br><br><br><br><br><br><br>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=1>");
		tb.append("<img src=\"L2UI.SquareGray\" width=295 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=3>");
		
		tb.append("<table width=100% bgcolor=2f2f2f>");
		tb.append("<tr><td width=100% align=center><font color=ac9887>You currently have " + EventWarnings.getInstance().getPoints(player) + " warnings.</font></font></td></tr>");
		tb.append("<tr></tr><tr><td width=100% align=center><font color=9f9f9f>Whenever you do something bad in event, such as disconnect or attempt to exploit, you will receive 1 warning.</font><br></td></tr>");
		tb.append("<tr><td width=100% align=center><font color=9f9f9f>If you have 3 warnings, you will not be able to register to events. Warnings decrease by 1 every day.</font></td></tr>");
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=295 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=1>");
		
		tb.append("</center>");
		
		tb.append("</body></html>");
		
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	protected void showStatisticsMenu(PlayerEventInfo player, String returnPage)
	{
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Mini Events</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=90 align=left> <font color=696969> Powered by:</font></td>");
		tb.append("<td width=130 align=left><font color=63AA1C><a action=\"bypass -h nxs_engine_info features\">Nexus Event Engine</a></font></td>");
		tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_" + returnPage + "_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=1>");
		
		tb.append("<br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
		
		tb.append("<center><font color=B8A987>Global event statistics</font></center>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=2>");
		
		tb.append("<table width=290 bgcolor=2E2E2E><tr>");
		tb.append("<td align=left width=200><font color=8f8f8f>" + player.getPlayersName() + "'s stats for all events.</font></td>");
		tb.append("<td align=right width=90><button value=\"Player stats\" action=\"bypass -h nxs_showstats_global_oneplayer cbmenu\" width=90 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=5>");
		
		tb.append("<table width=290 bgcolor=2E2E2E><tr>");
		tb.append("<td align=left width=200><font color=8f8f8f>Global server stats for all events.</font></td>");
		tb.append("<td align=right width=90><button value=\"Global stats\" action=\"bypass -h nxs_showstats_global_topplayers 1 NAME\" width=90 height=22 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		tb.append("</tr></table>");
		
		tb.append("<br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left>");
		
		tb.append("<br><br><br>");
		
		tb.append("</body></html>");
		
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	protected void showEngineInfoPage(PlayerEventInfo player, String returnPage)
	{
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Mini Events</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=90 align=left> <font color=696969> Powered by:</font></td>");
		tb.append("<td width=130 align=left><font color=63AA1C><a action=\"bypass -h nxs_engine_info " + returnPage + "\">Nexus Event Engine</a></font></td>");
		if (returnPage.equals("none"))
		{
			tb.append("<td width=65 align=right></td>");
		}
		else
		{
			tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_" + returnPage + "_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=1>");
		
		tb.append("<br><br><br><br><br><br><br><br><br><br>");
		
		tb.append("<center>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=1>");
		tb.append("<img src=\"L2UI.SquareGray\" width=278 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=3>");
		
		tb.append("<table width=100% bgcolor=3f3f3f>");
		tb.append("<tr><td width=100% align=center><font color=9f9f9f>This server is using <font color=9FBF80>Nexus Event engine</font></font></td></tr>");
		tb.append("<tr><td width=100% align=center><font color=9f9f9f>of version <font color=797979>2.21</font>, developed by <font color=BEA481>hNoke</font>.</font><br></td></tr>");
		tb.append("<tr><td width=100% align=center><font color=9f9f9f>For more informations visit<br1> <font color=BEA481>www.nexus-engine.net</font></font></td></tr>");
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=278 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=1>");
		
		tb.append("<br><br><br><br><br><br><br><br><br><br><br><br>");
		
		tb.append("<center><font color=5F5F5F>If you find any problems, <br1>please contact me on my website.</font></center>");
		
		tb.append("</center>");
		
		tb.append("</body></html>");
		
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	@SuppressWarnings("null")
	public void showMiniEventsMenu(PlayerEventInfo player)
	{
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Mini Events</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=90 align=left> <font color=696969> Powered by:</font></td>");
		tb.append("<td width=130 align=left><font color=63AA1C><a action=\"bypass -h nxs_engine_info mini\">Nexus Event Engine</a></font></td>");
		tb.append("<td width=65 align=right><button value=\"Features\" action=\"bypass -h nxs_features_menu mini\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=1>");
		
		boolean bg = false;
		int size = 0;
		for (EventType type : EventType.values())
		{
			if ((EventConfig.getInstance().isEventAllowed(type)) && (!type.isRegularEvent()) && (type.allowEdits()) && (EventManager.getInstance().getMiniEvents().get(type) != null))
			{
				int countAlive = 0;
				for (Map.Entry<Integer, MiniEventManager> e : (EventManager.getInstance().getMiniEvents().get(type)).entrySet())
				{
					if ((e.getValue().getMode().isAllowed()) && (e.getValue().getMode().isRunning()))
					{
						countAlive++;
					}
				}
				if (countAlive != 0)
				{
					if (size > 0)
					{
						tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
						tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
						tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
					}
					tb.append("<table width=281 bgcolor=2E2E2E");
					tb.append("<tr><td width=200 align=left>");
					tb.append("<font color=ac9887>* " + type.getHtmlTitle() + "</font>");
					tb.append("</td>");
					
					tb.append("<td width=81 align=right>");
					tb.append("<font color=696969>Registered # </font>");
					tb.append("</td></tr>");
					tb.append("</table>");
					
					tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
					tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
					tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
					for (Map.Entry<Integer, MiniEventManager> e : (EventManager.getInstance().getMiniEvents().get(type)).entrySet())
					{
						if ((e.getValue().getMode().isAllowed()) && (e.getValue().getMode().isRunning()) && (e.getValue().canRun()))
						{
							tb.append("<table width=281><tr>");
							if ((player != null) && (player.getRegisteredMiniEvent() != null) && (player.getRegisteredMiniEvent().equals(e.getValue())))
							{
								tb.append("<td width=180><font color=494949>Mode</font><font color=595959> " + e.getValue().getMode().getModeId() + "</font>  <font color=D29115>" + e.getValue().getMode().getVisibleName() + "</font>" + "</td>");
							}
							else
							{
								tb.append("<td width=180><font color=494949>Mode</font><font color=595959> " + e.getValue().getMode().getModeId() + "</font>  <font color=9f9f9f>" + e.getValue().getMode().getVisibleName() + "</font>" + "</td>");
							}
							tb.append("<td width=70><a action=\"bypass -h nxs_mini_view " + e.getKey() + " " + type.getAltTitle() + "\"><font color=9f9f9f>View info</font></a></td>");
							
							tb.append("<td width=40 align=right>");
							try
							{
								int count = e.getValue().getRegisteredTeamsCount();
								tb.append("<font color=7A7A7A>" + count + "&nbsp;&nbsp;</font>");
							}
							catch (Exception ex)
							{
								NexusLoader.debug("sent invalid bypass, event " + type.getAltTitle());
								tb.append("<font color=B46F6B>N/A</font>");
							}
							tb.append("</td></tr></table>");
							size++;
							bg = !bg;
						}
					}
				}
			}
		}
		if (size == 0)
		{
			tb.append("<br><center><font color=B46F6B>There are no aviable mini events right now.</font>");
		}
		else
		{
			tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
			tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		}
		tb.append("</body></html>");
		
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	public void showMiniEventMenu(PlayerEventInfo player, EventType event, int modeId)
	{
		showMiniEventMenu(player, event, modeId, false, null, "nxs_mini_menu");
	}
	
	public void showMiniEventMenu(PlayerEventInfo player, EventType event, int modeId, boolean fullDesc, String position, String returnPage)
	{
		boolean noBack = false;
		if (returnPage.startsWith("nxs_mini_view"))
		{
			noBack = true;
		}
		MiniEventManager manager = EventManager.getInstance().getMiniEvent(event, modeId);
		if (manager == null)
		{
			player.sendStaticPacket();
			return;
		}
		TextBuilder tb = new TextBuilder();
		
		tb.append("<html><title>Mini Events</title><body>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=280 border=0 bgcolor=484848><tr>");
		tb.append("<td width=90 align=left> <font color=696969> Powered by:</font></td>");
		if (noBack)
		{
			tb.append("<td width=130 align=left><font color=63AA1C><a action=\"bypass -h nxs_engine_info none\">Nexus Event Engine</a></font></td>");
		}
		else
		{
			tb.append("<td width=130 align=left><font color=63AA1C><a action=\"bypass -h nxs_engine_info mini\">Nexus Event Engine</a></font></td>");
		}
		if (noBack)
		{
			tb.append("<td width=65 align=right></td>");
		}
		else
		{
			tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h " + returnPage + "\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
		}
		tb.append("</tr></table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=10>");
		
		tb.append("<table width=281 bgcolor=2E2E2E>");
		tb.append("<tr>");
		tb.append("<td width=130><font color=9f9f9f>&nbsp;" + manager.getEventType().getHtmlTitle() + "</font></td>");
		tb.append("<td width=140 align=right><font color=ac9887>" + manager.getMode().getVisibleName() + "&nbsp;</font></td>");
		
		tb.append("</tr>");
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=6>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		String desc = manager.getHtmlDescription();
		if ((!fullDesc) && (desc.length() > 170))
		{
			desc = desc.substring(0, 155) + "... &nbsp;<a action=\"bypass -h nxs_mini_expand_eventinfo " + modeId + " " + manager.getEventType().getAltTitle() + " " + returnPage + "\">[view full info]</a>";
		}
		if (fullDesc)
		{
			desc = desc + "&nbsp;<font color=696969><a action=\"bypass -h nxs_mini_view " + modeId + " " + manager.getEventType().getAltTitle() + " " + returnPage + "\">[hide full info]</a></font>";
		}
		tb.append("<table width=281 bgcolor=2E2E2E>");
		tb.append("<tr>");
		tb.append("<td width=281><font color=" + (fullDesc ? "898989" : "696969") + ">" + desc + "</font></td>");
		tb.append("</tr>");
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=6>");
		
		tb.append("<table width=281 bgcolor=2E2E2E>");
		
		tb.append("<tr>");
		tb.append("<td width=90><font color=9f9f9f>&nbsp;Players:</font></td>");
		tb.append("<td width=115 align=center><font color=99816C>" + manager.getRegisteredTeamsCount() + " </font><font color=9f9f9f>registered</font></td>");
		if (player.getRegisteredMiniEvent() == manager)
		{
			tb.append("<td width=75 align=right><a action=\"bypass -h nxs_mini_unregister " + modeId + " " + manager.getEventType().getAltTitle() + "\"><font color=A26D64>Unregister</font></a></td>");
		}
		else
		{
			tb.append("<td width=75 align=right><a action=\"bypass -h nxs_mini_register " + modeId + " " + manager.getEventType().getAltTitle() + "\"><font color=849D68>Register me!</font></a></a></td>");
		}
		tb.append("</tr>");
		
		tb.append("<tr>");
		
		tb.append("<td width=90><font color=9f9f9f>&nbsp;Information:</font></td>");
		if (noBack)
		{
			tb.append("<td width=50 align=center><font color=696969><a action=\"bypass -h nxs_mini_maps " + modeId + " " + manager.getEventType().getAltTitle() + " noback\">Show maps</a></font></td>");
			tb.append("<td width=120 align=right><font color=696969><a action=\"bypass -h nxs_mini_matches_menu " + modeId + " " + manager.getEventType().getAltTitle() + " noback\">Spectate match</a></font></td>");
		}
		else
		{
			tb.append("<td width=50 align=center><font color=696969><a action=\"bypass -h nxs_mini_maps " + modeId + " " + manager.getEventType().getAltTitle() + "\">Show maps</a></font></td>");
			tb.append("<td width=120 align=right><font color=696969><a action=\"bypass -h nxs_mini_matches_menu " + modeId + " " + manager.getEventType().getAltTitle() + "\">Spectate match</a></font></td>");
		}
		tb.append("</tr>");
		
		int minLevel = manager.getInt("MinLevelToJoin");
		int maxLevel = manager.getInt("MaxLevelToJoin");
		for (AbstractFeature f : manager.getMode().getFeatures())
		{
			if ((f instanceof LevelFeature))
			{
				if (minLevel < ((LevelFeature) f).getMinLevel())
				{
					minLevel = ((LevelFeature) f).getMinLevel();
				}
				if (maxLevel > ((LevelFeature) f).getMaxLevel())
				{
					maxLevel = ((LevelFeature) f).getMaxLevel();
				}
			}
		}
		tb.append("<tr>");
		
		tb.append("<td width=90><font color=9f9f9f>&nbsp;Restrictions:</font></td>");
		
		tb.append("<td width=95 align=center><font color=" + (player.getLevel() >= minLevel ? "6D8053" : "805353") + ">Min level " + minLevel + "</font></td>");
		tb.append("<td width=95 align=right><font color=" + (player.getLevel() <= maxLevel ? "6D8053" : "805353") + ">Max level " + maxLevel + "</font></td>");
		
		tb.append("</tr>");
		
		boolean needParty = manager.requireParty();
		int partySize = manager.getDefaultPartySizeToJoin();
		
		tb.append("<tr>");
		tb.append("<td width=110><font color=9f9f9f> Requirements:</font></td>");
		if (needParty)
		{
			boolean meets = false;
			if ((player.getParty() != null) && (player.getParty().getMemberCount() == partySize))
			{
				meets = true;
			}
			tb.append("<td width=50 align=left><font color=ac9887></font></td>");
			tb.append("<td width=120 align=right><font color=" + (meets ? "6D8053" : "805353") + ">Party of " + partySize + " people</font></td>");
		}
		else
		{
			tb.append("<td width=70 align=left><font color=ac9887></font></td>");
			tb.append("<td width=120 align=right><font color=6D8053>None (no party)</font></td>");
		}
		tb.append("</tr>");
		if ((EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer")) && (player.isRegistered()) && (player.getRegisteredMiniEvent() != null) && (player.getRegisteredMiniEvent().equals(manager)))
		{
			tb.append("<tr>");
			tb.append("<td width=115><font color=9f9f9f>&nbsp;Buffer scheme:</font></td>");
			tb.append("<td width=105 align=center><font color=CAA26A>" + EventBuffer.getInstance().getPlayersCurrentScheme(player.getPlayersId()) + "</font></td>");
			if (noBack)
			{
				tb.append("<td width=70 align=right><font color=729768><a action=\"bypass -h nxs_buffer_select_eventscheme_menu none " + manager.getEventType().getAltTitle() + "\">Change</a></font></td>");
			}
			else
			{
				tb.append("<td width=70 align=right><font color=729768><a action=\"bypass -h nxs_buffer_select_eventscheme_menu mini " + manager.getEventType().getAltTitle() + "\">Change</a></font></td>");
			}
			tb.append("</tr>");
		}
		tb.append("</table>");
		
		int haveToWait = manager.getDelayHaveToWaitToJoinAgain(player);
		if (haveToWait > 0)
		{
			boolean min = false;
			haveToWait /= 1000;
			if (haveToWait > 60)
			{
				min = true;
				haveToWait /= 60;
			}
			tb.append("<table width=281 bgcolor=2E2E2E");
			
			tb.append("<tr>");
			tb.append("<td width=281 align=center><font color=7D5555>You can register again in " + haveToWait + " " + (min ? "minutes" : "seconds") + ".</font></td>");
			tb.append("</tr>");
			
			tb.append("</table>");
		}
		tb.append("<table width=281 bgcolor=2E2E2E");
		
		tb.append("<tr>");
		tb.append("<td width=140 align=center></td>");
		tb.append("<td width=140 align=center></td>");
		tb.append("</tr>");
		
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=6>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
		
		tb.append("<table width=281 bgcolor=2E2E2E>");
		tb.append("<tr>");
		
		TextBuilder positions = new TextBuilder();
		
		EventRewardSystem.EventRewards rewards = EventRewardSystem.getInstance().getAllRewardsFor(manager.getEventType(), modeId);
		RewardPosition pos = null;
		if (position != null)
		{
			if (position.split("-").length > 1)
			{
				String from = position.split("-")[0];
				String to = position.split("-")[1];
				
				positions.append(from + " - " + to + " ;");
			}
			else
			{
				positions.append(position + ";");
			}
		}
		for (Map.Entry<EventRewardSystem.PositionContainer, Map<Integer, EventRewardSystem.RewardItem>> e : rewards.getAllRewards().entrySet())
		{
			if ((e.getValue() != null) && (!(e.getValue()).isEmpty()) && (e.getKey().position.posType != null))
			{
				pos = e.getKey().position;
				if (pos.posType == RewardPosition.PositionType.General)
				{
					if ((position == null) || (!position.equals(pos.toString())))
					{
						if ((pos == RewardPosition.Winner) || (pos == RewardPosition.Looser))
						{
							positions.append("" + pos.toString() + ";");
						}
					}
				}
				else if (pos.posType == RewardPosition.PositionType.Numbered)
				{
					if ((position == null) || (!(e.getKey().parameter + ".").equals(position)))
					{
						positions.append("" + e.getKey().parameter + ". ;");
					}
				}
				else if (pos.posType == RewardPosition.PositionType.Range)
				{
					try
					{
						int from = Integer.parseInt(e.getKey().parameter.split("-")[0]);
						int to = Integer.parseInt(e.getKey().parameter.split("-")[1]);
						if ((position != null) && ((from + ".-" + to + ".").equals(position)))
						{
							continue;
						}
						positions.append("" + from + ". - " + to + ".;");
					}
					catch (Exception e2)
					{
					}
				}
			}
		}
		boolean hasRewards = false;
		String positionsString = positions.toString();
		if (positionsString.length() > 0)
		{
			hasRewards = true;
			positionsString = positionsString.substring(0, positionsString.length() - 1);
		}
		if (hasRewards)
		{
			tb.append("<td width=110 align=left><font color=ac9887>");
			tb.append("&nbsp;Rewards:");
			tb.append("</font></td>");
			
			tb.append("<td width=75 align=right><font color=696969>");
			tb.append("<combobox width=72 height=17 var=pos list=\"" + positionsString + "\">");
			tb.append("</font></td>");
			
			tb.append("<td width=100 align=right><font color=696969>");
			tb.append("<button value=\"Show Rewards\" action=\"bypass -h nxs_mini_showrewards " + modeId + " " + manager.getEventType().getAltTitle() + " $pos\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
			tb.append("</font></td>");
		}
		else
		{
			tb.append("<td width=280 align=center><font color=9f9f9f>This event has no or only secret rewards.</font></td>");
		}
		tb.append("</tr>");
		tb.append("</table>");
		
		tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
		tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
		tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=7>");
		RewardPosition.PositionType posType;
		if (hasRewards)
		{
			if (position == null)
			{
				tb.append("<table width=281 bgcolor=2E2E2E>");
				tb.append("<tr>");
				
				tb.append("<td align=center width=281><font color=595959>Select a position to see the rewards.</font></td>");
				
				tb.append("</tr>");
				tb.append("</table>");
			}
			else
			{
				if (position.split("-").length > 1)
				{
					posType = RewardPosition.PositionType.Range;
				}
				else
				{
					if (position.endsWith("."))
					{
						posType = RewardPosition.PositionType.Numbered;
					}
					else
					{
						posType = RewardPosition.PositionType.General;
					}
				}
				for (Map.Entry<EventRewardSystem.PositionContainer, Map<Integer, EventRewardSystem.RewardItem>> e : rewards.getAllRewards().entrySet())
				{
					if ((e.getKey().position != null) && (e.getKey().position.posType == posType))
					{
						if (posType == RewardPosition.PositionType.General)
						{
							if (e.getKey().position.toString().equals(position))
							{
							}
						}
						else if (posType == RewardPosition.PositionType.Numbered)
						{
							if ((e.getKey().parameter + ".").equals(position))
							{
							}
						}
						else
						{
							try
							{
								String param = e.getKey().parameter;
								String from = param.split("-")[0];
								String to = param.split("-")[1];
								
								param = from + ".-" + to + ".";
								if (!param.equals(position))
								{
									continue;
								}
							}
							catch (Exception e2)
							{
							}
							continue;
						}
						int i = 0;
						bg = false;
						name = null;
						for (Map.Entry<Integer, EventRewardSystem.RewardItem> item : (e.getValue()).entrySet())
						{
							if (bg)
							{
								tb.append("<table width=281 bgcolor=2E2E2E>");
							}
							else
							{
								tb.append("<table width=281 bgcolor=3B3B3B>");
							}
							bg = !bg;
							
							tb.append("<tr>");
							if (item.getValue().id == -1)
							{
								name = "XP";
							}
							else if (item.getValue().id == -2)
							{
								name = "SP";
							}
							else if (item.getValue().id == -3)
							{
								name = "Fame";
							}
							else
							{
								name = CallBack.getInstance().getOut().getItemName(item.getValue().id);
							}
							if (name.length() > 27)
							{
								name = name.substring(0, 24) + "...";
							}
							int width = 170;
							if (name.length() < 5)
							{
								width = 100;
							}
							else if (name.length() < 12)
							{
								width = 130;
							}
							tb.append("<td width=" + width + " align=left> <font color=ac9887>" + ++i + ".</font> <font color=9f9f9f>" + name + "</font></td>");
							
							String min = null;
							String max = null;
							if (item.getValue().minAmmount >= 1000000000)
							{
								min = (int) Math.round(item.getValue().minAmmount / 1.0E9D) + "kkk";
							}
							else if (item.getValue().minAmmount >= 1000000)
							{
								min = (int) Math.round(item.getValue().minAmmount / 1000000.0D) + "kk";
							}
							else if (item.getValue().minAmmount >= 1000)
							{
								min = (int) Math.round(item.getValue().minAmmount / 1000.0D) + "k";
							}
							else
							{
								min = "" + item.getValue().minAmmount + "x";
							}
							if (item.getValue().maxAmmount >= 1000000000)
							{
								max = (int) Math.round(item.getValue().maxAmmount / 1.0E9D) + "kkk";
							}
							else if (item.getValue().maxAmmount >= 1000000)
							{
								max = (int) Math.round(item.getValue().maxAmmount / 1000000.0D) + "kk";
							}
							else if (item.getValue().maxAmmount >= 1000)
							{
								max = (int) Math.round(item.getValue().maxAmmount / 1000.0D) + "k";
							}
							else
							{
								max = "" + item.getValue().maxAmmount + "x";
							}
							if (item.getValue().minAmmount == item.getValue().maxAmmount)
							{
								tb.append("<td width=40 align=right><font color=595959>" + min + "</font></td>");
							}
							else
							{
								tb.append("<td width=50 align=right><font color=595959>" + min + "-" + max + "</font></td>");
							}
							tb.append("<td align=right><font color=666666>" + item.getValue().chance + "%</font></td>");
							tb.append("</tr>");
							
							tb.append("</table>");
							
							tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
						}
					}
				}
			}
		}
		tb.append("</body></html>");
		String html = tb.toString();
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	protected void showEventMaps(PlayerEventInfo player, EventType event, int modeId, boolean returnPage)
	{
		MiniEventManager manager = EventManager.getInstance().getMiniEvent(event, modeId);
		if (manager == null)
		{
			player.sendStaticPacket();
			return;
		}
		String html = CallBack.getInstance().getOut().getHtml("data/html/mods/nexus/maps.htm");
		
		TextBuilder tb = new TextBuilder();
		tb.append("<table width=260><font color=\"9f9f9f\">");
		for (EventMap map : EventMapSystem.getInstance().getMaps(event).values())
		{
			if (!manager.getMode().getDisMaps().contains(Integer.valueOf(map.getGlobalId())))
			{
				if (manager.canRun(map))
				{
					tb.append("<tr><td align=center>");
					tb.append("<font color=9f9f9f>" + map.getMapName() + "</font>");
					tb.append("</td></tr>");
				}
			}
		}
		tb.append("</font></table>");
		
		html = html.replaceAll("%maps%", tb.toString());
		html = html.replaceAll("%event%", event.getAltTitle());
		html = html.replaceAll("%modeId%", Integer.toString(modeId));
		html = html.replaceAll("%modeName%", manager.getMode().getVisibleName());
		if (returnPage)
		{
			html = html.replaceAll("%returnPage%", "<button value=\"Back\" action=\"bypass -h nxs_mini_view " + modeId + " " + event.getAltTitle() + "\" width=70 height=19 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		else
		{
			html = html.replaceAll("%returnPage%", "");
		}
		player.sendHtmlText(html);
	}
	
	public void showSpectateableGames(PlayerEventInfo player, EventType event, int modeId, boolean returnPage)
	{
		MiniEventManager manager = EventManager.getInstance().getMiniEvent(event, modeId);
		if (manager == null)
		{
			player.sendStaticPacket();
			return;
		}
		String html = CallBack.getInstance().getOut().getHtml("data/html/mods/nexus/spectator_menu.htm");
		
		html = html.replaceAll("%type%", event.getAltTitle());
		html = html.replaceAll("%playersSize%", String.valueOf(EventManager.getInstance().getMiniEvent(event, modeId).getRegisteredTeamsCount()));
		html = html.replaceAll("%modeId%", Integer.toString(modeId));
		html = html.replaceAll("%modeName%", manager.getMode().getVisibleName());
		
		TextBuilder tb = new TextBuilder();
		if (manager.getActiveGames().isEmpty())
		{
			tb.append("<font color=815252>There's no active match at the moment.</font>");
		}
		else
		{
			for (MiniEventGame game : manager.getActiveGames())
			{
				tb.append("<button value=\"");
				
				int i = 1;
				for (EventTeam t : game.getTeams())
				{
					tb.append(t.getTeamName());
					if (i < game.getTeams().length)
					{
						tb.append(" -vs- ");
					}
					i++;
				}
				tb.append("\" action=\"bypass -h nxs_mini_spectate_game " + modeId + " " + event.getAltTitle() + " " + game.getGameId() + "\" width=190 height=19 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
				tb.append("<br1>");
			}
		}
		html = html.replaceAll("%games%", tb.toString());
		html = html.replaceAll("%title%", "Nexus Engine");
		if (returnPage)
		{
			html = html.replaceAll("%returnPage%", "<button value=\"Back\" action=\"bypass -h nxs_mini_view " + modeId + " " + event.getAltTitle() + "\" width=70 height=19 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
		}
		else
		{
			html = html.replaceAll("%returnPage%", "");
		}
		player.sendHtmlText(html);
	}
	
	public void showWindowRegistered(PlayerEventInfo player)
	{
		String html = CallBack.getInstance().getOut().getHtml("data/html/mods/nexus/registered.htm");
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	public void showWindowUnregistered(PlayerEventInfo player)
	{
		String html = CallBack.getInstance().getOut().getHtml("data/html/mods/nexus/unregistered.htm");
		
		player.sendHtmlText(html);
		player.sendStaticPacket();
	}
	
	protected class RegisteredPlayerList
	{
		int rows;
		String list;
		
		protected RegisteredPlayerList()
		{
		}
	}
}
