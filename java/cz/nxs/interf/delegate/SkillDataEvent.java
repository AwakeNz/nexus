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

import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.skills.Skill;

import cz.nxs.l2j.delegate.ISkillData;

public class SkillDataEvent
implements ISkillData {
    private Skill _skill;

    public SkillDataEvent(Skill cha) {
        _skill = cha;
    }

    public SkillDataEvent(int skillId, int level) {
        _skill = SkillData.getInstance().getSkill(skillId, level);
    }

    public Skill getOwner() {
        return _skill;
    }

    public String getName() {
        return _skill.getName();
    }

    public int getLevel() {
        return _skill.getLevel();
    }

    public boolean exists() {
        return _skill != null;
    }

    public String getSkillType() {
        return _skill.getAbnormalType().toString();
    }

    public boolean isHealSkill() {
        // if (getSkillType().equals("BALANCE_LIFE") || getSkillType().equals("CPHEAL_PERCENT") || getSkillType().equals("COMBATPOINTHEAL") || getSkillType().equals("CPHOT") || getSkillType().equals("HEAL") || getSkillType().equals("HEAL_PERCENT") || getSkillType().equals("HEAL_STATIC") || getSkillType().equals("HOT") || getSkillType().equals("MANAHEAL") || getSkillType().equals("MANAHEAL_PERCENT") || getSkillType().equals("MANARECHARGE") || getSkillType().equals("MPHOT") || getSkillType().equals("MANA_BY_LEVEL")) {
    	if (_skill.isHealingPotionSkill())
    	{
    	return true;
        }
        return false;
    }

    public boolean isResSkill() {
       // if (getSkillType().equals("RESURRECT")) {
    	if (_skill.hasEffectType(L2EffectType.RESURRECTION)) {
            return true;
        }
        return false;
    }

    public int getHitTime() {
        return _skill.getHitTime();
    }

    public int getReuseDelay() {
        return _skill.getReuseDelay();
    }

    public int getId() {
        return _skill.getId();
    }
}

