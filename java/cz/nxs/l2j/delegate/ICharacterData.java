/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.DoorData
 */
package cz.nxs.l2j.delegate;

import com.l2jserver.gameserver.enums.ChatType;

import cz.nxs.events.engine.base.Loc;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.DoorData;

public interface ICharacterData
{
	public String getName();
	
	public int getObjectId();
	
	public Loc getLoc();
	
	public double getPlanDistanceSq(int var1, int var2);
	
	public boolean isDoor();
	
	public DoorData getDoorData();
	
	public void startAbnormalEffect(int var1);
	
	public void stopAbnormalEffect(int var1);
	
	public void creatureSay(ChatType var1, String var2, String var3);
	
	public void doDie(CharacterData var1);
	
	public boolean isDead();
	
	public PlayerEventInfo getEventInfo();
}
