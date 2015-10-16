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
import cz.nxs.events.engine.stats.GlobalStatsModel;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class Zombies
extends HuntingGrounds {
    private static final int SURVIVOR_TEAM_ID = 1;
    private static final int ZOMBIE_TEAM_ID = 2;
    private FastMap<Integer, Integer> _skillsForSurvivors;
    private FastMap<Integer, Integer> _skillsForZombies;
    private String _zombiesCount;
    private int _zombieTransformId;
    private int _zombieInactivityTime;
    private int _zombieMinLevel;
    private int _zombieMinPvps;
    private int _zombieKillScore = 1;
    private int _survivorKillScore = 1;
    private int _zombiesInitialScore = 0;

    public Zombies(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Looser, RewardPosition.Tie, RewardPosition.Numbered, RewardPosition.Range, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill, RewardPosition.KillingSpree});
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.removeConfig("skillsForAllPlayers");
        this.removeConfig("bowWeaponId");
        this.removeConfig("arrowItemId");
        this.removeConfig("teamsCount");
        this.removeConfig("createParties");
        this.removeConfig("maxPartySize");
        this.removeConfig("teamsCount");
        this.removeConfig("firstBloodMessage");
        this.removeConfig("waweRespawn");
        this.addConfig(new ConfigModel("waweRespawn", "true", "Enables the wawe-style respawn system for zombies.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("skillsForPlayers", "35101-1", "IDs of skills which will be given to every survivor (non zombie player) on the event. The purpose of this is to make all survivors equally strong. Format: <font color=LEVEL>SKILLID-LEVEL</font> (eg. '35001-1').", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("skillsForZombies", "35102-1", "IDs of skills which will be given to every zombie on the event. The purpose of this is to make all zombies equally strong. Format: <font color=LEVEL>SKILLID-LEVEL</font> (eg. '35002-1').", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("bowWeaponId", "271", "The ID of the bow item which will be given to the survivors (non zombies) and will be the only weapon most players will use during the event. This weapon kills zombies with just one hit."));
        this.addConfig(new ConfigModel("arrowItemId", "17", "The ID of the arrows which will be given to the player in the event."));
        this.addConfig(new ConfigModel("enableAmmoSystem", "true", "Enable/disable the ammo system based on player's mana. Player's max MP is defaultly modified by a custom passive skill and everytime a player shots and arrow, his MP decreases by a value which is calculated from the ammount of ammo. There is also a MP regeneration system - see the configs below.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("ammoAmmount", "10", "Works if ammo system is enabled. Specifies the max ammount of ammo every player can have."));
        this.addConfig(new ConfigModel("ammoRestoredPerTick", "1", "Works if ammo system is enabled. Defines the ammount of ammo given to every player each <font color=LEVEL>'ammoRegTickInterval'</font> (configurable) seconds."));
        this.addConfig(new ConfigModel("ammoRegTickInterval", "10", "Works if ammo system is enabled. Defines the interval of restoring player's ammo. The value is in seconds (eg. value 10 will give ammo every 10 seconds to every player - the ammount of restored ammo is configurable (config <font color=LEVEL>ammoRestoredPerTick</font>)."));
        this.addConfig(new ConfigModel("countOfZombies", "1/10", "Defines the count of players transformed to zombies in the start of the event. Format: #ZOMBIES/#PLAYERS - <font color=LEVEL>eg. 1/10</font> means there's <font color=LEVEL>1</font> zombie when there are <font color=LEVEL>10</font> players in the event (20 players - 2 zombies, 100 players - 10 zombies, ...). There's always at least one zombie in the event."));
        this.addConfig(new ConfigModel("zombieTransformId", "303", "The ID of transformation used to morph players into zombies."));
        this.addConfig(new ConfigModel("zombieInactivityTime", "300", "In seconds. If no player is killed (by zombie) during this time, one random player will be transformed into a zombie and respawned on Zombie respawn (away from other players). Write 0 to disable this feature."));
        this.addConfig(new ConfigModel("zombieMinLevel", "0", "The minimum level required to become a zombie IN THE START OF THE EVENT."));
        this.addConfig(new ConfigModel("zombieMinPvPs", "0", "The minimum count of pvps required to become a zombie IN THE START OF THE EVENT."));
        this.addConfig(new ConfigModel("zombieKillScore", "1", "The count of score points given to a zombie when he kills a player."));
        this.addConfig(new ConfigModel("survivorKillScore", "1", "The count of score points given to a survivor when he kills a zombie."));
        this.addConfig(new ConfigModel("zombiesInitialScore", "1", "The initial score given to every zombie who gets automatically transformed in the beginning of the event."));
    }

    @Override
    public void initEvent() {
        String level;
        String[] splits;
        int i;
        String id;
        super.initEvent();
        this._bowItemId = this.getInt("bowWeaponId");
        this._arrowItemId = this.getInt("arrowItemId");
        this._ammoSystem = this.getBoolean("enableAmmoSystem");
        this._ammoAmmount = this.getInt("ammoAmmount");
        this._ammoRegPerTick = this.getInt("ammoRestoredPerTick");
        this._tickLength = this.getInt("ammoRegTickInterval");
        this._zombiesCount = this.getString("countOfZombies");
        this._zombieTransformId = this.getInt("zombieTransformId");
        this._zombieInactivityTime = this.getInt("zombieInactivityTime");
        this._zombieMinLevel = this.getInt("zombieMinLevel");
        this._zombieMinPvps = this.getInt("zombieMinPvPs");
        this._zombieKillScore = this.getInt("zombieKillScore");
        this._survivorKillScore = this.getInt("survivorKillScore");
        this._zombiesInitialScore = this.getInt("zombiesInitialScore");
        if (!this.getString("skillsForPlayers").equals("")) {
            splits = this.getString("skillsForPlayers").split(",");
            this._skillsForSurvivors = new FastMap();
            try {
                for (i = 0; i < splits.length; ++i) {
                    id = splits[i].split("-")[0];
                    level = splits[i].split("-")[1];
                    this._skillsForSurvivors.put((Object)Integer.parseInt(id), (Object)Integer.parseInt(level));
                }
            }
            catch (Exception e) {
                NexusLoader.debug((String)("Error while loading config 'skillsForPlayers' for event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
            }
        }
        if (!this.getString("skillsForZombies").equals("")) {
            splits = this.getString("skillsForZombies").split(",");
            this._skillsForZombies = new FastMap();
            try {
                for (i = 0; i < splits.length; ++i) {
                    id = splits[i].split("-")[0];
                    level = splits[i].split("-")[1];
                    this._skillsForZombies.put((Object)Integer.parseInt(id), (Object)Integer.parseInt(level));
                }
            }
            catch (Exception e) {
                NexusLoader.debug((String)("Error while loading config 'skillsForZombies' for event " + this.getEventName() + " - " + e.toString()), (Level)Level.SEVERE);
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
        this.createNewTeam(instanceId, 1, "Survivors", "Survivors");
        this.createNewTeam(instanceId, 2, "Zombies", "Zombies");
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
            ZombiesEventInstance match = this.createEventInstance(instance);
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

    private void scheduleSelectZombies(final int instanceId, long delay, final boolean firstRun, final int forceAddNewZombieCount) {
        if (delay == 0) {
            CallBack.getInstance().getOut().executeTask(new Runnable(){

                @Override
                public void run() {
                    List newZombies = Zombies.this.calculateZombies(instanceId, forceAddNewZombieCount > 0 ? forceAddNewZombieCount : -1, firstRun);
                    if (newZombies != null) {
                        for (PlayerEventInfo zombie : newZombies) {
                            Zombies.this.transformToZombie(zombie);
                            try {
                                if (!firstRun || Zombies.this._zombiesInitialScore <= 0) continue;
                                zombie.getEventTeam().raiseScore(Zombies.this._zombiesInitialScore);
                                Zombies.this.getPlayerData(zombie).raiseScore(Zombies.this._zombiesInitialScore);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } else {
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    List newZombies = Zombies.this.calculateZombies(instanceId, forceAddNewZombieCount > 0 ? forceAddNewZombieCount : -1, firstRun);
                    if (newZombies != null) {
                        for (PlayerEventInfo zombie : newZombies) {
                            Zombies.this.transformToZombie(zombie);
                            try {
                                if (!firstRun || Zombies.this._zombiesInitialScore <= 0) continue;
                                zombie.getEventTeam().raiseScore(Zombies.this._zombiesInitialScore);
                                Zombies.this.getPlayerData(zombie).raiseScore(Zombies.this._zombiesInitialScore);
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }, delay);
        }
    }

    private List<PlayerEventInfo> calculateZombies(int instanceId, int countToSpawn, boolean start) {
        int playersCount = this.getPlayers(instanceId).size();
        int survivorsCount = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)1)).getPlayers().size();
        int zombiesCount = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)2)).getPlayers().size();
        if (countToSpawn <= 0) {
            int zombies = Integer.parseInt(this._zombiesCount.split("/")[0]);
            int players = Integer.parseInt(this._zombiesCount.split("/")[1]);
            if (start) {
                countToSpawn = (int)Math.floor((double)playersCount / (double)players * (double)zombies);
                if (countToSpawn < 1) {
                    countToSpawn = 1;
                }
            } else {
                countToSpawn = (int)Math.floor((double)playersCount / (double)players * (double)zombies);
                countToSpawn-=zombiesCount;
            }
        }
        int i = 0;
        FastList newZombies = new FastList();
        if (countToSpawn >= survivorsCount) {
            countToSpawn = survivorsCount - 1;
        }
        if (countToSpawn > 0) {
            for (PlayerEventInfo player : this.getPlayers(instanceId)) {
                if (start && (player.getLevel() < this._zombieMinLevel || player.getPvpKills() < this._zombieMinPvps)) continue;
                newZombies.add(player);
                if (++i < countToSpawn) continue;
                break;
            }
        }
        return newZombies;
    }

    @Override
    protected void preparePlayers(int instanceId, boolean start) {
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            this.preparePlayer(player, start);
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    protected void preparePlayer(PlayerEventInfo player, boolean start) {
        SkillData skill = null;
        if (player.getEventTeam().getTeamId() == 1) {
            if (start) {
                ItemData wpn;
                if (this._skillsForSurvivors != null) {
                    for (Map.Entry e : this._skillsForSurvivors.entrySet()) {
                        skill = new SkillData(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
                        if (!skill.exists()) continue;
                        player.addSkill(skill, false);
                    }
                    player.sendSkillList();
                }
                if ((wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_RHAND())) != null) {
                    player.unEquipItemInBodySlotAndRecord(CallBack.getInstance().getValues().SLOT_R_HAND());
                }
                if ((wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_LHAND())) != null) {
                    player.unEquipItemInBodySlotAndRecord(CallBack.getInstance().getValues().SLOT_L_HAND());
                }
                ItemData flagItem = player.addItem(this._bowItemId, 1, false);
                player.equipItem(flagItem);
                player.addItem(this._arrowItemId, 400, false);
                return;
            } else {
                ItemData wpn;
                if (this._skillsForSurvivors != null) {
                    for (Map.Entry e : this._skillsForSurvivors.entrySet()) {
                        skill = new SkillData(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
                        if (!skill.exists()) continue;
                        player.removeSkill(skill.getId());
                    }
                }
                if (!(wpn = player.getPaperdollItem(CallBack.getInstance().getValues().PAPERDOLL_RHAND())).exists()) return;
                ItemData[] unequiped = player.unEquipItemInBodySlotAndRecord(wpn.getBodyPart());
                player.destroyItemByItemId(this._bowItemId, 1);
                player.inventoryUpdate(unequiped);
            }
            return;
        } else {
            if (player.getEventTeam().getTeamId() != 2) return;
            if (start) {
                if (this._skillsForZombies == null) return;
                for (Map.Entry e : this._skillsForZombies.entrySet()) {
                    skill = new SkillData(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
                    if (!skill.exists()) continue;
                    player.addSkill(skill, false);
                }
                player.sendSkillList();
                return;
            } else {
                if (this._skillsForZombies == null) return;
                for (Map.Entry e : this._skillsForZombies.entrySet()) {
                    skill = new SkillData(((Integer)e.getKey()).intValue(), ((Integer)e.getValue()).intValue());
                    if (!skill.exists()) continue;
                    player.removeSkill(skill.getId());
                }
            }
        }
    }

    private void zombiesInactive(int instanceId) {
        this.scheduleSelectZombies(instanceId, 0, false, 1);
    }

    private void transformToZombie(final PlayerEventInfo player) {
        if (!player.isDead()) {
            player.doDie();
        }
        this.preparePlayer(player, false);
        player.getEventTeam().removePlayer(player);
        ((EventTeam)((FastMap)this._teams.get((Object)player.getInstanceId())).get((Object)2)).addPlayer(player, true);
        this.preparePlayer(player, true);
        player.transform(this._zombieTransformId);
        this.getEventData(player.getInstanceId()).setKillMade();
        if (this.checkIfAnyPlayersLeft(player.getInstanceId())) {
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    if (player != null && player.isOnline() && Zombies.this.getMatch(player.getInstanceId()).isActive()) {
                        Zombies.this.respawnPlayer(player, player.getInstanceId());
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
        } else {
            try {
                if (player.getTeamId() == 2) {
                    this.preparePlayer(player, false);
                    player.untransform(true);
                    player.getEventTeam().removePlayer(player);
                    ((EventTeam)((FastMap)this._teams.get((Object)player.getInstanceId())).get((Object)1)).addPlayer(player, true);
                    this.preparePlayer(player, true);
                    if (player.isDead()) {
                        CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                            @Override
                            public void run() {
                                if (player != null && player.isOnline()) {
                                    Zombies.this.respawnPlayer(player, player.getInstanceId());
                                }
                            }
                        }, 10000);
                    }
                }
            }
            catch (Exception e) {
                NexusLoader.debug((String)"error while untransforming zombie:");
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

    private void setAllZombies(int instanceId) {
        for (PlayerEventInfo player : this.getPlayers(instanceId)) {
            try {
                if (player.getTeamId() != 1) continue;
                player.getEventTeam().removePlayer(player);
                ((EventTeam)((FastMap)this._teams.get((Object)player.getInstanceId())).get((Object)2)).addPlayer(player, true);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkIfAnyPlayersLeft(int instanceId) {
        FastMap fastMap = this._teams;
        synchronized (fastMap) {
            if (((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)1)).getPlayers().size() <= 0) {
                this.announce(instanceId, "All survivors have died!");
                this.endInstance(instanceId, true, true, false);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onEventEnd() {
        if (NexusLoader.detailedDebug) {
            this.print("Event: onEventEnd()");
        }
        int minScore = this.getInt("killsForReward");
        this.rewardAllPlayersFromTeam(-1, minScore, 0, 2);
    }

    @Override
    protected String getTitle(PlayerEventInfo pi) {
        if (pi.isAfk()) {
            return "AFK";
        }
        if (pi.getTeamId() == 2) {
            return "~ ZOMBIE ~";
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
        ++this._tick;
        if (this._tick % this._tickLength != 0) {
            return;
        }
        if (this._ammoSystem) {
            int oneAmmoMp = 0;
            for (TeamVsTeam.TvTEventInstance match : this._matches.values()) {
                for (PlayerEventInfo player : this.getPlayers(match.getInstance().getId())) {
                    if (player.getTeamId() != 1) continue;
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
        if (player.getPlayersId() != target.getObjectId()) {
            if (player.getTeamId() == 2) {
                player.getEventTeam().raiseScore(this._zombieKillScore);
                player.getEventTeam().raiseKills(this._zombieKillScore);
                this.getPlayerData(player).raiseScore(this._zombieKillScore);
                this.getPlayerData(player).raiseKills(this._zombieKillScore);
                this.getPlayerData(player).raiseSpree(1);
            } else if (player.getTeamId() == 1) {
                player.getEventTeam().raiseScore(this._survivorKillScore);
                player.getEventTeam().raiseKills(this._survivorKillScore);
                this.getPlayerData(player).raiseScore(this._survivorKillScore);
                this.getPlayerData(player).raiseKills(this._survivorKillScore);
                this.getPlayerData(player).raiseSpree(1);
            }
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
        if (player.getTeamId() == 2) {
            if (this._waweRespawn) {
                this._waweScheduler.addPlayer(player);
            } else {
                this.scheduleRevive(player, this.getInt("resDelay") * 1000);
            }
        } else {
            this.transformToZombie(player);
        }
    }

    @Override
    public boolean onAttack(CharacterData cha, CharacterData target) {
        PlayerEventInfo player;
        if (this._ammoSystem && cha.isPlayer() && target.isPlayer() && (player = cha.getEventInfo()).getTeamId() == 1) {
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
        if (player.getTeamId() == 2) {
            return false;
        }
        if (item.getItemId() == this._bowItemId && item.isEquipped()) {
            return false;
        }
        if (item.isWeapon()) {
            return false;
        }
        return super.canUseItem(player, item);
    }

    @Override
    public boolean canUseSkill(PlayerEventInfo player, SkillData skill) {
        if (this.getEventType() == EventType.Zombies) {
            return false;
        }
        return super.canUseSkill(player, skill);
    }

    @Override
    public void onDamageGive(CharacterData cha, CharacterData target, int damage, boolean isDOT) {
        try {
            if (cha.isPlayer() && target.isPlayer()) {
                PlayerEventInfo targetPlayer = target.getEventInfo();
                PlayerEventInfo player = cha.getEventInfo();
                if (player.getTeamId() != targetPlayer.getTeamId()) {
                    targetPlayer.abortCasting();
                    targetPlayer.doDie(cha);
                }
            }
        }
        catch (NullPointerException e) {
            // empty catch block
        }
    }

    @Override
    public boolean canDestroyItem(PlayerEventInfo player, ItemData item) {
        if (item.getItemId() == this._bowItemId || player.getTeamId() == 2) {
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
        this.scheduleSelectZombies(player.getInstanceId(), 0, false, 0);
    }

    @Override
    protected boolean checkIfEventCanContinue(int instanceId, PlayerEventInfo disconnectedPlayer) {
        int survivors = 0;
        int zombies = 0;
        for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
            if (team.getTeamId() == 1) {
                for (PlayerEventInfo pi : team.getPlayers()) {
                    ++survivors;
                }
            }
            if (team.getTeamId() != 2) continue;
            for (PlayerEventInfo pi : team.getPlayers()) {
                ++zombies;
            }
        }
        if (zombies == 0 ? survivors >= 2 : zombies >= 1 && survivors >= 1) {
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
        ZombiesEventPlayerData d = new ZombiesEventPlayerData(player, this);
        return d;
    }

    @Override
    public ZombiesEventPlayerData getPlayerData(PlayerEventInfo player) {
        return (ZombiesEventPlayerData)player.getEventData();
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
        return new ZombiesEventData(instanceId);
    }

    @Override
    protected ZombiesEventInstance createEventInstance(InstanceData instance) {
        return new ZombiesEventInstance(instance);
    }

    @Override
    protected ZombiesEventData getEventData(int instance) {
        return (ZombiesEventData)((TeamVsTeam.TvTEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._data;
    }

    public class ZombiesEventPlayerData
    extends PvPEventPlayerData {
        public ZombiesEventPlayerData(PlayerEventInfo owner, EventGame event) {
            super(owner, event, new GlobalStatsModel(Zombies.this.getEventType()));
        }
    }

    protected class ZombiesEventInstance
    extends HuntingGrounds.HGEventInstance {
        protected ZombiesEventInstance(InstanceData instance) {
            super(instance);
        }

        @Override
        public void run() {
            try {
                if (NexusLoader.detailedDebug) {
                    Zombies.this.print("Event: running task of state " + this._state.toString() + "...");
                }
                switch (this._state) {
                    case START: {
                        if (!Zombies.this.checkPlayers(this._instance.getId())) break;
                        Zombies.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, false);
                        Zombies.this.setupTitles(this._instance.getId());
                        Zombies.this.removeStaticDoors(this._instance.getId());
                        Zombies.this.enableMarkers(this._instance.getId(), true);
                        Zombies.this.preparePlayers(this._instance.getId(), true);
                        Zombies.this.scheduleSelectZombies(this._instance.getId(), 10000, true, 0);
                        Zombies.this.forceSitAll(this._instance.getId());
                        this.setNextState(TeamVsTeam.EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        Zombies.this.forceStandAll(this._instance.getId());
                        this.setNextState(TeamVsTeam.EventState.END);
                        this._clock.startClock(Zombies.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        this.setNextState(TeamVsTeam.EventState.INACTIVE);
                        Zombies.this.untransformAll(this._instance.getId());
                        Zombies.this.setAllZombies(this._instance.getId());
                        if (Zombies.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            int minScore = Zombies.this.getInt("killsForReward");
                            Zombies.this.rewardAllPlayersFromTeam(this._instance.getId(), minScore, 0, 2);
                        }
                        Zombies.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    Zombies.this.print("Event: ... finished running task. next state " + this._state.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                Zombies.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    private class InactivityTimer
    implements Runnable {
        int _instanceId;
        ScheduledFuture<?> _future;

        InactivityTimer(int instanceId) {
            this._future = null;
            this._instanceId = instanceId;
        }

        @Override
        public void run() {
            Zombies.this.zombiesInactive(this._instanceId);
        }

        public void schedule() {
            if (this._future != null) {
                this.abort();
            }
            this._future = CallBack.getInstance().getOut().scheduleGeneral(this, Zombies.this._zombieInactivityTime * 1000);
        }

        private void abort() {
            if (this._future != null) {
                this._future.cancel(false);
                this._future = null;
            }
        }
    }

    protected class ZombiesEventData
    extends HuntingGrounds.HGEventData {
        protected InactivityTimer _inactivityTimer;

        public ZombiesEventData(int instance) {
            super(instance);
            this._inactivityTimer = null;
        }

        private synchronized void startTimer() {
            if (this._inactivityTimer == null) {
                this._inactivityTimer = new InactivityTimer(this._instanceId);
            }
            this._inactivityTimer.schedule();
        }

        public void setKillMade() {
            if (Zombies.this._zombieInactivityTime > 0) {
                this.startTimer();
            }
        }
    }

}

