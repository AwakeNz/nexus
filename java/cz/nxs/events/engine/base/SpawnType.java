/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.base;

import cz.nxs.events.engine.base.EventType;
import java.util.List;

public enum SpawnType {
    Regular("CD9F36", null, "Adds place where the players of team %TEAM% will be spawned."),
    Door("916406", new EventType[]{EventType.Classic_1v1, EventType.PartyvsParty, EventType.Korean, EventType.MiniTvT}, "Adds door to the event's instance."),
    Npc("FFFFFF", null, "Adds an NPC to the event with ID you specify."),
    Fence("878578", null, "Adds fence to the event."),
    Buffer("68AFB3", new EventType[]{EventType.Classic_1v1, EventType.PartyvsParty, EventType.Korean, EventType.MiniTvT}, "A buffer NPC will be spawned here during the event period."),
    Spectator("FFFFFF", new EventType[]{EventType.Classic_1v1, EventType.PartyvsParty, EventType.Korean, EventType.MiniTvT}, "Defines an observation spot for spectators."),
    MapGuard("FFFFFF", null, "Adds a map guard to the event's instance. Map guard kills everyone who gets near."),
    Radar("FFFFFF", null, "Players from spawn's team will be guided to this location."),
    Safe("5BB84B", new EventType[]{EventType.Korean}, "Players will stay in this loc during the safe preparation phase. Don't forget to put fences arround this spot."),
    Flag("867BC4", new EventType[]{EventType.CTF, EventType.Underground_Coliseum}, "Defines the position of a flag."),
    Zombie("7C9B59", new EventType[]{EventType.Zombies, EventType.Mutant}, "Defines where the zombies and mutants (re)spawn."),
    Monster("879555", new EventType[]{EventType.SurvivalArena}, ""),
    Boss("BE2C49", new EventType[]{EventType.RBHunt}, ""),
    Zone("68AFB3", new EventType[]{EventType.Domination, EventType.MassDomination}, "Adds a Domination zone here. Teams have to get near to this place in order to score."),
    Chest("68AFB3", new EventType[]{EventType.LuckyChests, EventType.TreasureHunt, EventType.TreasureHuntPvp}, "Defines where the chests will be spawned."),
    Simon("68AFB3", new EventType[]{EventType.Simon}, "Spawns Simon the NPC here."),
    Russian("68AFB3", new EventType[]{EventType.RussianRoulette}, ""),
    Base("68AFB3", new EventType[]{EventType.Battlefields}, "Adds a conquerable base here. Teams have to get near to this place in order to score."),
    VIP("68AFB3", new EventType[]{EventType.TvTAdv}, "VIPs will be spawned in this spawn.");
    
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

