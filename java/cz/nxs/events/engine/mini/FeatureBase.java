/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.mini;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.mini.EventMode;
import java.util.Map;
import javolution.util.FastMap;

public class FeatureBase {
    private static FeatureBase _instance = new FeatureBase();
    private Map<EventMode.FeatureType, FeatureInfo> _data = new FastMap();

    public static FeatureBase getInstance() {
        return _instance;
    }

    public FeatureBase() {
        this.add(EventMode.FeatureType.Delays, "Delays", EventMode.FeatureCategory.Configs, EventType.getMiniEvents(), "specifies all delays for this event.");
        this.add(EventMode.FeatureType.Enchant, "Enchant", EventMode.FeatureCategory.Items, EventType.getMiniEvents(), "specifies all enchant related settings.");
        this.add(EventMode.FeatureType.ItemGrades, "Item Grades", EventMode.FeatureCategory.Items, EventType.getMiniEvents(), "allows you to specify allowed item grades.");
        this.add(EventMode.FeatureType.Items, "Items", EventMode.FeatureCategory.Items, EventType.getMiniEvents(), "specifies which items will be allowed and which disabled.");
        this.add(EventMode.FeatureType.Level, "Level", EventMode.FeatureCategory.Players, EventType.getMiniEvents(), "specifies max/min level allowed to participate this mode");
        this.add(EventMode.FeatureType.TimeLimit, "Time Limit", EventMode.FeatureCategory.Configs, EventType.getMiniEvents(), "specifies all time-based settings.");
        this.add(EventMode.FeatureType.Skills, "Skills", EventMode.FeatureCategory.Players, EventType.getMiniEvents(), "specifies all skills-related settings.");
        this.add(EventMode.FeatureType.Buffer, "Buffer", EventMode.FeatureCategory.Configs, EventType.getMiniEvents(), "specifies all buffs-related settings.");
        this.add(EventMode.FeatureType.Rounds, "Rounds", EventMode.FeatureCategory.Configs, new EventType[]{EventType.Classic_1v1, EventType.PartyvsParty, EventType.MiniTvT}, "allows you to edit the ammount of rounds only for this mode.");
        this.add(EventMode.FeatureType.TeamsAmmount, "Teams Ammount", EventMode.FeatureCategory.Configs, new EventType[]{EventType.Classic_1v1, EventType.PartyvsParty, EventType.MiniTvT}, "allows you to edit the ammount of teams only for this mode.");
        this.add(EventMode.FeatureType.TeamSize, "Team Size", EventMode.FeatureCategory.Players, new EventType[]{EventType.Korean, EventType.PartyvsParty, EventType.MiniTvT}, "allows you to edit the ammount of players in one team only for this mode.");
        this.add(EventMode.FeatureType.StrenghtChecks, "Strenght Checks", EventMode.FeatureCategory.Players, new EventType[]{EventType.Korean, EventType.PartyvsParty, EventType.Classic_1v1}, "allows you to edit the automatic match making strenght difference checks");
    }

    public void add(EventMode.FeatureType type, String visibleName, EventMode.FeatureCategory cat, EventType[] events, String desc) {
        FeatureInfo info = new FeatureInfo(cat, visibleName, events, desc);
        this._data.put(type, info);
    }

    public FeatureInfo get(EventMode.FeatureType type) {
        return this._data.get((Object)type);
    }

    public class FeatureInfo {
        private EventMode.FeatureCategory _category;
        private EventType[] _events;
        private String _desc;
        private String _visibleName;

        public FeatureInfo(EventMode.FeatureCategory cat, String visName, EventType[] events, String desc) {
            this._category = cat;
            this._events = events;
            this._desc = desc;
            this._visibleName = visName;
        }

        public EventMode.FeatureCategory getCategory() {
            return this._category;
        }

        public String getVisibleName() {
            return this._visibleName;
        }

        public String getDesc() {
            return this._desc;
        }

        public boolean isForEvent(EventType event) {
            for (EventType t : this._events) {
                if (t != event) continue;
                return true;
            }
            return false;
        }
    }

}

