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

public class TvTDescription
extends EventDescription {
    public String getDescription(Map<String, ConfigModel> configs) {
        String text = "" + this.getInt(configs, "teamsCount") + " teams fighting against each other. ";
        text = text + "Gain score by killing your opponents";
        if (this.getInt(configs, "killsForReward") > 0) {
            text = text + " (at least " + this.getInt(configs, "killsForReward") + " kill(s) is required to receive a reward)";
        }
        text = this.getBoolean(configs, "waweRespawn") ? text + " and dead players are resurrected by an advanced wawe-spawn engine each " + this.getInt(configs, "resDelay") + " seconds" : text + " and if you die, you will be resurrected in " + this.getInt(configs, "resDelay") + " seconds";
        if (this.getBoolean(configs, "createParties")) {
            text = text + ". The event automatically creates parties on start";
        }
        text = text + ".";
        return text;
    }
}

