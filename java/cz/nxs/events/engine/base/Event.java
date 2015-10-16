/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 */
package cz.nxs.events.engine.base;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;

public abstract class Event {
    protected EventType _type;

    public Event(EventType type) {
        this._type = type;
    }

    public final EventType getEventType() {
        return this._type;
    }

    public String getEventName() {
        return this._type.getAltTitle();
    }

    public void announce(String text) {
        CallBack.getInstance().getOut().announceToAllScreenMessage(text, this.getEventType().getAltTitle());
    }

    protected void debug(String text) {
        NexusLoader.debug((String)text);
    }

    protected void print(String msg) {
        NexusLoader.detailedDebug((String)msg);
    }
}

