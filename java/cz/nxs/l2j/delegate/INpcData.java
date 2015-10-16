/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.ObjectData
 */
package cz.nxs.l2j.delegate;

import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.ObjectData;

public interface INpcData {
    public ObjectData getObjectData();

    public void setName(String var1);

    public void setTitle(String var1);

    public int getNpcId();

    public void setEventTeam(int var1);

    public int getEventTeam();

    public void broadcastNpcInfo();

    public void broadcastSkillUse(CharacterData var1, CharacterData var2, int var3, int var4);

    public void deleteMe();
}

