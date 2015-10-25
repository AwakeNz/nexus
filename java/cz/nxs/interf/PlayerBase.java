/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  cz.nxs.l2j.CallBack
 *  cz.nxs.l2j.IPlayerBase
 *  javolution.util.FastMap
 */
package cz.nxs.interf;

import javolution.util.FastMap;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.IPlayerBase;

public class PlayerBase implements IPlayerBase
{
	private final FastMap<Integer, PlayerEventInfo> players = new FastMap<Integer, PlayerEventInfo>().shared();
	
	public void load()
	{
		CallBack.getInstance().setPlayerBase(this);
	}
	
	@Override
	public PlayerEventInfo getPlayer(int id)
	{
		return players.get(id);
	}
	
	@Override
	public FastMap<Integer, PlayerEventInfo> getPs()
	{
		return players;
	}
	
	protected PlayerEventInfo getPlayer(L2PcInstance player)
	{
		return player.getEventInfo();
	}
	
	@Override
	public PlayerEventInfo addInfo(PlayerEventInfo player)
	{
		players.put(player.getPlayersId(), player);
		return player;
	}
	
	@Override
	public void eventEnd(PlayerEventInfo player)
	{
		deleteInfo(player.getOwner());
	}
	
	@Override
	public void playerDisconnected(PlayerEventInfo player)
	{
		eventEnd(player);
	}
	
	@Override
	public void deleteInfo(int player)
	{
		players.remove(player);
	}
	
	protected void deleteInfo(L2PcInstance player)
	{
		players.remove(player.getObjectId());
	}
	
	public static final PlayerBase getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PlayerBase _instance = new PlayerBase();
		
		private SingletonHolder()
		{
		}
	}
	
}
