/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.engine.base.EventType
 *  cz.nxs.events.engine.team.EventTeam
 */
package cz.nxs.interf.callback;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import java.util.Collection;

public interface ICallback {
    public void eventStarts(int var1, EventType var2, Collection<? extends EventTeam> var3);

    public void playerKills(EventType var1, PlayerEventInfo var2, PlayerEventInfo var3);

    public void playerScores(EventType var1, PlayerEventInfo var2, int var3);

    public void playerFlagScores(EventType var1, PlayerEventInfo var2);

    public void playerKillsVip(EventType var1, PlayerEventInfo var2, PlayerEventInfo var3);

    public void eventEnded(int var1, EventType var2, Collection<? extends EventTeam> var3);
}

