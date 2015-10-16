/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.callback.CallbackManager
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.FenceData
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.mini.events;

import cz.nxs.events.EventGame;
import cz.nxs.events.engine.EventBuffer;
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
import cz.nxs.events.engine.mini.events.KoreanManager;
import cz.nxs.events.engine.stats.GlobalStats;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.events.engine.team.KoreanTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.FenceData;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IPlayerBase;
import cz.nxs.l2j.IValues;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import javolution.util.FastList;

public class KoreanGame
extends MiniEventGame
implements Runnable {
    private KoreanTeam[] _teams;
    private boolean _initState = true;
    private ScheduledFuture<?> _eventEnd;
    private ScheduledFuture<?> _roundStart;

    public KoreanGame(int gameId, EventMap arena, KoreanManager event, RegistrationData[] teams) {
        super(gameId, arena, event, teams);
        int teamsAmmount = 2;
        this._teams = new KoreanTeam[2];
        for (int i = 0; i < 2; ++i) {
            this._teams[i] = new KoreanTeam(i + 1, teams[i].getKeyPlayer().getPlayersName() + "'s party");
            for (PlayerEventInfo pi : teams[i].getPlayers()) {
                pi.onEventStart((EventGame)this);
                this._teams[i].addPlayer(pi, true);
            }
        }
        CallbackManager.getInstance().eventStarts(1, this.getEvent().getEventType(), Arrays.asList(this._teams));
    }

    @Override
    public void run() {
        this.initEvent();
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
            boolean removeBuffs = this.getEvent().getBoolean("removeBuffsOnStart");
            this.broadcastMessage(LanguageEngine.getMsg("game_teleporting"), true);
            this._eventEnd = CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    KoreanGame.this.endByTime();
                }
            }, this.getGameTime());
            for (KoreanTeam team : this._teams) {
                for (PlayerEventInfo pi : team.getPlayers()) {
                    pi.teleport(this._arena.getNextSpawn(team.getTeamId(), SpawnType.Safe).getLoc(), 0, true, this._instanceId);
                    if (removeBuffs) {
                        pi.removeBuffs();
                    }
                    pi.disableAfkCheck(true);
                    if (this.getEvent().getBoolean("removeCubics")) {
                        pi.removeCubics();
                    }
                    if (this._allowSchemeBuffer) {
                        EventBuffer.getInstance().buffPlayer(pi, true);
                    }
                    pi.enableAllSkills();
                }
            }
            this.scheduleMessage(LanguageEngine.getMsg("game_teleportDone"), 1500, true);
            this.handleDoors(1);
            int startTime = this._event.getMapConfigInt(this._arena, "WaitTime");
            this._roundStart = CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    KoreanGame.this.finishRoundStart();
                }
            }, startTime);
            this.scheduleMessage(LanguageEngine.getMsg("game_matchStartsIn", startTime / 1000), 5000, true);
        }
        catch (Exception e) {
            this.abortDueToError("Map wasn't set up correctly.");
            e.printStackTrace();
        }
    }

    private void finishRoundStart() {
        if (this._aborted) {
            return;
        }
        this.broadcastMessage(LanguageEngine.getMsg("game_korean_teleportingToArena"), true);
        this.unspawnBuffers();
        this.handleDoors(2);
        this.teleportToEventLocation();
        this._initState = false;
        final PlayerEventInfo player1 = this.getNextPlayer(1);
        final PlayerEventInfo player2 = this.getNextPlayer(2);
        this.scheduleMessage(LanguageEngine.getMsg("game_korean_nextFight", player1.getPlayersName(), player2.getPlayersName(), 8), 3000, true);
        CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

            @Override
            public void run() {
                KoreanGame.this.startFight(player1, player2);
            }
        }, 11000);
        this.startAnnouncing();
    }

    private void startFight(PlayerEventInfo player1, PlayerEventInfo player2) {
        if (this._aborted) {
            return;
        }
        SkillData skill = new SkillData(5965, 1);
        player1.disableAfkCheck(false);
        player1.setIsParalyzed(false);
        player1.setIsInvul(false);
        player1.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_STEALTH());
        player1.broadcastSkillUse(null, null, skill.getId(), skill.getLevel());
        player1.startAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REAL_TARGET());
        player2.disableAfkCheck(false);
        player2.setIsParalyzed(false);
        player2.setIsInvul(false);
        player2.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_STEALTH());
        player2.broadcastSkillUse(null, null, skill.getId(), skill.getLevel());
        player2.startAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REAL_TARGET());
        this.broadcastMessage(LanguageEngine.getMsg("game_korean_fightStarted"), true);
    }

    private void teleportToEventLocation() {
        try {
            for (KoreanTeam team : this._teams) {
                for (PlayerEventInfo member : team.getPlayers()) {
                    member.teleport(this._arena.getNextSpawn(team.getTeamId(), SpawnType.Regular).getLoc(), 0, false, -1);
                    member.setIsInvul(true);
                    member.setIsParalyzed(true);
                    member.startAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_STEALTH());
                }
            }
        }
        catch (Exception e) {
            this.abortDueToError("Map wasn't propably set up correctly.");
            e.printStackTrace();
        }
    }

    private PlayerEventInfo getNextPlayer(int teamId) {
        return this._teams[teamId - 1].getNextPlayer();
    }

    @Override
    public void onDie(final PlayerEventInfo player, CharacterData killer) {
        if (this._aborted) {
            return;
        }
        this.updateScore(player, killer);
        player.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REAL_TARGET());
        if (player.getEventTeam().getDeaths() >= player.getEventTeam().getPlayers().size()) {
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    KoreanGame.this.endByDie(KoreanGame.this.oppositeTeam(player.getEventTeam()));
                }
            }, 3000);
        } else {
            final PlayerEventInfo nextPlayer = ((KoreanTeam)player.getEventTeam()).getNextPlayer();
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    KoreanGame.this.announceNextPlayer(nextPlayer);
                }
            }, 3000);
        }
    }

    private void announceNextPlayer(PlayerEventInfo nextPlayer) {
        SkillData skill = new SkillData(5965, 1);
        nextPlayer.setIsParalyzed(false);
        nextPlayer.setIsInvul(false);
        nextPlayer.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_STEALTH());
        nextPlayer.broadcastSkillUse(null, null, skill.getId(), skill.getLevel());
        nextPlayer.startAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REAL_TARGET());
        this.broadcastMessage(LanguageEngine.getMsg("game_korean_nextPlayer", nextPlayer.getPlayersName()), false);
    }

    private void endByTime() {
        if (this._aborted) {
            return;
        }
        this.cancelSchedulers();
        this.broadcastMessage(LanguageEngine.getMsg("game_matchEnd_timeLimit", this.getGameTime() / 60000), false);
        this.scheduleMessage(LanguageEngine.getMsg("game_matchEnd_tie"), 3000, false);
        for (KoreanTeam team : this._teams) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), pi, RewardPosition.Tie_TimeLimit, null, pi.getTotalTimeAfk(), 0, 0);
                this.getPlayerData(pi).getGlobalStats().raise(GlobalStats.GlobalStatType.LOSES, 1);
                this._event.logPlayer(pi, 2);
            }
        }
        this.saveGlobalStats();
        CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

            @Override
            public void run() {
                KoreanGame.this.clearEvent();
            }
        }, 8000);
    }

    private void endByDie(EventTeam winner) {
        this.cancelSchedulers();
        this.broadcastMessage(LanguageEngine.getMsg("game_korean_winner", winner.getTeamName()), false);
        for (PlayerEventInfo pi2 : winner.getPlayers()) {
            if (pi2.isOnline()) {
                EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), pi2, RewardPosition.Winner, null, pi2.getTotalTimeAfk(), 0, 0);
                this.setEndStatus(pi2, 1);
            }
            this.getPlayerData(pi2).getGlobalStats().raise(GlobalStats.GlobalStatType.WINS, 1);
            this._event.logPlayer(pi2, 1);
        }
        for (PlayerEventInfo pi2 : this.oppositeTeam(winner).getPlayers()) {
            if (pi2.isOnline()) {
                EventRewardSystem.getInstance().rewardPlayer(this.getEvent().getEventType(), this.getEvent().getMode().getModeId(), pi2, RewardPosition.Looser, null, pi2.getTotalTimeAfk(), 0, 0);
                this.setEndStatus(pi2, 0);
            }
            this.getPlayerData(pi2).getGlobalStats().raise(GlobalStats.GlobalStatType.LOSES, 1);
            this._event.logPlayer(pi2, 2);
        }
        this.saveGlobalStats();
        CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

            @Override
            public void run() {
                KoreanGame.this.clearEvent();
            }
        }, 5000);
    }

    @Override
    public void clearEvent() {
        this.cancelSchedulers();
        this.cleanSpectators();
        this.applyStatsChanges();
        for (KoreanTeam team : this._teams) {
            for (PlayerEventInfo pi : team.getPlayers()) {
                if (!pi.isOnline()) continue;
                if (pi.isParalyzed()) {
                    pi.setIsParalyzed(false);
                }
                pi.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REAL_TARGET());
                pi.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_STEALTH());
                pi.setIsInvul(false);
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
            final EventTeam playerTeam = player.getEventTeam();
            playerTeam.removePlayer(player);
            player.restoreData();
            player.setXYZInvisible(player.getOrigLoc().getX(), player.getOrigLoc().getY(), player.getOrigLoc().getZ());
            if (!this._aborted) {
                if (playerTeam.getPlayers().isEmpty() || !this.checkTeamStatus(playerTeam.getTeamId())) {
                    this.cancelSchedulers();
                    CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                        @Override
                        public void run() {
                            KoreanGame.this.broadcastMessage(LanguageEngine.getMsg("event_disconnect_all"), false);
                            if (KoreanGame.this._initState) {
                                KoreanGame.this.clearEvent();
                            } else {
                                KoreanGame.this.endByDie(KoreanGame.this.oppositeTeam(playerTeam));
                            }
                        }
                    }, 3000);
                } else if (!this._initState && ((KoreanTeam)playerTeam).isFighting(player)) {
                    final PlayerEventInfo nextPlayer = ((KoreanTeam)playerTeam).getNextPlayer();
                    if (nextPlayer == null) {
                        this.cancelSchedulers();
                        CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                            @Override
                            public void run() {
                                KoreanGame.this.broadcastMessage(LanguageEngine.getMsg("event_disconnect_all"), false);
                                KoreanGame.this.endByDie(KoreanGame.this.oppositeTeam(playerTeam));
                            }
                        }, 5000);
                    } else {
                        CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                            @Override
                            public void run() {
                                KoreanGame.this.announceNextPlayer(nextPlayer);
                            }
                        }, 5000);
                    }
                }
            }
        }
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

    private KoreanTeam oppositeTeam(EventTeam team) {
        if (team.getTeamId() == 1) {
            return this._teams[1];
        }
        if (team.getTeamId() == 2) {
            return this._teams[0];
        }
        return null;
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

