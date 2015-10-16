/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.entity.Instance
 *  cz.nxs.l2j.delegate.IInstanceData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.model.entity.Instance;
import cz.nxs.l2j.delegate.IInstanceData;

public class InstanceData
implements IInstanceData {
    protected Instance _instance;

    public InstanceData(Instance i) {
        this._instance = i;
    }

    public Instance getOwner() {
        return this._instance;
    }

    public int getId() {
        return this._instance.getId();
    }

    public String getName() {
        return this._instance.getName();
    }
}

