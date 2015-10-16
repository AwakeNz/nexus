/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.callback.CallbackManager
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.InstanceData
 *  cz.nxs.interf.delegate.PartyData
 *  javolution.text.TextBuilder
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
import cz.nxs.events.engine.html.EventHtmlManager;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.base.MainEventInstanceType;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.PartyData;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import javolution.text.TextBuilder;
import javolution.util.FastMap;

public class Deathmatch
extends AbstractMainEvent {
    protected FastMap<Integer, DMEventInstance> _matches;
    protected boolean _waweRespawn;
    protected boolean _antifeed;

    public Deathmatch(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Looser, RewardPosition.Tie, RewardPosition.Numbered, RewardPosition.Range, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill, RewardPosition.KillingSpree});
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.addConfig(new ConfigModel("killsForReward", "0", "The minimum kills count required to get a reward (includes all possible rewards)."));
        this.addConfig(new ConfigModel("resDelay", "15", "The delay after which the player is resurrected. In seconds."));
        this.addConfig(new ConfigModel("waweRespawn", "true", "Enables the wawe-style respawn system.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("firstBloodMessage", "true", "You can turn off/on the first blood announce in the event (first kill made in the event). This is also rewardable - check out reward type FirstBlood.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("antifeedProtection", "true", "Enables the special anti-feed protection. This protection changes player's name, title, race, clan/ally crest, class and basically all of his apperance, sometimes also gender.", ConfigModel.InputType.Boolean));
    }

    @Override
    public void initEvent() {
        super.initEvent();
        this._waweRespawn = this.getBoolean("waweRespawn");
        this._antifeed = this.getBoolean("antifeedProtection");
        if (this._waweRespawn) {
            this.initWaweRespawns(this.getInt("resDelay"));
        }
        this._runningInstances = 0;
    }

    @Override
    protected int initInstanceTeams(MainEventInstanceType type, int instanceId) {
        this.createTeams(1, type.getInstance().getId());
        return 1;
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
            DMEventInstance match = this.createEventInstance(instance);
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
        this.rewardAllPlayers(-1, minKills, minKills);
        if (this._antifeed) {
            for (PlayerEventInfo player : this.getPlayers(0)) {
                player.stopAntifeedProtection(false);
            }
        }
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
            ((DMEventInstance)this._matches.get((Object)instance)).forceNotRewardThisInstance();
        }
        ((DMEventInstance)this._matches.get((Object)instance)).setNextState(EventState.END);
        if (canBeAborted) {
            ((DMEventInstance)this._matches.get((Object)instance)).setCanBeAborted();
        }
        if (canRewardIfAborted) {
            ((DMEventInstance)this._matches.get((Object)instance)).setCanRewardIfAborted();
        }
        ((DMEventInstance)this._matches.get((Object)instance)).scheduleNextTask(0);
    }

    @Override
    protected String getScorebar(int instance) {
        TextBuilder tb = new TextBuilder();
        int top = 0;
        for (PlayerEventInfo player : this.getPlayers(instance)) {
            if (this.getPlayerData(player).getKills() <= top) continue;
            top = this.getPlayerData(player).getKills();
        }
        tb.append(LanguageEngine.getMsg("dm_topKills", top) + " ");
        tb.append("   " + LanguageEngine.getMsg("event_scorebar_time", ((DMEventInstance)this._matches.get((Object)instance)).getClock().getTime()));
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
        return LanguageEngine.getMsg("event_title_pvppk", this.getPlayerData(pi).getScore(), this.getPlayerData(pi).getDeaths());
    }

    @Override
    public void onKill(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null) {
            return;
        }
        this.tryFirstBlood(player);
        this.giveOnKillReward(player);
        this.getPlayerData(player).raiseScore(1);
        this.getPlayerData(player).raiseKills(1);
        this.getPlayerData(player).raiseSpree(1);
        this.giveKillingSpreeReward(this.getPlayerData(player));
        if (player.isTitleUpdated()) {
            player.setTitle(this.getTitle(player), true);
            player.broadcastTitleInfo();
        }
        CallbackManager.getInstance().playerKills(this.getEventType(), player, target.getEventInfo());
        this.setScoreStats(player, this.getPlayerData(player).getScore());
        this.setKillsStats(player, this.getPlayerData(player).getKills());
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
    public boolean canSupport(PlayerEventInfo player, CharacterData target) {
        if (player.getPlayersId() == target.getObjectId()) {
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
        return true;
    }

    @Override
    public boolean onSay(PlayerEventInfo player, String text, int channel) {
        if (text.equals(".scheme")) {
            EventManager.getInstance().getHtmlManager().showSelectSchemeForEventWindow(player, "none", this.getEventType().getAltTitle());
            return false;
        }
        if (this._antifeed) {
            player.sendMessage(LanguageEngine.getMsg("dm_cantChat"));
            return false;
        }
        return true;
    }

    @Override
    public boolean canInviteToParty(PlayerEventInfo player, PlayerEventInfo target) {
        return false;
    }

    @Override
    protected boolean checkIfEventCanContinue(int instanceId, PlayerEventInfo disconnectedPlayer) {
        int alive = 0;
        for (PlayerEventInfo pi : this.getPlayers(instanceId)) {
            if (pi == null || !pi.isOnline()) continue;
            ++alive;
        }
        return alive >= 2;
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
                for (DMEventInstance match : this._matches.values()) {
                    if (instanceId != 0 && instanceId != match.getInstance().getId()) continue;
                    match.abort();
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            if (!player.isOnline()) continue;
            if (player.hasAntifeedProtection()) {
                player.stopAntifeedProtection(false);
            }
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
        if ((spawn = this.getSpawn(SpawnType.Regular, -1)) != null) {
            Loc loc = new Loc(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ());
            loc.addRadius(spawn.getRadius());
            pi.teleport(loc, 0, true, instance);
            pi.sendMessage(LanguageEngine.getMsg("event_respawned"));
        } else {
            this.debug("Error on respawnPlayer - no spawn type REGULAR, team -1 (FFA) has been found. Event aborted.");
        }
    }

    @Override
    public String getEstimatedTimeLeft() {
        if (this._matches == null) {
            return "Starting";
        }
        for (DMEventInstance match : this._matches.values()) {
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
    protected String addExtraEventInfoCb(int instance) {
        int top = 0;
        for (PlayerEventInfo player : this.getPlayers(instance)) {
            if (this.getPlayerData(player).getKills() <= top) continue;
            top = this.getPlayerData(player).getKills();
        }
        String status = "<font color=ac9887>Top kills count: </font><font color=7f7f7f>" + top + "</font>";
        return "<table width=510 bgcolor=3E3E3E><tr><td width=510 align=center>" + status + "</td></tr></table>";
    }

    @Override
    public String getHtmlDescription() {
        if (this._htmlDescription == null) {
            EventDescription desc = EventDescriptionSystem.getInstance().getDescription(this.getEventType());
            if (desc != null) {
                this._htmlDescription = desc.getDescription(this.getConfigs());
            } else {
                this._htmlDescription = "This is a free-for-all event, don't expect any help from teammates. Gain score by killing your opponents";
                this._htmlDescription = this._htmlDescription + " and if you die, you will be resurrected within " + this.getInt("resDelay") + " seconds. ";
                if (this.getBoolean("waweRespawn")) {
                    this._htmlDescription = this._htmlDescription + "Also, wawe-spawn system ensures that all dead players are spawned in the same moment (but in different spots). ";
                }
                if (this.getBoolean("antifeedProtection")) {
                    this._htmlDescription = this._htmlDescription + "This event has a protection, which completely changes the appearance of all players and temporary removes their title and clan/ally crests. ";
                }
                if (this.getInt("killsForReward") > 0) {
                    this._htmlDescription = this._htmlDescription + "In the end, you need at least " + this.getInt("killsForReward") + " kills to receive a reward.";
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
    protected DMData createEventData(int instance) {
        return new DMData(instance);
    }

    @Override
    protected DMEventInstance createEventInstance(InstanceData instance) {
        return new DMEventInstance(instance);
    }

    @Override
    protected DMData getEventData(int instance) {
        return ((DMEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._data;
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

    protected class DMEventInstance
    extends AbstractMainEvent.AbstractEventInstance {
        protected EventState _nextState;
        protected DMData _data;

        public DMEventInstance(InstanceData instance) {
            super(instance);
            this._nextState = EventState.START;
            this._data = Deathmatch.this.createEventData(this._instance.getId());
        }

        protected void setNextState(EventState state) {
            this._nextState = state;
        }

        @Override
        public boolean isActive() {
            return this._nextState != EventState.INACTIVE;
        }

        @Override
        public void run() {
            try {
                if (NexusLoader.detailedDebug) {
                    Deathmatch.this.print("Event: running task of state " + this._nextState.toString() + "...");
                }
                switch (this._nextState) {
                    case START: {
                        if (!Deathmatch.this.checkPlayers(this._instance.getId())) break;
                        if (Deathmatch.this._antifeed) {
                            for (PlayerEventInfo player : Deathmatch.this.getPlayers(this._instance.getId())) {
                                player.startAntifeedProtection(false);
                            }
                        }
                        Deathmatch.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, true);
                        Deathmatch.this.setupTitles(this._instance.getId());
                        Deathmatch.this.removeStaticDoors(this._instance.getId());
                        Deathmatch.this.enableMarkers(this._instance.getId(), true);
                        Deathmatch.this.forceSitAll(this._instance.getId());
                        this.setNextState(EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        Deathmatch.this.forceStandAll(this._instance.getId());
                        this.setNextState(EventState.END);
                        this._clock.startClock(Deathmatch.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        this.setNextState(EventState.INACTIVE);
                        if (Deathmatch.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            Deathmatch.this.rewardAllPlayers(this._instance.getId(), 0, Deathmatch.this.getInt("killsForReward"));
                        }
                        Deathmatch.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    Deathmatch.this.print("Event: ... finished running task. next state " + this._nextState.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                Deathmatch.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    protected class DMData
    extends AbstractMainEvent.AbstractEventData {
        protected DMData(int instance) {
            super(instance);
        }
    }

}

