/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.L2Object
 *  com.l2jserver.gameserver.model.actor.L2Character
 *  com.l2jserver.gameserver.model.actor.instance.L2DoorInstance
 *  cz.nxs.l2j.delegate.IDoorData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;

import cz.nxs.l2j.delegate.IDoorData;

public class DoorData extends CharacterData implements IDoorData
{
	protected L2DoorInstance _owner;
	
	public DoorData(L2DoorInstance d)
	{
		super(d);
		this._owner = d;
	}
	
	@Override
	public L2DoorInstance getOwner()
	{
		return this._owner;
	}
	
	@Override
	public int getDoorId()
	{
		return this._owner.getDoorId();
	}
	
	@Override
	public boolean isOpened()
	{
		return this._owner.getOpen();
	}
	
	@Override
	public void openMe()
	{
		this._owner.openMe();
	}
	
	@Override
	public void closeMe()
	{
		this._owner.closeMe();
	}
}
