/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 *  javolution.util.FastSet
 */
package cz.nxs.events.engine;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.SkillData;
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
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

public class EventBuffer {
    private Map<String, Map<Integer, Integer>> _aviableBuffs = new FastMap();
    private FastMap<Integer, Map<String, List<Integer>>> _buffs = new FastMap();
    private Map<Integer, String> _activeSchemes = new FastMap();
    private Map<Integer, String> _activePetSchemes = new FastMap();
    private Map<Integer, List<String>> _modified = new FastMap();
    private DataUpdater _dataUpdater;

    public EventBuffer() {
        this._dataUpdater = new DataUpdater();
        this.loadAviableBuffs(true);
    }

    public void reloadBuffer() {
        this.loadAviableBuffs(false);
    }

    public void loadPlayer(PlayerEventInfo player) {
        this.loadData(player.getPlayersId());
    }

    public void buffPlayer(PlayerEventInfo player, boolean heal) {
        Iterator<Integer> i$ = this.getBuffs(player).iterator();
        while (i$.hasNext()) {
            int buffId = i$.next();
            player.getSkillEffects(buffId, this.getLevelFor(buffId));
        }
        if (heal) {
            player.setCurrentHp(player.getMaxHp());
            player.setCurrentMp(player.getMaxMp());
            player.setCurrentCp(player.getMaxCp());
        }
    }

    public void buffPlayer(PlayerEventInfo player) {
        Iterator<Integer> i$ = this.getBuffs(player).iterator();
        while (i$.hasNext()) {
            int buffId = i$.next();
            player.getSkillEffects(buffId, this.getLevelFor(buffId));
        }
        if (EventConfig.getInstance().getGlobalConfigBoolean("bufferHealsPlayer")) {
            player.setCurrentHp(player.getMaxHp());
            player.setCurrentMp(player.getMaxMp());
            player.setCurrentCp(player.getMaxCp());
        }
    }

    public void buffPet(PlayerEventInfo player) {
        if (player.hasPet()) {
            if (this.getPlayersCurrentPetScheme(player.getPlayersId()) == null) {
                return;
            }
            Iterator<Integer> i$ = this.getBuffs(player, this.getPlayersCurrentPetScheme(player.getPlayersId())).iterator();
            while (i$.hasNext()) {
                int buffId = i$.next();
                player.getPetSkillEffects(buffId, this.getLevelFor(buffId));
            }
        }
    }

    public void addModifiedBuffs(PlayerEventInfo player, String schemeName) {
        if (!this._modified.containsKey(player.getPlayersId())) {
            this._modified.put(player.getPlayersId(), (List<String>)new FastList());
        }
        if (!this._modified.get(player.getPlayersId()).contains(schemeName)) {
            this._modified.get(player.getPlayersId()).add(schemeName);
        }
    }

    public void addModifiedBuffs(int player, String schemeName) {
        if (!this._modified.containsKey(player)) {
            this._modified.put(player, (List<String>)new FastList());
        }
        if (!this._modified.get(player).contains(schemeName)) {
            this._modified.get(player).add(schemeName);
        }
    }

