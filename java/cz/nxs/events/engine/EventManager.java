/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.NexusEvents
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.NpcData
 *  cz.nxs.interf.delegate.SkillData
 *  cz.nxs.interf.handlers.AdminCommandHandlerInstance
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine;

import java.util.Comparator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import javolution.util.FastMap;
import cz.nxs.events.Configurable;
import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.base.Event;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.html.EventHtmlManager;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.mini.MiniEventGame;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.mini.events.KoreanManager;
import cz.nxs.events.engine.mini.events.MiniTvTManager;
import cz.nxs.events.engine.mini.events.OnevsOneManager;
import cz.nxs.events.engine.mini.events.PartyvsPartyManager;
import cz.nxs.events.engine.stats.EventStatsManager;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.NexusEvents;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.SkillDataEvent;
import cz.nxs.interf.handlers.AdminCommandHandlerInstance;
import cz.nxs.l2j.CallBack;

public class EventManager
{
	private Map<EventType, Map<Integer, MiniEventManager>> _miniEvents;
	private Map<EventType, AbstractMainEvent> _mainEvents;
	private Map<DisconnectedPlayerData, Long> _disconnectedPlayers;
	private MainEventManager _mainManager;
	private EventHtmlManager _html;
	public static boolean ALLOW_VOICE_COMMANDS = EventConfig.getInstance().getGlobalConfigBoolean("allowVoicedCommands");
	public static String REGISTER_VOICE_COMMAND = EventConfig.getInstance().getGlobalConfigValue("registerVoicedCommand");
	public static String UNREGISTER_VOICE_COMMAND = EventConfig.getInstance().getGlobalConfigValue("unregisterVoicedCommand");
	public Comparator<PlayerEventInfo> compareByLevels;
	public Comparator<PlayerEventInfo> compareByPvps;
	public Comparator<EventTeam> compareTeamKills;
	public Comparator<PlayerEventInfo> comparePlayersKills;
	public Comparator<PlayerEventInfo> comparePlayersScore;
	public Comparator<EventTeam> compareTeamScore;
	
	public EventManager()
	{
		this.compareByLevels = (o1, o2) ->
		{
			int level2;
			int level1 = o1.getLevel();
			return level1 == (level2 = o2.getLevel()) ? 0 : (level1 < level2 ? 1 : -1);
		};
		this.compareByPvps = (o1, o2) ->
		{
			int pvp2;
			int pvp1 = o1.getPvpKills();
			return pvp1 == (pvp2 = o2.getPvpKills()) ? 0 : (pvp1 < pvp2 ? 1 : -1);
		};
		this.compareTeamKills = (t1, t2) ->
		{
			int kills2;
			int kills1 = t1.getKills();
			return kills1 == (kills2 = t2.getKills()) ? 0 : (kills1 < kills2 ? 1 : -1);
		};
		this.comparePlayersKills = (p1, p2) ->
		{
			int kills2;
			int kills1 = p1.getKills();
			return kills1 == (kills2 = p2.getKills()) ? 0 : (kills1 < kills2 ? 1 : -1);
		};
		this.comparePlayersScore = (p1, p2) ->
		{
			int score2;
			int score1 = p1.getScore();
			if (score1 == (score2 = p2.getScore()))
			{
				int deaths2;
				int deaths1 = p1.getDeaths();
				return deaths1 == (deaths2 = p2.getDeaths()) ? 0 : (deaths1 < deaths2 ? -1 : 1);
			}
			return score1 < score2 ? 1 : -1;
		};
		this.compareTeamScore = (t1, t2) ->
		{
			int score2;
			int score1 = t1.getScore();
			return score1 == (score2 = t2.getScore()) ? 0 : (score1 < score2 ? 1 : -1);
		};
		CallBack.getInstance().getOut().registerAdminHandler(new AdminNexus());
		this._miniEvents = new FastMap<>(EventType.values().length);
		this._mainEvents = new FastMap<>(EventType.values().length);
		NexusLoader.debug("Nexus engine: Loading events...");
		this.loadEvents();
	}
	
