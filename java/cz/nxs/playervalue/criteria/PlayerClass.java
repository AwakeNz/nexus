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
package cz.nxs.playervalue.criteria;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.base.GlobalConfigModel;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.playervalue.criteria.ICriteria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class PlayerClass
implements ICriteria {
    private FastMap<Integer, Integer> scores;
    private FastList<Integer> changed;
    private final Integer[] classes = CallBack.getInstance().getOut().getAllClassIds();

    public PlayerClass() {
        this.loadData();
    }

    private void loadData() {
        this.scores = new FastMap();
        this.changed = new FastList();
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT classId, score FROM nexus_playervalue_classes");
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                int classId = rset.getInt("classId");
                int score = rset.getInt("score");
                this.scores.put((Object)classId, (Object)score);
            }
            rset.close();
            statement.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            EventConfig.getInstance().getGlobalConfig("GearScore", "enableGearScore").setValue("false");
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {}
        }
        for (Integer i : this.classes) {
            if (this.scores.containsKey((Object)i)) continue;
            this.changed.add((Object)i);
            this.scores.put((Object)i, (Object)0);
        }
        this.save();
    }

    private void save() {
        if (this.changed.isEmpty()) {
            return;
        }
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            TextBuilder tb = new TextBuilder();
            Iterator i$ = this.changed.iterator();
            while (i$.hasNext()) {
                int i = (Integer)i$.next();
                tb.append("(" + i + "," + this.scores.get((Object)i) + "),");
            }
            String values = tb.toString();
            PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_playervalue_classes VALUES " + values.substring(0, values.length() - 1) + ";");
            statement.execute();
            statement.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {}
        }
        this.changed.clear();
    }

    public int getScore(int classId) {
        if (this.scores.containsKey((Object)classId)) {
            return (Integer)this.scores.get((Object)classId);
        }
        NexusLoader.debug((String)("PlayerValue engine: Class ID " + classId + " has no value setted up."), (Level)Level.WARNING);
        return 0;
    }

    public void setValue(int classId, int value) {
        this.scores.put((Object)classId, (Object)value);
        this.changed.add((Object)classId);
    }

    @Override
    public int getPoints(PlayerEventInfo player) {
        int playerClass = player.getActiveClass();
        return this.getScore(playerClass);
    }

    public static final PlayerClass getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final PlayerClass _instance = new PlayerClass();

        private SingletonHolder() {
        }
    }

}

