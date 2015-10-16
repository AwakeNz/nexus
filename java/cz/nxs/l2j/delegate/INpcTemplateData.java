/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.delegate.NpcData
 */
package cz.nxs.l2j.delegate;

import cz.nxs.interf.delegate.NpcData;

public interface INpcTemplateData {
    public void setSpawnName(String var1);

    public void setSpawnTitle(String var1);

    public boolean exists();

    public NpcData doSpawn(int var1, int var2, int var3, int var4, int var5);

    public NpcData doSpawn(int var1, int var2, int var3, int var4, int var5, int var6);

    public NpcData doSpawn(int var1, int var2, int var3, int var4, int var5, int var6, int var7);
}

