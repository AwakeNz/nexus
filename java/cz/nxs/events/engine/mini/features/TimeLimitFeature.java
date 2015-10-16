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

public class TimeLimitFeature
extends AbstractFeature {
    private int timeLimit = 600000;

    public TimeLimitFeature(EventType event, PlayerEventInfo gm, String parametersString) {
        super(event);
        this.addConfig("TimeLimit", "Event's time limit, after which the event will be automatically ended (in ms).", 1);
        if (parametersString == null) {
            parametersString = "600000";
        }
        this._params = parametersString;
        this.initValues();
    }

    @Override
    protected void initValues() {
        String[] params = this.splitParams(this._params);
        try {
            this.timeLimit = Integer.parseInt(params[0]);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
    }

    public int getTimeLimit() {
        return this.timeLimit;
    }

    @Override
    public boolean checkPlayer(PlayerEventInfo player) {
        return true;
    }

    @Override
    public EventMode.FeatureType getType() {
        return EventMode.FeatureType.TimeLimit;
    }
}

