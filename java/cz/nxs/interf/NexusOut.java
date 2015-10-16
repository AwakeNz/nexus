/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.L2DatabaseFactory
 *  com.l2jserver.gameserver.ThreadPoolManager
 *  com.l2jserver.gameserver.cache.HtmCache
 *  com.l2jserver.gameserver.datatables.ClanTable
 *  com.l2jserver.gameserver.datatables.DoorTable
 *  com.l2jserver.gameserver.datatables.ItemTable
 *  com.l2jserver.gameserver.handler.AdminCommandHandler
 *  com.l2jserver.gameserver.handler.IAdminCommandHandler
 *  com.l2jserver.gameserver.idfactory.IdFactory
 *  com.l2jserver.gameserver.instancemanager.InstanceManager
 *  com.l2jserver.gameserver.model.L2Clan
 *  com.l2jserver.gameserver.model.L2Object
 *  com.l2jserver.gameserver.model.L2World
 *  com.l2jserver.gameserver.model.L2WorldRegion
 *  com.l2jserver.gameserver.model.StatsSet
 *  com.l2jserver.gameserver.model.actor.instance.L2DoorInstance
 *  com.l2jserver.gameserver.model.actor.instance.L2FenceInstance
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  com.l2jserver.gameserver.model.actor.knownlist.ObjectKnownList
 *  com.l2jserver.gameserver.model.base.ClassId
 *  com.l2jserver.gameserver.model.entity.Instance
 *  com.l2jserver.gameserver.model.items.L2Item
 *  com.l2jserver.gameserver.network.serverpackets.CreatureSay
 *  com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket
 *  com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage
 *  com.l2jserver.gameserver.util.Broadcast
 *  com.l2jserver.util.Rnd
 *  cz.nxs.l2j.CallBack
 *  cz.nxs.l2j.INexusOut
 *  javolution.util.FastList
 */
package cz.nxs.interf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.enums.ChatType;
import com.l2jserver.gameserver.handler.AdminCommandHandler;
import com.l2jserver.gameserver.idfactory.IdFactory;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.L2WorldRegion;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.util.Broadcast;
import com.l2jserver.util.Rnd;

import cz.nxs.interf.delegate.DoorData;
import cz.nxs.interf.delegate.FenceData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.handlers.AdminCommandHandlerInstance;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;

public class NexusOut implements INexusOut
{
	public void load()
	{
		CallBack.getInstance().setNexusOut(this);
	}
	
	public static ScheduledFuture<?> scheduleGeneral(Runnable task, int delay)
	{
		return ThreadPoolManager.getInstance().scheduleGeneral(task, delay);
	}
	
