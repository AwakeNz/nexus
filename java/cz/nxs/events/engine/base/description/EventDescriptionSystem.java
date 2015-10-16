/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.base.description;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.description.EventDescription;
import java.util.Map;
import java.util.logging.Level;
import javolution.util.FastMap;

public class EventDescriptionSystem {
    private Map<EventType, EventDescription> _descriptions = new FastMap();

    public EventDescriptionSystem() {
        NexusLoader.debug((String)"Loaded editable Event Description system.", (Level)Level.INFO);
    }

    public void addDescription(EventType type, EventDescription description) {
        this._descriptions.put(type, description);
    }

    public EventDescription getDescription(EventType type) {
        if (this._descriptions.containsKey((Object)type)) {
            return this._descriptions.get((Object)type);
        }
        return null;
    }

    public static final EventDescriptionSystem getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final EventDescriptionSystem _instance = new EventDescriptionSystem();

        private SingletonHolder() {
        }
    }

}

