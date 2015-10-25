/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.L2Object
 *  com.l2jserver.gameserver.model.actor.L2Character
 *  com.l2jserver.gameserver.model.actor.L2Playable
 *  com.l2jserver.gameserver.model.actor.instance.L2DoorInstance
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  com.l2jserver.gameserver.model.skills.L2Skill
 *  com.l2jserver.gameserver.network.serverpackets.CreatureSay
 *  com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket
 *  cz.nxs.events.engine.base.Loc
 *  cz.nxs.l2j.delegate.ICharacterData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.enums.ChatType;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;

import cz.nxs.events.engine.base.Loc;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.delegate.ICharacterData;

public abstract class CharacterData extends ObjectData implements ICharacterData
{
	protected L2Character _owner;
	
	public CharacterData(L2Character cha)
	{
		super(cha);
		this._owner = cha;
	}
	
	@Override
	public L2Character getOwner()
	{
		return this._owner;
	}
	
	@Override
	public double getPlanDistanceSq(int targetX, int targetY)
	{
		return this._owner.getPlanDistanceSq(targetX, targetY);
	}
	
	@Override
	public Loc getLoc()
	{
		return new Loc(this._owner.getX(), this._owner.getY(), this._owner.getZ(), this._owner.getHeading());
	}
	
	@Override
	public int getObjectId()
	{
		return this._owner.getObjectId();
	}
	
	@Override
	public boolean isDoor()
	{
		return this._owner instanceof L2DoorInstance;
	}
	
	@Override
	public DoorData getDoorData()
	{
		return this.isDoor() ? new DoorData((L2DoorInstance) this._owner) : null;
	}
	
	@Override
	public void startAbnormalEffect(int mask)
	{
		this._owner.startAbnormalEffect(mask);
	}
	
	@Override
	public void stopAbnormalEffect(int mask)
	{
		this._owner.stopAbnormalEffect(mask);
	}
	
	@Override
	public PlayerEventInfo getEventInfo()
	{
		if (this._owner instanceof L2Playable)
		{
			return ((L2Playable) this._owner).getActingPlayer().getEventInfo();
		}
		return null;
	}
	
	@Override
	public String getName()
	{
		return this._owner.getName();
	}
	
	@Override
	public void creatureSay(ChatType channel, String charName, String text)
	{
		this._owner.broadcastPacket(new CreatureSay(_owner.getObjectId(), channel, charName, text));
	}
	
	@Override
	public void doDie(CharacterData killer)
	{
		this._owner.reduceCurrentHp(this._owner.getCurrentHp() * 2.0, killer.getOwner(), null);
	}
	
	@Override
	public boolean isDead()
	{
		return this._owner.isDead();
	}
}
