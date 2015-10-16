/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.NexusOut
 *  cz.nxs.interf.PlayerEventInfo
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
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.PvPEventPlayerData;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.base.IEventInstance;
import cz.nxs.events.engine.main.base.MainEventInstanceType;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.mini.SpawnType;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.NexusOut;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.interf.delegate.SkillData;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import javolution.text.TextBuilder;
import javolution.util.FastMap;

public class CTF
extends AbstractMainEvent {
    private FastMap<Integer, EventInstance> _matches;
    private boolean _waweRespawn;
    private int _teamsCount;
    private int _flagNpcId;
    private int _holderNpcId;

    public CTF(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new String[]{"Winner", "Loser", "Tie"});
        this._configs.put((Object)"killsForReward", (Object)new ConfigModel("killsForReward", "0", "The minimum kills count required to get a reward (includes all possible rewards)."));
        this._configs.put((Object)"resDelay", (Object)new ConfigModel("resDelay", "15", "The delay after which the player is resurrected. In seconds."));
        this._configs.put((Object)"waweRespawn", (Object)new ConfigModel("waweRespawn", "true", "Enables the wawe-style respawn system.", ConfigModel.InputType.Boolean));
        this._configs.put((Object)"flagSkillId", (Object)new ConfigModel("flagSkillId", "-1", "Skill given to all players holding a flag. Possible to create for example a slow effect using a passive skill. -1 to disable."));
        this._configs.put((Object)"flagNpcId", (Object)new ConfigModel("flagNpcId", "8990", "Flag NPC Id. Same for all teams, only title/name will change."));
        this._configs.put((Object)"flagHolderNpcId", (Object)new ConfigModel("flagHolderNpcId", "8991", "Flag Holder NPC Id. Same for all teams, only title/name will change."));
        this._configs.put((Object)"teamsCount", (Object)new ConfigModel("teamsCount", "2", "The ammount of teams in the event. Max is 3 for CTF."));
        this._configs.put((Object)"afkReturnFlagTime", (Object)new ConfigModel("afkReturnFlagTime", "99999", "The time after which will be the flag returned from AFK player back to it's holder. -1 to disable, value in ms. NOT WORKING CURRENTLY."));
        this._configs.put((Object)"flagReturnTime", (Object)new ConfigModel("flagReturnTime", "120000", "The time after which the flag will be returned from player back to it's holder. -1 to disable, value in ms."));
        this._configs.put((Object)"createParties", (Object)new ConfigModel("createParties", "true", "Put 'True' if you want this event to automatically create parties for players in each team.", ConfigModel.InputType.Boolean));
        this._configs.put((Object)"maxPartySize", (Object)new ConfigModel("maxPartySize", "10", "The maximum size of party, that can be created. Works only if <font color=LEVEL>createParties</font> is true."));
        this._instanceTypeConfigs.put((Object)"teamsCount", (Object)new ConfigModel("teamsCount", "2", "You may specify the count of teams only for this instance. This config overrides event's default teams ammount."));
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
        this._runningInstances = 0;
    }

    protected int initInstanceTeams(MainEventInstanceType type) {
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
        if (this.getBoolean("createParties")) {
            this.createParties(this.getInt("maxPartySize"));
        }
        this._matches = new FastMap();
        for (InstanceData instance : this._instances) {
            EventInstance match = new EventInstance(instance);
            this._matches.put((Object)instance.getId(), (Object)match);
            ++this._runningInstances;
            match.scheduleNextTask(0);
        }
    }

    @Override
    public void onEventEnd() {
        int minKills = this.getInt("killsForReward");
        this.rewardAllTeams(-1, 0, minKills);
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

    protected synchronized void endInstance(int instance, boolean canBeAborted) {
        ((EventInstance)this._matches.get((Object)instance)).setNextState(EventState.END);
        if (canBeAborted) {
            ((EventInstance)this._matches.get((Object)instance)).setCanBeAborted();
        }
        ((EventInstance)this._matches.get((Object)instance)).getClock().setTime(0);
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
        EventInstance match = (EventInstance)this._matches.get((Object)instance);
        if (count <= 3 && match != null && match.getClock() != null) {
            tb.append("Time: " + match.getClock().getTime());
        }
        return tb.toString();
    }

    @Override
    protected String getTitle(PlayerEventInfo pi) {
        if (pi.isAfk()) {
            return "AFK";
        }
        return "Score: " + this.getEventData(pi).getScore();
    }

    @Override
    public synchronized boolean onNpcAction(PlayerEventInfo player, NpcData npc) {
        int instance = player.getInstanceId();
        EventInstance match = (EventInstance)this._matches.get((Object)instance);
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
                    if (this.getEventData(player).hasFlag > 0) {
                        this.screenAnnounce(instance, player.getPlayersName() + " scores for the " + player.getEventTeam().getFullName() + " team! The " + this.getTeamName(this.getEventData(player).hasFlag) + " flag has been returned.");
                        player.getEventTeam().raiseScore(1);
                        this.getEventData(player).raiseScore(1);
                        this.setScoreStats(player, this.getEventData(player).getScore());
                        this.returnFlag(this.getEventData(player).hasFlag, false, false, instance, false);
                        if (player.isTitleUpdated()) {
                            player.setTitle(this.getTitle(player), true);
                            player.broadcastTitleInfo();
                        }
                        player.sendMessage("Congratulations! You've scored.");
                    } else {
                        player.sendMessage("Go for enemy's flag!");
                    }
                    return true;
                }
                if (status == 2) {
                    player.sendMessage("Your flag has been stolen.");
                    return true;
                }
                if (status == 3) {
                    player.sendMessage("Your flag has been stolen.");
                    return true;
                }
            } else {
                if (status == 1) {
                    this.equipFlag(player, npcTeam);
                    player.creatureSay("You've taken the flag! Run back!", "CTF", 15);
                    return true;
                }
                if (status == 2) {
                    player.sendMessage("This flag has been already stolen.");
                    return true;
                }
                if (status == 3) {
                    player.sendMessage("This flag has been already stolen.");
                    return true;
                }
            }
        } else if (isFlag) {
            if (npcTeam == player.getTeamId()) {
                if (status == 1) {
                    if (this.getEventData(player).hasFlag > 0) {
                        this.screenAnnounce(instance, player.getPlayersName() + " scores for the " + player.getEventTeam().getFullName() + " team! The " + this.getTeamName(this.getEventData(player).hasFlag) + " flag has been returned.");
                        player.getEventTeam().raiseScore(1);
                        this.getEventData(player).raiseScore(1);
                        this.setScoreStats(player, this.getEventData(player).getScore());
                        this.returnFlag(this.getEventData(player).hasFlag, false, false, instance, false);
                        if (player.isTitleUpdated()) {
                            player.setTitle(this.getTitle(player), true);
                            player.broadcastTitleInfo();
                        }
                        player.sendMessage("Congratulations! You've scored.");
                    } else {
                        player.sendMessage("Go for enemy's flag!");
                    }
                    return true;
                }
                if (status == 3) {
                    this.returnFlag(npcTeam, false, false, instance, false);
                    player.creatureSay("The " + this.getTeamName(npcTeam) + " flag has been returned by " + player.getPlayersName() + ".", "CTF", 15);
                    return true;
                }
            } else {
                if (status == 1) {
                    this.equipFlag(player, npcTeam);
                    player.creatureSay("You've taken the flag! Run back!", "CTF", 15);
                    return true;
                }
                if (status == 3) {
                    this.equipFlag(player, npcTeam);
                    player.creatureSay("You've picked up the flag! Run back!", "CTF", 15);
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
            player.getEventTeam().raiseKills(1);
            this.getEventData(player).raiseKills(1);
            if (player.isTitleUpdated()) {
                player.setTitle(this.getTitle(player), true);
                player.broadcastTitleInfo();
            }
            this.setKillsStats(player, this.getEventData(player).getKills());
        }
    }

    @Override
    public void onDie(PlayerEventInfo player, CharacterData killer) {
        if (this.getEventData(player).hasFlag > 0) {
            this.dropFlag(this.getEventData(player).hasFlag, player.getInstanceId());
        }
        this.getEventData(player).raiseDeaths(1);
        this.setDeathsStats(player, this.getEventData(player).getDeaths());
        if (this._waweRespawn) {
            this._waweScheduler.addPlayer(player);
        } else {
            this.scheduleRevive(player, this.getInt("resDelay") * 1000);
        }
    }

    @Override
    public boolean canUseItem(PlayerEventInfo player, ItemData item) {
        if (this.getEventData(player).hasFlag > 0 && (item.isWeapon() || item.isArmor() && item.getBodyPart() == 256)) {
            return false;
        }
        return super.canUseItem(player, item);
    }

    @Override
    public boolean canDestroyItem(PlayerEventInfo player, ItemData item) {
        if (this.getEventData(player).hasFlag > 0 && item.isWeapon()) {
            return false;
        }
        return super.canDestroyItem(player, item);
    }

    private void spawnFlags(int instance) {
        this.clearMapHistory(-1, SpawnType.Flag);
        EventInstance match = (EventInstance)this._matches.get((Object)instance);
        for (EventTeam team : ((FastMap)this._teams.get((Object)instance)).values()) {
            EventSpawn sp = this.getSpawn(SpawnType.Flag, team.getTeamId());
            EventInstance.access$400((EventInstance)match)[team.getTeamId() - 1].flagNpc = this.spawnNPC(sp.getLoc().getX(), sp.getLoc().getY(), sp.getLoc().getZ(), this._flagNpcId, instance, this.getTeamName(team.getTeamId()) + " Flag", this.getTeamName(team.getTeamId()) + " Team");
            EventInstance.access$400((EventInstance)match)[team.getTeamId() - 1].flagHolder = this.spawnNPC(sp.getLoc().getX(), sp.getLoc().getY(), sp.getLoc().getZ(), this._holderNpcId, instance, this.getTeamName(team.getTeamId()) + " Holder", "");
            EventInstance.access$400((EventInstance)match)[team.getTeamId() - 1].flagNpc.setEventTeam(team.getTeamId());
            EventInstance.access$400((EventInstance)match)[team.getTeamId() - 1].flagHolder.setEventTeam(team.getTeamId());
        }
    }

    private void unspawnFlags(int instance) {
        for (FlagData data : ((EventInstance)this._matches.get((Object)instance))._flags) {
            data.returnTask.abort();
            if (data.flagNpc != null) {
                data.flagNpc.deleteMe();
            }
            if (data.flagHolder == null) continue;
            data.flagHolder.deleteMe();
        }
    }

    public EventPlayerData createEventData(PlayerEventInfo player) {
        CTFEventPlayerData d = new CTFEventPlayerData(player, this);
        return d;
    }

    public CTFEventPlayerData getEventData(PlayerEventInfo player) {
        return (CTFEventPlayerData)player.getEventData();
    }

    @Override
    public synchronized void clearEvent(int instanceId) {
        if (this._matches != null) {
            for (EventInstance match : this._matches.values()) {
                if (instanceId != 0 && instanceId != match.getInstance().getId()) continue;
                match.abort();
                for (FlagData flag : match._flags) {
                    if (flag.flagOwner == null) continue;
                    this.unequipFlag(flag.flagOwner);
                }
                this.unspawnFlags(match.getInstance().getId());
            }
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
            player.restoreData();
            player.teleport(player.getOrigLoc(), 0, true, 0);
            player.sendMessage("You're being teleported back to you previous location.");
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
            pi.teleport(loc, 0, true, pi.getInstanceId());
            pi.sendMessage("You've been respawned.");
        } else {
            this.debug("Error on respawnPlayer - no spawn type REGULAR, team " + pi.getTeamId() + " has been found. Event aborted.");
        }
    }

    @Override
    public String getEstimatedTimeLeft() {
        if (this._matches == null) {
            return "Starting";
        }
        Iterator i$ = this._matches.values().iterator();
        if (i$.hasNext()) {
            EventInstance match = (EventInstance)i$.next();
            return match.getClock().getTime();
        }
        return null;
    }

    private void dropFlag(int flagTeam, int instance) {
        EventInstance match = (EventInstance)this._matches.get((Object)instance);
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
    }

    private void returnFlag(int flagTeam, boolean timeForced, boolean afkForced, int instance, boolean announce) {
        EventInstance match = (EventInstance)this._matches.get((Object)instance);
        FlagData data = match._flags[flagTeam - 1];
        data.returnTask.abort();
        if (announce) {
            if (afkForced) {
                if (data.flagOwner != null) {
                    this.announce(instance, data.flagOwner.getPlayersName() + " went afk while holding a flag. The " + this.getTeamName(flagTeam) + " flag has been returned.");
                } else {
                    this.announce(instance, "The " + this.getTeamName(flagTeam) + " flag returned, the flag holder went afk.");
                }
            } else if (!timeForced) {
                this.screenAnnounce(instance, "The " + this.getTeamName(flagTeam) + " flag returned!");
            } else if (data.flagOwner != null) {
                this.announce(instance, data.flagOwner.getPlayersName() + " didn't manage to score or return his flag in the " + this.getInt("flagReturnTime") / 1000 + " seconds time limit. The " + this.getTeamName(flagTeam) + " flag has been returned.");
            } else {
                this.announce(instance, "The " + this.getTeamName(flagTeam) + " flag returned, the time limit of " + this.getInt("flagReturnTime") / 1000 + " seconds has passed!");
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
    }

    private void equipFlag(PlayerEventInfo player, int flagTeamId) {
        int instance = player.getInstanceId();
        EventInstance match = (EventInstance)this._matches.get((Object)instance);
        FlagData data = match._flags[flagTeamId - 1];
        ItemData wpn = player.getPaperdollItem(5);
        if (wpn != null) {
            player.unEquipItemInBodySlotAndRecord(128);
        }
        if ((wpn = player.getPaperdollItem(7)) != null) {
            player.unEquipItemInBodySlotAndRecord(256);
        }
        ItemData flagItem = player.addItem(13535, 1, false);
        player.equipItem(flagItem);
        data.flagOwner = player;
        this.screenAnnounce(instance, player.getPlayersName() + " has taken the " + this.getTeamName(flagTeamId) + " flag!");
        data.flagNpc.deleteMe();
        data.status = 2;
        this.getEventData(player).hasFlag = flagTeamId;
        player.broadcastUserInfo();
        int id = this.getInt("flagSkillId");
        if (id != -1) {
            player.addSkill(new SkillData(id, 1), false);
        }
        data.returnTask.start();
    }

    private void unequipFlag(PlayerEventInfo player) {
        int id;
        int instance = player.getInstanceId();
        ItemData wpn = player.getPaperdollItem(5);
        if (wpn.exists()) {
            ItemData[] unequiped = player.unEquipItemInBodySlotAndRecord(wpn.getBodyPart());
            player.destroyItemByItemId(13535, 1);
            player.inventoryUpdate(unequiped);
        }
        if ((id = this.getInt("flagSkillId")) != -1) {
            player.removeSkill(id);
        }
        for (FlagData flag : ((EventInstance)this._matches.get((Object)instance))._flags) {
            if (flag.flagOwner == null || flag.flagOwner.getPlayersId() != player.getPlayersId()) continue;
            flag.flagOwner = null;
            flag.returnTask.abort();
        }
        this.getEventData(player).hasFlag = 0;
    }

    @Override
    public void onDisconnect(PlayerEventInfo player) {
        if (player.isOnline() && this.getEventData(player).hasFlag > 0) {
            this.announce("Player " + player.getPlayersName() + " holding " + this.getTeamName(this.getEventData(player).hasFlag) + " team's flag disconnected.");
            this.screenAnnounce(player.getInstanceId(), "The " + this.getTeamName(this.getEventData(player).hasFlag) + " flag has been returned.");
            this.returnFlag(this.getEventData(player).hasFlag, false, false, player.getInstanceId(), false);
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

    protected IEventInstance getMatch(int instanceId) {
        return (IEventInstance)this._matches.get((Object)instanceId);
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
            this.future = (time = CTF.this.getInt("flagReturnTime")) > 0 ? NexusOut.scheduleGeneral((Runnable)this, (long)time) : null;
        }

        public void abort() {
            if (this.future != null) {
                this.future.cancel(false);
            }
        }

        @Override
        public void run() {
            CTF.this.returnFlag(this.team, true, false, this.instance, true);
        }
    }

    public class CTFEventPlayerData
    extends PvPEventPlayerData {
        private int hasFlag;

        public CTFEventPlayerData(PlayerEventInfo owner, EventGame event) {
            super(owner, event);
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

    private class EventInstance
    implements Runnable,
    IEventInstance {
        private InstanceData _instance;
        private EventState _state;
        private AbstractMainEvent.Clock _clock;
        private boolean _canBeAborted;
        private ScheduledFuture<?> _task;
        private FlagData[] _flags;

        private EventInstance(InstanceData instance) {
            this._canBeAborted = false;
            this._task = null;
            this._instance = instance;
            this._state = EventState.START;
            this._clock = new AbstractMainEvent.Clock((AbstractMainEvent)CTF.this, (IEventInstance)this);
            this._flags = new FlagData[CTF.this._teamsCount];
            for (EventTeam team : ((FastMap)CTF.this._teams.get((Object)instance.getId())).values()) {
                this._flags[team.getTeamId() - 1] = new FlagData(team.getTeamId(), instance.getId());
            }
        }

        protected void setNextState(EventState state) {
            this._state = state;
        }

        @Override
        public boolean isActive() {
            return this._state != EventState.INACTIVE;
        }

        public void setCanBeAborted() {
            this._canBeAborted = true;
        }

        @Override
        public InstanceData getInstance() {
            return this._instance;
        }

        @Override
        public AbstractMainEvent.Clock getClock() {
            return this._clock;
        }

        @Override
        public ScheduledFuture<?> scheduleNextTask(int time) {
            if (time > 0) {
                this._task = NexusOut.scheduleGeneral((Runnable)this, (long)time);
            } else {
                NexusOut.executeTask((Runnable)this);
            }
            return this._task;
        }

        public void abort() {
            if (this._task != null) {
                this._task.cancel(false);
            }
            this._clock.abort();
        }

        @Override
        public void run() {
            try {
                switch (this._state) {
                    case START: {
                        CTF.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, false);
                        CTF.this.preparePlayers(this._instance.getId());
                        CTF.this.spawnFlags(this._instance.getId());
                        CTF.this.forceSitAll(this._instance.getId());
                        this.setNextState(EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        CTF.this.forceStandAll(this._instance.getId());
                        this.setNextState(EventState.END);
                        this._clock.startClock(CTF.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0);
                        CTF.this.unspawnFlags(this._instance.getId());
                        this.setNextState(EventState.INACTIVE);
                        if (CTF.this.instanceEnded() || !this._canBeAborted) break;
                        CTF.this.clearEvent(this._instance.getId());
                    }
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                CTF.this._manager.endDueToError("An error in the Event Engine occured. The event has been aborted.");
            }
        }
    }

    private class FlagData {
        public int team;
        public NpcData flagNpc;
        public NpcData flagHolder;
        public int status;
        public PlayerEventInfo flagOwner;
        public FlagReturnTask returnTask;

        public FlagData(int team, int instance) {
            this.team = team;
            this.status = 1;
            this.flagNpc = null;
            this.flagHolder = null;
            this.flagOwner = null;
            this.returnTask = new FlagReturnTask(team, instance);
        }
    }

}

