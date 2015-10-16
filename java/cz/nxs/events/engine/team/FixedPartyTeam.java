/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.team;

import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import javolution.util.FastList;

public class FixedPartyTeam
extends EventTeam {
    private final int _teamSize;

    public FixedPartyTeam(int teamId, String teamName, int size) {
        super(teamId, teamName);
        this._teamSize = size;
    }

    public FixedPartyTeam(int teamId, int size) {
        super(teamId, EventManager.getInstance().getTeamName(teamId) + " team");
        this._teamSize = size;
    }

    public PlayerEventInfo getLeader() {
        if (this.getPlayers().isEmpty()) {
            return null;
        }
        return (PlayerEventInfo)this.getPlayers().getFirst();
    }

    @Override
    protected int getTeamSize() {
        return this._teamSize;
    }
}

