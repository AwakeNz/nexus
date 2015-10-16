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
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
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
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.base.MainEventInstanceType;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.main.events.HuntingGrounds;
import cz.nxs.events.engine.main.events.TeamVsTeam;
import cz.nxs.events.engine.main.events.Zombies;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IValues;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class Mutant
extends Zombies {
    private static final int PLAYERS_TEAM_ID = 1;
    private static final int MUTANT_TEAM_ID = 2;
    private FastMap<Integer, Integer> _skillsForPlayers;
    private FastMap<Integer, Integer> _skillsForMutant;
    private String _mutantCount;
    private int _mutantTransformId;
    private int _mutantMinLevel;
    private int _mutanteMinPvps;
    private int _mutantWeaponId;
    private int _mutantKillScore = 1;
    private int _playerKillScore = 1;

    public Mutant(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Looser, RewardPosition.Tie, RewardPosition.Numbered, RewardPosition.Range, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill});
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.removeConfig("minPlayers");
        this.addConfig(new ConfigModel("minPlayers", "3", "The minimum count of players required to start one instance of the event. Min for Mutant is 3 (2 players and one mutant)."));
        this.addInstanceTypeConfig(new ConfigModel("minPlayers", "3", "Count of players required to start this instance. If there's less players, then the instance tries to divide it's players to stronger instances (check out config <font color=LEVEL>joinStrongerInstIfNeeded</font>) and if it doesn't success (the config is set to false or all possible stronger instances are full), it will unregister the players from the event. Check out other configs related to this. Min for mutant is 3."));
        this.removeConfig("skillsForAllPlayers");
        this.removeConfig("bowWeaponId");
        this.removeConfig("arrowItemId");
        this.removeConfig("teamsCount");
        this.removeConfig("createParties");
        this.removeConfig("maxPartySize");
        this.removeConfig("teamsCount");
        this.removeConfig("firstBloodMessage");
        this.removeConfig("waweRespawn");
        this.removeConfig("countOfZombies");
        this.removeConfig("zombieTransformId");
        this.removeConfig("zombieInactivityTime");
        this.removeConfig("zombieMinLevel");
        this.removeConfig("zombieMinPvPs");
        this.removeConfig("zombieKillScore");
        this.removeConfig("survivorKillScore");
        this.removeConfig("zombiesInitialScore");
        this.removeConfig("bowWeaponId");
        this.removeConfig("arrowItemId");
        this.removeConfig("enableAmmoSystem");
        this.removeConfig("ammoAmmount");
        this.removeConfig("ammoRestoredPerTick");
        this.removeConfig("ammoRegTickInterval");
        this.addConfig(new ConfigModel("waweRespawn", "true", "Enables the wawe-style respawn system for zombies.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("skillsForPlayers", "", "IDs of skills which will be given to every player, who is not a mutant in the event. Format: <font color=LEVEL>SKILLID-LEVEL</font> (eg. '35001-1').", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("skillsForMutant", "35103-1", "IDs of skills which will be given to every mutant in the event. This skill should contain stats which make the mutant extra strong. Format: <font color=LEVEL>SKILLID-LEVEL</font> (eg. '35002-1').", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("mutantWeaponId", "271", "The ID of the weapon which will be given to all mutants and will be the only weapon most the mutant will be able to use."));
        this.addConfig(new ConfigModel("countOfMutants", "1/10", "Defines the count of mutants in the the event. Format: #MUTANTS/#PLAYERS - <font color=LEVEL>eg. 1/10</font> means there's <font color=LEVEL>1</font> mutant when there are <font color=LEVEL>10</font> players in the event (20 players - 2 mutants, 100 players - 10 mutants, ...). There's always at least one mutant in the event."));
        this.addConfig(new ConfigModel("mutantTransformId", "303", "The ID of transformation used to morph players into zombies."));
        this.addConfig(new ConfigModel("mutantMinLevel", "0", "The minimum level required to become a zombie IN THE START OF THE EVENT."));
        this.addConfig(new ConfigModel("mutantMinPvPs", "0", "The minimum count of pvps required to become a zombie IN THE START OF THE EVENT."));
        this.addConfig(new ConfigModel("mutantKillScore", "1", "The count of score points given to a zombie when he kills a player."));
        this.addConfig(new ConfigModel("playerKillScore", "1", "The count of score points given to a survivor when he kills a zombie."));
    }

    @Override
    public void initEvent() {
        String level;
        String[] splits;
        int i;
        String id;
        super.initEvent();
        this._mutantWeaponId = this.getInt("mutantWeaponId");
        this._mutantCount = this.getString("countOfMutants");
        this._mutantTransformId = this.getInt("mutantTransformId");
        this._mutantMinLevel = this.getInt("mutantMinLevel");
        this._mutanteMinPvps = this.getInt("mutantMinPvPs");
        this._mutantKillScore = this.getInt("mutantKillScore");
        this._playerKillScore = this.getInt("playerKillScore");
        if (!this.getString("skillsForPlayers").equals("")) {
            splits = this.getString("skillsForPlayers").split(",");
            this._skillsForPlayers = new FastMap();
            try {
                for (i = 0; i < splits.length; ++i) {
                    id = splits[i].split("-")[0];
                    level = splits[i].split("-")[1];
                    this._skillsForPlayers.put((Object)Integer.parseInt(id), (Object)Integer.parseInt(level));
                }
            }
            catch (Exception e) {
                NexusLoader.debug((String)("Error while loading config 'skillsForPlayers' for event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
            }
        }
        if (!this.getString("skillsForMutant").equals("")) {
            splits = this.getString("skillsForMutant").split(",");
            this._skillsForMutant = new FastMap();
            try {
                for (i = 0; i < splits.length; ++i) {
                    id = splits[i].split("-")[0];
                    level = splits[i].split("-")[1];
                    this._skillsForMutant.put((Object)Integer.parseInt(id), (Object)Integer.parseInt(level));
                }
            }
            catch (Exception e) {
                NexusLoader.debug((String)("Error while loading config 'skillsForMutant' for event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
            }
        }
        this._tick = 0;
    }

    @Override
    protected int initInstanceTeams(MainEventInstanceType type, int instanceId) {
        this._teamsCount = 2;
        this.createTeams(this._teamsCount, type.getInstance().getId());
        return this._teamsCount;
    }

    @Override
    protected void createTeams(int count, int instanceId) {
        this.createNewTeam(instanceId, 1, "Players", "Players");
        this.createNewTeam(instanceId, 2, "Mutants", "Mutants");
    }

    @Override
    protected void dividePlayersToTeams(int instanceId, FastList<PlayerEventInfo> players, int teamsCount) {
        for (PlayerEventInfo pi : players) {
            pi.onEventStart((EventGame)this);
            ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)1)).addPlayer(pi, true);
        }
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
            MutantEventInstance match = this.createEventInstance(instance);
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

    protected void scheduleSelectMutants(final int instanceId, long delay, final boolean firstRun, final int forceAddNewMutantCount) {
        if (delay == 0) {
            CallBack.getInstance().getOut().executeTask(new Runnable(){

                @Override
                public void run() {
                    List<PlayerEventInfo> newZombies = Mutant.this.calculateMutants(instanceId, forceAddNewMutantCount > 0 ? forceAddNewMutantCount : -1, firstRun);
                    if (newZombies != null) {
                        for (PlayerEventInfo zombie : newZombies) {
                            Mutant.this.transformToMutant(zombie);
                        }
                    }
                }
            });
        } else {
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    List<PlayerEventInfo> newZombies = Mutant.this.calculateMutants(instanceId, forceAddNewMutantCount > 0 ? forceAddNewMutantCount : -1, firstRun);
                    if (newZombies != null) {
                        for (PlayerEventInfo zombie : newZombies) {
                            Mutant.this.transformToMutant(zombie);
                        }
                    }
                }
            }, delay);
        }
    }

    protected List<PlayerEventInfo> calculateMutants(int instanceId, int countToSpawn, boolean start) {
        int playersCount = this.getPlayers(instanceId).size();
        int survivorsCount = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)1)).getPlayers().size();
        int mutantCount = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)2)).getPlayers().size();
        if (countToSpawn <= 0) {
            int mutants = Integer.parseInt(this._mutantCount.split("/")[0]);
            int players = Integer.parseInt(this._mutantCount.split("/")[1]);
            if (start) {
                countToSpawn = (int)Math.floor((double)playersCount / (double)players * (double)mutants);
                if (countToSpawn < 1) {
                    countToSpawn = 1;
                }
            } else {
                countToSpawn = (int)Math.floor((double)playersCount / (double)players * (double)mutants);
                countToSpawn-=mutantCount;
            }
        }
        int i = 0;
        FastList newMutants = new FastList();
        if (survivorsCount >= 2) {
            if (countToSpawn >= survivorsCount) {
                countToSpawn = survivorsCount - 1;
            }
            if (countToSpawn > 0) {
                for (PlayerEventInfo player : this.getPlayers(instanceId)) {
                    if (start && (player.getLevel() < this._mutantMinLevel || player.getPvpKills() < this._mutanteMinPvps)) continue;
                    newMutants.add(player);
                    if (++i < countToSpawn) continue;
                    break;
                }
            }
        }
        return newMutants;
    }

    @Override
    protected void preparePlayer(PlayerEventInfo player, boolean start) {
        SkillData skill = null;
        if (player.getEventTeam().getTeamId() == 1) {
            if (start) {
                if (this._skillsForPlayers != null) {
                    for (Map.Entry e : this._skillsForPlayers.entrySet()) {
                        skill = new SkillData(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
                        if (!skill.exists()) continue;
                        player.addSkill(skill, false);
                    }
                    player.sendSkillList();
                }
            } else if (this._skillsForPlayers != null) {
                for (Map.Entry e : this._skillsForPlayers.entrySet()) {
                    skill = new SkillData(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
                    if (!skill.exists()) continue;
                    player.removeSkill(skill.getId());
                }
            }
        } else if (player.getEventTeam().getTeamId() == 2) {
            if (start) {
                if (this._skillsForMutant != null) {
                    for (Map.Entry e : this._skillsForMutant.entrySet()) {
                        skill = new SkillData(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
                        if (!skill.exists()) continue;
                        player.addSkill(skill, false);
                    }
                    player.sendSkillList();
                }
                if (this._mutantWeaponId > 0) {
                    ItemData wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_RHAND());
                    if (wpn != null) {
                        player.unEquipItemInBodySlotAndRecord(CallBack.getInstance().getValues().SLOT_R_HAND());
                    }
                    if ((wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_LHAND())) != null) {
                        player.unEquipItemInBodySlotAndRecord(CallBack.getInstance().getValues().SLOT_L_HAND());
                    }
                    ItemData flagItem = player.addItem(this._mutantWeaponId, 1, false);
                    player.equipItem(flagItem);
                }
            } else {
                ItemData wpn;
                if (this._skillsForMutant != null) {
                    for (Map.Entry e : this._skillsForMutant.entrySet()) {
                        skill = new SkillData(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
                        if (!skill.exists()) continue;
                        player.removeSkill(skill.getId());
                    }
                }
                if (this._mutantWeaponId > 0 && (wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_RHAND())).exists()) {
                    ItemData[] unequiped = player.unEquipItemInBodySlotAndRecord(wpn.getBodyPart());
                    player.destroyItemByItemId(this._mutantWeaponId, 1);
                    player.inventoryUpdate(unequiped);
                }
            }
        }
    }

    private void transformToMutant(final PlayerEventInfo player) {
        this.preparePlayer(player, false);
        player.getEventTeam().removePlayer(player);
        ((EventTeam)((FastMap)this._teams.get((Object)player.getInstanceId())).get((Object)2)).addPlayer(player, true);
        this.preparePlayer(player, true);
        player.transform(this._mutantTransformId);
        this.getEventData(player.getInstanceId()).setKillMade();
        if (player.isDead()) {
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    if (player != null && player.isOnline() && Mutant.this.getMatch(player.getInstanceId()).isActive()) {
                        Mutant.this.respawnPlayer(player, player.getInstanceId());
                        player.sendMessage("You will be respawned in 10 seconds.");
                    }
                }
            }, 10000);
        }
        player.setTitle(this.getTitle(player), true);
    }

    private void transformToPlayer(final PlayerEventInfo player, boolean endOfEvent) {
        if (endOfEvent) {
            player.untransform(true);
            player.setTitle(this.getTitle(player), true);
            player.broadcastTitleInfo();
        } else {
            try {
                if (player.getTeamId() == 2) {
                    this.preparePlayer(player, false);
                    player.untransform(true);
                    player.getEventTeam().removePlayer(player);
                    ((EventTeam)((FastMap)this._teams.get((Object)player.getInstanceId())).get((Object)1)).addPlayer(player, true);
                    this.preparePlayer(player, true);
                    player.setTitle(this.getTitle(player), true);
                    player.broadcastTitleInfo();
                    if (player.isDead()) {
                        CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                            @Override
                            public void run() {
                                if (player != null && player.isOnline()) {
                                    Mutant.this.respawnPlayer(player, player.getInstanceId());
                                }
                            }
                        }, 10000);
                    }
                }
            }
            catch (Exception e) {
                NexusLoader.debug((String)"error while untransforming mutant:");
                this.clearEvent();
                e.printStackTrace();
            }
        }
    }

    private void untransformAll(int instanceId) {
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            if (player.getTeamId() != 2) continue;
            this.transformToPlayer(player, true);
        }
    }

    private void setAllPlayers(int instanceId) {
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            try {
                if (player.getTeamId() != 2) continue;
                player.getEventTeam().removePlayer(player);
                ((EventTeam)((FastMap)this._teams.get((Object)player.getInstanceId())).get((Object)1)).addPlayer(player, true);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onEventEnd() {
        if (NexusLoader.detailedDebug) {
            this.print("Event: onEventEnd()");
        }
        int minScore = this.getInt("killsForReward");
        this.rewardAllPlayersFromTeam(-1, minScore, 0, 1);
    }

    @Override
    protected String getTitle(PlayerEventInfo pi) {
        if (pi.isAfk()) {
            return "AFK";
        }
        if (pi.getTeamId() == 2) {
            return "~ MUTANT ~";
        }
        return "Score: " + this.getPlayerData(pi).getScore();
    }

    @Override
    protected String getScorebar(int instance) {
        int count = ((FastMap)this._teams.get((Object)instance)).size();
        TextBuilder tb = new TextBuilder();
        for (EventTeam team : ((FastMap)this._teams.get((Object)instance)).values()) {
            tb.append(team.getTeamName() + ": " + team.getPlayers().size() + "  ");
        }
        if (count <= 3) {
            tb.append(LanguageEngine.getMsg("event_scorebar_time", ((TeamVsTeam.TvTEventInstance)this._matches.get((Object)instance)).getClock().getTime()));
        }
        return tb.toString();
    }

    @Override
    protected void clockTick() throws Exception {
    }

    @Override
    public void onKill(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null) {
            return;
        }
        if (player.getPlayersId() != target.getObjectId()) {
            if (player.getTeamId() == 2) {
                player.getEventTeam().raiseScore(this._mutantKillScore);
                player.getEventTeam().raiseKills(this._mutantKillScore);
                this.getPlayerData(player).raiseScore(this._mutantKillScore);
                this.getPlayerData(player).raiseKills(this._mutantKillScore);
                this.getPlayerData(player).raiseSpree(1);
            } else if (player.getTeamId() == 1) {
                player.getEventTeam().raiseScore(this._playerKillScore);
                player.getEventTeam().raiseKills(this._playerKillScore);
                this.getPlayerData(player).raiseScore(this._playerKillScore);
                this.getPlayerData(player).raiseKills(this._playerKillScore);
                this.getPlayerData(player).raiseSpree(1);
            }
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
        if (killer.getEventInfo() != null) {
            if (player.getTeamId() == 1) {
                if (this._waweRespawn) {
                    this._waweScheduler.addPlayer(player);
                } else {
                    this.scheduleRevive(player, this.getInt("resDelay") * 1000);
                }
            } else {
                this.transformToPlayer(player, false);
                PlayerEventInfo killerInfo = killer.getEventInfo();
                this.transformToMutant(killerInfo);
            }
        }
    }

    @Override
    public boolean onAttack(CharacterData cha, CharacterData target) {
        return true;
    }

    @Override
    public boolean canUseItem(PlayerEventInfo player, ItemData item) {
        if (player.getTeamId() == 2) {
            if (item.getItemId() == this._mutantWeaponId && item.isEquipped()) {
                return false;
            }
            if (this._mutantWeaponId > 0 && item.isWeapon()) {
                return false;
            }
        }
        if (this.notAllovedItems != null && Arrays.binarySearch(this.notAllovedItems, item.getItemId()) >= 0) {
            player.sendMessage(LanguageEngine.getMsg("event_itemNotAllowed"));
            return false;
        }
        if (item.isPotion() && !this.getBoolean("allowPotions")) {
            return false;
        }
        if (item.isScroll()) {
            return false;
        }
        if (item.isPetCollar() && !this._allowPets) {
            player.sendMessage(LanguageEngine.getMsg("event_petsNotAllowed"));
            return false;
        }
        return true;
    }

    @Override
    public void onDamageGive(CharacterData cha, CharacterData target, int damage, boolean isDOT) {
    }

    @Override
    public boolean canDestroyItem(PlayerEventInfo player, ItemData item) {
        if (item.getItemId() == this._mutantWeaponId || player.getTeamId() == 2) {
            return false;
        }
        return super.canDestroyItem(player, item);
    }

    @Override
    public boolean canSupport(PlayerEventInfo player, CharacterData target) {
        return false;
    }

    @Override
    public void onDisconnect(PlayerEventInfo player) {
        super.onDisconnect(player);
        this.scheduleSelectMutants(player.getInstanceId(), 0, false, 0);
    }

    @Override
    protected boolean checkIfEventCanContinue(int instanceId, PlayerEventInfo disconnectedPlayer) {
        int currentPlayers = 0;
        int currentMutants = 0;
        for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
            if (team.getTeamId() == 1) {
                for (PlayerEventInfo pi : team.getPlayers()) {
                    ++currentPlayers;
                }
            }
            if (team.getTeamId() != 2) continue;
            for (PlayerEventInfo pi : team.getPlayers()) {
                ++currentMutants;
            }
        }
        if (currentMutants == 0) {
            if (currentPlayers >= 3) {
                return true;
            }
            return false;
        }
        if (currentMutants == 1) {
            if (currentPlayers >= 2) {
                return true;
            }
            return false;
        }
        if (currentPlayers + currentMutants >= 3) {
            int mutants = Integer.parseInt(this._mutantCount.split("/")[0]);
            int players = Integer.parseInt(this._mutantCount.split("/")[1]);
            int countToHaveMutants = (int)Math.floor((double)currentPlayers / (double)players * (double)mutants);
            if (countToHaveMutants < 1) {
                countToHaveMutants = 1;
            }
            int toUntransform = 0;
            if (currentMutants > countToHaveMutants) {
                toUntransform = currentMutants - countToHaveMutants;
            }
            if (toUntransform > 0) {
                for (PlayerEventInfo mutant : ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)2)).getPlayers()) {
                    if (toUntransform <= 0) break;
                    this.transformToPlayer(mutant, false);
                    --toUntransform;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void teleportPlayers(int instanceId, SpawnType type, boolean ffa) {
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: ========================================");
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: STARTING TO TELEPORT PLAYERS (ffa = " + ffa + ")");
        }
        boolean removeBuffs = this.getBoolean("removeBuffsOnStart");
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: removeBuffs = " + removeBuffs);
        }
        int i = 0;
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            int radius;
            EventSpawn spawn = this.getSpawn(type, -1);
            if (spawn == null) {
                if (NexusLoader.detailedDebug) {
                    this.print("AbstractMainEvent: ! Missing spawn for team " + (((FastMap)this._teams.get((Object)instanceId)).size() == 1 ? -1 : player.getTeamId()) + ", map " + this._manager.getMap().getMapName() + ", event " + this.getEventType().getAltTitle() + " !!");
                }
                NexusLoader.debug((String)("Missing spawn for team " + (((FastMap)this._teams.get((Object)instanceId)).size() == 1 ? -1 : player.getTeamId()) + ", map " + this._manager.getMap().getMapName() + ", event " + this.getEventType().getAltTitle() + " !!"), (Level)Level.SEVERE);
            }
            if ((radius = spawn.getRadius()) == -1) {
                radius = 50;
            }
            Loc loc = new Loc(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ());
            loc.addRadius(radius);
            player.teleport(loc, 0, false, instanceId);
            if (NexusLoader.detailedDebug) {
                this.print("AbstractMainEvent: /// player " + player.getPlayersName() + " teleported to " + loc.getX() + ", " + loc.getY() + ", " + loc.getZ() + " (radius = " + radius + "), SPAWN ID " + spawn.getSpawnId() + ", SPAWN TEAM " + spawn.getSpawnTeam());
            }
            if (removeBuffs) {
                player.removeBuffs();
            }
            ++i;
        }
        if (NexusLoader.detailedDebug) {
            this.print("AbstractMainEvent: " + i + " PLAYERS TELEPORTED");
        }
        this.clearMapHistory(-1, type);
    }

    @Override
    public EventPlayerData createPlayerData(PlayerEventInfo player) {
        MutantEventPlayerData d = new MutantEventPlayerData(player, this);
        return d;
    }

    @Override
    public MutantEventPlayerData getPlayerData(PlayerEventInfo player) {
        return (MutantEventPlayerData)player.getEventData();
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
                    this.preparePlayers(match.getInstance().getId(), false);
                    this.untransformAll(match.getInstance().getId());
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
    protected void respawnPlayer(PlayerEventInfo pi, int instance) {
        if (NexusLoader.detailedDebug) {
            this.print("/// Event: respawning player " + pi.getPlayersName() + ", instance " + instance);
        }
        EventSpawn spawn = null;
        if (pi.getTeamId() == 1) {
            spawn = this.getSpawn(SpawnType.Regular, -1);
        } else if (pi.getTeamId() == 2) {
            spawn = this.getSpawn(SpawnType.Zombie, -1);
        }
        if (spawn != null) {
            Loc loc = new Loc(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ());
            loc.addRadius(spawn.getRadius());
            pi.teleport(loc, 0, true, instance);
            pi.sendMessage(LanguageEngine.getMsg("event_respawned"));
        } else {
            this.debug("Error on respawnPlayer - no spawn type REGULAR/ZOMBIE, team " + pi.getTeamId() + " has been found. Event aborted.");
        }
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
    public boolean allowsRejoinOnDisconnect() {
        return false;
    }

    @Override
    protected TeamVsTeam.TvTEventData createEventData(int instanceId) {
        return new MutantEventData(instanceId);
    }

    @Override
    protected MutantEventInstance createEventInstance(InstanceData instance) {
        return new MutantEventInstance(instance);
    }

    @Override
    protected MutantEventData getEventData(int instance) {
        return (MutantEventData)((TeamVsTeam.TvTEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._data;
    }

    public class MutantEventPlayerData
    extends Zombies.ZombiesEventPlayerData {
        public MutantEventPlayerData(PlayerEventInfo owner, EventGame event) {
            super(Mutant.this, owner, event);
        }
    }

    protected class MutantEventInstance
    extends Zombies.ZombiesEventInstance {
        protected MutantEventInstance(InstanceData instance) {
            super(Mutant.this, instance);
        }

        @Override
        public void run() {
            try {
                if (NexusLoader.detailedDebug) {
                    Mutant.this.print("Event: running task of state " + this._state.toString() + "...");
                }
                switch (this._state) {
                    case START: {
                        if (!Mutant.this.checkPlayers(this._instance.getId())) break;
                        Mutant.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, false);
                        Mutant.this.setupTitles(this._instance.getId());
                        Mutant.this.removeStaticDoors(this._instance.getId());
                        Mutant.this.enableMarkers(this._instance.getId(), true);
                        Mutant.this.preparePlayers(this._instance.getId(), true);
                        Mutant.this.scheduleSelectMutants(this._instance.getId(), 10000, true, 0);
                        Mutant.this.forceSitAll(this._instance.getId());
                        this.setNextState(TeamVsTeam.EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        Mutant.this.forceStandAll(this._instance.getId());
                        this.setNextState(TeamVsTeam.EventState.END);
                        this._clock.startClock(Mutant.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        this.setNextState(TeamVsTeam.EventState.INACTIVE);
                        Mutant.this.untransformAll(this._instance.getId());
                        Mutant.this.setAllPlayers(this._instance.getId());
                        if (Mutant.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            int minScore = Mutant.this.getInt("killsForReward");
                            Mutant.this.rewardAllPlayersFromTeam(this._instance.getId(), minScore, 0, 1);
                        }
                        Mutant.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    Mutant.this.print("Event: ... finished running task. next state " + this._state.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                Mutant.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    protected class MutantEventData
    extends Zombies.ZombiesEventData {
        public MutantEventData(int instance) {
            super(Mutant.this, instance);
        }

        @Override
        public void setKillMade() {
        }
    }

}

