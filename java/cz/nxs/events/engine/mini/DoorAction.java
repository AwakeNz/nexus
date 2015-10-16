/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.mini;

import java.util.StringTokenizer;

public enum DoorAction {
    Open,
    Close,
    Default;
    

    private DoorAction() {
    }

    public static DoorAction getAction(String note, int state) {
        String action = "Default";
        StringTokenizer st = new StringTokenizer(note);
        if (state == 1) {
            action = st.nextToken();
        } else if (state == 2) {
            st.nextToken();
            action = st.nextToken();
        }
        for (DoorAction d : DoorAction.values()) {
            if (!d.toString().equalsIgnoreCase(action)) continue;
            return d;
        }
        return Default;
    }
}

