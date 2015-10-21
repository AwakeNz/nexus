/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.DoorData
 *  cz.nxs.interf.delegate.FenceData
 *  cz.nxs.interf.delegate.InstanceData
 *  cz.nxs.interf.delegate.ItemData
 *  cz.nxs.interf.delegate.NpcData
 *  cz.nxs.interf.delegate.NpcTemplateData
 *  cz.nxs.interf.delegate.PartyData
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.mini;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastList;
import javolution.util.FastMap;
import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.events.engine.mini.features.BufferFeature;
import cz.nxs.events.engine.mini.features.EnchantFeature;
import cz.nxs.events.engine.mini.features.ItemGradesFeature;
import cz.nxs.events.engine.mini.features.ItemsFeature;
import cz.nxs.events.engine.mini.features.SkillsFeature;
import cz.nxs.events.engine.mini.features.TimeLimitFeature;
import cz.nxs.events.engine.stats.EventStatsManager;
import cz.nxs.events.engine.stats.GlobalStats;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.DoorData;
import cz.nxs.interf.delegate.FenceData;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.NpcTemplateData;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;

public abstract class MiniEventGame implements Runnable, EventGame
{
	protected static Logger _log = Logger.getLogger(MiniEventGame.class.getName());
	public static final int MAP_GUARD_ID = 9996;
	protected int _instanceId;
	protected int _gameId;
	protected MiniEventManager _event;
	protected EventMap _arena;
	protected Announcer _announcer;
	private LocChecker _locCheckerInstance;
	protected ScheduledFuture<?> _locChecker = null;
	private static int _locCheckDelay = 10000;
	protected List<PlayerEventInfo> _spectators;
	List<PlayerEventInfo> _voted;
	protected List<FenceData> _fences;
	protected List<NpcData> _buffers;
	protected List<NpcData> _mapGuards;
	protected List<NpcData> _npcs;
	protected boolean _aborted = false;
	private int[] notAllovedSkillls;
	private int[] notAllovedItems;
	private int[] setOffensiveSkills;
	private int[] setNotOffensiveSkills;
	private int[] setNeutralSkills;
	protected boolean _allowSchemeBuffer;
	protected boolean _allowSummons;
	protected boolean _allowPets;
	
	public MiniEventGame(int gameId, EventMap arena, MiniEventManager event, RegistrationData[] teams)
	{
		_gameId = gameId;
		_event = event;
		_arena = arena;
		_instanceId = 0;
	}
	
	public abstract int getInstanceId();
	
	public abstract EventTeam[] getTeams();
	
	protected void initAnnouncer()
	{
		_announcer = new Announcer();
		_announcer.setTime(System.currentTimeMillis() + getGameTime());
	}
	
	@Override
	public void run()
	{
		initEvent();
	}
	
	public void scheduleLocChecker()
	{
		if (_locCheckerInstance == null)
		{
			_locCheckerInstance = new LocChecker();
		}
		_locChecker = CallBack.getInstance().getOut().scheduleGeneral(_locCheckerInstance, _locCheckDelay);
	}
	
	protected abstract void checkPlayersLoc();
	
	protected abstract void checkIfPlayersTeleported();
	
	public void addSpectator(PlayerEventInfo player)
	{
		EventSpawn spectatorLoc;
		if (_spectators == null)
		{
			_spectators = new FastList<>();
		}
		if ((spectatorLoc = getMap().getNextSpawn(-1, SpawnType.Spectator)) == null)
		{
			spectatorLoc = getMap().getNextSpawn(-1, SpawnType.Regular);
		}
		if (spectatorLoc == null)
		{
			player.sendMessage(LanguageEngine.getMsg("observing_noSpawn"));
			return;
		}
		player.setIsSpectator(true);
		player.setActiveGame(this);
		player.removeSummon();
		player.removeCubics();
		if (player.getParty() != null)
		{
			player.getParty().removePartyMember(player);
		}
		player.setInstanceId(this._instanceId);
		player.enterObserverMode(spectatorLoc.getLoc().getX(), spectatorLoc.getLoc().getY(), spectatorLoc.getLoc().getZ());
		_spectators.add(player);
	}
	
