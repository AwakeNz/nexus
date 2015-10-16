/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 */
package cz.nxs.l2j.delegate;

import cz.nxs.interf.PlayerEventInfo;

public interface IPartyData {
    public void addPartyMember(PlayerEventInfo var1);

    public void removePartyMember(PlayerEventInfo var1);

    public PlayerEventInfo getLeader();

    public int getLeadersId();

    public PlayerEventInfo[] getPartyMembers();

    public int getMemberCount();
}

