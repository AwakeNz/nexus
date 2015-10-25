/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.PartyData
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.mini;

import cz.nxs.events.Configurable;
import cz.nxs.events.engine.EventBuffer;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.EventMapSystem;
import cz.nxs.events.engine.EventWarnings;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.Event;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.html.EventHtmlManager;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.MiniEventGame;
import cz.nxs.events.engine.mini.RegistrationData;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.events.engine.mini.features.DelaysFeature;
import cz.nxs.events.engine.mini.features.StrenghtChecksFeature;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;
import javolution.util.FastList;
import javolution.util.FastMap;

public abstract class MiniEventManager
extends Event
implements Runnable,
Configurable {
    protected static Logger _log = Logger.getLogger(MiniEventManager.class.getName());
    protected List<RegistrationData> _parties;
    protected int _lastGameId;
    protected List<MiniEventGame> _games;
    protected Map<Integer, Long> _loggedPlayers;
    protected boolean _locked;
    protected boolean _canRun;
    protected boolean _tournamentActive;
    protected EventMode _mode;
    private FastMap<String, ConfigModel> _configs;
    private FastMap<String, ConfigModel> _mapConfigs;
    private FastList<String> _configCategories;
    protected String _htmlDescription = null;
    private Comparator<RegistrationData> _compareByLevel;

    public MiniEventManager(EventType type) {
        super(type);
        this.set_compareByLevel(new Comparator<RegistrationData>(){

            @Override
            public int compare(RegistrationData p1, RegistrationData p2) {
                int level2;
                int level1 = p1.getAverageLevel();
                return level1 == (level2 = p2.getAverageLevel()) ? 0 : (level1 < level2 ? -1 : 1);
            }
        });
        this._tournamentActive = false;
        this._parties = new FastList();
        this._games = new FastList();
        this._loggedPlayers = new FastMap();
        this._mode = new EventMode(this.getEventType());
        this._configs = new FastMap();
        this._mapConfigs = new FastMap();
        this._configCategories = new FastList();
        this.loadConfigs();
        this._lastGameId = 0;
        this._canRun = false;
    }

    @Override
    public void loadConfigs() {
        this.addConfig(new ConfigModel("DelayToWaitSinceLastMatchMs", "600000", "The delay the player has to wait to join this event again, after the his last event ended. In miliseconds."));
        this.addConfig(new ConfigModel("TimeLimitMs", "600000", "The delay after the match will be automatically aborted. In ms (miliseconds)."));
        this.addConfig(new ConfigModel("MaxLevelDifference", "5", "Maximum level difference between opponents in the event."));
        this.addConfig(new ConfigModel("MinLevelToJoin", "0", "Minimum level for players participating the event (playerLevel >= value)."));
        this.addConfig(new ConfigModel("MaxLevelToJoin", "100", "Maximum level for players participating the event (playerLevel <= value)."));
        this.addConfig(new ConfigModel("notAllowedSkills", "", "Put here skills that won't be aviable for use in this event <font color=7f7f7f>(write one skill's ID and click Add, to remove the skill, simply click on it's ID in the list)</font>", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("notAllowedItems", "", "Put here items that won't be aviable for use in this event <font color=7f7f7f>(write one skill's ID and click Add; to remove the skill, simply click on it's ID in the list)</font>", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("setOffensiveSkills", "", "Skills written here will be usable only on player's opponents/enemies (not teammates) during events. <font color=7f7f7f>(write one skill's ID and click Add; to remove the skill, simply click on it's ID in the list)</font>", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("setNotOffensiveSkills", "", "Skills written here will be usable only on player's teammates (not opponents/enemies) during events. <font color=7f7f7f>(write one skill's ID and click Add; to remove the skill, simply click on it's ID in the list).", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("setNeutralSkills", "994", "Skills written here will be usable on both teammates and enemies Useful for example for skill Rush (ID 994), which is by default not offensive, and thus the engine doesn't allow the player to cast it on his opponent <font color=7f7f7f>(write one skill's ID and click Add; to remove the skill, simply click on it's ID in the list)</font>", ConfigModel.InputType.MultiAdd));
        this.addConfig(new ConfigModel("allowPotions", "false", "Put false if you want to disable potions on this event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("allowSummons", "true", "Put false if you want to disable summons on this event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("allowPets", "true", "Put false if you want to disable pets on this event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("allowHealers", "true", "Put false if you want to disable healers/buffers on this event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("removeCubics", "false", "Put true to remove cubics upon teleportation to the event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("dualboxCheckForEnemies", "true", "If enabled, only players with different IPs can be enemies in this event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("maxPlayersPerIp", "1", "You can specify here how many players with the same IP are allowed to be in the event. Put -1 to disable this feature."));
        this.addConfig(new ConfigModel("removeBuffsOnStart", "true", "If 'true', all buffs will be removed from players on first teleport to the event.", ConfigModel.InputType.Boolean));
        this.addConfig(new ConfigModel("removeBuffsOnRespawn", "false", "If 'true', all buffs will be removed from players when they respawn (or when the next round starts).", ConfigModel.InputType.Boolean));
    }

    public void setConfigs(Configurable template) {
        try {
            block2 : for (ConfigModel templateModel : template.getConfigs().values()) {
                for (ConfigModel thisModel : this.getConfigs().values()) {
                    if (!templateModel.getKey().equals(thisModel.getKey())) continue;
                    thisModel.setValue(templateModel.getValue());
                    continue block2;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract boolean checkCanFight(PlayerEventInfo var1, RegistrationData[] var2);

    public void check() {
        if (!this.checkCanRun()) {
            this.cleanMe(false);
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    MiniEventManager.this.check();
                }
            }, 30000);
            return;
        }
        if (this.getStartGameInterval() > 0) {
            CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                @Override
                public void run() {
                    MiniEventManager.this.createGame();
                }
            }, this.getStartGameInterval());
        }
    }

    public boolean checkCanRun() {
        int workingMapsCount = 0;
        for (EventMap map : EventMapSystem.getInstance().getMaps(this.getEventType()).values()) {
            if (this._mode.getDisMaps().contains((Object)map.getGlobalId()) || !this.canRun(map)) continue;
            ++workingMapsCount;
        }
        this._canRun = workingMapsCount > 0;
        return this._canRun;
    }

    @Override
    public void run() {
        this.check();
    }

    public void createGame() {
    }

    protected RegistrationData findOpponent(RegistrationData team) {
        for (RegistrationData opponent : this._parties) {
            if (opponent.isChosen() || opponent.getKeyPlayer().getPlayersId() == team.getKeyPlayer().getPlayersId() || !this.strenghtChecks(team, opponent) || !this.ipChecks(team, opponent)) continue;
            return opponent;
        }
        return null;
    }

    public boolean launchGame(RegistrationData[] teams, EventMap map) {
        return false;
    }

    public void cleanMe(boolean abortMatches) {
        this._locked = true;
        if (abortMatches) {
            for (MiniEventGame game : this._games) {
                game.abortDueToError(LanguageEngine.getMsg("game_aborted"));
            }
        }
        for (RegistrationData data : this._parties) {
            data.message(LanguageEngine.getMsg("game_unregistered", this.getEventName()), false);
            data.register(false, null);
        }
        this._games.clear();
        this._parties.clear();
        this._loggedPlayers.clear();
        this._locked = false;
    }

    protected int getStartGameInterval() {
        return 30000;
    }

    public int getDefaultPartySizeToJoin() {
        return 5;
    }

    protected int getNextGameId() {
        return ++this._lastGameId;
    }

    public int getJoinTimeRestriction() {
        for (AbstractFeature f : this._mode.getFeatures()) {
            if (f.getType() != EventMode.FeatureType.Delays) continue;
            return ((DelaysFeature)f).getRejoinDealy();
        }
        return this.getInt("DelayToWaitSinceLastMatchMs");
    }

    public boolean registerTeam(PlayerEventInfo player) {
        if (player == null) {
            return false;
        }
        if (!EventManager.getInstance().canRegister(player)) {
            player.sendMessage(LanguageEngine.getMsg("registering_status"));
            return false;
        }
        if (player.isRegistered()) {
            player.sendMessage(LanguageEngine.getMsg("registering_alreadyRegistered"));
            return false;
        }
        int i = EventWarnings.getInstance().getPoints(player);
        if (i >= EventWarnings.MAX_WARNINGS) {
            player.sendMessage(LanguageEngine.getMsg("registering_warningPoints", EventWarnings.MAX_WARNINGS, i));
            return false;
        }
        if (!this._mode.checkPlayer(player)) {
            player.sendMessage(LanguageEngine.getMsg("registering_notAllowed"));
            return false;
        }
        int playerLevel = player.getLevel();
        int maxLevel = this.getInt("MaxLevelToJoin");
        int minLevel = this.getInt("MinLevelToJoin");
        if (playerLevel < minLevel || playerLevel > maxLevel) {
            if (playerLevel < minLevel) {
                player.sendMessage(LanguageEngine.getMsg("registering_lowLevel"));
            } else {
                player.sendMessage(LanguageEngine.getMsg("registering_highLevel"));
            }
            return false;
        }
        if (this.isTemporaryLocked()) {
            player.sendMessage("Try it again in few seconds. If this thing keeps showing up, then there's propably something fucked up with this event, contact a GameMaster for fix.");
            return false;
        }
        if (!this.timeChecks(player)) {
            player.sendMessage(LanguageEngine.getMsg("registering_timeCheckFailed"));
            return false;
        }
        if (!this.ipChecks2(player)) {
            return false;
        }
        if (this.requireParty()) {
            if (player.getParty() == null) {
                player.sendMessage("You must have a party to join the event.");
                return false;
            }
            if (player.getParty().getLeadersId() != player.getPlayersId()) {
                player.sendMessage(LanguageEngine.getMsg("registering_partyLeader"));
                return false;
            }
            if (player.getParty().getMemberCount() != this.getDefaultPartySizeToJoin()) {
                player.sendMessage(LanguageEngine.getMsg("registering_partyMembers", this.getDefaultPartySizeToJoin()));
                return false;
            }
            if (!this.checkPartyStatus(player.getParty())) {
                player.sendMessage(LanguageEngine.getMsg("registering_partyCantRegister"));
                return false;
            }
        }
        if (EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer")) {
            if (!EventBuffer.getInstance().hasBuffs(player)) {
                player.sendMessage(LanguageEngine.getMsg("registering_buffs"));
            }
            EventManager.getInstance().getHtmlManager().showSelectSchemeForEventWindow(player, "none", this.getEventType().getAltTitle());
        }
        return true;
    }

    protected void addParty(RegistrationData playerData) {
        List<RegistrationData> list = this._parties;
        synchronized (list) {
            this._parties.add(playerData);
        }
    }

    public boolean unregisterTeam(PlayerEventInfo player) {
        if (!(player != null && player.isOnline())) {
            return false;
        }
        if (player.getRegisteredMiniEvent() == null || player.getRegisteredMiniEvent().getEventType() != this.getEventType()) {
            player.sendMessage(LanguageEngine.getMsg("unregistering_notRegistered"));
            return false;
        }
        if (this._locked) {
            player.sendMessage("Try it again in few seconds. If this thing keeps showing up, then there's propably something fucked up with this event, contact GameMaster for fix.");
            return false;
        }
        if (this.requireParty()) {
            if (player.getParty() == null) {
                player.sendMessage(LanguageEngine.getMsg("registering_noParty"));
                return false;
            }
            if (player.getParty().getLeadersId() != player.getPlayersId()) {
                player.sendMessage(LanguageEngine.getMsg("registering_partyLeader_unregister"));
                return false;
            }
        }
        return true;
    }

    public void deleteTeam(RegistrationData team) {
        team.message(LanguageEngine.getMsg("unregistering_unregistered2", this.getEventType().getHtmlTitle()), false);
        team.register(false, null);
        List<RegistrationData> list = this._parties;
        synchronized (list) {
            this._parties.remove(team);
        }
    }

    private boolean checkPartyStatus(PartyData party) {
        boolean buffs = EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer");
        for (PlayerEventInfo member : party.getPartyMembers()) {
            if (member == null) continue;
            if (member.isRegistered()) {
                party.getLeader().sendMessage(LanguageEngine.getMsg("registering_party_memberAlreadyRegistered", member.getPlayersName()));
                return false;
            }
            if (!this.timeChecks(member)) {
                party.getLeader().sendMessage(LanguageEngine.getMsg("registering_party_timeCheckFail", member.getPlayersName()));
                return false;
            }
            if (!this.allowHealers() && member.isPriest()) {
                party.getLeader().sendMessage(LanguageEngine.getMsg("registering_party_noHealer"));
                return false;
            }
            if (!buffs) continue;
            if (!EventBuffer.getInstance().hasBuffs(member)) {
                member.sendMessage(LanguageEngine.getMsg("registering_buffs"));
            }
            EventManager.getInstance().getHtmlManager().showSelectSchemeForEventWindow(member, "mini", this.getEventType().getAltTitle());
        }
        return true;
    }

    protected boolean timeChecks(PlayerEventInfo player) {
        int delay = this.getJoinTimeRestriction();
        int id = player.getPlayersId();
        long time = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> e : this._loggedPlayers.entrySet()) {
            if (e.getKey() != id) continue;
            if (time - (long)delay > e.getValue()) {
                this._loggedPlayers.remove(e);
                return true;
            }
            if (!player.isGM()) {
                player.sendMessage(LanguageEngine.getMsg("registering_timeCheckFail", (e.getValue() + (long)delay - time) / 60000));
                return false;
            }
            return true;
        }
        return true;
    }

    public int getDelayHaveToWaitToJoinAgain(PlayerEventInfo player) {
        int delay = this.getJoinTimeRestriction();
        int id = player.getPlayersId();
        long time = System.currentTimeMillis();
        for (Map.Entry<Integer, Long> e : this._loggedPlayers.entrySet()) {
            if (e.getKey() != id) continue;
            if (time - (long)delay > e.getValue()) {
                this._loggedPlayers.remove(e);
                return 0;
            }
            return (int)(e.getValue() - (time - (long)delay));
        }
        return 0;
    }

    protected void removeInactiveTeams() {
        int playersAmmount = 0;
        for (RegistrationData data : this._parties) {
            playersAmmount = 0;
            if (!data.getKeyPlayer().isOnline(true)) {
                this.deleteTeam(data);
                continue;
            }
            if (!this.checkPlayer(data.getKeyPlayer())) {
                this.deleteTeam(data);
                continue;
            }
            if (!this.requireParty()) continue;
            if (data.getParty() == null) {
                this.deleteTeam(data);
                continue;
            }
            if (data.getParty().getMemberCount() > this.getDefaultPartySizeToJoin()) {
                data.message(LanguageEngine.getMsg("unregistering_unregistered_partyBig", this.getEventType().getHtmlTitle()), false);
                this.deleteTeam(data);
                continue;
            }
            for (PlayerEventInfo pi : data.getPlayers()) {
                if (!pi.isOnline(true)) {
                    data.getPlayers().remove((Object)pi);
                    continue;
                }
                if (!(pi.isRegistered() && pi.getRegisteredMiniEvent().getEventType() == this.getEventType())) {
                    data.getPlayers().remove((Object)pi);
                    data.getKeyPlayer().sendMessage(LanguageEngine.getMsg("unregistering_memberKicked_anotherEvent", pi.getPlayersName()));
                    continue;
                }
                if (pi.getParty() == null || pi.getParty().getLeadersId() != data.getParty().getLeadersId()) {
                    data.getPlayers().remove((Object)pi);
                    data.getKeyPlayer().sendMessage(LanguageEngine.getMsg("unregistering_memberKicked_leftParty", pi.getPlayersName()));
                    continue;
                }
                if (!this.checkPlayer(pi)) {
                    data.getPlayers().remove((Object)pi);
                    data.getKeyPlayer().sendMessage(LanguageEngine.getMsg("unregistering_memberKicked", pi.getPlayersName()));
                }
                ++playersAmmount;
            }
            if (playersAmmount >= this.getDefaultPartySizeToJoin() / 2) continue;
            this.deleteTeam(data);
        }
    }

    private boolean checkPlayer(PlayerEventInfo pi) {
        if (!EventManager.getInstance().canRegister(pi)) {
            pi.sendMessage(LanguageEngine.getMsg("unregistering_unregistered"));
            return false;
        }
        if (!this._mode.checkPlayer(pi)) {
            pi.sendMessage(LanguageEngine.getMsg("unregistering_unregistered"));
            return false;
        }
        if (!this.allowHealers() && pi.isPriest()) {
            pi.sendMessage(LanguageEngine.getMsg("unregistering_memberKicked"));
            return false;
        }
        return true;
    }

    protected boolean strenghtChecks(RegistrationData t1, RegistrationData t2) {
        for (AbstractFeature feature : this.getMode().getFeatures()) {
            if (feature.getType() != EventMode.FeatureType.StrenghtChecks) continue;
            return ((StrenghtChecksFeature)feature).canFight(t1, t2);
        }
        if (Math.abs(t1.getAverageLevel() - t2.getAverageLevel()) > this.getMaxLevelDifference()) {
            return false;
        }
        return true;
    }

    protected boolean ipChecks(RegistrationData p1, RegistrationData p2) {
        if (this.getBoolean("dualboxCheckForEnemies")) {
            for (PlayerEventInfo player : p1.getPlayers()) {
                String ip1;
                if (player == null || !player.isOnline() || player.isGM() || (ip1 = player.getIp()) == null) continue;
                for (PlayerEventInfo player2 : p2.getPlayers()) {
                    String ip2;
                    if (player2 == null || !player2.isOnline() || player2.isGM() || (ip2 = player2.getIp()) == null || !ip1.equals(ip2)) continue;
                    if (p1.getPlayers().size() > 1) {
                        p1.message("Player " + player.getPlayersName() + " has the same IP as someone in " + p2.getKeyPlayer().getPlayersName() + "'s team.", false);
                        p2.message("Player " + player2.getPlayersName() + " has the same IP as someone in " + p1.getKeyPlayer().getPlayersName() + "'s team.", false);
                    } else {
                        p1.message("Your IP appears to be same as " + p2.getKeyPlayer().getPlayersName() + "'s IP. You can't go against him.", false);
                        p2.message("Your IP appears to be same as " + p1.getKeyPlayer().getPlayersName() + "'s IP. You can't go against him.", false);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean ipChecks2(PlayerEventInfo player) {
        int i = this.getInt("maxPlayersPerIp");
        if (i == -1 || player.isGM()) {
            return true;
        }
        if (!player.isOnline(true)) {
            return false;
        }
        int occurences = 0;
        String ip1 = player.getIp();
        if (ip1 == null) {
            return false;
        }
        for (RegistrationData data : this._parties) {
            for (PlayerEventInfo p : data.getPlayers()) {
                if (p == null || !p.isOnline() || !ip1.equals(p.getIp())) continue;
                ++occurences;
            }
        }
        if (occurences >= i) {
            player.sendMessage("There is already " + i + " players using your IP. You may not register. Try it again later.");
            return false;
        }
        return true;
    }

    public void logPlayer(PlayerEventInfo pi, int position) {
        long time = System.currentTimeMillis();
        int rejoin = this.getJoinTimeRestriction() / 60000;
        if (position > 1) {
            time-=(long)(this.getJoinTimeRestriction() / position);
            rejoin/=position;
        }
        this._loggedPlayers.put(pi.getPlayersId(), time);
        if (pi.isOnline()) {
            pi.sendMessage(LanguageEngine.getMsg("game_delayMsg", rejoin));
        }
    }

    public void notifyDisconnect(PlayerEventInfo player) {
    }

    public EventMode getMode() {
        return this._mode;
    }

    public boolean isTemporaryLocked() {
        return this._locked;
    }

    public void setIsTemporaryLocked(boolean b) {
        this._locked = b;
    }

    public final void notifyGameEnd(MiniEventGame game) {
        this._games.remove(game);
    }

    public String getString(String propName) {
        if (this._configs.containsKey((Object)propName)) {
            String value = ((ConfigModel)this._configs.get((Object)propName)).getValue();
            return value;
        }
        this.debug("Wrong String config for event " + this.getEventType().getAltTitle() + ", name " + propName);
        return "";
    }

    public int getInt(String propName) {
        if (this._configs.containsKey((Object)propName)) {
            int value = ((ConfigModel)this._configs.get((Object)propName)).getValueInt();
            return value;
        }
        this.debug("Wrong int config for event " + this.getEventType().getAltTitle() + ", name " + propName);
        return 0;
    }

    public boolean getBoolean(String propName) {
        if (this._configs.containsKey((Object)propName)) {
            return ((ConfigModel)this._configs.get((Object)propName)).getValueBoolean();
        }
        this.debug("Wrong boolean config for event " + this.getEventType().getAltTitle() + ", name " + propName);
        return false;
    }

    protected void addConfig(ConfigModel model) {
        this._configs.put((Object)model.getKey(), (Object)model);
    }

    protected void removeConfig(String key) {
        this._configs.remove((Object)key);
    }

    protected void addConfig(String category, ConfigModel model) {
        if (!this._configCategories.contains(this._configCategories)) {
            this._configCategories.add((Object)category);
        }
        this._configs.put((Object)model.getKey(), (Object)model.setCategory(category));
    }

    protected void addMapConfig(ConfigModel model) {
        this._mapConfigs.put((Object)model.getKey(), (Object)model);
    }

    protected void removeMapConfigs() {
        this._mapConfigs.clear();
    }

    protected void removeConfigs() {
        this._configCategories.clear();
        this._configs.clear();
    }

    @Override
    public Map<String, ConfigModel> getConfigs() {
        return this._configs;
    }

    @Override
    public void clearConfigs() {
        this.removeConfigs();
        this.removeMapConfigs();
    }

    @Override
    public FastList<String> getCategories() {
        return this._configCategories;
    }

    @Override
    public void setConfig(String key, String value, boolean addToValue) {
        if (!this._configs.containsKey((Object)key)) {
            return;
        }
        if (!addToValue) {
            ((ConfigModel)this._configs.get((Object)key)).setValue(value);
        } else {
            ((ConfigModel)this._configs.get((Object)key)).addToValue(value);
        }
    }

    @Override
    public Map<String, ConfigModel> getMapConfigs() {
        return this._mapConfigs;
    }

    public boolean canRun() {
        return this._canRun;
    }

    @Override
    public boolean canRun(EventMap map) {
        return this.getMissingSpawns(map).length() == 0;
    }

    protected String addMissingSpawn(SpawnType type, int team, int count) {
        return "<font color=bfbfbf>" + this.getMode().getModeName() + " </font><font color=696969>mode</font> -> <font color=9f9f9f>No</font> <font color=B46F6B>" + type.toString().toUpperCase() + "</font> <font color=9f9f9f>spawn for team " + team + " " + (team == 0 ? "(team doesn't matter)" : "") + " count " + count + " (or more)</font><br1>";
    }

    public String getMapConfig(EventMap map, String name) {
        return EventConfig.getInstance().getMapConfig(map, this.getEventType(), name);
    }

    public int getMapConfigInt(EventMap map, String name) {
        return EventConfig.getInstance().getMapConfigInt(map, this.getEventType(), name);
    }

    public boolean getMapConfigBoolean(EventMap map, String name) {
        return EventConfig.getInstance().getMapConfigBoolean(map, this.getEventType(), name);
    }

    protected int getMaxLevelDifference() {
        return this.getInt("MaxLevelDifference");
    }

    @Override
    public String getDescriptionForReward(RewardPosition reward) {
        return null;
    }

    public boolean isTournamentActive() {
        return this._tournamentActive;
    }

    public void setTournamentActive(boolean b) {
        this._tournamentActive = b;
    }

    public abstract String getHtmlDescription();

    public List<MiniEventGame> getActiveGames() {
        return this._games;
    }

    public int getRegisteredTeamsCount() {
        if (this._parties == null) {
            return 0;
        }
        return this._parties.size();
    }

    public List<RegistrationData> getRegistered() {
        return this._parties;
    }

    @Override
    public String getEventName() {
        return this.getEventType().getAltTitle();
    }

    public boolean requireParty() {
        return true;
    }

    public boolean allowTournament() {
        return true;
    }

    public int getMaxGamesCount() {
        return 99;
    }

    protected boolean allowHealers() {
        return this.getBoolean("allowHealers");
    }

	/**
	 * @return the _compareByLevel
	 */
	public Comparator<RegistrationData> get_compareByLevel()
	{
		return _compareByLevel;
	}

	/**
	 * @param _compareByLevel the _compareByLevel to set
	 */
	public void set_compareByLevel(Comparator<RegistrationData> _compareByLevel)
	{
		this._compareByLevel = _compareByLevel;
	}

}

