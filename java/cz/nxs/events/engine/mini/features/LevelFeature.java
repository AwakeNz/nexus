/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 */
package cz.nxs.events.engine.mini.features;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.interf.PlayerEventInfo;

public class LevelFeature
extends AbstractFeature {
    private int minLevel = 1;
    private int maxLevel = 85;

    public LevelFeature(EventType event, PlayerEventInfo gm, String parametersString) {
        super(event);
        this.addConfig("MinLevel", "The min level required to participate this event mode.", 1);
        this.addConfig("MaxLevel", "The max level to participate this event mode.", 1);
        if (parametersString == null) {
            parametersString = "1,85";
        }
        this._params = parametersString;
        this.initValues();
    }

    @Override
    protected void initValues() {
        String[] params = this.splitParams(this._params);
        try {
            this.minLevel = Integer.parseInt(params[0]);
            this.maxLevel = Integer.parseInt(params[1]);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
    }

    public int getMinLevel() {
        return this.minLevel;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public boolean checkPlayer(PlayerEventInfo player) {
        if (player.getLevel() < this.minLevel) {
            return false;
        }
        if (player.getLevel() > this.maxLevel) {
            return false;
        }
        return true;
    }

    @Override
    public EventMode.FeatureType getType() {
        return EventMode.FeatureType.Level;
    }
}

