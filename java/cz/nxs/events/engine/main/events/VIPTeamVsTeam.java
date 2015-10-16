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
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventManager;
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
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.main.events.TeamVsTeam;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IValues;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class VIPTeamVsTeam
extends TeamVsTeam {
    private int _vipsCount;
    private int _pointsForKillingVip;
    private int _pointsForKillingNonVip;
    private int _chooseFromTopPercent;
    private String _transformId;
    private int _healingRadius;
    private int _healingInterval;
    private boolean _healingVisualEffect;
    private String _healingPowerHp;
    private String _healingPowerMp;
    private String _healingPowerCp;
    private boolean _isHealInPercentHp;
    private boolean _isHealInPercentMp;
    private boolean _isHealInPercentCp;
    private int _vipRespawnDelay;
    private Map<Integer, Integer> _skillsForVip;
    public VIPTvTPlayerData data;
    int tick = 0;
    List<PlayerEventInfo> playersEffects = new FastList();

    public VIPTeamVsTeam(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Winner, RewardPosition.Looser, RewardPosition.Tie, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill, RewardPosition.KillingSpree});
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.addConfig(new ConfigModel("vipsCount", "3", "The number of VIP players in each team."));
        this.addConfig(new ConfigModel("pointsForKillingVip", "5", "The number of score points obtained by killing a VIP player."));
        this.addConfig(new ConfigModel("pointsForKillingNonVip", "1", "The number of score points obtained by killing a NON VIP player. Useful when you want this event to be based only on killing VIPs."));
        this.addConfig(new ConfigModel("chooseVipFromTopPercent", "30", "The VIP players will be randomly selected from the top players (Level or PvPs, depends on <font color=LEVEL>divideToTeamsMethod</font> config) in the team. Use this config to specify (in percent) how many players will be 'marked as TOP'. FOr example, if you set this value to '30' and the team has 100 players, the VIPs will be randomly selected from the top 30 players in the team."));
        this.addConfig(new ConfigModel("transformationId", "0", "You can specify if the player, who becames VIP, will be transformed into a transformation (eg. Zariche). Use this format to select the transformation ID per each team: <font color=5C8D5F>TEAM_ID</font>-<font color=635694>TRANSFORMATION_ID</font>,<font color=5C8D5F>TEAM_ID</font>-<font color=635694>TRANSFORMATION_ID</font> (eg. <font color=5C8D5F>1</font>-<font color=635694>301</font>,<font color=5C8D5F>2</font>-<font color=635694>302</font> will make team 1 (blue) VIPs to transform into Zariches and team 2 (red) VIPs to transform into Akamanahs). Put 0 to disable this feature."));
        this.addConfig(new ConfigModel("minPlayers", "4", "The minimum count of players required to start one instance of the event. <font color=FF0000>Minimum 4 is required for this event, otherwise this event will not start!</font>"));
        this.addConfig(new ConfigModel("vipHealRadius", "800", "The max. radius in which the VIP player can heal all nearby players. Each player can be healed only by one VIP."));
        this.addConfig(new ConfigModel("healInterval", "3", "Put here how often will the player be healed by the VIP (HP/MP/CP heal). Value in seconds - setting it to eg. 3 will heal player each 3 seconds, if he's standing near the VIP. Put 0 to turn the healing off."));
        this.addConfig(new ConfigModel("healVisualEffect", "true", "Put true to show some visual effects for players standing near the VIP. Works only if teams count = 2.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("vipHpHealPower", "0.5%", "Put here how much will the player's HP be healed, if the player stands near his team's VIP. Value can be a decimal and can also end with % - that will make the value in percent."));
        this.addConfig(new ConfigModel("vipMpHealPower", "1%", "Put here how much will the player's MP be healed, if the player stands near his team's VIP. Value can be a decimal and can also end with % - that will make the value in percent."));
        this.addConfig(new ConfigModel("vipCpHealPower", "10", "Put here how much will the player's CP be healed, if the player stands near his team's VIP. Value can be a decimal and can also end with % - that will make the value in percent."));
        this.addConfig(new ConfigModel("vipSpecialSkills", "395-1,396-1,1374-1,1375-1,1376-1,7065-1", "You can specify which skills will be given to all VIPs here. Format - SKILLID-LEVEL (eg. 25-2 (skill id 25, lvl 2). Default: All hero skills <font color=4f4f4f>(395, 396, 1374, 1375, 1376)</font>; custom skill to slow to 110 speed + lower the power of heal skills done on the VIP (by 75%) + raise max CP (+30000) + CP reg rate (x2) <font color=4f4f4f>(7065)</font>.", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("vipRespawnDelay", "10", "You can specify the delay after which new VIPs will be selected, if the old vips died. In seconds."));
    }

    @Override
    public void initEvent() {
        super.initEvent();
        this._vipsCount = this.getInt("vipsCount");
        this._pointsForKillingVip = this.getInt("pointsForKillingVip");
        this._pointsForKillingNonVip = this.getInt("pointsForKillingNonVip");
        this._chooseFromTopPercent = this.getInt("chooseVipFromTopPercent");
        this._transformId = this.getString("transformationId");
        this._healingRadius = this.getInt("vipHealRadius");
        this._healingInterval = this.getInt("healInterval");
        this._healingVisualEffect = this._teamsCount == 2 ? this.getBoolean("healVisualEffect") : false;
        this._healingPowerHp = this.getString("vipHpHealPower");
        this._healingPowerMp = this.getString("vipMpHealPower");
        this._healingPowerCp = this.getString("vipCpHealPower");
        this._isHealInPercentHp = this._healingPowerHp.endsWith("%");
        this._isHealInPercentMp = this._healingPowerMp.endsWith("%");
        this._isHealInPercentCp = this._healingPowerCp.endsWith("%");
        this._vipRespawnDelay = this.getInt("vipRespawnDelay") * 1000;
        String skills = this.getString("vipSpecialSkills");
        if (!(skills == null || skills.isEmpty())) {
            this._skillsForVip = new FastMap();
            for (String skill : skills.split(",")) {
                try {
                    this._skillsForVip.put(Integer.parseInt(skill.split("-")[0]), Integer.parseInt(skill.split("-")[1]));
                    continue;
                }
                catch (Exception e) {
                    NexusLoader.debug((String)"Wrong format for the vipSpecialSkills config of TvTA event.", (Level)Level.WARNING);
                    e.printStackTrace();
                    this._skillsForVip = null;
                    break;
                }
            }
        }
    }

    @Override
    public void runEvent() {
        super.runEvent();
    }

    private void scheduleSelectVips(int instance, int teamId, boolean eventStart, boolean shortDelay) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: scheduling select vips for team id " + teamId + " in instance " + instance + ". event start = " + eventStart + ", short delay = " + shortDelay);
        }
        if (eventStart) {
            this.announce(instance, LanguageEngine.getMsg("vip_selectNew", this._vipRespawnDelay / 1000));
        }
        int delay = this._vipRespawnDelay;
        if (shortDelay) {
            delay/=2;
        }
        CallBack.getInstance().getOut().scheduleGeneral(new SelectVipsTask(instance, teamId), delay);
    }

    protected synchronized void selectVips(int instanceId, int teamId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: selecting VIPs of instanceId " + instanceId + " for team " + teamId);
        }
        FastList newVips = new FastList();
        FastList temp = new FastList();
        List possibleVips = new FastList();
        for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
            int i;
            if (teamId != -1 && teamId != team.getTeamId()) continue;
            if (team.getPlayers().isEmpty()) continue;
            int currentVipsCount = 0;
            for (PlayerEventInfo player : team.getPlayers()) {
                if (this.getPlayerData(player).isVIP) {
                    ++currentVipsCount;
                    continue;
                }
                temp.add(player);
            }
            int count = this._vipsCount - currentVipsCount;
            if (NexusLoader.detailedDebug) {
                this.print("Event: selecting vips: team " + team.getTeamName() + "(" + team.getTeamId() + ") needs " + count + " VIPs.");
            }
            String s = this.getString("divideToTeamsMethod");
            Collections.sort(temp, EventManager.getInstance().compareByLevels);
            if (s.startsWith("PvPs")) {
                Collections.sort(temp, EventManager.getInstance().compareByPvps);
            }
            int from = 0;
            int to = (int)Math.ceil((double)temp.size() * ((double)this._chooseFromTopPercent / 100.0));
            block2 : for (i = 0; count > 0 && i < temp.size(); ++i) {
                possibleVips = temp.subList(from, Math.min(to + i, temp.size()));
                Collections.shuffle(possibleVips);
                for (PlayerEventInfo possibleVip : possibleVips) {
                    if (possibleVip == null || possibleVip.isDead() || possibleVip.isAfk() || this.getPlayerData(possibleVip).wasVIP) continue;
                    temp.remove((Object)possibleVip);
                    newVips.add(possibleVip);
                    if (--count > 0) continue;
                    continue block2;
                }
            }
            if (NexusLoader.detailedDebug) {
                this.print("Event: selecting vips part 2, count = " + count);
            }
            if (count > 0) {
                for (PlayerEventInfo player2 : temp) {
                    this.getPlayerData(player2).wasVIP = false;
                }
                from = 0;
                to = (int)Math.ceil((double)temp.size() * ((double)this._chooseFromTopPercent / 100.0));
                block5 : for (i = 0; count > 0 && i < temp.size(); ++i) {
                    possibleVips = temp.subList(from, Math.min(to + i, temp.size()));
                    Collections.shuffle(possibleVips);
                    for (PlayerEventInfo possibleVip : possibleVips) {
                        if (possibleVip == null || possibleVip.isDead() || possibleVip.isAfk() || this.getPlayerData(possibleVip).wasVIP) continue;
                        temp.remove((Object)possibleVip);
                        newVips.add(possibleVip);
                        if (--count > 0) continue;
                        continue block5;
                    }
                }
            }
            if (NexusLoader.detailedDebug) {
                this.print("Event: selecting vips part 3, count = " + count);
            }
            if (count > 0) {
                this.scheduleSelectVips(instanceId, team.getTeamId(), false, true);
            }
            temp.clear();
        }
        for (PlayerEventInfo player : newVips) {
            this.markVip(player);
            EventSpawn spawn = this.getSpawn(SpawnType.VIP, player.getTeamId());
            if (spawn == null) {
                NexusLoader.debug((String)("Missing spawn VIP for team " + (((FastMap)this._teams.get((Object)instanceId)).size() == 1 ? -1 : player.getTeamId()) + ", map " + this._manager.getMap().getMapName() + ", event " + this.getEventType().getAltTitle() + " !!"), (Level)Level.SEVERE);
            }
            Loc loc = new Loc(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ());
            loc.addRadius(spawn.getRadius());
            player.teleport(loc, 0, true, instanceId);
            if (!this.getBoolean("removeBuffsOnStart")) continue;
            player.removeBuffs();
        }
    }

    private void transform(PlayerEventInfo player) {
        String[] s;
        if (this._transformId == null || this._transformId.equals("0")) {
            return;
        }
        int id = 0;
        try {
            id = Integer.parseInt(this._transformId);
        }
        catch (Exception e) {
            id = 0;
        }
        if (id > 0) {
            player.transform(id);
            return;
        }
        for (String d : s = this._transformId.split(",")) {
            try {
                if (Integer.parseInt(d.split("-")[0]) != player.getTeamId()) continue;
                player.transform(Integer.parseInt(d.split("-")[1]));
                continue;
            }
            catch (Exception e) {
                // empty catch block
            }
        }
    }

    protected void markVip(PlayerEventInfo player) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: marking " + player.getPlayersName() + " in instance " + player.getInstanceId() + " as VIP.");
        }
        if (!this.getPlayerData(player).isVip()) {
            this.transform(player);
            this.vipSkills(player, true);
            player.startAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REAL_TARGET());
            this.getPlayerData(player).setVIP(true);
            if (!this.getEventData(player.getInstanceId())._vips.containsKey(player.getTeamId())) {
                this.getEventData(player.getInstanceId())._vips.put(player.getTeamId(), new FastList());
            }
            ((List)this.getEventData(player.getInstanceId())._vips.get(player.getTeamId())).add(player);
            player.setTitle(this.getTitle(player), true);
            player.broadcastTitleInfo();
            this.announce(player.getInstanceId(), "* " + LanguageEngine.getMsg("vip_becomeVip", player.getPlayersName()), player.getTeamId());
        }
    }

    protected void vipSkills(PlayerEventInfo player, boolean add) {
        if (this._skillsForVip != null) {
            for (Map.Entry<Integer, Integer> sk : this._skillsForVip.entrySet()) {
                SkillData skill = new SkillData(sk.getKey().intValue(), sk.getValue().intValue());
                if (add) {
                    player.addSkill(skill, false);
                    continue;
                }
                player.removeSkill(skill.getId());
            }
        }
        if (add) {
            player.setCurrentHp(player.getMaxHp());
            player.setCurrentMp(player.getMaxMp());
            player.setCurrentCp(player.getMaxCp());
        }
    }

    protected void cleanVip(PlayerEventInfo player) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: removing/cleaning " + player.getPlayersName() + " in instance " + player.getInstanceId() + " from VIP.");
        }
        if (this.getPlayerData(player).isVip()) {
            this.vipSkills(player, false);
            player.untransform(true);
            player.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REAL_TARGET());
            this.getPlayerData(player).setVIP(false);
            if (this.getEventData(player.getInstanceId())._vips.containsKey(player.getTeamId())) {
                ((List)this.getEventData(player.getInstanceId())._vips.get(player.getTeamId())).remove((Object)player);
            }
            player.setTitle(this.getTitle(player), true);
            player.broadcastTitleInfo();
        }
    }

    @Override
    public void onEventEnd() {
        super.onEventEnd();
    }

    @Override
    protected String getTitle(PlayerEventInfo pi) {
        if (this._hideTitles) {
            return "";
        }
        if (this.getPlayerData(pi).isVip()) {
            return "[VIP]";
        }
        if (pi.isAfk()) {
            return "AFK";
        }
        return LanguageEngine.getMsg("event_title_scoredeath", this.getPlayerData(pi).getScore(), this.getPlayerData(pi).getDeaths());
    }

    @Override
    public void onKill(PlayerEventInfo player, CharacterData target) {
        if (target.getEventInfo() == null) {
            return;
        }
        PlayerEventInfo targetInfo = target.getEventInfo();
        if (player.getTeamId() != targetInfo.getTeamId()) {
            this.tryFirstBlood(player);
            if (this.getPlayerData(targetInfo).isVip()) {
                this.giveOnKillReward(player);
                player.getEventTeam().raiseScore(this._pointsForKillingVip);
                player.getEventTeam().raiseKills(this._pointsForKillingVip);
                this.getPlayerData(player).raiseScore(this._pointsForKillingVip);
                this.getPlayerData(player).raiseKills(this._pointsForKillingVip);
                this.getPlayerData(player).raiseSpree(1);
                this.giveKillingSpreeReward(this.getPlayerData(player));
                CallbackManager.getInstance().playerKillsVip(this.getEventType(), player, target.getEventInfo());
            } else {
                this.giveOnKillReward(player);
                player.getEventTeam().raiseScore(this._pointsForKillingNonVip);
                player.getEventTeam().raiseKills(this._pointsForKillingNonVip);
                this.getPlayerData(player).raiseScore(this._pointsForKillingNonVip);
                this.getPlayerData(player).raiseKills(this._pointsForKillingNonVip);
                this.getPlayerData(player).raiseSpree(1);
                this.giveKillingSpreeReward(this.getPlayerData(player));
                CallbackManager.getInstance().playerKills(this.getEventType(), player, target.getEventInfo());
            }
            if (player.isTitleUpdated()) {
                player.setTitle(this.getTitle(player), true);
                player.broadcastTitleInfo();
            }
            this.setScoreStats(player, this.getPlayerData(player).getScore());
            this.setKillsStats(player, this.getPlayerData(player).getKills());
        }
        if (this.getPlayerData(targetInfo).isVip()) {
            this.announceToAllTeamsBut(targetInfo.getInstanceId(), "[+] " + LanguageEngine.getMsg("vip_vipDied", targetInfo.getPlayersName(), targetInfo.getEventTeam().getTeamName()), targetInfo.getTeamId());
            this.announce(targetInfo.getInstanceId(), "[-] " + LanguageEngine.getMsg("vip_vipKilled", targetInfo.getPlayersName()), targetInfo.getTeamId());
            this.cleanVip(targetInfo);
            this.scheduleSelectVips(targetInfo.getInstanceId(), targetInfo.getTeamId(), false, false);
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
    public int allowTransformationSkill(PlayerEventInfo playerEventInfo, SkillData skillData) {
        if (this._skillsForVip.containsKey(skillData.getId())) {
            return 1;
        }
        return 0;
    }

    @Override
    public void playerWentAfk(PlayerEventInfo player, boolean warningOnly, int afkTime) {
        if (warningOnly) {
            player.sendMessage(LanguageEngine.getMsg("event_afkWarning_kill", PlayerEventInfo.AFK_WARNING_DELAY / 1000, PlayerEventInfo.AFK_KICK_DELAY / 1000));
        } else if (((TeamVsTeam.TvTEventInstance)this._matches.get((Object)Integer.valueOf((int)player.getInstanceId())))._state == TeamVsTeam.EventState.END && this.getPlayerData(player).isVIP) {
            this.announce(player.getInstanceId(), "* " + LanguageEngine.getMsg("vip_vipAfk", player.getPlayersName()), player.getTeamId());
            this.announceToAllTeamsBut(player.getInstanceId(), "* " + LanguageEngine.getMsg("vip_enemyVipAfk", player.getPlayersName()), player.getTeamId());
            player.doDie();
        }
    }

    @Override
    public EventPlayerData createPlayerData(PlayerEventInfo player) {
        VIPTvTPlayerData d = new VIPTvTPlayerData(player, this);
        return d;
    }

    @Override
    public VIPTvTPlayerData getPlayerData(PlayerEventInfo player) {
        return (VIPTvTPlayerData)player.getEventData();
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
                    for (PlayerEventInfo player : this.getPlayers(match.getInstance().getId())) {
                        if (!this.getPlayerData(player).isVip()) continue;
                        this.cleanVip(player);
                    }
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
            player.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_IMPRISIONING_1());
            player.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REDCIRCLE());
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
    public void onDisconnect(PlayerEventInfo player) {
        if (this.getPlayerData(player).isVip()) {
            this.cleanVip(player);
            this.announce(player.getInstanceId(), "* " + LanguageEngine.getMsg("vip_vipDisconnected", player.getPlayersName(), player.getEventTeam().getTeamName()));
            this.scheduleSelectVips(player.getInstanceId(), player.getTeamId(), false, true);
        }
        super.onDisconnect(player);
    }

    @Override
    protected boolean checkIfEventCanContinue(int instanceId, PlayerEventInfo disconnectedPlayer) {
        int teamsOn = 0;
        for (EventTeam team : ((FastMap)this._teams.get((Object)instanceId)).values()) {
            int temp = 0;
            for (PlayerEventInfo pi : team.getPlayers()) {
                if (pi == null || !pi.isOnline()) continue;
                ++temp;
            }
            if (temp < 2) continue;
            ++teamsOn;
        }
        return teamsOn >= 2;
    }

    @Override
    protected void respawnPlayer(PlayerEventInfo pi, int instance) {
        if (NexusLoader.detailedDebug) {
            this.print("/// Event: respawning player " + pi.getPlayersName() + ", instance " + instance);
        }
        EventSpawn spawn = this.getPlayerData(pi).isVip() ? this.getSpawn(SpawnType.VIP, pi.getTeamId()) : this.getSpawn(SpawnType.Regular, pi.getTeamId());
        if (spawn != null) {
            Loc loc = new Loc(spawn.getLoc().getX(), spawn.getLoc().getY(), spawn.getLoc().getZ());
            loc.addRadius(spawn.getRadius());
            pi.teleport(loc, 0, true, instance);
            pi.sendMessage(LanguageEngine.getMsg("event_respawned"));
        } else {
            this.debug("Error on respawnPlayer - no spawn type REGULAR or VIP, team " + pi.getTeamId() + " has been found. Event aborted.");
        }
    }

    @Override
    protected void clockTick() throws Exception {
        int healingRadius = (int)Math.pow(this._healingRadius, 2.0);
        ++this.tick;
        for (TeamVsTeam.TvTEventInstance match : this._matches.values()) {
            for (Map.Entry e : this.getEventData(match.getInstance().getId())._vips.entrySet()) {
                int teamId = (Integer)e.getKey();
                for (PlayerEventInfo vip : (List)e.getValue()) {
                    for (PlayerEventInfo player : this.getPlayers(match.getInstance().getId())) {
                        double value;
                        if (player.getTeamId() != teamId || this.getPlayerData(player).isVIP || !this.getPlayerData(player).canHeal() || player.isDead() || player.getPlanDistanceSq(vip.getX(), vip.getY()) > (double)healingRadius) continue;
                        if (this._healingVisualEffect) {
                            this.playersEffects.add(player);
                        }
                        if (this._healingInterval <= 0 || this.tick % this._healingInterval != 0) continue;
                        if (this._isHealInPercentHp) {
                            value = Double.parseDouble(this._healingPowerHp.substring(0, this._healingPowerHp.length() - 1));
                            if (value > 0.0 && player.getCurrentHp() < (double)player.getMaxHp()) {
                                value = (double)(player.getMaxHp() / 100) * value;
                                player.setCurrentHp((int)(player.getCurrentHp() + value));
                            }
                        } else {
                            value = Double.parseDouble(this._healingPowerHp);
                            if (value > 0.0 && player.getCurrentHp() < (double)player.getMaxHp()) {
                                player.setCurrentHp((int)(player.getCurrentHp() + value));
                            }
                        }
                        if (this._isHealInPercentMp) {
                            value = Double.parseDouble(this._healingPowerMp.substring(0, this._healingPowerMp.length() - 1));
                            if (value > 0.0 && player.getCurrentMp() < (double)player.getMaxMp()) {
                                value = (double)(player.getMaxMp() / 100) * value;
                                player.setCurrentMp((int)(player.getCurrentMp() + value));
                            }
                        } else {
                            value = Double.parseDouble(this._healingPowerMp);
                            if (value > 0.0 && player.getCurrentMp() < (double)player.getMaxMp()) {
                                player.setCurrentMp((int)(player.getCurrentMp() + value));
                            }
                        }
                        if (this._isHealInPercentCp) {
                            value = Double.parseDouble(this._healingPowerCp.substring(0, this._healingPowerCp.length() - 1));
                            if (value <= 0.0 || player.getCurrentCp() >= (double)player.getMaxCp()) continue;
                            value = (double)(player.getMaxCp() / 100) * value;
                            player.setCurrentCp((int)(player.getCurrentCp() + value));
                            continue;
                        }
                        value = Double.parseDouble(this._healingPowerCp);
                        if (value <= 0.0 || player.getCurrentCp() >= (double)player.getMaxCp()) continue;
                        player.setCurrentCp((int)(player.getCurrentCp() + value));
                    }
                }
            }
            for (PlayerEventInfo player : this.getPlayers(match.getInstance().getId())) {
                this.getPlayerData(player).tickEnd();
                if (this.playersEffects.contains((Object)player) || this.getPlayerData(player).isVip()) {
                    this.startPlayerEffects(player, player.getTeamId());
                    continue;
                }
                this.startPlayerEffects(player, 0);
            }
            this.playersEffects.clear();
        }
    }

    private void startPlayerEffects(PlayerEventInfo player, int teamId) {
        if (teamId == 1) {
            player.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REDCIRCLE());
            player.startAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_IMPRISIONING_1());
        } else if (teamId == 2) {
            player.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_IMPRISIONING_1());
            player.startAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REDCIRCLE());
        } else {
            player.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_REDCIRCLE());
            player.stopAbnormalEffect(CallBack.getInstance().getValues().ABNORMAL_IMPRISIONING_1());
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
    public String getMissingSpawns(EventMap map) {
        TextBuilder tb = new TextBuilder();
        for (int i = 0; i < this.getTeamsCount(); ++i) {
            if (!map.checkForSpawns(SpawnType.Regular, i + 1, 1)) {
                tb.append(this.addMissingSpawn(SpawnType.Regular, i + 1, 1));
            }
            if (map.checkForSpawns(SpawnType.VIP, i + 1, 1)) continue;
            tb.append(this.addMissingSpawn(SpawnType.VIP, i + 1, 1));
        }
        return tb.toString();
    }

    @Override
    protected TeamVsTeam.TvTEventData createEventData(int instanceId) {
        return new TvTVIPEventData(instanceId);
    }

    @Override
    protected VIPEventInstance createEventInstance(InstanceData instance) {
        return new VIPEventInstance(instance);
    }

    @Override
    protected TvTVIPEventData getEventData(int instance) {
        return (TvTVIPEventData)((TeamVsTeam.TvTEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._data;
    }

    public class VIPTvTPlayerData
    extends PvPEventPlayerData {
        private boolean isVIP;
        private boolean wasVIP;
        private boolean tickHealed;

        public VIPTvTPlayerData(PlayerEventInfo owner, EventGame event) {
            super(owner, event, new GlobalStatsModel(VIPTeamVsTeam.this.getEventType()));
            this.isVIP = false;
            this.wasVIP = false;
            this.tickHealed = false;
        }

        public boolean isVip() {
            return this.isVIP;
        }

        public boolean wasVIP() {
            return this.wasVIP;
        }

        public void setVIP(boolean b) {
            if (this.isVIP && !b) {
                this.wasVIP = true;
            }
            this.isVIP = b;
        }

        public boolean canHeal() {
            if (this.tickHealed) {
                return false;
            }
            this.tickHealed = true;
            return true;
        }

        public void tickEnd() {
            this.tickHealed = false;
        }
    }

    private class SelectVipsTask
    implements Runnable {
        final int instance;
        final int teamId;

        public SelectVipsTask(int instance, int teamId) {
            this.instance = instance;
            this.teamId = teamId;
        }

        @Override
        public void run() {
            if (((TeamVsTeam.TvTEventInstance)VIPTeamVsTeam.this._matches.get((Object)Integer.valueOf((int)this.instance)))._state == TeamVsTeam.EventState.END) {
                try {
                    VIPTeamVsTeam.this.selectVips(this.instance, this.teamId);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    if (NexusLoader.detailedDebug) {
                        VIPTeamVsTeam.this.print("Event: error while selecting new vips: " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
                    }
                    VIPTeamVsTeam.this.announce("Sorry, an error occured in this event.");
                    VIPTeamVsTeam.this.clearEvent();
                }
            }
        }
    }

    protected class VIPEventInstance
    extends TeamVsTeam.TvTEventInstance {
        protected VIPEventInstance(InstanceData instance) {
            super(instance);
        }

        @Override
        public void run() {
            try {
                if (NexusLoader.detailedDebug) {
                    VIPTeamVsTeam.this.print("Event: running task of state " + this._state.toString() + "...");
                }
                switch (this._state) {
                    case START: {
                        if (!VIPTeamVsTeam.this.checkPlayers(this._instance.getId())) break;
                        VIPTeamVsTeam.this.teleportPlayers(this._instance.getId(), SpawnType.Regular, false);
                        VIPTeamVsTeam.this.setupTitles(this._instance.getId());
                        VIPTeamVsTeam.this.enableMarkers(this._instance.getId(), true);
                        VIPTeamVsTeam.this.forceSitAll(this._instance.getId());
                        this.setNextState(TeamVsTeam.EventState.FIGHT);
                        this.scheduleNextTask(10000);
                        break;
                    }
                    case FIGHT: {
                        VIPTeamVsTeam.this.forceStandAll(this._instance.getId());
                        this.setNextState(TeamVsTeam.EventState.END);
                        VIPTeamVsTeam.this.scheduleSelectVips(this._instance.getId(), -1, true, false);
                        this._clock.startClock(VIPTeamVsTeam.this._manager.getRunTime());
                        break;
                    }
                    case END: {
                        this._clock.setTime(0, true);
                        for (PlayerEventInfo player : VIPTeamVsTeam.this.getPlayers(this._instance.getId())) {
                            if (!VIPTeamVsTeam.this.getPlayerData(player).isVip()) continue;
                            VIPTeamVsTeam.this.cleanVip(player);
                        }
                        this.setNextState(TeamVsTeam.EventState.INACTIVE);
                        if (VIPTeamVsTeam.this.instanceEnded() || !this._canBeAborted) break;
                        if (this._canRewardIfAborted) {
                            VIPTeamVsTeam.this.rewardAllTeams(this._instance.getId(), 0, VIPTeamVsTeam.this.getInt("killsForReward"));
                        }
                        VIPTeamVsTeam.this.clearEvent(this._instance.getId());
                    }
                }
                if (NexusLoader.detailedDebug) {
                    VIPTeamVsTeam.this.print("Event: ... finished running task. next state " + this._state.toString());
                }
            }
            catch (Throwable e) {
                e.printStackTrace();
                VIPTeamVsTeam.this._manager.endDueToError(LanguageEngine.getMsg("event_error"));
            }
        }
    }

    protected class TvTVIPEventData
    extends TeamVsTeam.TvTEventData {
        private Map<Integer, List<PlayerEventInfo>> _vips;

        public TvTVIPEventData(int instance) {
            super(instance);
            this._vips = new FastMap();
        }
    }

}

