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

public class SinglePlayersFightsDescription
extends EventDescription {
    public String getDescription(Map<String, ConfigModel> configs, int roundsCount, int teamsCount, int teamSize, int rejoinDelay, int timeLimit) {
        String text = "This is a free-for-all mini event. ";
        text = text + "You will fight against " + (teamsCount - 1) + " enemy player" + (teamsCount > 2 ? "s" : "") + " in a randomly chosen map. ";
        if (roundsCount > 1) {
            text = text + "Each match has " + roundsCount + " rounds, the winner of round (the player, who kills all his opponents) receives  1 score. ";
            text = text + "The player, who has the biggest score in the end of all rounds, wins the match. ";
        } else {
            text = text + "This event has only one round. If you die, the event ends for you. ";
            text = text + "The winner of the match is the player, who kills all his opponents. ";
        }
        text = text + "Your opponents will be selected automatically and don't worry, there's a protection, which will ensure that you will always fight only players whose level is similar to yours. ";
        text = text + "If the match doesn't end within " + timeLimit / 60000 + " minutes, it will be aborted automatically. ";
        text = text + "Also, after you visit this event, you will have to wait at least " + rejoinDelay / 60000 + " minutes to join this event again. ";
        return text;
    }
}

