/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.callback.CallbackManager
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.FenceData
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.mini.events;

import cz.nxs.events.EventGame;
import cz.nxs.events.engine.EventBuffer;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.EventRewardSystem;
import cz.nxs.events.engine.EventWarnings;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.PvPEventPlayerData;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.MiniEventGame;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.mini.RegistrationData;
import cz.nxs.events.engine.mini.events.PartyvsPartyManager;
import cz.nxs.events.engine.stats.GlobalStats;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.events.engine.team.FixedPartyTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.FenceData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IPlayerBase;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class PartyvsPartyGame
extends MiniEventGame {
    private final int _teamsAmmount;
    private final int _roundsAmmount;
    private FixedPartyTeam[] _teams;
    private int _round;
    private ScheduledFuture<?> _eventEnd;
    private ScheduledFuture<?> _roundStart;

    public PartyvsPartyGame(int gameId, EventMap arena, PartyvsPartyManager event, RegistrationData[] teams) {
        super(gameId, arena, event, teams);
        this._teamsAmmount = event.getTeamsCount();
        this._roundsAmmount = event.getRoundsAmmount();
        this._teams = new FixedPartyTeam[this._teamsAmmount];
        for (int i = 0; i < this._teamsAmmount; ++i) {
            this._teams[i] = new FixedPartyTeam(i + 1, teams[i].getKeyPlayer().getPlayersName() + "'s party", event.getDefaultPartySizeToJoin());
            for (PlayerEventInfo pi : teams[i].getPlayers()) {
                pi.onEventStart((EventGame)this);
                this._teams[i].addPlayer(pi, true);
            }
        }
        CallbackManager.getInstance().eventStarts(1, this.getEvent().getEventType(), Arrays.asList(this._teams));
        this._round = 0;
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        this.loadBuffers();
        this.startEvent();
    }

    @Override
    protected void startEvent() {
        try {
            this.broadcastMessage(LanguageEngine.getMsg("game_teleporting"), true);
            this._eventEnd = CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    PartyvsPartyGame.this.endByTime();
                }
            }, this.getGameTime());
            this.scheduleMessage(LanguageEngine.getMsg("game_teleportDone"), 1500, true);
            this.nextRound(null, false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextRound(FixedPartyTeam lastWinner, boolean forceEnd) {
        if (this._aborted) {
            return;
        }
        if (this._round == this._roundsAmmount || forceEnd) {
            this.endByDie();
            return;
        }
        ++this._round;
        boolean removeBuffs = this.getEvent().getBoolean("removeBuffsOnRespawn");
        if (this._round == 1) {
            removeBuffs = this.getEvent().getBoolean("removeBuffsOnStart");
        }
        this.handleDoors(1);
        this.loadBuffers();
        for (FixedPartyTeam team : this._teams) {
            EventSpawn spawn = this._arena.getNextSpawn(team.getTeamId(), SpawnType.Regular);
            if (spawn == null) {
                this.abortDueToError("No regular spawn found for team " + team.getTeamId() + ". Match aborted.");
                this.clearEvent();
                return;
            }
            for (PlayerEventInfo pi : team.getPlayers()) {
                if (!pi.isOnline()) continue;
                pi.teleport(spawn.getLoc(), 0, true, this._instanceId);
                if (removeBuffs) {
                    pi.removeBuffs();
                }
                pi.disableAfkCheck(true);
                pi.root();
                pi.setIsInvul(true);
                if (this._round == 1 && this.getEvent().getBoolean("removeCubics")) {
                    pi.removeCubics();
                }
                if (this._allowSchemeBuffer) {
                    EventBuffer.getInstance().buffPlayer(pi, true);
                }
                if (this._round != 1) continue;
                pi.enableAllSkills();
            }
        }
        int startTime = this._round == 1 ? this.getEvent().getMapConfigInt(this._arena, "FirstRoundWaitDelay") : this.getEvent().getMapConfigInt(this._arena, "RoundWaitDelay");
        this.scheduleMessage(LanguageEngine.getMsg("game_roundStartIn", this.getRoundName(this._round, this._roundsAmmount), startTime / 1000), 5000, true);
        this._roundStart = CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

            @Override
            public void run() {
                PartyvsPartyGame.this.finishRoundStart();
            }
        }, startTime);
    }

    private void finishRoundStart() {
        if (this._aborted) {
            return;
        }
        this.unspawnBuffers();
        this.handleDoors(2);
        for (FixedPartyTeam team : this._teams) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                if (!pi.isOnline()) continue;
                pi.disableAfkCheck(false);
                pi.unroot();
                pi.setIsInvul(false);
            }
        }
        this.broadcastMessage(LanguageEngine.getMsg("game_roundStarted", this.getRoundName(this._round, this._roundsAmmount)), true);
        if (this._round == 1) {
            this.startAnnouncing();
        }
    }

    @Override
    public void onDie(PlayerEventInfo player, CharacterData killer) {
        if (this._aborted) {
            return;
        }
        this.updateScore(player, killer);
        final FixedPartyTeam team = this.checkLastAliveTeam();
        if (team != null) {
            boolean forceEnd;
            team.raiseScore(1);
            this.onScore((List<PlayerEventInfo>)team.getPlayers(), 1);
            boolean bl = forceEnd = !this.checkIfTheMatchCanContinue();
            if (this._round == this._roundsAmmount || forceEnd) {
                this.scheduleMessage(LanguageEngine.getMsg("game_matchEnd"), 3000, true);
            } else {
                this.scheduleMessage(LanguageEngine.getMsg("game_roundWonBy", this.getRoundName(this._round, this._roundsAmmount), team.getTeamName()), 3000, true);
            }
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    PartyvsPartyGame.this.nextRound(team, forceEnd);
                }
            }, 4000);
        }
    }

    private boolean checkIfTheMatchCanContinue() {
        int remainingRounds = this._roundsAmmount - this._round;
        int bestScore = 0;
        int secondScore = 0;
        for (FixedPartyTeam team : this._teams) {
            if (team.getScore() > bestScore) {
                secondScore = bestScore;
                bestScore = team.getScore();
                continue;
            }
            if (team.getScore() <= secondScore || secondScore == bestScore) continue;
            secondScore = team.getScore();
        }
        if (bestScore - secondScore > remainingRounds) {
            return false;
        }
        return true;
    }

    private FixedPartyTeam checkLastAliveTeam() {
        int aliveTeams = 0;
        FixedPartyTeam tempTeam = null;
        block0 : for (FixedPartyTeam team : this._teams) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                if (!pi.isOnline() || pi.isDead()) continue;
                ++aliveTeams;
                tempTeam = team;
                continue block0;
            }
        }
        if (aliveTeams == 1) {
            return tempTeam;
        }
        return null;
    }

    private void endByTime() {
        if (this._aborted) {
            return;
        }
        this.cancelSchedulers();
        this.broadcastMessage(LanguageEngine.getMsg("game_matchEnd_timeLimit", this.getGameTime() / 60000), false);
        this.scheduleMessage(LanguageEngine.getMsg("game_matchEnd_tie"), 3000, false);
        for (FixedPartyTeam team : this._teams) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                if (pi.isOnline()) {
                    EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), pi, RewardPosition.Tie_TimeLimit, null, pi.getTotalTimeAfk(), 0, 0);
                }
                this._event.logPlayer(pi, 2);
            }
        }
        this.saveGlobalStats();
        this.scheduleClearEvent(8000);
    }

    private void endByDie() {
        this.cancelSchedulers();
        FastList sortedTeams = new FastList();
        for (FixedPartyTeam team : this._teams) {
            sortedTeams.add(team);
        }
        Collections.sort(sortedTeams, EventManager.getInstance().compareTeamScore);
        FastMap scores = new FastMap();
        for (FixedPartyTeam team2 : sortedTeams) {
            if (!scores.containsKey(team2.getScore())) {
                scores.put(team2.getScore(), new FastList());
            }
            ((FastList)scores.get(team2.getScore())).add((Object)team2);
        }
        int place = 1;
        for (FixedPartyTeam team3 : sortedTeams) {
            this.broadcastMessage(LanguageEngine.getMsg("event_announceScore_includeKills", place, team3.getTeamName(), team3.getScore(), team3.getKills()), false);
            ++place;
        }
        place = 1;
        for (Map.Entry i : scores.entrySet()) {
            if (place == 1) {
                if (((FastList)i.getValue()).size() > 1) {
                    if (this._teamsAmmount > ((FastList)i.getValue()).size()) {
                        TextBuilder tb = new TextBuilder();
                        for (FixedPartyTeam team4 : (FastList)i.getValue()) {
                            tb.append(LanguageEngine.getMsg("event_team_announceWinner2_part1", team4.getTeamName()));
                        }
                        String s = tb.toString();
                        tb = new TextBuilder(s.substring(0, s.length() - 4));
                        tb.append(LanguageEngine.getMsg("event_team_announceWinner2_part2"));
                        this.broadcastMessage(tb.toString(), false);
                        for (FixedPartyTeam team5 : (FastList)i.getValue()) {
                            for (PlayerEventInfo pi : team5.getPlayers()) {
                                if (pi.isOnline()) {
                                    EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), pi, RewardPosition.Winner, null, pi.getTotalTimeAfk(), 0, 0);
                                    this.setEndStatus(pi, 1);
                                }
                                this.getPlayerData(pi).getGlobalStats().raise(GlobalStats.GlobalStatType.WINS, 1);
                                this._event.logPlayer(pi, 1);
                            }
                        }
                    } else {
                        this.broadcastMessage(LanguageEngine.getMsg("event_team_announceWinner3"), false);
                        for (FixedPartyTeam team6 : (FastList)i.getValue()) {
                            for (PlayerEventInfo pi : team6.getPlayers()) {
                                if (pi.isOnline()) {
                                    EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), pi, RewardPosition.Tie, null, pi.getTotalTimeAfk(), 0, 0);
                                }
                                this.getPlayerData(pi).getGlobalStats().raise(GlobalStats.GlobalStatType.WINS, 1);
                                this._event.logPlayer(pi, 2);
                            }
                        }
                    }
                } else {
                    this.broadcastMessage(LanguageEngine.getMsg("event_team_announceWinner1", ((FixedPartyTeam)((FastList)i.getValue()).getFirst()).getTeamName()), false);
                    for (PlayerEventInfo pi : ((FixedPartyTeam)((FastList)i.getValue()).getFirst()).getPlayers()) {
                        if (pi.isOnline()) {
                            EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), pi, RewardPosition.Winner, null, pi.getTotalTimeAfk(), 0, 0);
                            this.setEndStatus(pi, 1);
                        }
                        this.getPlayerData(pi).getGlobalStats().raise(GlobalStats.GlobalStatType.WINS, 1);
                        this._event.logPlayer(pi, 1);
                    }
                }
            } else {
                for (FixedPartyTeam team6 : (FastList)i.getValue()) {
                    for (PlayerEventInfo pi : team6.getPlayers()) {
                        if (pi.isOnline()) {
                            EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), pi, RewardPosition.Looser, null, pi.getTotalTimeAfk(), 0, 0);
                            this.setEndStatus(pi, 0);
                        }
                        this.getPlayerData(pi).getGlobalStats().raise(GlobalStats.GlobalStatType.LOSES, 1);
                        this._event.logPlayer(pi, 2);
                    }
                }
            }
            ++place;
        }
        this.saveGlobalStats();
        this.scheduleClearEvent(5000);
    }

    @Override
    public void clearEvent() {
        this.cancelSchedulers();
        this.cleanSpectators();
        this.applyStatsChanges();
        for (FixedPartyTeam team : this._teams) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                if (!pi.isOnline()) continue;
                if (pi.isImmobilized()) {
                    pi.unroot();
                }
                if (!pi.isGM()) {
                    pi.setIsInvul(false);
                }
                pi.restoreData();
                pi.teleport(pi.getOrigLoc(), 0, true, 0);
                pi.sendMessage(LanguageEngine.getMsg("event_teleportBack"));
                CallBack.getInstance().getPlayerBase().eventEnd(pi);
            }
        }
        if (this._fences != null) {
            CallBack.getInstance().getOut().unspawnFences(this._fences);
        }
        this.unspawnMapGuards();
        this.unspawnNpcs();
        this._event.notifyGameEnd(this);
    }

    @Override
    public void onDisconnect(PlayerEventInfo player) {
        if (player != null && player.isOnline()) {
            if (player.isSpectator()) {
                this.removeSpectator(player, true);
                return;
            }
            EventWarnings.getInstance().addPoints(player.getPlayersId(), 1);
            this.broadcastMessage(LanguageEngine.getMsg("game_playerDisconnected", player.getPlayersName()), true);
            EventTeam playerTeam = player.getEventTeam();
            playerTeam.removePlayer(player);
            player.restoreData();
            player.setXYZInvisible(player.getOrigLoc().getX(), player.getOrigLoc().getY(), player.getOrigLoc().getZ());
            if (!this._aborted) {
                if (playerTeam.getPlayers().isEmpty()) {
                    this.broadcastMessage(LanguageEngine.getMsg("game_teamDisconnected", playerTeam.getTeamName()), true);
                }
                if (this.checkIfTeamsDisconnected()) {
                    this.broadcastMessage(LanguageEngine.getMsg("event_disconnect_all"), true);
                    this.clearEvent();
                    return;
                }
                final FixedPartyTeam team = this.checkLastAliveTeam();
                if (team != null) {
                    CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                        @Override
                        public void run() {
                            PartyvsPartyGame.this.nextRound(team, false);
                        }
                    }, 3000);
                }
            }
        }
    }

    private boolean checkIfTeamsDisconnected() {
        int teamsOn = 0;
        block0 : for (FixedPartyTeam team : this._teams) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                if (!pi.isOnline()) continue;
                ++teamsOn;
                continue block0;
            }
        }
        return teamsOn == 0 || teamsOn == 1;
    }

    @Override
    protected void checkPlayersLoc() {
    }

    @Override
    protected void checkIfPlayersTeleported() {
    }

    private void cancelSchedulers() {
        if (this._aborted) {
            return;
        }
        this._aborted = true;
        CallbackManager.getInstance().eventEnded(1, this.getEvent().getEventType(), Arrays.asList(this._teams));
        if (this._announcer != null) {
            this._announcer.cancel();
            this._announcer = null;
        }
        if (this._locChecker != null) {
            this._locChecker.cancel(false);
            this._locChecker = null;
        }
        if (this._eventEnd != null) {
            this._eventEnd.cancel(false);
            this._eventEnd = null;
        }
        if (this._roundStart != null) {
            this._roundStart.cancel(false);
            this._roundStart = null;
        }
    }

    @Override
    public int getInstanceId() {
        return this._instanceId;
    }

    @Override
    public EventTeam[] getTeams() {
        return this._teams;
    }

    @Override
    public EventPlayerData createPlayerData(PlayerEventInfo player) {
        PvPEventPlayerData d = new PvPEventPlayerData(player, this, new GlobalStatsModel(this._event.getEventType()));
        return d;
    }

    @Override
    public PvPEventPlayerData getPlayerData(PlayerEventInfo player) {
        return (PvPEventPlayerData)player.getEventData();
    }

}

