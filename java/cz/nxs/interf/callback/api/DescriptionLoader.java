/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.engine.base.EventType
 *  cz.nxs.events.engine.base.description.EventDescription
 *  cz.nxs.events.engine.base.description.EventDescriptionSystem
 */
package cz.nxs.interf.callback.api;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.description.EventDescription;
import cz.nxs.events.engine.base.description.EventDescriptionSystem;
import cz.nxs.interf.callback.api.descriptions.CTFDescription;
import cz.nxs.interf.callback.api.descriptions.ChestsDescription;
import cz.nxs.interf.callback.api.descriptions.DMDescription;
import cz.nxs.interf.callback.api.descriptions.DominationDescription;
import cz.nxs.interf.callback.api.descriptions.KoreanDescription;
import cz.nxs.interf.callback.api.descriptions.LMSDescription;
import cz.nxs.interf.callback.api.descriptions.MassDominationDescription;
import cz.nxs.interf.callback.api.descriptions.MiniTvTDescription;
import cz.nxs.interf.callback.api.descriptions.PartyFightsDescription;
import cz.nxs.interf.callback.api.descriptions.SinglePlayersFightsDescription;
import cz.nxs.interf.callback.api.descriptions.TvTAdvancedDescription;
import cz.nxs.interf.callback.api.descriptions.TvTDescription;

public class DescriptionLoader {
    public static void load() {
        EventDescriptionSystem.getInstance().addDescription(EventType.TvT, (EventDescription)new TvTDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.TvTAdv, (EventDescription)new TvTAdvancedDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.CTF, (EventDescription)new CTFDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.Domination, (EventDescription)new DominationDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.MassDomination, (EventDescription)new MassDominationDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.DM, (EventDescription)new DMDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.LastMan, (EventDescription)new LMSDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.LuckyChests, (EventDescription)new ChestsDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.Classic_1v1, (EventDescription)new SinglePlayersFightsDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.PartyvsParty, (EventDescription)new PartyFightsDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.Korean, (EventDescription)new KoreanDescription());
        EventDescriptionSystem.getInstance().addDescription(EventType.MiniTvT, (EventDescription)new MiniTvTDescription());
    }
}

