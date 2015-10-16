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

public class MassDominationDescription
extends EventDescription {
    public String getDescription(Map<String, ConfigModel> configs) {
        String text = "" + this.getInt(configs, "teamsCount") + " teams fighting against each other. ";
        text = text + "There are " + this.getInt(configs, "countOfZones") + " zones, each represented by an NPC. ";
        text = text + "In order to gain a score, your team must own at least " + this.getInt(configs, "zonesToOwnToScore") + " zones. ";
        text = text + "To own a zone, your team must get close to each of these zones and kill all other enemies standing near the zone too. ";
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

