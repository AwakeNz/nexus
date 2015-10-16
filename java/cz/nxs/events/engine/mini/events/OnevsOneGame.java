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
import cz.nxs.events.engine.mini.events.OnevsOneManager;
import cz.nxs.events.engine.stats.GlobalStats;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.events.engine.team.OnePlayerTeam;
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

public class OnevsOneGame
extends MiniEventGame {
    private final int _teamsAmmount;
    private final int _roundsAmmount;
    protected OnePlayerTeam[] _players;
    private ScheduledFuture<?> _eventEnd;
    private ScheduledFuture<?> _roundStart;
    private int _round;

    public OnevsOneGame(int gameId, EventMap arena, OnevsOneManager event, RegistrationData[] teams) {
        super(gameId, arena, event, teams);
        this._teamsAmmount = event.getTeamsCount();
        this._roundsAmmount = event.getRoundsAmmount();
        this._players = new OnePlayerTeam[this._teamsAmmount];
        for (int i = 0; i < this._teamsAmmount; ++i) {
            this._players[i] = new OnePlayerTeam(i + 1, teams[i].getKeyPlayer().getPlayersName());
            teams[i].getKeyPlayer().onEventStart((EventGame)this);
            this._players[i].addPlayer(teams[i].getKeyPlayer(), true);
        }
        CallbackManager.getInstance().eventStarts(1, this.getEvent().getEventType(), Arrays.asList(this._players));
        this._round = 0;
    }

    @Override
    protected void initEvent() {
        super.initEvent();
        this.startEvent();
    }

    @Override
    protected void startEvent() {
        try {
            this.broadcastMessage(LanguageEngine.getMsg("game_teleporting"), false);
            this._eventEnd = CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    OnevsOneGame.this.endByTime();
                }
            }, this.getGameTime());
            this.scheduleMessage(LanguageEngine.getMsg("game_teleportDone"), 1500, true);
            this.nextRound(false);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void nextRound(boolean forceEnd) {
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
        for (OnePlayerTeam team : this._players) {
            if (team.getPlayer() == null || !team.getPlayer().isOnline()) continue;
            EventSpawn spawn = this._arena.getNextSpawn(team.getTeamId(), SpawnType.Regular);
            if (spawn == null) {
                this.abortDueToError("No regular spawn found for team " + team.getTeamId() + ". Match aborted.");
                this.clearEvent();
                return;
            }
            team.getPlayer().teleport(spawn.getLoc(), 0, false, this._instanceId);
            if (removeBuffs) {
                team.getPlayer().removeBuffs();
            }
            team.getPlayer().disableAfkCheck(true);
            team.getPlayer().root();
            team.getPlayer().setIsInvul(true);
            if (this._round == 1 && this.getEvent().getBoolean("removeCubics")) {
                team.getPlayer().removeCubics();
            }
            if (this._allowSchemeBuffer) {
                EventBuffer.getInstance().buffPlayer(team.getPlayer(), true);
            }
            if (this._round != 1) continue;
            team.getPlayer().enableAllSkills();
        }
        int startTime = this._round == 1 ? this.getEvent().getMapConfigInt(this._arena, "FirstRoundWaitDelay") : this.getEvent().getMapConfigInt(this._arena, "RoundWaitDelay");
        this.scheduleMessage(LanguageEngine.getMsg("game_roundStartIn", this.getRoundName(this._round, this._roundsAmmount), startTime / 1000), 5000, true);
        this._roundStart = CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

            @Override
            public void run() {
                OnevsOneGame.this.finishRoundStart();
            }
        }, startTime);
    }

    private void finishRoundStart() {
        if (this._aborted) {
            return;
        }
        this.unspawnBuffers();
        this.handleDoors(2);
        for (OnePlayerTeam team : this._players) {
            if (team.getPlayer() == null || !team.getPlayer().isOnline()) continue;
            team.getPlayer().disableAfkCheck(false);
            team.getPlayer().unroot();
            team.getPlayer().setIsInvul(false);
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
        OnePlayerTeam team = this.checkLastAlivePlayer();
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
                    OnevsOneGame.this.nextRound(forceEnd);
                }
            }, 4000);
        }
    }

    private boolean checkIfTheMatchCanContinue() {
        int remainingRounds = this._roundsAmmount - this._round;
        int bestScore = 0;
        int secondScore = 0;
        for (OnePlayerTeam team : this._players) {
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

    private OnePlayerTeam checkLastAlivePlayer() {
        int alivePlayers = 0;
        OnePlayerTeam tempTeam = null;
        for (OnePlayerTeam team : this._players) {
            if (team.getPlayer() == null || !team.getPlayer().isOnline() || team.getPlayer().isDead()) continue;
            ++alivePlayers;
            tempTeam = team;
        }
        if (alivePlayers == 1) {
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
        for (OnePlayerTeam team : this._players) {
            if (team.getPlayer() == null) continue;
            EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), team.getPlayer(), RewardPosition.Tie_TimeLimit, null, team.getPlayer().getTotalTimeAfk(), 0, 0);
            this.getPlayerData(team.getPlayer()).getGlobalStats().raise(GlobalStats.GlobalStatType.LOSES, 1);
            this._event.logPlayer(team.getPlayer(), 2);
        }
        this.saveGlobalStats();
        this.scheduleClearEvent(8000);
    }

    private void endByDie() {
        this.cancelSchedulers();
        FastList sortedTeams = new FastList();
        for (OnePlayerTeam team3 : this._players) {
            sortedTeams.add(team3);
        }
        Collections.sort(sortedTeams, EventManager.getInstance().compareTeamScore);
        FastMap scores = new FastMap();
        for (OnePlayerTeam team2 : sortedTeams) {
            if (!scores.containsKey(team2.getScore())) {
                scores.put(team2.getScore(), new FastList());
            }
            ((FastList)scores.get(team2.getScore())).add((Object)team2);
        }
        int place = 1;
        for (OnePlayerTeam team : sortedTeams) {
            this.broadcastMessage(LanguageEngine.getMsg("event_announceScore_includeKills", place, team.getTeamName(), team.getScore(), team.getKills()), false);
            ++place;
        }
        place = 1;
        for (Map.Entry i : scores.entrySet()) {
            if (place == 1) {
                if (((FastList)i.getValue()).size() > 1) {
                    if (this._teamsAmmount > ((FastList)i.getValue()).size()) {
                        TextBuilder tb = new TextBuilder();
                        for (OnePlayerTeam team4 : (FastList)i.getValue()) {
                            tb.append(LanguageEngine.getMsg("event_ffa_announceWinner2_part1", team4.getTeamName()));
                        }
                        String s = tb.toString();
                        tb = new TextBuilder(s.substring(0, s.length() - 4));
                        tb.append(LanguageEngine.getMsg("event_ffa_announceWinner2_part1"));
                        this.broadcastMessage(tb.toString(), false);
                        for (OnePlayerTeam team5 : (FastList)i.getValue()) {
                            if (team5.getPlayer() == null) continue;
                            if (team5.getPlayer().isOnline()) {
                                EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), team5.getPlayer(), RewardPosition.Winner, null, team5.getPlayer().getTotalTimeAfk(), 0, 0);
                                this.setEndStatus(team5.getPlayer(), 1);
                            }
                            this.getPlayerData(team5.getPlayer()).getGlobalStats().raise(GlobalStats.GlobalStatType.WINS, 1);
                            this._event.logPlayer(team5.getPlayer(), 1);
                        }
                    } else {
                        this.broadcastMessage(LanguageEngine.getMsg("event_ffa_announceWinner3"), false);
                        for (OnePlayerTeam team4 : (FastList)i.getValue()) {
                            if (team4.getPlayer() == null) continue;
                            if (team4.getPlayer().isOnline()) {
                                EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), team4.getPlayer(), RewardPosition.Tie, null, team4.getPlayer().getTotalTimeAfk(), 0, 0);
                            }
                            this.getPlayerData(team4.getPlayer()).getGlobalStats().raise(GlobalStats.GlobalStatType.WINS, 1);
                            this._event.logPlayer(team4.getPlayer(), 2);
                        }
                    }
                } else {
                    OnePlayerTeam winnerPlayer = (OnePlayerTeam)((FastList)i.getValue()).getFirst();
                    this.broadcastMessage(LanguageEngine.getMsg("event_ffa_announceWinner1", ((OnePlayerTeam)((FastList)i.getValue()).getFirst()).getTeamName()), false);
                    if (winnerPlayer.getPlayer() != null) {
                        if (winnerPlayer.getPlayer().isOnline()) {
                            EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), winnerPlayer.getPlayer(), RewardPosition.Winner, null, winnerPlayer.getPlayer().getTotalTimeAfk(), 0, 0);
                            this.setEndStatus(winnerPlayer.getPlayer(), 1);
                        }
                        this.getPlayerData(winnerPlayer.getPlayer()).getGlobalStats().raise(GlobalStats.GlobalStatType.WINS, 1);
                        this._event.logPlayer(winnerPlayer.getPlayer(), 1);
                    }
                }
            } else {
                for (OnePlayerTeam team6 : (FastList)i.getValue()) {
                    if (team6.getPlayer() == null) continue;
                    if (team6.getPlayer().isOnline()) {
                        EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), team6.getPlayer(), RewardPosition.Looser, null, team6.getPlayer().getTotalTimeAfk(), 0, 0);
                        this.setEndStatus(team6.getPlayer(), 0);
                    }
                    this.getPlayerData(team6.getPlayer()).getGlobalStats().raise(GlobalStats.GlobalStatType.LOSES, 1);
                    this._event.logPlayer(team6.getPlayer(), 2);
                }
            }
            ++place;
        }
        this.saveGlobalStats();
        this.scheduleClearEvent(8000);
    }

    @Override
    public void clearEvent() {
        this.cancelSchedulers();
        this.cleanSpectators();
        this.applyStatsChanges();
        for (OnePlayerTeam team : this._players) {
            if (team.getPlayer() == null || !team.getPlayer().isOnline()) continue;
            if (team.getPlayer().isImmobilized()) {
                team.getPlayer().unroot();
            }
            if (!team.getPlayer().isGM()) {
                team.getPlayer().setIsInvul(false);
            }
            team.getPlayer().restoreData();
            team.getPlayer().teleport(team.getPlayer().getOrigLoc(), 0, true, 0);
            team.getPlayer().sendMessage(LanguageEngine.getMsg("event_teleportBack"));
            CallBack.getInstance().getPlayerBase().eventEnd(team.getPlayer());
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
            if (this._teamsAmmount == 2) {
                this.broadcastMessage(LanguageEngine.getMsg("game_playerDisconnected2", player.getPlayersName()), true);
            } else {
                this.broadcastMessage(LanguageEngine.getMsg("game_playerDisconnected", player.getPlayersName()), true);
            }
            EventTeam playerTeam = player.getEventTeam();
            player.restoreData();
            player.setXYZInvisible(player.getOrigLoc().getX(), player.getOrigLoc().getY(), player.getOrigLoc().getZ());
            if (!this._aborted) {
                playerTeam.removePlayer(player);
                if (this.checkIfPlayersDisconnected()) {
                    this.broadcastMessage(LanguageEngine.getMsg("event_disconnect_all"), true);
                    this.clearEvent();
                    return;
                }
                OnePlayerTeam team = this.checkLastAlivePlayer();
                if (team != null) {
                    CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                        @Override
                        public void run() {
                            OnevsOneGame.this.nextRound(false);
                        }
                    }, 3000);
                }
            }
        }
    }

    private boolean checkIfPlayersDisconnected() {
        int teamsOn = 0;
        for (OnePlayerTeam team : this._players) {
            if (team.getPlayer() == null || !team.getPlayer().isOnline()) continue;
            ++teamsOn;
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
        CallbackManager.getInstance().eventEnded(1, this.getEvent().getEventType(), Arrays.asList(this._players));
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
        return this._players;
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

