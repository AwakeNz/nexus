/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.util.FastMap
 */
package cz.nxs.playervalue.criteria;

import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.base.GlobalConfigModel;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.playervalue.criteria.ICriteria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javolution.util.FastMap;

public class PlayerSkills
implements ICriteria {
    private Map<Integer, Levels> _skills;

    public PlayerSkills() {
        this.loadData();
    }

    private void loadData() {
        this._skills = new FastMap();
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT skillId, level, score FROM nexus_playervalue_skills");
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                int skillId = rset.getInt("skillId");
                int level = rset.getInt("level");
                int score = rset.getInt("score");
                if (this._skills.containsKey(skillId)) {
                    this._skills.get(skillId).add(level, score);
                    continue;
                }
                this._skills.put(skillId, new Levels(level, score));
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
    }

    public int getScoreForSkill(int skillId, int level) {
        if (this._skills.containsKey(skillId)) {
            return this._skills.get(skillId).get(level);
        }
        return 0;
    }

    @Override
    public int getPoints(PlayerEventInfo player) {
        int points = 0;
        for (SkillData skill : player.getSkills()) {
            points+=this.getScoreForSkill(skill.getId(), skill.getLevel());
        }
        return 0;
    }

    public static final PlayerSkills getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final PlayerSkills _instance = new PlayerSkills();

        private SingletonHolder() {
        }
    }

    private class Levels {
        public FastMap<Integer, Integer> levels;

        public Levels(int level, int points) {
            this.levels = new FastMap();
            this.add(level, points);
        }

        public void add(int level, int points) {
            this.levels.put((Object)level, (Object)points);
        }

        public int get(int level) {
            if (level == -1) {
                int top = 0;
                Iterator i$ = this.levels.values().iterator();
                while (i$.hasNext()) {
                    int points = (Integer)i$.next();
                    if (points <= top) continue;
                    top = points;
                }
                return top;
            }
            if (this.levels.containsKey((Object)level)) {
                return (Integer)this.levels.get((Object)level);
            }
            if (level >= 0) {
                return this.get(--level);
            }
            if (this.levels.containsKey((Object)-1)) {
                return (Integer)this.levels.get((Object)-1);
            }
            return 0;
        }
    }

}

