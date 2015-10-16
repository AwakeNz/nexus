/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.ItemData
 *  cz.nxs.interf.delegate.NpcData
 *  cz.nxs.interf.delegate.SkillData
 */
package cz.nxs.events;

import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.SkillDataEvent;

public interface EventGame
{
	public EventPlayerData createPlayerData(PlayerEventInfo var1);
	
	public EventPlayerData getPlayerData(PlayerEventInfo var1);
	
	public void clearEvent();
	
	public boolean canAttack(PlayerEventInfo var1, CharacterData var2);
	
	public boolean onAttack(CharacterData var1, CharacterData var2);
	
	public boolean canSupport(PlayerEventInfo var1, CharacterData var2);
	
	public void onKill(PlayerEventInfo var1, CharacterData var2);
	
	public void onDie(PlayerEventInfo var1, CharacterData var2);
	
	public void onDamageGive(CharacterData var1, CharacterData var2, int var3, boolean var4);
	
	public void onDisconnect(PlayerEventInfo var1);
	
	public boolean addDisconnectedPlayer(PlayerEventInfo var1, EventManager.DisconnectedPlayerData var2);
	
	public boolean onSay(PlayerEventInfo var1, String var2, int var3);
	
	public boolean onNpcAction(PlayerEventInfo var1, NpcData var2);
	
	public boolean canUseItem(PlayerEventInfo var1, ItemData var2);
	
	public void onItemUse(PlayerEventInfo var1, ItemData var2);
	
	public boolean canUseSkill(PlayerEventInfo var1, SkillDataEvent var2);
	
	public void onSkillUse(PlayerEventInfo var1, SkillDataEvent var2);
	
	public boolean canDestroyItem(PlayerEventInfo var1, ItemData var2);
	
	public boolean canInviteToParty(PlayerEventInfo var1, PlayerEventInfo var2);
	
	public boolean canTransform(PlayerEventInfo var1);
	
	public boolean canBeDisarmed(PlayerEventInfo var1);
	
	public int allowTransformationSkill(PlayerEventInfo var1, SkillDataEvent var2);
	
	public boolean canSaveShortcuts(PlayerEventInfo var1);
	
	public int isSkillOffensive(SkillDataEvent var1);
	
	public boolean isSkillNeutral(SkillDataEvent var1);
	
	public void playerWentAfk(PlayerEventInfo var1, boolean var2, int var3);
	
	public void playerReturnedFromAfk(PlayerEventInfo var1);
}
