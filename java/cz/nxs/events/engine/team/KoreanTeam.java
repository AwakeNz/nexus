/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.team;

import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;

public class KoreanTeam
extends EventTeam {
    private Map<Integer, Integer> _players;
    private int _order;
    private int _nextPlayer;
    private PlayerEventInfo _fighting;

    public KoreanTeam(int teamId, String teamName) {
        super(teamId, teamName);
        this._players = new FastMap(this.getTeamSize());
        this._order = 0;
        this._nextPlayer = 0;
        this._fighting = null;
    }

    public boolean isFighting(PlayerEventInfo player) {
        return this._fighting != null && this._fighting.getPlayersId() == player.getPlayersId();
    }

    @Override
    protected int getTeamSize() {
        return 4;
    }

    @Override
    public void addPlayer(PlayerEventInfo pi, boolean init) {
        super.addPlayer(pi, init);
        ++this._order;
        this._players.put(this._order, pi.getPlayersId());
    }

    public PlayerEventInfo getNextPlayer() {
        if (this.getPlayers().isEmpty()) {
            return null;
        }
        int next = 0;
        do {
            ++this._nextPlayer;
        } while ((next = this._players.get(this._nextPlayer).intValue()) == 0);
        for (PlayerEventInfo pi : this.getPlayers()) {
            if (pi.getPlayersId() != next) continue;
            this._fighting = pi;
            return pi;
        }
        return null;
    }
}

