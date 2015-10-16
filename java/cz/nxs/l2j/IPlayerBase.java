/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastMap
 */
package cz.nxs.l2j;

import cz.nxs.interf.PlayerEventInfo;
import javolution.util.FastMap;

public interface IPlayerBase {
    public PlayerEventInfo addInfo(PlayerEventInfo var1);

    public PlayerEventInfo getPlayer(int var1);

    public FastMap<Integer, PlayerEventInfo> getPs();

    public void eventEnd(PlayerEventInfo var1);

    public void playerDisconnected(PlayerEventInfo var1);

    public void deleteInfo(int var1);
}

