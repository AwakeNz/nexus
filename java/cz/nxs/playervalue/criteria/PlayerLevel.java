/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.text.TextBuilder
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
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastMap;

public class PlayerLevel
implements ICriteria {
    private Map<Integer, Integer> _levels;

    public PlayerLevel() {
        this.loadData();
    }

    private void loadData() {
        this._levels = new FastMap();
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT level, score FROM nexus_playervalue_levels");
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                int level = rset.getInt("level");
                int score = rset.getInt("score");
                this._levels.put(level, score);
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
        if (this._levels.isEmpty()) {
            this.recalculate1(10, 10);
            this.saveToDb(this._levels);
            return;
        }
        FastMap missing = new FastMap();
        for (int i = 1; i <= 85; ++i) {
            if (this._levels.containsKey(i)) continue;
            missing.put(i, 0);
            NexusLoader.debug((String)("PlayerValue engine - PlayerLevel criteria - in table 'nexus_playervalue_levels' was missing record for level " + i + ". The engine will try to add it back with value 0, but you might need to correct it."), (Level)Level.SEVERE);
        }
        if (!missing.isEmpty()) {
            this.saveToDb((Map<Integer, Integer>)missing);
        }
    }

    public void saveToDb(Map<Integer, Integer> levels) {
        if (levels.isEmpty()) {
            return;
        }
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            TextBuilder tb = new TextBuilder();
            for (Map.Entry<Integer, Integer> i : levels.entrySet()) {
                tb.append("(" + i.getKey() + "," + i.getValue() + "),");
            }
            String values = tb.toString();
            PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_playervalue_levels VALUES " + values.substring(0, values.length() - 1) + ";");
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
    }

    private void recalculate1(int firstValue, int levelCoefficient) {
        int value = firstValue;
        for (int level = 1; level <= 85; ++level) {
            this._levels.put(level, value+=levelCoefficient);
        }
    }

    private void recalculate2(int startLevel, int levelCoefficient) {
        for (int level = 1; level <= 85; ++level) {
            int value = level - startLevel;
            if ((value*=levelCoefficient) < 0) {
                value = 0;
            }
            this._levels.put(level, value);
        }
    }

    @Override
    public int getPoints(PlayerEventInfo player) {
        if (this._levels.containsKey(player.getLevel())) {
            return this._levels.get(player.getLevel());
        }
        return 0;
    }

    public static final PlayerLevel getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final PlayerLevel _instance = new PlayerLevel();

        private SingletonHolder() {
        }
    }

}

