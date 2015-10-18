/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.PartyData
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.mini;

import java.util.List;

import javolution.util.FastList;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.l2j.CallBack;

public class RegistrationData
{
	private final FastList<PlayerEventInfo> _players;
	private boolean _choosen = false;
	
	public RegistrationData(FastList<PlayerEventInfo> players)
	{
		_players = players;
	}
	
	public PlayerEventInfo getKeyPlayer()
	{
		return _players.getFirst();
	}
	
	public List<PlayerEventInfo> getPlayers()
	{
		return _players;
	}
	
	public PartyData getParty()
	{
		if (getKeyPlayer().isOnline())
		{
			return getKeyPlayer().getParty();
		}
		return null;
	}
	
	public void register(boolean isRegistered, MiniEventManager registeredEvent)
	{
		for (PlayerEventInfo pi : _players)
		{
			pi.setIsRegisteredToMiniEvent(isRegistered, registeredEvent);
			if (isRegistered)
			{
				continue;
			}
			CallBack.getInstance().getPlayerBase().eventEnd(pi);
		}
	}
	
	public void message(String msg, boolean screen)
	{
		for (PlayerEventInfo pi : _players)
		{
			if (screen)
			{
				pi.screenMessage(msg, "", true);
				continue;
			}
			pi.sendMessage(msg);
		}
	}
	
	public int getAverageLevel()
	{
		int i = 0;
		for (PlayerEventInfo player : _players)
		{
			i += player.getLevel();
		}
		i = Math.round(i / _players.size());
		return i;
	}
	
	public boolean isChosen()
	{
		return _choosen;
	}
	
	public void setIsChosen(boolean b)
	{
		_choosen = b;
	}
}
