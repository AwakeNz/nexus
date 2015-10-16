/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.L2Object
 *  com.l2jserver.gameserver.model.L2World
 *  com.l2jserver.gameserver.model.L2WorldRegion
 *  com.l2jserver.gameserver.model.actor.instance.L2FenceInstance
 *  com.l2jserver.gameserver.model.actor.knownlist.ObjectKnownList
 *  cz.nxs.l2j.delegate.IFenceData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.L2WorldRegion;
import com.l2jserver.gameserver.model.actor.instance.L2FenceInstance;
import com.l2jserver.gameserver.model.actor.knownlist.ObjectKnownList;
import cz.nxs.interf.delegate.ObjectData;
import cz.nxs.l2j.delegate.IFenceData;

public class FenceData
extends ObjectData
implements IFenceData {
    private L2FenceInstance _owner;

    public FenceData(L2FenceInstance cha) {
        super((L2Object)cha);
        this._owner = cha;
    }

    public L2FenceInstance getOwner() {
        return this._owner;
    }

    public void deleteMe() {
        L2WorldRegion region = this._owner.getWorldRegion();
        this._owner.decayMe();
        if (region != null) {
            region.removeVisibleObject((L2Object)this._owner);
        }
        this._owner.getKnownList().removeAllKnownObjects();
        L2World.getInstance().removeObject((L2Object)this._owner);
    }
}

