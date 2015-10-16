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
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventBuffer;
import cz.nxs.events.engine.EventWarnings;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.PvPEventPlayerData;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.base.description.EventDescription;
import cz.nxs.events.engine.base.description.EventDescriptionSystem;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.main.events.Deathmatch;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IPlayerBase;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class LastManStanding
extends Deathmatch {
    private int _roundWaitTime;
    private int _maxRounds;
    private int _roundTimeLimit;
    private int _scoreForRoundWinner;
    private boolean _disableAnnouncingCountdown;
    private String[] _scorebarFormat;

    public LastManStanding(EventType type, MainEventManager manager) {
        super(type, manager);
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        ConfigModel scorebarFormat = new ConfigModel("screenScoreBarFormat", "AliveAndRounds", "Specify here how will the player's screen Score bar look like. <br1><font color=LEVEL>Alive</font> shows the count of players that are still alive, excluding you.<br1><font color=LEVEL>Time</font> shows the time left for the event (using Run time value)<br1><font color=LEVEL>Rounds</font> shows the current round / max rounds in the event.<br1><font color=LEVEL>Top</font> shows the score of the top player in the event.<br1>Example: <font color=LEVEL>AliveAndRounds</font> will show following text: 'Alive: 12, Round: 1/3', where 12 is the count of alive players excluding you, 1 is the current round and 3 si the total count of rounds in this event (configurable).", ConfigModel.InputType.Enum);
        scorebarFormat.addEnumOptions(new String[]{"Alive", "Rounds", "Time", "Top", "AliveAndRounds", "AliveAndTime", "AliveAndTop", "RoundsAndTime", "RoundsAndTop", "TopAndTime"});
        this.addConfig(scorebarFormat);
        this.removeConfig("runTime");
        this.removeConfig("rejoinAfterDisconnect");
        this.removeConfig("removeWarningAfterRejoin");
        this.addConfig(new ConfigModel("runTime", "30", "The run time of this event, launched automatically by the scheduler. Max value globally for all events is 120 minutes. <font color=699768>It is recommended to use a higher run time (30+ minutes) in combination with lower value of </font><font color=LEVEL>maxRounds</font> <font color=699768>(3-5).</font> In minutes!"));
        this.addConfig(new ConfigModel("maxRounds", "3", "The maximum count of rounds that will be runned in this event. One round ends when there's only one player alive. If an event instance reaches this rounds limit, the event instance will end. The event ends (meaning you can start/schedule a new event) only when all event instances have ended."));
        this.addConfig(new ConfigModel("roundTimeLimit", "600", "The time after it automatically ends current round. Useful to prevent afking on events or if any stupid player don't know what to do (even tho if a player goes afk, he will be killed automatically). In seconds."));
        this.addConfig(new ConfigModel("scoreForRoundWinner", "3", "Number of score points given to a round winner (the only player who survived). Remember, that one kill = 1 score."));
        this.addConfig(new ConfigModel("roundWaitTime", "5", "The time players have to wait when a new round started. They are rooted and can't attack anyone. There's a countdown too. This is here because it looks cool."));
        this.addConfig(new ConfigModel("disableCountdown", "true", "Put true to disable classic event's end countdown announcement. Good if you want to have this event only round-based, like it ends after 3 rounds and not look like there's a 20 minutes limit. Putting high run time (eg. 30 minutes) and lower rounds count (3-5) is recommended for this event.", ConfigModel.InputType.Boolean));
    }

    @Override
    public void initEvent() {
        super.initEvent();
        this._maxRounds = this.getInt("maxRounds");
        this._roundTimeLimit = this.getInt("roundTimeLimit");
        this._scoreForRoundWinner = this.getInt("scoreForRoundWinner");
        this._roundWaitTime = this.getInt("roundWaitTime");
        this._disableAnnouncingCountdown = this.getBoolean("disableCountdown");
        this._scorebarFormat = this.getString("screenScoreBarFormat").split("And");
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
            LMSEventInstance match = this.createEventInstance(instance);
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

    private void startRound(final int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: trying to start new round for instance " + instanceId);
        }
        if (this.getEventData(instanceId).canStartNewRound()) {
            if (NexusLoader.detailedDebug) {
                this.print("Event: starting new round; current round = " + this.getEventData(instanceId)._round);
            }
            this.getEventData(instanceId)._roundActive = true;
            this.getEventData(instanceId)._alivePlayers = this.getPlayers(instanceId).size();
            for (PlayerEventInfo player : this.getPlayers(instanceId)) {
                this.respawnPlayer(player, instanceId);
                if (!this._allowSchemeBuffer) continue;
                EventBuffer.getInstance().buffPlayer(player, true);
            }
            this.getEventData(instanceId).newRound();
            this.getEventData(instanceId).setWaitingState(true);
            this.waitingStateEffects(instanceId, true);
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    if (LastManStanding.this.getEventData(instanceId)._isActive && ((Deathmatch.DMEventInstance)LastManStanding.this._matches.get((Object)Integer.valueOf((int)instanceId)))._nextState == Deathmatch.EventState.END) {
                        LastManStanding.this.announce(instanceId, LanguageEngine.getMsg("lms_roundStarted", LastManStanding.this.getEventData(instanceId)._round));
                        LastManStanding.this.getEventData(instanceId).setWaitingState(false);
                        LastManStanding.this.waitingStateEffects(instanceId, false);
                    }
                }
            }, this._roundWaitTime * 1000);
            if (NexusLoader.detailedDebug) {
                this.print("Event: new round started!");
            }
        } else {
            if (NexusLoader.detailedDebug) {
                this.print("Event: CAN'T START new round!");
            }
            this.announce(instanceId, "Configs are wrong for Last Man Standing event. Event aborted until fixed.");
            NexusLoader.debug((String)"Rounds count config for LMS must be at least 1. Event has been aborted", (Level)Level.WARNING);
            this.endInstance(instanceId, true, false, true);
        }
    }

    private void waitingStateEffects(int instance, boolean apply) {
        for (PlayerEventInfo player : this.getPlayers(instance)) {
            player.setIsParalyzed(apply);
            player.paralizeEffect(apply);
            player.setIsInvul(apply);
        }
    }

    private synchronized void endRound(final int instanceId, boolean aborted, boolean endInstance) {
        if (!this.getEventData(instanceId)._roundActive) {
            return;
        }
        if (NexusLoader.detailedDebug) {
            this.print("Event: ending round of instance " + instanceId + " aborted = " + aborted + ", end instance " + endInstance);
        }
        this.getEventData(instanceId)._roundActive = false;
        PlayerEventInfo winner = null;
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            if (player.isDead()) continue;
            winner = player;
        }
        if (!(aborted || winner == null)) {
            this.getPlayerData(winner).raiseScore(this._scoreForRoundWinner);
            this.setScoreStats(winner, this.getPlayerData(winner).getScore());
            this.announce(instanceId, LanguageEngine.getMsg("lms_roundWon", winner.getPlayersName(), this.getEventData(instanceId)._round));
        }
        if (this.getEventData(instanceId).canStartNewRound() && !endInstance) {
            this.announce(instanceId, LanguageEngine.getMsg("lms_roundStartsIn", 10));
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    if (LastManStanding.this.getEventData(instanceId).isActive() && ((Deathmatch.DMEventInstance)LastManStanding.this._matches.get((Object)Integer.valueOf((int)instanceId)))._nextState == Deathmatch.EventState.END) {
                        LastManStanding.this.startRound(instanceId);
                    }
                }
            }, 10000);
        } else {
            this.announce(instanceId, LanguageEngine.getMsg("lms_eventEnded"));
            Object i$ = this.getEventData(instanceId);
            synchronized (i$) {
                this.getEventData(instanceId).setInactive();
            }
            this.endInstance(instanceId, true, true, false);
        }
    }

    private void endRoundDueToTime(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: end due to round time = " + instanceId);
        }
        this.announce(instanceId, LanguageEngine.getMsg("lms_roundAborted_timeLimit", this._roundTimeLimit / 60));
        this.endRound(instanceId, true, false);
    }

    private void endRoundDueToEventTimeLimit(int instanceId, boolean announceTimeLimit) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: ending round due to event time limit " + instanceId + ", announce time limit = " + announceTimeLimit);
        }
        LMSData lMSData = this.getEventData(instanceId);
        synchronized (lMSData) {
            this.getEventData(instanceId).setInactive();
        }
        if (announceTimeLimit) {
            this.announce(instanceId, LanguageEngine.getMsg("lms_roundAborted"));
        }
        this.endRound(instanceId, true, true);
    }

    @Override
    public void onEventEnd() {
        if (NexusLoader.detailedDebug) {
            this.print("Event: onEventEnd()");
        }
        for (Deathmatch.DMEventInstance match : this._matches.values()) {
            if (!this.getEventData(match.getInstance().getId()).isActive()) continue;
            this.endRoundDueToEventTimeLimit(match.getInstance().getId(), true);
        }
        super.onEventEnd();
    }

    @Override
    protected String getScorebar(int instance) {
        int countAlive = this.getEventData(instance)._alivePlayers - 1;
        String time = ((Deathmatch.DMEventInstance)this._matches.get((Object)instance)).getClock().getTime();
        String rounds = "" + this.getEventData(instance)._round + "/" + this._maxRounds;
        int top = 0;
        for (PlayerEventInfo player : this.getPlayers(instance)) {
            if (this.getPlayerData(player).getScore() <= top) continue;
            top = this.getPlayerData(player).getScore();
        }
        TextBuilder tb = new TextBuilder();
        String[] types = this._scorebarFormat;
        for (int i = 0; i < types.length; ++i) {
            String type = types[i];
            if (type.equals("Alive")) {
                tb.append(LanguageEngine.getMsg("lms_scorebar_alive") + " " + countAlive);
            } else if (type.equals("Time")) {
                tb.append(LanguageEngine.getMsg("event_scorebar_time", time));
            } else if (type.equals("Rounds")) {
                tb.append(LanguageEngine.getMsg("lms_scorebar_rounds") + " " + rounds);
            } else if (type.equals("Top")) {
                tb.append(LanguageEngine.getMsg("lms_scorebar_top") + " " + top);
            }
            if (i + 1 >= types.length) continue;
            tb.append("  ");
        }
        return tb.toString();
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
        if (player.isTitleUpdated()) {
            player.setTitle(this.getTitle(player), true);
            player.broadcastTitleInfo();
        }
        CallbackManager.getInstance().playerKills(this.getEventType(), player, target.getEventInfo());
        this.setScoreStats(player, this.getPlayerData(player).getScore());
        this.setKillsStats(player, this.getPlayerData(player).getKills());
    }

    @Override
    public synchronized void onDie(PlayerEventInfo player, CharacterData killer) {
        if (NexusLoader.detailedDebug) {
            this.print("/// Event: onDie - player " + player.getPlayersName() + " (instance " + player.getInstanceId() + "), killer " + killer.getName());
        }
        this.getPlayerData(player).raiseDeaths(1);
        this.getPlayerData(player).setSpree(0);
        player.disableAfkCheck(true);
        this.setDeathsStats(player, this.getPlayerData(player).getDeaths());
        if (this.getEventData(player.getInstanceId()).playerDied()) {
            this.endRound(player.getInstanceId(), false, false);
        } else if (this.getEventData(player.getInstanceId()).canStartNewRound()) {
            player.sendMessage(LanguageEngine.getMsg("lms_notifyPlayerRespawn"));
        }
    }

    @Override
    public void playerWentAfk(PlayerEventInfo player, boolean warningOnly, int afkTime) {
        if (warningOnly) {
            player.sendMessage(LanguageEngine.getMsg("event_afkWarning_kill", PlayerEventInfo.AFK_WARNING_DELAY / 1000, PlayerEventInfo.AFK_KICK_DELAY / 1000));
        } else if (this.getEventData(player.getInstanceId())._roundActive) {
            this.announce(player.getInstanceId(), LanguageEngine.getMsg("event_afkMarked_andDied", player.getPlayersName()));
            player.doDie();
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
    public void onDisconnect(PlayerEventInfo player) {
        if (player.isOnline()) {
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: player " + player.getPlayersName() + " (instance id = " + player.getInstanceId() + ") disconnecting from the event");
            }
            EventTeam team = player.getEventTeam();
            player.restoreData();
            player.setXYZInvisible(player.getOrigLoc().getX(), player.getOrigLoc().getY(), player.getOrigLoc().getZ());
            EventWarnings.getInstance().addPoints(player.getPlayersId(), 1);
            boolean running = false;
            AbstractMainEvent.AbstractEventInstance playersMatch = this.getMatch(player.getInstanceId());
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
                this.debug(this.getEventName() + ": Player " + player.getPlayersName() + " disconnected from main event, still enought players to continue the event.");
                if (team.getPlayers().isEmpty()) {
                    this.announce(player.getInstanceId(), LanguageEngine.getMsg("event_disconnect_team", team.getTeamName()));
                    this.debug(this.getEventName() + ": all players from team " + team.getTeamName() + " have disconnected.");
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: ALL PLAYERS FROM TEAM " + team.getTeamName() + " disconnected");
                    }
                }
                if (!this.checkIfEventCanContinue(player.getInstanceId(), player)) {
                    this.announce(player.getInstanceId(), LanguageEngine.getMsg("event_disconnect_all"));
                    this.endInstance(player.getInstanceId(), true, false, false);
                    this.debug(this.getEventName() + ": no players left in the teams, the fight cannot continue. The event has been aborted!");
                    if (NexusLoader.detailedDebug) {
                        this.print("AbstractMainEvent: NO PLAYERS LEFT IN THE TEAMS, THE FIGHT CAN'T CONTINUE! (checkIfEventCanContinue = false)");
                    }
                    return;
                }
                if (this.checkIfAllDied(player.getInstanceId())) {
                    this.endRound(player.getInstanceId(), false, false);
                }
            } else if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: -.- event IS NOT active anymore");
            }
        }
    }

    private boolean checkIfAllDied(int instanceId) {
        int alive = 0;
        for (PlayerEventInfo pi : this.getPlayers(instanceId)) {
            if (pi == null || pi.isDead()) continue;
            ++alive;
        }
        return alive < 2;
    }

    @Override
    public synchronized void clearEvent(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: called CLEAREVENT for instance " + instanceId);
        }
        try {
            if (this._matches != null) {
                for (Deathmatch.DMEventInstance match : this._matches.values()) {
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
                player.paralizeEffect(false);
            }
            player.setIsInvul(false);
            player.removeRadarAllMarkers();
            if (player.isImmobilized()) {
                player.unroot();
            }
            if (!player.isGM()) {
                player.setIsInvul(false);
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
        if (NexusLoader.detailedDebug) {
            this.print("Event: called global clearEvent()");
        }
        try {
            if (this._matches != null) {
                for (Deathmatch.DMEventInstance match : this._matches.values()) {
                    if (!this.getEventData(match.getInstance().getId()).isActive()) continue;
                    this.endRoundDueToEventTimeLimit(match.getInstance().getId(), true);
                }
            }
        }
        catch (Exception e) {
            // empty catch block
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
            pi.disableAfkCheck(false);
            pi.teleport(loc, 0, true, instance);
        } else {
            this.debug("Error on respawnPlayer - no spawn type REGULAR, team -1 (FFA) has been found. Event aborted.");
        }
    }

    @Override
    public String getEstimatedTimeLeft() {
        if (this._matches == null) {
            return "Starting";
        }
        for (Deathmatch.DMEventInstance match : this._matches.values()) {
            if (!this.getEventData(match.getInstance().getId())._isActive) continue;
            return "+-" + (this._maxRounds - this.getEventData(match.getInstance().getId())._round + 1) + " rounds";
        }
        return null;
    }

    @Override
    protected String addExtraEventInfoCb(int instance) {
        int countAlive = this.getEventData(instance)._alivePlayers - 1;
        String rounds = "" + this.getEventData(instance)._round + " of " + this._maxRounds;
        String status = "<td align=center width=200><font color=ac9887>Round: </font><font color=9f9f9f>" + rounds + "</font></td><td align=center width=200><font color=ac9887>Alive: </font><font color=9f9f9f>" + countAlive + " players</font></td>";
        return "<table width=510 bgcolor=3E3E3E><tr>" + status + "</tr></table>";
    }

    @Override
    public String getHtmlDescription() {
        if (this._htmlDescription == null) {
            EventDescription desc = EventDescriptionSystem.getInstance().getDescription(this.getEventType());
            if (desc != null) {
                this._htmlDescription = desc.getDescription(this.getConfigs());
            } else {
                this._htmlDescription = "This is a free-for-all event, don't expect any help from teammates. ";
                this._htmlDescription = this._htmlDescription + "This event has " + this.getInt("maxRounds") + " rounds. You can gain score by killing your opponents (1 kill = 1 score), but if you die, you won't get resurrected until the next round starts. ";
                this._htmlDescription = this._htmlDescription + "The player, who wins the round (when all other players are dead) receives additional " + this.getInt("scoreForRoundWinner") + " score points. ";
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
    protected String getTitle(PlayerEventInfo pi) {
        if (this._hideTitles) {
            return "";
        }
        if (pi.isAfk()) {
            return "AFK";
        }
        return "Score: " + this.getPlayerData(pi).getScore() + " Deaths: " + this.getPlayerData(pi).getDeaths();
    }

    @Override
    protected void clockTick() throws Exception {
        for (Deathmatch.DMEventInstance match : this._matches.values()) {
            ((LMSData)match._data).onTick();
        }
    }

    @Override
    protected AbstractMainEvent.AbstractEventInstance getMatch(int instanceId) {
        return (AbstractMainEvent.AbstractEventInstance)this._matches.get((Object)instanceId);
    }

    @Override
    protected Deathmatch.DMData createEventData(int instance) {
        return new LMSData(instance);
    }

    @Override
    protected LMSEventInstance createEventInstance(InstanceData instance) {
        return new LMSEventInstance(instance);
    }

    @Override
    protected LMSData getEventData(int instance) {
        try {
            return (LMSData)((Deathmatch.DMEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._data;
        }
        catch (Exception e) {
            NexusLoader.debug((String)("Error on getEventData for instance " + instance));
            e.printStackTrace();
            return null;
        }
    }

    protected class LMSEventInstance
    extends Deathmatch.DMEventInstance {
        public LMSEventInstance(InstanceData instance) {
            super(instance);
            if (LastManStanding.this._disableAnnouncingCountdown) {
                this._clock.disableAnnouncingCountdown();
            }
        }

        @Override
        public void run() {
            try {
                if (NexusLoader.detailedDebug) {
                    LastManStanding.this.print("Event: running task of state " + this._nextState.toString() + "...");
                }
                switch (this._nextState) {
                    case START: {
                        if (!LastManStanding.this.checkPlayers(this._instance.getId())) break;
                        if (LastManStanding.this._antifeed) {
                            for (PlayerEventInfo player : LastManStanding.this.getPlayers(this._instance.getId())) {
                                player.startAntifeedProtection(false);
                            }
                        }
                        LastManStanding.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, true);
                        LastManStanding.this.setupTitles(this._instance.getId());
                        LastManStanding.this.enableMarkers(this._instance.getId(), true);
                        LastManStanding.this.forceSitAll(this._instance.getId());
                        this.setNextState(Deathmatch.EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        LastManStanding.this.forceStandAll(this._instance.getId());
                        this.setNextState(Deathmatch.EventState.END);
                        LastManStanding.this.startRound(this._instance.getId());
                        this._clock.startClock(LastManStanding.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        this.setNextState(Deathmatch.EventState.INACTIVE);
                        if (LastManStanding.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            LastManStanding.this.rewardAllPlayers(this._instance.getId(), 0, LastManStanding.this.getInt("killsForReward"));
                        }
                        LastManStanding.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    LastManStanding.this.print("Event: ... finished running task. next state " + this._nextState.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                LastManStanding.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    protected class LMSData
    extends Deathmatch.DMData {
        private boolean _isActive;
        private boolean _waitingState;
        private boolean _roundActive;
        private int _waitingStateTime;
        private int _round;
        private int _alivePlayers;
        private Timelimit _timelimit;

        protected LMSData(int instance) {
            super(instance);
            this._alivePlayers = 0;
            this._round = 0;
            this._isActive = true;
            this._waitingState = false;
            this._roundActive = true;
        }

        public void onTick() {
            this._timelimit.onTick();
            if (this._waitingState && this._waitingStateTime > 0) {
                --this._waitingStateTime;
                switch (this._waitingStateTime) {
                    case 1: 
                    case 2: 
                    case 3: 
                    case 4: 
                    case 5: 
                    case 10: 
                    case 15: 
                    case 20: 
                    case 30: 
                    case 60: 
                    case 120: 
                    case 180: {
                        LastManStanding.this.announce(this._instanceId, LanguageEngine.getMsg("lms_roundStart", this._round, this._waitingStateTime));
                    }
                }
            }
        }

        private boolean playerDied() {
            if (this._alivePlayers > 0) {
                --this._alivePlayers;
            }
            return this._alivePlayers == 1;
        }

        private boolean canStartNewRound() {
            return this._isActive && this._round < LastManStanding.this._maxRounds;
        }

        private void newRound() {
            this._isActive = true;
            ++this._round;
            this._timelimit = new Timelimit();
        }

        private void setWaitingState(boolean b) {
            this._waitingState = b;
            if (b) {
                this._waitingStateTime = LastManStanding.this._roundWaitTime + 1;
            }
        }

        protected boolean isActive() {
            return this._isActive;
        }

        protected synchronized void setInactive() {
            this._isActive = false;
        }

        private class Timelimit {
            private int limit;
            private boolean aborted;

            public Timelimit() {
                this.aborted = false;
                this.limit = LastManStanding.this._roundTimeLimit;
            }

            public void onTick() {
                if (this.limit > 0) {
                    --this.limit;
                }
                if (!(this.aborted || this.limit > 0)) {
                    this.aborted = true;
                    LastManStanding.this.endRoundDueToTime(LMSData.this._instanceId);
                    if (NexusLoader.detailedDebug) {
                        LastManStanding.this.print("Event: round ended due to time limit");
                    }
                }
            }
        }

    }

}

