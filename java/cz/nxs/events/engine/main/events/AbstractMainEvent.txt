/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.PlayerEventInfo$Radar
 *  cz.nxs.interf.callback.CallbackManager
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.DoorData
 *  cz.nxs.interf.delegate.FenceData
 *  cz.nxs.interf.delegate.InstanceData
 *  cz.nxs.interf.delegate.ItemData
 *  cz.nxs.interf.delegate.NpcData
 *  cz.nxs.interf.delegate.NpcTemplateData
 *  cz.nxs.interf.delegate.PartyData
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastList$Node
 *  javolution.util.FastMap
 *  javolution.util.FastSet
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.Configurable;
import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventBuffer;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.EventRewardSystem;
import cz.nxs.events.engine.EventWarnings;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.Event;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.PvPEventPlayerData;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.html.EventHtmlManager;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.base.MainEventInstanceType;
import cz.nxs.events.engine.stats.EventStatsManager;
import cz.nxs.events.engine.stats.GlobalStats;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.DoorData;
import cz.nxs.interf.delegate.FenceData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.NpcTemplateData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.ClassType;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IPlayerBase;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

public abstract class AbstractMainEvent
extends Event
implements Configurable,
EventGame {
    protected MainEventManager _manager;
    protected Map<SpawnType, String> _spawnTypes;
    protected InstanceData[] _instances;
    protected int _runningInstances;
    protected String _htmlDescription = null;
    protected FastMap<Integer, FastMap<Integer, EventTeam>> _teams;
    protected Map<Integer, List<FenceData>> _fences;
    protected Map<Integer, List<NpcData>> _npcs;
    protected List<Integer> _rewardedInstances;
    private String scorebarText;
    private FastMap<MainEventInstanceType, FastList<PlayerEventInfo>> _tempPlayers;
    private FastList<String> _configCategories;
    private FastMap<String, ConfigModel> _configs;
    private FastMap<String, ConfigModel> _mapConfigs;
    private FastMap<String, ConfigModel> _instanceTypeConfigs;
    protected RewardPosition[] _rewardTypes = null;
    protected WaweRespawnScheduler _waweScheduler;
    protected List<PlayerEventInfo> _spectators;
    protected boolean _allowScoreBar;
    protected boolean _allowSchemeBuffer;
    protected boolean _removeBuffsOnEnd;
    protected boolean _allowSummons;
    protected boolean _allowPets;
    protected boolean _hideTitles;
    protected boolean _removePartiesOnStart;
    protected boolean _rejoinEventAfterDisconnect;
    protected boolean _removeWarningAfterReconnect;
    protected boolean _enableRadar;
    protected int _countOfShownTopPlayers;
    private int firstRegisteredRewardCount;
    private String firstRegisteredRewardType;
    private boolean _firstBlood;
    protected PlayerEventInfo _firstBloodPlayer;
    private int _partiesCount;
    private int _afkHalfReward;
    private int _afkNoReward;
    private FastMap<Integer, MainEventInstanceType> _types;
    protected int[] notAllovedSkillls;
    protected int[] notAllovedItems;
    private String[] enabledTiers;
    private int[] setOffensiveSkills;
    private int[] setNotOffensiveSkills;
    private int[] setNeutralSkills;
    private List<PlayerEventInfo> _firstRegistered;
    private Object firstBloodLock = new Object();

    public AbstractMainEvent(EventType type, MainEventManager manager) {
        super(type);
        this._manager = manager;
        this._teams = new FastMap();
        this._rewardedInstances = new FastList();
        this._spawnTypes = new FastMap();
        this._spawnTypes.put(SpawnType.Regular, "Defines where the players will be spawned.");
        this._spawnTypes.put(SpawnType.Buffer, "Defines where the buffer(s) will be spawned.");
        this._spawnTypes.put(SpawnType.Fence, "Defines where fences will be spawned.");
        this._configCategories = new FastList();
        this._configs = new FastMap();
        this._mapConfigs = new FastMap();
        this._instanceTypeConfigs = new FastMap();
        this.loadConfigs();
        this._types = new FastMap();
    }

    @Override
    public void loadConfigs() {
        this.addConfig(new ConfigModel("allowScreenScoreBar", "true", "True to allow the screen score bar, showing mostly scores for all teams and time left till the event ends.", ConfigModel.InputType.Boolean));
        if (!this.getEventType().isFFAEvent()) {
            ConfigModel divideMethod = new ConfigModel("divideToTeamsMethod", "LevelOnly", "The method used to divide the players into the teams on start of the event. All following methods try to put similar count of healers to all teams.<br1><font color=LEVEL>LevelOnly</font> sorts players by their level and then divides them into the teams (eg. Player1 (lvl85) to teamA, Player2(level84) to teamB, Player3(lvl81) to teamA, Player4(lvl75) to teamB, Player5(lvl70) to teamA,...)<br1><font color=LEVEL>PvPsAndLevel</font>: in addition to sorting by level, this method's main sorting factor are player's PvP kills. The rest of dividing procedure is same as for LevelsOnly. Useful for PvP servers, where level doesn't matter much.<br1>", ConfigModel.InputType.Enum);
            divideMethod.addEnumOptions(new String[]{"LevelOnly", "PvPsAndLevel"});
            this.addConfig(divideMethod);
            this.addConfig(new ConfigModel("balanceHealersInTeams", "true", "Put true if you want the engine to try to balance the count of healers in all teams (in all teams same healers count), making it as similar as possible.", ConfigModel.InputType.Boolean));
        } else {
            this.addConfig(new ConfigModel("announcedTopPlayersCount", "5", "You can specify the count of top players, that will be announced (in chat) in the end of the event."));
        }
        this.addConfig(new ConfigModel("runTime", "20", "The run time of this event, launched automatically by the scheduler. Max value globally for all events is 120 minutes. In minutes!"));
        this.addConfig(new ConfigModel("minLvl", "-1", "Minimum level for players participating the event (playerLevel >= value)."));
        this.addConfig(new ConfigModel("maxLvl", "100", "Maximum level for players participating the event (playerLevel <= value)."));
        this.addConfig(new ConfigModel("minPlayers", "2", "The minimum count of players required to start one instance of the event."));
        this.addConfig(new ConfigModel("maxPlayers", "-1", "The maximum count of players possible to play in the event. Put -1 to make it unlimited."));
        this.addConfig(new ConfigModel("removeBufsOnEnd", "true", "Put true to make that the buffs are removed from all players when the event ends (or gets aborted).", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("removePartiesOnStart", "false", "Put true if you want that when the event starts, to automatically delete all parties, that had been created BEFORE the event started.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("rejoinAfterDisconnect", "true", "When a player is on event and disconnects from the server, this gives <font color=7f7f7f>(if set on true)</font> him the opportunity to get back to the event if he relogins. The engine will simply wait if he logins again, and then teleport him back to the event (to his previous team). Sometimes it can happen that, for example, the whole team disconnects and the event is aborted, so then the engine will not teleport the player back to the event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("removeWarningAfterRejoin", "true", "Works if <font color=LEVEL>rejoinAfterDisconnect = true</font>. When a player successfully re-joins his previous event after he disconnected from server and then logged in again, this feature will remove the warning point which he received when he disconnected. Remember that if a player has a configurable count of warnings (by default 3), he is unable to participate in any event. Warnings decrease by 1 every day.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("playersInInstance", "-1", "This config currently has no use ;)."));
        this.addConfig(new ConfigModel("allowPotions", "false", "Specify if you want to allow players using potions in the event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("allowSummons", "true", "Put false if you want to disable summons on this event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("allowPets", "true", "Put false if you want to disable pets on this event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("allowHealers", "true", "Put false if you want to permit healer classes to register to the event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("hideTitles", "false", "Put true to disable titles containing player's event stats.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("removeBuffsOnStart", "true", "If 'true', all buffs will be removed from players on first teleport to the event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("removeBuffsOnRespawn", "false", "If 'true', all buffs will be removed from players when they respawn. Useful for certain servers.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("notAllowedSkills", "", "Put here skills that won't be aviable for use in this event <font color=7f7f7f>(write one skill's ID and click Add; to remove the skill, simply click on it's ID in the list)</font>", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("notAllowedItems", "", "Put here items that won't be aviable for use in this event <font color=7f7f7f>(write one skill's ID and click Add; to remove the skill, simply click on it's ID in the list)</font>", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("enableRadar", "true", "Enable/disable the quest-like radar for players. It will show an arrow above player's head and point him to a RADAR type spawn of his team. Useful for example when you create a RADAR spawn right next to enemy team's flag (it will show all players from the one team where is the flag they need to capture). Works only if the active map contains a RADAR spawn (and spawn's teamID must be > 0).", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("dualboxCheck", "true", "You can enable/disable the registration dualbox check here.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("maxPlayersPerIp", "1", "If the 'dualboxCheck' config is enabled, you can specify here how many players with the same IP are allowed to be in the event."));
        this.addConfig(new ConfigModel("afkHalfReward", "120", "The time (in seconds) the player must be AFK to lower his reward (in the end of the event) by 50%. The AFK counter starts counting the time spent AFK after <font color=LEVEL>afkWarningDelay</font> + <font color=LEVEL>afkKickDelay</font> miliseconds (these two are Global configs) of idling (not clicking, not moving, not doing anything). Write 0 to disable this feature."));
        this.addConfig(new ConfigModel("afkNoReward", "300", "The time (in seconds) the player must be AFK to receive no reward in the end of the event.The AFK counter starts counting the time spent AFK after <font color=LEVEL>afkWarningDelay</font> + <font color=LEVEL>afkKickDelay</font> miliseconds (these two are Global configs) of idling (not clicking, not moving, not doing anything). Write 0 to disable this feature."));
        this.addConfig(new ConfigModel("firstRegisteredRewardCount", "10", "If you have specified a 'FirstRegisteredReward' reward, you can define here how many first registered players will be rewarded in the end of the event."));
        this.addConfig(new ConfigModel("firstRegisteredRewardType", "WinnersOnly", "Select here who will be rewarded with the 'FirstRegisteredReward' reward in the end of the event.", ConfigModel.InputType.Enum).addEnumOptions(new String[]{"WinnersOnly", "All"}));
        this.addConfig(new ConfigModel("countOfShownTopPlayers", "10", "Count of players shown in the Top-scorers list in the community board. Better not to use high values. If you don't want to use this feature (not recommended - ugly HTML), put 0."));
        this.addConfig(new ConfigModel("enabledTiers", "AllItems", "This config is not fully implemented. Requires gameserver support.", ConfigModel.InputType.MultiAdd));
        this.addInstanceTypeConfig(new ConfigModel("strenghtRate", "5", "Every instance has it's rate. This rate determines how 'strong' the players are inside. Strenght rate is used in some engine's calculations. Check out other configs. <font color=B46F6B>Values MUST be within 1-10. Setting it more causes problems.</font>"));
        this.addInstanceTypeConfig(new ConfigModel("minLvl", "-1", "Min level (for players) for this instance."));
        this.addInstanceTypeConfig(new ConfigModel("maxLvl", "100", "Max level (for players) for this instance."));
        this.addInstanceTypeConfig(new ConfigModel("minPvps", "0", "Min PvP points count to play in this instance."));
        this.addInstanceTypeConfig(new ConfigModel("maxPvps", "-1", "Max PvP points count to play in this instance. Put -1 to make it infinity."));
        this.addInstanceTypeConfig(new ConfigModel("minPlayers", "2", "Count of players required to start this instance. If there's less players, then the instance tries to divide it's players to stronger instances (check out config <font color=LEVEL>joinStrongerInstIfNeeded</font>) and if it doesn't success (the config is set to false or all possible stronger instances are full), it will unregister the players from the event. Check out other configs related to this."));
        this.addInstanceTypeConfig(new ConfigModel("joinStrongerInstIfNeeded", "False", "If there are not enought players needed for this instance to start (as specified in <font color=LEVEL>minPlayers</font> config), the instance will try to divide it's players <font color=7f7f7f>(players, that CAN'T join any other instance - cuz they either don't meet their criteria or the instances are full already)</font> to stronger instances (if they aren't full yet; level, pvp, equip and other checks are not applied in this case).", ConfigModel.InputType.Boolean));
        this.addInstanceTypeConfig(new ConfigModel("joinStrongerInstMaxDiff", "2", "If <font color=LEVEL>joinStrongerInstIfNeeded</font> is enabled, this specifies the maximum allowed difference between strength rate of both instances (where <font color=ac9887>the weaker instance</font> with not enought players divides it's players to <font color=ac9887>a stronger instance</font>)."));
        this.addInstanceTypeConfig(new ConfigModel("maxPlayers", "-1", "Max players ammount aviable for this instance. Put -1 to make it infinity."));
    }

    public abstract void runEvent();

    public abstract void onEventEnd();

    public abstract void clearEvent(int var1);

    protected abstract boolean instanceEnded();

    protected abstract void endInstance(int var1, boolean var2, boolean var3, boolean var4);

    protected abstract void respawnPlayer(PlayerEventInfo var1, int var2);

    protected abstract String getScorebar(int var1);

    protected abstract String getTitle(PlayerEventInfo var1);

    public abstract String getHtmlDescription();

    protected abstract AbstractEventInstance createEventInstance(InstanceData var1);

    protected abstract AbstractEventInstance getMatch(int var1);

    protected abstract int initInstanceTeams(MainEventInstanceType var1, int var2);

    protected abstract AbstractEventData createEventData(int var1);

    protected abstract AbstractEventData getEventData(int var1);

    public void startRegistration() {
        this._tempPlayers = new FastMap();
        this._firstRegistered = new FastList();
        this.firstRegisteredRewardCount = this.getInt("firstRegisteredRewardCount");
        this.firstRegisteredRewardType = this.getString("firstRegisteredRewardType");
        if (!this.getString("enabledTiers").equals("")) {
            String[] splits = this.getString("enabledTiers").split(",");
            this.enabledTiers = new String[splits.length];
            try {
                for (int i = 0; i < splits.length; ++i) {
                    this.enabledTiers[i] = splits[i];
                }
                Arrays.sort(this.enabledTiers);
            }
            catch (Exception e) {
                NexusLoader.debug((String)("Error while loading config 'enabledTiers' for event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
            }
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: startRegistration() done");
        }
    }

    public void initMap() {
        try {
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: starting initMap()");
            }
            this._fences = new FastMap();
            this._npcs = new FastMap();
            EventMap map = this._manager.getMap();
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: init map - " + map.getMapName());
            }
            for (InstanceData instance : this._instances) {
                NpcData npc;
                int mapGuardId;
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: initmap iterating instance " + instance.getId());
                }
                this._fences.put(instance.getId(), (List<FenceData>)new FastList());
                for (EventSpawn spawn : map.getSpawns(-1, SpawnType.Fence)) {
                    FenceData fence = CallBack.getInstance().getOut().createFence(2, spawn.getFenceWidth(), spawn.getFenceLength(), spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ(), map.getGlobalId());
                    this._fences.get(instance.getId()).add(fence);
                }
                CallBack.getInstance().getOut().spawnFences(this._fences.get(instance.getId()), instance.getId());
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: initmap iterating instance spawned fences");
                }
                this._npcs.put(instance.getId(), (List<NpcData>)new FastList());
                for (EventSpawn spawn2 : map.getSpawns(-1, SpawnType.Npc)) {
                    if (spawn2.getNpcId() == -1) continue;
                    npc = new NpcTemplateData(spawn2.getNpcId()).doSpawn(spawn2.getLoc().getX(), spawn2.getLoc().getY(), spawn2.getLoc().getZ(), 1, instance.getId());
                    this._npcs.get(instance.getId()).add(npc);
                }
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: initmap iterating instance spawned npcs");
                }
                if ((mapGuardId = EventConfig.getInstance().getGlobalConfigInt("mapGuardNpcId")) != -1) {
                    for (EventSpawn spawn3 : map.getSpawns(-1, SpawnType.MapGuard)) {
                        npc = new NpcTemplateData(mapGuardId).doSpawn(spawn3.getLoc().getX(), spawn3.getLoc().getY(), spawn3.getLoc().getZ(), 1, instance.getId());
                        this._npcs.get(instance.getId()).add(npc);
                    }
                }
                if (!NexusLoader.detailedDebug) continue;
                this.print("AbstractMainEvent: initmap iterating instance spawned map guards");
            }
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: initmap finished");
            }
        }
        catch (NullPointerException e) {
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: error on initMap() " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
            }
            NexusLoader.debug((String)"Error on initMap()", (Level)Level.WARNING);
            e.printStackTrace();
        }
    }

    public void cleanMap(int instanceId) {
        try {
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: starting cleanmap(), instanceId " + instanceId);
            }
            if (this._instances != null) {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: instances are not null");
                }
                for (InstanceData instance : this._instances) {
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: iterating instance " + instance.getId());
                    }
                    if (instanceId != 0 && instance.getId() != instanceId) continue;
                    if (this._fences != null && this._fences.containsKey(instance.getId())) {
                        CallBack.getInstance().getOut().unspawnFences(this._fences.get(instance.getId()));
                    }
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: instance + " + instance.getId() + ", fences deleted");
                    }
                    if (this._npcs != null && this._npcs.containsKey(instance.getId())) {
                        for (NpcData npc : this._npcs.get(instance.getId())) {
                            npc.deleteMe();
                        }
                    }
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: instance + " + instance.getId() + ", npcs deleted");
                    }
                    if (this._fences != null) {
                        this._fences.remove(instance.getId());
                    }
                    if (this._npcs != null) {
                        this._npcs.remove(instance.getId());
                    }
                    if (!NexusLoader.detailedDebug) continue;
                    this.print("AbstractMainEvent: instance + " + instance.getId() + " cleaned.");
                }
            }
            if (instanceId == 0) {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: set npcs and fences to null (instanceId = 0)");
                }
                this._npcs = null;
                this._fences = null;
            } else if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: cannot set npcs and fences to null yet, instanceId != 0");
            }
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Lifted jumps to return sites
     */
    public void initEvent() {
        block26 : {
            block25 : {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: initEvent starting");
                }
                if (this._rewardTypes == null) {
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: event " + this.getEventName() + " has not set up any _rewardTypes");
                    }
                    this.debug("Event " + this.getEventName() + " has not set up _rewardTypes. You've propably forgotten to call 'setRewardTypes()' in event's constructor.");
                }
                this._firstBlood = false;
                this._spectators = new FastList();
                if (!this._rewardedInstances.isEmpty()) {
                    this._rewardedInstances.clear();
                }
                Collections.sort(this._manager.getMap().getSpawns(), EventMap.compareByIdAsc);
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: spawns sorted, instances cleaned");
                }
                this._afkHalfReward = this.getInt("afkHalfReward");
                this._afkNoReward = this.getInt("afkNoReward");
                this._allowScoreBar = this.getBoolean("allowScreenScoreBar");
                this._allowSchemeBuffer = EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer");
                this._removeBuffsOnEnd = this.getBoolean("removeBufsOnEnd");
                this._allowSummons = this.getBoolean("allowSummons");
                this._allowPets = this.getBoolean("allowPets");
                this._hideTitles = this.getBoolean("hideTitles");
                this._removePartiesOnStart = this.getBoolean("removePartiesOnStart");
                this._countOfShownTopPlayers = this.getInt("countOfShownTopPlayers");
                this._rejoinEventAfterDisconnect = this.getBoolean("rejoinAfterDisconnect");
                this._removeWarningAfterReconnect = this.getBoolean("removeWarningAfterRejoin");
                this._enableRadar = this.getBoolean("enableRadar");
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: scorebar - " + this._allowScoreBar + ", scheme buffer = " + this._allowSchemeBuffer);
                }
                if (!this.getString("notAllowedItems").equals("")) {
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: loading not allowed items");
                    }
                    splits = this.getString("notAllowedItems").split(",");
                    this.notAllovedItems = new int[splits.length];
                    try {
                        for (i = 0; i < splits.length; ++i) {
                            this.notAllovedItems[i] = Integer.parseInt(splits[i]);
                        }
                        Arrays.sort(this.notAllovedItems);
                        if (NexusLoader.detailedDebug) {
                            this.print("AbstractMainEvent: not allowed items = " + this.notAllovedItems.toString());
                        } else {
                            ** GOTO lbl49
                        }
                    }
                    catch (Exception e) {
                        NexusLoader.debug((String)("Error while loading config 'notAllowedItems' for event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
                        if (NexusLoader.detailedDebug) {
                            this.print("AbstractMainEvent: error while loading not allowed items " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
                        } else {
                            ** GOTO lbl49
                        }
                        break block25;
                    }
                }
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: no not allowed items specified!");
                }
            }
            if (!this.getString("notAllowedSkills").equals("")) {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: loading not allowed skills!");
                }
                splits = this.getString("notAllowedSkills").split(",");
                this.notAllovedSkillls = new int[splits.length];
                try {
                    for (i = 0; i < splits.length; ++i) {
                        this.notAllovedSkillls[i] = Integer.parseInt(splits[i]);
                    }
                    Arrays.sort(this.notAllovedSkillls);
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: not allowed skills = " + this.notAllovedSkillls.toString());
                    } else {
                        ** GOTO lbl70
                    }
                }
                catch (Exception e) {
                    NexusLoader.debug((String)("Error while loading config 'notAllowedSkills' for event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: error while loading not allowed skills " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
                    } else {
                        ** GOTO lbl70
                    }
                    break block26;
                }
            }
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: no not allowed skills specified!");
            }
        }
        this.loadOverridenSkillsParameters();
        this._partiesCount = 0;
        this._firstBloodPlayer = null;
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: initEvent finished for AbstractMainEvent()");
        }
        this.dumpConfigs();
    }

    private void loadOverridenSkillsParameters() {
        block23 : {
            String[] splits;
            String s;
            int i;
            block22 : {
                block21 : {
                    s = EventConfig.getInstance().getGlobalConfigValue("setOffensiveSkills");
                    if (s != null && s.length() > 0) {
                        try {
                            splits = s.split(";");
                            this.setOffensiveSkills = new int[splits.length];
                            try {
                                for (i = 0; i < splits.length; ++i) {
                                    this.setOffensiveSkills[i] = Integer.parseInt(splits[i]);
                                }
                                Arrays.sort(this.setOffensiveSkills);
                                if (NexusLoader.detailedDebug) {
                                    this.print("AbstractMainEvent: set offensive skills = " + this.setOffensiveSkills.toString());
                                }
                            }
                            catch (Exception e) {
                                NexusLoader.debug((String)("Error while loading GLOBAL config 'setOffensiveSkills' in event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
                            }
                        }
                        catch (Exception e) {
                            NexusLoader.debug((String)("Error while loading GLOBAL config 'setOffensiveSkills' in event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
                            if (!NexusLoader.detailedDebug) break block21;
                            this.print("AbstractMainEvent: error while loading 'setOffensiveSkills' GLOBAL config " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
                        }
                    }
                }
                if ((s = EventConfig.getInstance().getGlobalConfigValue("setNotOffensiveSkills")) != null && s.length() > 0) {
                    try {
                        splits = s.split(";");
                        this.setNotOffensiveSkills = new int[splits.length];
                        try {
                            for (i = 0; i < splits.length; ++i) {
                                this.setNotOffensiveSkills[i] = Integer.parseInt(splits[i]);
                            }
                            Arrays.sort(this.setNotOffensiveSkills);
                            if (NexusLoader.detailedDebug) {
                                this.print("AbstractMainEvent: set not offensive skills = " + this.setNotOffensiveSkills.toString());
                            }
                        }
                        catch (Exception e) {
                            NexusLoader.debug((String)("Error while loading GLOBAL config 'setNotOffensiveSkills' in event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
                        }
                    }
                    catch (Exception e) {
                        NexusLoader.debug((String)("Error while loading GLOBAL config 'setNotOffensiveSkills' in event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
                        if (!NexusLoader.detailedDebug) break block22;
                        this.print("AbstractMainEvent: error while loading 'setNotOffensiveSkills' GLOBAL config " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
                    }
                }
            }
            if ((s = EventConfig.getInstance().getGlobalConfigValue("setNeutralSkills")) != null && s.length() > 0) {
                try {
                    splits = s.split(";");
                    this.setNeutralSkills = new int[splits.length];
                    try {
                        for (i = 0; i < splits.length; ++i) {
                            this.setNeutralSkills[i] = Integer.parseInt(splits[i]);
                        }
                        Arrays.sort(this.setNeutralSkills);
                        if (NexusLoader.detailedDebug) {
                            this.print("AbstractMainEvent: set neutral skills = " + this.setNeutralSkills.toString());
                        }
                    }
                    catch (Exception e) {
                        NexusLoader.debug((String)("Error while loading GLOBAL config 'setNeutralSkills' in event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
                    }
                }
                catch (Exception e) {
                    NexusLoader.debug((String)("Error while loading GLOBAL config 'setNeutralSkills' in event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
                    if (!NexusLoader.detailedDebug) break block23;
                    this.print("AbstractMainEvent: error while loading 'setNeutralSkills' GLOBAL config " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
                }
            }
        }
    }

    @Override
    public int isSkillOffensive(SkillData skill) {
        if (this.setOffensiveSkills != null && Arrays.binarySearch(this.setOffensiveSkills, skill.getId()) >= 0) {
            return 1;
        }
        if (this.setNotOffensiveSkills != null && Arrays.binarySearch(this.setNotOffensiveSkills, skill.getId()) >= 0) {
            return 0;
        }
        return -1;
    }

    @Override
    public boolean isSkillNeutral(SkillData skill) {
        if (this.setNeutralSkills != null && Arrays.binarySearch(this.setNeutralSkills, skill.getId()) >= 0) {
            return true;
        }
        return false;
    }

    private void dumpConfigs() {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: dumping configs START =====================");
        }
        for (Map.Entry e : this._configs.entrySet()) {
            if (!NexusLoader.detailedDebug) continue;
            this.print((String)e.getKey() + " - " + ((ConfigModel)e.getValue()).getValue());
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: dumping configs END ====================");
        }
    }

    protected void createTeams(int count, int instanceId) {
        try {
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: creating " + count + " teams for instanceId " + instanceId);
            }
            switch (count) {
                case 1: {
                    this.createNewTeam(instanceId, 1, LanguageEngine.getMsg("team_ffaevent"));
                    break;
                }
                case 2: {
                    this.createNewTeam(instanceId, 1, LanguageEngine.getMsg("team_blue"), LanguageEngine.getMsg("team_fullname_blue"));
                    this.createNewTeam(instanceId, 2, LanguageEngine.getMsg("team_red"), LanguageEngine.getMsg("team_fullname_red"));
                    break;
                }
                case 3: {
                    this.createNewTeam(instanceId, 1, LanguageEngine.getMsg("team_blue"), LanguageEngine.getMsg("team_fullname_blue"));
                    this.createNewTeam(instanceId, 2, LanguageEngine.getMsg("team_red"), LanguageEngine.getMsg("team_fullname_red"));
                    this.createNewTeam(instanceId, 3, LanguageEngine.getMsg("team_green"), LanguageEngine.getMsg("team_fullname_green"));
                    break;
                }
                case 4: {
                    this.createNewTeam(instanceId, 1, LanguageEngine.getMsg("team_blue"), LanguageEngine.getMsg("team_fullname_blue"));
                    this.createNewTeam(instanceId, 2, LanguageEngine.getMsg("team_red"), LanguageEngine.getMsg("team_fullname_red"));
                    this.createNewTeam(instanceId, 3, LanguageEngine.getMsg("team_green"), LanguageEngine.getMsg("team_fullname_green"));
                    this.createNewTeam(instanceId, 4, LanguageEngine.getMsg("team_purple"), LanguageEngine.getMsg("team_fullname_purple"));
                    break;
                }
                case 5: {
                    this.createNewTeam(instanceId, 1, LanguageEngine.getMsg("team_blue"), LanguageEngine.getMsg("team_fullname_blue"));
                    this.createNewTeam(instanceId, 2, LanguageEngine.getMsg("team_red"), LanguageEngine.getMsg("team_fullname_red"));
                    this.createNewTeam(instanceId, 3, LanguageEngine.getMsg("team_green"), LanguageEngine.getMsg("team_fullname_green"));
                    this.createNewTeam(instanceId, 4, LanguageEngine.getMsg("team_purple"), LanguageEngine.getMsg("team_fullname_purple"));
                    this.createNewTeam(instanceId, 5, LanguageEngine.getMsg("team_yellow"), LanguageEngine.getMsg("team_fullname_yellow"));
                    break;
                }
                default: {
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: the teams count is too high on event " + this.getEventName());
                    }
                    NexusLoader.debug((String)("The TEAMS COUNT is too high for event " + this.getEventName() + " - max value is 5!! The event will start with 5 teams."), (Level)Level.WARNING);
                    this.createTeams(5, instanceId);
                    return;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createNewTeam(int instanceId, int id, String name, String fullName) {
        ((FastMap)this._teams.get((Object)instanceId)).put((Object)id, (Object)new EventTeam(id, name, fullName));
        if (NexusLoader.detailedDebug) {
            this.print("... AbstractMainEvent: created new team for instanceId " + instanceId + ", id " + id + ", fullname " + fullName);
        }
    }

    protected void createNewTeam(int instanceId, int id, String name) {
        ((FastMap)this._teams.get((Object)instanceId)).put((Object)id, (Object)new EventTeam(id, name));
        if (NexusLoader.detailedDebug) {
            this.print("... AbstractMainEvent: created new team for instanceId " + instanceId + ", id " + id);
        }
    }

    public boolean canRegister(PlayerEventInfo player) {
        if (!this.getBoolean("allowHealers") && player.isPriest()) {
            player.sendMessage("Healers are not allowed on the event.");
            return false;
        }
        if (!this.checkItems(player)) {
            player.sendMessage("Come back after you store the disallowed items to your warehouse.");
            return false;
        }
        int maxPlayers = this.getInt("maxPlayers");
        if (maxPlayers != -1 && maxPlayers >= this._manager.getPlayersCount()) {
            if (NexusLoader.detailedDebug) {
                this.print("... registerPlayer() in AbstractMainEvent (canRegister()) for " + player.getPlayersName() + ", the event is full already! " + maxPlayers + "/" + this._manager.getPlayersCount());
            }
            player.sendMessage(LanguageEngine.getMsg("registering_full"));
            return false;
        }
        FastMap<MainEventInstanceType, FastList<PlayerEventInfo>> fastMap = this._tempPlayers;
        synchronized (fastMap) {
            for (MainEventInstanceType instance : this._types.values()) {
                if (this.canJoinInstance(player, instance)) {
                    if (NexusLoader.detailedDebug) {
                        this.print("... registerPlayer() in AbstractMainEvent (canRegister()) for " + player.getPlayersName() + " player CAN join instancetype " + instance.getId());
                    }
                    if (!this._tempPlayers.containsKey((Object)instance)) {
                        this._tempPlayers.put((Object)instance, (Object)new FastList());
                    } else {
                        int max = instance.getConfigInt("maxPlayers");
                        if (max > -1 && ((FastList)this._tempPlayers.get((Object)instance)).size() >= max) {
                            if (!NexusLoader.detailedDebug) continue;
                            this.print("... registerPlayer() in AbstractMainEvent (canRegister()) for " + player.getPlayersName() + " instance type " + instance.getId() + " is full already (max " + max + ")");
                            continue;
                        }
                    }
                    ((FastList)this._tempPlayers.get((Object)instance)).add((Object)player);
                    if (NexusLoader.detailedDebug) {
                        this.print("... registerPlayer() in AbstractMainEvent (canRegister()) for " + player.getPlayersName() + " registered to instance type " + instance.getId());
                    }
                    if (this._firstRegistered.size() < this.firstRegisteredRewardCount) {
                        this._firstRegistered.add(player);
                        if (this.firstRegisteredRewardType.equals("WinnersOnly")) {
                            player.sendMessage(LanguageEngine.getMsg("registered_first_type1", this.firstRegisteredRewardCount));
                        } else {
                            player.sendMessage(LanguageEngine.getMsg("registered_first_type2", this.firstRegisteredRewardCount));
                        }
                    }
                    return true;
                }
                if (!NexusLoader.detailedDebug) continue;
                this.print("... registerPlayer() in AbstractMainEvent (canRegister()) for " + player.getPlayersName() + " player CANNOT join instancetype " + instance.getId());
            }
        }
        player.sendMessage(LanguageEngine.getMsg("registering_noInstance"));
        return false;
    }

    protected boolean checkItems(PlayerEventInfo player) {
        return true;
    }

    public void playerUnregistered(PlayerEventInfo player) {
        FastMap<MainEventInstanceType, FastList<PlayerEventInfo>> fastMap = this._tempPlayers;
        synchronized (fastMap) {
            for (Map.Entry e : this._tempPlayers.entrySet()) {
                for (PlayerEventInfo pi : (FastList)e.getValue()) {
                    if (pi.getPlayersId() != player.getPlayersId()) continue;
                    ((FastList)this._tempPlayers.get(e.getKey())).remove((Object)pi);
                    if (NexusLoader.detailedDebug) {
                        this.print("... playerUnregistered player " + player.getPlayersName() + " removed from _tempPlayers");
                    }
                    return;
                }
            }
        }
        if (this._firstRegistered != null && this._firstRegistered.contains((Object)player)) {
            this._firstRegistered.remove((Object)player);
        }
        if (NexusLoader.detailedDebug) {
            this.print("... palyerUnregistered couldn't remove player " + player.getPlayersName() + " from _tempPlayers");
        }
    }

    public boolean canStart() {
        if (EventManager.getInstance().getMainEventManager().getPlayersCount() < this.getInt("minPlayers")) {
            return false;
        }
        return true;
    }

    protected void reorganizeInstances() {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: calling reorganizeInstance");
        }
        FastList sameStrenghtInstances = new FastList();
        this.dumpTempPlayers();
        for (int currentStrenght = 1; currentStrenght <= 10; ++currentStrenght) {
            for (Map.Entry e : this._tempPlayers.entrySet()) {
                if (this.isFull((MainEventInstanceType)e.getKey()) || ((MainEventInstanceType)e.getKey()).getStrenghtRate() != currentStrenght) continue;
                sameStrenghtInstances.add(e.getKey());
            }
            Collections.sort(sameStrenghtInstances, new Comparator<MainEventInstanceType>(){

                @Override
                public int compare(MainEventInstanceType i1, MainEventInstanceType i2) {
                    int neededPlayers2;
                    int neededPlayers1 = i1.getConfigInt("minPlayers") - ((FastList)AbstractMainEvent.this._tempPlayers.get((Object)i1)).size();
                    return neededPlayers1 == (neededPlayers2 = i2.getConfigInt("minPlayers") - ((FastList)AbstractMainEvent.this._tempPlayers.get((Object)i2)).size()) ? 0 : (neededPlayers1 < neededPlayers2 ? -1 : 1);
                }
            });
            this.reorganize((List<MainEventInstanceType>)sameStrenghtInstances);
            sameStrenghtInstances.clear();
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: instances DONE reorganized!!");
        }
        this.dumpTempPlayers();
    }

    private void dumpTempPlayers() {
        block5 : {
            if (NexusLoader.detailedDebug) {
                this.print("***** AbstractMainEvent: STARTING tempPlayers dump");
            }
            try {
                for (Map.Entry e : this._tempPlayers.entrySet()) {
                    if (!NexusLoader.detailedDebug) continue;
                    this.print("... ***** AbstractMainEvent: instance " + ((MainEventInstanceType)e.getKey()).getName() + " (" + ((MainEventInstanceType)e.getKey()).getId() + ") has " + ((FastList)e.getValue()).size() + " players");
                }
            }
            catch (Exception e) {
                if (!NexusLoader.detailedDebug) break block5;
                this.print("error while dumping temp players - " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
            }
        }
        if (NexusLoader.detailedDebug) {
            this.print("***** AbstractMainEvent: ENDED tempPlayers dump");
        }
    }

    protected void reorganize(List<MainEventInstanceType> instances) {
        block0 : for (MainEventInstanceType instance : instances) {
            if (this.hasEnoughtPlayers(instance)) {
                instances.remove(instance);
                continue;
            }
            int count = ((FastList)this._tempPlayers.get((Object)instance)).size();
            int toMove = instance.getConfigInt("minPlayers") - count;
            for (MainEventInstanceType possibleInstance : instances) {
                int moved;
                if (possibleInstance == instance) continue;
                if ((toMove-=(moved = this.movePlayers(instance, possibleInstance, toMove))) == 0) {
                    instances.remove(instance);
                    continue block0;
                }
                if (toMove <= 0) continue;
            }
        }
        if (!instances.isEmpty()) {
            int minPlayers = Integer.MAX_VALUE;
            MainEventInstanceType inst = null;
            for (MainEventInstanceType instance22 : instances) {
                if (instance22.getConfigInt("minPlayers") >= minPlayers) continue;
                minPlayers = instance22.getConfigInt("minPlayers");
                inst = instance22;
            }
            for (MainEventInstanceType instance22 : instances) {
                if (instance22 == inst) continue;
                this.movePlayers(inst, instance22, -1);
            }
            System.out.println("*** Done, instance " + inst.getName() + " has " + ((FastList)this._tempPlayers.get((Object)inst)).size() + " players.");
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: reorganize() - instance " + inst.getName() + " has " + ((FastList)this._tempPlayers.get((Object)inst)).size() + " players");
            }
        }
    }

    protected int movePlayers(MainEventInstanceType target, MainEventInstanceType source, int count) {
        if (count == 0) {
            return 0;
        }
        int moved = 0;
        for (PlayerEventInfo player : (FastList)this._tempPlayers.get((Object)source)) {
            ((FastList)this._tempPlayers.get((Object)target)).add((Object)player);
            ((FastList)this._tempPlayers.get((Object)source)).remove((Object)player);
            if (count == -1 || ++moved < count) continue;
            break;
        }
        return moved;
    }

    protected boolean isFull(MainEventInstanceType instance) {
        return ((FastList)this._tempPlayers.get((Object)instance)).size() >= instance.getConfigInt("maxPlayers");
    }

    protected boolean hasEnoughtPlayers(MainEventInstanceType instance) {
        return ((FastList)this._tempPlayers.get((Object)instance)).size() >= instance.getConfigInt("minPlayers");
    }

    protected boolean dividePlayers() {
        int playersCount;
        int sumPlayers;
        int toMove;
        int strenght;
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: starting dividePlayers");
        }
        this.reorganizeInstances();
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: starting notEnoughtPlayersInstance operations");
        }
        FastList notEnoughtPlayersInstances = new FastList();
        for (Map.Entry e : this._tempPlayers.entrySet()) {
            if (((FastList)e.getValue()).size() >= ((MainEventInstanceType)e.getKey()).getConfigInt("minPlayers")) continue;
            notEnoughtPlayersInstances.add(e.getKey());
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: notEnoughtPlayersInstances size = " + notEnoughtPlayersInstances.size());
        }
        FastList fixed = new FastList();
        for (MainEventInstanceType currentInstance2 : notEnoughtPlayersInstances) {
            if (currentInstance2 == null) continue;
            if (fixed.contains(currentInstance2)) continue;
            strenght = currentInstance2.getStrenghtRate();
            playersCount = ((FastList)this._tempPlayers.get((Object)currentInstance2)).size();
            boolean joinStrongerInstIfNeeded = currentInstance2.getConfigBoolean("joinStrongerInstIfNeeded");
            int maxDiff = currentInstance2.getConfigInt("joinStrongerInstMaxDiff");
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: iterating through notEnoughtInstances: " + currentInstance2.getId() + " [" + currentInstance2.getStrenghtRate() + "] - playersCount (" + playersCount + "), strenght (" + strenght + ")");
            }
            for (MainEventInstanceType possibleInstance : notEnoughtPlayersInstances) {
                if (possibleInstance == null || fixed.contains(possibleInstance)) continue;
                if (possibleInstance == currentInstance2) continue;
                playersCount = ((FastList)this._tempPlayers.get((Object)currentInstance2)).size();
                if (possibleInstance.getStrenghtRate() == strenght) {
                    if (((FastList)this._tempPlayers.get((Object)possibleInstance)).size() + playersCount < possibleInstance.getConfigInt("minPlayers") || !NexusLoader.detailedDebug) continue;
                    this.print("How could have this happened? (" + currentInstance2.getName() + ", " + possibleInstance.getName() + ")");
                    continue;
                }
                if (!joinStrongerInstIfNeeded || possibleInstance.getStrenghtRate() <= strenght || possibleInstance.getStrenghtRate() - strenght > maxDiff) continue;
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: /// possible instance " + possibleInstance.getName() + "[" + possibleInstance.getStrenghtRate() + "] - playersCount (" + ((FastList)this._tempPlayers.get((Object)possibleInstance)).size() + "), strenght (" + possibleInstance.getStrenghtRate() + ")");
                }
                if ((sumPlayers = ((FastList)this._tempPlayers.get((Object)possibleInstance)).size() + playersCount) < possibleInstance.getConfigInt("minPlayers")) continue;
                int max = possibleInstance.getConfigInt("maxPlayers");
                toMove = sumPlayers > max ? max - ((FastList)this._tempPlayers.get((Object)possibleInstance)).size() : ((FastList)this._tempPlayers.get((Object)currentInstance2)).size();
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: /*/*/ moving " + toMove + " players from " + currentInstance2.getName() + " to " + possibleInstance.getName());
                }
                this.movePlayers(possibleInstance, currentInstance2, toMove);
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: /*/*/ size of " + possibleInstance.getName() + " is now " + ((FastList)this._tempPlayers.get((Object)possibleInstance)).size());
                }
                if (((FastList)this._tempPlayers.get((Object)possibleInstance)).size() < possibleInstance.getConfigInt("minPlayers")) continue;
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: /*/*/ instance " + possibleInstance.getName() + " removed from notEnoughtPlayersInstances.");
                }
                fixed.add(possibleInstance);
            }
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: reorganizing notEnoughtPlayers first part done");
        }
        this.dumpTempPlayers();
        for (MainEventInstanceType currentInstance2 : notEnoughtPlayersInstances) {
            playersCount = ((FastList)this._tempPlayers.get((Object)currentInstance2)).size();
            if (playersCount == 0) continue;
            strenght = currentInstance2.getStrenghtRate();
            boolean joinStrongerInstIfNeeded = currentInstance2.getConfigBoolean("joinStrongerInstIfNeeded");
            int maxDiff = currentInstance2.getConfigInt("joinStrongerInstMaxDiff");
            for (MainEventInstanceType fixedInstance : fixed) {
                if (!joinStrongerInstIfNeeded || fixedInstance.getStrenghtRate() <= strenght || fixedInstance.getStrenghtRate() - strenght > maxDiff || (sumPlayers = ((FastList)this._tempPlayers.get((Object)fixedInstance)).size()) >= fixedInstance.getConfigInt("maxPlayers")) continue;
                toMove = fixedInstance.getConfigInt("maxPlayers") - ((FastList)this._tempPlayers.get((Object)fixedInstance)).size();
                this.movePlayers(fixedInstance, currentInstance2, toMove);
            }
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: reorganizing notEnoughtPlayers second part done");
        }
        this.dumpTempPlayers();
        int c = 0;
        for (MainEventInstanceType toRemove : fixed) {
            notEnoughtPlayersInstances.remove(toRemove);
            ++c;
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: fixed " + c + " notEnoughtPlayers instances");
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: starting tempPlayers reorganizations");
        }
        block6 : for (Map.Entry e2 : this._tempPlayers.entrySet()) {
            int temp;
            int canMove;
            playersCount = ((FastList)e2.getValue()).size();
            if (playersCount == 0) continue;
            strenght = ((MainEventInstanceType)e2.getKey()).getStrenghtRate();
            boolean joinStrongerInstIfNeeded = ((MainEventInstanceType)e2.getKey()).getConfigBoolean("joinStrongerInstIfNeeded");
            int maxDiff = ((MainEventInstanceType)e2.getKey()).getConfigInt("joinStrongerInstMaxDiff");
            if (this.hasEnoughtPlayers((MainEventInstanceType)e2.getKey())) continue;
            while (playersCount > 0) {
                temp = playersCount;
                for (Map.Entry i : this._tempPlayers.entrySet()) {
                    if (playersCount <= 0) break;
                    if (!this.hasEnoughtPlayers((MainEventInstanceType)i.getKey()) || ((MainEventInstanceType)i.getKey()).getStrenghtRate() != strenght || (canMove = ((MainEventInstanceType)i.getKey()).getConfigInt("maxPlayers") - ((FastList)i.getValue()).size()) <= 0 || this.movePlayers((MainEventInstanceType)i.getKey(), (MainEventInstanceType)e2.getKey(), 1) != 1) continue;
                    --playersCount;
                }
                if (playersCount != temp) continue;
            }
            if (playersCount == 0 || !joinStrongerInstIfNeeded) continue;
            while (playersCount > 0) {
                temp = playersCount;
                for (Map.Entry i : this._tempPlayers.entrySet()) {
                    if (playersCount <= 0) break;
                    if (!this.hasEnoughtPlayers((MainEventInstanceType)i.getKey()) || ((MainEventInstanceType)i.getKey()).getStrenghtRate() <= strenght || ((MainEventInstanceType)i.getKey()).getStrenghtRate() - strenght > maxDiff || (canMove = ((MainEventInstanceType)i.getKey()).getConfigInt("maxPlayers") - ((FastList)i.getValue()).size()) <= 0 || this.movePlayers((MainEventInstanceType)i.getKey(), (MainEventInstanceType)e2.getKey(), 1) != 1) continue;
                    --playersCount;
                }
                if (playersCount != temp) continue;
                continue block6;
            }
        }
        if (NexusLoader.detailedDebug) {
            this.print("* AbstractMainEvent: instances organizing FINISHED:");
        }
        this.dumpTempPlayers();
        for (MainEventInstanceType inst : notEnoughtPlayersInstances) {
            int i = 0;
            for (PlayerEventInfo player : (FastList)this._tempPlayers.get((Object)inst)) {
                player.screenMessage(LanguageEngine.getMsg("registering_notEnoughtPlayers"), this.getEventName(), true);
                this._manager.unregisterPlayer(player, true);
                ++i;
            }
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: ... Not enought players for instance " + inst.getName() + " (" + ((FastList)this._tempPlayers.get((Object)inst)).size() + "), instance removed; " + i + " players unregistered.");
            }
            this._tempPlayers.remove((Object)inst);
        }
        int aviableInstances = 0;
        this._instances = new InstanceData[this._tempPlayers.size()];
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: dividing players into teams - instances count = " + this._tempPlayers.size());
        }
        for (Map.Entry e3 : this._tempPlayers.entrySet()) {
            InstanceData instance;
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: STARTING event for instance: " + ((MainEventInstanceType)e3.getKey()).getName());
            }
            this._instances[aviableInstances] = instance = CallBack.getInstance().getOut().createInstance(((MainEventInstanceType)e3.getKey()).getName(), this._manager.getRunTime() * 1000 + 60000, 0, true);
            ((MainEventInstanceType)e3.getKey()).setInstance(instance);
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: ... created InstanceData, duration: " + (this._manager.getRunTime() * 1000 + 60000));
            }
            ++aviableInstances;
            this._teams.put((Object)instance.getId(), (Object)new FastMap());
            int teamsCount = this.initInstanceTeams((MainEventInstanceType)e3.getKey(), instance.getId());
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: ... teamscount = " + teamsCount + "; DIVIND to teams:");
            }
            this.dividePlayersToTeams(instance.getId(), (FastList)e3.getValue(), teamsCount);
        }
        this._tempPlayers.clear();
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: aviable instances = " + aviableInstances);
        }
        if (aviableInstances == 0) {
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: evnet COULD NOT START due to lack of players in instances");
            }
            this.announce(LanguageEngine.getMsg("announce_noInstance"));
            this.clearEvent();
            return false;
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: ... dividePlayers allowed event to start!");
        }
        for (Map.Entry i : this._teams.entrySet()) {
            CallbackManager.getInstance().eventStarts(((Integer)i.getKey()).intValue(), this.getEventType(), ((FastMap)i.getValue()).values());
            for (EventTeam team : ((FastMap)i.getValue()).values()) {
                team.calcAverageLevel();
            }
        }
        return true;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void dividePlayersToTeams(int instanceId, FastList<PlayerEventInfo> players, int teamsCount) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: /// dividingplayers to teams for INSTANCE " + instanceId);
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: /// players count = " + players.size());
        }
        if (!(this.getEventType().isFFAEvent() || teamsCount <= 1)) {
            int teamId;
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: /// team based event");
            }
            String type = this.getString("divideToTeamsMethod");
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: /// using method: " + type);
            }
            Collections.sort(players, EventManager.getInstance().compareByLevels);
            if (type.startsWith("PvPs")) {
                Collections.sort(players, EventManager.getInstance().compareByPvps);
            }
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: /// players sorted:");
            }
            FastMap sortedPlayers = new FastMap();
            for (ClassType classType : ClassType.values()) {
                sortedPlayers.put((Object)classType, (Object)new FastList());
            }
            for (PlayerEventInfo pi : players) {
                ((FastList)sortedPlayers.get((Object)pi.getClassType())).add((Object)pi);
            }
            for (Map.Entry te : sortedPlayers.entrySet()) {
                if (!NexusLoader.detailedDebug) continue;
                this.print("AbstractMainEvent: /// ... " + ((ClassType)te.getKey()).toString() + " - " + ((FastList)te.getValue()).size() + " players");
            }
            if (this.getBoolean("balanceHealersInTeams")) {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: /// balancing healers in teams");
                }
                teamId = 0;
                for (int healersCount = ((FastList)sortedPlayers.get((Object)ClassType.Priest)).size(); healersCount > 0; --healersCount) {
                    PlayerEventInfo player = (PlayerEventInfo)((FastList)sortedPlayers.get((Object)ClassType.Priest)).head().getNext().getValue();
                    ((FastList)sortedPlayers.get((Object)ClassType.Priest)).remove((Object)player);
                    player.onEventStart((EventGame)this);
                    ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)(++teamId))).addPlayer(player, true);
                    if (teamId < teamsCount) continue;
                    teamId = 0;
                }
            }
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: /// healers balanced into teams:");
            }
            for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
                if (!NexusLoader.detailedDebug) continue;
                this.print("AbstractMainEvent: /// team " + team.getTeamName() + " has " + team.getPlayers().size() + " healers");
            }
            teamId = 0;
            for (Map.Entry e : sortedPlayers.entrySet()) {
                for (PlayerEventInfo pi2 : (FastList)e.getValue()) {
                    int leastPlayers = Integer.MAX_VALUE;
                    for (EventTeam team2 : ((FastMap)this._teams.get((Object)instanceId)).values()) {
                        if (team2.getPlayers().size() >= leastPlayers) continue;
                        leastPlayers = team2.getPlayers().size();
                        teamId = team2.getTeamId();
                    }
                    pi2.onEventStart((EventGame)this);
                    ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)teamId)).addPlayer(pi2, true);
                }
            }
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: /// players divided:");
            }
            for (EventTeam team3 : ((FastMap)this._teams.get((Object)instanceId)).values()) {
                if (!NexusLoader.detailedDebug) continue;
                this.print("AbstractMainEvent: /// team " + team3.getTeamName() + " has " + team3.getPlayers().size() + " PLAYERS");
            }
            return;
        } else {
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: /// FFA event");
            }
            for (PlayerEventInfo pi : players) {
                pi.onEventStart((EventGame)this);
                ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)1)).addPlayer(pi, true);
            }
        }
    }

    protected void createParties(int partySize) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: CREATING PARTIES... ");
        }
        for (Map.Entry teams : this._teams.entrySet()) {
            if (NexusLoader.detailedDebug) {
                this.print("* AbstractMainEvent: PROCESSING INSTANCE " + teams.getKey() + " (creating parties)");
            }
            for (EventTeam team : ((FastMap)teams.getValue()).values()) {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: / parties: processing team " + team.getTeamName());
                }
                int totalCount = 0;
                FastMap players = new FastMap();
                for (ClassType classType : ClassType.values()) {
                    players.put((Object)classType, (Object)new FastList());
                }
                for (PlayerEventInfo player : team.getPlayers()) {
                    if (!player.isOnline()) continue;
                    ((FastList)players.get((Object)player.getClassType())).add((Object)player);
                    ++totalCount;
                }
                for (List pls : players.values()) {
                    Collections.sort(pls, EventManager.getInstance().compareByLevels);
                }
                int healersCount = ((FastList)players.get((Object)ClassType.Priest)).size();
                int partiesCount = (int)Math.ceil((double)totalCount / (double)partySize);
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: ////// total count of players in the team " + totalCount + "; PARTIES COUNT " + partiesCount + "; healers count " + healersCount);
                }
                FastList toParty = new FastList();
                int healersToGive = (int)Math.ceil((double)healersCount / (double)partiesCount);
                if (healersToGive == 0) {
                    healersToGive = 1;
                }
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: ////// healersToGive to each party: " + healersToGive);
                }
                for (int i = 0; i < partiesCount; ++i) {
                    if (healersCount > 0) {
                        for (int h = 0; h < healersToGive && healersCount >= healersToGive; --healersCount, ++h) {
                            PlayerEventInfo pi = (PlayerEventInfo)((FastList)players.get((Object)ClassType.Priest)).head().getNext().getValue();
                            if (pi == null) {
                                pi = (PlayerEventInfo)((FastList)players.get((Object)ClassType.Priest)).head().getNext().getValue();
                            }
                            toParty.add((Object)pi);
                            ((FastList)players.get((Object)ClassType.Priest)).remove((Object)pi);
                        }
                    }
                    boolean b = false;
                    while (toParty.size() < partySize) {
                        boolean added = false;
                        Iterator i$ = ((FastList)players.get((Object)(b ? ClassType.Mystic : ClassType.Fighter))).iterator();
                        if (i$.hasNext()) {
                            PlayerEventInfo fighter = (PlayerEventInfo)i$.next();
                            toParty.add((Object)fighter);
                            ((FastList)players.get((Object)(b ? ClassType.Mystic : ClassType.Fighter))).remove((Object)fighter);
                            added = true;
                        }
                        boolean bl = b = !b;
                        if (!added && (i$ = ((FastList)players.get((Object)(b ? ClassType.Mystic : ClassType.Fighter))).iterator()).hasNext()) {
                            PlayerEventInfo mystic = (PlayerEventInfo)i$.next();
                            toParty.add((Object)mystic);
                            ((FastList)players.get((Object)(b ? ClassType.Mystic : ClassType.Fighter))).remove((Object)mystic);
                            added = true;
                        }
                        if (added) continue;
                        if (healersCount <= 0) break;
                        i$ = ((FastList)players.get((Object)ClassType.Priest)).iterator();
                        if (!i$.hasNext()) continue;
                        PlayerEventInfo healer = (PlayerEventInfo)i$.next();
                        toParty.add((Object)healer);
                        ((FastList)players.get((Object)ClassType.Priest)).remove((Object)healer);
                        added = true;
                        --healersCount;
                    }
                    this.dumpParty(team, toParty);
                    this.partyPlayers(toParty);
                    toParty.clear();
                }
            }
        }
    }

    private void dumpParty(EventTeam team, FastList<PlayerEventInfo> players) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: ////// START of dump of party for team " + team.getTeamName());
        }
        for (PlayerEventInfo pl : players) {
            if (!NexusLoader.detailedDebug) continue;
            this.print("AbstractMainEvent: /*/*/*/*/*/*/ player " + pl.getPlayersName() + " is of class id " + pl.getClassType().toString());
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: ////// END of dump of party for team " + team.getTeamName());
        }
    }

    protected void partyPlayers(FastList<PlayerEventInfo> players) {
        block18 : {
            try {
                if (players.size() > 1) {
                    ++this._partiesCount;
                    PartyData party = null;
                    int count = 0;
                    int nextDelay = 800;
                    for (PlayerEventInfo player : players) {
                        if (player.getParty() != null) {
                            player.getParty().removePartyMember(player);
                        }
                        player.setCanInviteToParty(false);
                    }
                    PlayerEventInfo leader = null;
                    for (PlayerEventInfo player2 : players) {
                        if (count == 0) {
                            leader = player2;
                            party = new PartyData(player2);
                        } else {
                            CallBack.getInstance().getOut().scheduleGeneral(new AddToParty(party, player2), 800 * count);
                        }
                        if (++count < 9) continue;
                        break;
                    }
                    if (leader != null) {
                        if (NexusLoader.detailedDebug) {
                            this.print("AbstractMainEvent: reallowing inviting to the party back to the leader (" + leader.getPlayersName() + ").");
                        } else if (NexusLoader.detailedDebug) {
                            this.print("AbstractMainEvent: NOT reallowing inviting to the party back to the leader because he is null!");
                        }
                    }
                    final PlayerEventInfo fLeader = leader;
                    CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                        @Override
                        public void run() {
                            if (fLeader != null) {
                                fLeader.setCanInviteToParty(true);
                            }
                        }
                    }, 800 * (count + 1));
                }
            }
            catch (Exception e) {
                block17 : {
                    e.printStackTrace();
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: createParties error (and parties will be deleted): " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
                    }
                    this.debug("Error while partying players: " + e.toString() + ". Deleting parties...");
                    try {
                        for (PlayerEventInfo player : players) {
                            if (player.getParty() == null) continue;
                            player.getParty().removePartyMember(player);
                        }
                    }
                    catch (Exception e2) {
                        e2.printStackTrace();
                        if (!NexusLoader.detailedDebug) break block17;
                        this.print("AbstractMainEvent: error while removing parties (cause of another error): " + NexusLoader.getTraceString((StackTraceElement[])e2.getStackTrace()));
                    }
                }
                this.debug("Parties deleted.");
                if (!NexusLoader.detailedDebug) break block18;
                this.print("AbstractMainEvent: parties deleted.");
            }
        }
    }

    protected void teleportPlayers(int instanceId, SpawnType type, boolean ffa) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: ========================================");
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: STARTING TO TELEPORT PLAYERS (ffa = " + ffa + ")");
        }
        boolean removeBuffs = this.getBoolean("removeBuffsOnStart");
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: removeBuffs = " + removeBuffs);
        }
        int i = 0;
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            int radius;
            EventSpawn spawn = this.getSpawn(type, ffa ? -1 : player.getTeamId());
            if (spawn == null) {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: ! Missing spawn for team " + (((FastMap)this._teams.get((Object)instanceId)).size() == 1 ? -1 : player.getTeamId()) + ", map " + this._manager.getMap().getMapName() + ", event " + this.getEventType().getAltTitle() + " !!");
                }
                NexusLoader.debug((String)("Missing spawn for team " + (((FastMap)this._teams.get((Object)instanceId)).size() == 1 ? -1 : player.getTeamId()) + ", map " + this._manager.getMap().getMapName() + ", event " + this.getEventType().getAltTitle() + " !!"), (Level)Level.SEVERE);
            }
            if ((radius = spawn.getRadius()) == -1) {
                radius = 50;
            }
            Loc loc = new Loc(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ());
            loc.addRadius(radius);
            player.teleport(loc, 0, false, instanceId);
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: /// player " + player.getPlayersName() + " teleported to " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + " (radius = " + radius + "), SPAWN ID " + spawn.getSpawnId() + ", SPAWN TEAM " + spawn.getSpawnTeam());
            }
            if (removeBuffs) {
                player.removeBuffs();
            }
            ++i;
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: " + i + " PLAYERS TELEPORTED");
        }
        this.clearMapHistory(-1, type);
    }

    protected boolean checkPlayers(int instanceId) {
        if (!this.checkIfEventCanContinue(instanceId, null)) {
            this.announce(instanceId, LanguageEngine.getMsg("announce_alldisconnected"));
            this.endInstance(instanceId, true, false, true);
            this.debug(this.getEventName() + ": no players left in the teams after teleporting to the event, the fight can't continue. The event has been aborted!");
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: check players: FALSE (NOT ENOUGHT players to start the event)");
            }
            return false;
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: check players: OK (enought players to start the event)");
        }
        return true;
    }

    protected void enableMarkers(int instanceId, boolean useEventSpawnMarkers) {
        if (!this._enableRadar) {
            return;
        }
        for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                pi.createRadar();
            }
        }
        if (useEventSpawnMarkers) {
            FastList<EventSpawn> markers = null;
            for (EventTeam team2 : ((FastMap)this._teams.get((Object)instanceId)).values()) {
                markers = this._manager.getMap().getMarkers(team2.getTeamId());
                if (markers == null || markers.isEmpty()) continue;
                EventSpawn marker = null;
                Iterator i$ = markers.iterator();
                if (i$.hasNext()) {
                    EventSpawn pMarkers;
                    marker = pMarkers = i$.next();
                }
                for (PlayerEventInfo pi : team2.getPlayers()) {
                    pi.getRadar().setLoc(marker.getLoc().getX(), marker.getLoc().getY(), marker.getLoc().getZ());
                    pi.getRadar().setRepeat(true);
                    pi.getRadar().enable();
                }
            }
        }
    }

    protected void removeStaticDoors(int instanceId) {
        try {
            CallBack.getInstance().getOut().addDoorToInstance(instanceId, 17190001, true);
            CallBack.getInstance().getOut().getInstanceDoors(instanceId)[0].openMe();
        }
        catch (Exception e) {
            NexusLoader.debug((String)("tried to removeStaticDoors, but an error occured - " + e.toString()));
        }
    }

    protected void disableMarkers(int instanceId) {
        if (!this._enableRadar) {
            return;
        }
        for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                pi.getRadar().disable();
            }
        }
    }

    protected void addMarker(PlayerEventInfo pi, EventSpawn marker, boolean repeat) {
        if (!this._enableRadar) {
            return;
        }
        pi.getRadar().setLoc(marker.getLoc().getX(), marker.getLoc().getY(), marker.getLoc().getZ());
        pi.getRadar().setRepeat(repeat);
        if (!pi.getRadar().isEnabled()) {
            pi.getRadar().enable();
        }
    }

    protected void removeMarker(PlayerEventInfo pi, EventSpawn marker) {
        pi.removeRadarMarker(marker.getLoc().getX(), marker.getLoc().getY(), marker.getLoc().getZ());
    }

    protected void setupTitles(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: SETUPING TITLES");
        }
        for (PlayerEventInfo pi : this.getPlayers(instanceId)) {
            PartyData pt;
            if (this._allowSchemeBuffer) {
                EventBuffer.getInstance().buffPlayer(pi, true);
            }
            if (this._removePartiesOnStart && (pt = pi.getParty()) != null) {
                pi.getParty().removePartyMember(pi);
            }
            if (!pi.isTitleUpdated()) continue;
            pi.setTitle(this.getTitle(pi), true);
        }
    }

    protected EventSpawn getSpawn(SpawnType type, int teamId) {
        EventMap map = this._manager.getMap();
        if (map == null) {
            return null;
        }
        return map.getNextSpawn(teamId, type);
    }

    protected void clearMapHistory(int teamId, SpawnType type) {
        EventMap map = this._manager.getMap();
        if (map != null) {
            map.clearHistory(teamId, type);
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: map history clean done");
            }
        } else if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: couldn't clean map, map is NULL!");
        }
    }

    protected void forceSitAll(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: FORCE SIT ALL");
        }
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            player.abortCasting();
            player.disableAfkCheck(true);
            player.sitDown();
        }
    }

    protected void forceStandAll(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: FORCE STAND UP");
        }
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            player.disableAfkCheck(false);
            player.standUp();
        }
    }

    protected void sysMsgToAll(String text) {
        if (NexusLoader.detailedDebug) {
            this.print("? AbstractMainEvent: sysMsgToAll - " + text);
        }
        for (PlayerEventInfo pi : this.getPlayers(0)) {
            pi.sendMessage(text);
        }
    }

    protected void sysMsgToAll(int instance, String text) {
        if (NexusLoader.detailedDebug) {
            this.print("? AbstractMainEvent: sysMsgToAll to instance " + instance + "; text= " + text);
        }
        for (PlayerEventInfo pi : this.getPlayers(instance)) {
            pi.sendMessage(text);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public Set<PlayerEventInfo> getPlayers(int instanceId) {
        FastSet players = new FastSet();
        if (this._teams.isEmpty()) {
            return players;
        }
        if (instanceId == 0 || instanceId == 1 || instanceId == -1) {
            for (FastMap fm : this._teams.values()) {
                for (EventTeam team : fm.values()) {
                    for (PlayerEventInfo player : team.getPlayers()) {
                        players.add(player);
                    }
                }
            }
            return players;
        } else {
            for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
                for (PlayerEventInfo player : team.getPlayers()) {
                    players.add(player);
                }
            }
        }
        return players;
    }

    protected void initWaweRespawns(int delay) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: STARTING WAWE SPAWN SYSTEM");
        }
        this._waweScheduler = new WaweRespawnScheduler(delay * 1000);
    }

    public void addSpectator(PlayerEventInfo gm, int instanceId) {
        if (gm.isInEvent() || gm.isRegistered()) {
            gm.sendMessage(LanguageEngine.getMsg("observing_alreadyRegistered"));
            return;
        }
        if (this._spectators != null) {
            EventSpawn selected = null;
            for (EventSpawn s : EventManager.getInstance().getMainEventManager().getMap().getSpawns()) {
                if (s.getSpawnType() != SpawnType.Regular && s.getSpawnType() != SpawnType.Safe) continue;
                selected = s;
            }
            if (selected == null) {
                gm.sendMessage(LanguageEngine.getMsg("observing_noSpawn"));
                return;
            }
            gm.initOrigInfo();
            gm.setInstanceId(instanceId);
            gm.teleToLocation(selected.getLoc().getX(), selected.getLoc().getY(), selected.getLoc().getZ(), false);
            Object i$ = this._spectators;
            synchronized (i$) {
                this._spectators.add(gm);
            }
        }
    }

    public void removeSpectator(PlayerEventInfo gm) {
        if (this._spectators != null) {
            gm.setInstanceId(0);
            gm.teleToLocation(gm.getOrigLoc().getX(), gm.getOrigLoc().getY(), gm.getOrigLoc().getZ(), false);
            List<PlayerEventInfo> list = this._spectators;
            synchronized (list) {
                this._spectators.remove((Object)gm);
            }
        }
    }

    public boolean isWatching(PlayerEventInfo gm) {
        return this._spectators != null && this._spectators.contains((Object)gm);
    }

    protected void clearPlayers(boolean unregister, int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent:  =====================");
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: CALLED CLEAR PLAYERS for instanceId " + instanceId + ", unregister = " + unregister);
        }
        if (instanceId == 0) {
            if (this._waweScheduler != null) {
                this._waweScheduler.stop();
            }
            EventManager.getInstance().clearDisconnectedPlayers();
        }
        if (this._spectators != null) {
            for (PlayerEventInfo spectator : this._spectators) {
                if (instanceId != 0 && spectator.getInstanceId() != instanceId) continue;
                spectator.setInstanceId(0);
                spectator.teleToLocation(spectator.getOrigLoc().getX(), spectator.getOrigLoc().getY(), spectator.getOrigLoc().getZ(), false);
                this._spectators.remove((Object)spectator);
            }
            if (instanceId == 0) {
                this._spectators.clear();
                this._spectators = null;
            }
        }
        this.cleanMap(instanceId);
        int unregistered = 0;
        if (unregister) {
            switch (this._manager.getState()) {
                case REGISTERING: {
                    for (PlayerEventInfo player2 : this._manager.getPlayers()) {
                        player2.setIsRegisteredToMainEvent(false, null);
                        CallBack.getInstance().getPlayerBase().eventEnd(player2);
                        ++unregistered;
                    }
                    break;
                }
                case END: 
                case RUNNING: 
                case TELE_BACK: {
                    this._manager.paralizeAll(false);
                    for (PlayerEventInfo player2 : this.getPlayers(instanceId)) {
                        this._manager.getPlayers().remove((Object)player2);
                        if (player2.getEventTeam() != null) {
                            player2.getEventTeam().removePlayer(player2);
                        }
                        player2.setIsRegisteredToMainEvent(false, null);
                        CallBack.getInstance().getPlayerBase().eventEnd(player2);
                        ++unregistered;
                    }
                    for (PlayerEventInfo player2 : this._manager.getPlayers()) {
                        if (instanceId != 0 && player2.getInstanceId() != instanceId) continue;
                        player2.setIsRegisteredToMainEvent(false, null);
                        CallBack.getInstance().getPlayerBase().eventEnd(player2);
                        ++unregistered;
                    }
                    break;
                }
            }
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: unregistered " + unregistered + " players");
        }
        if (this._instances != null) {
            for (InstanceData instance : this._instances) {
                if (instanceId != 0 && instanceId != instance.getId()) continue;
                CallbackManager.getInstance().eventEnded(instanceId, this.getEventType(), ((FastMap)this._teams.get((Object)instance.getId())).values());
                for (EventTeam team : ((FastMap)this._teams.get((Object)instance.getId())).values()) {
                    for (PlayerEventInfo pi : team.getPlayers()) {
                        team.removePlayer(pi);
                    }
                }
            }
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: Event " + this.getEventName() + " finished clearPlayers() for instance ID " + instanceId);
        }
        NexusLoader.debug((String)("Event " + this.getEventName() + " finished clearPlayers() for instance ID " + instanceId));
        if (instanceId == 0) {
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: checking if all unregistered...");
            }
            Collection playersLeft = this.getPlayers(0);
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: playersLeft size = " + playersLeft.size());
            }
            if (playersLeft.size() > 0 || this._manager.getPlayers().size() > 0) {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: the event hasn't cleaned itself properly, perfoming additional cleanings...");
                }
                NexusLoader.debug((String)"The event hasn't cleant itself properly. There was an error while running the event, propably. Cleaning it other way.", (Level)Level.WARNING);
                for (PlayerEventInfo player : playersLeft) {
                    if (player.getEventTeam() != null) {
                        player.getEventTeam().removePlayer(player);
                    }
                    player.setIsRegisteredToMainEvent(false, null);
                    CallBack.getInstance().getPlayerBase().eventEnd(player);
                }
                playersLeft = this._manager.getPlayers();
                for (PlayerEventInfo player3 : playersLeft) {
                    if (player3.getEventTeam() != null) {
                        player3.getEventTeam().removePlayer(player3);
                    }
                    player3.setIsRegisteredToMainEvent(false, null);
                    CallBack.getInstance().getPlayerBase().eventEnd(player3);
                }
                playersLeft = this.getPlayers(0);
                if (playersLeft.size() == 0) {
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: additional cleanings finished");
                    }
                    NexusLoader.debug((String)"Additional cleaning finished. All players unregistered successfully now.");
                }
            }
        }
        if (instanceId == 0) {
            this._tempPlayers.clear();
            this._rewardedInstances.clear();
            this._instances = null;
            this._manager.clean(null);
        }
    }

    public void setKillsStats(PlayerEventInfo player, int ammount) {
    }

    public void setDeathsStats(PlayerEventInfo player, int ammount) {
    }

    public void setScoreStats(PlayerEventInfo player, int ammount) {
    }

    @Override
    public RewardPosition[] getRewardTypes() {
        return this._rewardTypes;
    }

    public void setRewardTypes(RewardPosition[] types) {
        this._rewardTypes = types;
    }

    public String getString(String propName) {
        if (this._configs.containsKey((Object)propName)) {
            String value = ((ConfigModel)this._configs.get((Object)propName)).getValue();
            return value;
        }
        return "";
    }

    public int getInt(String propName) {
        if (this._configs.containsKey((Object)propName)) {
            int value = ((ConfigModel)this._configs.get((Object)propName)).getValueInt();
            return value;
        }
        return 0;
    }

    public boolean getBoolean(String propName) {
        if (this._configs.containsKey((Object)propName)) {
            return ((ConfigModel)this._configs.get((Object)propName)).getValueBoolean();
        }
        return false;
    }

    protected void addConfig(ConfigModel model) {
        this._configs.put((Object)model.getKey(), (Object)model);
    }

    protected void removeConfig(String key) {
        this._configs.remove((Object)key);
    }

    protected void addConfig(String category, ConfigModel model) {
        if (!this._configCategories.contains((Object)category)) {
            this._configCategories.add((Object)category);
        }
        this._configs.put((Object)model.getKey(), (Object)model.setCategory(category));
    }

    protected void addMapConfig(ConfigModel model) {
        this._mapConfigs.put((Object)model.getKey(), (Object)model);
    }

    protected void addInstanceTypeConfig(ConfigModel model) {
        this._instanceTypeConfigs.put((Object)model.getKey(), (Object)model);
    }

    protected void removeConfigs() {
        this._configCategories.clear();
        this._configs.clear();
    }

    protected void removeMapConfigs() {
        this._mapConfigs.clear();
    }

    protected void removeInstanceTypeConfigs() {
        this._instanceTypeConfigs.clear();
    }

    @Override
    public final Map<String, ConfigModel> getConfigs() {
        return this._configs;
    }

    @Override
    public void clearConfigs() {
        this.removeConfigs();
        this.removeMapConfigs();
        this.removeInstanceTypeConfigs();
    }

    @Override
    public FastList<String> getCategories() {
        return this._configCategories;
    }

    @Override
    public void setConfig(String key, String value, boolean addToValue) {
        if (!this._configs.containsKey((Object)key)) {
            return;
        }
        if (!addToValue) {
            ((ConfigModel)this._configs.get((Object)key)).setValue(value);
        } else {
            ((ConfigModel)this._configs.get((Object)key)).addToValue(value);
        }
    }

    @Override
    public Map<String, ConfigModel> getMapConfigs() {
        return this._mapConfigs;
    }

    @Override
    public Map<SpawnType, String> getAviableSpawnTypes() {
        return this._spawnTypes;
    }

    public int getMaxPlayers() {
        return this.getInt("maxPlayers");
    }

    public String getEstimatedTimeLeft() {
        return "N/A";
    }

    @Override
    public boolean canRun(EventMap map) {
        return this.getMissingSpawns(map).length() == 0;
    }

    @Override
    public abstract String getMissingSpawns(EventMap var1);

    protected String addMissingSpawn(SpawnType type, int team, int count) {
        return "<font color=B46F6B>" + this.getEventType().getAltTitle() + "</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>" + type.toString().toUpperCase() + "</font> <font color=9f9f9f>spawn for team " + team + " " + (team == 0 ? "(team doesn't matter)" : "") + " count " + count + " (or more)</font><br1>";
    }

    public void announce(int instance, String msg) {
        for (PlayerEventInfo pi : this.getPlayers(instance)) {
            pi.creatureSay(this.getEventName() + ": " + msg, this.getEventName(), 18);
        }
        if (this._spectators != null) {
            for (PlayerEventInfo spectator : this._spectators) {
                if (!spectator.isOnline() || spectator.getInstanceId() != instance) continue;
                spectator.creatureSay(this.getEventName() + ": " + msg, this.getEventName(), 18);
            }
        }
    }

    public void announce(int instance, String msg, int team) {
        if (NexusLoader.detailedDebug) {
            this.print("? AbstractMainEvent: announcing to instance " + instance + " team " + team + " msg: " + msg);
        }
        for (PlayerEventInfo pi : this.getPlayers(instance)) {
            if (pi.getTeamId() != team) continue;
            pi.creatureSay(this.getEventName() + ": " + msg, this.getEventName(), 18);
        }
        if (this._spectators != null) {
            for (PlayerEventInfo spectator : this._spectators) {
                if (!spectator.isOnline() || spectator.getInstanceId() != instance) continue;
                spectator.creatureSay(this.getEventName() + ": " + msg, this.getEventName() + " [T" + team + " msg]", 18);
            }
        }
    }

    public void announceToAllTeamsBut(int instance, String msg, int excludedTeam) {
        if (NexusLoader.detailedDebug) {
            this.print("? AbstractMainEvent: announcing to all teams but " + excludedTeam + ", instance " + instance + " msg " + msg);
        }
        for (PlayerEventInfo pi : this.getPlayers(instance)) {
            if (pi.getTeamId() == excludedTeam) continue;
            pi.creatureSay(this.getEventName() + ": " + msg, this.getEventName(), 18);
        }
        if (this._spectators != null) {
            for (PlayerEventInfo spectator : this._spectators) {
                if (!spectator.isOnline() || spectator.getInstanceId() != instance) continue;
                spectator.creatureSay(this.getEventName() + ": " + msg, this.getEventName() + " [all except T" + excludedTeam + " msg]", 18);
            }
        }
    }

    public void screenAnnounce(int instance, String msg) {
        if (NexusLoader.detailedDebug) {
            this.print("? AbstractMainEvent: screenannounce to instance " + instance + " msg: " + msg);
        }
        for (PlayerEventInfo pi : this.getPlayers(instance)) {
            pi.creatureSay(msg, this.getEventName(), 15);
        }
        if (this._spectators != null) {
            for (PlayerEventInfo spectator : this._spectators) {
                if (!spectator.isOnline() || spectator.getInstanceId() != instance) continue;
                spectator.creatureSay(msg, this.getEventName(), 15);
            }
        }
    }

    protected void scheduleRevive(PlayerEventInfo pi, int time) {
        new ReviveTask(this, pi, time);
    }

    protected void setInstanceNotReceiveRewards(int instanceId) {
        List<Integer> list = this._rewardedInstances;
        synchronized (list) {
            this._rewardedInstances.add(instanceId);
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: instance of ID " + instanceId + " has been marked as NOTREWARDED");
        }
    }

    protected void rewardFirstRegisteredFFA(List<PlayerEventInfo> list) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: rewarding first registered (ffa)");
        }
        int count = 0;
        if (list != null) {
            for (PlayerEventInfo player : list) {
                if (!this._firstRegistered.contains((Object)player) || !player.isOnline()) continue;
                player.sendMessage(LanguageEngine.getMsg("event_extraReward", this.firstRegisteredRewardCount));
                EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.FirstRegistered, null, player.getTotalTimeAfk(), 0, 0);
                ++count;
            }
        } else {
            for (PlayerEventInfo player : this._firstRegistered) {
                player.sendMessage(LanguageEngine.getMsg("event_extraReward", this.firstRegisteredRewardCount));
                EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.FirstRegistered, null, player.getTotalTimeAfk(), 0, 0);
                ++count;
            }
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: " + count + " players were given FirstRegistered reward");
        }
    }

    protected void rewardFirstRegistered(List<EventTeam> list) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: rewarding first registered (teams)");
        }
        int count = 0;
        if (list != null) {
            for (EventTeam t : list) {
                for (PlayerEventInfo player : t.getPlayers()) {
                    if (!this._firstRegistered.contains((Object)player) || !player.isOnline()) continue;
                    player.sendMessage(LanguageEngine.getMsg("event_extraReward", this.firstRegisteredRewardCount));
                    EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.FirstRegistered, null, player.getTotalTimeAfk(), 0, 0);
                    ++count;
                }
            }
        } else {
            for (PlayerEventInfo player : this._firstRegistered) {
                player.sendMessage(LanguageEngine.getMsg("event_extraReward", this.firstRegisteredRewardCount));
                EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.FirstRegistered, null, player.getTotalTimeAfk(), 0, 0);
                ++count;
            }
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: " + count + " players were given FirstRegistered reward");
        }
    }

    protected void rewardAllPlayersFromTeam(int instanceId, int minScore, int minKills, int teamId) {
        try {
            if (this.getEventType().isFFAEvent()) {
                NexusLoader.debug((String)(this.getEventName() + " cannot use rewardAllPlayers since this is a FFA event."), (Level)Level.SEVERE);
                return;
            }
            if (this._instances == null) {
                NexusLoader.debug((String)(this.getEventName() + " _instances were null when the event tried to reward!"), (Level)Level.SEVERE);
                return;
            }
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: CALLED REWARD ALL PLAYERS  for instance " + instanceId + ", min score " + minScore + ", min kills " + minKills);
            }
            boolean firstXRewardWinners = "WinnersOnly".equals(this.firstRegisteredRewardType);
            for (InstanceData instance : this._instances) {
                if (instance.getId() != instanceId && instanceId != -1) continue;
                List<Integer> list = this._rewardedInstances;
                synchronized (list) {
                    if (this._rewardedInstances.contains(instance.getId())) {
                        continue;
                    }
                    this._rewardedInstances.add(instance.getId());
                }
                EventTeam team = (EventTeam)((FastMap)this._teams.get((Object)instance.getId())).get((Object)teamId);
                if (team == null) {
                    NexusLoader.debug((String)(this.getEventName() + " no team of ID " + teamId + " to be rewarded!"), (Level)Level.SEVERE);
                    return;
                }
                int playersCount = team.getPlayers().size();
                FastList sorted = new FastList();
                FastMap map = new FastMap();
                for (PlayerEventInfo player2 : team.getPlayers()) {
                    sorted.add((Object)player2);
                }
                Collections.sort(sorted, EventManager.getInstance().comparePlayersScore);
                for (PlayerEventInfo player2 : sorted) {
                    map.put(player2, this.getPlayerData(player2).getScore());
                }
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: before giving reward");
                }
                Map<Integer, List<PlayerEventInfo>> scores = EventRewardSystem.getInstance().rewardPlayers((Map<PlayerEventInfo, Integer>)map, this.getEventType(), 1, minScore, this._afkHalfReward, this._afkNoReward);
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: rewards given");
                }
                int place = 1;
                int limitToAnnounce = this.getInt("announcedTopPlayersCount");
                int totalLimit = Math.min(limitToAnnounce * 2, 15);
                int counter = 1;
                for (Map.Entry<Integer, List<PlayerEventInfo>> e : scores.entrySet()) {
                    if (counter > totalLimit) break;
                    if (place > limitToAnnounce) continue;
                    for (PlayerEventInfo player3 : e.getValue()) {
                        if (counter > totalLimit) break;
                        this.announce(instance.getId(), LanguageEngine.getMsg("event_announceScore", place, player3.getPlayersName(), this.getPlayerData(player3).getScore()));
                        ++counter;
                    }
                    ++place;
                }
                place = 1;
                for (Map.Entry<Integer, List<PlayerEventInfo>> i : scores.entrySet()) {
                    if (place == 1) {
                        if (firstXRewardWinners) {
                            this.rewardFirstRegisteredFFA(i.getValue());
                        }
                        if (i.getValue().size() > 1) {
                            if (playersCount > i.getValue().size()) {
                                TextBuilder tb = new TextBuilder("*** ");
                                for (PlayerEventInfo player4 : i.getValue()) {
                                    tb.append(LanguageEngine.getMsg("event_ffa_announceWinner2_part1", player4.getPlayersName()) + " ");
                                }
                                String s = tb.toString();
                                tb = new TextBuilder(s.substring(0, s.length() - 4));
                                tb.append(LanguageEngine.getMsg("event_ffa_announceWinner2_part2"));
                                this.announce(instance.getId(), tb.toString());
                            } else {
                                this.announce(instance.getId(), "*** " + LanguageEngine.getMsg("event_ffa_announceWinner3"));
                            }
                        } else {
                            this.announce(instance.getId(), "*** " + LanguageEngine.getMsg("event_ffa_announceWinner1", i.getValue().get(0).getPlayersName()));
                        }
                        for (PlayerEventInfo player3 : i.getValue()) {
                            this.getPlayerData(player3).getGlobalStats().raise(GlobalStats.GlobalStatType.WINS, 1);
                        }
                    } else {
                        for (PlayerEventInfo player3 : i.getValue()) {
                            this.getPlayerData(player3).getGlobalStats().raise(GlobalStats.GlobalStatType.LOSES, 1);
                        }
                    }
                    ++place;
                }
            }
            if (!firstXRewardWinners) {
                this.rewardFirstRegisteredFFA(null);
            }
            this.saveGlobalStats(instanceId);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void rewardAllPlayers(int instanceId, int minScore, int minKills) {
        try {
            if (!this.getEventType().isFFAEvent()) {
                NexusLoader.debug((String)(this.getEventName() + " cannot use rewardAllPlayers since it is an non-FFA event."), (Level)Level.SEVERE);
                return;
            }
            if (this._instances == null) {
                NexusLoader.debug((String)(this.getEventName() + " _instances were null when the event tried to reward!"), (Level)Level.SEVERE);
                return;
            }
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: CALLED REWARD ALL PLAYERS  for instance " + instanceId + ", min score " + minScore + ", min kills " + minKills);
            }
            boolean firstXRewardWinners = "WinnersOnly".equals(this.firstRegisteredRewardType);
            for (InstanceData instance : this._instances) {
                if (instance.getId() != instanceId && instanceId != -1) continue;
                List<Integer> list = this._rewardedInstances;
                synchronized (list) {
                    if (this._rewardedInstances.contains(instance.getId())) {
                        continue;
                    }
                    this._rewardedInstances.add(instance.getId());
                }
                int playersCount = this.getPlayers(instance.getId()).size();
                FastList sorted = new FastList();
                FastMap map = new FastMap();
                for (PlayerEventInfo player2 : this.getPlayers(instance.getId())) {
                    sorted.add((Object)player2);
                }
                Collections.sort(sorted, EventManager.getInstance().comparePlayersScore);
                for (PlayerEventInfo player2 : sorted) {
                    map.put(player2, this.getPlayerData(player2).getScore());
                }
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: before giving reward");
                }
                Map<Integer, List<PlayerEventInfo>> scores = EventRewardSystem.getInstance().rewardPlayers((Map<PlayerEventInfo, Integer>)map, this.getEventType(), 1, minScore, this._afkHalfReward, this._afkNoReward);
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: rewards given");
                }
                int place = 1;
                int limitToAnnounce = this.getInt("announcedTopPlayersCount");
                int totalLimit = Math.min(limitToAnnounce * 2, 15);
                int counter = 1;
                for (Map.Entry<Integer, List<PlayerEventInfo>> e : scores.entrySet()) {
                    if (counter > totalLimit) break;
                    if (place > limitToAnnounce) continue;
                    for (PlayerEventInfo player3 : e.getValue()) {
                        if (counter > totalLimit) break;
                        this.announce(instance.getId(), LanguageEngine.getMsg("event_announceScore", place, player3.getPlayersName(), this.getPlayerData(player3).getScore()));
                        ++counter;
                    }
                    ++place;
                }
                place = 1;
                for (Map.Entry<Integer, List<PlayerEventInfo>> i : scores.entrySet()) {
                    if (place == 1) {
                        if (firstXRewardWinners) {
                            this.rewardFirstRegisteredFFA(i.getValue());
                        }
                        if (i.getValue().size() > 1) {
                            if (playersCount > i.getValue().size()) {
                                TextBuilder tb = new TextBuilder("*** ");
                                for (PlayerEventInfo player4 : i.getValue()) {
                                    tb.append(LanguageEngine.getMsg("event_ffa_announceWinner2_part1", player4.getPlayersName()) + " ");
                                }
                                String s = tb.toString();
                                tb = new TextBuilder(s.substring(0, s.length() - 4));
                                tb.append(LanguageEngine.getMsg("event_ffa_announceWinner2_part2"));
                                this.announce(instance.getId(), tb.toString());
                            } else {
                                this.announce(instance.getId(), "*** " + LanguageEngine.getMsg("event_ffa_announceWinner3"));
                            }
                        } else {
                            this.announce(instance.getId(), "*** " + LanguageEngine.getMsg("event_ffa_announceWinner1", i.getValue().get(0).getPlayersName()));
                        }
                        for (PlayerEventInfo player3 : i.getValue()) {
                            this.getPlayerData(player3).getGlobalStats().raise(GlobalStats.GlobalStatType.WINS, 1);
                        }
                    } else {
                        for (PlayerEventInfo player3 : i.getValue()) {
                            this.getPlayerData(player3).getGlobalStats().raise(GlobalStats.GlobalStatType.LOSES, 1);
                        }
                    }
                    ++place;
                }
            }
            if (!firstXRewardWinners) {
                this.rewardFirstRegisteredFFA(null);
            }
            this.saveGlobalStats(instanceId);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void rewardAllTeams(int instanceId, int minScore, int minKills) {
        try {
            if (this.getEventType().isFFAEvent()) {
                NexusLoader.debug((String)(this.getEventName() + " cannot use rewardAllTeams since it is an FFA event."));
                return;
            }
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: CALLED REWARD ALL TEAMS  for instance " + instanceId + ", min score " + minScore + ", min kills " + minKills);
            }
            boolean firstXRewardWinners = "WinnersOnly".equals(this.firstRegisteredRewardType);
            for (InstanceData instance : this._instances) {
                if (instance.getId() != instanceId && instanceId != -1) continue;
                List<Integer> list = this._rewardedInstances;
                synchronized (list) {
                    if (this._rewardedInstances.contains(instance.getId())) {
                        continue;
                    }
                    this._rewardedInstances.add(instance.getId());
                }
                int teamsCount = ((FastMap)this._teams.get((Object)instance.getId())).size();
                FastList sorted = new FastList();
                FastMap map = new FastMap();
                for (EventTeam team2 : ((FastMap)this._teams.get((Object)instance.getId())).values()) {
                    sorted.add((Object)team2);
                }
                Collections.sort(sorted, EventManager.getInstance().compareTeamScore);
                for (EventTeam team2 : sorted) {
                    map.put(team2, team2.getScore());
                }
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: before giving reward");
                }
                Map<Integer, List<EventTeam>> scores = EventRewardSystem.getInstance().rewardTeams((Map<EventTeam, Integer>)map, this.getEventType(), 1, minScore, this._afkHalfReward, this._afkNoReward);
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: rewards given");
                }
                int place = 1;
                for (EventTeam team3 : sorted) {
                    this.announce(instance.getId(), LanguageEngine.getMsg("event_announceScore", place, team3.getFullName(), team3.getScore()));
                    team3.setFinalPosition(place);
                    ++place;
                }
                place = 1;
                for (Map.Entry<Integer, List<EventTeam>> i : scores.entrySet()) {
                    if (place == 1) {
                        if (firstXRewardWinners) {
                            this.rewardFirstRegistered(i.getValue());
                        }
                        if (i.getValue().size() > 1) {
                            if (teamsCount > i.getValue().size()) {
                                TextBuilder tb = new TextBuilder("*** ");
                                for (EventTeam team4 : i.getValue()) {
                                    tb.append(LanguageEngine.getMsg("event_team_announceWinner2_part1", team4.getFullName()) + " ");
                                }
                                String s = tb.toString();
                                tb = new TextBuilder(s.substring(0, s.length() - 4));
                                tb.append(LanguageEngine.getMsg("event_team_announceWinner2_part2"));
                                this.announce(instance.getId(), tb.toString());
                            } else {
                                this.announce(instance.getId(), "*** " + LanguageEngine.getMsg("event_team_announceWinner3"));
                            }
                        } else {
                            this.announce(instance.getId(), "*** " + LanguageEngine.getMsg("event_team_announceWinner1", i.getValue().get(0).getFullName()));
                        }
                        for (EventTeam team5 : i.getValue()) {
                            for (PlayerEventInfo player : team5.getPlayers()) {
                                this.getPlayerData(player).getGlobalStats().raise(GlobalStats.GlobalStatType.WINS, 1);
                            }
                        }
                    } else {
                        for (EventTeam team5 : i.getValue()) {
                            for (PlayerEventInfo player : team5.getPlayers()) {
                                this.getPlayerData(player).getGlobalStats().raise(GlobalStats.GlobalStatType.LOSES, 1);
                            }
                        }
                    }
                    ++place;
                }
                this.saveGlobalStats(instance.getId());
            }
            if (!firstXRewardWinners) {
                this.rewardFirstRegistered(null);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void saveGlobalStats(int instance) {
        FastMap stats = new FastMap();
        for (PlayerEventInfo player : this.getPlayers(instance)) {
            this.getPlayerData(player).getGlobalStats().raise(GlobalStats.GlobalStatType.COUNT_PLAYED, 1);
            stats.put(player, this.getPlayerData(player).getGlobalStats());
        }
        EventStatsManager.getInstance().getGlobalStats().updateGlobalStats((Map<PlayerEventInfo, GlobalStatsModel>)stats);
    }

    protected NpcData spawnNPC(int x, int y, int z, int npcId, int instanceId, String name, String title) {
        NpcTemplateData template;
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: spawning npc " + x + ", " + y + ", " + z + ", npc id " + npcId + ", instance " + instanceId + ", name " + name + ", title " + title);
        }
        if (!(template = new NpcTemplateData(npcId)).exists()) {
            return null;
        }
        template.setSpawnName(name);
        template.setSpawnTitle(title);
        try {
            NpcData npc = template.doSpawn(x, y, z, 1, instanceId);
            if (npc != null) {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: npc spawned succesfully.");
                } else if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: npc null after spawning (template exists = " + template.exists() + ").");
                }
            }
            return npc;
        }
        catch (Exception e) {
            e.printStackTrace();
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: error while spawning npc - " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
            }
            return null;
        }
    }

    public void insertConfigs(MainEventInstanceType type) {
        for (Map.Entry e : this._instanceTypeConfigs.entrySet()) {
            type.addDefaultConfig((String)e.getKey(), ((ConfigModel)e.getValue()).getValue(), ((ConfigModel)e.getValue()).getDesc(), ((ConfigModel)e.getValue()).getDefaultVal(), ((ConfigModel)e.getValue()).getInput(), ((ConfigModel)e.getValue()).getInputParams());
        }
    }

    public void addInstanceType(MainEventInstanceType type) {
        this._types.put((Object)type.getId(), (Object)type);
    }

    public void removeInstanceType(MainEventInstanceType type) {
        this._types.remove((Object)type.getId());
    }

    public MainEventInstanceType getInstanceType(int id) {
        return (MainEventInstanceType)this._types.get((Object)id);
    }

    public FastMap<Integer, MainEventInstanceType> getInstanceTypes() {
        return this._types;
    }

    public InstanceData[] getInstances() {
        return this._instances;
    }

    public int getTeamsCountInInstance(int instance) {
        return ((FastMap)this._teams.get((Object)instance)).size();
    }

    protected void tryFirstBlood(PlayerEventInfo killer) {
        Object object = this.firstBloodLock;
        synchronized (object) {
            if (!this._firstBlood) {
                for (RewardPosition pos : this.getRewardTypes()) {
                    if (pos != RewardPosition.FirstBlood) continue;
                    this._firstBloodPlayer = killer;
                    if (this.getBoolean("firstBloodMessage")) {
                        this.screenAnnounce(killer.getInstanceId(), LanguageEngine.getMsg("event_firstBlood", killer.getPlayersName()));
                    }
                    EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, killer, RewardPosition.FirstBlood, null, 0, 0, 0);
                    if (!NexusLoader.detailedDebug) break;
                    this.print("AbstractMainEvent: FIRST BLOOD reward given to " + killer.getPlayersName());
                    break;
                }
                this._firstBlood = true;
            }
        }
    }

    protected void giveOnKillReward(PlayerEventInfo killer) {
        for (RewardPosition pos : this.getRewardTypes()) {
            if (pos != RewardPosition.OnKill) continue;
            EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, killer, RewardPosition.OnKill, null, 0, 0, 0);
            break;
        }
    }

    protected void giveKillingSpreeReward(EventPlayerData killerData) {
        if (killerData instanceof PvPEventPlayerData) {
            int spree = ((PvPEventPlayerData)killerData).getSpree();
            if (EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, killerData.getOwner(), RewardPosition.KillingSpree, String.valueOf(spree), 0, 0, 0)) {
                killerData.getOwner().sendMessage("You have been awarded for your " + spree + " kills in row!");
            }
        }
    }

    public String getScorebarCb(int instance) {
        int teamsCount = this.getTeamsCountInInstance(instance);
        TextBuilder tb = new TextBuilder();
        if (teamsCount > 1) {
            tb.append("<table width=510 bgcolor=3E3E3E><tr><td width=510 align=center><font color=ac9887>Score:</font> ");
            int i = 0;
            for (EventTeam team : ((FastMap)this._teams.get((Object)instance)).values()) {
                ++i;
                if (teamsCount > 3) {
                    if (i != teamsCount) {
                        tb.append("<font color=" + EventManager.getInstance().getTeamColorForHtml(team.getTeamId()) + ">" + team.getTeamName() + "</font><font color=9f9f9f> - " + team.getScore() + "  |  </font>");
                        continue;
                    }
                    tb.append("<font color=" + EventManager.getInstance().getTeamColorForHtml(team.getTeamId()) + ">" + team.getTeamName() + "</font><font color=9f9f9f> - " + team.getScore() + "</font>");
                    continue;
                }
                if (i != teamsCount) {
                    tb.append("<font color=" + EventManager.getInstance().getTeamColorForHtml(team.getTeamId()) + ">" + team.getFullName() + "</font><font color=9f9f9f> - " + team.getScore() + "  |  </font>");
                    continue;
                }
                tb.append("<font color=" + EventManager.getInstance().getTeamColorForHtml(team.getTeamId()) + ">" + team.getFullName() + "</font><font color=9f9f9f> - " + team.getScore() + "</font>");
            }
            tb.append("</td></tr></table>");
        }
        return tb.toString();
    }

    public String getEventInfoCb(int instance, Object param) {
        TextBuilder tb = new TextBuilder();
        try {
            int teamsCount = this.getTeamsCountInInstance(instance);
            FastList teams = new FastList();
            teams.addAll(((FastMap)this._teams.get((Object)instance)).values());
            Collections.sort(teams, EventManager.getInstance().compareTeamScore);
            if (teamsCount == 2) {
                tb.append(this.addExtraEventInfoCb(instance));
                tb.append("<br><img src=\"L2UI.SquareBlank\" width=510 height=3>");
                tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
                tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
                tb.append("<table width=510 bgcolor=2E2E2E>");
                boolean firstTeam = true;
                for (EventTeam team : teams) {
                    if (firstTeam) {
                        tb.append("<tr><td width=250 align=center><font color=" + EventManager.getInstance().getTeamColorForHtml(team.getTeamId()) + ">1. " + team.getFullName() + "</font> <font color=6f6f6f>(" + team.getPlayers().size() + " players; " + team.getAverageLevel() + " avg lvl)</font></td>");
                    } else {
                        tb.append("<td width=10></td><td width=250 align=center><font color=" + EventManager.getInstance().getTeamColorForHtml(team.getTeamId()) + ">2. " + team.getFullName() + "</font> <font color=6f6f6f>(" + team.getPlayers().size() + " players; " + team.getAverageLevel() + " avg lvl)</font></td></tr>");
                    }
                    firstTeam = false;
                }
                tb.append("<tr></tr>");
                int countTopScorers = this._countOfShownTopPlayers;
                FastMap topPlayers = new FastMap();
                FastList temp = new FastList();
                int counter = 0;
                for (EventTeam team2 : teams) {
                    topPlayers.put(team2.getTeamId(), new FastList());
                    temp.addAll(team2.getPlayers());
                    Collections.sort(temp, EventManager.getInstance().comparePlayersScore);
                    if (temp.size() < countTopScorers) {
                        countTopScorers = temp.size();
                    }
                    for (PlayerEventInfo player : temp) {
                        ((List)topPlayers.get(team2.getTeamId())).add(player);
                        if (++counter < countTopScorers) continue;
                        break;
                    }
                    temp.clear();
                    counter = 0;
                }
                firstTeam = true;
                int i = 0;
                while (i < countTopScorers) {
                    PlayerEventInfo tempPlayer;
                    if (firstTeam) {
                        tempPlayer = (PlayerEventInfo)((List)topPlayers.get(1)).get(i);
                        tb.append("<tr><td width=250 align=center><font color=9f9f9f>" + (i + 1) + ". " + tempPlayer.getPlayersName() + "</font><font color=" + EventManager.getInstance().getDarkColorForHtml(1) + "> - " + tempPlayer.getScore() + " score</font></td>");
                    } else {
                        tempPlayer = (PlayerEventInfo)((List)topPlayers.get(2)).get(i);
                        tb.append("<td width=10></td><td width=250 align=center><font color=9f9f9f>" + (i + 1) + ". " + tempPlayer.getPlayersName() + "</font><font color=" + EventManager.getInstance().getDarkColorForHtml(2) + "> - " + tempPlayer.getScore() + " score</font></td></tr>");
                    }
                    boolean bl = !firstTeam;
                    firstTeam = bl;
                    if (!firstTeam) continue;
                    ++i;
                }
                tb.append("</table>");
                tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
                tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
            } else if (teamsCount == 1) {
                tb.append(this.addExtraEventInfoCb(instance));
                tb.append("<br><img src=\"L2UI.SquareBlank\" width=510 height=3>");
                tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
                tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
                tb.append("<table width=510 bgcolor=2E2E2E>");
                FastList tempPlayers = new FastList();
                tempPlayers.addAll(this.getPlayers(instance));
                Collections.sort(tempPlayers, EventManager.getInstance().comparePlayersScore);
                int countTopPlayers = this._countOfShownTopPlayers;
                int i = 0;
                for (PlayerEventInfo player : tempPlayers) {
                    String kd = String.valueOf(player.getDeaths() == 0 ? (double)player.getKills() : (double)player.getKills() / (double)player.getDeaths());
                    kd = kd.substring(0, Math.min(3, kd.length()));
                    tb.append("<tr><td width=510 align=center><font color=9f9f9f>" + (i + 1) + ".</font> <font color=ac9887>" + player.getPlayersName() + "</font><font color=7f7f7f> - " + player.getScore() + " points</font>  <font color=5f5f5f>(K:D ratio: " + kd + ")</font></td>");
                    tb.append("</tr>");
                    if (++i < countTopPlayers) continue;
                    break;
                }
                tb.append("</table>");
                tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
                tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
            } else if (teamsCount > 2) {
                int page = param != null && param instanceof Integer ? (Integer)param : 1;
                int maxPages = (int)Math.ceil(teamsCount - 1);
                int countTopScorers = this._countOfShownTopPlayers;
                int shownTeam1Id = 1;
                int shownTeam2Id = 2;
                if (page > 1) {
                    shownTeam1Id+=page - 1;
                    shownTeam2Id+=page - 1;
                }
                tb.append(this.addExtraEventInfoCb(instance));
                tb.append("<br><img src=\"L2UI.SquareBlank\" width=510 height=3>");
                tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
                tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
                tb.append("<table width=510 bgcolor=2E2E2E>");
                boolean firstTeam = true;
                for (EventTeam team : teams) {
                    if (team.getTeamId() != shownTeam1Id && team.getTeamId() != shownTeam2Id) continue;
                    if (firstTeam) {
                        tb.append("<tr><td width=250 align=center><font color=" + EventManager.getInstance().getTeamColorForHtml(team.getTeamId()) + ">" + shownTeam1Id + ". " + team.getFullName() + "</font> <font color=6f6f6f>(" + team.getPlayers().size() + " players; " + team.getAverageLevel() + " avg lvl)</font></td>");
                    } else {
                        tb.append("<td width=10></td><td width=250 align=center><font color=" + EventManager.getInstance().getTeamColorForHtml(team.getTeamId()) + ">" + shownTeam2Id + ". " + team.getFullName() + "</font> <font color=6f6f6f>(" + team.getPlayers().size() + " players; " + team.getAverageLevel() + " avg lvl)</font></td></tr>");
                    }
                    firstTeam = false;
                }
                tb.append("<tr></tr>");
                FastMap topPlayers = new FastMap();
                FastList temp = new FastList();
                int counter = 0;
                for (EventTeam team3 : teams) {
                    if (team3.getTeamId() != shownTeam1Id && team3.getTeamId() != shownTeam2Id) continue;
                    topPlayers.put(team3.getTeamId(), new FastList());
                    temp.addAll(team3.getPlayers());
                    Collections.sort(temp, EventManager.getInstance().comparePlayersScore);
                    if (temp.size() < countTopScorers) {
                        countTopScorers = temp.size();
                    }
                    for (PlayerEventInfo player : temp) {
                        ((List)topPlayers.get(team3.getTeamId())).add(player);
                        if (++counter < countTopScorers) continue;
                        break;
                    }
                    temp.clear();
                    counter = 0;
                }
                firstTeam = true;
                int i = 0;
                while (i < countTopScorers) {
                    PlayerEventInfo tempPlayer;
                    if (firstTeam) {
                        tempPlayer = (PlayerEventInfo)((List)topPlayers.get(shownTeam1Id)).get(i);
                        tb.append("<tr><td width=250 align=center><font color=9f9f9f>" + (i + 1) + ". " + tempPlayer.getPlayersName() + "</font><font color=" + EventManager.getInstance().getDarkColorForHtml(shownTeam1Id) + "> - " + tempPlayer.getScore() + " score</font></td>");
                    } else {
                        tempPlayer = (PlayerEventInfo)((List)topPlayers.get(shownTeam2Id)).get(i);
                        tb.append("<td width=10></td><td width=250 align=center><font color=9f9f9f>" + (i + 1) + ". " + tempPlayer.getPlayersName() + "</font><font color=" + EventManager.getInstance().getDarkColorForHtml(shownTeam2Id) + "> - " + tempPlayer.getScore() + " score</font></td></tr>");
                    }
                    boolean bl = !firstTeam;
                    firstTeam = bl;
                    if (!firstTeam) continue;
                    ++i;
                }
                tb.append("</table>");
                tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
                tb.append("<img src=\"L2UI.SquareGray\" width=512 height=2>");
                tb.append("<img src=\"L2UI.SquareBlank\" width=510 height=3>");
                boolean previousButton = false;
                boolean nextButton = false;
                if (page > 1) {
                    previousButton = true;
                }
                if (page < maxPages) {
                    nextButton = true;
                }
                if (nextButton && previousButton) {
                    tb.append("<table width=510 bgcolor=2E2E2E><tr><td width=200 align=left><button value=\"Prev page\" action=\"bypass -h " + EventHtmlManager.BBS_COMMAND + " nextpageteam " + (page - 1) + " " + instance + "\" width=85 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>" + "<td width=200 align=right><button value=\"Next page\" action=\"bypass -h " + EventHtmlManager.BBS_COMMAND + " nextpageteam " + (page + 1) + " " + instance + "\" width=85 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
                } else if (nextButton) {
                    tb.append("<table width=510 bgcolor=2E2E2E><tr><td width=510 align=right><button value=\"Next page\" action=\"bypass -h " + EventHtmlManager.BBS_COMMAND + " nextpageteam " + (page + 1) + " " + instance + "\" width=85 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
                } else if (previousButton) {
                    tb.append("<table width=510 bgcolor=2E2E2E><tr><td width=510 align=left><button value=\"Prev page\" action=\"bypass -h " + EventHtmlManager.BBS_COMMAND + " nextpageteam " + (page - 1) + " " + instance + "\" width=85 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
                }
                tb.append("</table>");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return tb.toString();
    }

    protected String addExtraEventInfoCb(int instance) {
        boolean firstBloodEnabled = false;
        for (RewardPosition pos : this.getRewardTypes()) {
            if (pos != RewardPosition.FirstBlood) continue;
            firstBloodEnabled = true;
            break;
        }
        if (firstBloodEnabled) {
            return "<table width=510 bgcolor=3E3E3E><tr><td width=510 align=center><font color=CE7171>First blood:</font><font color=7f7f7f> " + (this._firstBloodPlayer != null ? this._firstBloodPlayer.getPlayersName() : "None yet") + "</font></td></tr></table>";
        }
        return "";
    }

    @Override
    public String getDescriptionForReward(RewardPosition reward) {
        if (reward == RewardPosition.FirstRegistered) {
            String type = this.getString("firstRegisteredRewardType");
            if (type.equals("All")) {
                return "The reward for the " + this.getInt("firstRegisteredRewardCount") + " first registered players, given in the end of the event. <br1>Check out event configs for more customization.";
            }
            if (type.equals("WinnersOnly")) {
                return "The reward for the " + this.getInt("firstRegisteredRewardCount") + " first registered players, given in the end of the event only if the players won the event. <br1>Check out event configs for more customization.";
            }
        }
        return null;
    }

    protected boolean canJoinInstance(PlayerEventInfo player, MainEventInstanceType instance) {
        int minLvl = instance.getConfigInt("minLvl");
        int maxLvl = instance.getConfigInt("maxLvl");
        if (maxLvl != -1 && player.getLevel() > maxLvl || player.getLevel() < minLvl) {
            return false;
        }
        int minPvps = instance.getConfigInt("minPvps");
        int maxPvps = instance.getConfigInt("maxPvps");
        if (player.getPvpKills() < minPvps || maxPvps != -1 && player.getPvpKills() > maxPvps) {
            return false;
        }
        player.sendMessage(LanguageEngine.getMsg("event_choosingInstance", instance.getName()));
        return true;
    }

    @Override
    public void playerWentAfk(PlayerEventInfo player, boolean warningOnly, int afkTime) {
        if (warningOnly) {
            player.sendMessage(LanguageEngine.getMsg("event_afkWarning", PlayerEventInfo.AFK_WARNING_DELAY / 1000, PlayerEventInfo.AFK_KICK_DELAY / 1000));
        } else if (afkTime == 0) {
            player.sendMessage(LanguageEngine.getMsg("event_afkMarked"));
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: player " + player.getPlayersName() + " has just gone afk");
            }
        } else if (afkTime % 60 == 0) {
            player.sendMessage(LanguageEngine.getMsg("event_afkDurationInfo", afkTime / 60));
        }
        if (player.isTitleUpdated()) {
            player.setTitle(this.getTitle(player), true);
            player.broadcastTitleInfo();
        }
    }

    @Override
    public void playerReturnedFromAfk(PlayerEventInfo player) {
        if (player.isTitleUpdated()) {
            player.setTitle(this.getTitle(player), true);
            player.broadcastTitleInfo();
        }
    }

    @Override
    public boolean addDisconnectedPlayer(PlayerEventInfo player, EventManager.DisconnectedPlayerData data) {
        boolean added = false;
        if (data != null) {
            if (this._rejoinEventAfterDisconnect && this._manager.getState() == MainEventManager.State.RUNNING) {
                EventTeam team;
                AbstractEventInstance instance = this.getMatch(data.getInstance());
                if (instance != null && instance.isActive() && (team = data.getTeam()) != null) {
                    player.sendMessage(LanguageEngine.getMsg("registering_afterDisconnect_true"));
                    player.setIsRegisteredToMainEvent(true, this.getEventType());
                    List<PlayerEventInfo> list = this._manager.getPlayers();
                    synchronized (list) {
                        this._manager.getPlayers().add(player);
                    }
                    player.onEventStart((EventGame)this);
                    ((EventTeam)((FastMap)this._teams.get((Object)instance.getInstance().getId())).get((Object)team.getTeamId())).addPlayer(player, true);
                    this.prepareDisconnectedPlayer(player);
                    this.respawnPlayer(player, instance.getInstance().getId());
                    if (this._removeWarningAfterReconnect) {
                        EventWarnings.getInstance().removeWarning(player, 1);
                    }
                }
            } else {
                player.sendMessage(LanguageEngine.getMsg("registering_afterDisconnect_false"));
            }
        }
        return added;
    }

    protected void prepareDisconnectedPlayer(PlayerEventInfo player) {
        PartyData pt;
        boolean removeBuffs = this.getBoolean("removeBuffsOnStart");
        if (removeBuffs) {
            player.removeBuffs();
        }
        if (this._allowSchemeBuffer) {
            EventBuffer.getInstance().buffPlayer(player, true);
        }
        if (this._removePartiesOnStart && (pt = player.getParty()) != null) {
            player.getParty().removePartyMember(player);
        }
        if (player.isTitleUpdated()) {
            player.setTitle(this.getTitle(player), true);
        }
    }

    @Override
    public void onDisconnect(PlayerEventInfo player) {
        if (player.isOnline()) {
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: player " + player.getPlayersName() + " (instance id = " + player.getInstanceId() + ") disconnecting from the event");
            }
            if (this._spectators != null && this._spectators.contains((Object)player)) {
                List<PlayerEventInfo> list = this._spectators;
                synchronized (list) {
                    this._spectators.remove((Object)player);
                }
                player.setInstanceId(0);
                player.setXYZInvisible(player.getOrigLoc().getX(), player.getOrigLoc().getY(), player.getOrigLoc().getZ());
            }
            EventTeam team = player.getEventTeam();
            EventPlayerData playerData = player.getEventData();
            player.restoreData();
            player.setXYZInvisible(player.getOrigLoc().getX(), player.getOrigLoc().getY(), player.getOrigLoc().getZ());
            EventWarnings.getInstance().addPoints(player.getPlayersId(), 1);
            boolean running = false;
            boolean allowRejoin = true;
            AbstractEventInstance playersMatch = this.getMatch(player.getInstanceId());
            if (playersMatch == null) {
                NexusLoader.debug((String)"Player's EventInstance is null, called onDisconnect", (Level)Level.WARNING);
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: !!! -.- player's EVENT INSTANCE is null after calling onDisconnect. Player's instanceId is = " + player.getInstanceId());
                }
                running = false;
            } else {
                running = playersMatch.isActive();
            }
            team.removePlayer(player);
            this._manager.getPlayers().remove((Object)player);
            CallBack.getInstance().getPlayerBase().playerDisconnected(player);
            if (running) {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: -.- event is active");
                }
                this.debug(this.getEventName() + ": Player " + player.getPlayersName() + " disconnected from " + this.getEventName() + " event.");
                if (team.getPlayers().isEmpty()) {
                    this.announce(player.getInstanceId(), LanguageEngine.getMsg("event_disconnect_team", team.getTeamName()));
                    allowRejoin = false;
                    this.debug(this.getEventName() + ": all players from team " + team.getTeamName() + " have disconnected.");
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: ALL PLAYERS FROM TEAM " + team.getTeamName() + " disconnected");
                    }
                }
                if (!this.checkIfEventCanContinue(player.getInstanceId(), player)) {
                    this.announce(player.getInstanceId(), LanguageEngine.getMsg("event_disconnect_all"));
                    this.endInstance(player.getInstanceId(), true, false, false);
                    allowRejoin = false;
                    this.debug(this.getEventName() + ": no players left in the teams, the fight can't continue. The event has been aborted!");
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: NO PLAYERS LEFT IN THE TEAMS, THE FIGHT CAN'T CONTINUE! (checkIfEventCanContinue = false)");
                    }
                    return;
                }
                if (allowRejoin && this.allowsRejoinOnDisconnect()) {
                    EventManager.getInstance().addDisconnectedPlayer(player, team, playerData, this);
                }
            } else if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: -.- event IS NOT active anymore");
            }
        }
    }

    protected boolean checkIfEventCanContinue(int instanceId, PlayerEventInfo disconnectedPlayer) {
        int teamsOn = 0;
        block0 : for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                if (pi == null || !pi.isOnline()) continue;
                ++teamsOn;
                continue block0;
            }
        }
        return teamsOn >= 2;
    }

    @Override
    public boolean canUseItem(PlayerEventInfo player, ItemData item) {
        if (this.notAllovedItems != null && Arrays.binarySearch(this.notAllovedItems, item.getItemId()) >= 0) {
            player.sendMessage(LanguageEngine.getMsg("event_itemNotAllowed"));
            return false;
        }
        if (item.isPotion() && !this.getBoolean("allowPotions")) {
            return false;
        }
        if (item.isScroll()) {
            return false;
        }
        if (item.isPetCollar() && !this._allowPets) {
            player.sendMessage(LanguageEngine.getMsg("event_petsNotAllowed"));
            return false;
        }
        return true;
    }

    @Override
    public boolean canDestroyItem(PlayerEventInfo player, ItemData item) {
        return true;
    }

    @Override
    public void onItemUse(PlayerEventInfo player, ItemData item) {
    }

    @Override
    public boolean canUseSkill(PlayerEventInfo player, SkillData skill) {
        if (this.notAllovedSkillls != null && Arrays.binarySearch(this.notAllovedSkillls, skill.getId()) >= 0) {
            player.sendMessage(LanguageEngine.getMsg("event_skillNotAllowed"));
            return false;
        }
        if (skill.getSkillType().equals("RESURRECT")) {
            return false;
        }
        if (skill.getSkillType().equals("RECALL")) {
            return false;
        }
        if (skill.getSkillType().equals("SUMMON_FRIEND")) {
            return false;
        }
        if (skill.getSkillType().equals("FAKE_DEATH")) {
            return false;
        }
        if (!this._allowSummons && skill.getSkillType().equals("SUMMON")) {
            player.sendMessage(LanguageEngine.getMsg("event_summonsNotAllowed"));
            return false;
        }
        return true;
    }

    @Override
    public void onSkillUse(final PlayerEventInfo player, SkillData skill) {
        if (skill.getSkillType() != null && skill.getSkillType().equals("SUMMON")) {
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    EventBuffer.getInstance().buffPet(player);
                }
            }, 2000);
        }
    }

    @Override
    public boolean canSupport(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null || target.getEventInfo().getEvent() != player.getEvent()) {
            return false;
        }
        if (target.getEventInfo().getTeamId() == player.getTeamId()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canAttack(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null) {
            return true;
        }
        if (target.getEventInfo().getEvent() != player.getEvent()) {
            return false;
        }
        if (target.getEventInfo().getTeamId() != player.getTeamId()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onAttack(CharacterData cha, CharacterData target) {
        return true;
    }

    @Override
    public boolean onSay(PlayerEventInfo player, String text, int channel) {
        if (text.equals(".scheme")) {
            EventManager.getInstance().getHtmlManager().showSelectSchemeForEventWindow(player, "none", this.getEventType().getAltTitle());
            return false;
        }
        return true;
    }

    @Override
    public boolean onNpcAction(PlayerEventInfo player, NpcData npc) {
        return false;
    }

    @Override
    public void onDamageGive(CharacterData cha, CharacterData target, int damage, boolean isDOT) {
    }

    @Override
    public void onKill(PlayerEventInfo player, CharacterData target) {
    }

    @Override
    public void onDie(PlayerEventInfo player, CharacterData killer) {
    }

    @Override
    public boolean canInviteToParty(PlayerEventInfo player, PlayerEventInfo target) {
        if (target.getEvent() != player.getEvent()) {
            return false;
        }
        if (!(player.canInviteToParty() && target.canInviteToParty())) {
            return false;
        }
        if (target.getTeamId() == player.getTeamId()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canTransform(PlayerEventInfo player) {
        return true;
    }

    @Override
    public boolean canBeDisarmed(PlayerEventInfo player) {
        return true;
    }

    @Override
    public int allowTransformationSkill(PlayerEventInfo playerEventInfo, SkillData skillData) {
        return 0;
    }

    @Override
    public boolean canSaveShortcuts(PlayerEventInfo player) {
        return true;
    }

    public boolean isInEvent(CharacterData ch) {
        return false;
    }

    public boolean allowKill(CharacterData target, CharacterData killer) {
        return true;
    }

    public boolean allowsRejoinOnDisconnect() {
        return true;
    }

    protected void clockTick() throws Exception {
    }

    public class Clock
    implements Runnable {
        private AbstractEventInstance _event;
        private int time;
        private boolean _announcesCountdown;
        private ScheduledFuture<?> _task;

        public Clock(AbstractEventInstance instance) {
            this._announcesCountdown = true;
            this._task = null;
            this._event = instance;
        }

        public String getTime() {
            String mins = "" + this.time / 60;
            String secs = this.time % 60 < 10 ? "0" + this.time % 60 : "" + this.time % 60;
            return "" + mins + ":" + secs + "";
        }

        public void disableAnnouncingCountdown() {
            this._announcesCountdown = false;
        }

        @Override
        public void run() {
            try {
                AbstractMainEvent.this.clockTick();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            if (AbstractMainEvent.this._allowScoreBar && AbstractMainEvent.this._instances != null) {
                for (InstanceData instance : AbstractMainEvent.this._instances) {
                    try {
                        AbstractMainEvent.this.scorebarText = AbstractMainEvent.this.getScorebar(instance.getId());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        if (NexusLoader.detailedDebug) {
                            AbstractMainEvent.this.print("ERROR on CLOCK.getScorebar: " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
                        }
                        if (NexusLoader.detailedDebug) {
                            AbstractMainEvent.this.print("Event aborted");
                        }
                        for (InstanceData ins : AbstractMainEvent.this._instances) {
                            AbstractMainEvent.this.announce(ins.getId(), LanguageEngine.getMsg("event_mysteriousError"));
                        }
                        AbstractMainEvent.this.clearEvent();
                        return;
                    }
                    if (AbstractMainEvent.this.scorebarText == null) continue;
                    for (PlayerEventInfo player : AbstractMainEvent.this.getPlayers(instance.getId())) {
                        player.sendEventScoreBar(AbstractMainEvent.this.scorebarText);
                    }
                    if (AbstractMainEvent.this._spectators == null) continue;
                    for (PlayerEventInfo spec : AbstractMainEvent.this._spectators) {
                        if (spec.getInstanceId() != instance.getId()) continue;
                        spec.sendEventScoreBar(AbstractMainEvent.this.scorebarText);
                    }
                }
            }
            if (this._announcesCountdown) {
                switch (this.time) {
                    case 60: 
                    case 300: 
                    case 600: 
                    case 1200: 
                    case 1800: {
                        AbstractMainEvent.this.announce(this._event.getInstance().getId(), LanguageEngine.getMsg("event_countdown_min", this.time / 60));
                        break;
                    }
                    case 1: 
                    case 2: 
                    case 3: 
                    case 4: 
                    case 5: 
                    case 10: 
                    case 30: {
                        AbstractMainEvent.this.announce(this._event.getInstance().getId(), LanguageEngine.getMsg("event_countdown_sec", this.time));
                    }
                }
            }
            if (this.time <= 0) {
                if (NexusLoader.detailedDebug) {
                    AbstractMainEvent.this.print("AbstractMainEvent: Clock.time is " + this.time + ", scheduling next event task");
                }
                this._task = this._event.scheduleNextTask(0);
            } else {
                this.setTime(this.time - 1, false);
                this._task = CallBack.getInstance().getOut().scheduleGeneral(this, 1000);
            }
        }

        public void abort() {
            if (this._task != null) {
                this._task.cancel(false);
            }
        }

        public synchronized void setTime(int t, boolean debug) {
            if (debug && NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: setting value of Clock.time to " + t);
            }
            this.time = t;
        }

        public void startClock(int mt) {
            if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: starting Clock and setting Clock.time to " + mt);
            }
            this.time = mt;
            CallBack.getInstance().getOut().scheduleGeneral(this, 1);
        }
    }

    private class ReviveTask
    implements Runnable {
        private PlayerEventInfo player;
        private int instance;
        final /* synthetic */ AbstractMainEvent this$0;

        private ReviveTask(AbstractMainEvent abstractMainEvent, PlayerEventInfo p, int time) {
            this.this$0 = abstractMainEvent;
            this.player = p;
            this.instance = this.player.getInstanceId();
            CallBack.getInstance().getOut().scheduleGeneral(this, time);
            this.player.sendMessage(LanguageEngine.getMsg("event_revive", time / 1000));
        }

        private ReviveTask(AbstractMainEvent abstractMainEvent, PlayerEventInfo p) {
            this.this$0 = abstractMainEvent;
            this.player = p;
            this.instance = this.player.getInstanceId();
            CallBack.getInstance().getOut().executeTask(this);
        }

        private ReviveTask(AbstractMainEvent abstractMainEvent, PlayerEventInfo p, int time, int instance) {
            this.this$0 = abstractMainEvent;
            this.player = p;
            this.instance = instance;
            CallBack.getInstance().getOut().scheduleGeneral(this, time);
            this.player.sendMessage(LanguageEngine.getMsg("event_revive", time / 1000));
        }

        @Override
        public void run() {
            if (this.player.getActiveEvent() != null && this.player.isDead()) {
                this.player.doRevive();
                if (this.this$0._allowSchemeBuffer) {
                    EventBuffer.getInstance().buffPlayer(this.player);
                    EventBuffer.getInstance().buffPet(this.player);
                }
                this.player.setCurrentCp(this.player.getMaxCp());
                this.player.setCurrentHp(this.player.getMaxHp());
                this.player.setCurrentMp(this.player.getMaxMp());
                this.player.setTitle(this.this$0.getTitle(this.player), true);
                this.this$0.respawnPlayer(this.player, this.instance);
                if (this.this$0.getBoolean("removeBuffsOnRespawn")) {
                    this.player.removeBuffs();
                }
            }
        }
    }

    protected class WaweRespawnScheduler
    implements Runnable {
        private ScheduledFuture<?> _future;
        private int _delay;
        private FastList<PlayerEventInfo> _players;

        public WaweRespawnScheduler(int delay) {
            this._delay = delay;
            this._future = CallBack.getInstance().getOut().scheduleGeneral(this, delay);
            this._players = new FastList();
        }

        public void addPlayer(PlayerEventInfo player) {
            FastList<PlayerEventInfo> fastList = this._players;
            synchronized (fastList) {
                this._players.add((Object)player);
            }
            player.screenMessage(LanguageEngine.getMsg("event_revive", Math.max(1, this._future.getDelay(TimeUnit.SECONDS))), AbstractMainEvent.this.getEventType().getAltTitle(), true);
            player.sendMessage(LanguageEngine.getMsg("event_revive", Math.max(1, this._future.getDelay(TimeUnit.SECONDS))));
        }

        public void stop() {
            if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: stopping wawe spawn scheduler");
            }
            this._players.clear();
            if (this._future != null) {
                this._future.cancel(false);
            }
            this._future = null;
        }

        @Override
        public void run() {
            if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: running wawe spawn scheduler...");
            }
            int count = 0;
            FastList<PlayerEventInfo> fastList = this._players;
            synchronized (fastList) {
                for (PlayerEventInfo pi : this._players) {
                    if (pi == null || !pi.isDead()) continue;
                    ++count;
                    new ReviveTask(AbstractMainEvent.this, pi);
                }
                this._players.clear();
            }
            if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: ...wawe scheduler respawned " + count + " players");
            }
            this._future = CallBack.getInstance().getOut().scheduleGeneral(this, this._delay);
        }
    }

    private class AddToParty
    implements Runnable {
        private PartyData party;
        private PlayerEventInfo player;

        public AddToParty(PartyData party, PlayerEventInfo player) {
            this.party = party;
            this.player = player;
        }

        @Override
        public void run() {
            try {
                if (this.party.exists()) {
                    if (this.player.getParty() != null) {
                        this.player.getParty().removePartyMember(this.player);
                    }
                    if (this.party.getMemberCount() >= AbstractMainEvent.this.getInt("maxPartySize")) {
                        return;
                    }
                    this.party.addPartyMember(this.player);
                }
                this.player.setCanInviteToParty(true);
            }
            catch (NullPointerException e) {
                e.printStackTrace();
                if (NexusLoader.detailedDebug) {
                    AbstractMainEvent.this.print("AbstractMainEvent: error while adding players to the party: " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
                }
                AbstractMainEvent.this.debug("error while adding players to the party: " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
            }
        }
    }

    protected abstract class AbstractEventData {
        protected int _instanceId;

        protected AbstractEventData(int instance) {
            this._instanceId = instance;
            if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: abstracteventdata created data for instanceId = " + instance);
            }
        }
    }

    protected abstract class AbstractEventInstance
    implements Runnable {
        protected InstanceData _instance;
        protected Clock _clock;
        protected boolean _canBeAborted;
        protected boolean _canRewardIfAborted;
        protected boolean _forceNotRewardThisInstance;
        protected ScheduledFuture<?> _task;

        public AbstractEventInstance(InstanceData instance) {
            this._canBeAborted = false;
            this._canRewardIfAborted = false;
            this._forceNotRewardThisInstance = false;
            this._task = null;
            this._instance = instance;
            this._clock = new Clock(this);
            if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: created abstracteventinstance for instanceId " + instance.getId());
            }
        }

        public abstract boolean isActive();

        public void setCanBeAborted() {
            this._canBeAborted = true;
        }

        public void forceNotRewardThisInstance() {
            this._forceNotRewardThisInstance = true;
            List<Integer> list = AbstractMainEvent.this._rewardedInstances;
            synchronized (list) {
                AbstractMainEvent.this._rewardedInstances.add(this._instance.getId());
            }
        }

        public void setCanRewardIfAborted() {
            this._canRewardIfAborted = true;
        }

        public InstanceData getInstance() {
            return this._instance;
        }

        public Clock getClock() {
            return this._clock;
        }

        public ScheduledFuture<?> scheduleNextTask(int time) {
            if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: abstractmaininstance: scheduling next task in " + time);
            }
            if (this._clock._task != null) {
                if (NexusLoader.detailedDebug) {
                    AbstractMainEvent.this.print("AbstractMainEvent: abstractmaininstane: _clock_task is not null");
                }
                this._clock._task.cancel(false);
                this._clock._task = null;
            } else if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: abstractmaininstance: _clock_task is NULL!");
            }
            if (time > 0) {
                this._task = CallBack.getInstance().getOut().scheduleGeneral(this, time);
            } else {
                CallBack.getInstance().getOut().executeTask(this);
            }
            if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: next task scheduled.");
            }
            return this._task;
        }

        public void abort() {
            if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: abstractmaininstance: aborting...");
            }
            if (this._task != null) {
                this._task.cancel(false);
                if (NexusLoader.detailedDebug) {
                    AbstractMainEvent.this.print("AbstractMainEvent: abstractmaininsance _task is not null");
                }
            } else if (NexusLoader.detailedDebug) {
                AbstractMainEvent.this.print("AbstractMainEvent: abstractmaininstance _task is NULL!");
            }
            this._clock.abort();
        }
    }

}