	private void loadEvents()
	{
		int count = 0;
		this._disconnectedPlayers = new FastMap<>();
		this._mainManager = new MainEventManager();
		for (EventType event : EventType.values())
		{
			Event eventInstance;
			if ((event == EventType.Unassigned) || ((eventInstance = event.loadEvent(this._mainManager)) == null) || !event.isRegularEvent())
			{
				continue;
			}
			this._mainEvents.put(eventInstance.getEventType(), (AbstractMainEvent) eventInstance);
			++count;
		}
		NexusLoader.debug("Nexus engine: Loaded " + count + " main events.");
		this._miniEvents.put(EventType.Unassigned, new FastMap<>());
		this._miniEvents.put(EventType.Classic_1v1, new FastMap<>());
		this._miniEvents.put(EventType.PartyvsParty, new FastMap<>());
		this._miniEvents.put(EventType.Korean, new FastMap<>());
		this._miniEvents.put(EventType.MiniTvT, new FastMap<>());
		NexusLoader.debug("Nexus engine: Loaded " + _miniEvents.size() + " mini event types.");
	}
	
	public void setHtmlManager(EventHtmlManager manager)
	{
		this._html = manager;
	}
	
	public MiniEventManager createManager(EventType type, int modeId, String name, String visibleName, boolean loadConfigs)
	{
		Configurable defaultMode;
		MiniEventManager manager = null;
		switch (type)
		{
			case Classic_1v1:
			{
				manager = new OnevsOneManager(type);
				break;
			}
			case PartyvsParty:
			{
				manager = new PartyvsPartyManager(type);
				break;
			}
			case Korean:
			{
				manager = new KoreanManager(type);
				break;
			}
			case MiniTvT:
			{
				manager = new MiniTvTManager(type);
				break;
			}
			default:
			{
				NexusLoader.debug("Event " + type.getAltTitle() + " isn't implemented yet.", Level.WARNING);
				return null;
			}
		}
		manager.getMode().setModeName(name);
		manager.getMode().setVisibleName(visibleName);
		if (loadConfigs && ((defaultMode = this.getEvent(type, 1)) != null))
		{
			manager.setConfigs(defaultMode);
		}
		this._miniEvents.get(type).put(modeId, manager);
		return manager;
	}
	
	public static final EventManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public Map<EventType, Map<Integer, MiniEventManager>> getMiniEvents()
	{
		return this._miniEvents;
	}
	
	public Map<EventType, AbstractMainEvent> getMainEvents()
	{
		return this._mainEvents;
	}
	
	public Configurable getEvent(EventType type)
	{
		return this.getEvent(type, 1);
	}
	
	public Configurable getEvent(EventType type, int modeId)
	{
		if (type.isRegularEvent())
		{
			return this.getMainEvent(type);
		}
		return this.getMiniEvent(type, modeId);
	}
	
	public MiniEventManager getMiniEvent(EventType type, int id)
	{
		if (this._miniEvents.get(type) == null)
		{
			return null;
		}
		return this._miniEvents.get(type).get(id);
	}
	
	public AbstractMainEvent getMainEvent(EventType type)
	{
		if (!this._mainEvents.containsKey(type))
		{
			return null;
		}
		return this._mainEvents.get(type);
	}
	
	public AbstractMainEvent getCurrentMainEvent()
	{
		return this._mainManager.getCurrent();
	}
	
	public boolean onBypass(PlayerEventInfo player, String bypass)
	{
		return this._html.onBypass(player, bypass);
	}
	
	public boolean showNpcHtml(PlayerEventInfo player, NpcData npc)
	{
		return this._html.showNpcHtml(player, npc);
	}
	
	public EventHtmlManager getHtmlManager()
	{
		return this._html;
	}
	
	public boolean canRegister(PlayerEventInfo player)
	{
		if (player.isInJail())
		{
			player.sendMessage(LanguageEngine.getMsg("registering_jail"));
			return false;
		}
		if (player.isInSiege())
		{
			player.sendMessage(LanguageEngine.getMsg("registering_siege"));
			return false;
		}
		if (player.isInDuel())
		{
			player.sendMessage(LanguageEngine.getMsg("registering_duel"));
			return false;
		}
		if (player.isOlympiadRegistered() || player.isInOlympiadMode())
		{
			player.sendMessage(LanguageEngine.getMsg("registering_olympiad"));
			return false;
		}
		if (player.getKarma() > 0)
		{
			player.sendMessage(LanguageEngine.getMsg("registering_karma"));
			return false;
		}
		if (player.isCursedWeaponEquipped())
		{
			player.sendMessage(LanguageEngine.getMsg("registering_cursedWeapon"));
			return false;
		}
		return true;
	}
	
	public boolean isInEvent(CharacterData cha)
	{
		if (this.getCurrentMainEvent() != null)
		{
			return this.getCurrentMainEvent().isInEvent(cha);
		}
		return false;
	}
	