    public boolean hasBuffs(PlayerEventInfo player) {
        try {
            return !((Map)this._buffs.get((Object)player.getPlayersId())).isEmpty();
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean addScheme(PlayerEventInfo player, String schemeName) {
        if (((Map)this._buffs.get((Object)player.getPlayersId())).containsKey(schemeName)) {
            return false;
        }
        if (((Map)this._buffs.get((Object)player.getPlayersId())).size() >= 6) {
            if (player != null) {
                player.sendMessage("You can't have more than 6 schemes.");
            }
            return false;
        }
        ((Map)this._buffs.get((Object)player.getPlayersId())).put(schemeName, new FastList());
        this.setPlayersCurrentScheme(player.getPlayersId(), schemeName);
        this.addModifiedBuffs(player, schemeName);
        return true;
    }

    public boolean removeScheme(PlayerEventInfo player, String schemeName) {
        if (!((Map)this._buffs.get((Object)player.getPlayersId())).containsKey(schemeName)) {
            return false;
        }
        ((Map)this._buffs.get((Object)player.getPlayersId())).remove(schemeName);
        if (schemeName.equals(this.getPlayersCurrentScheme(player.getPlayersId()))) {
            this.setPlayersCurrentScheme(player.getPlayersId(), this.getFirstScheme(player.getPlayersId()));
        }
        this.addModifiedBuffs(player, schemeName);
        return true;
    }

    public String getPlayersCurrentScheme(int player) {
        String current = this._activeSchemes.get(player);
        if (current == null) {
            current = this.setPlayersCurrentScheme(player, this.getFirstScheme(player));
        }
        return current;
    }

    public String getPlayersCurrentPetScheme(int player) {
        String current = this._activePetSchemes.get(player);
        return current;
    }

    public String setPlayersCurrentScheme(int player, String schemeName) {
        return this.setPlayersCurrentScheme(player, schemeName, true);
    }

    public String setPlayersCurrentScheme(int player, String schemeName, boolean updateInDb) {
        if (schemeName == null) {
            this._activeSchemes.remove(player);
            return null;
        }
        if (!((Map)this._buffs.get((Object)player)).containsKey(schemeName)) {
            ((Map)this._buffs.get((Object)player)).put(schemeName, new FastList());
        }
        if (updateInDb) {
            if (this._activeSchemes.containsKey(player)) {
                this.addModifiedBuffs(player, this._activeSchemes.get(player));
            }
            this.addModifiedBuffs(player, schemeName);
        }
        this._activeSchemes.put(player, schemeName);
        return schemeName;
    }

    public String setPlayersCurrentPetScheme(int player, String schemeName) {
        if (schemeName == null) {
            this._activePetSchemes.remove(player);
            return null;
        }
        if (!((Map)this._buffs.get((Object)player)).containsKey(schemeName)) {
            ((Map)this._buffs.get((Object)player)).put(schemeName, new FastList());
        }
        this._activePetSchemes.put(player, schemeName);
        return schemeName;
    }

    public String getFirstScheme(int player) {
        Iterator i$;
        if (this._buffs.containsKey((Object)player) && (i$ = ((Map)this._buffs.get((Object)player)).entrySet().iterator()).hasNext()) {
            Map.Entry e = i$.next();
            return (String)e.getKey();
        }
        return null;
    }

    public Set<Map.Entry<String, List<Integer>>> getSchemes(PlayerEventInfo player) {
        if (this._buffs.containsKey((Object)player.getPlayersId())) {
            return ((Map)this._buffs.get((Object)player.getPlayersId())).entrySet();
        }
        return new FastSet();
    }

    public boolean addBuff(int buffId, PlayerEventInfo player) {
        String scheme = this.getPlayersCurrentScheme(player.getPlayersId());
        if (scheme == null) {
            return false;
        }
        if (!((Map)this._buffs.get((Object)player.getPlayersId())).containsKey(scheme)) {
            return false;
        }
        if (((List)((Map)this._buffs.get((Object)player.getPlayersId())).get(scheme)).contains(buffId)) {
            return false;
        }
        ((List)((Map)this._buffs.get((Object)player.getPlayersId())).get(scheme)).add(buffId);
        this.addModifiedBuffs(player, scheme);
        return true;
    }

    public void removeBuff(int buffId, PlayerEventInfo player) {
        String scheme = this.getPlayersCurrentScheme(player.getPlayersId());
        if (scheme == null) {
            return;
        }
        ((List)((Map)this._buffs.get((Object)player.getPlayersId())).get(scheme)).remove(new Integer(buffId));
        this.addModifiedBuffs(player, scheme);
    }

    public boolean containsSkill(int buffId, PlayerEventInfo player) {
        String scheme = this.getPlayersCurrentScheme(player.getPlayersId());
        if (scheme == null) {
            return false;
        }
        return ((List)((Map)this._buffs.get((Object)player.getPlayersId())).get(scheme)).contains(buffId);
    }

    public List<Integer> getBuffs(PlayerEventInfo player) {
        String scheme = this.getPlayersCurrentScheme(player.getPlayersId());
        if (scheme == null) {
            return new FastList();
        }
        return (List)((Map)this._buffs.get((Object)player.getPlayersId())).get(scheme);
    }

    public List<Integer> getBuffs(PlayerEventInfo player, String scheme) {
        return (List)((Map)this._buffs.get((Object)player.getPlayersId())).get(scheme);
    }

    private void loadData(int playerId) {
        FastMap<Integer, Map<String, List<Integer>>> fastMap = this._buffs;
        synchronized (fastMap) {
            this._buffs.put((Object)playerId, (Object)new FastMap());
            Connection con = null;
            PreparedStatement statement = null;
            try {
                con = CallBack.getInstance().getOut().getConnection();
                statement = con.prepareStatement("SELECT * FROM nexus_playerbuffs WHERE playerId = " + playerId);
                ResultSet rset = statement.executeQuery();
                while (rset.next()) {
                    String scheme = rset.getString("scheme");
                    int active = rset.getInt("active");
                    ((Map)this._buffs.get((Object)playerId)).put(scheme, new FastList());
                    for (String buffId : rset.getString("buffs").split(",")) {
                        try {
                            ((List)((Map)this._buffs.get((Object)playerId)).get(scheme)).add(Integer.parseInt(buffId));
                            continue;
                        }
                        catch (Exception e) {
                            // empty catch block
                        }
                    }
                    if (active != 1) continue;
                    this.setPlayersCurrentScheme(playerId, scheme, false);
                }
                rset.close();
                statement.close();
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
        }
    }

    private synchronized void storeData() {
        if (this._modified.isEmpty()) {
            return;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            for (Map.Entry<Integer, List<String>> modified : this._modified.entrySet()) {
                for (String modifiedScheme : modified.getValue()) {
                    statement = con.prepareStatement("DELETE FROM nexus_playerbuffs WHERE playerId = " + modified.getKey() + " AND scheme = '" + modifiedScheme + "'");
                    statement.execute();
                    if (!((Map)this._buffs.get((Object)modified.getKey())).containsKey(modifiedScheme)) continue;
                    TextBuilder tb = new TextBuilder();
                    Iterator i$ = ((List)((Map)this._buffs.get((Object)modified.getKey())).get(modifiedScheme)).iterator();
                    while (i$.hasNext()) {
                        int buffId = (Integer)i$.next();
                        tb.append("" + buffId + ",");
                    }
                    String buffs = tb.toString();
                    if (buffs.length() > 0) {
                        buffs = buffs.substring(0, buffs.length() - 1);
                    }
                    statement = con.prepareStatement("REPLACE INTO nexus_playerbuffs VALUES (?,?,?,?)");
                    statement.setInt(1, modified.getKey());
                    statement.setString(2, modifiedScheme);
                    statement.setString(3, buffs);
                    statement.setInt(4, modifiedScheme.equals(this.getPlayersCurrentScheme(modified.getKey())) ? 1 : 0);
                    statement.executeUpdate();
                    statement.close();
                }
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
        this._modified.clear();
    }

    public Map<String, Map<Integer, Integer>> getAviableBuffs() {
        return this._aviableBuffs;
    }

    public int getLevelFor(int skillId) {
        for (Map<Integer, Integer> e : this._aviableBuffs.values()) {
            for (Map.Entry<Integer, Integer> entry : e.entrySet()) {
                if (entry.getKey() != skillId) continue;
                return entry.getValue();
            }
        }
        return -1;
    }

    private void loadAviableBuffs(boolean test) {
        if (!this._aviableBuffs.isEmpty()) {
            this._aviableBuffs.clear();
        }
        int count = 0;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            statement = con.prepareStatement("SELECT * FROM nexus_buffs");
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                String name;
                String category = rset.getString("category");
                int buffId = rset.getInt("buffId");
                int level = rset.getInt("level");
                if (test && ((name = rset.getString("name")) == null || name.length() == 0)) {
                    try {
                        name = new SkillData(buffId, level).getName();
                        if (name != null) {
                            PreparedStatement statement2 = con.prepareStatement("UPDATE nexus_buffs SET name = '" + name + "' WHERE buffId = " + buffId + " AND level = " + level + "");
                            statement2.execute();
                            statement2.close();
                        }
                    }
                    catch (Exception e) {
                        // empty catch block
                    }
                }
                if (!this._aviableBuffs.containsKey(category)) {
                    this._aviableBuffs.put(category, (Map<Integer, Integer>)new FastMap());
                }
                this._aviableBuffs.get(category).put(buffId, level);
                ++count;
            }
            rset.close();
            statement.close();
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
        NexusLoader.debug((String)("Loaded " + count + " buffs for Event Buffer."), (Level)Level.INFO);
    }

    public static final EventBuffer getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final EventBuffer _instance = new EventBuffer();

        private SingletonHolder() {
        }
    }

    private class DataUpdater
    implements Runnable {
        private DataUpdater() {
            CallBack.getInstance().getOut().scheduleGeneralAtFixedRate(this, 10000, 10000);
        }

        @Override
        public void run() {
            EventBuffer.this.storeData();
        }
    }

}

