/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.base.description;

import cz.nxs.events.engine.base.ConfigModel;
import java.util.Map;

public abstract class EventDescription {
    public String getDescription(Map<String, ConfigModel> configs) {
        return "";
    }

    public String getDescription(Map<String, ConfigModel> configs, int roundsCount, int teamsCount, int teamSize, int rejoinDelay, int timeLimit) {
        return "";
    }

    public final String getString(Map<String, ConfigModel> configs, String propName) {
        if (configs.containsKey(propName)) {
            String value = configs.get(propName).getValue();
            return value;
        }
        return "";
    }

    public final int getInt(Map<String, ConfigModel> configs, String propName) {
        if (configs.containsKey(propName)) {
            int value = configs.get(propName).getValueInt();
            return value;
        }
        return 0;
    }

    public final boolean getBoolean(Map<String, ConfigModel> configs, String propName) {
        if (configs.containsKey(propName)) {
            return configs.get(propName).getValueBoolean();
        }
        return false;
    }
}

