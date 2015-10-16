/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.base;

import cz.nxs.events.engine.base.EventType;

public class EventTypeHolder {
    public EventType _type;

    public EventTypeHolder(EventType type) {
        this._type = type;
    }

    public EventTypeHolder register() {
        EventType.addHolder((EventTypeHolder)this);
        return this;
    }

    public EventType getType() {
        return this._type;
    }
}

