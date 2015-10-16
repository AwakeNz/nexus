/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.handler.IAdminCommandHandler
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  cz.nxs.l2j.handler.NexusAdminCommand
 */
package cz.nxs.interf.handlers;

import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.handler.NexusAdminCommand;

public abstract class AdminCommandHandlerInstance implements IAdminCommandHandler, NexusAdminCommand
{
	@Override
	public abstract boolean useAdminCommand(String var1, PlayerEventInfo var2);
	
	@Override
	public final boolean useAdminCommand(String command, L2PcInstance player)
	{
		return this.useAdminCommand(command, player.getEventInfo());
	}
}
