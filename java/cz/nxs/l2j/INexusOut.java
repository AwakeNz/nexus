/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.DoorData
 *  cz.nxs.interf.delegate.FenceData
 *  cz.nxs.interf.delegate.InstanceData
 *  cz.nxs.interf.handlers.AdminCommandHandlerInstance
 */
package cz.nxs.l2j;

import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.DoorData;
import cz.nxs.interf.delegate.FenceData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.handlers.AdminCommandHandlerInstance;
import java.sql.Connection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

public interface INexusOut {
    public ScheduledFuture<?> scheduleGeneral(Runnable var1, long var2);

    public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable var1, long var2, long var4);

    public void executeTask(Runnable var1);

    public void purge();

    public int getNextObjectId();

    public int random(int var1, int var2);

    public int random(int var1);

    public Connection getConnection();

    public InstanceData createInstance(String var1, int var2, int var3, boolean var4);

    public InstanceData createInstance(String var1, int var2, int var3, boolean var4, String var5);

    public void addDoorToInstance(int var1, int var2, boolean var3);

    public DoorData[] getInstanceDoors(int var1);

    public void registerAdminHandler(AdminCommandHandlerInstance var1);

    public String getClanName(int var1);

    public String getAllyName(int var1);

    public PlayerEventInfo getPlayer(int var1);

    public PlayerEventInfo getPlayer(String var1);

    public Integer[] getAllClassIds();

    public PlayerEventInfo[] getAllPlayers();

    public void announceToAllScreenMessage(String var1, String var2);

    public String getHtml(String var1);

    public String getEventHtml(String var1);

    public void reloadHtmls();

    public boolean doorExists(int var1);

    public FenceData createFence(int var1, int var2, int var3, int var4, int var5, int var6, int var7);

    public void spawnFences(List<FenceData> var1, int var2);

    public void unspawnFences(List<FenceData> var1);

    public int getGradeFromFirstLetter(String var1);

    public String getItemName(int var1);

    public Set<Integer> getAllWeaponsId();

    public Set<Integer> getAllArmorsId();
}