	public void removeSpectator(PlayerEventInfo pi, boolean disconnect)
	{
		if (!pi.isOnline())
		{
			return;
		}
		if (!disconnect)
		{
			pi.removeObserveMode();
			CallBack.getInstance().getPlayerBase().eventEnd(pi);
		}
		_spectators.remove(pi);
	}
	
	protected void cleanSpectators()
	{
		if (_spectators != null)
		{
			for (PlayerEventInfo pi : _spectators)
			{
				removeSpectator(pi, false);
			}
		}
	}
	
	protected void initEvent()
	{
		String[] splits;
		int i;
		_instanceId = CallBack.getInstance().getOut().createInstance("Game " + getEvent().getEventName() + " ID" + _gameId, getGameTime() + 59000, 0, true).getId();
		handleDoors(0);
		loadFences();
		CallBack.getInstance().getOut().spawnFences(_fences, _instanceId);
		loadNpcs();
		loadMapGuards();
		initAnnouncer();
		_allowSchemeBuffer = EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer");
		_allowSummons = getEvent().getBoolean("allowSummons");
		_allowPets = getEvent().getBoolean("allowPets");
		if (!_event.getString("notAllowedSkills").equals(""))
		{
			splits = _event.getString("notAllowedSkills").split(",");
			notAllovedSkillls = new int[splits.length];
			try
			{
				for (i = 0; i < splits.length; ++i)
				{
					notAllovedSkillls[i] = Integer.parseInt(splits[i]);
				}
				Arrays.sort(notAllovedSkillls);
			}
			catch (Exception e)
			{
				// empty catch block
			}
		}
		if (!_event.getString("notAllowedItems").equals(""))
		{
			splits = _event.getString("notAllowedItems").split(",");
			notAllovedItems = new int[splits.length];
			try
			{
				for (i = 0; i < splits.length; ++i)
				{
					notAllovedItems[i] = Integer.parseInt(splits[i]);
				}
				Arrays.sort(notAllovedItems);
			}
			catch (Exception e)
			{
				// empty catch block
			}
		}
		loadOverridenSkillsParameters();
	}
	
	private void loadOverridenSkillsParameters()
	{
		String[] splits;
		int i;
		String s = EventConfig.getInstance().getGlobalConfigValue("setOffensiveSkills");
		try
		{
			splits = s.split(";");
			setOffensiveSkills = new int[splits.length];
			try
			{
				for (i = 0; i < splits.length; ++i)
				{
					setOffensiveSkills[i] = Integer.parseInt(splits[i]);
				}
				Arrays.sort(setOffensiveSkills);
			}
			catch (Exception e)
			{
				NexusLoader.debug("Error while loading GLOBAL config 'setOffensiveSkills' in event " + _event.getEventName() + " - " + e.toString(), Level.SEVERE);
			}
		}
		catch (Exception e)
		{
			NexusLoader.debug("Error while loading GLOBAL config 'setOffensiveSkills' in event " + _event.getEventName() + " - " + e.toString(), Level.SEVERE);
		}
		s = EventConfig.getInstance().getGlobalConfigValue("setNotOffensiveSkills");
		try
		{
			splits = s.split(";");
			setNotOffensiveSkills = new int[splits.length];
			try
			{
				for (i = 0; i < splits.length; ++i)
				{
					setNotOffensiveSkills[i] = Integer.parseInt(splits[i]);
				}
				Arrays.sort(setNotOffensiveSkills);
			}
			catch (Exception e)
			{
				NexusLoader.debug("Error while loading GLOBAL config 'setNotOffensiveSkills' in event " + _event.getEventName() + " - " + e.toString(), Level.SEVERE);
			}
		}
		catch (Exception e)
		{
			NexusLoader.debug("Error while loading GLOBAL config 'setNotOffensiveSkills' in event " + _event.getEventName() + " - " + e.toString(), Level.SEVERE);
		}
		s = EventConfig.getInstance().getGlobalConfigValue("setNeutralSkills");
		try
		{
			splits = s.split(";");
			setNeutralSkills = new int[splits.length];
			try
			{
				for (i = 0; i < splits.length; ++i)
				{
					setNeutralSkills[i] = Integer.parseInt(splits[i]);
				}
				Arrays.sort(setNeutralSkills);
			}
			catch (Exception e)
			{
				NexusLoader.debug("Error while loading GLOBAL config 'setNeutralSkills' in event " + _event.getEventName() + " - " + e.toString(), Level.SEVERE);
			}
		}
		catch (Exception e)
		{
			NexusLoader.debug("Error while loading GLOBAL config 'setNeutralSkills' in event " + _event.getEventName() + " - " + e.toString(), Level.SEVERE);
		}
	}
	
