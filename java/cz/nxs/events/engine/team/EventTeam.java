/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.PartyData
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.team;

import cz.nxs.events.engine.EventManager;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.PartyData;
import javolution.util.FastList;

public class EventTeam {
    protected int _teamId;
    private String _teamName;
    private String _fullName;
    private FastList<PlayerEventInfo> _players;
    private int _levelSum;
    private int _averageLevel;
    public int _nameColor;
    private int _kills;
    private int _deaths;
    private int _score;
    private int _finalPosition;

    public EventTeam(int teamId) {
        this._teamId = teamId;
        this.initializePlayers();
    }

    public EventTeam(int teamId, String teamName, String fullName) {
        this._teamId = teamId;
        this._teamName = teamName;
        this._fullName = fullName;
        this._nameColor = EventManager.getInstance().getTeamColorForName(teamId);
        this._levelSum = 0;
        this._averageLevel = 0;
        this._kills = 0;
        this._deaths = 0;
        this._score = 0;
        this._finalPosition = -1;
        this.initializePlayers();
    }

    public EventTeam(int teamId, String teamName) {
        this(teamId, teamName, teamName);
    }

    protected void initializePlayers() {
        this._players = this.getTeamSize() > 0 ? new FastList(this.getTeamSize()) : new FastList();
    }

    public synchronized boolean removePlayer(PlayerEventInfo pi) {
        return this._players.remove((Object)pi);
    }

    public void addPlayer(PlayerEventInfo pi, boolean init) {
        FastList<PlayerEventInfo> fastList = this._players;
        synchronized (fastList) {
            this._players.add((Object)pi);
        }
        if (init) {
            this.initPlayer(pi);
        }
        this._levelSum+=pi.getLevel();
    }

    public void calcAverageLevel() {
        this._averageLevel = (int)((double)this._levelSum / (double)this._players.size());
    }

    public int getAverageLevel() {
        return this._averageLevel;
    }

    protected void initPlayer(PlayerEventInfo pi) {
        pi.setEventTeam(this);
        pi.setNameColor(this.getNameColor());
        pi.broadcastUserInfo();
    }

    public void message(String msg, String name, boolean special) {
        for (PlayerEventInfo pi : this._players) {
            pi.screenMessage(msg, name, special);
        }
    }

    public void createParties() {
        int count = 0;
        int size = this.getPlayers().size();
        PartyData party = null;
        if (size <= 1) {
            return;
        }
        for (PlayerEventInfo player : this.getPlayers()) {
            if (count % 9 == 0 && size - count != 1) {
                party = new PartyData(player);
            } else if (count % 9 < 9) {
                party.addPartyMember(player);
            }
            ++count;
        }
    }

    public FastList<PlayerEventInfo> getPlayers() {
        return this._players;
    }

    public int getTeamId() {
        return this._teamId;
    }

    public int getDeaths() {
        return this._deaths;
    }

    public int getKills() {
        return this._kills;
    }

    public int getScore() {
        return this._score;
    }

    public void raiseScore(int count) {
        this._score+=count;
    }

    public int getNameColor() {
        return this._nameColor;
    }

    public String getTeamName() {
        return this._teamName;
    }

    public String getFullName() {
        return this._fullName;
    }

    public void raiseDeaths(int count) {
        this._deaths+=count;
    }

    public void raiseKills(int count) {
        this._kills+=count;
    }

    public void resetDeaths() {
        this._deaths = 0;
    }

    public void resetScore() {
        this._score = 0;
    }

    protected int getTeamSize() {
        return -1;
    }

    public String getNameColorInString() {
        return EventManager.getInstance().getTeamColorForHtml(this._teamId);
    }

    public void setFinalPosition(int pos) {
        this._finalPosition = pos;
    }

    public int getFinalPosition() {
        return this._finalPosition;
    }
}

