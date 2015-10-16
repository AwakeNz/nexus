/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.l2j.delegate;

public interface ISkillData {
    public int getId();

    public String getName();

    public int getLevel();

    public boolean exists();

    public String getSkillType();

    public boolean isHealSkill();

    public boolean isResSkill();

    public int getHitTime();

    public int getReuseDelay();
}

