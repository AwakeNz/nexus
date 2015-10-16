/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.stats;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.stats.EventStats;
import cz.nxs.events.engine.stats.GlobalStatsModel;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class GlobalStats
extends EventStats {
    private Map<PlayerEventInfo, Map<EventType, GlobalStatsModel>> _playerGlobalStats;
    private Map<PlayerEventInfo, String> _playerGlobalStatsHtml;
    private Map<SortType, Map<Integer, String>> _globalStatsHtml;
    private Map<Integer, GlobalStatsSum> _data;
    private ScheduledFuture<?> _globalStatsReload;
    private long _lastLoad;
    public boolean _enableStatistics;
    public boolean _enableGlobalStatistics;
    private String _statsSorting;
    private boolean _ignoreBannedPlayers = true;
    private boolean _ignoreGMs = false;
    private int _playersPerPage = 8;
    private int _statsRefresh = 1800;
    private boolean _showDetailedPlayerInfo = true;
    private boolean _showPkCount = true;
    private boolean globalStatsLoaded = false;

    private void loadConfigs() {
        this._enableStatistics = EventConfig.getInstance().getGlobalConfigBoolean("enableStatistics");
        this._enableGlobalStatistics = EventConfig.getInstance().getGlobalConfigBoolean("enableGlobalStatistics");
        this._statsRefresh = EventConfig.getInstance().getGlobalConfigInt("globalStatisticsRefresh");
        this._statsSorting = EventConfig.getInstance().getGlobalConfigValue("statsSorting");
        this._ignoreBannedPlayers = EventConfig.getInstance().getGlobalConfigBoolean("statsIgnoreBanned");
        this._ignoreGMs = EventConfig.getInstance().getGlobalConfigBoolean("statsIgnoreGMs");
        this._playersPerPage = EventConfig.getInstance().getGlobalConfigInt("statsPlayersPerPage");
        this._showDetailedPlayerInfo = EventConfig.getInstance().getGlobalConfigBoolean("statsDetailedPlayerInfo");
        this._showPkCount = EventConfig.getInstance().getGlobalConfigBoolean("statsShowPkCount");
    }

    public GlobalStatsModel getPlayerGlobalStatsCopy(PlayerEventInfo player, EventType type) {
        GlobalStatsModel oldModel = this._playerGlobalStats.get((Object)player).get((Object)type);
        FastMap stats = new FastMap();
        stats.putAll(oldModel.stats);
        GlobalStatsModel newModel = new GlobalStatsModel(type, (Map<GlobalStatType, Integer>)stats);
        return newModel;
    }

    public GlobalStatsModel getPlayerGlobalStats(PlayerEventInfo player, EventType type) {
        return this._playerGlobalStats.get((Object)player).get((Object)type);
    }

    public void setPlayerGlobalStats(PlayerEventInfo player, EventType type, GlobalStatsModel stats) {
        this._playerGlobalStats.get((Object)player).put(type, stats);
    }

    @Override
    public void load() {
        this._playerGlobalStats = new FastMap();
        this._playerGlobalStatsHtml = new FastMap();
        this.loadConfigs();
        this.loadGlobalStats();
        NexusLoader.debug((String)"Global statistics engine loaded.");
    }

    @Override
    public void onLogin(PlayerEventInfo player) {
        this.loadPlayer(player);
    }

    @Override
    public void onDisconnect(PlayerEventInfo player) {
        this.forgetPlayerGlobalStats(player);
    }

    @Override
    public void statsChanged(PlayerEventInfo player) {
        this._playerGlobalStatsHtml.remove((Object)player);
    }

    @Override
    public void onCommand(PlayerEventInfo player, String command) {
        if (command.startsWith("oneplayer")) {
            PlayerEventInfo target = null;
            String name = null;
            String sortType = null;
            String page = null;
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();
            if (st.hasMoreTokens()) {
                name = st.nextToken();
            }
            if (st.hasMoreTokens()) {
                sortType = st.nextToken();
            }
            if (st.hasMoreTokens()) {
                page = st.nextToken();
            }
            boolean backToCbMenu = false;
            if (name == null) {
                target = player;
            } else if (name.equals("cbmenu")) {
                target = player;
                backToCbMenu = true;
            } else {
                target = CallBack.getInstance().getOut().getPlayer(name);
            }
            if (target != null) {
                this.showPlayersGlobalStats(player, target, sortType, page, backToCbMenu);
            } else {
                player.screenMessage("This player is either offline or doesn't exist.", "Statistics", false);
                player.sendMessage("This player is either offline or doesn't exist.");
            }
        } else if (command.startsWith("topplayers")) {
            String params = command.substring(11);
            this.showGlobalStats(player, params);
        }
    }

    private void showGlobalStats(PlayerEventInfo player, String params) {
        if (!(this.globalStatsLoaded && this._enableStatistics && this._enableGlobalStatistics)) {
            player.sendMessage("Statistics engine is turned off.");
            return;
        }
        StringTokenizer st = new StringTokenizer(params);
        int page = Integer.parseInt(st.nextToken());
        if (page == 0) {
            page = 1;
        }
        SortType sort = st.hasMoreTokens() ? SortType.valueOf(st.nextToken()) : null;
        boolean backToCbMenu = false;
        backToCbMenu = true;
        if (sort != null) {
            String text = null;
            try {
                text = this._globalStatsHtml.get((Object)sort).get(page);
            }
            catch (Exception e) {
                if (player != null) {
                    player.sendMessage("Statistics engine will become functional as soon as some events are runned.");
                }
                return;
            }
            if (text != null) {
                text = this.updateStatuses(text, sort.toString(), page);
                text = backToCbMenu ? text.replaceAll("%back%", "<button value=\"Back\" width=60 action=\"bypass nxs_showstats_cbmenu\" height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">") : text.replaceAll("%back%", "");
                this.showHtmlText(player, text);
            }
        }
    }

    private void showPlayersGlobalStats(PlayerEventInfo player, PlayerEventInfo target, String sortType, String page, boolean backToCbMenu) {
        if (!this._enableStatistics) {
            player.sendMessage("Statistics engine is turned off.");
            return;
        }
        this.statsChanged(target);
        if (this._ignoreGMs && target.isGM() && !player.isGM()) {
            player.sendMessage("GM's stats are uber secret.");
            return;
        }
        String text = null;
        text = !this._playerGlobalStatsHtml.containsKey((Object)target) ? this.generatePlayersGlobalStatsHtml(target) : this._playerGlobalStatsHtml.get((Object)target);
        if ((text = this.addExtraData(text, sortType, page, backToCbMenu)) != null) {
            this.showHtmlText(player, text);
            player.sendStaticPacket();
        }
    }

    private String addExtraData(String text, String sortType, String page, boolean backToCbMenu) {
        if (backToCbMenu) {
            text = text.replaceAll("%data%", "<button value=\"Back\" width=60 action=\"bypass nxs_showstats_cbmenu\" height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
        } else if (sortType != null && page != null) {
            int pageNumber = Integer.parseInt(page);
            text = text.replaceAll("%data%", "<button value=\"Back\" width=60 action=\"bypass nxs_showstats_global_topplayers " + pageNumber + " " + sortType.toString() + "\" height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
        } else {
            text = text.replaceAll("%data%", "");
        }
        return text;
    }

    private String generatePlayersGlobalStatsHtml(PlayerEventInfo player) {
        GlobalStatsSum sum;
        TextBuilder tb = new TextBuilder();
        tb.append("<html><body><br><center>");
        if (this._showDetailedPlayerInfo && (sum = this._data.get(player.getPlayersId())) != null) {
            tb.append("<font color=ac9887>" + player.getPlayersName() + " </font><font color=9f9f9f>(Lvl " + player.getLevel() + " " + player.getClassName() + ")</font><br>");
            tb.append("<center><table width=430 bgcolor=2E2E2E>");
            String clan = CallBack.getInstance().getOut().getClanName(player.getClanId());
            String ally = CallBack.getInstance().getOut().getAllyName(player.getClanId());
            tb.append("<tr><td width=90><font color=B09D8E>Clan name:</font></td><td width=155 align=left><font color=A9A8A7>" + (clan == null ? "<font color=6f6f6f>No clan</font>" : clan) + "</font></td>");
            tb.append("<td width=80><font color=B09D8E>Ally name:</font></td><td width=120 align=left><font color=A9A8A7>" + (ally == null ? "<font color=6f6f6f>No ally</font>" : ally) + "</font></td></tr>");
            String pvps = String.valueOf(player.getPvpKills());
            String pks = String.valueOf(player.getPkKills());
            if (!this._showPkCount) {
                pks = "<font color=6f6f6f>-secret-</font>";
            }
            tb.append("<tr><td width=90><font color=B09D8E>PvP kills:</font></td><td width=155 align=left><font color=B3AA9D>" + pvps + "</font></td>");
            tb.append("<td width=80><font color=B09D8E>PK count:</font></td><td width=120 align=left><font color=B3AA9D>" + pks + "</font></td></tr>");
            tb.append("<tr></tr><tr><td width=90><font color=B09D8E>Won:</font></td><td width=155 align=left><font color=A9A8A7>" + sum.get(GlobalStatType.WINS) + " </font><font color=8f8f8f>events</font></td>");
            tb.append("<td width=80><font color=B09D8E>Lost:</font></td><td width=120 align=left><font color=A9A8A7>" + sum.get(GlobalStatType.LOSES) + " <font color=8f8f8f>events</font></td></tr>");
            tb.append("<tr><td width=86><font color=B09D8E>Participated:</font></td><td width=120 align=left><font color=A9A8A7><font color=8f8f8f>in</font> " + sum.get(GlobalStatType.COUNT_PLAYED) + " <font color=8f8f8f>events</font></td>");
            tb.append("<td width=80><font color=B09D8E>K:D ratio:</font></td><td width=155 align=left><font color=A9A8A7>" + sum.kdRatio + "</font></font></td></tr>");
            tb.append("<tr><td width=90><font color=B09D8E>Kills/Deaths:</font></td><td width=155 align=left><font color=A9A8A7>" + sum.get(GlobalStatType.KILLS) + " / " + sum.get(GlobalStatType.DEATHS) + "</font></font></td>");
            tb.append("<td width=80><font color=B09D8E>Score:</font></td><td width=120 align=left><font color=A9A8A7>" + sum.get(GlobalStatType.SCORE) + "</font></td></tr>");
            tb.append("</table><br><br><br>");
        }
        tb.append("<font color=ac9887>" + player.getPlayersName() + "'s event statistics</font><br1>");
        tb.append("<font color=6f6f6f>(click on event for more info)</font><br>");
        tb.append("<table width=740 bgcolor=4E4E4E><tr> <td width=130><font color=B09D8E>Event</font></td><td width=90 align=center><font color=A9A8A7>Times played</font></td><td width=65 align=center><font color=A9A8A7>Win %</font></td><td width=65 align=center><font color=A9A8A7>K:D ratio</font></td><td width=65 align=center><font color=A9A8A7>Wins</font></td><td width=65 align=center><font color=A9A8A7>Loses</font></td><td width=65 align=center><font color=A9A8A7>Kills</font></td><td width=65 align=center><font color=A9A8A7>Deaths</font></td><td width=65 align=center><font color=A9A8A7>Score</font></td></tr></table>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=740 height=6>");
        boolean bg = false;
        for (EventType event : EventType.values()) {
            if (this._playerGlobalStats.get((Object)player) == null) {
                tb.append("<table width=740><tr><td>Event data not available.</td></tr></table>");
                break;
            }
            GlobalStatsModel stats = this._playerGlobalStats.get((Object)player).get((Object)event);
            if (stats == null) continue;
            int kills = stats.get(GlobalStatType.KILLS);
            int deaths = stats.get(GlobalStatType.DEATHS);
            int timesPlayed = stats.get(GlobalStatType.COUNT_PLAYED);
            int wins = stats.get(GlobalStatType.WINS);
            String kdRatio = String.valueOf(deaths == 0 ? (double)kills : (double)kills / (double)deaths);
            String success = String.valueOf((int)((double)wins / (double)timesPlayed * 100.0));
            kdRatio = kdRatio.substring(0, Math.min(3, kdRatio.length()));
            success = success.substring(0, Math.min(5, success.length())) + "%";
            tb.append("<table width=740 bgcolor=" + (bg ? "3E3E3E" : "2E2E2E") + "><tr><td width=130><font color=B09D8E>" + event.getHtmlTitle() + "</font> </td><td width=90 align=center><font color=B3AA9D>" + stats.get(GlobalStatType.COUNT_PLAYED) + "</font></td><td width=65 align=center><font color=B3AA9D>" + success + "</font></td><td width=65 align=center><font color=B3AA9D>" + kdRatio + "</font></td><td width=65 align=center><font color=B3AA9D>" + stats.get(GlobalStatType.WINS) + "</font></td><td width=65 align=center><font color=B3AA9D>" + stats.get(GlobalStatType.LOSES) + "</font></td><td width=65 align=center><font color=B3AA9D>" + stats.get(GlobalStatType.KILLS) + "</font></td><td width=65 align=center><font color=B3AA9D>" + stats.get(GlobalStatType.DEATHS) + "</font></td><td width=65 align=center><font color=B3AA9D>" + stats.get(GlobalStatType.SCORE) + "</font></td></tr>");
            tb.append("</table><img src=\"L2UI.SquareBlank\" width=740 height=3>");
            bg = !bg;
        }
        tb.append("<br>%data%");
        tb.append("</center></body></html>");
        this._playerGlobalStatsHtml.put(player, tb.toString());
        return tb.toString();
    }

    private void loadPlayer(PlayerEventInfo player) {
        if (!this._playerGlobalStats.containsKey((Object)player)) {
            Map<PlayerEventInfo, Map<EventType, GlobalStatsModel>> map = this._playerGlobalStats;
            synchronized (map) {
                this._playerGlobalStats.put(player, (Map<EventType, GlobalStatsModel>)new FastMap());
            }
        }
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT event, count_played, wins, loses, kills, deaths, score FROM nexus_stats_global WHERE player = " + player.getPlayersId());
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                EventType type = EventType.getType(rset.getString("event"));
                if (type == null) continue;
                FastMap map = new FastMap();
                map.put(GlobalStatType.COUNT_PLAYED, rset.getInt("count_played"));
                map.put(GlobalStatType.WINS, rset.getInt("wins"));
                map.put(GlobalStatType.LOSES, rset.getInt("loses"));
                map.put(GlobalStatType.KILLS, rset.getInt("kills"));
                map.put(GlobalStatType.DEATHS, rset.getInt("deaths"));
                map.put(GlobalStatType.SCORE, rset.getInt("score"));
                GlobalStatsModel stats = new GlobalStatsModel(type, (Map<GlobalStatType, Integer>)map);
                try {
                    Map<PlayerEventInfo, Map<EventType, GlobalStatsModel>> map2 = this._playerGlobalStats;
                    synchronized (map2) {
                        this._playerGlobalStats.get((Object)player).put(type, stats);
                        continue;
                    }
                }
                catch (Exception e) {
                    try {
                        NexusLoader.debug((String)("An error occured while running GlobalStas.loadPlayer for player " + player.getPlayersName()), (Level)Level.WARNING);
                    }
                    catch (NullPointerException e2) {
                        NexusLoader.debug((String)"An error occured while running GlobalStas.loadPlayer, player is null", (Level)Level.WARNING);
                    }
                    e.printStackTrace();
                    continue;
                }
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
    }

    public void updateGlobalStats(Map<PlayerEventInfo, GlobalStatsModel> data) {
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            for (Map.Entry<PlayerEventInfo, GlobalStatsModel> e : data.entrySet()) {
                this.statsChanged(e.getKey());
                GlobalStatsModel stats = this.getPlayerGlobalStats(e.getKey(), e.getValue().getEvent());
                if (stats == null) {
                    stats = e.getValue();
                    this.setPlayerGlobalStats(e.getKey(), stats.getEvent(), stats);
                } else {
                    stats.add(e.getValue());
                }
                PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_stats_global VALUES (?,?,?,?,?,?,?,?,?)");
                statement.setInt(1, e.getKey().getPlayersId());
                statement.setString(2, stats.getEvent().getAltTitle());
                statement.setInt(3, stats.get(GlobalStatType.COUNT_PLAYED));
                statement.setInt(4, stats.get(GlobalStatType.WINS));
                statement.setInt(5, stats.get(GlobalStatType.LOSES));
                statement.setInt(6, stats.get(GlobalStatType.KILLS));
                statement.setInt(7, stats.get(GlobalStatType.DEATHS));
                statement.setInt(8, stats.get(GlobalStatType.SCORE));
                statement.setString(9, stats.getFavoriteEvent());
                statement.executeUpdate();
                statement.close();
            }
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

    private void forgetPlayerGlobalStats(PlayerEventInfo player) {
        Map<PlayerEventInfo, Map<EventType, GlobalStatsModel>> map = this._playerGlobalStats;
        synchronized (map) {
            this._playerGlobalStats.remove((Object)player);
        }
    }

    protected String updateStatuses(String text, String sortType, int page) {
        String updated = text;
        int start = 0;
        int end = 0;
        while ((start = updated.indexOf("<i>")) != -1) {
            end = updated.indexOf("</i>");
            String name = updated.substring(start+=3, end);
            PlayerEventInfo player = CallBack.getInstance().getOut().getPlayer(name);
            if (player != null) {
                updated = updated.replaceFirst("<i>", "<font color=9EB39D><a action=\"bypass -h nxs_showstats_global_oneplayer " + name + " " + sortType + " " + page + "\">");
                updated = updated.replaceFirst("</i>", "</a></font>");
                continue;
            }
            updated = updated.replaceFirst("<i>", "<font color=A9A8A7>");
            updated = updated.replaceFirst("</i>", "</font>");
        }
        updated = updated.replaceAll("%reloaded%", this.calcLastLoadedTime());
        return updated;
    }

    protected void loadGlobalStats() {
        this.loadConfigs();
        if (!(this._enableStatistics && this._enableGlobalStatistics)) {
            return;
        }
        this._globalStatsHtml = new FastMap();
        TextBuilder tb = new TextBuilder();
        Connection con = null;
        PreparedStatement statement = null;
        String charName = null;
        int playersPerPage = this._playersPerPage;
        String condition = "";
        if (this._ignoreGMs && this._ignoreBannedPlayers) {
            condition = "WHERE characters.accesslevel = 0";
        } else if (this._ignoreGMs) {
            condition = "WHERE characters.accesslevel <= 0";
        } else if (this._ignoreBannedPlayers) {
            condition = "WHERE characters.accesslevel >= 0";
        }
        try {
            int deaths;
            int loses;
            int kills;
            int wins;
            int score;
            int timesPlayed;
            con = CallBack.getInstance().getOut().getConnection();
            this._data = new FastMap();
            statement = con.prepareStatement("SELECT characters.char_name, characters.charId, characters.online, characters.level, characters.pvpkills, characters.pkkills, characters.clanid, characters.classid, nexus_stats_global.* FROM nexus_stats_global INNER JOIN characters ON characters.charId = nexus_stats_global.player " + condition + " ORDER BY characters.char_name");
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                int charId = rset.getInt("charId");
                if (!this._data.containsKey(charId)) {
                    charName = rset.getString("char_name");
                    int level = rset.getInt("level");
                    int pvpkills = rset.getInt("pvpkills");
                    int pkkills = rset.getInt("pkkills");
                    int clanid = rset.getInt("clanid");
                    int classid = rset.getInt("classid");
                    this._data.put(charId, new GlobalStatsSum(charName, level, pvpkills, pkkills, clanid, classid));
                }
                if ((timesPlayed = rset.getInt("count_played")) > this._data.get((Object)Integer.valueOf((int)charId)).mostPlayedCount) {
                    this._data.get((Object)Integer.valueOf((int)charId)).mostPlayedCount = timesPlayed;
                    this._data.get((Object)Integer.valueOf((int)charId)).mostPlayedEvent = EventType.getType(rset.getString("event"));
                }
                wins = rset.getInt("wins");
                loses = rset.getInt("loses");
                kills = rset.getInt("kills");
                deaths = rset.getInt("deaths");
                score = rset.getInt("score");
                this._data.get(charId).raise(GlobalStatType.COUNT_PLAYED, timesPlayed);
                this._data.get(charId).raise(GlobalStatType.WINS, wins);
                this._data.get(charId).raise(GlobalStatType.LOSES, loses);
                this._data.get(charId).raise(GlobalStatType.KILLS, kills);
                this._data.get(charId).raise(GlobalStatType.DEATHS, deaths);
                this._data.get(charId).raise(GlobalStatType.SCORE, score);
            }
            rset.close();
            statement.close();
            int type = 1;
            if (this._statsSorting.equals("advanced")) {
                type = 2;
            } else if (this._statsSorting.equals("full")) {
                type = 3;
            }
            for (SortType sortType : SortType.values()) {
                if (type == 1 ? sortType != SortType.NAME && sortType != SortType.LEVEL : type == 2 && sortType != SortType.NAME && sortType != SortType.LEVEL && sortType != SortType.COUNTPLAYED && sortType != SortType.KDRATIO) continue;
                FastList sorted = new FastList();
                sorted.addAll(this._data.values());
                switch (sortType) {
                    case NAME: {
                        break;
                    }
                    case LEVEL: {
                        Collections.sort(sorted, new Comparator<GlobalStatsSum>(){

                            @Override
                            public int compare(GlobalStatsSum stats1, GlobalStatsSum stats2) {
                                int level1 = stats1.level;
                                int level2 = stats2.level;
                                return level1 == level2 ? 0 : (level1 < level2 ? 1 : -1);
                            }
                        });
                        break;
                    }
                    case WINS: {
                        Collections.sort(sorted, new Comparator<GlobalStatsSum>(){

                            @Override
                            public int compare(GlobalStatsSum stats1, GlobalStatsSum stats2) {
                                int wins2;
                                int wins1 = stats1.get(GlobalStatType.WINS);
                                return wins1 == (wins2 = stats2.get(GlobalStatType.WINS)) ? 0 : (wins1 < wins2 ? 1 : -1);
                            }
                        });
                        break;
                    }
                    case DEATHS: {
                        Collections.sort(sorted, new Comparator<GlobalStatsSum>(){

                            @Override
                            public int compare(GlobalStatsSum stats1, GlobalStatsSum stats2) {
                                int deaths2;
                                int deaths1 = stats1.get(GlobalStatType.DEATHS);
                                return deaths1 == (deaths2 = stats2.get(GlobalStatType.DEATHS)) ? 0 : (deaths1 < deaths2 ? 1 : -1);
                            }
                        });
                        break;
                    }
                    case SCORE: {
                        Collections.sort(sorted, new Comparator<GlobalStatsSum>(){

                            @Override
                            public int compare(GlobalStatsSum stats1, GlobalStatsSum stats2) {
                                int score2;
                                int score1 = stats1.get(GlobalStatType.SCORE);
                                return score1 == (score2 = stats2.get(GlobalStatType.SCORE)) ? 0 : (score1 < score2 ? 1 : -1);
                            }
                        });
                        break;
                    }
                    case COUNTPLAYED: {
                        Collections.sort(sorted, new Comparator<GlobalStatsSum>(){

                            @Override
                            public int compare(GlobalStatsSum stats1, GlobalStatsSum stats2) {
                                int count2;
                                int count1 = stats1.get(GlobalStatType.COUNT_PLAYED);
                                return count1 == (count2 = stats2.get(GlobalStatType.COUNT_PLAYED)) ? 0 : (count1 < count2 ? 1 : -1);
                            }
                        });
                        break;
                    }
                    case LOSES: {
                        Collections.sort(sorted, new Comparator<GlobalStatsSum>(){

                            @Override
                            public int compare(GlobalStatsSum stats1, GlobalStatsSum stats2) {
                                int loses2;
                                int loses1 = stats1.get(GlobalStatType.LOSES);
                                return loses1 == (loses2 = stats2.get(GlobalStatType.LOSES)) ? 0 : (loses1 < loses2 ? 1 : -1);
                            }
                        });
                        break;
                    }
                    case KDRATIO: {
                        Collections.sort(sorted, new Comparator<GlobalStatsSum>(){

                            @Override
                            public int compare(GlobalStatsSum stats1, GlobalStatsSum stats2) {
                                try {
                                    double ratio1 = Double.valueOf(stats1.kdRatio);
                                    double ratio2 = Double.valueOf(stats2.kdRatio);
                                    return ratio1 == ratio2 ? 0 : (ratio1 < ratio2 ? 1 : -1);
                                }
                                catch (Exception e) {
                                    return 0;
                                }
                            }
                        });
                    }
                }
                int size = this._data.size();
                int count = 0;
                boolean bg = false;
                for (GlobalStatsSum stats : sorted) {
                    if (++count % playersPerPage == 1) {
                        tb.append("<html><body><br><center><font color=ac9887>Server event statistics</font><br1><font color=7f7f7f>(reloaded: %reloaded%)</font><br><br>");
                        tb.append("<table width=725><tr><td width=70 align=left><button value=\"Refresh\" width=70 action=\"bypass nxs_showstats_global_topplayers " + ((count - 1) / playersPerPage + 1) + " " + sortType.toString() + "\" height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td><td width=500 align=center><font color=7f7f7f>(click on column name to sort the players)</font></td><td width=60><font color=ac9887>Name:</font></td><td width=100 align=left><edit var=\"name\" width=100 height=14></td><td width=65 align=right><button value=\"Find\" width=60 action=\"bypass nxs_showstats_global_oneplayer $name " + sortType.toString() + " " + ((count - 1) / playersPerPage + 1) + "\" height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr></table>");
                        if (this._statsSorting.equals("simple")) {
                            tb.append("<br><center><table width=747 bgcolor=5A5A5A><tr><td width=25 ><font color=B09D8E>&nbsp;#</font></td><td width=125><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.NAME.toString() + "\">Name</a>&nbsp;(<a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.LEVEL.toString() + "\">lvl</a>)</font></td><td width=55><font color=A9A8A7>Clan</font></td><td width=55><font color=A9A8A7>Played ev.</font></td><td width=50><center><font color=A9A8A7>&nbsp;Win%</font></td><td width=50><center><font color=A9A8A7>K:D</font></td><td width=50><center><font color=A9A8A7>Wins</font></td><td width=55><center><font color=A9A8A7>Loses</font></td><td width=55><center><font color=A9A8A7>Score</font></td><td width=55><center><font color=A9A8A7>Deaths</font></td><td width=95><center><font color=A9A8A7>Favorite Event</font></td></tr></table><br>" + "<center>");
                        } else if (this._statsSorting.equals("advanced")) {
                            tb.append("<br><center><table width=747 bgcolor=5A5A5A><tr><td width=25 ><font color=B09D8E>&nbsp;#</font></td><td width=125><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.NAME.toString() + "\">Name</a>&nbsp;(<a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.LEVEL.toString() + "\">lvl</a>)</font></td><td width=55><font color=A9A8A7>Clan</font></td><td width=55><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.COUNTPLAYED.toString() + "\">Played ev.</a></font></td><td width=50><center><font color=A9A8A7>&nbsp;Win%</font></td><td width=50><center><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.KDRATIO.toString() + "\">K:D</a></font></td><td width=50><center><font color=A9A8A7>Wins</font></td><td width=55><center><font color=A9A8A7>Loses</font></td><td width=55><center><font color=A9A8A7>Score</font></td><td width=55><center><font color=A9A8A7>Deaths</font></td><td width=95><center><font color=A9A8A7>Favorite Event</font></td></tr></table><br>" + "<center>");
                        } else if (this._statsSorting.equals("full")) {
                            tb.append("<br><center><table width=747 bgcolor=5A5A5A><tr><td width=25 ><font color=B09D8E>&nbsp;#</font></td><td width=125><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.NAME.toString() + "\">Name</a>&nbsp;(<a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.LEVEL.toString() + "\">lvl</a>)</font></td><td width=55><font color=A9A8A7>Clan</font></td><td width=55><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.COUNTPLAYED.toString() + "\">Played ev.</a></font></td><td width=50><center><font color=A9A8A7>&nbsp;Win%</font></td><td width=50><center><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.KDRATIO.toString() + "\">K:D</a></font></td><td width=50><center><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.WINS.toString() + "\">Wins</a></font></td><td width=55><center><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.LOSES.toString() + "\">Loses</a></font></td><td width=55><center><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.SCORE.toString() + "\">Score</a></font></td><td width=55><center><font color=A9A8A7><a action=\"bypass -h nxs_showstats_global_topplayers " + (count - 1) / playersPerPage + " " + SortType.DEATHS.toString() + "\">Deaths</a></font></td><td width=95><center><font color=A9A8A7>Favorite Event</font></td></tr></table><br>" + "<center>");
                        }
                    }
                    tb.append("<center><table width=740 " + (bg ? "bgcolor=3E3E3E" : "bgcolor=2E2E2E") + "><tr><td width=30 align=left><font color=B09D8E>&nbsp;" + count + ".</font></td>");
                    bg = !bg;
                    String clan = CallBack.getInstance().getOut().getClanName(stats.clan);
                    if (clan == null) {
                        clan = "";
                    } else if (clan.length() > 15) {
                        clan = clan.substring(0, 12) + "..";
                    }
                    tb.append("<td width=115><i>" + stats.name + "</i> <font color=A9A8A7>(" + stats.level + ")</font></td><td width=108 align=left><font color=B09D8E>" + clan + "</font></td>");
                    timesPlayed = stats.get(GlobalStatType.COUNT_PLAYED);
                    wins = stats.get(GlobalStatType.WINS);
                    loses = stats.get(GlobalStatType.LOSES);
                    kills = stats.get(GlobalStatType.KILLS);
                    deaths = stats.get(GlobalStatType.DEATHS);
                    score = stats.get(GlobalStatType.SCORE);
                    String kdRatio = stats.kdRatio;
                    String success = String.valueOf((int)((double)wins / (double)timesPlayed * 100.0));
                    success = success.substring(0, Math.min(5, success.length()));
                    tb.append("<td width=53 align=left><font color=B3AA9D>" + timesPlayed + "</font></td><td width=53><font color=B3AA9D>" + success + "%</font></td><td width=45><font color=B3AA9D>&nbsp;" + kdRatio + "</font></td><td width=57><center><font color=B3AA9D>" + wins + "</font></td><td width=55><center><font color=B3AA9D>" + loses + "</font></td><td width=55><center>&nbsp;&nbsp;<font color=B3AA9D>" + score + "</font></td>" + "<td width=55><center>&nbsp;&nbsp;&nbsp;<font color=B3AA9D>" + deaths + "</font></td><td width=120><center><font color=B3AA9D>" + (stats.mostPlayedEvent != null ? stats.mostPlayedEvent.getAltTitle() : "N/A") + "</font> <font color=7B7A79>(" + stats.mostPlayedCount + "x)</font></td></tr></table><img src=\"L2UI.SquareBlank\" width=740 height=3>");
                    if (count % playersPerPage != 0) continue;
                    tb.append("<center><br><br><table width=140><tr><td width=70 align=left>" + ((count - 1) / playersPerPage != 0 ? new StringBuilder().append("<button value=\"Page ").append((count - 1) / playersPerPage).append("\" width=60 action=\"bypass nxs_showstats_global_topplayers ").append((count - 1) / playersPerPage).append(" ").append(sortType.toString()).append("\" height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">").toString() : new StringBuilder().append("<font color=ac9887>Page ").append((count - 1) / playersPerPage + 1).append("</font>").toString()) + "</td>" + "<td width=70 align=right>" + ((count - 1) / playersPerPage != size / playersPerPage ? new StringBuilder().append("<button value=\"Page ").append((count - 1) / playersPerPage + 2).append("\" width=60 action=\"bypass nxs_showstats_global_topplayers ").append((count - 1) / playersPerPage + 2).append(" ").append(sortType.toString()).append("\" height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">").toString() : new StringBuilder().append("<font color=ac9887>Page ").append((count - 1) / playersPerPage + 2).append("</font>").toString()) + "</td></tr></table>");
                    tb.append("<center><br>%back%</center></body></html>");
                    if (!this._globalStatsHtml.containsKey((Object)sortType)) {
                        this._globalStatsHtml.put(sortType, (Map<Integer, String>)new FastMap());
                    }
                    this._globalStatsHtml.get((Object)sortType).put(count / playersPerPage, tb.toString());
                    tb.clear();
                }
                if (count % playersPerPage == 0) continue;
                tb.append("<center><br><br><table width=140><tr><td width=70 align=left>" + ((count - 1) / playersPerPage != 0 ? new StringBuilder().append("<button value=\"Page ").append((count - 1) / playersPerPage).append("\" width=60 action=\"bypass nxs_showstats_global_topplayers ").append((count - 1) / playersPerPage).append(" ").append(sortType.toString()).append("\" height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">").toString() : new StringBuilder().append("<font color=ac9887>Page ").append((count - 1) / playersPerPage + 1).append("</font>").toString()) + "</td>" + "<td width=70 align=right>" + ((count - 1) / playersPerPage != size / playersPerPage ? new StringBuilder().append("<button value=\"Page ").append((count - 1) / playersPerPage + 2).append("\" width=60 action=\"bypass nxs_showstats_global_topplayers ").append((count - 1) / playersPerPage + 2).append(" ").append(sortType.toString()).append("\" height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">").toString() : new StringBuilder().append("<font color=ac9887>Page ").append((count - 1) / playersPerPage + 1).append("</font>").toString()) + "</td></tr></table>");
                tb.append("<center><br>%back%</center></body></html>");
                if (!this._globalStatsHtml.containsKey((Object)sortType)) {
                    this._globalStatsHtml.put(sortType, (Map<Integer, String>)new FastMap());
                }
                if (this._globalStatsHtml.get((Object)sortType).containsKey(count / playersPerPage + 1)) continue;
                this._globalStatsHtml.get((Object)sortType).put(count / playersPerPage + 1, tb.toString());
                tb.clear();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        this.globalStatsLoaded = true;
        this._lastLoad = System.currentTimeMillis();
        NexusLoader.debug((String)"Global statistics reloaded.");
        this.scheduleReloadGlobalStats();
    }

    private String calcLastLoadedTime() {
        long time = System.currentTimeMillis();
        long diff = (time - this._lastLoad) / 1000;
        if (diff > 3600) {
            return "" + diff / 3600 + " hours ago";
        }
        if (diff > 60) {
            return "" + diff / 60 + " minutes ago";
        }
        return "" + diff + " seconds ago";
    }

    private synchronized void scheduleReloadGlobalStats() {
        if (this._globalStatsReload != null) {
            this._globalStatsReload.cancel(false);
            this._globalStatsReload = null;
        }
        this._globalStatsReload = CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

            @Override
            public void run() {
                GlobalStats.this.loadGlobalStats();
            }
        }, this._statsRefresh * 1000);
    }

    private class GlobalStatsSum {
        protected Map<GlobalStatType, Integer> stats;
        protected String name;
        protected int level;
        protected int pvp;
        protected int pk;
        protected int clan;
        protected int classId;
        protected EventType mostPlayedEvent;
        protected int mostPlayedCount;
        protected String kdRatio;

        public GlobalStatsSum(String name, int level, int pvp, int pk, int clan, int classId) {
            this.mostPlayedCount = 0;
            this.name = name;
            this.level = level;
            this.pvp = pvp;
            this.pk = pk;
            this.clan = clan;
            this.classId = classId;
            this.stats = new FastMap();
            for (GlobalStatType t : GlobalStatType.values()) {
                this.stats.put(t, 0);
            }
        }

        public int get(GlobalStatType type) {
            return this.stats.get((Object)type);
        }

        public void set(GlobalStatType type, int value) {
            this.stats.put(type, value);
        }

        public void raise(GlobalStatType type, int value) {
            this.set(type, this.get(type) + value);
            if (type == GlobalStatType.KILLS || type == GlobalStatType.DEATHS) {
                this.updateKdRatio();
            }
        }

        private void updateKdRatio() {
            int kills = this.get(GlobalStatType.KILLS);
            int deaths = this.get(GlobalStatType.DEATHS);
            this.kdRatio = String.valueOf(deaths == 0 ? (double)kills : (double)kills / (double)deaths);
            this.kdRatio = this.kdRatio.substring(0, Math.min(3, this.kdRatio.length()));
        }
    }

    private static enum SortType {
        NAME("characters.char_name"),
        COUNTPLAYED("nexus_stats_global.count_played DESC"),
        WINS("nexus_stats_global.wins DESC"),
        LOSES("nexus_stats_global.loses DESC"),
        SCORE("nexus_stats_global.score DESC"),
        DEATHS("nexus_stats_global.deaths DESC"),
        LEVEL("characters.level DESC"),
        KDRATIO("");
        

        private SortType(String dbName) {
        }
    }

    public static enum GlobalStatType {
        COUNT_PLAYED("count played"),
        WINS("wins"),
        LOSES("loses"),
        KILLS("kills"),
        DEATHS("deaths"),
        SCORE("score");
        
        String name;

        private GlobalStatType(String name) {
            this.name = name;
        }
    }

}

