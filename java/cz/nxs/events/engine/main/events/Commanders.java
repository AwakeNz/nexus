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
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.base.ConfigModel;
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
import cz.nxs.events.engine.main.events.TeamVsTeam;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.interf.delegate.SkillData;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import javolution.util.FastList;
import javolution.util.FastMap;

public class Commanders
extends TeamVsTeam {
    protected int _baseNpcId;
    protected int _countOfSuperiorTeams;
    protected int _tick;
    private FastMap<Integer, Integer> _skillsForAll;

    public Commanders(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Winner, RewardPosition.Looser, RewardPosition.Tie, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill, RewardPosition.KillingSpree});
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.addConfig(new ConfigModel("skillsForAllPlayers", "35100-1", "IDs of skills which will be given to players on the event. Format: <font color=LEVEL>SKILLID-LEVEL</font> (eg. '35000-1').", ConfigModel.InputType.MultiAdd));
    }

    @Override
    public void initEvent() {
        super.initEvent();
        if (!this.getString("skillsForAllPlayers").equals("")) {
            String[] splits = this.getString("skillsForAllPlayers").split(",");
            this._skillsForAll = new FastMap();
            try {
                for (int i = 0; i < splits.length; ++i) {
                    String id = splits[i].split("-")[0];
                    String level = splits[i].split("-")[1];
                    this._skillsForAll.put((Object)Integer.parseInt(id), (Object)Integer.parseInt(level));
                }
            }
            catch (Exception e) {
                NexusLoader.debug((String)("Error while loading config 'skillsForAllPlayers' for event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
            }
        }
        this._tick = 0;
    }

    protected int getCountOfTeams(int instanceId) {
        int countOfPlayersUnderOneCommander = 50;
        int countOfPlayers = this.getPlayers(instanceId).size();
        int countOfTeams = countOfPlayers / 50;
        if (countOfTeams % 2 != 0) {
            --countOfTeams;
        }
        return countOfTeams;
    }

    @Override
    protected int initInstanceTeams(MainEventInstanceType type, int instanceId) {
        this._teamsCount = this.getCountOfTeams(type.getInstance().getId());
        if (this._teamsCount < 2) {
            this._teamsCount = 2;
        }
        this.createTeams(this._teamsCount, type.getInstance().getId());
        return this._teamsCount;
    }

    @Override
    protected void createTeams(int count, int instanceId) {
        try {
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: creating " + count + " teams for instanceId " + instanceId);
            }
            for (int i = 0; i < count; ++i) {
                this.createNewTeam(instanceId, count + 1, "Noneyet", "Noneyet");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void createNewTeam(int instanceId, int id, String name, String fullName) {
        ((FastMap)this._teams.get((Object)instanceId)).put((Object)id, (Object)new EventTeam(id, name, fullName));
    }

    protected void preparePlayers(int instanceId, boolean start) {
    }

    protected void setupTeams(int instanceId, int maxSuperTeams) {
        int superTeam = 1;
        for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
            for (PlayerEventInfo player : team.getPlayers()) {
                this.getPlayerData(player).setSuperTeam(superTeam);
            }
            if (superTeam == maxSuperTeams) {
                superTeam = 1;
            }
            ++superTeam;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void spawnCommanderStuff(int instanceId, boolean spawn) {
        if (spawn) {
            this.clearMapHistory(-1, SpawnType.Base);
            for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
                EventSpawn sp = this.getSpawn(SpawnType.Base, team.getTeamId());
                NpcData base = this.spawnNPC(sp.getLoc().getX(), sp.getLoc().getY(), sp.getLoc().getZ(), this._baseNpcId, instanceId, "Base", "Team Base");
                this.getEventData(instanceId).setBase(team.getTeamId(), base);
            }
            return;
        } else {
            NpcData base = null;
            for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
                base = this.getEventData(instanceId).getBase(team.getTeamId());
                if (base == null) continue;
                base.deleteMe();
            }
        }
    }

    protected void setNewCommander(int instanceId, PlayerEventInfo newCommander, int teamId) {
        this.announce(instanceId, "*** Your commander is " + newCommander.getPlayersName(), teamId);
    }

    protected void commanderAction(int instanceId, int teamid, String action) {
    }

    protected void hiveDead(int instanceId, PlayerEventInfo newCommander, int teamId) {
    }

    protected void handleSkills(int instanceId, boolean add) {
        if (this._skillsForAll != null) {
            SkillData skill = null;
            for (PlayerEventInfo player : this.getPlayers(instanceId)) {
                if (add) {
                    for (Map.Entry e : this._skillsForAll.entrySet()) {
                        skill = new SkillData(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
                        if (!skill.exists()) continue;
                        player.addSkill(skill, false);
                    }
                    player.sendSkillList();
                    continue;
                }
                for (Map.Entry e : this._skillsForAll.entrySet()) {
                    skill = new SkillData(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
                    if (!skill.exists()) continue;
                    player.removeSkill(skill.getId());
                }
            }
        }
    }

    @Override
    protected void clockTick() throws Exception {
    }

    @Override
    public void onKill(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null) {
            return;
        }
        if (player.getTeamId() != target.getEventInfo().getTeamId()) {
            this.tryFirstBlood(player);
            this.giveOnKillReward(player);
            player.getEventTeam().raiseScore(1);
            player.getEventTeam().raiseKills(1);
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
    public void onDamageGive(CharacterData cha, CharacterData target, int damage, boolean isDOT) {
    }

    @Override
    public boolean canAttack(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null) {
            return true;
        }
        if (target.getEventInfo().getEvent() != player.getEvent()) {
            return false;
        }
        if (this.getPlayerData(target.getEventInfo()).getSuperTeam() == this.getPlayerData(player).getSuperTeam()) {
            return false;
        }
        if (this.isCommander(player)) {
            player.sendMessage("The commander can't attack.");
            return false;
        }
        return true;
    }

    protected boolean isCommander(PlayerEventInfo player) {
        if (this.getPlayerData(player).isCommander()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canSupport(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null || target.getEventInfo().getEvent() != player.getEvent()) {
            return false;
        }
        if (this.getPlayerData(player).getSuperTeam() == this.getPlayerData(target.getEventInfo()).getSuperTeam()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canUseSkill(PlayerEventInfo player, SkillData skill) {
        return false;
    }

    @Override
    public synchronized void clearEvent(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: called CLEAREVENT for instance " + instanceId);
        }
        try {
            if (this._matches != null) {
                for (TeamVsTeam.TvTEventInstance match : this._matches.values()) {
                    if (instanceId != 0 && instanceId != match.getInstance().getId()) continue;
                    match.abort();
                    this.spawnCommanderStuff(match.getInstance().getId(), false);
                    this.handleSkills(match.getInstance().getId(), false);
                    this.preparePlayers(match.getInstance().getId(), false);
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
    public String getHtmlDescription() {
        if (this._htmlDescription == null) {
            EventDescription desc = EventDescriptionSystem.getInstance().getDescription(this.getEventType());
            if (desc != null) {
                this._htmlDescription = desc.getDescription(this.getConfigs());
            } else {
                this._htmlDescription = "" + this.getInt("teamsCount") + " teams fighting against each other. ";
                this._htmlDescription = this._htmlDescription + "Gain score by killing your opponents";
                if (this.getInt("killsForReward") > 0) {
                    this._htmlDescription = this._htmlDescription + " (at least " + this.getInt("killsForReward") + " kill(s) is required to receive a reward)";
                }
                this._htmlDescription = this.getBoolean("waweRespawn") ? this._htmlDescription + " and dead players are resurrected by an advanced wawe-spawn engine each " + this.getInt("resDelay") + " seconds" : this._htmlDescription + " and if you die, you will be resurrected in " + this.getInt("resDelay") + " seconds";
                if (this.getBoolean("createParties")) {
                    this._htmlDescription = this._htmlDescription + ". The event automatically creates parties on start";
                }
                this._htmlDescription = this._htmlDescription + ".";
            }
        }
        return this._htmlDescription;
    }

    @Override
    public EventPlayerData createPlayerData(PlayerEventInfo player) {
        CommandersPlayerData d = new CommandersPlayerData(player, this);
        return d;
    }

    @Override
    public CommandersPlayerData getPlayerData(PlayerEventInfo player) {
        return (CommandersPlayerData)player.getEventData();
    }

    @Override
    protected TeamVsTeam.TvTEventData createEventData(int instanceId) {
        return new ComsEventData(instanceId);
    }

    @Override
    protected ComsEventInstance createEventInstance(InstanceData instance) {
        return new ComsEventInstance(instance);
    }

    @Override
    protected ComsEventData getEventData(int instance) {
        return (ComsEventData)((TeamVsTeam.TvTEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._data;
    }

    public class CommandersPlayerData
    extends PvPEventPlayerData {
        boolean _commander;
        int _superTeam;

        public CommandersPlayerData(PlayerEventInfo owner, EventGame event) {
            super(owner, event, new GlobalStatsModel(Commanders.this.getEventType()));
            this._commander = false;
        }

        private void setSuperTeam(int i) {
            this._superTeam = i;
        }

        private int getSuperTeam() {
            return this._superTeam;
        }

        private void setCommander(boolean b) {
            this._commander = b;
        }

        private boolean isCommander() {
            return this._commander;
        }
    }

    protected class ComsEventInstance
    extends TeamVsTeam.TvTEventInstance {
        protected ComsEventInstance(InstanceData instance) {
            super(Commanders.this, instance);
        }

        @Override
        public void run() {
            try {
                if (NexusLoader.detailedDebug) {
                    Commanders.this.print("Event: running task of state " + this._state.toString() + "...");
                }
                switch (this._state) {
                    case START: {
                        if (!Commanders.this.checkPlayers(this._instance.getId())) break;
                        Commanders.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, false);
                        Commanders.this.setupTitles(this._instance.getId());
                        Commanders.this.setupTeams(this._instance.getId(), Commanders.this._countOfSuperiorTeams);
                        Commanders.this.removeStaticDoors(this._instance.getId());
                        Commanders.this.enableMarkers(this._instance.getId(), true);
                        Commanders.this.spawnCommanderStuff(this._instance.getId(), true);
                        Commanders.this.handleSkills(this._instance.getId(), true);
                        Commanders.this.preparePlayers(this._instance.getId(), true);
                        Commanders.this.forceSitAll(this._instance.getId());
                        this.setNextState(TeamVsTeam.EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        Commanders.this.forceStandAll(this._instance.getId());
                        if (Commanders.this.getBoolean("createParties")) {
                            Commanders.this.createParties(Commanders.this.getInt("maxPartySize"));
                        }
                        this.setNextState(TeamVsTeam.EventState.END);
                        this._clock.startClock(Commanders.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        this.setNextState(TeamVsTeam.EventState.INACTIVE);
                        if (Commanders.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            Commanders.this.rewardAllTeams(this._instance.getId(), Commanders.this.getInt("killsForReward"), Commanders.this.getInt("killsForReward"));
                        }
                        Commanders.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    Commanders.this.print("Event: ... finished running task. next state " + this._state.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                Commanders.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    protected class ComsEventData
    extends TeamVsTeam.TvTEventData {
        private Map<Integer, PlayerEventInfo> _commanders;
        private Map<Integer, NpcData> _bases;

        public ComsEventData(int instance) {
            super(Commanders.this, instance);
            this._commanders = new FastMap();
            this._bases = new FastMap();
        }

        public PlayerEventInfo getCommander(int team) {
            return this._commanders.get(team);
        }

        public void setCommander(int team, PlayerEventInfo commander) {
            this._commanders.put(team, commander);
        }

        public NpcData getBase(int team) {
            return this._bases.get(team);
        }

        public void setBase(int team, NpcData base) {
            this._bases.put(team, base);
        }
    }

}

