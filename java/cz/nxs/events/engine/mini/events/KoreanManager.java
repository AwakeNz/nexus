/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javolution.text.TextBuilder
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
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.mini.RegistrationData;
import cz.nxs.events.engine.mini.events.KoreanGame;
import cz.nxs.events.engine.mini.events.PartyvsPartyManager;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.events.engine.mini.features.DelaysFeature;
import cz.nxs.events.engine.mini.features.TeamSizeFeature;
import cz.nxs.events.engine.mini.features.TimeLimitFeature;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javolution.text.TextBuilder;

public class KoreanManager
extends PartyvsPartyManager {
    private final int MAX_GAMES_COUNT = 3;
    private static RewardPosition[] _rewardTypes = new RewardPosition[]{RewardPosition.Winner, RewardPosition.Looser, RewardPosition.Tie_TimeLimit};

    public KoreanManager(EventType type) {
        super(type);
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.removeMapConfigs();
        this.addMapConfig(new ConfigModel("WaitTime", "60000", "The waiting delay during which players will be able to rebuff, organize, etc. In ms."));
        this.removeConfig("RoundsAmmount");
        this.removeConfig("TeamsAmmount");
        this.removeConfig("removeBuffsOnRespawn");
    }

    @Override
    public boolean launchGame(RegistrationData[] teams, EventMap map) {
        if (map == null) {
            map = EventMapSystem.getInstance().getNextMap(this, this._lastMapIndex, this.getMode());
        }
        if (map == null) {
            this.cleanMe(true);
            this._mode.setAllowed(false);
            _log.warning("No map aviable for event " + this.getEventType().getAltTitle() + " !!! Mode has been disabled.");
            return false;
        }
        this._lastMapIndex = EventMapSystem.getInstance().getMapIndex(this.getEventType(), map);
        this.getNextGameId();
        KoreanGame game = new KoreanGame(this._lastGameId, map, this, teams);
        new Thread((Runnable)game, this.getEventName() + " ID" + this._lastGameId).start();
        this._games.add(game);
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
    public RewardPosition[] getRewardTypes() {
        return _rewardTypes;
    }

    @Override
    public int getMaxGamesCount() {
        return 3;
    }

    @Override
    public int getTeamsCount() {
        return 2;
    }

    @Override
    public String getHtmlDescription() {
        if (this._htmlDescription == null) {
            int partySize = this.getInt("PartySize");
            int rejoinDelay = this.getInt("DelayToWaitSinceLastMatchMs");
            int timeLimit = this.getInt("TimeLimitMs");
            for (AbstractFeature feature : this.getMode().getFeatures()) {
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
                this._htmlDescription = desc.getDescription(this.getConfigs(), 1, 2, partySize, rejoinDelay, timeLimit);
            } else {
                this._htmlDescription = "This is a team-based mini event. You need a party of exactly " + partySize + " players (and be the party leader) to register. ";
                this._htmlDescription = this._htmlDescription + "You will fight against one enemy party in a randomly chosen map. ";
                this._htmlDescription = this._htmlDescription + "The fight is in the famous Korean-style - it's a set of continous 1v1 fights. If you die, you will be replaced by someone from your party. ";
                this._htmlDescription = this._htmlDescription + "The match ends when all players from one party are dead. ";
                this._htmlDescription = this._htmlDescription + "Your opponent will be selected automatically and don't worry, there's a protection, which will ensure that you will always fight only players whose level is similar to yours. ";
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
            if (!map.checkForSpawns(SpawnType.Regular, i + 1, 1)) {
                tb.append(this.addMissingSpawn(SpawnType.Regular, i + 1, 1));
            }
            if (map.checkForSpawns(SpawnType.Safe, i + 1, 1)) continue;
            tb.append(this.addMissingSpawn(SpawnType.Safe, i + 1, 1));
        }
        return tb.toString();
    }
}