	public boolean allowDie(CharacterData cha, CharacterData killer)
	{
		if (this.getCurrentMainEvent() != null)
		{
			return this.getCurrentMainEvent().allowKill(cha, killer);
		}
		return true;
	}
	
	public void onDamageGive(CharacterData cha, CharacterData attacker, int damage, boolean isDOT)
	{
		if (this.getCurrentMainEvent() != null)
		{
			this.getCurrentMainEvent().onDamageGive(attacker, cha, damage, isDOT);
		}
	}
	
	public boolean onAttack(CharacterData cha, CharacterData target)
	{
		if (this.getCurrentMainEvent() != null)
		{
			return this.getCurrentMainEvent().onAttack(cha, target);
		}
		return true;
	}
	
	public boolean tryVoicedCommand(PlayerEventInfo player, String text)
	{
		if ((player != null) && ALLOW_VOICE_COMMANDS)
		{
			if (text.equalsIgnoreCase(REGISTER_VOICE_COMMAND))
			{
				EventManager.getInstance().getMainEventManager().registerPlayer(player);
				return true;
			}
			if (text.equalsIgnoreCase(UNREGISTER_VOICE_COMMAND))
			{
				EventManager.getInstance().getMainEventManager().unregisterPlayer(player, false);
				return true;
			}
			if (text.equalsIgnoreCase(".suicide") && player.isInEvent())
			{
				player.sendMessage("You are being suicided.");
				player.doDie();
				return true;
			}
		}
		return false;
	}
	
	public void removeEventSkills(PlayerEventInfo player)
	{
		for (SkillDataEvent sk : player.getSkills())
		{
			if ((sk.getId() < 35000) || (sk.getId() > 35099))
			{
				continue;
			}
			player.removeBuff(sk.getId());
			player.removeSkill(sk.getId());
		}
	}
	
	public void onPlayerLogin(final PlayerEventInfo player)
	{
		this.removeEventSkills(player);
		EventStatsManager.getInstance().onLogin(player);
		DisconnectedPlayerData data = null;
		for (Map.Entry<DisconnectedPlayerData, Long> e : this._disconnectedPlayers.entrySet())
		{
			if (e.getKey().player.getPlayersId() != player.getPlayersId())
			{
				continue;
			}
			data = e.getKey();
			this._disconnectedPlayers.remove(e.getKey());
			break;
		}
		if (data != null)
		{
			final DisconnectedPlayerData fData = data;
			final EventGame event = data.event;
			if (event != null)
			{
				CallBack.getInstance().getOut().scheduleGeneral(() -> event.addDisconnectedPlayer(player, fData), 1500);
			}
		}
	}
	
	public void addDisconnectedPlayer(PlayerEventInfo player, EventTeam team, EventPlayerData d, EventGame event)
	{
		long time = System.currentTimeMillis();
		DisconnectedPlayerData data = new DisconnectedPlayerData(player, event, d, team, time, player.getInstanceId());
		this._disconnectedPlayers.put(data, time);
	}
	
	public void clearDisconnectedPlayers()
	{
		this._disconnectedPlayers.clear();
	}
	
	public void spectateGame(PlayerEventInfo player, EventType event, int modeId, int gameId)
	{
		MiniEventManager manager = this.getMiniEvent(event, modeId);
		if (manager == null)
		{
			player.sendStaticPacket();
			return;
		}
		MiniEventGame game = null;
		for (MiniEventGame g : manager.getActiveGames())
		{
			if (g.getGameId() != gameId)
			{
				continue;
			}
			game = g;
			break;
		}
		if (game == null)
		{
			player.sendMessage(LanguageEngine.getMsg("observing_gameEnded"));
			return;
		}
		if (!this.canRegister(player))
		{
			player.sendMessage(LanguageEngine.getMsg("observing_cant"));
			return;
		}
		if (player.isRegistered())
		{
			player.sendMessage(LanguageEngine.getMsg("observing_alreadyRegistered"));
			return;
		}
		CallBack.getInstance().getPlayerBase().addInfo(player);
		player.initOrigInfo();
		game.addSpectator(player);
	}
	
	public void removePlayerFromObserverMode(PlayerEventInfo pi)
	{
		MiniEventGame game = pi.getActiveGame();
		if (game == null)
		{
			return;
		}
		game.removeSpectator(pi, false);
	}
	
