/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.PlayerEventInfo$Radar
 *  cz.nxs.interf.callback.CallbackManager
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.InstanceData
 *  cz.nxs.interf.delegate.NpcData
 *  cz.nxs.interf.delegate.PartyData
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.PvPEventPlayerData;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.base.description.EventDescription;
import cz.nxs.events.engine.base.description.EventDescriptionSystem;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.base.MainEventInstanceType;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.IValues;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class Battlefield
extends AbstractMainEvent {
    protected FastMap<Integer, BattlefieldEventInstance> _matches;
    protected boolean _waweRespawn;
    protected int _teamsCount;
    protected int _towerNpcId;
    protected int _towerRadius;
    protected int _towerCheckInterval;
    protected int _scoreForCapturingTower;
    private int _timeToHoldTowerToCapture;
    private int _holdAllTowersFor;
    protected int _percentMajorityToCapture;
    protected String _scoreType;
    protected int _minPlayersToCaptureTheBase;
    protected boolean isMinPlayersToCaptureTheBaseInPercent;
    protected int _minTowersToOwnToScore;
    protected int tick;
    protected int countOfTowers;

    public Battlefield(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Winner, RewardPosition.Looser, RewardPosition.Tie, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill, RewardPosition.KillingSpree});
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.addConfig(new ConfigModel("scoreForReward", "0", "The minimum score required to get a reward (includes all possible rewards). Score in this event is gained by capturing bases."));
        this.addConfig(new ConfigModel("killsForReward", "0", "The minimum kills count required to get a reward (includes all possible rewards)."));
        this.addConfig(new ConfigModel("resDelay", "15", "The delay after which the player is resurrected. In seconds."));
        this.addConfig(new ConfigModel("waweRespawn", "true", "Enables the wawe-style respawn system."));
        this.addConfig(new ConfigModel("countOfBases", "2", "Specifies how many bases will be in the event. In order to score, one team must capture more bases than the other team(s). If you have 2 or 4 teams set in this event, you should only use odd numbers for the count of towers, such as 3, 5, 7 or 9. Don't forget to create a same count of Base spawns in the map you are running this event in. ", ConfigModel.InputType.Enum).addEnumOptions(new String[]{"2", "3", "4", "5", "6", "7", "8", "9", "10"}));
        this.addConfig(new ConfigModel("baseNpcId", "8998", "The ID of NPC that symbolizes the base."));
        this.addConfig(new ConfigModel("baseRadius", "180", "The radius of base to count players inside."));
        this.addConfig(new ConfigModel("allowBaseNpcEffects", "true", "Enables Base NPC's special effects, if blue or red team owns it. Due to client limitations, this will only work if the event has 2 teams.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("allowFireworkOnScore", "true", "Enables Base NPC's small firework effect, when a team scores. Working only if <font color=LEVEL>holdBaseFor</font> is higher than 5 (to prevent spamming this skill).", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("allowPlayerEffects", "true", "Enables special effects for players from the team owning the base and standing near the Base NPC (in <font color=LEVEL>baseRadius</font>). Only works if the event has 2 teams.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("baseCheckInterval", "1", "In seconds. The time after it checks and counts players near the base(s) and adds score to the team, that has more players inside the base. Setting this to 1 is usually good (higher values make this event less expensive for cpu)"));
        this.addConfig(new ConfigModel("minPlayersToCaptureBase", "25%", "The min count of players the team must have near the base in order to capture it. You can set this value in percent by adding % (eg. 5%) - this will calculate the min count of players from the size of the team (eg. 20% and 50 players in the team = at least 10 players are needed to capture a base)."));
        this.addConfig(new ConfigModel("typeOfScoring", "AllTeams", "Define the way the event will give score to teams for capturing bases. If you select 'AllTeams', the event will score to all teams based on the count of bases they own (eg. team A has 2 bases - will receive 2 score, team B has 1 base - will receive 1 score). Setting 'DominatingTeam' will make it so that only the team which has MORE bases than the other teams will be receiving score points.", ConfigModel.InputType.Enum).addEnumOptions(new String[]{"AllTeams", "DominatingTeam"}));
        this.addConfig(new ConfigModel("scoreForCapturingBase", "1", "The ammount of points team gets each <font color=LEVEL>scoreCheckInterval</font> seconds if owns the base."));
        this.addConfig(new ConfigModel("holdBaseToCapture", "0", "In seconds. In order to capture a single base, the team needs to stay for this time near it."));
        this.addConfig(new ConfigModel("holdAllBasesToScore", "0", "In seconds. If the team captures enought bases to score, they will still need to hold them for this time in order to get <font color=LEVEL>scoreForCapturingBase</font> score."));
        this.addConfig(new ConfigModel("minTowersToOwnToScore", "1", "The min count of towers one team must own in order to get any score."));
        this.addConfig(new ConfigModel("percentMajorityToScore", "50", "In percent. In order to score a point, the team must have more players near the base NPC in <font color=LEVEL>baseRadius</font> radius, than the other team(s). The ammount of players from the scoring team must be higher than the ammount of players from the other teams by this percent value. Put 100 to make that all other team(s)' players in <font color=LEVEL>baseRadius</font> must be dead to score; or put 0 to make that it will give score to the team that has more players and not care about any percent counting (eg. if team A has 15 players and team B has 16, it will simply reward team B)."));
        this.addConfig(new ConfigModel("createParties", "true", "Put 'True' if you want this event to automatically create parties for players in each team."));
        this.addConfig(new ConfigModel("maxPartySize", "9", "The maximum size of party, that can be created. Works only if <font color=LEVEL>createParties</font> is true."));
        this.addConfig(new ConfigModel("teamsCount", "2", "The ammount of teams in the event. Max is 5. <font color=FF0000>In order to change the count of teams in the event, you must also edit this config in the Instance's configuration.</font>"));
        this.addConfig(new ConfigModel("firstBloodMessage", "true", "You can turn off/on the first blood announce in the event (first kill made in the event). This is also rewardable - check out reward type FirstBlood.", ConfigModel.InputType.Boolean));
        this.addInstanceTypeConfig(new ConfigModel("teamsCount", "2", "You may specify the count of teams only for this instance. This config overrides events default teams count."));
    }

    @Override
    public void initEvent() {
        super.initEvent();
        this._waweRespawn = this.getBoolean("waweRespawn");
        if (this._waweRespawn) {
            this.initWaweRespawns(this.getInt("resDelay"));
        }
        this._towerNpcId = this.getInt("baseNpcId");
        this._towerRadius = (int)Math.pow(this.getInt("baseRadius"), 2.0);
        this._towerCheckInterval = this.getInt("baseCheckInterval");
        String s = this.getString("minPlayersToCaptureBase");
        if (s.endsWith("%")) {
            this._minPlayersToCaptureTheBase = Integer.parseInt(s.substring(0, s.length() - 1));
            this.isMinPlayersToCaptureTheBaseInPercent = true;
        } else {
            this._minPlayersToCaptureTheBase = Integer.parseInt(s);
            this.isMinPlayersToCaptureTheBaseInPercent = false;
        }
        this._scoreType = this.getString("typeOfScoring");
        this._minTowersToOwnToScore = this.getInt("minTowersToOwnToScore");
        this._timeToHoldTowerToCapture = this.getInt("holdBaseToCapture");
        this._holdAllTowersFor = this.getInt("holdBaseFor");
        this._scoreForCapturingTower = this.getInt("scoreForCapturingBase");
        this._percentMajorityToCapture = this.getInt("percentMajorityToScore");
        this.countOfTowers = this._manager.getMap().getSpawns(-1, SpawnType.Base).size();
        this._runningInstances = 0;
        this.tick = 0;
    }

    @Override
    protected int initInstanceTeams(MainEventInstanceType type, int instanceId) {
        this._teamsCount = type.getConfigInt("teamsCount");
        if (this._teamsCount < 2 || this._teamsCount > 5) {
            this._teamsCount = this.getInt("teamsCount");
        }
        if (this._teamsCount < 2 || this._teamsCount > 5) {
            this._teamsCount = 2;
        }
        this.createTeams(this._teamsCount, type.getInstance().getId());
        return this._teamsCount;
    }

    @Override
    public void runEvent() {
        if (NexusLoader.detailedDebug) {
            this.print("Event: started runEvent()");
        }
        if (!this.dividePlayers()) {
            this.clearEvent();
            return;
        }
        this._matches = new FastMap();
        for (InstanceData instance : this._instances) {
            if (NexusLoader.detailedDebug) {
                this.print("Event: creating eventinstance for instance " + instance.getId());
            }
            BattlefieldEventInstance match = this.createEventInstance(instance);
            this._matches.put((Object)instance.getId(), (Object)match);
            ++this._runningInstances;
            match.scheduleNextTask(0);
            if (!NexusLoader.detailedDebug) continue;
            this.print("Event: event instance started");
        }
        if (NexusLoader.detailedDebug) {
            this.print("Event: finished runEvent()");
        }
    }

    @Override
    protected void enableMarkers(int instanceId, boolean createEventSpawnMarkers) {
        if (!this._enableRadar) {
            return;
        }
        for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                pi.createRadar();
            }
            this.startRadar(instanceId, team);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private void startRadar(int instanceId, EventTeam team) {
        EventSpawn zone = this.selectZoneForRadar(instanceId, team);
        if (zone != null) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                pi.getRadar().setLoc(zone.getLoc().getX(), zone.getLoc().getY(), zone.getLoc().getZ());
                pi.getRadar().setRepeat(true);
                pi.getRadar().enable();
            }
            return;
        } else {
            for (PlayerEventInfo pi : team.getPlayers()) {
                pi.getRadar().setRepeat(false);
                pi.getRadar().disable();
            }
        }
    }

    private EventSpawn selectZoneForRadar(int instanceId, EventTeam team) {
        EventSpawn zone = null;
        int teamId = team.getTeamId();
        int topImportance = Integer.MAX_VALUE;
        Tower tempTopImportance = null;
        for (Tower tower2 : this.getEventData(instanceId)._towers) {
            if (tower2 == null || tower2.getOwningTeam() != 0 || tower2.getSpawn().getSpawnTeam() != teamId || tower2.getSpawn().getImportance() >= topImportance) continue;
            topImportance = tower2.getSpawn().getImportance();
            tempTopImportance = tower2;
        }
        if (tempTopImportance == null) {
            topImportance = Integer.MAX_VALUE;
            for (Tower tower2 : this.getEventData(instanceId)._towers) {
                if (tower2 == null || tower2.getOwningTeam() != 0 || tower2.getSpawn().getImportance() >= topImportance) continue;
                topImportance = tower2.getSpawn().getImportance();
                tempTopImportance = tower2;
            }
        }
        if (tempTopImportance == null) {
            topImportance = Integer.MAX_VALUE;
            for (Tower tower2 : this.getEventData(instanceId)._towers) {
                if (tower2 == null || tower2.getSpawn().getSpawnTeam() != teamId || tower2.getOwningTeam() == teamId || tower2.getSpawn().getImportance() >= topImportance) continue;
                topImportance = tower2.getSpawn().getImportance();
                tempTopImportance = tower2;
            }
        }
        if (tempTopImportance == null) {
            topImportance = 0;
            for (Tower tower2 : this.getEventData(instanceId)._towers) {
                if (tower2 == null || tower2.getOwningTeam() == teamId || tower2.getSpawn().getImportance() <= topImportance) continue;
                topImportance = tower2.getSpawn().getImportance();
                tempTopImportance = tower2;
            }
        }
        if (tempTopImportance != null) {
            zone = tempTopImportance.getSpawn();
        }
        return zone;
    }

    protected void spawnTowers(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: spawning bases for instance " + instanceId);
        }
        this.clearMapHistory(-1, SpawnType.Base);
        int i = 0;
        for (EventSpawn sp : this._manager.getMap().getSpawns(-1, SpawnType.Base)) {
            NpcData base = this.spawnNPC(sp.getLoc().getX(), sp.getLoc().getY(), sp.getLoc().getZ(), this._towerNpcId, instanceId, "Base " + ++i, "Domination event");
            this.getEventData(instanceId).addTower(base, sp.getRadius(), sp);
        }
    }

    protected void unspawnTowers(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: unspawning base for instance " + instanceId);
        }
        for (Tower tower : this.getEventData(instanceId)._towers) {
            if (tower.getNpc() == null) continue;
            tower.setOwningTeam(0, false);
            tower.getNpc().deleteMe();
        }
    }

    protected void setBaseEffects(int teamId, NpcData baseNpc) {
        if (this.getBoolean("allowBaseNpcEffects") && this._teamsCount == 2) {
            if (teamId == 1) {
                baseNpc.stopAbnormalEffect(4);
                baseNpc.startAbnormalEffect(2097152);
            } else if (teamId == 2) {
                baseNpc.stopAbnormalEffect(2097152);
                baseNpc.startAbnormalEffect(4);
            } else {
                baseNpc.stopAbnormalEffect(4);
                baseNpc.stopAbnormalEffect(2097152);
            }
        }
    }

    @Override
    public void onEventEnd() {
        if (NexusLoader.detailedDebug) {
            this.print("Event: onEventEnd()");
        }
        int minKills = this.getInt("killsForReward");
        int minScore = this.getInt("scoreForReward");
        this.rewardAllTeams(-1, minScore, minKills);
    }

    @Override
    protected synchronized boolean instanceEnded() {
        --this._runningInstances;
        if (NexusLoader.detailedDebug) {
            this.print("Event: notifying instance ended: runningInstances = " + this._runningInstances);
        }
        if (this._runningInstances == 0) {
            this._manager.end();
            return true;
        }
        return false;
    }

    @Override
    protected synchronized void endInstance(int instance, boolean canBeAborted, boolean canRewardIfAborted, boolean forceNotReward) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: endInstance() " + instance + ", canBeAborted " + canBeAborted + ", canReward.. " + canRewardIfAborted + " forceNotReward " + forceNotReward);
        }
        if (forceNotReward) {
            ((BattlefieldEventInstance)this._matches.get((Object)instance)).forceNotRewardThisInstance();
        }
        ((BattlefieldEventInstance)this._matches.get((Object)instance)).setNextState(EventState.END);
        if (canBeAborted) {
            ((BattlefieldEventInstance)this._matches.get((Object)instance)).setCanBeAborted();
        }
        if (canRewardIfAborted) {
            ((BattlefieldEventInstance)this._matches.get((Object)instance)).setCanRewardIfAborted();
        }
        ((BattlefieldEventInstance)this._matches.get((Object)instance)).scheduleNextTask(0);
    }

    @Override
    protected String getScorebar(int instance) {
        int count = ((FastMap)this._teams.get((Object)instance)).size();
        TextBuilder tb = new TextBuilder();
        for (EventTeam team : ((FastMap)this._teams.get((Object)instance)).values()) {
            if (count <= 4) {
                tb.append(team.getTeamName() + ": " + team.getScore() + "  ");
                continue;
            }
            tb.append(team.getTeamName().substring(0, 1) + ": " + team.getScore() + "  ");
        }
        if (count <= 3) {
            tb.append(LanguageEngine.getMsg("event_scorebar_time", ((BattlefieldEventInstance)this._matches.get((Object)instance)).getClock().getTime()));
        }
        return tb.toString();
    }

    @Override
    protected String getTitle(PlayerEventInfo pi) {
        if (pi.isAfk()) {
            return "AFK";
        }
        return "Score: " + this.getPlayerData(pi).getScore();
    }

    @Override
    public void onKill(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null) {
            return;
        }
        if (player.getTeamId() != target.getEventInfo().getTeamId()) {
            this.tryFirstBlood(player);
            this.giveOnKillReward(player);
            player.getEventTeam().raiseKills(1);
            this.getPlayerData(player).raiseKills(1);
            this.getPlayerData(player).raiseSpree(1);
            this.giveKillingSpreeReward(this.getPlayerData(player));
            if (player.isTitleUpdated()) {
                player.setTitle(this.getTitle(player), true);
                player.broadcastTitleInfo();
            }
            CallbackManager.getInstance().playerKills(this.getEventType(), player, target.getEventInfo());
            this.setKillsStats(player, this.getPlayerData(player).getKills());
        }
    }

    @Override
    public void onDie(PlayerEventInfo player, CharacterData killer) {
        if (NexusLoader.detailedDebug) {
            this.print("/// Event: onDie - player " + player.getPlayersName() + " (instance " + player.getInstanceId() + "), killer " + killer.getName());
        }
        this.getPlayerData(player).raiseDeaths(1);
        this.getPlayerData(player).setSpree(0);
        this.setDeathsStats(player, this.getPlayerData(player).getDeaths());
        if (this._waweRespawn) {
            this._waweScheduler.addPlayer(player);
        } else {
            this.scheduleRevive(player, this.getInt("resDelay") * 1000);
        }
    }

    @Override
    public EventPlayerData createPlayerData(PlayerEventInfo player) {
        PvPEventPlayerData d = new PvPEventPlayerData(player, this, new GlobalStatsModel(this.getEventType()));
        return d;
    }

    @Override
    public PvPEventPlayerData getPlayerData(PlayerEventInfo player) {
        return (PvPEventPlayerData)player.getEventData();
    }

    @Override
    public synchronized void clearEvent(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: called CLEAREVENT for instance " + instanceId);
        }
        try {
            if (this._matches != null) {
                for (BattlefieldEventInstance match : this._matches.values()) {
                    if (instanceId != 0 && instanceId != match.getInstance().getId()) continue;
                    match.abort();
                    this.unspawnTowers(match.getInstance().getId());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            if (!player.isOnline()) continue;
            if (player.isParalyzed()) {
                player.setIsParalyzed(false);
            }
            if (player.isImmobilized()) {
                player.unroot();
            }
            if (!player.isGM()) {
                player.setIsInvul(false);
            }
            player.removeRadarAllMarkers();
            player.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_IMPRISIONING_1());
            player.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REDCIRCLE());
            player.setInstanceId(0);
            if (this._removeBuffsOnEnd) {
                player.removeBuffs();
            }
            player.restoreData();
            player.teleport(player.getOrigLoc(), 0, true, 0);
            player.sendMessage(LanguageEngine.getMsg("event_teleportBack"));
            if (player.getParty() != null) {
                PartyData party = player.getParty();
                party.removePartyMember(player);
            }
            player.broadcastUserInfo();
        }
        this.clearPlayers(true, instanceId);
    }

    @Override
    public synchronized void clearEvent() {
        if (NexusLoader.detailedDebug) {
            this.print("Event: called global clearEvent()");
        }
        this.clearEvent(0);
    }

    @Override
    protected void respawnPlayer(PlayerEventInfo pi, int instance) {
        EventSpawn spawn;
        if (NexusLoader.detailedDebug) {
            this.print("/// Event: respawning player " + pi.getPlayersName() + ", instance " + instance);
        }
        if ((spawn = this.getSpawn(SpawnType.Regular, pi.getTeamId())) != null) {
            Loc loc = new Loc(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ());
            loc.addRadius(spawn.getRadius());
            pi.teleport(loc, 0, true, instance);
            pi.sendMessage(LanguageEngine.getMsg("event_respawned"));
        } else {
            this.debug("Error on respawnPlayer - no spawn type REGULAR, team " + pi.getTeamId() + " has been found. Event aborted.");
        }
    }

    @Override
    protected void clockTick() throws Exception {
        ++this.tick;
        if (this.tick % this._towerCheckInterval != 0) {
            return;
        }
        for (BattlefieldEventInstance instance : this._matches.values()) {
            int instanceId = instance.getInstance().getId();
            if (this.tick % 10 == 0) {
                for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
                    this.startRadar(instanceId, team);
                }
            }
            TowerData towerData = this.getEventData(instanceId);
            FastMap ownedTowers = new FastMap();
            for (int i = 0; i < towerData._towers.length; ++i) {
                FastMap players = new FastMap(this._teamsCount);
                Tower tower = towerData._towers[i];
                NpcData towerNpc = towerData._towers[i].getNpc();
                int radius = towerData._towers[i].getRadius();
                int baseX = towerNpc.getLoc().getX();
                int baseY = towerNpc.getLoc().getY();
                int baseZ = towerNpc.getLoc().getZ();
                for (PlayerEventInfo player : this.getPlayers(instanceId)) {
                    if (player.getDistanceSq(baseX, baseY, baseZ) > (double)radius || !player.isVisible() || player.isDead()) continue;
                    if (!players.containsKey(player.getTeamId())) {
                        players.put(player.getTeamId(), new FastList());
                    }
                    ((List)players.get(player.getTeamId())).add(player);
                }
                boolean highestCount22 = false;
                int team = 0;
                boolean isThereMajorityTeam = true;
                for (Map.Entry teamData : players.entrySet()) {
                    void highestCount22;
                    if (((List)teamData.getValue()).size() > highestCount22) {
                        int highestCount22 = ((List)teamData.getValue()).size();
                        team = (Integer)teamData.getKey();
                        continue;
                    }
                    if (highestCount22 == false || ((List)teamData.getValue()).size() != highestCount22) continue;
                    isThereMajorityTeam = false;
                    break;
                }
                if (isThereMajorityTeam && team != 0) {
                    int majorityTeamPlayersCount = ((List)players.get(team)).size();
                    boolean dominatesBase = false;
                    if (this._percentMajorityToCapture == 0) {
                        dominatesBase = true;
                    } else if (this._percentMajorityToCapture == 100) {
                        boolean teamWithMorePlayers = false;
                        for (Map.Entry teamData2 : players.entrySet()) {
                            if ((Integer)teamData2.getKey() == team || ((List)teamData2.getValue()).size() <= 0) continue;
                            teamWithMorePlayers = true;
                            break;
                        }
                        if (!teamWithMorePlayers) {
                            dominatesBase = true;
                        }
                    } else {
                        boolean teamWithMorePlayers = false;
                        for (Map.Entry teamData2 : players.entrySet()) {
                            int percent;
                            double d;
                            if ((Integer)teamData2.getKey() == team || (percent = 100 - (int)((d = (double)((List)teamData2.getValue()).size() / (double)majorityTeamPlayersCount) * 100.0)) >= this._percentMajorityToCapture) continue;
                            teamWithMorePlayers = true;
                            break;
                        }
                        if (!teamWithMorePlayers) {
                            dominatesBase = true;
                        }
                    }
                    if (dominatesBase) {
                        int countInTeam = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getPlayers().size();
                        int minCountOfPlayersNearTheBase = this.isMinPlayersToCaptureTheBaseInPercent ? (int)Math.round((double)countInTeam * ((double)this._minPlayersToCaptureTheBase * 0.01)) : this._minPlayersToCaptureTheBase;
                        if (minCountOfPlayersNearTheBase < 1) {
                            minCountOfPlayersNearTheBase = 1;
                        }
                        if (majorityTeamPlayersCount < minCountOfPlayersNearTheBase) {
                            if (this.tick % 2 == 0) {
                                for (PlayerEventInfo player2 : (List)players.get(team)) {
                                    if (player2 == null || !player2.isOnline()) continue;
                                    player2.sendMessage("At least " + minCountOfPlayersNearTheBase + " players from your team are required to capture a base.");
                                }
                            }
                            dominatesBase = false;
                        }
                    }
                    if (dominatesBase) {
                        if (tower.getOwningTeam() == 0) {
                            if (tower.setCapturingTime(tower.getCapturingTime() + this._towerCheckInterval)) {
                                this.announce(instanceId, "* " + ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getFullName() + " has gained the control of base " + (i + 1));
                                if (this.getBoolean("allowPlayerEffects") && this._teamsCount == 2) {
                                    for (PlayerEventInfo player3 : tower.getEffectedPlayers()) {
                                        if (player3 == null) continue;
                                        tower.removeEffectedPlayer(player3);
                                        player3.stopAbnormalEffect(player3.getTeamId() == 1 ? CallBack.getInstance().getValues().ABNORMAL_IMPRISIONING_1() : CallBack.getInstance().getValues().ABNORMAL_REDCIRCLE());
                                    }
                                    tower.resetEffectedPlayers();
                                }
                                tower.setOwningTeam(team, true);
                                this.setBaseEffects(team, towerNpc);
                                towerNpc.setTitle("Owner: " + ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getFullName());
                                towerNpc.broadcastNpcInfo();
                                for (PlayerEventInfo player2 : (List)players.get(team)) {
                                    this.getPlayerData(player2).raiseScore(this._scoreForCapturingTower);
                                    this.setScoreStats(player2, this.getPlayerData(player2).getScore());
                                    if (player2.isTitleUpdated()) {
                                        player2.setTitle(this.getTitle(player2), true);
                                        player2.broadcastTitleInfo();
                                    }
                                    CallbackManager.getInstance().playerScores(this.getEventType(), player2, this._scoreForCapturingTower);
                                }
                            } else if (tower.getCapturingTime() == this._towerCheckInterval) {
                                this.announce(instanceId, ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getFullName() + " is now capturing base " + (i + 1));
                            }
                        } else if (tower.getOwningTeam() != team) {
                            if (tower.setCapturingTime(tower.getCapturingTime() + this._towerCheckInterval)) {
                                this.announce(instanceId, "* " + ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getFullName() + " has gained the control of base " + (i + 1));
                                if (this.getBoolean("allowPlayerEffects") && this._teamsCount == 2) {
                                    for (PlayerEventInfo player2 : tower.getEffectedPlayers()) {
                                        if (player2 == null) continue;
                                        tower.removeEffectedPlayer(player2);
                                        player2.stopAbnormalEffect(player2.getTeamId() == 1 ? CallBack.getInstance().getValues().ABNORMAL_IMPRISIONING_1() : CallBack.getInstance().getValues().ABNORMAL_REDCIRCLE());
                                    }
                                    tower.resetEffectedPlayers();
                                }
                                tower.setOwningTeam(team, true);
                                this.setBaseEffects(team, towerNpc);
                                towerNpc.setTitle("Owner: " + ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getFullName());
                                towerNpc.broadcastNpcInfo();
                                for (PlayerEventInfo player2 : (List)players.get(team)) {
                                    this.getPlayerData(player2).raiseScore(this._scoreForCapturingTower);
                                    this.setScoreStats(player2, this.getPlayerData(player2).getScore());
                                    if (player2.isTitleUpdated()) {
                                        player2.setTitle(this.getTitle(player2), true);
                                        player2.broadcastTitleInfo();
                                    }
                                    CallbackManager.getInstance().playerScores(this.getEventType(), player2, this._scoreForCapturingTower);
                                }
                            } else if (tower.getCapturingTime() == this._towerCheckInterval) {
                                this.announce(instanceId, ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getFullName() + " is now capturing base " + (i + 1));
                            }
                        }
                    } else if (tower.getCapturingTime() > 0) {
                        tower.setCapturingTime(0);
                    }
                } else if (tower.getCapturingTime() > 0) {
                    tower.setCapturingTime(0);
                }
                if (tower.getOwningTeam() <= 0) continue;
                if (this.getBoolean("allowPlayerEffects") && this._teamsCount == 2) {
                    if (players.containsKey(tower.getOwningTeam())) {
                        for (PlayerEventInfo player4 : (List)players.get(tower.getOwningTeam())) {
                            if (tower.containsEffectedPlayer(player4)) continue;
                            tower.addEffectedPlayer(player4);
                            player4.startAbnormalEffect(player4.getTeamId() == 1 ? CallBack.getInstance().getValues().ABNORMAL_IMPRISIONING_1() : CallBack.getInstance().getValues().ABNORMAL_REDCIRCLE());
                        }
                    }
                    for (PlayerEventInfo player5 : tower.getEffectedPlayers()) {
                        if (players.containsKey(tower.getOwningTeam()) && ((List)players.get(tower.getOwningTeam())).contains((Object)player5)) continue;
                        tower.removeEffectedPlayer(player5);
                        player5.stopAbnormalEffect(player5.getTeamId() == 1 ? CallBack.getInstance().getValues().ABNORMAL_IMPRISIONING_1() : CallBack.getInstance().getValues().ABNORMAL_REDCIRCLE());
                    }
                }
                tower.raiseOwnedTime(this._towerCheckInterval);
                if (!ownedTowers.containsKey(tower.getOwningTeam())) {
                    ownedTowers.put(tower.getOwningTeam(), new FastList());
                }
                ((List)ownedTowers.get(tower.getOwningTeam())).add(towerNpc);
            }
            if (this._scoreType.equals("AllTeams")) {
                this._minTowersToOwnToScore = 1;
                for (Map.Entry e : ownedTowers.entrySet()) {
                    int team = (Integer)e.getKey();
                    int countOfTowers = ((List)e.getValue()).size();
                    if (countOfTowers < this._minTowersToOwnToScore || countOfTowers <= 0) continue;
                    ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).raiseScore(countOfTowers);
                }
                continue;
            }
            if (!this._scoreType.equals("DominatingTeam")) continue;
            boolean ownsRequiredCountOfBases = false;
            boolean teamWithMostBases2 = false;
            int mostBasesCount = 0;
            for (Map.Entry e : ownedTowers.entrySet()) {
                if (((List)e.getValue()).size() > mostBasesCount) {
                    int teamWithMostBases2 = (Integer)e.getKey();
                    mostBasesCount = ((List)e.getValue()).size();
                    ownsRequiredCountOfBases = true;
                    continue;
                }
                if (((List)e.getValue()).size() == 0 || ((List)e.getValue()).size() != mostBasesCount) continue;
                ownsRequiredCountOfBases = false;
                break;
            }
            if (ownsRequiredCountOfBases) {
                boolean bl = ownsRequiredCountOfBases = mostBasesCount >= this._minTowersToOwnToScore;
            }
            if (ownsRequiredCountOfBases) {
                void teamWithMostBases2;
                if (teamWithMostBases2 != towerData._dominatingTeam) {
                    this.announce(instanceId, "++ " + ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)((int)teamWithMostBases2))).getFullName() + " owns most bases - " + mostBasesCount + "!");
                    towerData.setDominatingTeam((int)teamWithMostBases2);
                    towerData.resetDominatingTime();
                } else {
                    towerData.raiseDominatingTime(this._towerCheckInterval);
                }
                if (towerData.getDominatingTime() >= this._holdAllTowersFor) {
                    ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)((int)teamWithMostBases2))).raiseScore(this._scoreForCapturingTower);
                    towerData.resetDominatingTime();
                    if (this._holdAllTowersFor <= 5) continue;
                    this.announce(instanceId, "*** " + ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)((int)teamWithMostBases2))).getTeamName() + "s scored for owning " + mostBasesCount + " bases!");
                    if (!this.getBoolean("allowFireworkOnScore")) continue;
                    for (Tower tow : towerData._towers) {
                        if (tow.getNpc() == null || tow.getOwningTeam() != towerData.getDominatingTeam()) continue;
                        tow.getNpc().broadcastSkillUse((CharacterData)tow.getNpc(), (CharacterData)tow.getNpc(), 2024, 1);
                    }
                    continue;
                }
                int toHold = this._holdAllTowersFor - towerData._holdingAllTowersFor;
                boolean announce = false;
                if (towerData._holdingAllTowersFor == 0) {
                    announce = true;
                } else if (toHold >= 60 && toHold % 60 == 0) {
                    announce = true;
                } else {
                    switch (toHold) {
                        case 5: 
                        case 10: 
                        case 20: 
                        case 30: 
                        case 45: {
                            announce = true;
                        }
                    }
                }
                if (!announce) continue;
                boolean min = false;
                Object[] arrobject = new Object[3];
                arrobject[0] = toHold;
                arrobject[1] = min ? "minutes" : "seconds";
                arrobject[2] = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)((int)teamWithMostBases2))).getFullName();
                this.announce(instanceId, " ** " + LanguageEngine.getMsg("mDom_leftToScore", arrobject));
                continue;
            }
            if (towerData.getDominatingTeam() != 0 && towerData.getDominatingTime() > 0) {
                this.announce(instanceId, "-- " + ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)towerData._dominatingTeam)).getFullName() + " has lost domination of bases.");
            }
            towerData.setDominatingTeam(0);
            towerData.resetDominatingTime();
        }
    }

    @Override
    public String getEstimatedTimeLeft() {
        if (this._matches == null) {
            return "Starting";
        }
        for (BattlefieldEventInstance match : this._matches.values()) {
            if (!match.isActive()) continue;
            return match.getClock().getTime();
        }
        return "N/A";
    }

    @Override
    public int getTeamsCount() {
        return this.getInt("teamsCount");
    }

    @Override
    public String getMissingSpawns(EventMap map) {
        TextBuilder tb = new TextBuilder();
        for (int i = 0; i < this.getTeamsCount(); ++i) {
            if (map.checkForSpawns(SpawnType.Regular, i + 1, 1)) continue;
            tb.append(this.addMissingSpawn(SpawnType.Regular, i + 1, 1));
        }
        if (!map.checkForSpawns(SpawnType.Base, -1, 1)) {
            tb.append(this.addMissingSpawn(SpawnType.Base, 0, 1));
        }
        return tb.toString();
    }

    @Override
    protected String addExtraEventInfoCb(int instance) {
        int owningTeam = ((BattlefieldEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._towerData._dominatingTeam;
        String status = "<font color=ac9887>Dominates:</font> <font color=" + EventManager.getInstance().getDarkColorForHtml(owningTeam) + ">" + EventManager.getInstance().getTeamName(owningTeam) + " team</font>";
        return "<table width=510 bgcolor=3E3E3E><tr><td width=510 align=center>" + status + "</td></tr></table>";
    }

    @Override
    public String getHtmlDescription() {
        if (this._htmlDescription == null) {
            EventDescription desc = EventDescriptionSystem.getInstance().getDescription(this.getEventType());
            this._htmlDescription = desc != null ? desc.getDescription(this.getConfigs()) : "No information about this event yet.";
        }
        return this._htmlDescription;
    }

    @Override
    protected AbstractMainEvent.AbstractEventInstance getMatch(int instanceId) {
        return (AbstractMainEvent.AbstractEventInstance)this._matches.get((Object)instanceId);
    }

    @Override
    protected TowerData createEventData(int instance) {
        return new TowerData(instance);
    }

    @Override
    protected BattlefieldEventInstance createEventInstance(InstanceData instance) {
        return new BattlefieldEventInstance(instance);
    }

    @Override
    protected TowerData getEventData(int instance) {
        return ((BattlefieldEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._towerData;
    }

    protected static enum EventState {
        START,
        FIGHT,
        END,
        TELEPORT,
        INACTIVE;
        

        private EventState() {
        }
    }

    protected class BattlefieldEventInstance
    extends AbstractMainEvent.AbstractEventInstance {
        protected EventState _state;
        protected TowerData _towerData;

        protected BattlefieldEventInstance(InstanceData instance) {
            super(instance);
            this._state = EventState.START;
            this._towerData = Battlefield.this.createEventData(instance.getId());
        }

        protected void setNextState(EventState state) {
            this._state = state;
        }

        @Override
        public boolean isActive() {
            return this._state != EventState.INACTIVE;
        }

        @Override
        public void run() {
            try {
                if (NexusLoader.detailedDebug) {
                    Battlefield.this.print("Event: running task of state " + this._state.toString() + "...");
                }
                switch (this._state) {
                    case START: {
                        if (!Battlefield.this.checkPlayers(this._instance.getId())) break;
                        Battlefield.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, false);
                        Battlefield.this.setupTitles(this._instance.getId());
                        Battlefield.this.spawnTowers(this._instance.getId());
                        Battlefield.this.enableMarkers(this._instance.getId(), true);
                        Battlefield.this.forceSitAll(this._instance.getId());
                        this.setNextState(EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        Battlefield.this.forceStandAll(this._instance.getId());
                        if (Battlefield.this.getBoolean("createParties")) {
                            Battlefield.this.createParties(Battlefield.this.getInt("maxPartySize"));
                        }
                        this.setNextState(EventState.END);
                        this._clock.startClock(Battlefield.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        Battlefield.this.unspawnTowers(this._instance.getId());
                        this.setNextState(EventState.INACTIVE);
                        if (Battlefield.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            Battlefield.this.rewardAllTeams(this._instance.getId(), Battlefield.this.getInt("scoreForReward"), Battlefield.this.getInt("killsForReward"));
                        }
                        Battlefield.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    Battlefield.this.print("Event: ... finished running task. next state " + this._state.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                Battlefield.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    protected class TowerData
    extends AbstractMainEvent.AbstractEventData {
        private Tower[] _towers;
        private int _order;
        private int _dominatingTeam;
        private int _holdingAllTowersFor;

        protected TowerData(int instance) {
            super(instance);
            this._towers = new Tower[Battlefield.this.countOfTowers];
            this._dominatingTeam = 0;
            this._holdingAllTowersFor = 0;
            this._order = 0;
        }

        protected void addTower(NpcData base, int radius, EventSpawn spawn) {
            if (this._order < Battlefield.this.countOfTowers) {
                this._towers[this._order] = new Tower(spawn, base, radius > 0 ? (int)Math.pow(radius, 2.0) : Battlefield.this._towerRadius);
                ++this._order;
            } else {
                NexusLoader.debug((String)("too many towers for TowerData (" + this._order + "; " + Battlefield.this.countOfTowers + ")"));
            }
        }

        protected void setDominatingTeam(int team) {
            this._dominatingTeam = team;
        }

        protected int getDominatingTeam() {
            return this._dominatingTeam;
        }

        protected int raiseDominatingTime(int time) {
            this._holdingAllTowersFor+=time;
            return this._holdingAllTowersFor;
        }

        protected int getDominatingTime() {
            return this._holdingAllTowersFor;
        }

        protected void resetDominatingTime() {
            this._holdingAllTowersFor = 0;
        }

        protected Tower getTower(int index) {
            return this._towers[index];
        }
    }

    protected class Tower {
        private NpcData _npc;
        private EventSpawn _spawn;
        private final int _radius;
        private int _owningTeam;
        private int _ownedTime;
        private int _capturingTime;
        private List<PlayerEventInfo> _effects;

        public Tower(EventSpawn spawn, NpcData npc, int radius) {
            this._spawn = spawn;
            this._npc = npc;
            this._radius = radius;
            this._owningTeam = 0;
            this._capturingTime = 0;
            this._effects = new FastList();
        }

        public void setOwningTeam(int team, boolean updateTime) {
            this._owningTeam = team;
            if (updateTime) {
                this.setOwnedTime(0);
            }
        }

        public boolean setCapturingTime(int i) {
            this._capturingTime = i;
            if (this._capturingTime >= Battlefield.this._timeToHoldTowerToCapture) {
                return true;
            }
            return false;
        }

        public int getCapturingTime() {
            return this._capturingTime;
        }

        public void addEffectedPlayer(PlayerEventInfo player) {
            this._effects.add(player);
        }

        public void removeEffectedPlayer(PlayerEventInfo player) {
            this._effects.remove((Object)player);
        }

        public boolean containsEffectedPlayer(PlayerEventInfo player) {
            return this._effects.contains((Object)player);
        }

        public List<PlayerEventInfo> getEffectedPlayers() {
            return this._effects;
        }

        public void resetEffectedPlayers() {
            this._effects.clear();
        }

        public int getOwningTeam() {
            return this._owningTeam;
        }

        public int getOwnedTime() {
            return this._ownedTime;
        }

        public void setOwnedTime(int i) {
            this._ownedTime = i;
        }

        public void raiseOwnedTime(int count) {
            this._ownedTime+=count;
        }

        public NpcData getNpc() {
            return this._npc;
        }

        public int getRadius() {
            return this._radius;
        }

        public EventSpawn getSpawn() {
            return this._spawn;
        }

        public Loc getLoc() {
            return this._npc.getLoc();
        }
    }

}

