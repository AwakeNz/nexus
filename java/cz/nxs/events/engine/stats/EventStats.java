/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.ShowBoardData
 */
package cz.nxs.events.engine.stats;

import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.ShowBoardData;

public abstract class EventStats {
    public void showHtmlText(PlayerEventInfo player, String text) {
        if (text.length() < 4090) {
            ShowBoardData sb = new ShowBoardData(text, "101");
            sb.sendToPlayer(player);
            sb = new ShowBoardData(null, "102");
            sb.sendToPlayer(player);
            sb = new ShowBoardData(null, "103");
            sb.sendToPlayer(player);
        } else if (text.length() < 8180) {
            ShowBoardData sb = new ShowBoardData(text.substring(0, 4090), "101");
            sb.sendToPlayer(player);
            sb = new ShowBoardData(text.substring(4090, text.length()), "102");
            sb.sendToPlayer(player);
            sb = new ShowBoardData(null, "103");
            sb.sendToPlayer(player);
        } else if (text.length() < 12270) {
            ShowBoardData sb = new ShowBoardData(text.substring(0, 4090), "101");
            sb.sendToPlayer(player);
            sb = new ShowBoardData(text.substring(4090, 8180), "102");
            sb.sendToPlayer(player);
            sb = new ShowBoardData(text.substring(8180, text.length()), "103");
            sb.sendToPlayer(player);
        }
    }

    public abstract void load();

    public abstract void onLogin(PlayerEventInfo var1);

    public abstract void onDisconnect(PlayerEventInfo var1);

    public abstract void onCommand(PlayerEventInfo var1, String var2);

    public abstract void statsChanged(PlayerEventInfo var1);
}

