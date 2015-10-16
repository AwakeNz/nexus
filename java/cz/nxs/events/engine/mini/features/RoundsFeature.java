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

public class RoundsFeature
extends AbstractFeature {
    private int roundsAmmount = 1;

    public RoundsFeature(EventType event, PlayerEventInfo gm, String parametersString) {
        super(event);
        this.addConfig("RoundsAmmount", "The ammount of rounds for matches started under this mode (overrides the value from general configs). The value must be > 0 otherwise this config will be ignored.", 1);
        if (parametersString == null) {
            parametersString = "1";
        }
        this._params = parametersString;
        this.initValues();
    }

    @Override
    protected void initValues() {
        String[] params = this.splitParams(this._params);
        try {
            this.roundsAmmount = Integer.parseInt(params[0]);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
    }

    public int getRoundsAmmount() {
        return this.roundsAmmount;
    }

    @Override
    public boolean checkPlayer(PlayerEventInfo player) {
        return true;
    }

    @Override
    public EventMode.FeatureType getType() {
        return EventMode.FeatureType.Rounds;
    }
}

