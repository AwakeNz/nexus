/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket
 *  com.l2jserver.gameserver.network.serverpackets.ShowBoard
 *  cz.nxs.l2j.delegate.IShowBoardData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.ShowBoard;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.delegate.IShowBoardData;

public class ShowBoardData
implements IShowBoardData {
    private ShowBoard _board;

    public ShowBoardData(ShowBoard sb) {
        this._board = sb;
    }

    public ShowBoardData(String text, String id) {
        this._board = new ShowBoard(text, id);
    }

    public void sendToPlayer(PlayerEventInfo player) {
        player.getOwner().sendPacket((L2GameServerPacket)this._board);
    }
}

