/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.base;

public enum RewardPosition {
    None(null, ""),
    Winner(PositionType.General, "The best player/team."),
    Looser(PositionType.General, "All non-winners."),
    Tie(PositionType.General, ""),
    Tie_TimeLimit(PositionType.General, ""),
    Numbered(PositionType.Numbered, ""),
    Range(PositionType.Range, ""),
    KillingSpree(PositionType.Numbered, "Rewards players who do X kills in a row."),
    OnKill(PositionType.EventSpecific, "Reward for killing another player."),
    FirstRegistered(PositionType.EventSpecific, "Reward for first X (configurable) players in the event."),
    FirstBlood(PositionType.EventSpecific, "Reward for the player who makes first kill in event."),
    FlagScore(PositionType.EventSpecific, "Reward for scoring with the flag."),
    FlagReturn(PositionType.EventSpecific, "Reward for player who returns his team's flag back."),
    ChestReward(PositionType.EventSpecific, "Reward when a regular chest is killed."),
    ChestRewardLucky(PositionType.EventSpecific, "Reward when a lucky chest is killed."),
    ChestRewardAncient(PositionType.EventSpecific, "Reward when a ancient chest is killed.");
    
    public PositionType posType;
    public String description;

    private RewardPosition(PositionType posType, String description) {
        this.posType = posType;
        this.description = description;
    }

    public static RewardPosition getPosition(String name) {
        for (RewardPosition p : RewardPosition.values()) {
            if (!p.toString().equalsIgnoreCase(name)) continue;
            return p;
        }
        return null;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public static enum PositionType {
        General,
        Numbered,
        Range,
        EventSpecific;
        

        private PositionType() {
        }
    }

}

