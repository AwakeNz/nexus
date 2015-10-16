/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.team;

import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import javolution.util.FastList;

public class OnePlayerTeam
extends EventTeam {
    public OnePlayerTeam(int teamId, String teamName) {
        super(teamId, teamName);
    }

    public PlayerEventInfo getPlayer() {
        if (this.getPlayers().isEmpty()) {
            return null;
        }
        return (PlayerEventInfo)this.getPlayers().getFirst();
    }

    @Override
    protected int getTeamSize() {
        return 1;
    }
}

