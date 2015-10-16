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

public class DelaysFeature
extends AbstractFeature {
    private int rejoinDelay = 600000;

    public DelaysFeature(EventType event, PlayerEventInfo gm, String parametersString) {
        super(event);
        this.addConfig("RejoinDelay", "The delay player has to wait to rejoin this mode again (in ms). This delay is divided by 2 if the player has lost his last match.", 1);
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
            this.rejoinDelay = Integer.parseInt(params[0]);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
    }

    public int getRejoinDealy() {
        return this.rejoinDelay;
    }

    @Override
    public boolean checkPlayer(PlayerEventInfo player) {
        return true;
    }

    @Override
    public EventMode.FeatureType getType() {
        return EventMode.FeatureType.Delays;
    }
}

