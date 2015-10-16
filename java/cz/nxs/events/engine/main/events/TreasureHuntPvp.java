/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.events.TreasureHunt;

public class TreasureHuntPvp
extends TreasureHunt {
    public TreasureHuntPvp(EventType type, MainEventManager manager) {
        super(type, manager);
        this.setRewardTypes(new RewardPosition[]{RewardPosition.Looser, RewardPosition.Tie, RewardPosition.Numbered, RewardPosition.Range, RewardPosition.FirstBlood, RewardPosition.FirstRegistered, RewardPosition.OnKill});
        this._allowPvp = true;
    }
}