	public static ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable task, int initial, int delay)
	{
		return ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(task, initial, delay);
	}
	
	@Override
	public void executeTask(Runnable task)
	{
		ThreadPoolManager.getInstance().executeEvent(task);
	}
	
	@Override
	public void purge()
	{
		ThreadPoolManager.getInstance().purge();
	}
	
	@Override
	public int getNextObjectId()
	{
		return IdFactory.getInstance().getNextId();
	}
	
	@Override
	public int random(int min, int max)
	{
		return Rnd.get(min, max);
	}
	
	public static int random(int max)
	{
		return Rnd.get(max);
	}
	
	public static Connection getConnection()
	{
		try
		{
			return ConnectionFactory.getInstance().getConnection();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public InstanceData createInstance(String name, int duration, int emptyDestroyTime, boolean isPvp, String template)
	{
		int instanceId = InstanceManager.getInstance().createDynamicInstance(template);
		InstanceManager.getInstance().getInstance(instanceId).setName(name);
		InstanceManager.getInstance().getInstance(instanceId).setAllowSummon(false);
		InstanceManager.getInstance().getInstance(instanceId).setDuration(duration);
		if (emptyDestroyTime > 0)
		{
			InstanceManager.getInstance().getInstance(instanceId).setEmptyDestroyTime(emptyDestroyTime);
		}
		InstanceManager.getInstance().getInstance(instanceId).setPvPInstance(isPvp);
		InstanceManager.getInstance().getInstance(instanceId).disableMessages();
		return new InstanceData(InstanceManager.getInstance().getInstance(instanceId));
	}
	
	@Override
	public InstanceData createInstance(String name, int duration, int emptyDestroyTime, boolean isPvp)
	{
		int instanceId = InstanceManager.getInstance().createDynamicInstance(null);
		InstanceManager.getInstance().getInstance(instanceId).setName(name);
		InstanceManager.getInstance().getInstance(instanceId).setAllowSummon(false);
		InstanceManager.getInstance().getInstance(instanceId).setDuration(duration);
		if (emptyDestroyTime > 0)
		{
			InstanceManager.getInstance().getInstance(instanceId).setEmptyDestroyTime(emptyDestroyTime);
		}
		InstanceManager.getInstance().getInstance(instanceId).setPvPInstance(isPvp);
		InstanceManager.getInstance().getInstance(instanceId).disableMessages();
		return new InstanceData(InstanceManager.getInstance().getInstance(instanceId));
	}
	
	@Override
	public void addDoorToInstance(int instanceId, int doorId, boolean opened)
	{
		StatsSet set = new StatsSet();
		set.set("id", String.valueOf(doorId));
		set.set("default_status", opened ? "open" : "close");
		InstanceManager.getInstance().getInstance(instanceId).addDoor(doorId, set);
	}
	
	@Override
	public DoorData[] getInstanceDoors(int instanceId)
	{
		FastList doors = new FastList<>();
		for (L2DoorInstance d : InstanceManager.getInstance().getInstance(instanceId).getDoors())
		{
			doors.add(new DoorData(d));
		}
		return doors.toArray(new DoorData[doors.size()]);
	}
	
	@Override
	public void registerAdminHandler(AdminCommandHandlerInstance handler)
	{
		AdminCommandHandler.getInstance().registerHandler(handler);
	}
	
	@Override
	public PlayerEventInfo getPlayer(int playerId)
	{
		try
		{
			return L2World.getInstance().getPlayer(playerId).getEventInfo();
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	@Override
	public PlayerEventInfo getPlayer(String name)
	{
		try
		{
			return L2World.getInstance().getPlayer(name).getEventInfo();
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	@Override
	public String getClanName(int clanId)
	{
		try
		{
			return ClanTable.getInstance().getClan(clanId).getName();
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	@Override
	public String getAllyName(int clanId)
	{
		try
		{
			return ClanTable.getInstance().getClan(clanId).getAllyName();
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	@Override
	public void announceToAllScreenMessage(String message, String announcer)
	{
		Broadcast.toAllOnlinePlayers(new CreatureSay(0, ChatType.CRITICAL_ANNOUNCE, "", announcer + ": " + message));
	}
	
	@Override
	public String getHtml(String path)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		if (!html.setFile(null, path))
		{
			return null;
		}
		return html.getText();
	}
	
	@Override
	public String getEventHtml(String path)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		if (!html.setEventHtml(path))
		{
			return null;
		}
		return html.getText();
	}
	
	@Override
	public void reloadHtmls()
	{
		HtmCache.getInstance().reload();
	}
	
	@Override
	public String getItemName(int id)
	{
		try
		{
			return ItemTable.getInstance().getTemplate(id).getName();
		}
		catch (Exception e)
		{
			return "Unknown item";
		}
	}
	
	@Override
	public boolean doorExists(int id)
	{
		return DoorTable.getInstance().getDoor(id) != null;
	}
	
	@Override
	public FenceData createFence(int type, int width, int length, int x, int y, int z, int eventId)
	{
		return new FenceData(new L2FenceInstance(this.getNextObjectId(), type, width, length, x, y, z, eventId));
	}
	
	@Override
	public void spawnFences(List<FenceData> list, int instance)
	{
		for (FenceData fence : list)
		{
			if (fence.getOwner() == null)
			{
				continue;
			}
			if (instance > 0)
			{
				fence.getOwner().setInstanceId(instance);
			}
			fence.getOwner().spawnMe(fence.getOwner().getXLoc(), fence.getOwner().getYLoc(), fence.getOwner().getZLoc());
		}
	}
	
	@Override
	public void unspawnFences(List<FenceData> list)
	{
		for (FenceData fence : list)
		{
			if (fence == null)
			{
				continue;
			}
			L2WorldRegion region = fence.getOwner().getWorldRegion();
			fence.getOwner().decayMe();
			if (region != null)
			{
				region.removeVisibleObject((L2Object) fence.getOwner());
			}
			fence.getOwner().getKnownList().removeAllKnownObjects();
			L2World.getInstance().removeObject((L2Object) fence.getOwner());
		}
	}
	
	@Override
	public int getGradeFromFirstLetter(String s)
	{
		if (s.equalsIgnoreCase("n") || s.equalsIgnoreCase("ng") || s.equalsIgnoreCase("no"))
		{
			return 0;
		}
		if (s.equalsIgnoreCase("d"))
		{
			return 1;
		}
		if (s.equalsIgnoreCase("c"))
		{
			return 2;
		}
		if (s.equalsIgnoreCase("b"))
		{
			return 3;
		}
		if (s.equalsIgnoreCase("a"))
		{
			return 4;
		}
		if (s.equalsIgnoreCase("s"))
		{
			return 5;
		}
		if (s.equalsIgnoreCase("s80"))
		{
			return 6;
		}
		if (s.equalsIgnoreCase("s84"))
		{
			return 7;
		}
		return 0;
	}
	
	@Override
	public Set<Integer> getAllWeaponsId()
	{
		return ItemTable.getInstance().getAllWeaponsId();
	}
	
	@Override
	public Set<Integer> getAllArmorsId()
	{
		return ItemTable.getInstance().getAllArmorsId();
	}
	
	@Override
	public Integer[] getAllClassIds()
	{
		FastList idsList = new FastList<>();
		for (ClassId id : ClassId.values())
		{
			idsList.add(id.getId());
		}
		return idsList.toArray(new Integer[idsList.size()]);
	}
	
	@Override
	public PlayerEventInfo[] getAllPlayers()
	{
		FastList eventInfos = new FastList();
		for (L2PcInstance player : L2World.getInstance().getAllPlayersArray())
		{
			eventInfos.add(player.getEventInfo());
		}
		return eventInfos.toArray(new PlayerEventInfo[eventInfos.size()]);
	}
	
	protected static final NexusOut getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final NexusOut _instance = new NexusOut();
		
		private SingletonHolder()
		{
		}
	}
	
	public static void closeConnection(Connection con) throws SQLException
	{
		con.close();
	}
	
	/*
	 * (non-Javadoc)
	 * @see cz.nxs.l2j.INexusOut#scheduleGeneral(java.lang.Runnable, long)
	 */
	@Override
	public ScheduledFuture<?> scheduleGeneral(Runnable var1, long var2)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see cz.nxs.l2j.INexusOut#scheduleGeneralAtFixedRate(java.lang.Runnable, long, long)
	 */
	@Override
	public ScheduledFuture<?> scheduleGeneralAtFixedRate(Runnable var1, long var2, long var4)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
}
