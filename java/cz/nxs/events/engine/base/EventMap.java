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
package cz.nxs.events.engine.base;

import cz.nxs.events.Configurable;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.EventMapSystem;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.interf.PlayerEventInfo;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class EventMap {
    private int _globalId;
    private boolean _saved = true;
    private String _mapName;
    private String _configs;
    private String _mapDesc;
    private int _highestSpawnId;
    private List<EventType> _events;
    private List<EventSpawn> _spawns;
    private Map<Integer, Map<SpawnType, Integer>> _history;
    private Map<Integer, EventSpawn> _lastSpawns;
    private List<EventSpawn> _doorsSpawn;
    private boolean _hasDoors;
    private Map<EventType, Map<String, ConfigModel>> _configModels;
    public static Comparator<EventSpawn> compareByIdAsc = new Comparator<EventSpawn>(){

        @Override
        public int compare(EventSpawn s1, EventSpawn s2) {
            int id2;
            int id1 = s1.getSpawnId();
            return id1 == (id2 = s2.getSpawnId()) ? 0 : (id1 < id2 ? -1 : 1);
        }
    };
    public static Comparator<EventSpawn> compareByIdDesc = new Comparator<EventSpawn>(){

        @Override
        public int compare(EventSpawn s1, EventSpawn s2) {
            int id2;
            int id1 = s1.getSpawnId();
            return id1 == (id2 = s2.getSpawnId()) ? 0 : (id1 > id2 ? -1 : 1);
        }
    };
    public static Comparator<EventSpawn> compareByType = new Comparator<EventSpawn>(){

        @Override
        public int compare(EventSpawn s1, EventSpawn s2) {
            SpawnType t1 = s1.getSpawnType();
            SpawnType t2 = s2.getSpawnType();
            return t1.compareTo(t2);
        }
    };

    public EventMap(int mapId, String mapName, String mapDesc, List<EventType> events, List<EventSpawn> spawns, String configs) {
        this._globalId = mapId;
        this._mapName = mapName;
        this._mapDesc = mapDesc;
        this._configs = configs;
        this._spawns = new FastList();
        this._history = new FastMap();
        this._lastSpawns = new FastMap();
        this._events = events;
        if (this._events == null) {
            NexusLoader.debug((String)"_events null in EventMap constructor");
            this._events = new FastList();
        }
        this._configModels = new FastMap();
        this.addSpawns(spawns);
        this.initDoors();
    }

    public void loadConfigs() {
        for (EventType event : this._events) {
            this.initEventsConfigs(event);
        }
        EventConfig.getInstance().loadMapConfigs(this, this._configs);
    }

    private void initEventsConfigs(EventType event) {
        this._configModels.put(event, (Map<String, ConfigModel>)new FastMap());
        Configurable conf = EventManager.getInstance().getEvent(event);
        if (conf == null || conf.getMapConfigs() == null) {
            return;
        }
        for (ConfigModel config : conf.getMapConfigs().values()) {
            this._configModels.get((Object)event).put(config.getKey(), new ConfigModel(config.getKey(), config.getValue(), config.getDesc(), config.getInput()));
        }
    }

    private void deleteEventsConfigs(EventType event) {
        this._configModels.remove((Object)event);
    }

    public void setConfigValue(EventType event, String key, String value, boolean addToValue) {
        try {
            if (!this._configModels.containsKey((Object)event)) {
                NexusLoader.debug((String)("Trying to set MapConfig's: map ID " + this.getGlobalId() + " event " + event.getAltTitle() + ", config's key = " + key + ". The map doesn't have such event."));
                return;
            }
            if (this._configModels.get((Object)event).get(key) == null) {
                NexusLoader.debug((String)("Trying to set MapConfig's: map ID " + this.getGlobalId() + " event " + event.getAltTitle() + ", config's key = " + key + ", but this config doesn't exist for that map! Skipping..."));
                return;
            }
            if (!addToValue) {
                this._configModels.get((Object)event).get(key).setValue(value);
            } else {
                this._configModels.get((Object)event).get(key).addToValue(value);
            }
        }
        catch (Exception e) {
            NexusLoader.debug((String)("Error setting map config's value to " + value + ", config's key = " + key + ", map ID = " + this.getGlobalId() + " and event = " + event.getAltTitle()), (Level)Level.WARNING);
            e.printStackTrace();
        }
    }

    public Map<EventType, Map<String, ConfigModel>> getConfigModels() {
        return this._configModels;
    }

    public ConfigModel getConfigModel(EventType event, String key) {
        try {
            if (!this._configModels.containsKey((Object)event)) {
                NexusLoader.debug((String)("Trying to set MapConfig's value: map ID " + this.getGlobalId() + " event " + event.getAltTitle() + ", config's key = " + key + ". The map doesn't have such event."));
                return null;
            }
            return this._configModels.get((Object)event).get(key);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addSpawns(List<EventSpawn> spawns) {
        if (spawns == null) {
            return;
        }
        this._spawns.addAll(spawns);
        for (EventSpawn spawn : spawns) {
            if (!this._history.containsKey(spawn.getSpawnTeam())) {
                this._history.put(spawn.getSpawnTeam(), (Map<SpawnType, Integer>)new FastMap());
            }
            if (this._history.get(spawn.getSpawnTeam()).containsKey((Object)spawn.getSpawnType())) continue;
            this._history.get(spawn.getSpawnTeam()).put(spawn.getSpawnType(), 0);
        }
        this.recalcLastSpawnId();
    }

    public FastList<EventSpawn> getSpawns(int teamId, SpawnType type) {
        FastList temp = new FastList();
        for (EventSpawn spawn : this._spawns) {
            if (spawn.getSpawnTeam() != teamId && teamId != -1 || spawn.getSpawnType() != type) continue;
            temp.add((Object)spawn);
        }
        return temp;
    }

    public FastList<EventSpawn> getMarkers(int teamId) {
        return this.getSpawns(teamId, SpawnType.Radar);
    }

    public void clearHistory(int teamId, SpawnType type) {
        if (teamId == -1) {
            for (Map.Entry<Integer, Map<SpawnType, Integer>> e : this._history.entrySet()) {
                this._history.get(e.getKey()).put(type, 0);
            }
        } else {
            this._history.get(teamId).put(type, 0);
        }
    }

    public EventSpawn getNextSpawn(int teamId, SpawnType type) {
        FastList<EventSpawn> spawns = this.getSpawns(teamId, type);
        if (spawns == null || spawns.isEmpty()) {
            return null;
        }
        if (teamId == -1) {
            teamId = 0;
        }
        int lastId = 0;
        try {
            lastId = this._history.get(teamId).get((Object)type);
        }
        catch (NullPointerException e) {
            lastId = 0;
        }
        EventSpawn nextSpawn = null;
        for (EventSpawn spawn : spawns) {
            if (spawn.getSpawnId() <= lastId) continue;
            nextSpawn = spawn;
            break;
        }
        if (nextSpawn == null) {
            nextSpawn = (EventSpawn)spawns.getFirst();
        }
        lastId = nextSpawn.getSpawnId();
        if (!this._history.containsKey(teamId)) {
            this._history.put(teamId, (Map<SpawnType, Integer>)new FastMap());
        }
        this._history.get(teamId).put(type, lastId);
        return nextSpawn;
    }

    public List<EventSpawn> getSpawns() {
        return this._spawns;
    }

    public EventSpawn getSpawn(int spawnId) {
        for (EventSpawn spawn : this._spawns) {
            if (spawn.getSpawnId() != spawnId) continue;
            return spawn;
        }
        return null;
    }

    public boolean removeSpawn(int spawnId, boolean db) {
        for (EventSpawn spawn : this._spawns) {
            if (spawn.getSpawnId() != spawnId) continue;
            this._spawns.remove(spawn);
            if (this.getSpawns(spawn.getSpawnTeam(), spawn.getSpawnType()).isEmpty()) {
                this._history.remove((Object)spawn.getSpawnType());
            }
            if (db) {
                EventMapSystem.getInstance().removeSpawnFromDb(spawn);
            }
            this.recalcLastSpawnId();
            return true;
        }
        return false;
    }

    private void recalcLastSpawnId() {
        int highestId = 0;
        for (EventSpawn spawn : this._spawns) {
            if (spawn.getSpawnId() <= highestId) continue;
            highestId = spawn.getSpawnId();
        }
        this._highestSpawnId = highestId;
    }

    private void initDoors() {
        for (EventSpawn spawn : this._spawns) {
            if (spawn.getSpawnType() != SpawnType.Door) continue;
            if (this._doorsSpawn == null) {
                this._doorsSpawn = new FastList();
            }
            this._doorsSpawn.add(spawn);
            this._hasDoors = true;
        }
    }

    public String[] getAviableConfigs(EventType type) {
        if (type != EventType.Unassigned && this._events.contains((Object)type)) {
            Configurable event = EventManager.getInstance().getEvent(type, 1);
            if (event == null) {
                System.out.println("null event at getAviableConfigs(EventType)");
                return null;
            }
            return event.getMapConfigs().keySet().toArray(new String[event.getMapConfigs().size()]);
        }
        System.out.println("getAviableConfigs - type " + type.getAltTitle() + " returned null.");
        return null;
    }

    public boolean hasDoor() {
        return this._hasDoors;
    }

    public List<EventSpawn> getDoors() {
        return this._doorsSpawn;
    }

    public EventSpawn getLastSpawn(int teamId) {
        return this._lastSpawns.get(teamId);
    }

    public String getMapName() {
        return this._mapName;
    }

    public String getMapDesc() {
        return this._mapDesc;
    }

    public int getGlobalId() {
        return this._globalId;
    }

    public List<EventType> getEvents() {
        return this._events;
    }

    public String getConfigs() {
        return this._configs;
    }

    public void setConfigs(String s) {
        this._configs = s;
    }

    public void setMapName(String name) {
        this._mapName = name;
        this._saved = false;
    }

    public void setMapDesc(String desc) {
        this._mapDesc = desc;
        this._saved = false;
    }

    public int getNewSpawnId() {
        return this._highestSpawnId + 1;
    }

    public void addEvent(EventType type) {
        this._events.add(type);
        EventMapSystem.getInstance().addMapToEvent(this, type);
        this.initEventsConfigs(type);
        this._saved = false;
    }

    public void removeEvent(EventType type) {
        if (this._events.remove((Object)type)) {
            EventMapSystem.getInstance().removeMapFromEvent(this, type);
            this.deleteEventsConfigs(type);
            if (this._events.isEmpty()) {
                this._events.add(EventType.Unassigned);
                EventMapSystem.getInstance().addMapToEvent(this, EventType.Unassigned);
            }
            this._saved = false;
        }
    }

    public boolean isSaved() {
        return this._saved;
    }

    public void setSaved(boolean b) {
        this._saved = b;
        if (this._saved) {
            this.initDoors();
        }
    }

    public boolean checkForSpawns(SpawnType type, int teamId, int count) {
        try {
            return this.getSpawns(teamId, type).size() >= count;
        }
        catch (NullPointerException npe) {
            return false;
        }
    }

    public String getMissingSpawns() {
        TextBuilder tb = new TextBuilder();
        for (EventType type : this.getEvents()) {
            Configurable event = EventManager.getInstance().getEvent(type);
            if (event == null) {
                return "";
            }
            if (!type.isRegularEvent()) continue;
            tb.append(event.getMissingSpawns(this));
        }
        return tb.toString();
    }

    public String getNotWorkingEvents() {
        TextBuilder tb = new TextBuilder();
        for (EventType type : this.getEvents()) {
            if (!type.isMiniEvent()) continue;
            String temp = "";
            for (MiniEventManager manager : EventManager.getInstance().getMiniEvents().get((Object)type).values()) {
                temp = temp + manager.getMissingSpawns(this);
            }
            if (temp.length() <= 0) continue;
            tb.append("<font color=LEVEL>" + type.getHtmlTitle() + "</font><br1>");
            tb.append(temp);
            tb.append("<br>");
        }
        return tb.toString();
    }

    public void checkMap(PlayerEventInfo gm) {
        for (EventType type : this.getEvents()) {
            if (type == EventType.Classic_1v1 || type == EventType.Classic_2v2 || type == EventType.PartyvsParty || type == EventType.TvT || type == EventType.TvTAdv || type == EventType.MiniTvT) {
                if (!this.checkForSpawns(SpawnType.Regular, 1, 1)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1, count 1");
                }
                if (this.checkForSpawns(SpawnType.Regular, 2, 1)) continue;
                gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 2, count 1");
                continue;
            }
            if (type == EventType.CTF) {
                if (!this.checkForSpawns(SpawnType.Flag, 1, 1)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type FLAG, team 1, count 1.");
                }
                if (!this.checkForSpawns(SpawnType.Flag, 2, 1)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type FLAG, team 2, count 1.");
                }
                if (!this.checkForSpawns(SpawnType.Regular, 1, 1)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1, count 1 or more.");
                }
                if (this.checkForSpawns(SpawnType.Regular, 2, 1)) continue;
                gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 2, count 1 or more.");
                continue;
            }
            if (type == EventType.DM || type == EventType.LMS) {
                if (this.checkForSpawns(SpawnType.Regular, 1, -1)) continue;
                gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, count 1 or more. (team doesn't matter)");
                continue;
            }
            if (type == EventType.Mutant || type == EventType.Zombies) {
                if (!this.checkForSpawns(SpawnType.Regular, 1, -1)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR,count 1 or more. (team doesn't matter");
                }
                if (this.checkForSpawns(SpawnType.Zombie, 1, -1)) continue;
                gm.sendMessage(type.getAltTitle() + ": Missing spawn type ZOMBIE, count 1 or more.");
                continue;
            }
            if (type == EventType.Korean) {
                if (!this.checkForSpawns(SpawnType.Safe, 1, 1)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type SAFE, team 1, count 1. This is initial spawn for Players.");
                }
                if (!this.checkForSpawns(SpawnType.Safe, 2, 1)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type SAFE, team 2, count 1. This is initial spawn for Players.");
                }
                if (!this.checkForSpawns(SpawnType.Regular, 1, 4)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1, count 4 (for each player one spot)");
                }
                if (this.checkForSpawns(SpawnType.Regular, 2, 4)) continue;
                gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 2, count 4 (for each player one spot)");
                continue;
            }
            if (type == EventType.Underground_Coliseum) {
                if (!this.checkForSpawns(SpawnType.Regular, 1, 4)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1. count 1. This is initial spawn used to teleport players before event starts.");
                }
                if (!this.checkForSpawns(SpawnType.Regular, 2, 4)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 2, count 1. This is initial spawn used to teleport players before event starts.");
                }
                if (!this.checkForSpawns(SpawnType.Safe, 1, 1)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type SAFE, team 1, count 1. This is respawn spot.");
                }
                if (this.checkForSpawns(SpawnType.Safe, 2, 1)) continue;
                gm.sendMessage(type.getAltTitle() + ": Missing spawn type SAFE, team 2, count 1. This is respawn spot.");
                continue;
            }
            if (type == EventType.RBHunt) {
                if (!this.checkForSpawns(SpawnType.Boss, -1, 1)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type BOSS count 1.");
                }
                if (!this.checkForSpawns(SpawnType.Regular, 1, 1)) {
                    gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1, count 1 or more.");
                }
                if (this.checkForSpawns(SpawnType.Regular, 2, 1)) continue;
                gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 2, count 1 or more.");
                continue;
            }
            if (type != EventType.SurvivalArena) continue;
            boolean round1 = false;
            boolean round2 = false;
            boolean round3 = false;
            if (!round1) {
                gm.sendMessage(type.getAltTitle() + ": Missing spawn type MONSTER for FIRST round!");
            }
            if (!round2) {
                gm.sendMessage(type.getAltTitle() + ": Missing spawn type MONSTER for SECOND round!");
            }
            if (!round3) {
                gm.sendMessage(type.getAltTitle() + ": Missing spawn type MONSTER for FINAL round!");
            }
            if (this.checkForSpawns(SpawnType.Regular, 1, 1)) continue;
            gm.sendMessage(type.getAltTitle() + ": Missing spawn type REGULAR, team 1, count 1.");
        }
    }

}

