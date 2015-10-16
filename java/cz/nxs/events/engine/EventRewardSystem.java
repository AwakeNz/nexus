/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.ItemData
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javolution.util.FastList;
import javolution.util.FastMap;

public class EventRewardSystem {
    private Map<EventType, FastMap<Integer, EventRewards>> _rewards = new FastMap();
    private int count = 0;
    private int notEnoughtScore = 0;

    public EventRewardSystem() {
        for (EventType t : EventType.values()) {
            this._rewards.put(t, (FastMap)new FastMap());
        }
        this.loadRewards();
    }

    private EventType getType(String s) {
        for (EventType t : EventType.values()) {
            if (!t.getAltTitle().equalsIgnoreCase(s)) continue;
            return t;
        }
        return null;
    }

    public EventRewards getAllRewardsFor(EventType event, int modeId) {
        if (this._rewards.get((Object)event).get((Object)modeId) == null) {
            this._rewards.get((Object)event).put((Object)modeId, (Object)new EventRewards());
        }
        EventRewards er = (EventRewards)this._rewards.get((Object)event).get((Object)modeId);
        return er;
    }

    public void loadRewards() {
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT eventType, modeId, position, parameter, item_id, min, max, chance FROM nexus_rewards");
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                EventRewards rewards = null;
                EventType type = this.getType(rset.getString("eventType"));
                int modeId = rset.getInt("modeId");
                if (!this._rewards.get((Object)type).containsKey((Object)modeId)) {
                    rewards = new EventRewards();
                    this._rewards.get((Object)type).put((Object)modeId, (Object)rewards);
                } else {
                    rewards = (EventRewards)this._rewards.get((Object)type).get((Object)modeId);
                }
                rewards.addItem(RewardPosition.getPosition(rset.getString("position")), rset.getString("parameter"), rset.getInt("item_id"), rset.getInt("min"), rset.getInt("max"), rset.getInt("chance"));
            }
            rset.close();
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {}
        }
        NexusLoader.debug((String)"Nexus Engine: Reward System Loaded.");
    }

    public int addRewardToDb(EventType type, RewardPosition position, String parameter, int modeId, int id, int minAmmount, int maxAmmount, int chance, boolean updateOnly) {
        if (this._rewards.get((Object)type).get((Object)modeId) == null) {
            this._rewards.get((Object)type).put((Object)modeId, (Object)new EventRewards());
        }
        EventRewards rewards = (EventRewards)this._rewards.get((Object)type).get((Object)modeId);
        int newId = 0;
        if (!updateOnly) {
            newId = rewards.addItem(position, parameter, id, minAmmount, maxAmmount, chance);
        }
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_rewards VALUES (?,?,?,?,?,?,?,?)");
            statement.setString(1, type.getAltTitle());
            statement.setInt(2, modeId);
            statement.setString(3, position.toString());
            statement.setString(4, parameter == null ? "" : parameter);
            statement.setInt(5, id);
            statement.setInt(6, minAmmount);
            statement.setInt(7, maxAmmount);
            statement.setInt(8, chance);
            statement.execute();
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {}
        }
        return newId;
    }

    public int createReward(EventType type, RewardPosition position, String parameter, int modeId) {
        return this.addRewardToDb(type, position, parameter, modeId, 57, 1, 1, 100, false);
    }

    public boolean setPositionRewarded(EventType type, int modeId, RewardPosition position, String parameter) {
        if (this._rewards.get((Object)type).get((Object)modeId) == null) {
            return false;
        }
        if (((EventRewards)this._rewards.get((Object)type).get((Object)modeId)).getContainer(position, parameter) != null) {
            return false;
        }
        ((EventRewards)this._rewards.get((Object)type).get((Object)modeId)).getOrCreateContainer(position, parameter);
        return true;
    }

    public boolean removePositionRewarded(EventType type, int modeId, RewardPosition position, String parameter) {
        if (this._rewards.get((Object)type).get((Object)modeId) == null) {
            return false;
        }
        if (((EventRewards)this._rewards.get((Object)type).get((Object)modeId)).getContainer(position, parameter) == null) {
            return false;
        }
        PositionContainer container = ((EventRewards)this._rewards.get((Object)type).get((Object)modeId)).getContainer(position, parameter);
        Map<Integer, RewardItem> map = ((EventRewards)this._rewards.get((Object)type).get((Object)modeId)).getAllRewards().get(container);
        for (Map.Entry<Integer, RewardItem> e : map.entrySet()) {
            this.removeRewardFromDb(type, e.getKey(), modeId);
        }
        ((EventRewards)this._rewards.get((Object)type).get((Object)modeId)).getAllRewards().remove(container);
        return true;
    }

    public void updateRewardInDb(EventType type, int rewardId, int modeId) {
        RewardItem item;
        EventRewards rewards;
        if (this._rewards.get((Object)type).get((Object)modeId) == null) {
            this._rewards.get((Object)type).put((Object)modeId, (Object)new EventRewards());
        }
        if ((item = (rewards = (EventRewards)this._rewards.get((Object)type).get((Object)modeId)).getItem(rewardId)) == null) {
            return;
        }
        PositionContainer position = this.getRewardPosition(type, modeId, rewardId);
        this.addRewardToDb(type, position.position, position.parameter, modeId, item.id, item.minAmmount, item.maxAmmount, item.chance, true);
    }

    public void removeFromDb(EventType type, RewardPosition position, String parameter, int modeId, int itemId, int min, int max, int chance) {
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_rewards WHERE eventType = '" + type.getAltTitle() + "' AND position = '" + position.toString() + "' AND parameter = '" + (parameter == null ? "" : parameter) + "' AND modeId = " + modeId + " AND item_id = " + itemId + " AND min = " + min + " AND max = " + max + " AND chance = " + chance);
            statement.execute();
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {}
        }
    }

    public void removeRewardFromDb(EventType type, int rewardId, int modeId) {
        PositionContainer container = this.getRewardPosition(type, modeId, rewardId);
        if (this._rewards.get((Object)type).get((Object)modeId) == null) {
            this._rewards.get((Object)type).put((Object)modeId, (Object)new EventRewards());
        }
        EventRewards rewards = (EventRewards)this._rewards.get((Object)type).get((Object)modeId);
        RewardItem item = rewards.getItem(rewardId);
        rewards.removeItem(container.position, container.parameter, rewardId);
        this.removeFromDb(type, container.position, container.parameter, modeId, item.id, item.minAmmount, item.maxAmmount, item.chance);
    }

    public Map<Integer, RewardItem> getRewards(EventType type, int modeId, RewardPosition position, String parameter) {
        Map<Integer, RewardItem> map;
        if (this._rewards.get((Object)type).get((Object)modeId) == null) {
            this._rewards.get((Object)type).put((Object)modeId, (Object)new EventRewards());
        }
        if ((map = ((EventRewards)this._rewards.get((Object)type).get((Object)modeId)).getRewards(position, parameter)) != null) {
            return map;
        }
        return new FastMap();
    }

    public RewardItem getReward(EventType type, int modeId, int rewardId) {
        if (this._rewards.get((Object)type).get((Object)modeId) == null) {
            this._rewards.get((Object)type).put((Object)modeId, (Object)new EventRewards());
        }
        return ((EventRewards)this._rewards.get((Object)type).get((Object)modeId)).getItem(rewardId);
    }

    public PositionContainer getRewardPosition(EventType type, int modeId, int rewardId) {
        if (this._rewards.get((Object)type).get((Object)modeId) == null) {
            this._rewards.get((Object)type).put((Object)modeId, (Object)new EventRewards());
        }
        for (Map.Entry e : ((EventRewards)this._rewards.get((Object)type).get((Object)modeId))._rewards.entrySet()) {
            Iterator i$ = ((Map)e.getValue()).keySet().iterator();
            while (i$.hasNext()) {
                int i = (Integer)i$.next();
                if (i != rewardId) continue;
                return (PositionContainer)e.getKey();
            }
        }
        return new PositionContainer(RewardPosition.None, null);
    }

    public Map<Integer, List<EventTeam>> rewardTeams(Map<EventTeam, Integer> teams, EventType event, int modeId, int minScore, int halfRewardAfkTime, int noRewardAfkTime) {
        int score;
        this.count = 0;
        this.notEnoughtScore = 0;
        int totalCount = teams.size();
        FastMap scores = new FastMap();
        for (Map.Entry<EventTeam, Integer> e : teams.entrySet()) {
            EventTeam team = e.getKey();
            score = e.getValue();
            if (!scores.containsKey(score)) {
                scores.put(score, new FastList());
            }
            ((List)scores.get(score)).add(team);
        }
        int position = 1;
        for (Map.Entry e2 : scores.entrySet()) {
            PositionContainer temp;
            score = (Integer)e2.getKey();
            int count = ((List)e2.getValue()).size();
            if (position == 1) {
                if (count == 1) {
                    temp = this.existsReward(event, modeId, RewardPosition.Numbered, "1");
                    if (temp != null) {
                        this.giveRewardsToTeams(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                    } else {
                        temp = this.existsRangeReward(event, modeId, position);
                        if (temp != null) {
                            this.giveRewardsToTeams(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                        } else {
                            temp = this.existsReward(event, modeId, RewardPosition.Winner, null);
                            if (temp != null) {
                                this.giveRewardsToTeams(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                            }
                        }
                    }
                } else if (totalCount > count) {
                    temp = this.existsReward(event, modeId, RewardPosition.Numbered, "1");
                    if (temp != null) {
                        this.giveRewardsToTeams(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                    } else {
                        temp = this.existsRangeReward(event, modeId, position);
                        if (temp != null) {
                            this.giveRewardsToTeams(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                        } else {
                            temp = this.existsReward(event, modeId, RewardPosition.Winner, null);
                            if (temp != null) {
                                this.giveRewardsToTeams(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                            }
                        }
                    }
                } else {
                    temp = this.existsReward(event, modeId, RewardPosition.Tie, null);
                    if (temp != null) {
                        this.giveRewardsToTeams(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                    }
                }
            } else {
                temp = this.existsReward(event, modeId, RewardPosition.Numbered, String.valueOf(position));
                if (temp != null) {
                    this.giveRewardsToTeams(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                } else {
                    temp = this.existsRangeReward(event, modeId, position);
                    if (temp != null) {
                        this.giveRewardsToTeams(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                    } else {
                        temp = this.existsReward(event, modeId, RewardPosition.Looser, null);
                        if (temp != null) {
                            this.giveRewardsToTeams(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                        }
                    }
                }
            }
            ++position;
        }
        try {
            AbstractMainEvent ev;
            if (event.isRegularEvent() && (ev = EventManager.getInstance().getMainEvent(event)) != null) {
                this.dump(ev.getPlayers(0).size());
            }
        }
        catch (Exception e2) {
            // empty catch block
        }
        return scores;
    }

    private void dump(int total) {
        NexusLoader.debug((String)("" + total + " was the count of players in the event."));
        NexusLoader.debug((String)("" + this.count + " players were rewarded."));
        NexusLoader.debug((String)("" + this.notEnoughtScore + " players were not rewarded because they didn't have enought score."));
        if (NexusLoader.detailedDebug) {
            NexusLoader.detailedDebug((String)("" + total + " was the count of players in the event."));
        }
        if (NexusLoader.detailedDebug) {
            NexusLoader.detailedDebug((String)("" + this.count + " players were rewarded."));
        }
        if (NexusLoader.detailedDebug) {
            NexusLoader.detailedDebug((String)("" + this.notEnoughtScore + " players were not rewarded because they didn't have enought score."));
        }
    }

    public Map<Integer, List<PlayerEventInfo>> rewardPlayers(Map<PlayerEventInfo, Integer> players, EventType event, int modeId, int minScore, int halfRewardAfkTime, int noRewardAfkTime) {
        int score;
        this.count = 0;
        this.notEnoughtScore = 0;
        int totalCount = players.size();
        FastMap scores = new FastMap();
        for (Map.Entry<PlayerEventInfo, Integer> e : players.entrySet()) {
            PlayerEventInfo player = e.getKey();
            score = e.getValue();
            if (!scores.containsKey(score)) {
                scores.put(score, new FastList());
            }
            ((List)scores.get(score)).add(player);
        }
        int position = 1;
        for (Map.Entry e2 : scores.entrySet()) {
            PositionContainer temp;
            score = (Integer)e2.getKey();
            int count = ((List)e2.getValue()).size();
            if (position == 1) {
                if (count == 1) {
                    temp = this.existsReward(event, modeId, RewardPosition.Numbered, "1");
                    if (temp != null) {
                        this.giveRewardsToPlayers(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                    } else {
                        temp = this.existsRangeReward(event, modeId, position);
                        if (temp != null) {
                            this.giveRewardsToPlayers(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                        } else {
                            temp = this.existsReward(event, modeId, RewardPosition.Winner, null);
                            if (temp != null) {
                                this.giveRewardsToPlayers(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                            }
                        }
                    }
                } else if (totalCount > count) {
                    temp = this.existsReward(event, modeId, RewardPosition.Numbered, "1");
                    if (temp != null) {
                        this.giveRewardsToPlayers(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                    } else {
                        temp = this.existsRangeReward(event, modeId, position);
                        if (temp != null) {
                            this.giveRewardsToPlayers(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                        } else {
                            temp = this.existsReward(event, modeId, RewardPosition.Winner, null);
                            if (temp != null) {
                                this.giveRewardsToPlayers(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                            }
                        }
                    }
                } else {
                    temp = this.existsReward(event, modeId, RewardPosition.Tie, null);
                    if (temp != null) {
                        this.giveRewardsToPlayers(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                    }
                }
            } else {
                temp = this.existsReward(event, modeId, RewardPosition.Numbered, String.valueOf(position));
                if (temp != null) {
                    this.giveRewardsToPlayers(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                } else {
                    temp = this.existsRangeReward(event, modeId, position);
                    if (temp != null) {
                        this.giveRewardsToPlayers(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                    } else {
                        temp = this.existsReward(event, modeId, RewardPosition.Looser, null);
                        if (temp != null) {
                            this.giveRewardsToPlayers(temp, (List)e2.getValue(), event, modeId, minScore, halfRewardAfkTime, noRewardAfkTime);
                        }
                    }
                }
            }
            ++position;
        }
        return scores;
    }

    private void giveRewardsToPlayers(PositionContainer container, List<PlayerEventInfo> players, EventType event, int modeId, int minScore, int halfRewardAfkTime, int noRewardAfkTime) {
        for (PlayerEventInfo player : players) {
            if (player.isOnline()) {
                if (player.getEventData().getScore() >= minScore) {
                    ++this.count;
                    this.rewardPlayer(event, modeId, player, container.position, container.parameter, player.getTotalTimeAfk(), halfRewardAfkTime, noRewardAfkTime);
                    continue;
                }
                ++this.notEnoughtScore;
                if (minScore <= 0 || player.getScore() >= minScore) continue;
                player.sendMessage(LanguageEngine.getMsg("event_notEnoughtScore", minScore));
                continue;
            }
            NexusLoader.debug((String)("trying to reward player " + player.getPlayersName() + " (player) which is not online()"), (Level)Level.WARNING);
        }
    }

    private void giveRewardsToTeams(PositionContainer container, List<EventTeam> teams, EventType event, int modeId, int minScore, int halfRewardAfkTime, int noRewardAfkTime) {
        for (EventTeam team : teams) {
            for (PlayerEventInfo player : team.getPlayers()) {
                if (player.isOnline()) {
                    if (player.getEventData().getScore() >= minScore) {
                        ++this.count;
                        this.rewardPlayer(event, modeId, player, container.position, container.parameter, player.getTotalTimeAfk(), halfRewardAfkTime, noRewardAfkTime);
                        continue;
                    }
                    ++this.notEnoughtScore;
                    if (minScore <= 0 || player.getScore() >= minScore) continue;
                    player.sendMessage(LanguageEngine.getMsg("event_notEnoughtScore", minScore));
                    continue;
                }
                NexusLoader.debug((String)("trying to reward player " + player.getPlayersName() + " (team) which is not online()"), (Level)Level.WARNING);
            }
        }
    }

    private PositionContainer existsReward(EventType event, int modeId, RewardPosition pos, String parameter) {
        if (this._rewards.get((Object)event).get((Object)modeId) == null) {
            return null;
        }
        PositionContainer c = ((EventRewards)this._rewards.get((Object)event).get((Object)modeId)).getContainer(pos, parameter);
        if (c == null || ((EventRewards)this._rewards.get((Object)event).get((Object)modeId)).getAllRewards().get(c).isEmpty()) {
            return null;
        }
        return c;
    }

    private PositionContainer existsRangeReward(EventType event, int modeId, int position) {
        if (this._rewards.get((Object)event).get((Object)modeId) == null) {
            return null;
        }
        for (Map.Entry<PositionContainer, Map<Integer, RewardItem>> e : ((EventRewards)this._rewards.get((Object)event).get((Object)modeId)).getAllRewards().entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty() || e.getKey().position.posType == null || e.getKey().position.posType != RewardPosition.PositionType.Range) continue;
            int from = Integer.parseInt(e.getKey().parameter.split("-")[0]);
            int to = Integer.parseInt(e.getKey().parameter.split("-")[1]);
            if (position < from || position > to) continue;
            return e.getKey();
        }
        return null;
    }

    public boolean rewardPlayer(EventType event, int modeId, PlayerEventInfo player, RewardPosition position, String parameter, int afkTime, int halfRewardAfkTime, int noRewardAfkTime) {
        if (player == null) {
            return false;
        }
        if (this._rewards.get((Object)event).get((Object)modeId) == null) {
            this._rewards.get((Object)event).put((Object)modeId, (Object)new EventRewards());
        }
        if (((EventRewards)this._rewards.get((Object)event).get((Object)modeId)).getRewards(position, parameter) == null) {
            return false;
        }
        if (noRewardAfkTime > 0 && afkTime >= noRewardAfkTime) {
            player.sendMessage("You receive no reward because you were afk too much.");
            return false;
        }
        if (halfRewardAfkTime > 0 && afkTime >= halfRewardAfkTime) {
            player.sendMessage("You receive half reward because you were afk too much.");
        }
        boolean given = false;
        for (RewardItem item : ((EventRewards)this._rewards.get((Object)event).get((Object)modeId)).getRewards(position, parameter).values()) {
            int ammount = item.getAmmount(player);
            if (ammount <= 0) continue;
            if (ammount > 1 && halfRewardAfkTime > 0 && afkTime >= halfRewardAfkTime) {
                ammount/=2;
            }
            if (item.id == -1) {
                player.addExpAndSp((long)ammount, 0);
            } else if (item.id == -2) {
                player.addExpAndSp(0, ammount);
            } else if (item.id == -3) {
                player.setFame(player.getFame() + ammount);
            } else {
                player.addItem(item.id, ammount, true);
            }
            given = true;
        }
        return given;
    }

    public static final EventRewardSystem getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final EventRewardSystem _instance = new EventRewardSystem();

        private SingletonHolder() {
        }
    }

    public class RewardItem {
        public int id;
        public int minAmmount;
        public int maxAmmount;
        public int chance;
        public int pvpRequired;
        public int levelRequired;

        public RewardItem(int id, int minAmmount, int maxAmmount, int chance, int pvpRequired, int levelRequired) {
            this.id = id;
            this.minAmmount = minAmmount;
            this.maxAmmount = maxAmmount;
            this.chance = chance;
            this.pvpRequired = pvpRequired;
            this.levelRequired = levelRequired;
        }

        public int getAmmount(PlayerEventInfo player) {
            if (CallBack.getInstance().getOut().random(100) < this.chance) {
                return CallBack.getInstance().getOut().random(this.minAmmount, this.maxAmmount);
            }
            NexusLoader.debug((String)("chance check for reward failed for player " + player.getPlayersName() + ", reward item " + this.id));
            return 0;
        }
    }

    public class EventRewards {
        private int _lastId;
        private Map<PositionContainer, Map<Integer, RewardItem>> _rewards;

        public EventRewards() {
            this._lastId = 0;
            this._rewards = new FastMap();
        }

        public PositionContainer getOrCreateContainer(RewardPosition position, String posParameter) {
            PositionContainer container = null;
            container = this.getContainer(position, posParameter);
            if (container == null) {
                container = new PositionContainer(position, posParameter);
            }
            if (!this._rewards.containsKey(container)) {
                this._rewards.put(container, (Map<Integer, RewardItem>)new FastMap());
            }
            return container;
        }

        public int addItem(RewardPosition position, String posParameter, int id, int minAmmount, int maxAmmount, int chance) {
            if (position == null) {
                NexusLoader.debug((String)("Null RewardPosition for item ID " + id + ", minAmmount " + minAmmount + " maxAmmount " + maxAmmount + " chance " + chance), (Level)Level.WARNING);
                return this._lastId++;
            }
            if ("".equals(posParameter)) {
                posParameter = null;
            }
            PositionContainer container = this.getOrCreateContainer(position, posParameter);
            ++this._lastId;
            RewardItem item = new RewardItem(id, minAmmount, maxAmmount, chance, 0, 0);
            this._rewards.get(container).put(this._lastId, item);
            return this._lastId;
        }

        public PositionContainer getContainer(RewardPosition position, String parameter) {
            for (PositionContainer ps : this._rewards.keySet()) {
                if (ps.position == null || !ps.position.toString().equals(position.toString()) || parameter != null && !parameter.equals("null") && !parameter.equals(ps.parameter)) continue;
                return ps;
            }
            return null;
        }

        public void removeItem(RewardPosition position, String parameter, int rewardId) {
            PositionContainer ps = this.getContainer(position, parameter);
            if (ps != null && this._rewards.containsKey(ps)) {
                this._rewards.get(ps).remove(rewardId);
            }
        }

        public Map<Integer, RewardItem> getRewards(RewardPosition position, String parameter) {
            PositionContainer ps = this.getContainer(position, parameter);
            if (ps != null) {
                return this._rewards.get(ps);
            }
            return null;
        }

        public Map<PositionContainer, Map<Integer, RewardItem>> getAllRewards() {
            return this._rewards;
        }

        public RewardItem getItem(int rewardId) {
            for (Map<Integer, RewardItem> i : this._rewards.values()) {
                for (Map.Entry<Integer, RewardItem> e : i.entrySet()) {
                    if (e.getKey() != rewardId) continue;
                    return e.getValue();
                }
            }
            return null;
        }
    }

    public class PositionContainer {
        public RewardPosition position;
        public String parameter;
        public boolean rewarded;

        PositionContainer(RewardPosition position, String parameter) {
            this.position = position;
            this.parameter = parameter;
        }

        public void setRewarded(boolean b) {
            this.rewarded = b;
        }

        public boolean isRewarded() {
            return this.rewarded;
        }
    }

}

