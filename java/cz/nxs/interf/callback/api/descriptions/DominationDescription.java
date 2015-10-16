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

public class DominationDescription
extends EventDescription {
    public String getDescription(Map<String, ConfigModel> configs) {
        String text = "" + this.getInt(configs, "teamsCount") + " teams fighting against each other. ";
        text = text + "The goal of this event is to capture and hold ";
        text = text + "a zone. The zone is represented by an NPC and to capture it, you need to stand near the NPC and ensure that no other enemies are standing near the zone too. ";
        if (this.getInt(configs, "killsForReward") > 0) {
            text = text + "At least " + this.getInt(configs, "killsForReward") + " kill(s) is required to receive a reward. ";
        }
        if (this.getInt(configs, "scoreForReward") > 0) {
            text = text + "At least " + this.getInt(configs, "scoreForReward") + " score (obtained when your team owns the zone and you stand near it) is required to receive a reward. ";
        }
        text = this.getBoolean(configs, "waweRespawn") ? text + "Dead players are resurrected by an advanced wawe-spawn engine each " + this.getInt(configs, "resDelay") + " seconds. " : text + "If you die, you will get resurrected in " + this.getInt(configs, "resDelay") + " seconds. ";
        if (this.getBoolean(configs, "createParties")) {
            text = text + "The event automatically creates parties on start.";
        }
        return text;
    }
}

