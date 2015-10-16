/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
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
package cz.nxs.events.engine.main.events.template;

import cz.nxs.events.EventGame;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.PvPEventPlayerData;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.base.SpawnType;
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
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import javolution.text.TextBuilder;
import javolution.util.FastMap;

public class FFAEventTemplate
extends AbstractMainEvent {
    private FastMap<Integer, CustomEventInstance> _matches;
    private boolean _waweRespawn;
    private int _teamsCount;

    @Override
    protected CustomEventData createEventData(int instanceId) {
        return new CustomEventData(instanceId);
    }

    @Override
    protected CustomEventInstance createEventInstance(InstanceData instance) {
        return new CustomEventInstance(instance);
    }

    @Override
    protected CustomEventData getEventData(int instance) {
        return ((CustomEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._data;
    }

    public FFAEventTemplate(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Winner, RewardPosition.Looser, RewardPosition.Tie, RewardPosition.OnKill});
        this.addConfig(new ConfigModel("killsForReward", "0", "The minimum kills count required to get a reward (includes all possible rewards)."));
        this.addConfig(new ConfigModel("resDelay", "15", "The delay after which the player is resurrected. In seconds."));
        this.addConfig(new ConfigModel("waweRespawn", "true", "Enables the wawe-style respawn system.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("createParties", "true", "Put 'True' if you want this event to automatically create parties for players in each team.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("maxPartySize", "10", "The maximum size of party, that can be created. Works only if <font color=LEVEL>createParties</font> is true."));
        this.addConfig(new ConfigModel("teamsCount", "2", "The ammount of teams in the event. Max is 5."));
        this.addInstanceTypeConfig(new ConfigModel("teamsCount", "2", "You may specify the count of teams only for this instance. This config overrides event's default teams ammount."));
    }

    @Override
    public void initEvent() {
        super.initEvent();
        this._waweRespawn = this.getBoolean("waweRespawn");
        if (this._waweRespawn) {
            this.initWaweRespawns(this.getInt("resDelay"));
        }
        this._runningInstances = 0;
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
        if (!this.dividePlayers()) {
            this.clearEvent();
            return;
        }
        this._matches = new FastMap();
        for (InstanceData instance : this._instances) {
            CustomEventInstance match = new CustomEventInstance(instance);
            this._matches.put((Object)instance.getId(), (Object)match);
            ++this._runningInstances;
            match.scheduleNextTask(0);
        }
    }

    @Override
    public void onEventEnd() {
        int minKills = this.getInt("killsForReward");
        int minScore = this.getInt("scoreForReward");
        this.rewardAllTeams(-1, minScore, minKills);
    }

    @Override
    protected synchronized boolean instanceEnded() {
        --this._runningInstances;
        if (this._runningInstances == 0) {
            this._manager.end();
            return true;
        }
        return false;
    }

    @Override
    protected synchronized void endInstance(int instance, boolean canBeAborted, boolean canRewardIfAborted, boolean forceNotReward) {
        if (forceNotReward) {
            ((CustomEventInstance)this._matches.get((Object)instance)).forceNotRewardThisInstance();
        }
        ((CustomEventInstance)this._matches.get((Object)instance)).setNextState(EventState.END);
        if (canBeAborted) {
            ((CustomEventInstance)this._matches.get((Object)instance)).setCanBeAborted();
        }
        if (canRewardIfAborted) {
            ((CustomEventInstance)this._matches.get((Object)instance)).setCanRewardIfAborted();
        }
        ((CustomEventInstance)this._matches.get((Object)instance)).scheduleNextTask(0);
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
            tb.append(LanguageEngine.getMsg("event_scorebar_time", ((CustomEventInstance)this._matches.get((Object)instance)).getClock().getTime()));
        }
        return tb.toString();
    }

    @Override
    protected String getTitle(PlayerEventInfo pi) {
        if (pi.isAfk()) {
            return "AFK";
        }
        return "Kills: " + this.getPlayerData(pi).getScore() + " Deaths: " + this.getPlayerData(pi).getDeaths();
    }

    @Override
    public void onKill(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null) {
            return;
        }
        if (player.getTeamId() != target.getEventInfo().getTeamId()) {
            this.giveOnKillReward(player);
            player.getEventTeam().raiseScore(1);
            player.getEventTeam().raiseKills(1);
            this.getPlayerData(player).raiseScore(1);
            this.getPlayerData(player).raiseKills(1);
            this.getPlayerData(player).raiseSpree(1);
            if (player.isTitleUpdated()) {
                player.setTitle(this.getTitle(player), true);
                player.broadcastTitleInfo();
            }
            CallbackManager.getInstance().playerKills(this.getEventType(), player, target.getEventInfo());
            this.setScoreStats(player, this.getPlayerData(player).getScore());
            this.setKillsStats(player, this.getPlayerData(player).getKills());
        }
    }

    @Override
    public void onDie(PlayerEventInfo player, CharacterData killer) {
        this.getPlayerData(player).raiseDeaths(1);
        this.setDeathsStats(player, this.getPlayerData(player).getDeaths());
        if (this._waweRespawn) {
            this._waweScheduler.addPlayer(player);
        } else {
            this.scheduleRevive(player, this.getInt("resDelay") * 1000);
        }
    }

    private void spawnStuff(int instanceId) {
    }

    private void unspawnStuff(int instanceId) {
    }

    @Override
    public EventPlayerData createPlayerData(PlayerEventInfo player) {
        CustomEventPlayerData d = new CustomEventPlayerData(player, this);
        return d;
    }

    @Override
    public CustomEventPlayerData getPlayerData(PlayerEventInfo player) {
        return (CustomEventPlayerData)player.getEventData();
    }

    @Override
    public synchronized void clearEvent(int instanceId) {
        try {
            if (this._matches != null) {
                for (CustomEventInstance match : this._matches.values()) {
                    if (instanceId != 0 && instanceId != match.getInstance().getId()) continue;
                    match.abort();
                    this.unspawnStuff(instanceId);
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
        this.clearEvent(0);
    }

    @Override
    protected void respawnPlayer(PlayerEventInfo pi, int instance) {
        EventSpawn spawn = this.getSpawn(SpawnType.Regular, pi.getTeamId());
        if (spawn != null) {
            Loc loc = new Loc(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ());
            loc.addRadius(spawn.getRadius());
            pi.teleport(loc, 0, true, instance);
            pi.sendMessage(LanguageEngine.getMsg("event_respawned"));
        } else {
            this.debug("Error on respawnPlayer - no spawn type REGULAR, team " + pi.getTeamId() + " has been found. Event aborted.");
        }
    }

    @Override
    public void onDisconnect(PlayerEventInfo player) {
        if (player.isOnline()) {
            // empty if block
        }
        super.onDisconnect(player);
    }

    @Override
    protected boolean checkIfEventCanContinue(int instanceId, PlayerEventInfo disconnectedPlayer) {
        return super.checkIfEventCanContinue(instanceId, disconnectedPlayer);
    }

    @Override
    protected void clockTick() throws Exception {
    }

    @Override
    public boolean onSay(PlayerEventInfo player, String text, int channel) {
        return true;
    }

    @Override
    public boolean onNpcAction(PlayerEventInfo player, NpcData npc) {
        return false;
    }

    @Override
    public void onDamageGive(CharacterData cha, CharacterData target, int damage, boolean isDOT) {
        super.onDamageGive(cha, target, damage, isDOT);
    }

    @Override
    public boolean canSupport(PlayerEventInfo player, CharacterData target) {
        return super.canSupport(player, target);
    }

    @Override
    public boolean canAttack(PlayerEventInfo player, CharacterData target) {
        return super.canAttack(player, target);
    }

    @Override
    public boolean canUseItem(PlayerEventInfo player, ItemData item) {
        return super.canUseItem(player, item);
    }

    @Override
    public boolean canDestroyItem(PlayerEventInfo player, ItemData item) {
        return super.canDestroyItem(player, item);
    }

    @Override
    public void onItemUse(PlayerEventInfo player, ItemData item) {
        super.onItemUse(player, item);
    }

    @Override
    public boolean canUseSkill(PlayerEventInfo player, SkillData skill) {
        return super.canUseSkill(player, skill);
    }

    @Override
    public void onSkillUse(PlayerEventInfo player, SkillData skill) {
        super.onSkillUse(player, skill);
    }

    @Override
    public String getEstimatedTimeLeft() {
        if (this._matches == null) {
            return "Starting";
        }
        for (CustomEventInstance match : this._matches.values()) {
            if (!match.isActive()) continue;
            return match.getClock().getTime();
        }
        return "N/A";
    }

    @Override
    public int getTeamsCount() {
        return 1;
    }

    @Override
    public String getMissingSpawns(EventMap map) {
        if (!map.checkForSpawns(SpawnType.Regular, -1, 1)) {
            return this.addMissingSpawn(SpawnType.Regular, 0, 1);
        }
        return "";
    }

    @Override
    public String getHtmlDescription() {
        if (this._htmlDescription == null) {
            this._htmlDescription = "No information about this event.";
        }
        return this._htmlDescription;
    }

    @Override
    protected AbstractMainEvent.AbstractEventInstance getMatch(int instanceId) {
        return (AbstractMainEvent.AbstractEventInstance)this._matches.get((Object)instanceId);
    }

    public class CustomEventPlayerData
    extends PvPEventPlayerData {
        public CustomEventPlayerData(PlayerEventInfo owner, EventGame event) {
            super(owner, event, new GlobalStatsModel(FFAEventTemplate.this.getEventType()));
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

    private class CustomEventInstance
    extends AbstractMainEvent.AbstractEventInstance {
        protected EventState _state;
        protected CustomEventData _data;

        private CustomEventInstance(InstanceData instance) {
            super(instance);
            this._state = EventState.START;
            this._data = FFAEventTemplate.this.createEventData(instance.getId());
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
                switch (this._state) {
                    case START: {
                        if (!FFAEventTemplate.this.checkPlayers(this._instance.getId())) break;
                        FFAEventTemplate.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, false);
                        FFAEventTemplate.this.setupTitles(this._instance.getId());
                        FFAEventTemplate.this.spawnStuff(this._instance.getId());
                        FFAEventTemplate.this.forceSitAll(this._instance.getId());
                        this.setNextState(EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        FFAEventTemplate.this.forceStandAll(this._instance.getId());
                        this.setNextState(EventState.END);
                        this._clock.startClock(FFAEventTemplate.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        FFAEventTemplate.this.unspawnStuff(this._instance.getId());
                        this.setNextState(EventState.INACTIVE);
                        if (FFAEventTemplate.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            FFAEventTemplate.this.rewardAllTeams(this._instance.getId(), FFAEventTemplate.this.getInt("scoreForReward"), FFAEventTemplate.this.getInt("killsForReward"));
                        }
                        FFAEventTemplate.this.clearEvent(this._instance.getId());
                    }
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                FFAEventTemplate.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    private class CustomEventData
    extends AbstractMainEvent.AbstractEventData {
        public CustomEventData(int instance) {
            super(instance);
        }
    }

}

