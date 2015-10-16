/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.L2Object
 *  com.l2jserver.gameserver.model.actor.L2Npc
 *  com.l2jserver.gameserver.model.actor.L2Summon
 *  com.l2jserver.gameserver.model.actor.instance.L2FenceInstance
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  cz.nxs.l2j.delegate.IObjectData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2FenceInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import cz.nxs.interf.delegate.FenceData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.l2j.delegate.IObjectData;

public class ObjectData
implements IObjectData {
    protected L2Object _owner;

    public ObjectData(L2Object cha) {
        this._owner = cha;
    }

    public L2Object getOwner() {
        return this._owner;
    }

    public int getObjectId() {
        return this._owner.getObjectId();
    }

    public boolean isPlayer() {
        return this._owner instanceof L2PcInstance;
    }

    public boolean isSummon() {
        return this._owner instanceof L2Summon;
    }

    public boolean isFence() {
        return this._owner instanceof L2FenceInstance;
    }

    public FenceData getFence() {
        if (!this.isFence()) {
            return null;
        }
        return new FenceData((L2FenceInstance)this._owner);
    }

    public NpcData getNpc() {
        return new NpcData((L2Npc)this._owner);
    }

    public boolean isNpc() {
        return this._owner instanceof L2Npc;
    }
}

