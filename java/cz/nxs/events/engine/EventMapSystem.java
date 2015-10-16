/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class EventMapSystem {
    private Map<EventType, Map<Integer, EventMap>> _maps = new FastMap();
    private int _lastMapId = 0;

    public EventMapSystem() {
        for (EventType type : EventType.values()) {
            this._maps.put(type, (Map<Integer, EventMap>)new FastMap());
        }
    }

    private EventType[] getTypes(String s) {
        String[] splits = s.split(";");
        FastList types = new FastList();
        for (String typeString : splits) {
            EventType t = EventType.getType(typeString);
            if (t == null) continue;
            types.add(t);
        }
        return types.toArray((T[])new EventType[types.size()]);
    }

    public String convertToString(List<EventType> types) {
        TextBuilder tb = new TextBuilder();
        int i = 1;
        for (EventType t : types) {
            tb.append(t.toString());
            if (i < types.size()) {
                tb.append(";");
            }
            ++i;
        }
        return tb.toString();
    }

    public void loadMaps() {
        Connection con = null;
        int count = 0;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT mapId, mapName, eventType, configs, description FROM nexus_maps");
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                String type = rset.getString("eventType");
                FastList types = new FastList();
                for (EventType t : this.getTypes(type)) {
                    types.add(t);
                }
                EventMap map = new EventMap(rset.getInt("mapId"), rset.getString("mapName"), rset.getString("description"), (List<EventType>)types, this.loadSpawns(rset.getInt("mapId")), rset.getString("configs"));
                if (map.getMapDesc() == null) {
                    map.setMapDesc("");
                    map.setSaved(true);
                }
                map.loadConfigs();
                if (map.getGlobalId() > this._lastMapId) {
                    this._lastMapId = map.getGlobalId();
                }
                for (EventType t2 : types) {
                    this._maps.get((Object)t2).put(this._maps.get((Object)t2).size() + 1, map);
                }
                ++count;
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
        NexusLoader.debug((String)("Nexus Engine: Loaded " + count + " EventMaps."));
    }

    public List<EventSpawn> loadSpawns(int arenaId) {
        Connection con = null;
        FastList spawns = new FastList();
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT mapId, spawnId, x, y, z, teamId, type, note FROM nexus_spawns WHERE mapId = " + arenaId);
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                EventSpawn spawn = new EventSpawn(rset.getInt("mapId"), rset.getInt("spawnId"), new Loc(rset.getInt("x"), rset.getInt("y"), rset.getInt("z")), rset.getInt("teamId"), rset.getString("type"));
                String note = rset.getString("note");
                if (note != null) {
                    spawn.setNote(note);
                }
                spawn.setSaved(true);
                spawns.add(spawn);
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
        return spawns;
    }

    public void addSpawnToDb(EventSpawn spawn) {
        if (spawn.isSaved()) {
            return;
        }
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_spawns VALUES (" + spawn.getMapId() + ", " + spawn.getSpawnId() + ", " + spawn.getLoc().getX() + ", " + spawn.getLoc().getY() + ", " + spawn.getLoc().getZ() + ", " + spawn.getSpawnTeam() + ", '" + spawn.getSpawnType().toString() + "', " + (spawn.getNote() == null ? "''" : new StringBuilder().append("'").append(spawn.getNote()).append("'").toString()) + ")");
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
        spawn.setSaved(true);
    }

    public void removeSpawnFromDb(EventSpawn spawn) {
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_spawns WHERE mapId = " + spawn.getMapId() + " AND spawnId = " + spawn.getSpawnId());
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

    public void removeMapFromDb(EventMap map) {
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("DELETE FROM nexus_maps WHERE mapId = " + map.getGlobalId());
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

    public void addMapToDb(EventMap map, boolean force) {
        if (map.isSaved() && !force) {
            return;
        }
        map.setConfigs(EventConfig.getInstance().convertMapConfigs(map));
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_maps VALUES (" + map.getGlobalId() + ", '" + map.getMapName().replaceAll("'", "") + "', '" + this.convertToString(map.getEvents()) + "', '" + map.getConfigs() + "', '" + map.getMapDesc() + "')");
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
        map.setSaved(true);
    }

    public EventMap getNextMap(MiniEventManager manager, int lastId, EventMode mode) {
        EventType type = manager.getEventType();
        int nextMapId = lastId;
        EventMap map = null;
        for (int limit = 0; limit < this._maps.get((Object)type).size() + 99; ++limit) {
            map = this._maps.get((Object)type).get(++nextMapId);
            if (map == null) {
                nextMapId = 0;
                continue;
            }
            if (!manager.canRun(map) || mode.getDisMaps().contains((Object)map.getGlobalId())) {
                map = null;
                continue;
            }
            return map;
        }
        if (map == null) {
            NexusLoader.debug((String)("No map aviable for event " + type.getAltTitle() + " and mode " + mode.getModeName()), (Level)Level.WARNING);
        }
        return map;
    }

    public int getMapIndex(EventType event, EventMap map) {
        for (Map.Entry<Integer, EventMap> e : this._maps.get((Object)event).entrySet()) {
            if (e.getValue().getGlobalId() != map.getGlobalId()) continue;
            return e.getKey();
        }
        return 0;
    }

    public EventMap getMapById(int id) {
        for (Map<Integer, EventMap> map : this._maps.values()) {
            for (Map.Entry<Integer, EventMap> m : map.entrySet()) {
                if (m.getValue().getGlobalId() != id) continue;
                return m.getValue();
            }
        }
        return null;
    }

    public int getNewMapId() {
        return ++this._lastMapId;
    }

    public int getMapsCount(EventType type) {
        return this._maps.get((Object)type).size();
    }

    public Map<Integer, EventMap> getMaps(EventType type) {
        return this._maps.get((Object)type);
    }

    public boolean removeMap(int id) {
        EventMap map = this.getMapById(id);
        if (map == null) {
            return false;
        }
        this.removeMapFromDb(map);
        if (map.getGlobalId() >= this._lastMapId) {
            --this._lastMapId;
        }
        for (EventType type : map.getEvents()) {
            for (Map.Entry<Integer, EventMap> e : this._maps.get((Object)type).entrySet()) {
                if (e.getValue().getGlobalId() != id) continue;
                this._maps.get((Object)type).remove(e.getKey());
                this.reorganizeMaps(type);
            }
        }
        for (EventSpawn spawn : map.getSpawns()) {
            this.removeSpawnFromDb(spawn);
        }
        return true;
    }

    private void reorganizeMaps(EventType type) {
        Collection<EventMap> maps = this._maps.get((Object)type).values();
        FastMap mapping = new FastMap();
        for (EventMap map : maps) {
            mapping.put(mapping.size() + 1, map);
        }
        this._maps.put(type, (Map<Integer, EventMap>)mapping);
    }

    public void addMap(EventMap map) {
        for (EventType type : map.getEvents()) {
            this._maps.get((Object)type).put(this._maps.get((Object)type).size() + 1, map);
        }
    }

    public void addMapToEvent(EventMap map, EventType type) {
        FastList maps = new FastList();
        maps.addAll(this._maps.get((Object)type).values());
        maps.add(map);
        this._maps.get((Object)type).clear();
        int i = 0;
        for (EventMap m : maps) {
            this._maps.get((Object)type).put(i, m);
            ++i;
        }
    }

    public void removeMapFromEvent(EventMap map, EventType type) {
        for (Map.Entry<Integer, EventMap> e : this._maps.get((Object)type).entrySet()) {
            if (e.getValue().getGlobalId() != map.getGlobalId()) continue;
            this._maps.get((Object)type).remove(e.getKey());
        }
    }

    public List<EventMap> getMainEventMaps(EventType type) {
        if (!type.isRegularEvent()) {
            return null;
        }
        FastList maps = new FastList();
        maps.addAll(this._maps.get((Object)type).values());
        return maps;
    }

    public EventMap getMap(EventType type, String mapName) {
        for (EventMap map : this._maps.get((Object)type).values()) {
            if (!map.getMapName().toString().equals(mapName)) continue;
            return map;
        }
        return null;
    }

    public static final EventMapSystem getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final EventMapSystem _instance = new EventMapSystem();

        private SingletonHolder() {
        }
    }

}

