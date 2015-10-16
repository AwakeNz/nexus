/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.datatables.SkillTable
 *  com.l2jserver.gameserver.model.skills.L2Skill
 *  com.l2jserver.gameserver.model.skills.L2SkillType
 *  cz.nxs.l2j.delegate.ISkillData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.model.skills.L2SkillType;
import cz.nxs.l2j.delegate.ISkillData;

public class SkillData
implements ISkillData {
    private L2Skill _skill;

    public SkillData(L2Skill cha) {
        this._skill = cha;
    }

    public SkillData(int skillId, int level) {
        this._skill = SkillTable.getInstance().getInfo(skillId, level);
    }

    public L2Skill getOwner() {
        return this._skill;
    }

    public String getName() {
        return this._skill.getName();
    }

    public int getLevel() {
        return this._skill.getLevel();
    }

    public boolean exists() {
        return this._skill != null;
    }

    public String getSkillType() {
        return this._skill.getSkillType().toString();
    }

    public boolean isHealSkill() {
        if (this.getSkillType().equals("BALANCE_LIFE") || this.getSkillType().equals("CPHEAL_PERCENT") || this.getSkillType().equals("COMBATPOINTHEAL") || this.getSkillType().equals("CPHOT") || this.getSkillType().equals("HEAL") || this.getSkillType().equals("HEAL_PERCENT") || this.getSkillType().equals("HEAL_STATIC") || this.getSkillType().equals("HOT") || this.getSkillType().equals("MANAHEAL") || this.getSkillType().equals("MANAHEAL_PERCENT") || this.getSkillType().equals("MANARECHARGE") || this.getSkillType().equals("MPHOT") || this.getSkillType().equals("MANA_BY_LEVEL")) {
            return true;
        }
        return false;
    }

    public boolean isResSkill() {
        if (this.getSkillType().equals("RESURRECT")) {
            return true;
        }
        return false;
    }

    public int getHitTime() {
        return this._skill.getHitTime();
    }

    public int getReuseDelay() {
        return this._skill.getReuseDelay();
    }

    public int getId() {
        return this._skill.getId();
    }
}

