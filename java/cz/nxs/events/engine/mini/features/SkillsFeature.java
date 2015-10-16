/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.SkillData
 */
package cz.nxs.events.engine.mini.features;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.SkillData;
import java.util.Arrays;

public class SkillsFeature
extends AbstractFeature {
    private boolean disableSkills = false;
    private boolean allowResSkills = false;
    private boolean allowHealSkills = false;
    private int[] disabledSkills = null;

    public SkillsFeature(EventType event, PlayerEventInfo gm, String parametersString) {
        super(event);
        this.addConfig("DisableSkills", "If 'true', then all skills will be disabled for this mode. Put 'false' to enable them.", 1);
        this.addConfig("AllowResurrections", "Put 'false' to disable all resurrection-type skills. Put 'true' to enable them.", 1);
        this.addConfig("AllowHeals", "Put 'false' to disable all heal-type skills. Put 'true' to enable them. This config doesn't affect self-heals.", 1);
        this.addConfig("DisabledSkills", "Specify here which skills will be disabled for this mode. Write their IDs and separate by SPACE. Eg. <font color=LEVEL>50 150 556</font>. Put <font color=LEVEL>0</font> to disable this config.", 2);
        if (parametersString == null) {
            parametersString = "false,false,true,0";
        }
        this._params = parametersString;
        this.initValues();
    }

    @Override
    protected void initValues() {
        String[] params = this.splitParams(this._params);
        try {
            this.disableSkills = Boolean.parseBoolean(params[0]);
            this.allowResSkills = Boolean.parseBoolean(params[1]);
            this.allowHealSkills = Boolean.parseBoolean(params[2]);
            String[] splitted = params[3].split(" ");
            this.disabledSkills = new int[splitted.length];
            for (int i = 0; i < splitted.length; ++i) {
                this.disabledSkills[i] = Integer.parseInt(splitted[i]);
            }
            Arrays.sort(this.disabledSkills);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
    }

    public boolean checkSkill(PlayerEventInfo player, SkillData skill) {
        if (this.disableSkills) {
            return false;
        }
        if (skill.isResSkill() && !this.allowResSkills) {
            return false;
        }
        if (skill.isHealSkill() && !this.allowHealSkills) {
            return false;
        }
        if (Arrays.binarySearch(this.disabledSkills, skill.getId()) >= 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean checkPlayer(PlayerEventInfo player) {
        return true;
    }

    @Override
    public EventMode.FeatureType getType() {
        return EventMode.FeatureType.Skills;
    }
}