	public String getDarkColorForHtml(int teamId)
	{
		switch (teamId)
		{
			case 1:
			{
				return "7C8194";
			}
			case 2:
			{
				return "987878";
			}
			case 3:
			{
				return "868F81";
			}
			case 4:
			{
				return "937D8D";
			}
			case 5:
			{
				return "93937D";
			}
			case 6:
			{
				return "D2934D";
			}
			case 7:
			{
				return "3EC1C1";
			}
			case 8:
			{
				return "D696D1";
			}
			case 9:
			{
				return "9B7957";
			}
			case 10:
			{
				return "949494";
			}
		}
		return "8f8f8f";
	}
	
	public String getTeamColorForHtml(int teamId)
	{
		switch (teamId)
		{
			case 1:
			{
				return "5083CF";
			}
			case 2:
			{
				return "D04F4F";
			}
			case 3:
			{
				return "56C965";
			}
			case 4:
			{
				return "9F52CD";
			}
			case 5:
			{
				return "DAC73D";
			}
			case 6:
			{
				return "D2934D";
			}
			case 7:
			{
				return "3EC1C1";
			}
			case 8:
			{
				return "D696D1";
			}
			case 9:
			{
				return "9B7957";
			}
			case 10:
			{
				return "949494";
			}
		}
		return "FFFFFF";
	}
	
	public int getTeamColorForName(int teamId)
	{
		switch (teamId)
		{
			case 1:
			{
				return 13599568;
			}
			case 2:
			{
				return 5197776;
			}
			case 3:
			{
				return 6670678;
			}
			case 4:
			{
				return 13456031;
			}
			case 5:
			{
				return 4048858;
			}
			case 6:
			{
				return 5084114;
			}
			case 7:
			{
				return 12697918;
			}
			case 8:
			{
				return 13735638;
			}
			case 9:
			{
				return 5732763;
			}
			case 10:
			{
				return 9737364;
			}
		}
		return 0;
	}
	
	public String getTeamName(int teamId)
	{
		switch (teamId)
		{
			case 1:
			{
				return "Blue";
			}
			case 2:
			{
				return "Red";
			}
			case 3:
			{
				return "Green";
			}
			case 4:
			{
				return "Purple";
			}
			case 5:
			{
				return "Yellow";
			}
			case 6:
			{
				return "Orange";
			}
			case 7:
			{
				return "Teal";
			}
			case 8:
			{
				return "Pink";
			}
			case 9:
			{
				return "Brown";
			}
			case 10:
			{
				return "Grey";
			}
		}
		return "No";
	}
	
	public void debug(String message)
	{
		NexusLoader.debug(message);
	}
	
	public void debug(Exception e)
	{
		e.printStackTrace();
	}
	
	public MainEventManager getMainEventManager()
	{
		return this._mainManager;
	}
	
	private static class SingletonHolder
	{
		protected static final EventManager _instance = new EventManager();
		
		private SingletonHolder()
		{
		}
	}
	
	public class AdminNexus extends AdminCommandHandlerInstance
	{
		private final String[] ADMIN_COMMANDS;
		
		public AdminNexus()
		{
			this.ADMIN_COMMANDS = new String[]
			{
				"admin_event_manage"
			};
		}
		
		@Override
		public boolean useAdminCommand(String command, PlayerEventInfo activeChar)
		{
			if (command.startsWith("admin_event_manage"))
			{
				StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				if (!st.hasMoreTokens())
				{
					NexusEvents.onAdminBypass(activeChar, "menu");
				}
				else
				{
					NexusEvents.onAdminBypass(activeChar, command.substring(19));
				}
			}
			return true;
		}
		
		@Override
		public String[] getAdminCommandList()
		{
			return this.ADMIN_COMMANDS;
		}
	}
	
	public class DisconnectedPlayerData
	{
		protected PlayerEventInfo player;
		protected EventGame event;
		private final EventPlayerData data;
		private final EventTeam team;
		private final long time;
		private final int instance;
		
		public DisconnectedPlayerData(PlayerEventInfo player, EventGame event, EventPlayerData data, EventTeam team, long time, int instance)
		{
			this.time = time;
			this.player = player;
			this.data = data;
			this.team = team;
			this.event = event;
			this.instance = instance;
		}
		
		public PlayerEventInfo getPlayer()
		{
			return this.player;
		}
		
		public EventGame getEvent()
		{
			return this.event;
		}
		
		public EventTeam getTeam()
		{
			return this.team;
		}
		
		public EventPlayerData getPlayerData()
		{
			return this.data;
		}
		
		public long getTime()
		{
			return this.time;
		}
		
		public int getInstance()
		{
			return this.instance;
		}
	}
	
}
