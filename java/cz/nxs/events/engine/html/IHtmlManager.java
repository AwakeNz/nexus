/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.NpcData
 */
package cz.nxs.events.engine.html;

import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.NpcData;

public interface IHtmlManager {
    public boolean showNpcHtml(PlayerEventInfo var1, NpcData var2);

    public boolean onBypass(PlayerEventInfo var1, String var2);

    public boolean onCbBypass(PlayerEventInfo var1, String var2);
}

