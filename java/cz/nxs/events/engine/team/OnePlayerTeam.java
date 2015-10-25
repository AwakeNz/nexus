/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.team;

import cz.nxs.interf.PlayerEventInfo;

public class OnePlayerTeam extends EventTeam
{
	public OnePlayerTeam(int teamId, String teamName)
	{
		super(teamId, teamName);
	}
	
	public PlayerEventInfo getPlayer()
	{
		if (getPlayers().isEmpty())
		{
			return null;
		}
		return getPlayers().getFirst();
	}
	
	@Override
	protected int getTeamSize()
	{
		return 1;
	}
}
