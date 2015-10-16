/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.configtemplate.templates;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.configtemplate.ConfigTemplate;

public class TvTClassic
extends ConfigTemplate {
    private ConfigTemplate.SetConfig[] configs = new ConfigTemplate.SetConfig[]{new ConfigTemplate.SetConfig("killsForReward", "1"), new ConfigTemplate.SetConfig("resDelay", "15"), new ConfigTemplate.SetConfig("waweRespawn", "false"), new ConfigTemplate.SetConfig("createParties", "true"), new ConfigTemplate.SetConfig("maxPartySize", "10"), new ConfigTemplate.SetConfig("teamsCount", "2"), new ConfigTemplate.SetConfig("allowScreenScoreBar", "true"), new ConfigTemplate.SetConfig("divideToTeamsMethod", "LevelOnly"), new ConfigTemplate.SetConfig("balanceHealersInTeams", "true"), new ConfigTemplate.SetConfig("minLvl", "20"), new ConfigTemplate.SetConfig("maxLvl", "85"), new ConfigTemplate.SetConfig("minPlayers", "4"), new ConfigTemplate.SetConfig("maxPlayers", "500"), new ConfigTemplate.SetConfig("playersInInstance", "0"), new ConfigTemplate.SetConfig("allowPotions", "false"), new ConfigTemplate.SetConfig("removeBuffsOnStart", "true"), new ConfigTemplate.SetConfig("removeBuffsOnRespawn", "false"), new ConfigTemplate.SetConfig("notAllowedSkills", "0")};

    @Override
    public String getName() {
        return "Team vs Team classic";
    }

    @Override
    public EventType getEventType() {
        return EventType.TvT;
    }

    @Override
    public String getDescription() {
        return "Classic settings for a regular TvT event, no wawe spawn. Don't forget to setup apropriate InstanceTypes for your server, to make sure all players can play in a balanced event.";
    }

    @Override
    public ConfigTemplate.SetConfig[] getConfigs() {
        return this.configs;
    }
}

