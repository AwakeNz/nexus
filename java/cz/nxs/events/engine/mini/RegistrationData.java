/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.PartyData
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.mini;

import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.IPlayerBase;
import java.util.List;
import javolution.util.FastList;

public class RegistrationData {
    private FastList<PlayerEventInfo> _players;
    private boolean _choosen = false;

    public RegistrationData(FastList<PlayerEventInfo> players) {
        this._players = players;
    }

    public PlayerEventInfo getKeyPlayer() {
        return (PlayerEventInfo)this._players.getFirst();
    }

    public List<PlayerEventInfo> getPlayers() {
        return this._players;
    }

    public PartyData getParty() {
        if (this.getKeyPlayer().isOnline()) {
            return this.getKeyPlayer().getParty();
        }
        return null;
    }

    public void register(boolean isRegistered, MiniEventManager registeredEvent) {
        for (PlayerEventInfo pi : this._players) {
            pi.setIsRegisteredToMiniEvent(isRegistered, registeredEvent);
            if (isRegistered) continue;
            CallBack.getInstance().getPlayerBase().eventEnd(pi);
        }
    }

    public void message(String msg, boolean screen) {
        for (PlayerEventInfo pi : this._players) {
            if (screen) {
                pi.screenMessage(msg, "", true);
                continue;
            }
            pi.sendMessage(msg);
        }
    }

    public int getAverageLevel() {
        int i = 0;
        for (PlayerEventInfo player : this._players) {
            i+=player.getLevel();
        }
        i = Math.round(i / this._players.size());
        return i;
    }

    public boolean isChosen() {
        return this._choosen;
    }

    public void setIsChosen(boolean b) {
        this._choosen = b;
    }
}

