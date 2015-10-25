/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.team;

import java.util.Map;

import javolution.util.FastMap;
import cz.nxs.interf.PlayerEventInfo;

public class KoreanTeam extends EventTeam
{
	private final Map<Integer, Integer> _players;
	private int _order;
	private int _nextPlayer;
	private PlayerEventInfo _fighting;
	
	public KoreanTeam(int teamId, String teamName)
	{
		super(teamId, teamName);
		_players = new FastMap<>(getTeamSize());
		_order = 0;
		_nextPlayer = 0;
		_fighting = null;
	}
	
	public boolean isFighting(PlayerEventInfo player)
	{
		return (_fighting != null) && (_fighting.getPlayersId() == player.getPlayersId());
	}
	
	@Override
	protected int getTeamSize()
	{
		return 4;
	}
	
	@Override
	public void addPlayer(PlayerEventInfo pi, boolean init)
	{
		super.addPlayer(pi, init);
		++_order;
		_players.put(_order, pi.getPlayersId());
	}
	
	public PlayerEventInfo getNextPlayer()
	{
		if (getPlayers().isEmpty())
		{
			return null;
		}
		int next = 0;
		do
		{
			++_nextPlayer;
		}
		while ((next = _players.get(_nextPlayer).intValue()) == 0);
		for (PlayerEventInfo pi : getPlayers())
		{
			if (pi.getPlayersId() != next)
			{
				continue;
			}
			_fighting = pi;
			return pi;
		}
		return null;
	}
}
