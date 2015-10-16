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
 *  cz.nxs.interf.delegate.PartyData
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.PvPEventPlayerData;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.base.description.EventDescription;
import cz.nxs.events.engine.base.description.EventDescriptionSystem;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.main.events.TeamVsTeam;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.IValues;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import javolution.util.FastMap;

public class HuntingGrounds
extends TeamVsTeam {
    protected int _tick;
    protected int _bowItemId;
    protected int _arrowItemId;
    protected boolean _ammoSystem;
    protected int _ammoAmmount;
    protected int _ammoRegPerTick;
    protected int _tickLength;
    private FastMap<Integer, Integer> _skillsForAll;

    public HuntingGrounds(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Winner, RewardPosition.Looser, RewardPosition.Tie, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill, RewardPosition.KillingSpree});
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.addConfig(new ConfigModel("skillsForAllPlayers", "35100-1", "IDs of skills which will be given to players on the event. The purpose of this is to make all players equally strong. Format: <font color=LEVEL>SKILLID-LEVEL</font> (eg. '35000-1').", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("bowWeaponId", "271", "The ID of the bow item which will be given to all players and will be the only weapon most players will use during the event. This weapon kills players with just one hit."));
        this.addConfig(new ConfigModel("arrowItemId", "17", "The ID of the arrows which will be given to the player in the event."));
        this.addConfig(new ConfigModel("enableAmmoSystem", "true", "Enable/disable the ammo system based on player's mana. Player's max MP is defaultly modified by a custom passive skill and everytime a player shots and arrow, his MP decreases by a value which is calculated from the ammount of ammo. There is also a MP regeneration system - see the configs below.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("ammoAmmount", "10", "Works if ammo system is enabled. Specifies the max ammount of ammo every player can have."));
        this.addConfig(new ConfigModel("ammoRestoredPerTick", "1", "Works if ammo system is enabled. Defines the ammount of ammo given to every player each <font color=LEVEL>'ammoRegTickInterval'</font> (configurable) seconds."));
        this.addConfig(new ConfigModel("ammoRegTickInterval", "10", "Works if ammo system is enabled. Defines the interval of restoring player's ammo. The value is in seconds (eg. value 10 will give ammo every 10 seconds to every player - the ammount of restored ammo is configurable (config <font color=LEVEL>ammoRestoredPerTick</font>)."));
    }

    @Override
    public void initEvent() {
        super.initEvent();
        this._bowItemId = this.getInt("bowWeaponId");
        this._arrowItemId = this.getInt("arrowItemId");
        this._ammoSystem = this.getBoolean("enableAmmoSystem");
        this._ammoAmmount = this.getInt("ammoAmmount");
        this._ammoRegPerTick = this.getInt("ammoRestoredPerTick");
        this._tickLength = this.getInt("ammoRegTickInterval");
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

    protected void preparePlayers(int instanceId, boolean start) {
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

    protected void handleWeapons(int instanceId, boolean equip) {
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            ItemData wpn;
            if (equip) {
                wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_RHAND());
                if (wpn != null) {
                    player.unEquipItemInBodySlotAndRecord(CallBack.getInstance().getValues().SLOT_R_HAND());
                }
                if ((wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_LHAND())) != null) {
                    player.unEquipItemInBodySlotAndRecord(CallBack.getInstance().getValues().SLOT_L_HAND());
                }
                ItemData flagItem = player.addItem(this._bowItemId, 1, false);
                player.equipItem(flagItem);
                player.addItem(this._arrowItemId, 400, false);
                continue;
            }
            wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_RHAND());
            if (!wpn.exists()) continue;
            ItemData[] unequiped = player.unEquipItemInBodySlotAndRecord(wpn.getBodyPart());
            player.destroyItemByItemId(this._bowItemId, 1);
            player.inventoryUpdate(unequiped);
        }
    }

    @Override
    protected void clockTick() throws Exception {
        ++this._tick;
        if (this._tick % this._tickLength != 0) {
            return;
        }
        if (this._ammoSystem) {
            int oneAmmoMp = 0;
            for (TeamVsTeam.TvTEventInstance match : this._matches.values()) {
                for (PlayerEventInfo player : this.getPlayers(match.getInstance().getId())) {
                    try {
                        oneAmmoMp = player.getMaxMp() / this._ammoAmmount;
                        int mpToRegenerate = this._ammoRegPerTick * oneAmmoMp;
                        int currentMp = (int)player.getCurrentMp();
                        if (currentMp >= player.getMaxMp()) continue;
                        int toAdd = mpToRegenerate;
                        if (currentMp + mpToRegenerate > player.getMaxMp()) {
                            toAdd = player.getMaxMp() - currentMp;
                        }
                        player.setCurrentMp(currentMp + toAdd);
                    }
                    catch (NullPointerException e) {}
                }
            }
        }
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
    public boolean onAttack(CharacterData cha, CharacterData target) {
        if (this._ammoSystem && cha.isPlayer() && target.isPlayer()) {
            PlayerEventInfo player = cha.getEventInfo();
            int oneShotMp = player.getMaxMp() / this._ammoAmmount;
            if (player.getCurrentMp() >= (double)oneShotMp) {
                player.setCurrentMp((int)(player.getCurrentMp() - (double)oneShotMp));
            } else {
                player.sendMessage("Not enought MP.");
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canUseItem(PlayerEventInfo player, ItemData item) {
        if (item.getItemId() == this._bowItemId && item.isEquipped()) {
            return false;
        }
        if (item.isWeapon()) {
            return false;
        }
        return super.canUseItem(player, item);
    }

    @Override
    public void onDamageGive(CharacterData cha, CharacterData target, int damage, boolean isDOT) {
        try {
            if (cha.isPlayer() && target.isPlayer()) {
                PlayerEventInfo targetPlayer = target.getEventInfo();
                targetPlayer.abortCasting();
                targetPlayer.doDie(cha);
            }
        }
        catch (NullPointerException e) {
            // empty catch block
        }
    }

    @Override
    public boolean canDestroyItem(PlayerEventInfo player, ItemData item) {
        if (item.getItemId() == this._bowItemId) {
            return false;
        }
        return super.canDestroyItem(player, item);
    }

    @Override
    public boolean canSupport(PlayerEventInfo player, CharacterData target) {
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
                    this.handleWeapons(match.getInstance().getId(), false);
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
    protected TeamVsTeam.TvTEventData createEventData(int instanceId) {
        return new HGEventData(instanceId);
    }

    @Override
    protected HGEventInstance createEventInstance(InstanceData instance) {
        return new HGEventInstance(instance);
    }

    @Override
    protected HGEventData getEventData(int instance) {
        return (HGEventData)((TeamVsTeam.TvTEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._data;
    }

    protected class HGEventInstance
    extends TeamVsTeam.TvTEventInstance {
        protected HGEventInstance(InstanceData instance) {
            super(HuntingGrounds.this, instance);
        }

        @Override
        public void run() {
            try {
                if (NexusLoader.detailedDebug) {
                    HuntingGrounds.this.print("Event: running task of state " + this._state.toString() + "...");
                }
                switch (this._state) {
                    case START: {
                        if (!HuntingGrounds.this.checkPlayers(this._instance.getId())) break;
                        HuntingGrounds.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, false);
                        HuntingGrounds.this.setupTitles(this._instance.getId());
                        HuntingGrounds.this.removeStaticDoors(this._instance.getId());
                        HuntingGrounds.this.enableMarkers(this._instance.getId(), true);
                        HuntingGrounds.this.handleWeapons(this._instance.getId(), true);
                        HuntingGrounds.this.handleSkills(this._instance.getId(), true);
                        HuntingGrounds.this.preparePlayers(this._instance.getId(), true);
                        HuntingGrounds.this.forceSitAll(this._instance.getId());
                        this.setNextState(TeamVsTeam.EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        HuntingGrounds.this.forceStandAll(this._instance.getId());
                        if (HuntingGrounds.this.getBoolean("createParties")) {
                            HuntingGrounds.this.createParties(HuntingGrounds.this.getInt("maxPartySize"));
                        }
                        this.setNextState(TeamVsTeam.EventState.END);
                        this._clock.startClock(HuntingGrounds.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        this.setNextState(TeamVsTeam.EventState.INACTIVE);
                        if (HuntingGrounds.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            HuntingGrounds.this.rewardAllTeams(this._instance.getId(), HuntingGrounds.this.getInt("killsForReward"), HuntingGrounds.this.getInt("killsForReward"));
                        }
                        HuntingGrounds.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    HuntingGrounds.this.print("Event: ... finished running task. next state " + this._state.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                HuntingGrounds.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    protected class HGEventData
    extends TeamVsTeam.TvTEventData {
        public HGEventData(int instance) {
            super(HuntingGrounds.this, instance);
        }
    }

}

