/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.L2Party
 *  com.l2jserver.gameserver.model.L2Party$messageType
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  cz.nxs.l2j.delegate.IPartyData
 *  javolution.util.FastList
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.delegate.IPartyData;
import javolution.util.FastList;

public class PartyData
implements IPartyData {
    private L2Party _party;

    public PartyData(L2Party p) {
        this._party = p;
    }

    public PartyData(PlayerEventInfo leader) {
        leader.getOwner().setParty(new L2Party(leader.getOwner(), 1));
        this._party = leader.getOwner().getParty();
    }

    public L2Party getParty() {
        return this._party;
    }

    public boolean exists() {
        return this._party != null;
    }

    public void addPartyMember(PlayerEventInfo player) {
        player.getOwner().joinParty(this._party);
    }

    public void removePartyMember(PlayerEventInfo player) {
        this._party.removePartyMember(player.getOwner(), L2Party.messageType.None);
    }

    public PlayerEventInfo getLeader() {
        return this._party.getLeader().getEventInfo();
    }

    public PlayerEventInfo[] getPartyMembers() {
        FastList players = new FastList();
        for (L2PcInstance player : this._party.getPartyMembers()) {
            players.add(player.getEventInfo());
        }
        return players.toArray(new PlayerEventInfo[players.size()]);
    }

    public int getMemberCount() {
        return this._party.getMemberCount();
    }

    public int getLeadersId() {
        return this._party.getPartyLeaderOID();
    }
}

