/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.callback.CallbackManager
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.InstanceData
 *  cz.nxs.interf.delegate.ItemData
 *  cz.nxs.interf.delegate.NpcData
 *  cz.nxs.interf.delegate.PartyData
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.text.TextBuilder
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.EventRewardSystem;
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
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IValues;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import javolution.text.TextBuilder;
import javolution.util.FastMap;

public class CaptureTheFlag
extends AbstractMainEvent {
    private FastMap<Integer, CTFEventInstance> _matches;
    private boolean _waweRespawn;
    private int _teamsCount;
    private int _flagNpcId;
    private int _holderNpcId;
    private int _flagItemId;
    private boolean _returnFlagOnDie;
    private boolean _interactDistCheck;

    public CaptureTheFlag(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Winner, RewardPosition.Looser, RewardPosition.Tie, RewardPosition.FlagScore, RewardPosition.FlagReturn, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill, RewardPosition.KillingSpree});
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.addConfig(new ConfigModel("killsForReward", "0", "The minimum kills count required to get a reward (includes all possible rewards)."));
        this.addConfig(new ConfigModel("resDelay", "15", "The delay after which the player is resurrected. In seconds."));
        this.addConfig(new ConfigModel("waweRespawn", "true", "Enables the wawe-style respawn system.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("flagSkillId", "-1", "Skill given to all players holding a flag. Possible to create for example a slow effect using a passive skill. -1 to disable."));
        this.addConfig(new ConfigModel("flagNpcId", "8990", "Flag NPC Id. Same for all teams, only title/name will change."));
        this.addConfig(new ConfigModel("flagHolderNpcId", "8991", "Flag Holder NPC Id. Same for all teams, only title/name will change."));
        this.addConfig(new ConfigModel("teamsCount", "2", "The ammount of teams in the event. Max is 3 for CTF. <font color=FF0000>In order to change the count of teams in the event, you must also edit this config in the Instance's configuration.</font>"));
        this.addConfig(new ConfigModel("afkReturnFlagTime", "99999", "The time after which will the flag be returned from AFKing player back to it's holder. -1 to disable, value in ms. NOT WORKING CURRENTLY."));
        this.addConfig(new ConfigModel("flagReturnTime", "120000", "The time after which will the flag be returned from player back to it's holder. -1 to disable, value in ms."));
        this.addConfig(new ConfigModel("createParties", "true", "Put 'True' if you want this event to automatically create parties for players in each team.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("maxPartySize", "9", "The maximum size of party, that can be created. Works only if <font color=LEVEL>createParties</font> is true."));
        this.addConfig(new ConfigModel("flagItemId", "13535", "The item ID of the flag item."));
        this.addConfig(new ConfigModel("returnFlagOnDie", "false", "Put true to return the flag when a player holding it dies back to enemy's flag holder. Put false to make it so the flag drops on ground and can be picked up by your teammate or returned by your enemy.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("npcInteractDistCheck", "false", "You can turn off/on the Flag/Holder NPC interract distance checker here.", ConfigModel.InputType.Boolean));
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
        this._flagNpcId = this.getInt("flagNpcId");
        this._holderNpcId = this.getInt("flagHolderNpcId");
        this._flagItemId = this.getInt("flagItemId");
        this._runningInstances = 0;
        this._returnFlagOnDie = this.getBoolean("returnFlagOnDie");
        this._interactDistCheck = this.getBoolean("npcInterractDistCheck");
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
            CTFEventInstance match = this.createEventInstance(instance);
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
    public void onEventEnd() {
        if (NexusLoader.detailedDebug) {
            this.print("Event: onEventEnd()");
        }
        int minKills = this.getInt("killsForReward");
        this.rewardAllTeams(-1, 0, minKills);
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
            ((CTFEventInstance)this._matches.get((Object)instance)).forceNotRewardThisInstance();
        }
        ((CTFEventInstance)this._matches.get((Object)instance)).setNextState(EventState.END);
        if (canBeAborted) {
            ((CTFEventInstance)this._matches.get((Object)instance)).setCanBeAborted();
        }
        if (canRewardIfAborted) {
            ((CTFEventInstance)this._matches.get((Object)instance)).setCanRewardIfAborted();
        }
        ((CTFEventInstance)this._matches.get((Object)instance)).scheduleNextTask(0);
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
        CTFEventInstance match = (CTFEventInstance)this._matches.get((Object)instance);
        if (count <= 3 && match != null && match.getClock() != null) {
            tb.append(LanguageEngine.getMsg("event_scorebar_time", match.getClock().getTime()));
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
    public synchronized boolean onNpcAction(PlayerEventInfo player, NpcData npc) {
        int instance = player.getInstanceId();
        CTFEventInstance match = (CTFEventInstance)this._matches.get((Object)instance);
        if (this._interactDistCheck && player.getPlanDistanceSq(npc.getLoc().getX(), npc.getLoc().getY()) > 30000.0) {
            player.sendMessage(LanguageEngine.getMsg("ctf_tooFar"));
            return false;
        }
        boolean isFlag = false;
        boolean isHolder = false;
        int npcTeam = 0;
        FlagData data = null;
        if (match._flags == null) {
            return false;
        }
        for (FlagData d : match._flags) {
            if (d.flagNpc != null && d.flagNpc.getObjectId() == npc.getObjectId()) {
                isFlag = true;
                data = d;
                npcTeam = d.team;
                continue;
            }
            if (d.flagHolder.getObjectId() != npc.getObjectId()) continue;
            isHolder = true;
            data = d;
            npcTeam = d.team;
        }
        if (data == null) {
            return false;
        }
        int status = data.status;
        if (isHolder) {
            if (npcTeam == player.getTeamId()) {
                if (status == 1) {
                    if (this.getPlayerData(player).hasFlag > 0) {
                        this.screenAnnounce(instance, LanguageEngine.getMsg("ctf_score", player.getPlayersName(), player.getEventTeam().getFullName(), this.getTeamName(this.getPlayerData(player).hasFlag)));
                        player.getEventTeam().raiseScore(1);
                        this.getPlayerData(player).raiseScore(1);
                        this.setScoreStats(player, this.getPlayerData(player).getScore());
                        EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.FlagScore, null, player.getTotalTimeAfk(), 0, 0);
                        this.returnFlag(this.getPlayerData(player).hasFlag, false, false, instance, false);
                        if (player.isTitleUpdated()) {
                            player.setTitle(this.getTitle(player), true);
                            player.broadcastTitleInfo();
                        }
                        CallbackManager.getInstance().playerFlagScores(this.getEventType(), player);
                        player.sendMessage(LanguageEngine.getMsg("ctf_score_player"));
                    } else {
                        player.sendMessage(LanguageEngine.getMsg("ctf_goForEnemyFlag"));
                    }
                    return true;
                }
                if (status == 2) {
                    player.sendMessage(LanguageEngine.getMsg("ctf_yourFlagStolen"));
                    return true;
                }
                if (status == 3) {
                    player.sendMessage(LanguageEngine.getMsg("ctf_yourFlagStolen"));
                    return true;
                }
            } else {
                if (status == 1) {
                    this.equipFlag(player, npcTeam);
                    player.creatureSay(LanguageEngine.getMsg("ctf_flagTaken"), "CTF", 15);
                    return true;
                }
                if (status == 2) {
                    player.sendMessage(LanguageEngine.getMsg("ctf_flagStolen"));
                    return true;
                }
                if (status == 3) {
                    player.sendMessage(LanguageEngine.getMsg("ctf_flagStolen"));
                    return true;
                }
            }
        } else if (isFlag) {
            if (npcTeam == player.getTeamId()) {
                if (status == 1) {
                    if (this.getPlayerData(player).hasFlag > 0) {
                        this.screenAnnounce(instance, LanguageEngine.getMsg("ctf_score", player.getPlayersName(), player.getEventTeam().getFullName(), this.getTeamName(this.getPlayerData(player).hasFlag)));
                        player.getEventTeam().raiseScore(1);
                        this.getPlayerData(player).raiseScore(1);
                        this.setScoreStats(player, this.getPlayerData(player).getScore());
                        EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.FlagScore, null, player.getTotalTimeAfk(), 0, 0);
                        this.returnFlag(this.getPlayerData(player).hasFlag, false, false, instance, false);
                        if (player.isTitleUpdated()) {
                            player.setTitle(this.getTitle(player), true);
                            player.broadcastTitleInfo();
                        }
                        CallbackManager.getInstance().playerFlagScores(this.getEventType(), player);
                        player.sendMessage(LanguageEngine.getMsg("ctf_score_player"));
                    } else {
                        player.sendMessage(LanguageEngine.getMsg("ctf_goForEnemyFlag"));
                    }
                    return true;
                }
                if (status == 3) {
                    EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.FlagReturn, null, player.getTotalTimeAfk(), 0, 0);
                    this.returnFlag(npcTeam, false, false, instance, false);
                    this.screenAnnounce(instance, LanguageEngine.getMsg("ctf_flagReturned", this.getTeamName(npcTeam), player.getPlayersName()));
                    return true;
                }
            } else {
                if (status == 1) {
                    this.equipFlag(player, npcTeam);
                    player.creatureSay(LanguageEngine.getMsg("ctf_flagTaken"), "CTF", 15);
                    return true;
                }
                if (status == 3) {
                    this.equipFlag(player, npcTeam);
                    player.creatureSay(LanguageEngine.getMsg("ctf_flagTaken"), "CTF", 15);
                    return true;
                }
            }
        }
        return false;
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
        if (this.getPlayerData(player).hasFlag > 0) {
            if (this._returnFlagOnDie) {
                this.screenAnnounce(player.getInstanceId(), LanguageEngine.getMsg("ctf_flagReturned2", this.getTeamName(this.getPlayerData(player).hasFlag)));
                EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.FlagReturn, null, player.getTotalTimeAfk(), 0, 0);
                this.returnFlag(this.getPlayerData(player).hasFlag, false, false, player.getInstanceId(), false);
            } else {
                this.dropFlag(this.getPlayerData(player).hasFlag, player.getInstanceId());
            }
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
    public boolean canUseItem(PlayerEventInfo player, ItemData item) {
        if (this.getPlayerData(player).hasFlag > 0 && (item.isWeapon() || item.isArmor() && item.getBodyPart() == CallBack.getInstance().getValues().SLOT_L_HAND()) || item.getItemId() == this._flagItemId) {
            if (!(item.getItemId() != this._flagItemId || item.isEquipped())) {
                return true;
            }
            return false;
        }
        if (this.getPlayerData(player).hasFlag == 0 && item.getItemId() == this._flagItemId) {
            if (item.isEquipped()) {
                return true;
            }
            return false;
        }
        return super.canUseItem(player, item);
    }

    @Override
    public boolean canDestroyItem(PlayerEventInfo player, ItemData item) {
        if (this.getPlayerData(player).hasFlag > 0 && item.getItemId() == this._flagItemId) {
            return false;
        }
        return super.canDestroyItem(player, item);
    }

    @Override
    public boolean canBeDisarmed(PlayerEventInfo player) {
        if (this.getPlayerData(player).hasFlag > 0) {
            return false;
        }
        return true;
    }

    private void spawnFlags(int instance) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: spawning flags for instanceId " + instance);
        }
        this.clearMapHistory(-1, SpawnType.Flag);
        CTFEventInstance match = (CTFEventInstance)this._matches.get((Object)instance);
        for (EventTeam team : ((FastMap)this._teams.get((Object)instance)).values()) {
            EventSpawn sp = this.getSpawn(SpawnType.Flag, team.getTeamId());
            CTFEventInstance.access$600((CTFEventInstance)match)[team.getTeamId() - 1].flagNpc = this.spawnNPC(sp.getLoc().getX(), sp.getLoc().getY(), sp.getLoc().getZ(), this._flagNpcId, instance, this.getTeamName(team.getTeamId()) + " Flag", this.getTeamName(team.getTeamId()) + " Team");
            CTFEventInstance.access$600((CTFEventInstance)match)[team.getTeamId() - 1].flagHolder = this.spawnNPC(sp.getLoc().getX(), sp.getLoc().getY(), sp.getLoc().getZ(), this._holderNpcId, instance, this.getTeamName(team.getTeamId()) + " Holder", "");
            CTFEventInstance.access$600((CTFEventInstance)match)[team.getTeamId() - 1].flagNpc.setEventTeam(team.getTeamId());
            CTFEventInstance.access$600((CTFEventInstance)match)[team.getTeamId() - 1].flagHolder.setEventTeam(team.getTeamId());
        }
    }

    private void unspawnFlags(int instance) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: unspawning flags for instanceId " + instance);
        }
        for (FlagData data : ((CTFEventInstance)this._matches.get((Object)instance))._flags) {
            data.returnTask.abort();
            if (data.flagNpc != null) {
                data.flagNpc.deleteMe();
            }
            if (data.flagHolder == null) continue;
            data.flagHolder.deleteMe();
        }
    }

    @Override
    public EventPlayerData createPlayerData(PlayerEventInfo player) {
        CTFEventPlayerData d = new CTFEventPlayerData(player, this);
        return d;
    }

    @Override
    public CTFEventPlayerData getPlayerData(PlayerEventInfo player) {
        return (CTFEventPlayerData)player.getEventData();
    }

    @Override
    public synchronized void clearEvent(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: called CLEAREVENT for instance " + instanceId);
        }
        try {
            if (this._matches != null) {
                for (CTFEventInstance match : this._matches.values()) {
                    if (instanceId != 0 && instanceId != match.getInstance().getId()) continue;
                    match.abort();
                    for (FlagData flag : match._flags) {
                        if (flag.flagOwner == null) continue;
                        this.unequipFlag(flag.flagOwner);
                    }
                    this.unspawnFlags(match.getInstance().getId());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            if (!player.isOnline()) continue;
            this.removeFlagFromPlayer(player);
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
    public String getEstimatedTimeLeft() {
        if (this._matches == null) {
            return "Starting";
        }
        for (CTFEventInstance match : this._matches.values()) {
            if (!match.isActive()) continue;
            return match.getClock().getTime();
        }
        return "N/A";
    }

    @Override
    protected String addExtraEventInfoCb(int instance) {
        String flags = "";
        int width = 510 / ((CTFEventInstance)this._matches.get((Object)instance))._flags.length;
        for (FlagData flag : ((CTFEventInstance)this._matches.get((Object)instance))._flags) {
            flags = flags + "<td width=" + width + " align=center><font color=" + EventManager.getInstance().getDarkColorForHtml(flag.team) + ">" + EventManager.getInstance().getTeamName(flag.team) + " flag - " + (flag.status == 1 ? "<font color=7f7f7f>Safe</font>" : "<font color=7f7f7f>Stolen!</font>") + "</font></td>";
        }
        return "<table width=510 bgcolor=3E3E3E><tr>" + flags + "</tr></table>";
    }

    @Override
    public String getHtmlDescription() {
        if (this._htmlDescription == null) {
            EventDescription desc = EventDescriptionSystem.getInstance().getDescription(this.getEventType());
            if (desc != null) {
                this._htmlDescription = desc.getDescription(this.getConfigs());
            } else {
                this._htmlDescription = "There are " + this.getInt("teamsCount") + " teams; in order to score you need to steal enemy team's flag and bring it back your team's base (to the flag holder). ";
                if (this.getInt("flagReturnTime") > -1) {
                    this._htmlDescription = this._htmlDescription + "If you hold the flag and don't manage to score within " + this.getInt("flagReturnTime") / 1000 + " seconds, the flag will be returned back to enemy's flag holder. ";
                }
                this._htmlDescription = this.getBoolean("waweRespawn") ? this._htmlDescription + "Dead players are resurrected by an advanced wawe-spawn engine each " + this.getInt("resDelay") + " seconds." : this._htmlDescription + "If you die, you will be resurrected in " + this.getInt("resDelay") + " seconds. ";
                if (this.getBoolean("createParties")) {
                    this._htmlDescription = this._htmlDescription + "The event automatically creates parties on start.";
                }
            }
        }
        return this._htmlDescription;
    }

    private void dropFlag(int flagTeam, int instance) {
        if (NexusLoader.detailedDebug) {
            this.print("/ Event: dropping flag of team " + flagTeam + " in instance " + instance);
        }
        CTFEventInstance match = (CTFEventInstance)this._matches.get((Object)instance);
        FlagData data = match._flags[flagTeam - 1];
        data.returnTask.abort();
        if (data.flagNpc != null) {
            data.flagNpc.deleteMe();
        }
        data.flagNpc = this.spawnNPC(data.flagOwner.getX(), data.flagOwner.getY(), data.flagOwner.getZ(), this._flagNpcId, instance, this.getTeamName(flagTeam) + " Flag", this.getTeamName(flagTeam) + " Team");
        data.flagNpc.setEventTeam(flagTeam);
        data.status = 3;
        this.screenAnnounce(instance, data.flagOwner.getPlayersName() + " dropped the " + this.getTeamName(flagTeam) + " flag.");
        if (data.flagOwner != null) {
            this.unequipFlag(data.flagOwner);
        }
        if (NexusLoader.detailedDebug) {
            this.print("/ Event: drop flag finished");
        }
    }

    private void returnFlag(int flagTeam, boolean timeForced, boolean afkForced, int instance, boolean announce) {
        if (NexusLoader.detailedDebug) {
            this.print("/ Event: returning flag of team " + flagTeam + " in instance " + instance + ". timeforced = " + timeForced + ", afk forced " + afkForced + ", announce " + announce);
        }
        CTFEventInstance match = (CTFEventInstance)this._matches.get((Object)instance);
        FlagData data = match._flags[flagTeam - 1];
        data.returnTask.abort();
        if (announce) {
            if (afkForced) {
                if (data.flagOwner != null) {
                    this.announce(instance, LanguageEngine.getMsg("ctf_flagReturn_afk1", data.flagOwner.getPlayersName(), this.getTeamName(flagTeam)));
                } else {
                    this.announce(instance, LanguageEngine.getMsg("ctf_flagReturn_afk2", this.getTeamName(flagTeam)));
                }
            } else if (!timeForced) {
                this.screenAnnounce(instance, LanguageEngine.getMsg("ctf_flagReturn", this.getTeamName(flagTeam)));
            } else if (data.flagOwner != null) {
                this.announce(instance, LanguageEngine.getMsg("ctf_flagReturn_timeOver1", data.flagOwner.getPlayersName(), this.getInt("flagReturnTime") / 1000, this.getTeamName(flagTeam)));
            } else {
                this.announce(instance, LanguageEngine.getMsg("ctf_flagReturn_timeOver2", this.getTeamName(flagTeam)), this.getInt("flagReturnTime") / 1000);
            }
        }
        if (data.flagOwner != null) {
            this.unequipFlag(data.flagOwner);
        }
        if (data.status == 3) {
            data.flagNpc.deleteMe();
        }
        EventSpawn sp = this.getSpawn(SpawnType.Flag, flagTeam);
        data.flagNpc = this.spawnNPC(sp.getLoc().getX(), sp.getLoc().getY(), sp.getLoc().getZ(), this._flagNpcId, instance, this.getTeamName(flagTeam) + " Flag", this.getTeamName(flagTeam) + " Team");
        data.flagNpc.setEventTeam(flagTeam);
        data.status = 1;
        if (NexusLoader.detailedDebug) {
            this.print("/ Event: return flag finished");
        }
    }

    private void equipFlag(PlayerEventInfo player, int flagTeamId) {
        if (NexusLoader.detailedDebug) {
            this.print("/ Event: equipping flag of team " + flagTeamId + " on player " + player.getPlayersName() + " (instance " + player.getInstanceId() + ")");
        }
        int instance = player.getInstanceId();
        CTFEventInstance match = (CTFEventInstance)this._matches.get((Object)instance);
        FlagData data = match._flags[flagTeamId - 1];
        ItemData wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_RHAND());
        if (wpn != null) {
            player.unEquipItemInBodySlotAndRecord(CallBack.getInstance().getValues().SLOT_R_HAND());
        }
        if ((wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_LHAND())) != null) {
            player.unEquipItemInBodySlotAndRecord(CallBack.getInstance().getValues().SLOT_L_HAND());
        }
        ItemData flagItem = player.addItem(this._flagItemId, 1, false);
        player.equipItem(flagItem);
        data.flagOwner = player;
        this.screenAnnounce(instance, LanguageEngine.getMsg("ctf_flagTaken_announce", player.getPlayersName(), this.getTeamName(flagTeamId)));
        data.flagNpc.deleteMe();
        data.status = 2;
        this.getPlayerData(player).hasFlag = flagTeamId;
        player.broadcastUserInfo();
        int id = this.getInt("flagSkillId");
        if (id != -1) {
            player.addSkill(new SkillData(id, 1), false);
        }
        data.returnTask.start();
        if (NexusLoader.detailedDebug) {
            this.print("/ Event: finished equip flag");
        }
    }

    private void unequipFlag(PlayerEventInfo player) {
        if (NexusLoader.detailedDebug) {
            this.print("/ Event: unequipping flag from " + player.getPlayersName() + " in instance " + player.getInstanceId());
        }
        this.removeFlagFromPlayer(player);
        int instance = player.getInstanceId();
        for (FlagData flag : ((CTFEventInstance)this._matches.get((Object)instance))._flags) {
            if (flag.flagOwner == null || flag.flagOwner.getPlayersId() != player.getPlayersId()) continue;
            flag.flagOwner = null;
            flag.returnTask.abort();
        }
        this.getPlayerData(player).hasFlag = 0;
        if (NexusLoader.detailedDebug) {
            this.print("/ Event: finished unequip flag");
        }
    }

    private void removeFlagFromPlayer(PlayerEventInfo player) {
        ItemData wpn;
        int id;
        if (NexusLoader.detailedDebug) {
            this.print("/ Event: removing flag from player " + player.getPlayersName() + ", instance " + player.getInstanceId());
        }
        if ((wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_RHAND())).exists()) {
            ItemData[] unequiped = player.unEquipItemInBodySlotAndRecord(wpn.getBodyPart());
            player.destroyItemByItemId(this._flagItemId, 1);
            player.inventoryUpdate(unequiped);
        }
        if ((id = this.getInt("flagSkillId")) != -1) {
            player.removeSkill(id);
        }
        if (NexusLoader.detailedDebug) {
            this.print("/ Event: finished removing flag");
        }
    }

    @Override
    public void onDisconnect(PlayerEventInfo player) {
        if (player.isOnline() && this.getPlayerData(player).hasFlag > 0) {
            if (NexusLoader.detailedDebug) {
                this.print("/ Event: removing flag from player " + player.getPlayersName() + " - he disconnected");
            }
            this.announce(LanguageEngine.getMsg("ctf_flagHolderDisconnect", player.getPlayersName(), this.getTeamName(this.getPlayerData(player).hasFlag)));
            this.screenAnnounce(player.getInstanceId(), LanguageEngine.getMsg("ctf_flagReturned2", this.getTeamName(this.getPlayerData(player).hasFlag)));
            this.returnFlag(this.getPlayerData(player).hasFlag, false, false, player.getInstanceId(), false);
        }
        super.onDisconnect(player);
    }

    public String getTeamName(int id) {
        for (FastMap i : this._teams.values()) {
            for (EventTeam team : i.values()) {
                if (team.getTeamId() != id) continue;
                return team.getTeamName();
            }
        }
        return "Unknown";
    }

    @Override
    public int getTeamsCount() {
        return this.getInt("teamsCount");
    }

    @Override
    public String getMissingSpawns(EventMap map) {
        TextBuilder tb = new TextBuilder();
        for (int i = 0; i < this.getTeamsCount(); ++i) {
            if (!map.checkForSpawns(SpawnType.Regular, i + 1, 1)) {
                tb.append(this.addMissingSpawn(SpawnType.Regular, i + 1, 1));
            }
            if (map.checkForSpawns(SpawnType.Flag, i + 1, 1)) continue;
            tb.append(this.addMissingSpawn(SpawnType.Flag, i + 1, 1));
        }
        return tb.toString();
    }

    @Override
    protected AbstractMainEvent.AbstractEventInstance getMatch(int instanceId) {
        return (AbstractMainEvent.AbstractEventInstance)this._matches.get((Object)instanceId);
    }

    @Override
    protected CTFEventInstance createEventInstance(InstanceData instance) {
        return new CTFEventInstance(instance);
    }

    @Override
    protected FlagData createEventData(int instance) {
        return new FlagData(instance);
    }

    @Override
    protected AbstractMainEvent.AbstractEventData getEventData(int instance) {
        return null;
    }

    private class FlagReturnTask
    implements Runnable {
        private final int team;
        private int instance;
        private ScheduledFuture<?> future;

        public FlagReturnTask(int team, int instance) {
            this.team = team;
            this.instance = instance;
        }

        public void start() {
            int time;
            if (this.future != null) {
                this.future.cancel(false);
            }
            this.future = (time = CaptureTheFlag.this.getInt("flagReturnTime")) > 0 ? CallBack.getInstance().getOut().scheduleGeneral(this, time) : null;
        }

        public void abort() {
            if (this.future != null) {
                this.future.cancel(false);
            }
        }

        @Override
        public void run() {
            CaptureTheFlag.this.returnFlag(this.team, true, false, this.instance, true);
        }
    }

    public class CTFEventPlayerData
    extends PvPEventPlayerData {
        private int hasFlag;

        public CTFEventPlayerData(PlayerEventInfo owner, EventGame event) {
            super(owner, event, new GlobalStatsModel(CaptureTheFlag.this.getEventType()));
            this.hasFlag = 0;
        }

        public void setHasFlag(int b) {
            this.hasFlag = b;
        }

        public int hasFlag() {
            return this.hasFlag;
        }
    }

    private static enum EventState {
        START,
        FIGHT,
        END,
        TELEPORT,
        INACTIVE;
        

        private EventState() {
        }
    }

    private class CTFEventInstance
    extends AbstractMainEvent.AbstractEventInstance {
        private EventState _state;
        private FlagData[] _flags;

        private CTFEventInstance(InstanceData instance) {
            super(instance);
            this._state = EventState.START;
            this._flags = new FlagData[CaptureTheFlag.this._teamsCount];
            for (EventTeam team : ((FastMap)CaptureTheFlag.this._teams.get((Object)instance.getId())).values()) {
                this._flags[team.getTeamId() - 1] = new FlagData(instance.getId());
                this._flags[team.getTeamId() - 1].setTeam(team.getTeamId());
            }
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
                    CaptureTheFlag.this.print("Event: running task of state " + this._state.toString() + "...");
                }
                switch (this._state) {
                    case START: {
                        if (!CaptureTheFlag.this.checkPlayers(this._instance.getId())) break;
                        CaptureTheFlag.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, false);
                        CaptureTheFlag.this.setupTitles(this._instance.getId());
                        CaptureTheFlag.this.enableMarkers(this._instance.getId(), true);
                        CaptureTheFlag.this.spawnFlags(this._instance.getId());
                        CaptureTheFlag.this.forceSitAll(this._instance.getId());
                        this.setNextState(EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        CaptureTheFlag.this.forceStandAll(this._instance.getId());
                        if (CaptureTheFlag.this.getBoolean("createParties")) {
                            CaptureTheFlag.this.createParties(CaptureTheFlag.this.getInt("maxPartySize"));
                        }
                        this.setNextState(EventState.END);
                        this._clock.startClock(CaptureTheFlag.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        CaptureTheFlag.this.unspawnFlags(this._instance.getId());
                        this.setNextState(EventState.INACTIVE);
                        if (CaptureTheFlag.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            CaptureTheFlag.this.rewardAllTeams(this._instance.getId(), 0, CaptureTheFlag.this.getInt("killsForReward"));
                        }
                        CaptureTheFlag.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    CaptureTheFlag.this.print("Event: ... finished running task. next state " + this._state.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                CaptureTheFlag.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    private class FlagData
    extends AbstractMainEvent.AbstractEventData {
        public int _instance;
        public int team;
        public NpcData flagNpc;
        public NpcData flagHolder;
        public int status;
        public PlayerEventInfo flagOwner;
        public FlagReturnTask returnTask;

        public FlagData(int instance) {
            super(instance);
            this._instance = instance;
            this.status = 1;
            this.flagNpc = null;
            this.flagHolder = null;
            this.flagOwner = null;
        }

        private void setTeam(int team) {
            this.team = team;
            this.returnTask = new FlagReturnTask(team, this._instance);
        }
    }

}

