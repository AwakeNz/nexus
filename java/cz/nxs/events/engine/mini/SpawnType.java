/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.mini;

import cz.nxs.events.engine.base.EventType;
import java.util.List;

public enum SpawnType {
    Regular("CD9F36", null, "Adds place where the players of team %TEAM% will be spawned."),
    Door("916406", new EventType[]{EventType.Classic_1v1, EventType.PartyvsParty, EventType.Korean, EventType.MiniTvT}, "Adds door to the event's instance."),
    Npc("FFFFFF", null, "Adds an NPC to the event with ID you specify."),
    Fence("878578", null, "Adds fence to the event's instance."),
    Buffer("68AFB3", new EventType[]{EventType.Classic_1v1, EventType.PartyvsParty, EventType.Korean, EventType.MiniTvT}, "Adds buffer NPC to the event's instance."),
    Spectator("FFFFFF", new EventType[]{EventType.Classic_1v1, EventType.PartyvsParty, EventType.Korean, EventType.MiniTvT}, "Defines observation spot for all spectators."),
    MapGuard("FFFFFF", null, "Adds a map guard to the event's instance."),
    Safe("5BB84B", new EventType[]{EventType.Korean}, ""),
    Flag("867BC4", new EventType[]{EventType.CTF, EventType.Underground_Coliseum}, ""),
    Zombie("7C9B59", new EventType[]{EventType.Zombies, EventType.Mutant}, ""),
    Monster("879555", new EventType[]{EventType.SurvivalArena}, ""),
    Boss("BE2C49", new EventType[]{EventType.RBHunt}, ""),
    Zone("68AFB3", new EventType[]{EventType.Domination, EventType.MassDomination}, ""),
    Chest("68AFB3", new EventType[]{EventType.LuckyChests}, ""),
    Simon("68AFB3", new EventType[]{EventType.Simon}, ""),
    Russian("68AFB3", new EventType[]{EventType.RussianRoulette}, ""),
    Base("68AFB3", new EventType[]{EventType.Battlefields}, ""),
    VIP("68AFB3", new EventType[]{EventType.TVTv}, "");
    
    private String htmlColor;
    private EventType[] events;
    private String desc;

    private SpawnType(String htmlColor, EventType[] allowedEvents, String description) {
        this.htmlColor = htmlColor;
        this.events = allowedEvents;
        this.desc = description;
    }

    public String getHtmlColor() {
        return this.htmlColor;
    }

    public String getDefaultDesc() {
        return this.desc;
    }

    public boolean isForEvents(List<EventType> events) {
        if (this.events == null) {
            return true;
        }
        for (EventType t : events) {
            if (!this.isForEvent(t)) continue;
            return true;
        }
        return false;
    }

    private boolean isForEvent(EventType type) {
        for (EventType t : this.events) {
            if (t.getId() != type.getId()) continue;
            return true;
        }
        return false;
    }
}