	public int isSkillOffensive(SkillData skill)
	{
		if ((setOffensiveSkills != null) && (Arrays.binarySearch(setOffensiveSkills, skill.getId()) >= 0))
		{
			return 1;
		}
		if ((setNotOffensiveSkills != null) && (Arrays.binarySearch(setNotOffensiveSkills, skill.getId()) >= 0))
		{
			return 0;
		}
		return -1;
	}
	
	public boolean isSkillNeutral(SkillData skill)
	{
		if ((setNeutralSkills != null) && (Arrays.binarySearch(setNeutralSkills, skill.getId()) >= 0))
		{
			return true;
		}
		return false;
	}
	
	protected void updateScore(PlayerEventInfo player, CharacterData killer)
	{
		player.raiseDeaths(1);
		player.getEventTeam().raiseDeaths(1);
		if ((killer != null) && (killer.getEventInfo() != null))
		{
			if (killer.getEventInfo().getEventTeam() == null)
			{
				return;
			}
			killer.getEventInfo().raiseKills(1);
			killer.getEventInfo().getEventTeam().raiseKills(1);
		}
	}
	
	protected void startEvent()
	{
	}
	
	protected void setEndStatus(PlayerEventInfo pi, int status)
	{
	}
	
	public void applyStatsChanges()
	{
	}
	
	protected void onScore(List<PlayerEventInfo> players, int ammount)
	{
	}
	
	protected void abortDueToError(String message)
	{
		broadcastMessage(message, false);
		clearEvent();
		EventManager.getInstance().debug(_event.getEventType() + " match automatically aborted: " + message);
	}
	
	public void broadcastMessage(String msg, boolean abortable)
	{
		if (abortable && _aborted)
		{
			return;
		}
		for (EventTeam team : getTeams())
		{
			for (PlayerEventInfo pi : team.getPlayers())
			{
				pi.screenMessage(msg, getEvent().getEventName(), false);
			}
		}
		if (this._spectators != null)
		{
			for (PlayerEventInfo pi : _spectators)
			{
				pi.screenMessage(msg, getEvent().getEventName(), false);
			}
		}
	}
	
	protected boolean checkTeamStatus(int teamId)
	{
		for (PlayerEventInfo pi : getTeams()[teamId - 1].getPlayers())
		{
			if (!pi.isOnline() || pi.isDead())
			{
				continue;
			}
			return true;
		}
		return false;
	}
	
	protected void loadFences()
	{
		try
		{
			_fences = new FastList<>();
			for (EventSpawn spawn : _arena.getSpawns(0, SpawnType.Fence))
			{
				_fences.add(CallBack.getInstance().getOut().createFence(2, spawn.getFenceWidth(), spawn.getFenceLength(), spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ(), _arena.getGlobalId()));
			}
		}
		catch (NullPointerException e)
		{
			// empty catch block
		}
	}
	
