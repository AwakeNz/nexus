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

public class KoreanDescription
extends EventDescription {
    public String getDescription(Map<String, ConfigModel> configs, int roundsCount, int teamsCount, int teamSize, int rejoinDelay, int timeLimit) {
        String text = "This is a team-based mini event. You need a party of exactly " + teamSize + " players (and be the party leader) to register. ";
        text = text + "You will fight against one enemy party in a randomly chosen map. ";
        text = text + "The fight is in the famous Korean-style - it's a set of continous 1v1 fights. If you die, you will be replaced by someone from your party. ";
        text = text + "The match ends when all players from one party are dead. ";
        text = text + "Your opponent will be selected automatically and don't worry, there's a protection, which will ensure that you will always fight only players whose level is similar to yours. ";
        text = text + "If the match doesn't end within " + timeLimit / 60000 + " minutes, it will be aborted automatically. ";
        text = text + "Also, after you visit this event, you will have to wait at least " + rejoinDelay / 60000 + " minutes to join this event again. ";
        return text;
    }
}

