/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import javolution.util.FastMap;

public class EventWarnings {
    private Map<Integer, Integer> _warnings = new FastMap();
    private ScheduledFuture<?> _decTask = null;
    private SaveScheduler _saveScheduler;
    public static int MAX_WARNINGS = 3;

    public EventWarnings() {
        this._saveScheduler = new SaveScheduler();
        this.loadData();
        this.decreasePointsTask();
        NexusLoader.debug((String)"Nexus Engine: Loaded EventWarnings engine.");
    }

    private void decreasePointsTask() {
        if (this._decTask != null) {
            this._decTask.cancel(false);
        }
        Calendar cal = Calendar.getInstance();
        cal.set(11, 23);
        cal.set(12, 59);
        cal.set(13, 30);
        long delay = cal.getTimeInMillis() - System.currentTimeMillis();
        this._decTask = CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

            @Override
            public void run() {
                Iterator i$ = EventWarnings.this._warnings.keySet().iterator();
                while (i$.hasNext()) {
                    int id = (Integer)i$.next();
                    EventWarnings.this.decreasePoints(id, 1);
                    PlayerEventInfo pi = CallBack.getInstance().getOut().getPlayer(id);
                    if (pi == null) continue;
                    pi.sendMessage(LanguageEngine.getMsg("system_warningsDecreased", EventWarnings.this.getPoints(id)));
                }
                EventWarnings.this.saveData();
                EventWarnings.this.decreasePointsTask();
            }
        }, delay);
    }

    public int getPoints(PlayerEventInfo player) {
        if (player == null) {
            return -1;
        }
        return this._warnings.containsKey(player.getPlayersId()) ? this._warnings.get(player.getPlayersId()) : 0;
    }

    public int getPoints(int player) {
        return this._warnings.containsKey(player) ? this._warnings.get(player) : 0;
    }

    public void addWarning(PlayerEventInfo player, int ammount) {
        if (player == null) {
            return;
        }
        this.addPoints(player.getPlayersId(), ammount);
        if (ammount > 0) {
            player.sendMessage(LanguageEngine.getMsg("system_warning", MAX_WARNINGS - this.getPoints(player)));
        }
    }

    public void addPoints(int player, int ammount) {
        int points = 0;
        if (this._warnings.containsKey(player)) {
            points = this._warnings.get(player);
        }
        if ((points+=ammount) < 0) {
            points = 0;
        }
        if (points > 0) {
            this._warnings.put(player, points);
        } else {
            this._warnings.remove(player);
        }
    }

    public void removeWarning(PlayerEventInfo player, int ammount) {
        this.addWarning(player, - ammount);
    }

    public void decreasePoints(int player, int ammount) {
        this.addPoints(player, - ammount);
    }

    private void loadData() {
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT id, points FROM nexus_warnings");
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                this._warnings.put(rset.getInt("id"), rset.getInt("points"));
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

    public void saveData() {
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_warnings");
            statement.execute();
            statement.close();
            for (Map.Entry<Integer, Integer> e : this._warnings.entrySet()) {
                statement = con.prepareStatement("INSERT INTO nexus_warnings VALUES (" + e.getKey() + "," + e.getValue() + ")");
                statement.execute();
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

    public static final EventWarnings getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final EventWarnings _instance = new EventWarnings();

        private SingletonHolder() {
        }
    }

    private class SaveScheduler
    implements Runnable {
        public SaveScheduler() {
            this.schedule();
        }

        private void schedule() {
            CallBack.getInstance().getOut().scheduleGeneral(this, 1800000);
        }

        @Override
        public void run() {
            EventWarnings.this.saveData();
            this.schedule();
        }
    }

}