	private void loadMapGuards()
	{
		int id = EventConfig.getInstance().getGlobalConfigInt("mapGuardNpcId");
		if (id == -1)
		{
			return;
		}
		NpcTemplateData template = new NpcTemplateData(id);
		if (!template.exists())
		{
			_log.warning("Missing template for EventMap Guard.");
			return;
		}
		for (EventSpawn spawn : getMap().getSpawns())
		{
			if (spawn.getSpawnType() != SpawnType.MapGuard)
			{
				continue;
			}
			try
			{
				NpcData data = template.doSpawn(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ(), 1, _instanceId);
				if (_mapGuards == null)
				{
					_mapGuards = new FastList<>();
				}
				_mapGuards.add(data);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	protected void loadNpcs()
	{
		for (EventSpawn spawn : getMap().getSpawns(-1, SpawnType.Npc))
		{
			try
			{
				int npcId = spawn.getNpcId();
				if (npcId == -1)
				{
					continue;
				}
				NpcData data = new NpcTemplateData(npcId).doSpawn(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ(), 1, _instanceId);
				if (_npcs == null)
				{
					_npcs = new FastList<>();
				}
				_npcs.add(data);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	protected void unspawnNpcs()
	{
		if (_npcs != null)
		{
			for (NpcData npc : _npcs)
			{
				if (npc == null)
				{
					continue;
				}
				npc.deleteMe();
			}
			_npcs.clear();
		}
	}
	
	protected void loadBuffers()
	{
		try
		{
			int bufferId = EventConfig.getInstance().getGlobalConfigInt("npcBufferId");
			for (AbstractFeature feature : _event.getMode().getFeatures())
			{
				if (feature.getType() != EventMode.FeatureType.Buffer)
				{
					continue;
				}
				if (!((BufferFeature) feature).canSpawnBuffer())
				{
					return;
				}
				if (((BufferFeature) feature).getCustomNpcBufferId() == 0)
				{
					continue;
				}
				bufferId = ((BufferFeature) feature).getCustomNpcBufferId();
			}
			if (bufferId == -1)
			{
				return;
			}
			NpcTemplateData template = new NpcTemplateData(bufferId);
			if (!template.exists())
			{
				_log.warning("Missing NPC Buffer's template (ID " + bufferId + ") for event system.");
				return;
			}
			for (EventSpawn spawn : _arena.getSpawns())
			{
				if (spawn.getSpawnType() != SpawnType.Buffer)
				{
					continue;
				}
				NpcData data = template.doSpawn(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ(), 1, _instanceId);
				if (_buffers == null)
				{
					_buffers = new FastList<>();
				}
				_buffers.add(data);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	protected void unspawnBuffers()
	{
		if (_buffers != null)
		{
			for (NpcData npc : _buffers)
			{
				if (npc == null)
				{
					continue;
				}
				npc.deleteMe();
			}
			_buffers.clear();
		}
	}
	
	protected void unspawnMapGuards()
	{
		if (_mapGuards != null)
		{
			for (NpcData npc : _mapGuards)
			{
				if (npc == null)
				{
					continue;
				}
				npc.deleteMe();
			}
			_mapGuards.clear();
		}
	}
	
	/*
	 * Enabled force condition propagation Lifted jumps to return sites
	 */
	protected void handleDoors(int state)
	{
		if (!_arena.hasDoor())
		{
			return;
		}
		if (state == 0)
		{
			Iterator<EventSpawn> i$ = _arena.getDoors().iterator();
			while (i$.hasNext())
			{
				DoorAction action;
				EventSpawn doorSpawn;
				CallBack.getInstance().getOut().addDoorToInstance(this._instanceId, doorSpawn.getDoorId(), (action = DoorAction.getAction((doorSpawn = i$.next()).getNote(), 1)) == DoorAction.Open);
			}
			return;
		}
		else
		{
			for (DoorData door : CallBack.getInstance().getOut().getInstanceDoors(this._instanceId))
			{
				for (EventSpawn doorSpawn : this._arena.getDoors())
				{
					DoorAction action = DoorAction.getAction(doorSpawn.getNote(), state);
					if (doorSpawn.getDoorId() != door.getDoorId())
					{
						continue;
					}
					if ((action == DoorAction.Close) && door.isOpened())
					{
						door.closeMe();
						continue;
					}
					if ((action != DoorAction.Open) || door.isOpened())
					{
						continue;
					}
					door.openMe();
				}
			}
			return;
		}
	}
	
	@Override
	public void playerWentAfk(PlayerEventInfo player, boolean warningOnly, int afkTime)
	{
		if (warningOnly)
		{
			player.sendMessage(LanguageEngine.getMsg("event_afkWarning", PlayerEventInfo.AFK_WARNING_DELAY / 1000, PlayerEventInfo.AFK_KICK_DELAY / 1000));
		}
		else if (afkTime == 0)
		{
			player.sendMessage(LanguageEngine.getMsg("event_afkMarked"));
		}
		else if ((afkTime % 60) == 0)
		{
			player.sendMessage(LanguageEngine.getMsg("event_afkDurationInfo", afkTime / 60));
		}
	}
	
	@Override
	public void playerReturnedFromAfk(PlayerEventInfo player)
	{
	}
	
	protected void scheduleClearEvent(int delay)
	{
		CallBack.getInstance().getOut().scheduleGeneral(() -> MiniEventGame.this.clearEvent(), 8000);
	}
	
	protected void startAnnouncing()
	{
		if (this._announcer != null)
		{
			this._announcer.announce = true;
		}
	}
	
	public EventMap getMap()
	{
		return this._arena;
	}
	
	public int getGameId()
	{
		return this._gameId;
	}
	
	public MiniEventManager getEvent()
	{
		return this._event;
	}
	
	protected void saveGlobalStats()
	{
		FastMap stats = new FastMap();
		for (EventTeam team : this.getTeams())
		{
			for (PlayerEventInfo pi : team.getPlayers())
			{
				this.getPlayerData(pi).getGlobalStats().raise(GlobalStats.GlobalStatType.COUNT_PLAYED, 1);
				stats.put(pi, this.getPlayerData(pi).getGlobalStats());
			}
		}
		EventStatsManager.getInstance().getGlobalStats().updateGlobalStats(stats);
	}
	
	protected void scheduleMessage(final String message, int delay, final boolean abortable)
	{
		CallBack.getInstance().getOut().scheduleGeneral(() -> MiniEventGame.this.broadcastMessage(message, abortable), delay);
	}
	
	protected String getRoundName(int round, int maxRounds)
	{
		if (round == maxRounds)
		{
			return LanguageEngine.getMsg("round_final");
		}
		switch (round)
		{
			case 1:
			{
				return LanguageEngine.getMsg("round_1");
			}
			case 2:
			{
				return LanguageEngine.getMsg("round_2");
			}
			case 3:
			{
				return LanguageEngine.getMsg("round_3");
			}
			case 4:
			{
				return LanguageEngine.getMsg("round_4");
			}
			case 5:
			{
				return LanguageEngine.getMsg("round_5");
			}
			case 6:
			{
				return LanguageEngine.getMsg("round_6");
			}
			case 7:
			{
				return LanguageEngine.getMsg("round_7");
			}
			case 8:
			{
				return LanguageEngine.getMsg("round_8");
			}
			case 9:
			{
				return LanguageEngine.getMsg("round_9");
			}
			case 10:
			{
				return LanguageEngine.getMsg("round_10");
			}
		}
		return "" + round + "th";
	}
	
	protected int getGameTime()
	{
		for (AbstractFeature f : this._event.getMode().getFeatures())
		{
			if (f.getType() != EventMode.FeatureType.TimeLimit)
			{
				continue;
			}
			return ((TimeLimitFeature) f).getTimeLimit();
		}
		return this._event.getInt("TimeLimitMs");
	}
	
	@Override
	public void onKill(PlayerEventInfo player, CharacterData target)
	{
	}
	
	@Override
	public boolean canAttack(PlayerEventInfo player, CharacterData target)
	{
		if (target.getEventInfo() == null)
		{
			return true;
		}
		if (target.getEventInfo().getEvent() != player.getEvent())
		{
			return false;
		}
		if (target.getEventInfo().getTeamId() != player.getTeamId())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onAttack(CharacterData cha, CharacterData target)
	{
		return true;
	}
	
	@Override
	public boolean canSupport(PlayerEventInfo player, CharacterData target)
	{
		if ((target.getEventInfo() == null) || (target.getEventInfo().getEvent() != player.getEvent()))
		{
			return false;
		}
		if (target.getEventInfo().getTeamId() == player.getTeamId())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void onDie(PlayerEventInfo player, CharacterData killer)
	{
	}
	
	@Override
	public void onDamageGive(CharacterData cha, CharacterData target, int damage, boolean isDOT)
	{
	}
	
	@Override
	public void onDisconnect(PlayerEventInfo player)
	{
	}
	
	@Override
	public boolean addDisconnectedPlayer(PlayerEventInfo player, EventManager.DisconnectedPlayerData data)
	{
		return false;
	}
	
	@Override
	public boolean onSay(PlayerEventInfo player, String text, int channel)
	{
		if (text.equals(".scheme"))
		{
			EventManager.getInstance().getHtmlManager().showSelectSchemeForEventWindow(player, "none", this.getEvent().getEventType().getAltTitle());
			return false;
		}
		if (text.equalsIgnoreCase(".voteabort") || text.equalsIgnoreCase(".voteend"))
		{
			this.voteEnd(player);
			return false;
		}
		return true;
	}
	
	private void voteEnd(PlayerEventInfo player)
	{
		if (this._voted == null)
		{
			this._voted = new FastList();
		}
		if (!this._voted.contains(player))
		{
			this._voted.add(player);
			this.broadcastMessage("A player voted to end this mini event.", true);
			for (EventTeam t : this.getTeams())
			{
				for (PlayerEventInfo p : t.getPlayers())
				{
					if (this._voted.contains(p))
					{
						continue;
					}
					return;
				}
			}
			this.abortDueToError("Players voted to abort this match.");
		}
	}
	
	@Override
	public boolean onNpcAction(PlayerEventInfo player, NpcData npc)
	{
		return true;
	}
	
	@Override
	public boolean canUseItem(PlayerEventInfo player, ItemData item)
	{
		if ((this.notAllovedItems != null) && (Arrays.binarySearch(this.notAllovedItems, item.getItemId()) >= 0))
		{
			player.sendMessage(LanguageEngine.getMsg("event_itemNotAllowed"));
			return false;
		}
		if (item.isScroll())
		{
			return false;
		}
		if (item.isPotion() && !this._event.getBoolean("allowPotions"))
		{
			return false;
		}
		for (AbstractFeature f : this.getEvent().getMode().getFeatures())
		{
			if (!((f.getType() != EventMode.FeatureType.ItemGrades) || ((ItemGradesFeature) f).checkItem(player, item)))
			{
				return false;
			}
			if (!((f.getType() != EventMode.FeatureType.Items) || ((ItemsFeature) f).checkItem(player, item)))
			{
				return false;
			}
			if ((f.getType() != EventMode.FeatureType.Enchant) || ((EnchantFeature) f).checkItem(player, item))
			{
				continue;
			}
			return false;
		}
		if (item.isPetCollar() && !this._allowPets)
		{
			player.sendMessage(LanguageEngine.getMsg("event_petsNotAllowed"));
			return false;
		}
		return true;
	}
	
	@Override
	public void onItemUse(PlayerEventInfo player, ItemData item)
	{
	}
	
	@Override
	public boolean canUseSkill(PlayerEventInfo player, SkillData skill)
	{
		if ((this.notAllovedSkillls != null) && (Arrays.binarySearch(this.notAllovedSkillls, skill.getId()) >= 0))
		{
			player.sendMessage(LanguageEngine.getMsg("event_skillNotAllowed"));
			return false;
		}
		if (skill.getSkillType().equals("RESURRECT"))
		{
			return false;
		}
		if (skill.getSkillType().equals("RECALL"))
		{
			return false;
		}
		if (skill.getSkillType().equals("SUMMON_FRIEND"))
		{
			return false;
		}
		if (skill.getSkillType().equals("FAKE_DEATH"))
		{
			return false;
		}
		for (AbstractFeature f : this.getEvent().getMode().getFeatures())
		{
			if ((f.getType() != EventMode.FeatureType.Skills) || ((SkillsFeature) f).checkSkill(player, skill))
			{
				continue;
			}
			return false;
		}
		if (!this._allowSummons && skill.getSkillType().equals("SUMMON"))
		{
			player.sendMessage(LanguageEngine.getMsg("event_summonsNotAllowed"));
			return false;
		}
		return true;
	}
	
	@Override
	public void onSkillUse(PlayerEventInfo player, SkillData skill)
	{
	}
	
	@Override
	public boolean canDestroyItem(PlayerEventInfo player, ItemData item)
	{
		return true;
	}
	
	@Override
	public boolean canInviteToParty(PlayerEventInfo player, PlayerEventInfo target)
	{
		if (target.getEvent() != player.getEvent())
		{
			return false;
		}
		if (target.getTeamId() == player.getTeamId())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canTransform(PlayerEventInfo player)
	{
		return true;
	}
	
	@Override
	public boolean canBeDisarmed(PlayerEventInfo player)
	{
		return true;
	}
	
	@Override
	public int allowTransformationSkill(PlayerEventInfo player, SkillData skill)
	{
		return 0;
	}
	
	@Override
	public boolean canSaveShortcuts(PlayerEventInfo player)
	{
		return true;
	}
	
	protected class Announcer implements Runnable
	{
		private long _start;
		boolean announce;
		private ScheduledFuture<?> _nextAnnounce;
		
		protected Announcer()
		{
			this.announce = false;
		}
		
		public void setTime(long startTime)
		{
			this._start = startTime;
			this.run();
		}
		
		@Override
		public void run()
		{
			int delay = (int) Math.round((this._start - System.currentTimeMillis()) / 1000.0);
			if (this.announce && (delay > 0))
			{
				this.announce(delay);
			}
			int nextMsg = 0;
			if (delay > 3600)
			{
				nextMsg = delay - 3600;
			}
			else if (delay > 1800)
			{
				nextMsg = delay - 1800;
			}
			else if (delay > 900)
			{
				nextMsg = delay - 900;
			}
			else if (delay > 600)
			{
				nextMsg = delay - 600;
			}
			else if (delay > 300)
			{
				nextMsg = delay - 300;
			}
			else if (delay > 60)
			{
				nextMsg = delay - 60;
			}
			else if (delay > 10)
			{
				nextMsg = delay - 10;
			}
			else
			{
				return;
			}
			if (delay > 0)
			{
				this._nextAnnounce = CallBack.getInstance().getOut().scheduleGeneral(this, nextMsg * 1000);
			}
		}
		
		private void announce(int delay)
		{
			if ((delay >= 3600) && ((delay % 3600) == 0))
			{
				int d = delay / 3600;
				Object[] arrobject = new Object[2];
				arrobject[0] = d;
				arrobject[1] = "hour" + (d == 1 ? "" : "s");
				MiniEventGame.this.broadcastMessage(LanguageEngine.getMsg("game_countdown", arrobject), false);
			}
			else if (delay >= 60)
			{
				int d = delay / 60;
				Object[] arrobject = new Object[2];
				arrobject[0] = d;
				arrobject[1] = "minute" + (d == 1 ? "" : "s");
				MiniEventGame.this.broadcastMessage(LanguageEngine.getMsg("game_countdown", arrobject), false);
			}
			else
			{
				Object[] arrobject = new Object[2];
				arrobject[0] = delay;
				arrobject[1] = "second" + (delay == 1 ? "" : "s");
				MiniEventGame.this.broadcastMessage(LanguageEngine.getMsg("game_countdown", arrobject), false);
			}
		}
		
		public void cancel()
		{
			if (this._nextAnnounce != null)
			{
				this._nextAnnounce.cancel(false);
			}
		}
	}
	
	private class LocChecker implements Runnable
	{
		private LocChecker()
		{
		}
		
		@Override
		public void run()
		{
			try
			{
				MiniEventGame.this.checkPlayersLoc();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			MiniEventGame.this.scheduleLocChecker();
		}
	}
	
}
