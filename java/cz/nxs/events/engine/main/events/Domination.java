/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class Domination
extends AbstractMainEvent {
    protected FastMap<Integer, DominationEventInstance> _matches;
    protected boolean _waweRespawn;
    protected int _teamsCount;
    protected int _zoneNpcId;
    protected int _zoneRadius;
    protected int _zoneCheckInterval;
    protected int _scoreForCapturingZone;
    private int _holdZoneFor;
    protected int _percentMajorityToScore;
    protected int _tick;

    public Domination(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Winner, RewardPosition.Looser, RewardPosition.Tie, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill, RewardPosition.KillingSpree});
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.addConfig(new ConfigModel("scoreForReward", "0", "The minimum score required to get a reward (includes all possible rewards). Score in this event is gained by standing near the zone, if the player wasn't afk, he should always have some score."));
        this.addConfig(new ConfigModel("killsForReward", "0", "The minimum kills count required to get a reward (includes all possible rewards)."));
        this.addConfig(new ConfigModel("resDelay", "15", "The delay after which the player is resurrected. In seconds."));
        this.addConfig(new ConfigModel("waweRespawn", "true", "Enables the wawe-style respawn system."));
        this.addConfig(new ConfigModel("zoneNpcId", "8992", "The ID of NPC that symbolizes the zone."));
        this.addConfig(new ConfigModel("zoneRadius", "180", "The radius of zone to count players inside."));
        this.addConfig(new ConfigModel("allowZoneNpcEffects", "true", "Enables Zone NPC's special effects, if blue or red team owns it. Due to client limitations, this will only work if the event has 2 teams.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("allowFireworkOnScore", "true", "Enables Zone NPC's small firework effect, when a team scores. Working only if <font color=LEVEL>holdZoneFor</font> is higher than 5 (to prevent spamming this skill).", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("allowPlayerEffects", "true", "Enables special effects for players from the team owning the zone and standing near the Zone NPC (in <font color=LEVEL>zoneRadius</font>). Only works if the event has 2 teams.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("zoneCheckInterval", "1", "In seconds. The time after it checks and counts players near the zone(s) and adds score to the team, that has more players inside the zone. Setting this to 1 is usually good (higher values make this event less expensive for cpu :)"));
        this.addConfig(new ConfigModel("scoreForCapturingZone", "1", "The ammount of points team gets each <font color=LEVEL>scoreCheckInterval</font> seconds if owns the zone."));
        this.addConfig(new ConfigModel("holdZoneFor", "0", "In seconds. The team needs to own this zone for this time to get <font color=LEVEL>scoreForCapturingZone</font> points. "));
        this.addConfig(new ConfigModel("percentMajorityToScore", "50", "In percent. In order to score a point, the team must have more players near the zone NPC in <font color=LEVEL>zoneRadius</font> radius, than the other team(s). The ammount of players from the scoring team must be higher than the ammount of players from the other teams by this percent value. Put 100 to make that all other team(s)' players in <font color=LEVEL>zoneRadius</font> must be dead to score; or put 0 to make that it will give score to the team that has more players and not care about any percent counting (eg. if team A has 15 players and team B has 16, it will simply reward team B)."));
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
        this._zoneNpcId = this.getInt("zoneNpcId");
        this._zoneRadius = (int)Math.pow(this.getInt("zoneRadius"), 2.0);
        this._zoneCheckInterval = this.getInt("zoneCheckInterval");
        this._holdZoneFor = this.getInt("holdZoneFor");
        this._scoreForCapturingZone = this.getInt("scoreForCapturingZone");
        this._percentMajorityToScore = this.getInt("percentMajorityToScore");
        this._runningInstances = 0;
        this._tick = 0;
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
            DominationEventInstance match = this.createEventInstance(instance);
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

    protected void spawnZone(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: spawning Zone for instance " + instanceId);
        }
        this.clearMapHistory(-1, SpawnType.Zone);
        EventSpawn sp = this.getSpawn(SpawnType.Zone, -1);
        NpcData zone = this.spawnNPC(sp.getLoc().getX(), sp.getLoc().getY(), sp.getLoc().getZ(), this._zoneNpcId, instanceId, "Zone", "Domination event");
        int radius = sp.getRadius();
        if (radius > 0) {
            this._zoneRadius = (int)Math.pow(radius, 2.0);
        }
        this.getEventData(instanceId).addZone(zone, this._zoneRadius);
        this.getEventData(instanceId)._zone.getNpc().setTitle("No owner");
        this.getEventData(instanceId)._zone.getNpc().broadcastNpcInfo();
    }

    protected void unspawnZone(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: unspawning zone for instance " + instanceId);
        }
        if (this.getEventData(instanceId)._zone != null) {
            this.getEventData(instanceId)._zone.deleteMe();
            if (NexusLoader.detailedDebug) {
                this.print("Event: zone is not null and was deleted");
            }
        } else if (NexusLoader.detailedDebug) {
            this.print("Event: ... zone is already null!!!");
        }
    }

    protected void setZoneEffects(int teamId, NpcData zoneNpc) {
        if (this.getBoolean("allowZoneNpcEffects") && this._teamsCount == 2) {
            if (teamId == 1) {
                zoneNpc.stopAbnormalEffect(4);
                zoneNpc.startAbnormalEffect(2097152);
            } else if (teamId == 2) {
                zoneNpc.stopAbnormalEffect(2097152);
                zoneNpc.startAbnormalEffect(4);
            } else {
                zoneNpc.stopAbnormalEffect(4);
                zoneNpc.stopAbnormalEffect(2097152);
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
            ((DominationEventInstance)this._matches.get((Object)instance)).forceNotRewardThisInstance();
        }
        ((DominationEventInstance)this._matches.get((Object)instance)).setNextState(EventState.END);
        if (canBeAborted) {
            ((DominationEventInstance)this._matches.get((Object)instance)).setCanBeAborted();
        }
        if (canRewardIfAborted) {
            ((DominationEventInstance)this._matches.get((Object)instance)).setCanRewardIfAborted();
        }
        ((DominationEventInstance)this._matches.get((Object)instance)).scheduleNextTask(0);
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
            tb.append(LanguageEngine.getMsg("event_scorebar_time", ((DominationEventInstance)this._matches.get((Object)instance)).getClock().getTime()));
        }
        return tb.toString();
    }

    @Override
    protected String getTitle(PlayerEventInfo pi) {
        if (this._hideTitles) {
            return "";
        }
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
                for (DominationEventInstance match : this._matches.values()) {
                    if (instanceId != 0 && instanceId != match.getInstance().getId()) continue;
                    match.abort();
                    this.unspawnZone(match.getInstance().getId());
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
        ++this._tick;
        if (this._tick % this._zoneCheckInterval != 0) {
            return;
        }
        FastMap players = new FastMap(this._teamsCount);
        for (DominationEventInstance match : this._matches.values()) {
            int instanceId = match.getInstance().getId();
            int zoneX = this.getEventData(instanceId)._zone.getLoc().getX();
            int zoneY = this.getEventData(instanceId)._zone.getLoc().getY();
            int zoneZ = this.getEventData(instanceId)._zone.getLoc().getZ();
            for (PlayerEventInfo player : this.getPlayers(instanceId)) {
                if (player.getDistanceSq(zoneX, zoneY, zoneZ) > (double)this._zoneRadius || !player.isVisible() || player.isDead()) continue;
                if (!players.containsKey(player.getTeamId())) {
                    players.put(player.getTeamId(), new FastList());
                }
                ((List)players.get(player.getTeamId())).add(player);
            }
            int highestCount = 0;
            int team = 0;
            boolean isThereMajorityTeam = true;
            for (Map.Entry teamData : players.entrySet()) {
                if (((List)teamData.getValue()).size() > highestCount) {
                    highestCount = ((List)teamData.getValue()).size();
                    team = (Integer)teamData.getKey();
                    continue;
                }
                if (highestCount == 0 || ((List)teamData.getValue()).size() != highestCount) continue;
                isThereMajorityTeam = false;
                break;
            }
            if (isThereMajorityTeam && team != 0) {
                Iterator i$;
                PlayerEventInfo player22;
                boolean ownsZone = false;
                if (this._percentMajorityToScore == 0) {
                    ownsZone = true;
                } else if (this._percentMajorityToScore == 100) {
                    boolean teamWithMorePlayers = false;
                    for (Map.Entry teamData2 : players.entrySet()) {
                        if ((Integer)teamData2.getKey() == team || ((List)teamData2.getValue()).size() <= 0) continue;
                        teamWithMorePlayers = true;
                        break;
                    }
                    if (!teamWithMorePlayers) {
                        ownsZone = true;
                    }
                } else {
                    int majorityTeamPlayers = ((List)players.get(team)).size();
                    boolean teamWithMorePlayers = false;
                    for (Map.Entry teamData3 : players.entrySet()) {
                        double d;
                        int percent;
                        if ((Integer)teamData3.getKey() == team || (percent = 100 - (int)((d = (double)((List)teamData3.getValue()).size() / (double)majorityTeamPlayers) * 100.0)) >= this._percentMajorityToScore) continue;
                        teamWithMorePlayers = true;
                        break;
                    }
                    if (!teamWithMorePlayers) {
                        ownsZone = true;
                    }
                }
                if (ownsZone) {
                    if (this.getEventData(instanceId)._holdingTeam != team) {
                        if (this.getEventData(instanceId)._holdingTeam != 0 && this.getBoolean("allowPlayerEffects") && this._teamsCount == 2) {
                            i$ = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)this.getEventData(instanceId)._holdingTeam)).getPlayers().iterator();
                            while (i$.hasNext()) {
                                player22.stopAbnormalEffect((player22 = (PlayerEventInfo)i$.next()).getTeamId() == 1 ? 2097152 : 4);
                            }
                        }
                        this.announce(instanceId, LanguageEngine.getMsg("dom_gainedZone", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getFullName()));
                        this.getEventData(instanceId)._zone.getNpc().setTitle(LanguageEngine.getMsg("dom_npcTitle_owner", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getTeamName()));
                        this.getEventData(instanceId)._zone.getNpc().broadcastNpcInfo();
                        this.getEventData(instanceId)._holdingTeam = team;
                        this.getEventData(instanceId)._holdingTime = 0;
                        this.setZoneEffects(team, this.getEventData(instanceId)._zone);
                    } else {
                        ZoneData.access$412(this.getEventData(instanceId), this._zoneCheckInterval);
                    }
                    if (this.getBoolean("allowPlayerEffects") && this._teamsCount == 2) {
                        for (PlayerEventInfo player22 : ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getPlayers()) {
                            if (player22.getDistanceSq(zoneX, zoneY, zoneZ) <= (double)this._zoneRadius && player22.isVisible() && !player22.isDead()) {
                                player22.startAbnormalEffect(player22.getTeamId() == 1 ? 2097152 : 4);
                                continue;
                            }
                            player22.stopAbnormalEffect(player22.getTeamId() == 1 ? 2097152 : 4);
                        }
                    }
                    if (this.getEventData(instanceId)._holdingTime >= this._holdZoneFor) {
                        ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).raiseScore(this._scoreForCapturingZone);
                        for (PlayerEventInfo player22 : (List)players.get(team)) {
                            this.getPlayerData(player22).raiseScore(this._scoreForCapturingZone);
                            this.setScoreStats(player22, this.getPlayerData(player22).getScore());
                            if (player22.isTitleUpdated()) {
                                player22.setTitle(this.getTitle(player22), true);
                                player22.broadcastTitleInfo();
                            }
                            CallbackManager.getInstance().playerScores(this.getEventType(), player22, this._scoreForCapturingZone);
                        }
                        this.getEventData(instanceId)._holdingTime = 0;
                        if (this._holdZoneFor <= 5) continue;
                        this.announce(instanceId, "*** " + LanguageEngine.getMsg("dom_score", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getFullName()));
                        if (!this.getBoolean("allowFireworkOnScore")) continue;
                        this.getEventData(instanceId)._zone.broadcastSkillUse((CharacterData)this.getEventData(instanceId)._zone, (CharacterData)this.getEventData(instanceId)._zone, 2024, 1);
                        continue;
                    }
                    int toHold = this._holdZoneFor - this.getEventData(instanceId)._holdingTime;
                    boolean announce = false;
                    if (this.getEventData(instanceId)._holdingTime == 0) {
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
                    arrobject[1] = min ? "minute" : "second";
                    arrobject[2] = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getFullName();
                    this.announce(instanceId, "* " + LanguageEngine.getMsg("dom_leftToScore", arrobject));
                    continue;
                }
                if (this.getEventData(instanceId)._holdingTeam != 0) {
                    this.announce(instanceId, LanguageEngine.getMsg("dom_lostZone", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)this.getEventData(instanceId)._holdingTeam)).getFullName()));
                    this.getEventData(instanceId)._zone.getNpc().setTitle(LanguageEngine.getMsg("dom_npcTitle_noOwner"));
                    this.getEventData(instanceId)._zone.getNpc().broadcastNpcInfo();
                    this.setZoneEffects(0, this.getEventData(instanceId)._zone);
                    if (this.getBoolean("allowPlayerEffects") && this._teamsCount == 2) {
                        i$ = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)this.getEventData(instanceId)._holdingTeam)).getPlayers().iterator();
                        while (i$.hasNext()) {
                            player22.stopAbnormalEffect((player22 = (PlayerEventInfo)i$.next()).getTeamId() == 1 ? 2097152 : 4);
                        }
                    }
                }
                this.getEventData(instanceId)._holdingTime = 0;
                this.getEventData(instanceId)._holdingTeam = 0;
                continue;
            }
            if (this.getEventData(instanceId)._holdingTeam != 0) {
                this.announce(instanceId, LanguageEngine.getMsg("dom_lostZone", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)this.getEventData(instanceId)._holdingTeam)).getFullName()));
                this.getEventData(instanceId)._zone.getNpc().setTitle(LanguageEngine.getMsg("dom_npcTitle_noOwner"));
                this.getEventData(instanceId)._zone.getNpc().broadcastNpcInfo();
                this.setZoneEffects(0, this.getEventData(instanceId)._zone);
                if (this.getBoolean("allowPlayerEffects") && this._teamsCount == 2) {
                    Iterator i$ = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)this.getEventData(instanceId)._holdingTeam)).getPlayers().iterator();
                    while (i$.hasNext()) {
                        PlayerEventInfo player3;
                        player3.stopAbnormalEffect((player3 = (PlayerEventInfo)i$.next()).getTeamId() == 1 ? 2097152 : 4);
                    }
                }
            }
            this.getEventData(instanceId)._holdingTime = 0;
            this.getEventData(instanceId)._holdingTeam = 0;
        }
    }

    @Override
    public String getEstimatedTimeLeft() {
        if (this._matches == null) {
            return "Starting";
        }
        for (DominationEventInstance match : this._matches.values()) {
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
        if (!map.checkForSpawns(SpawnType.Zone, -1, 1)) {
            tb.append(this.addMissingSpawn(SpawnType.Zone, 0, 1));
        }
        return tb.toString();
    }

    @Override
    protected String addExtraEventInfoCb(int instance) {
        int owningTeam = ((DominationEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._zoneData._holdingTeam;
        String status = "<font color=ac9887>Zone owned by:</font> <font color=" + EventManager.getInstance().getDarkColorForHtml(owningTeam) + ">" + EventManager.getInstance().getTeamName(owningTeam) + " team</font>";
        return "<table width=510 bgcolor=3E3E3E><tr><td width=510 align=center>" + status + "</td></tr></table>";
    }

    @Override
    public String getHtmlDescription() {
        if (this._htmlDescription == null) {
            EventDescription desc = EventDescriptionSystem.getInstance().getDescription(this.getEventType());
            if (desc != null) {
                this._htmlDescription = desc.getDescription(this.getConfigs());
            } else {
                this._htmlDescription = "" + this.getInt("teamsCount") + " teams fighting against each other. ";
                this._htmlDescription = this._htmlDescription + "The goal of this event is to capture and hold ";
                this._htmlDescription = this._htmlDescription + "a zone. The zone is represented by an NPC and to capture it, you need to stand near the NPC and ensure that no other enemies are standing near the zone too. ";
                if (this.getInt("killsForReward") > 0) {
                    this._htmlDescription = this._htmlDescription + "At least " + this.getInt("killsForReward") + " kill(s) is required to receive a reward. ";
                }
                if (this.getInt("scoreForReward") > 0) {
                    this._htmlDescription = this._htmlDescription + "At least " + this.getInt("scoreForReward") + " score (obtained when your team owns the zone and you stand near it) is required to receive a reward. ";
                }
                this._htmlDescription = this.getBoolean("waweRespawn") ? this._htmlDescription + "Dead players are resurrected by an advanced wawe-spawn engine each " + this.getInt("resDelay") + " seconds. " : this._htmlDescription + "If you die, you will get resurrected in " + this.getInt("resDelay") + " seconds. ";
                if (this.getBoolean("createParties")) {
                    this._htmlDescription = this._htmlDescription + "The event automatically creates parties on start.";
                }
            }
        }
        return this._htmlDescription;
    }

    @Override
    protected AbstractMainEvent.AbstractEventInstance getMatch(int instanceId) {
        return (AbstractMainEvent.AbstractEventInstance)this._matches.get((Object)instanceId);
    }

    @Override
    protected ZoneData createEventData(int instance) {
        return new ZoneData(instance);
    }

    @Override
    protected DominationEventInstance createEventInstance(InstanceData instance) {
        return new DominationEventInstance(instance);
    }

    @Override
    protected ZoneData getEventData(int instance) {
        return ((DominationEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._zoneData;
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

    protected class DominationEventInstance
    extends AbstractMainEvent.AbstractEventInstance {
        protected EventState _state;
        protected ZoneData _zoneData;

        protected DominationEventInstance(InstanceData instance) {
            super(instance);
            this._state = EventState.START;
            this._zoneData = Domination.this.createEventData(instance.getId());
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
                    Domination.this.print("Event: running task of state " + this._state.toString() + "...");
                }
                switch (this._state) {
                    case START: {
                        if (!Domination.this.checkPlayers(this._instance.getId())) break;
                        Domination.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, false);
                        Domination.this.setupTitles(this._instance.getId());
                        Domination.this.enableMarkers(this._instance.getId(), true);
                        Domination.this.spawnZone(this._instance.getId());
                        Domination.this.forceSitAll(this._instance.getId());
                        this.setNextState(EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        Domination.this.forceStandAll(this._instance.getId());
                        if (Domination.this.getBoolean("createParties")) {
                            Domination.this.createParties(Domination.this.getInt("maxPartySize"));
                        }
                        this.setNextState(EventState.END);
                        this._clock.startClock(Domination.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        Domination.this.unspawnZone(this._instance.getId());
                        this.setNextState(EventState.INACTIVE);
                        if (Domination.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            Domination.this.rewardAllTeams(this._instance.getId(), Domination.this.getInt("scoreForReward"), Domination.this.getInt("killsForReward"));
                        }
                        Domination.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    Domination.this.print("Event: ... finished running task. next state " + this._state.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                Domination.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    protected class ZoneData
    extends AbstractMainEvent.AbstractEventData {
        private NpcData _zone;
        private int _holdingTeam;
        private int _holdingTime;

        protected ZoneData(int instance) {
            super(instance);
        }

        protected void addZone(NpcData zone, int radius) {
            this._zone = zone;
        }

        static /* synthetic */ int access$412(ZoneData x0, int x1) {
            return x0._holdingTime+=x1;
        }
    }

}

