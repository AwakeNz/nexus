/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 */
package cz.nxs.events.engine.mini.tournament;

import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.mini.RegistrationData;
import cz.nxs.interf.PlayerEventInfo;
import java.util.List;

public class Tournament {
    private static boolean _active = false;
    private static Tournament _tournament = null;
    private static MiniEventManager _event = null;

    public static Tournament getTournament() {
        return _tournament;
    }

    public static void setTournamentEvent(MiniEventManager event) {
        _event = event;
    }

    public static void startTournament(PlayerEventInfo gm) {
        if (!(_event == null || _event.isTournamentActive())) {
            Tournament tournament;
            _tournament = tournament = new Tournament();
            _event.setTournamentActive(true);
            _active = true;
        } else {
            gm.sendMessage("You must first select an event.");
        }
    }

    public static void register(PlayerEventInfo player) {
        if (_active) {
            _event.registerTeam(player);
        }
    }

    public List<RegistrationData> getRegistered() {
        return _event.getRegistered();
    }

    public MiniEventManager getEvent() {
        return _event;
    }
}

