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
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.EventRewardSystem;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventMap;
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
import cz.nxs.events.engine.main.events.Deathmatch;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IValues;
import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class TreasureHunt
extends Deathmatch {
    protected FastMap<Integer, THEventInstance> _matches;
    protected boolean _waweRespawn;
    protected boolean _antifeed;
    protected int _normalChestChance;
    protected int _fakeChestChance;
    protected int _luckyChestChance;
    protected int _ancientChestChance;
    protected int _unluckyChestChance;
    protected int _explodingChestChance;
    protected int _nukeChestChance;
    protected int _normalChestNpcId;
    protected int _fakeChestNpcId;
    protected int _luckyChestNpcId;
    protected int _ancientChestNpcId;
    protected int _unluckyChestNpcId;
    protected int _explodingChestNpcId;
    protected int _nukeChestNpcId;
    private int _endCheckInterval;
    protected boolean _allowPvp = false;
    protected int _countOfChests;

    public TreasureHunt(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Looser, RewardPosition.Tie, RewardPosition.Numbered, RewardPosition.Range, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.ChestReward, RewardPosition.ChestRewardLucky, RewardPosition.ChestRewardAncient});
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.addConfig(new ConfigModel("normalChestChance", "75000", "The chance in percent to spawn a normal chest. 100 000 equals 100%."));
        this.addConfig(new ConfigModel("luckyChestChance", "10000", "The chance in percent to spawn a lucky chest. 100 000 equals 100%."));
        this.addConfig(new ConfigModel("ancientChestChance", "2000", "The chance in percent to spawn an ancient chest. 100 000 equals 100%."));
        this.addConfig(new ConfigModel("unluckyChestChance", "2500", "The chance in percent to spawn an unlucky chest. 100 000 equals 100%."));
        this.addConfig(new ConfigModel("fakeChestChance", "2500", "The chance in percent to spawn a fake chest. 100 000 equals 100%."));
        this.addConfig(new ConfigModel("explodingChestChance", "7500", "The chance in percent to spawn a exploding chest. 100 000 equals 100%."));
        this.addConfig(new ConfigModel("nukeChestChance", "500", "The chance in percent to spawn a nuke chest. 100 000 equals 100%."));
        this.addConfig(new ConfigModel("normalChestNpcId", "8689", "The NpcId in percent to spawn a normal chest. "));
        this.addConfig(new ConfigModel("luckyChestNpcId", "8688", "The NpcId in percent to spawn a lucky chest. "));
        this.addConfig(new ConfigModel("ancientChestNpcId", "8687", "The NpcId in percent to spawn a ancient chest. "));
        this.addConfig(new ConfigModel("unluckyChestNpcId", "8686", "The NpcId in percent to spawn a unlucky chest. "));
        this.addConfig(new ConfigModel("fakeChestNpcId", "8685", "The NpcId in percent to spawn a fake chest. "));
        this.addConfig(new ConfigModel("explodingChestNpcId", "8684", "The NpcId in percent to spawn a exploding chest. "));
        this.addConfig(new ConfigModel("nukeChestNpcId", "8683", "The NpcId in percent to spawn a nuke chest. "));
        this.addConfig(new ConfigModel("checkInactiveDelay", "300", "In seconds. If no chests are opened within this time, the event will be aborted. Eg. if you set this 120 and nobody manages to find and open a chest for 120 seconds, the event will be ended. Disable this by setting 0."));
        this.addConfig(new ConfigModel("scoreForReward", "0", "The minimum of score required to get a reward (includes all possible rewards). Score is gained by killing chests."));
        this.addConfig(new ConfigModel("resDelay", "15", "The delay after which a dead player is resurrected. In seconds."));
        if (this._allowPvp) {
            this.addConfig(new ConfigModel("waweRespawn", "true", "Enables the wawe-style respawn system.", ConfigModel.InputType.Boolean));
            this.addConfig(new ConfigModel("firstBloodMessage", "true", "You can turn off/on the first blood announce in the event (first kill made in the event). This is also rewardable - check out reward type FirstBlood.", ConfigModel.InputType.Boolean));
            this.addConfig(new ConfigModel("antifeedProtection", "true", "Enables the special anti-feed protection. This protection changes player's name, title, race, clan/ally crest, class and basically all of his apperance, sometimes also gender.", ConfigModel.InputType.Boolean));
        } else {
            this.removeConfig("killsForReward");
            this.removeConfig("waweRespawn");
        }
    }

    @Override
    public void initEvent() {
        super.initEvent();
        if (this._allowPvp) {
            this._waweRespawn = this.getBoolean("waweRespawn");
            this._antifeed = this.getBoolean("antifeedProtection");
            if (this._waweRespawn) {
                this.initWaweRespawns(this.getInt("resDelay"));
            }
        }
        this._normalChestChance = this.getInt("normalChestChance");
        this._fakeChestChance = this.getInt("fakeChestChance");
        this._luckyChestChance = this.getInt("luckyChestChance");
        this._ancientChestChance = this.getInt("ancientChestChance");
        this._unluckyChestChance = this.getInt("unluckyChestChance");
        this._explodingChestChance = this.getInt("explodingChestChance");
        this._nukeChestChance = this.getInt("nukeChestChance");
        this._normalChestNpcId = this.getInt("normalChestNpcId");
        this._fakeChestNpcId = this.getInt("fakeChestNpcId");
        this._luckyChestNpcId = this.getInt("luckyChestNpcId");
        this._ancientChestNpcId = this.getInt("ancientChestNpcId");
        this._unluckyChestNpcId = this.getInt("unluckyChestNpcId");
        this._explodingChestNpcId = this.getInt("explodingChestNpcId");
        this._nukeChestNpcId = this.getInt("nukeChestNpcId");
        this._endCheckInterval = this.getInt("checkInactiveDelay");
        this._countOfChests = this.getInt("countOfChests");
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
            THEventInstance match = this.createEventInstance(instance);
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

    public void spawnChest(int instanceId, ChestType type, EventSpawn sp) {
        if (sp == null) {
            return;
        }
        Loc loc = sp.getLoc();
        loc.addRadius(sp.getRadius());
        int npcId = this.getChestId(type);
        NpcData npc = this.spawnNPC(loc.getX(), loc.getY(), loc.getZ(), npcId, instanceId, null, null);
        this.getEventData(instanceId).addChest(npc);
    }

    public void disequipWeapons(int instanceId) {
        if (this._allowPvp) {
            return;
        }
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            ItemData wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_RHAND());
            if (wpn != null) {
                player.unEquipItemInBodySlotAndRecord(CallBack.getInstance().getValues().SLOT_R_HAND());
            }
            if ((wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_LHAND())) == null) continue;
            player.unEquipItemInBodySlotAndRecord(CallBack.getInstance().getValues().SLOT_L_HAND());
        }
    }

    public void spawnChests(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: spawning " + this._countOfChests + " chests");
        }
        for (EventSpawn spawn : this._manager.getMap().getSpawns(-1, SpawnType.Chest)) {
            if (this.random() < this._nukeChestChance) {
                this.spawnChest(instanceId, ChestType.NUKE, spawn);
                continue;
            }
            if (this.random() < this._explodingChestChance) {
                this.spawnChest(instanceId, ChestType.EXPLODING, spawn);
                continue;
            }
            if (this.random() < this._unluckyChestChance) {
                this.spawnChest(instanceId, ChestType.UNLUCKY, spawn);
                continue;
            }
            if (this.random() < this._fakeChestChance) {
                this.spawnChest(instanceId, ChestType.FAKE, spawn);
                continue;
            }
            if (this.random() < this._ancientChestChance) {
                this.spawnChest(instanceId, ChestType.ANCIENT, spawn);
                continue;
            }
            if (this.random() < this._luckyChestChance) {
                this.spawnChest(instanceId, ChestType.LUCKY, spawn);
                continue;
            }
            this.spawnChest(instanceId, ChestType.NORMAL, spawn);
        }
    }

    private int random() {
        return CallBack.getInstance().getOut().random(100000);
    }

    public void unspawnChests(int instanceId) {
        for (NpcData npc : this.getEventData((int)instanceId)._chests) {
            if (npc == null) continue;
            npc.deleteMe();
            this.getEventData(instanceId).removeChest(npc);
        }
    }

    @Override
    public void onEventEnd() {
        if (NexusLoader.detailedDebug) {
            this.print("Event: onEventEnd()");
        }
        int minScore = this.getInt("scoreForReward");
        int minKills = this.getInt("killsForReward");
        this.rewardAllPlayers(-1, minScore, minKills);
        if (this._allowPvp && this._antifeed) {
            for (PlayerEventInfo player : this.getPlayers(0)) {
                player.stopAntifeedProtection(false);
            }
        }
    }

    @Override
    protected String getScorebar(int instance) {
        TextBuilder tb = new TextBuilder();
        int top = 0;
        for (PlayerEventInfo player : this.getPlayers(instance)) {
            if (this.getPlayerData(player).getScore() <= top) continue;
            top = this.getPlayerData(player).getScore();
        }
        tb.append("Top score: " + top);
        tb.append("   Time: " + ((THEventInstance)this._matches.get((Object)instance)).getClock().getTime());
        return tb.toString();
    }

    @Override
    protected String getTitle(PlayerEventInfo pi) {
        if (pi.isAfk()) {
            return "AFK";
        }
        return "Score: " + this.getPlayerData(pi).getScore();
    }

    protected void checkEventEnd(int instance) {
        if (this.getEventData(instance)._endCheckerFuture != null) {
            this.getEventData(instance)._endCheckerFuture.cancel(false);
            this.getEventData(instance)._endCheckerFuture = null;
        }
        if (this.getEventData((int)instance)._chests.isEmpty()) {
            this.announce("All chests were killed. Event has ended.");
            this.endInstance(instance, true, false, false);
        } else {
            this.chestOpened(instance);
        }
    }

    private void chestOpened(int instance) {
        if (this._endCheckInterval > 0) {
            this.getEventData(instance)._endCheckerFuture = CallBack.getInstance().getOut().scheduleGeneral(new EndChecker(instance), this._endCheckInterval * 1000);
        }
    }

    @Override
    public void onKill(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null) {
            if (target.getNpc() != null) {
                this.selectChestOnKillAction(player.getInstanceId(), player, target.getNpc());
                this.getEventData(player.getInstanceId()).removeChest(target.getNpc());
                this.checkEventEnd(player.getInstanceId());
            }
        } else if (this._allowPvp) {
            this.tryFirstBlood(player);
            this.giveOnKillReward(player);
            this.getPlayerData(player).raiseKills(1);
            this.getPlayerData(player).raiseSpree(1);
            if (player.isTitleUpdated()) {
                player.setTitle(this.getTitle(player), true);
                player.broadcastTitleInfo();
            }
            CallbackManager.getInstance().playerKills(this.getEventType(), player, target.getEventInfo());
            this.setKillsStats(player, this.getPlayerData(player).getKills());
        }
    }

    protected void selectChestOnKillAction(int instanceId, PlayerEventInfo player, NpcData npc) {
        ChestType type = this.getChestType(npc);
        if (type != null) {
            switch (type) {
                case NORMAL: {
                    this.getPlayerData(player).raiseScore(1);
                    if (player.isTitleUpdated()) {
                        player.setTitle(this.getTitle(player), true);
                        player.broadcastTitleInfo();
                    }
                    player.screenMessage("You have scored!", this.getEventName(), false);
                    EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.ChestReward, null, player.getTotalTimeAfk(), 0, 0);
                    this.setScoreStats(player, this.getPlayerData(player).getScore());
                    break;
                }
                case LUCKY: {
                    this.getPlayerData(player).raiseScore(2);
                    if (player.isTitleUpdated()) {
                        player.setTitle(this.getTitle(player), true);
                        player.broadcastTitleInfo();
                    }
                    player.screenMessage("You have scored! Double points!", this.getEventName(), false);
                    EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.ChestRewardLucky, null, player.getTotalTimeAfk(), 0, 0);
                    this.setScoreStats(player, this.getPlayerData(player).getScore());
                    break;
                }
                case UNLUCKY: {
                    if (this.getPlayerData(player).getScore() <= 0) break;
                    this.getPlayerData(player).raiseScore(-1);
                    if (player.isTitleUpdated()) {
                        player.setTitle(this.getTitle(player), true);
                        player.broadcastTitleInfo();
                    }
                    player.screenMessage("Bad chest, -1 score.", this.getEventName(), false);
                    this.setScoreStats(player, this.getPlayerData(player).getScore());
                    break;
                }
                case FAKE: {
                    player.screenMessage("This chest wasn't real.", this.getEventName(), false);
                    break;
                }
                case ANCIENT: {
                    this.getPlayerData(player).raiseScore(5);
                    if (player.isTitleUpdated()) {
                        player.setTitle(this.getTitle(player), true);
                        player.broadcastTitleInfo();
                    }
                    player.screenMessage("You have opened an ancient chest and it gave you 5 points.", this.getEventName(), false);
                    EventRewardSystem.getInstance().rewardPlayer(this.getEventType(), 1, player, RewardPosition.ChestRewardAncient, null, player.getTotalTimeAfk(), 0, 0);
                    this.setScoreStats(player, this.getPlayerData(player).getScore());
                    break;
                }
                case EXPLODING: {
                    break;
                }
                case NUKE: {
                    player.screenMessage("You opened the chest, there was a nuke.", this.getEventName(), false);
                    for (PlayerEventInfo pl : this.getPlayers(instanceId)) {
                        if (pl.isDead()) continue;
                        pl.doDie();
                        if (pl.getPlayersId() == player.getPlayersId()) continue;
                        pl.screenMessage("You have been nuked. Thanks go to " + player.getPlayersName() + ".", "THunt", false);
                    }
                    break;
                }
            }
        }
    }

    @Override
    public boolean allowKill(CharacterData target, CharacterData killer) {
        System.out.println("debugging: TreasureHunt.allowKill() 0");
        if (target.isNpc() && killer.isPlayer()) {
            System.out.println("debugging: TreasureHunt.allowKill() 1");
            NpcData npc = target.getNpc();
            PlayerEventInfo player = killer.getEventInfo();
            ChestType type = this.getChestType(npc);
            if (type != null && type == ChestType.EXPLODING) {
                System.out.println("debugging: TreasureHunt.allowKill() 2");
                this.explosionAnimation(npc, player);
                return false;
            }
        }
        return true;
    }

    protected void explosionAnimation(final NpcData npc, PlayerEventInfo player) {
        npc.broadcastSkillUse((CharacterData)npc, player.getCharacterData(), 5430, 1);
        player.doDie();
        CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

            @Override
            public void run() {
                if (npc != null) {
                    npc.deleteMe();
                }
            }
        }, 250);
    }

    protected ChestType getChestType(NpcData npc) {
        if (npc != null) {
            ChestType type = null;
            if (npc.getNpcId() == this._normalChestNpcId) {
                type = ChestType.NORMAL;
            } else if (npc.getNpcId() == this._luckyChestNpcId) {
                type = ChestType.LUCKY;
            } else if (npc.getNpcId() == this._unluckyChestNpcId) {
                type = ChestType.UNLUCKY;
            } else if (npc.getNpcId() == this._fakeChestNpcId) {
                type = ChestType.FAKE;
            } else if (npc.getNpcId() == this._ancientChestNpcId) {
                type = ChestType.ANCIENT;
            } else if (npc.getNpcId() == this._explodingChestNpcId) {
                type = ChestType.EXPLODING;
            } else if (npc.getNpcId() == this._nukeChestNpcId) {
                type = ChestType.NUKE;
            }
            return type;
        }
        return null;
    }

    protected int getChestId(ChestType type) {
        int npcId = 0;
        switch (type) {
            case NORMAL: {
                npcId = this._normalChestNpcId;
                break;
            }
            case LUCKY: {
                npcId = this._luckyChestNpcId;
                break;
            }
            case UNLUCKY: {
                npcId = this._unluckyChestNpcId;
                break;
            }
            case FAKE: {
                npcId = this._fakeChestNpcId;
                break;
            }
            case ANCIENT: {
                npcId = this._ancientChestNpcId;
                break;
            }
            case EXPLODING: {
                npcId = this._explodingChestNpcId;
                break;
            }
            case NUKE: {
                npcId = this._nukeChestNpcId;
            }
        }
        return npcId;
    }

    @Override
    public void onDie(PlayerEventInfo player, CharacterData killer) {
        if (NexusLoader.detailedDebug) {
            this.print("/// Event: onDie - player " + player.getPlayersName() + " (instance " + player.getInstanceId() + "), killer " + killer.getName());
        }
        this.getPlayerData(player).raiseDeaths(1);
        this.getPlayerData(player).setSpree(0);
        this.setDeathsStats(player, this.getPlayerData(player).getDeaths());
        if (this._allowPvp && this._waweRespawn) {
            this._waweScheduler.addPlayer(player);
        } else {
            this.scheduleRevive(player, this.getInt("resDelay") * 1000);
        }
    }

    @Override
    public boolean canSupport(PlayerEventInfo player, CharacterData target) {
        if (player.getPlayersId() != target.getObjectId()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean canAttack(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null) {
            return true;
        }
        if (target.getEventInfo().getEvent() != player.getEvent()) {
            return false;
        }
        return this._allowPvp;
    }

    @Override
    public boolean onSay(PlayerEventInfo player, String text, int channel) {
        if (text.equals(".scheme")) {
            EventManager.getInstance().getHtmlManager().showSelectSchemeForEventWindow(player, "none", this.getEventType().getAltTitle());
            return false;
        }
        if (this._allowPvp && this._antifeed) {
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
    public boolean canUseItem(PlayerEventInfo player, ItemData item) {
        if (!this._allowPvp) {
            player.sendMessage("Weapons are not allowed in this event.");
            return false;
        }
        return true;
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
                    this.unspawnChests(match.getInstance().getId());
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
    public String getMissingSpawns(EventMap map) {
        if (!map.checkForSpawns(SpawnType.Regular, -1, 1)) {
            return this.addMissingSpawn(SpawnType.Regular, 0, 1);
        }
        if (!map.checkForSpawns(SpawnType.Chest, -1, 1)) {
            return this.addMissingSpawn(SpawnType.Chest, 0, 1);
        }
        return "";
    }

    @Override
    protected String addExtraEventInfoCb(int instance) {
        int top = 0;
        for (PlayerEventInfo player : this.getPlayers(instance)) {
            if (this.getPlayerData(player).getScore() <= top) continue;
            top = this.getPlayerData(player).getScore();
        }
        String status = "<font color=ac9887>Top score count: </font><font color=7f7f7f>" + top + "</font>";
        return "<table width=510 bgcolor=3E3E3E><tr><td width=510 align=center>" + status + "</td></tr></table>";
    }

    @Override
    public boolean isInEvent(CharacterData ch) {
        NpcData npc;
        if (ch.isNpc() && this.getChestType(npc = ch.getNpc()) != null) {
            return true;
        }
        return false;
    }

    @Override
    public String getHtmlDescription() {
        if (this._htmlDescription == null) {
            EventDescription desc = EventDescriptionSystem.getInstance().getDescription(this.getEventType());
            this._htmlDescription = desc != null ? desc.getDescription(this.getConfigs()) : "No information about this event yet.";
        }
        return this._htmlDescription;
    }

    @Override
    protected AbstractMainEvent.AbstractEventInstance getMatch(int instanceId) {
        return (AbstractMainEvent.AbstractEventInstance)this._matches.get((Object)instanceId);
    }

    @Override
    protected THEventInstance createEventInstance(InstanceData instance) {
        return new THEventInstance(instance);
    }

    @Override
    protected THData createEventData(int instance) {
        return new THData(instance);
    }

    @Override
    protected THData getEventData(int instance) {
        return ((THEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._data;
    }

    private class EndChecker
    implements Runnable {
        int instance;

        public EndChecker(int instance) {
            this.instance = instance;
        }

        @Override
        public void run() {
            TreasureHunt.this.announce("Some chests hided so well that nobody managed to find them. Event has ended.");
            TreasureHunt.this.endInstance(this.instance, true, false, false);
        }
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

    protected class THEventInstance
    extends Deathmatch.DMEventInstance {
        protected EventState _nextState;
        protected THData _data;

        public THEventInstance(InstanceData instance) {
            super(instance);
            this._nextState = EventState.START;
            this._data = TreasureHunt.this.createEventData(this._instance.getId());
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
                    TreasureHunt.this.print("Event: running task of state " + this._nextState.toString() + "...");
                }
                switch (this._nextState) {
                    case START: {
                        if (!TreasureHunt.this.checkPlayers(this._instance.getId())) break;
                        if (TreasureHunt.this._allowPvp && TreasureHunt.this._antifeed) {
                            for (PlayerEventInfo player : TreasureHunt.this.getPlayers(this._instance.getId())) {
                                player.startAntifeedProtection(false);
                            }
                        }
                        TreasureHunt.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, true);
                        TreasureHunt.this.spawnChests(this._instance.getId());
                        TreasureHunt.this.disequipWeapons(this._instance.getId());
                        TreasureHunt.this.setupTitles(this._instance.getId());
                        TreasureHunt.this.enableMarkers(this._instance.getId(), true);
                        TreasureHunt.this.forceSitAll(this._instance.getId());
                        this.setNextState(EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        TreasureHunt.this.forceStandAll(this._instance.getId());
                        this.setNextState(EventState.END);
                        this._clock.startClock(TreasureHunt.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        TreasureHunt.this.unspawnChests(this._instance.getId());
                        this.setNextState(EventState.INACTIVE);
                        if (TreasureHunt.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            TreasureHunt.this.rewardAllPlayers(this._instance.getId(), 0, TreasureHunt.this.getInt("killsForReward"));
                        }
                        TreasureHunt.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    TreasureHunt.this.print("Event: ... finished running task. next state " + this._nextState.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                TreasureHunt.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    protected class THData
    extends Deathmatch.DMData {
        public List<NpcData> _chests;
        private ScheduledFuture<?> _endCheckerFuture;

        protected THData(int instance) {
            super(instance);
            this._endCheckerFuture = null;
            this._chests = new FastList();
        }

        public void addChest(NpcData ch) {
            this._chests.add(ch);
        }

        public void removeChest(NpcData ch) {
            if (this._chests.contains((Object)ch)) {
                this._chests.remove((Object)ch);
            }
        }
    }

    public static enum ChestType {
        NORMAL,
        FAKE,
        LUCKY,
        ANCIENT,
        UNLUCKY,
        EXPLODING,
        NUKE;
        

        private ChestType() {
        }
    }

}

