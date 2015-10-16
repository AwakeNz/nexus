/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.PartyData
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.mini.events;

import cz.nxs.events.engine.EventMapSystem;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.base.description.EventDescription;
import cz.nxs.events.engine.base.description.EventDescriptionSystem;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.mini.RegistrationData;
import cz.nxs.events.engine.mini.events.PartyvsPartyGame;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.events.engine.mini.features.DelaysFeature;
import cz.nxs.events.engine.mini.features.RoundsFeature;
import cz.nxs.events.engine.mini.features.TeamSizeFeature;
import cz.nxs.events.engine.mini.features.TeamsAmmountFeature;
import cz.nxs.events.engine.mini.features.TimeLimitFeature;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.IPlayerBase;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class PartyvsPartyManager
extends MiniEventManager {
    protected int _lastMapIndex = 0;
    private FastList<RegistrationData> tempTeams = new FastList();
    private final int MAX_GAMES_COUNT = 3;
    private Map<SpawnType, String> _spawnTypes = new FastMap();
    private static RewardPosition[] _rewardTypes = new RewardPosition[]{RewardPosition.Winner, RewardPosition.Looser, RewardPosition.Tie_TimeLimit, RewardPosition.Tie};

    public PartyvsPartyManager(EventType type) {
        super(type);
        this._spawnTypes.put(SpawnType.Regular, "Defines where the players will be spawned.");
        this._spawnTypes.put(SpawnType.Buffer, "Defines where the buffer(s) will be spawned.");
        this._spawnTypes.put(SpawnType.Fence, "Defines where fences will be spawned.");
        this.check();
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.addConfig(new ConfigModel("PartySize", "6", "The exact size of registered party. If the party has lower or higher # of players, it won't be able to join."));
        this.addConfig(new ConfigModel("TeamsAmmount", "2", "The count of teams (parties) fighting in the event."));
        this.addConfig(new ConfigModel("RoundsAmmount", "3", "The count of rounds the event has."));
        this.addMapConfig(new ConfigModel("FirstRoundWaitDelay", "30000", "The delay the players has to wait when he is teleported to the map (first round). During this time, he will be preparing himself for the fight and getting buffs. In miliseconds."));
        this.addMapConfig(new ConfigModel("RoundWaitDelay", "20000", "The waiting delay for players to prepare for the match before all rounds' (except for the first round) start. In miliseconds."));
    }

    @Override
    public void run() {
        this.check();
    }

    @Override
    public void createGame() {
        if (this._locked) {
            return;
        }
        this.removeInactiveTeams();
        if (this._games.size() >= this.getMaxGamesCount()) {
            this.check();
            return;
        }
        int iterateLimit = this._parties.size();
        int limit = 1;
        FastList tempData = new FastList();
        this.setIsTemporaryLocked(true);
        try {
            while (limit != 0) {
                if (iterateLimit != 0) {
                    this.tempTeams.clear();
                    for (RegistrationData team : this._parties) {
                        if (team.isChosen()) continue;
                        if (this.tempTeams.isEmpty()) {
                            if (tempData.contains(team)) continue;
                            this.tempTeams.add((Object)team);
                            tempData.add(team);
                        } else if (!this.tempTeams.contains((Object)team)) {
                            if (!this.strenghtChecks(team, (RegistrationData)this.tempTeams.getFirst()) || !this.ipChecks(team, (RegistrationData)this.tempTeams.getFirst())) continue;
                            this.tempTeams.add((Object)team);
                        }
                        if (this.tempTeams.size() < this.getTeamsCount()) continue;
                        for (RegistrationData d : this.tempTeams) {
                            d.setIsChosen(true);
                        }
                        this.launchGame((RegistrationData[])this.tempTeams.toArray((Object[])new RegistrationData[this.tempTeams.size()]), null);
                        --limit;
                        break;
                    }
                    --iterateLimit;
                    continue;
                } else {
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (RegistrationData p : this._parties) {
            if (!p.isChosen()) continue;
            this._parties.remove(p);
        }
        this.setIsTemporaryLocked(false);
        this.check();
    }

    @Override
    public boolean launchGame(RegistrationData[] teams, EventMap map) {
        if (map == null) {
            map = EventMapSystem.getInstance().getNextMap(this, this._lastMapIndex, this.getMode());
        }
        if (map == null) {
            this.cleanMe(true);
            this._mode.setAllowed(false);
            _log.warning("No map available for event " + this.getEventType().getAltTitle() + " !!! Mode has been disabled.");
            return false;
        }
        this._lastMapIndex = EventMapSystem.getInstance().getMapIndex(this.getEventType(), map);
        this.getNextGameId();
        PartyvsPartyGame game = new PartyvsPartyGame(this._lastGameId, map, this, teams);
        new Thread((Runnable)game, this.getEventName() + " ID" + this._lastGameId).start();
        this._games.add(game);
        return true;
    }

    @Override
    public boolean registerTeam(PlayerEventInfo player) {
        if (!super.registerTeam(player)) {
            return false;
        }
        FastList partyPlayers = new FastList();
        for (PlayerEventInfo p : player.getParty().getPartyMembers()) {
            if (p == null) continue;
            CallBack.getInstance().getPlayerBase().addInfo(p);
            partyPlayers.add((Object)p);
        }
        RegistrationData regData = new RegistrationData(partyPlayers);
        regData.register(true, this);
        regData.message(LanguageEngine.getMsg("registering_registered2", this.getEventName()), true);
        this.addParty(regData);
        return true;
    }

    @Override
    public synchronized boolean unregisterTeam(PlayerEventInfo player) {
        if (!super.unregisterTeam(player)) {
            return false;
        }
        for (RegistrationData t : this._parties) {
            if (t.getKeyPlayer().getPlayersId() != player.getPlayersId()) continue;
            this.deleteTeam(t);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkCanFight(PlayerEventInfo gm, RegistrationData[] teams) {
        if (teams.length != 2) {
            gm.sendMessage("2 teams are required.");
            return false;
        }
        if (teams[0].getPlayers().size() < this.getDefaultPartySizeToJoin() / 2 || teams[1].getPlayers().size() < this.getDefaultPartySizeToJoin() / 2) {
            gm.sendMessage("Not enought players in one of the teams, minimal # of players registered is " + this.getDefaultPartySizeToJoin() / 2 + ".");
            return false;
        }
        return true;
    }

    @Override
    protected int getStartGameInterval() {
        return 30000;
    }

    @Override
    public int getDefaultPartySizeToJoin() {
        for (AbstractFeature feature : this.getMode().getFeatures()) {
            if (feature.getType() != EventMode.FeatureType.TeamSize || ((TeamSizeFeature)feature).getTeamSize() <= 0) continue;
            return ((TeamSizeFeature)feature).getTeamSize();
        }
        return this.getInt("PartySize");
    }

    @Override
    public boolean requireParty() {
        return true;
    }

    @Override
    public int getMaxGamesCount() {
        return 3;
    }

    @Override
    public RewardPosition[] getRewardTypes() {
        return _rewardTypes;
    }

    @Override
    public Map<SpawnType, String> getAviableSpawnTypes() {
        return this._spawnTypes;
    }

    @Override
    public int getTeamsCount() {
        for (AbstractFeature feature : this.getMode().getFeatures()) {
            if (feature.getType() != EventMode.FeatureType.TeamsAmmount || ((TeamsAmmountFeature)feature).getTeamsAmmount() <= 0) continue;
            return ((TeamsAmmountFeature)feature).getTeamsAmmount();
        }
        return this.getInt("TeamsAmmount");
    }

    public int getRoundsAmmount() {
        for (AbstractFeature feature : this.getMode().getFeatures()) {
            if (feature.getType() != EventMode.FeatureType.Rounds || ((RoundsFeature)feature).getRoundsAmmount() <= 0) continue;
            return ((RoundsFeature)feature).getRoundsAmmount();
        }
        return this.getInt("RoundsAmmount");
    }

    @Override
    public String getHtmlDescription() {
        if (this._htmlDescription == null) {
            int roundsCount = this.getInt("RoundsAmmount");
            int teamsCount = this.getInt("TeamsAmmount");
            int partySize = this.getInt("PartySize");
            int rejoinDelay = this.getInt("DelayToWaitSinceLastMatchMs");
            int timeLimit = this.getInt("TimeLimitMs");
            for (AbstractFeature feature : this.getMode().getFeatures()) {
                if (feature instanceof RoundsFeature) {
                    roundsCount = ((RoundsFeature)feature).getRoundsAmmount();
                    continue;
                }
                if (feature instanceof TeamsAmmountFeature) {
                    teamsCount = ((TeamsAmmountFeature)feature).getTeamsAmmount();
                    continue;
                }
                if (feature instanceof DelaysFeature) {
                    rejoinDelay = ((DelaysFeature)feature).getRejoinDealy();
                    continue;
                }
                if (feature instanceof TimeLimitFeature) {
                    timeLimit = ((TimeLimitFeature)feature).getTimeLimit();
                    continue;
                }
                if (!(feature instanceof TeamSizeFeature)) continue;
                partySize = ((TeamSizeFeature)feature).getTeamSize();
            }
            EventDescription desc = EventDescriptionSystem.getInstance().getDescription(this.getEventType());
            if (desc != null) {
                this._htmlDescription = desc.getDescription(this.getConfigs(), roundsCount, teamsCount, partySize, rejoinDelay, timeLimit);
            } else {
                this._htmlDescription = "This is a team-based mini event. You need a party of exactly " + partySize + " players (and be the party leader) to register. ";
                this._htmlDescription = this._htmlDescription + "You will fight against " + (teamsCount - 1) + " enemy part" + (teamsCount > 2 ? "ies" : "y") + " in a randomly chosen map. ";
                if (roundsCount > 1) {
                    this._htmlDescription = this._htmlDescription + "Each match has " + roundsCount + " rounds, the winner of round (the party, who kills all it's opponents) receives  1 score. ";
                    this._htmlDescription = this._htmlDescription + "The party, who has the biggest score in the end of all rounds, wins the match. ";
                } else {
                    this._htmlDescription = this._htmlDescription + "This match has only one round. If you die, you can get revived only by your party-mate. ";
                    this._htmlDescription = this._htmlDescription + "The winner of the match is the party, who kills all it's opponents. ";
                }
                this._htmlDescription = this._htmlDescription + "Your opponent(s) will be selected automatically and don't worry, there's a protection, which will ensure that you will always fight only players whose level is similar to yours. ";
                this._htmlDescription = this._htmlDescription + "If the match doesn't end within " + timeLimit / 60000 + " minutes, it will be aborted automatically. ";
                this._htmlDescription = this._htmlDescription + "Also, after you visit this event, you will have to wait at least " + rejoinDelay / 60000 + " minutes to join this event again. ";
            }
        }
        return this._htmlDescription;
    }

    @Override
    public String getMissingSpawns(EventMap map) {
        TextBuilder tb = new TextBuilder();
        for (int i = 0; i < this.getTeamsCount(); ++i) {
            if (map.checkForSpawns(SpawnType.Regular, i + 1, 1)) continue;
            tb.append(this.addMissingSpawn(SpawnType.Regular, i + 1, 1));
        }
        return tb.toString();
    }
}

