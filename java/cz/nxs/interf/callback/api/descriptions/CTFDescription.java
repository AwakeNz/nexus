/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.engine.base.ConfigModel
 *  cz.nxs.events.engine.base.description.EventDescription
 */
package cz.nxs.interf.callback.api.descriptions;

import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.description.EventDescription;
import java.util.Map;

public class CTFDescription
extends EventDescription {
    public String getDescription(Map<String, ConfigModel> configs) {
        String text = "There are " + this.getInt(configs, "teamsCount") + " teams; in order to score you need to steal enemy team's flag and bring it back your team's base (to the flag holder). ";
        if (this.getInt(configs, "flagReturnTime") > -1) {
            text = text + "If you hold the flag and don't manage to score within " + this.getInt(configs, "flagReturnTime") / 1000 + " seconds, the flag will be returned back to enemy's flag holder. ";
        }
        text = this.getBoolean(configs, "waweRespawn") ? text + "Dead players are resurrected by an advanced wawe-spawn engine each " + this.getInt(configs, "resDelay") + " seconds." : text + "If you die, you will be resurrected in " + this.getInt(configs, "resDelay") + " seconds. ";
        if (this.getBoolean(configs, "createParties")) {
            text = text + "The event automatically creates parties on start.";
        }
        return text;
    }
}

