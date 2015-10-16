/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.base;

import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.SpawnType;
import java.util.StringTokenizer;

public class EventSpawn {
    private Loc _loc;
    private int _spawnId;
    private int _teamId;
    private int _mapId;
    private SpawnType _type;
    private int _fenceWidth;
    private int _fenceLength;
    private String _note = null;
    private boolean _saved;

    public EventSpawn(int mapId, int spawnId, Loc loc, int teamId, String type) {
        this._loc = loc;
        this._spawnId = spawnId;
        this._teamId = teamId;
        this._mapId = mapId;
        this._type = EventSpawn.assignSpawnType(type);
    }

    private static SpawnType assignSpawnType(String typeString) {
        for (SpawnType st : SpawnType.values()) {
            if (!st.toString().equalsIgnoreCase(typeString)) continue;
            return st;
        }
        return SpawnType.Regular;
    }

    public SpawnType getSpawnType() {
        return this._type;
    }

    public int getDoorId() {
        if (this._type == SpawnType.Door) {
            return this._loc.getX();
        }
        return -1;
    }

    public int getNpcId() {
        try {
            if (this._type == SpawnType.Npc) {
                return Integer.parseInt(this._note);
            }
            return -1;
        }
        catch (Exception e) {
            return -1;
        }
    }

    public Loc getLoc() {
        return new Loc(this._loc.getX(), this._loc.getY(), this._loc.getZ(), this._loc.getHeading());
    }

    public int getMapId() {
        return this._mapId;
    }

    public void setType(String s) {
        this._type = EventSpawn.assignSpawnType(s);
        this._saved = false;
    }

    public int getSpawnTeam() {
        return this._teamId;
    }

    public int getSpawnId() {
        return this._spawnId;
    }

    public void setNote(String note) {
        this._note = note;
        if (this._type == SpawnType.Fence) {
            try {
                StringTokenizer st = new StringTokenizer(note, " ");
                this._fenceWidth = Integer.parseInt(st.nextToken());
                this._fenceLength = Integer.parseInt(st.nextToken());
            }
            catch (Exception e) {
                EventManager.getInstance().debug("The value for fence's length / weight can be only a number! Reseting back to default values.");
                this._fenceWidth = 100;
                this._fenceLength = 100;
            }
        }
        this._saved = false;
    }

    public void setId(int i) {
        this._spawnId = i;
        this._saved = false;
    }

    public void setTeamId(int i) {
        this._teamId = i;
        this._saved = false;
    }

    public void setX(int i) {
        Loc newLoc;
        this._loc = newLoc = new Loc(i, this._loc.getY(), this._loc.getZ());
        this._saved = false;
    }

    public void setY(int i) {
        Loc newLoc;
        this._loc = newLoc = new Loc(this._loc.getX(), i, this._loc.getZ());
        this._saved = false;
    }

    public void setZ(int i) {
        Loc newLoc;
        this._loc = newLoc = new Loc(this._loc.getX(), this._loc.getY(), i);
        this._saved = false;
    }

    public int getImportance() {
        String note = this.getNote();
        try {
            return Integer.parseInt(note.split("-")[0]);
        }
        catch (Exception e) {
            this.setNote("1-false");
            return this.getImportance();
        }
    }

    public boolean canRespawnHere() {
        String note = this.getNote();
        try {
            return Boolean.parseBoolean(note.split("-")[1]);
        }
        catch (Exception e) {
            this.setNote("1-false");
            return this.canRespawnHere();
        }
    }

    public void setImportance(int i) {
        String respawnHere;
        String importance;
        String note = this.getNote();
        try {
            importance = note.split("-")[0];
            respawnHere = note.split("-")[1];
        }
        catch (Exception e) {
            this.setNote("1-false");
            note = this.getNote();
            importance = note.split("-")[0];
            respawnHere = note.split("-")[1];
        }
        importance = String.valueOf(i);
        note = importance + "-" + respawnHere;
        this.setNote(note);
    }

    public void setRespawnHere(boolean b) {
        String respawnHere;
        String importance;
        String note = this.getNote();
        try {
            importance = note.split("-")[0];
            respawnHere = note.split("-")[1];
        }
        catch (Exception e) {
            this.setNote("1-false");
            note = this.getNote();
            importance = note.split("-")[0];
            respawnHere = note.split("-")[1];
        }
        respawnHere = String.valueOf(b);
        note = importance + "-" + respawnHere;
        this.setNote(note);
    }

    public int getRadius() {
        try {
            int radius = Integer.parseInt(this._note);
            return radius;
        }
        catch (Exception e) {
            return -1;
        }
    }

    public int getFenceWidth() {
        if (this._type == SpawnType.Fence) {
            return this._fenceWidth;
        }
        return 0;
    }

    public int getFenceLength() {
        if (this._type == SpawnType.Fence) {
            return this._fenceLength;
        }
        return 0;
    }

    public boolean isSaved() {
        return this._saved;
    }

    public void setSaved(boolean b) {
        this._saved = b;
    }

    public String getNote() {
        return this._note;
    }
}

