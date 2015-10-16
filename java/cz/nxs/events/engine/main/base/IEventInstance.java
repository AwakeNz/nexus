/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.delegate.InstanceData
 */
package cz.nxs.events.engine.main.base;

import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.interf.delegate.InstanceData;
import java.util.concurrent.ScheduledFuture;

public interface IEventInstance {
    public InstanceData getInstance();

    public ScheduledFuture<?> scheduleNextTask(int var1);

    public AbstractMainEvent.Clock getClock();

    public boolean isActive();
}

