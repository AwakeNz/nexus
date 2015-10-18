/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.Config
 *  com.l2jserver.gameserver.GameTimeController
 *  com.l2jserver.gameserver.ThreadPoolManager
 *  com.l2jserver.gameserver.ai.CtrlIntention
 *  com.l2jserver.gameserver.ai.L2CharacterAI
 *  com.l2jserver.gameserver.datatables.SkillTable
 *  com.l2jserver.gameserver.instancemanager.TransformationManager
 *  com.l2jserver.gameserver.model.L2Object
 *  com.l2jserver.gameserver.model.L2Party
 *  com.l2jserver.gameserver.model.L2Radar
 *  com.l2jserver.gameserver.model.L2ShortCut
 *  com.l2jserver.gameserver.model.Location
 *  com.l2jserver.gameserver.model.actor.L2Character
 *  com.l2jserver.gameserver.model.actor.L2Npc
 *  com.l2jserver.gameserver.model.actor.L2Summon
 *  com.l2jserver.gameserver.model.actor.appearance.PcAppearance
 *  com.l2jserver.gameserver.model.actor.instance.L2CubicInstance
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  com.l2jserver.gameserver.model.actor.templates.L2PcTemplate
 *  com.l2jserver.gameserver.model.base.ClassType
 *  com.l2jserver.gameserver.model.base.PlayerClass
 *  com.l2jserver.gameserver.model.effects.AbnormalEffect
 *  com.l2jserver.gameserver.model.effects.L2Effect
 *  com.l2jserver.gameserver.model.itemcontainer.PcInventory
 *  com.l2jserver.gameserver.model.items.instance.L2ItemInstance
 *  com.l2jserver.gameserver.model.olympiad.OlympiadManager
 *  com.l2jserver.gameserver.model.skills.L2Skill
 *  com.l2jserver.gameserver.network.L2GameClient
 *  com.l2jserver.gameserver.network.serverpackets.ActionFailed
 *  com.l2jserver.gameserver.network.serverpackets.CreatureSay
 *  com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage
 *  com.l2jserver.gameserver.network.serverpackets.InventoryUpdate
 *  com.l2jserver.gameserver.network.serverpackets.ItemList
 *  com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket
 *  com.l2jserver.gameserver.network.serverpackets.MagicSkillLaunched
 *  com.l2jserver.gameserver.network.serverpackets.MagicSkillUse
 *  com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage
 *  com.l2jserver.gameserver.network.serverpackets.PlaySound
 *  com.l2jserver.gameserver.network.serverpackets.SetupGauge
 *  com.l2jserver.gameserver.network.serverpackets.ShortCutInit
 *  com.l2jserver.gameserver.network.serverpackets.ShortCutRegister
 *  com.l2jserver.gameserver.network.serverpackets.SkillCoolTime
 *  com.l2jserver.gameserver.util.Broadcast
 *  com.l2jserver.gameserver.util.PlayerEventStatus
 *  cz.nxs.events.EventGame
 *  cz.nxs.events.engine.EventConfig
 *  cz.nxs.events.engine.EventManager
 *  cz.nxs.events.engine.base.EventPlayerData
 *  cz.nxs.events.engine.base.EventType
 *  cz.nxs.events.engine.base.Loc
 *  cz.nxs.events.engine.base.PvPEventPlayerData
 *  cz.nxs.events.engine.main.MainEventManager
 *  cz.nxs.events.engine.main.events.AbstractMainEvent
 *  cz.nxs.events.engine.mini.MiniEventGame
 *  cz.nxs.events.engine.mini.MiniEventManager
 *  cz.nxs.events.engine.stats.EventStatsManager
 *  cz.nxs.events.engine.team.EventTeam
 *  cz.nxs.l2j.CallBack
 *  cz.nxs.l2j.ClassType
 *  cz.nxs.l2j.INexusOut
 *  cz.nxs.l2j.IPlayerEventInfo
 *  cz.nxs.l2j.IValues
 *  javolution.util.FastList
 *  javolution.util.FastMap
 *  org.mmocore.network.MMOConnection
 */
package cz.nxs.interf;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import javolution.util.FastList;

import com.l2jserver.Config;
import com.l2jserver.gameserver.GameTimeController;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.data.xml.impl.TransformData;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.ChatType;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.PlayerClass;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.olympiad.OlympiadManager;
import com.l2jserver.gameserver.model.skills.AbnormalVisualEffect;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jserver.gameserver.network.serverpackets.ItemList;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.network.serverpackets.PlaySound;
import com.l2jserver.gameserver.network.serverpackets.SetupGauge;
import com.l2jserver.gameserver.network.serverpackets.ShortCutInit;
import com.l2jserver.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jserver.gameserver.network.serverpackets.SkillCoolTime;
import com.l2jserver.gameserver.util.Broadcast;

import cz.nxs.events.EventGame;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.PvPEventPlayerData;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.mini.MiniEventGame;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.stats.EventStatsManager;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.interf.delegate.ShortCutData;
import cz.nxs.interf.delegate.SkillDataEvent;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.ClassType;
import cz.nxs.l2j.IPlayerEventInfo;
import cz.nxs.l2j.IValues;

public class PlayerEventInfo implements IPlayerEventInfo
{
	public static final boolean AFK_CHECK_ENABLED = EventConfig.getInstance().getGlobalConfigBoolean("afkChecksEnabled");
	public static final int AFK_WARNING_DELAY = EventConfig.getInstance().getGlobalConfigInt("afkWarningDelay");
	public static final int AFK_KICK_DELAY = EventConfig.getInstance().getGlobalConfigInt("afkKickDelay");
	protected final L2PcInstance _owner;
	private final int _playersId;
	private boolean _isInEvent;
	private boolean _isRegistered;
	private boolean _isInFFAEvent;
	private boolean _isSpectator;
	private boolean _canBuff;
	private boolean _canParty = true;
	private boolean _antifeedProtection;
	private boolean _titleUpdate;
	protected boolean _disableAfkCheck;
	private int _origNameColor;
	private Location _origLoc;
	private String _origTitle;
	private EventPlayerData _eventData;
	private int _status;
	protected EventGame _activeEvent;
	private EventTeam _eventTeam;
	private MiniEventManager _registeredMiniEvent;
	private EventType _registeredMainEvent;
	private AfkChecker _afkChecker;
	private Radar _radar;
	private boolean _hasMarkers;
	private final List<ShortCutData> _customShortcuts = new FastList<>();
	
	public PlayerEventInfo(L2PcInstance owner)
	{
		this._owner = owner;
		this._playersId = owner == null ? -1 : owner.getObjectId();
		this._isRegistered = false;
		this._isInEvent = false;
		this._isInFFAEvent = false;
		this._status = 0;
		this._disableAfkCheck = false;
		this._titleUpdate = true;
		this._hasMarkers = false;
	}
	
	@Override
	public void initOrigInfo()
	{
		this._owner.setEventStatus();
		this._origNameColor = this._owner.getAppearance().getNameColor();
		this._origTitle = this._owner.getTitle();
		this._origLoc = new Location(this._owner.getX(), this._owner.getY(), this._owner.getZ(), this._owner.getHeading());
	}
	
	@Override
	public void restoreData()
	{
		this._owner.getAppearance().setNameColor(this._origNameColor);
		this._owner.setTitle(this._origTitle);
		this._owner.getAppearance().setVisibleTitle(this._origTitle);
		this._owner.broadcastTitleInfo();
		this._owner.broadcastUserInfo();
		this.clean();
	}
	
	@Override
	public void onEventStart(EventGame event)
	{
		this.initOrigInfo();
		this._isInEvent = true;
		this._activeEvent = event;
		this._eventData = event.createPlayerData(this);
		if (AFK_CHECK_ENABLED)
		{
			this._afkChecker = new AfkChecker(this);
		}
	}
	
	@Override
	public void clean()
	{
		if (this._afkChecker != null)
		{
			this._afkChecker.stop();
		}
		if (this._radar != null)
		{
			this._radar.disable();
		}
		this._isRegistered = false;
		this._isInEvent = false;
		this._isInFFAEvent = false;
		this._registeredMiniEvent = null;
		this._registeredMainEvent = null;
		this._hasMarkers = false;
		this._activeEvent = null;
		this._eventTeam = null;
		this._canParty = true;
		this._eventData = null;
		this._status = 0;
	}
	
	@Override
	public void teleport(Loc loc, int delay, boolean randomOffset, int instanceId)
	{
		new Teleport(this._owner, loc, delay, randomOffset, instanceId);
	}
	
	@Override
	public void teleToLocation(Loc loc, boolean randomOffset)
	{
		this._owner.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), randomOffset);
	}
	
	@Override
	public void teleToLocation(int x, int y, int z, boolean randomOffset)
	{
		this._owner.teleToLocation(x, y, z, randomOffset);
	}
	
	@Override
	public void teleToLocation(int x, int y, int z, int heading, boolean randomOffset)
	{
		this._owner.teleToLocation(x, y, z, heading, randomOffset);
	}
	
	@Override
	public void setXYZInvisible(int x, int y, int z)
	{
		this._owner.setXYZInvisible(x, y, z);
	}
	
	@Override
	public void setFame(int count)
	{
		this._owner.setFame(count);
	}
	
	@Override
	public int getFame()
	{
		return this._owner.getFame();
	}
	
	protected void notifyKill(L2Character target)
	{
		if (!((this._activeEvent == null) || this._isSpectator))
		{
			this._activeEvent.onKill(this, new CharacterData(target));
		}
	}
	
	protected void notifyDie(L2Character killer)
	{
		if (!((this._activeEvent == null) || this._isSpectator))
		{
			this._activeEvent.onDie(this, new CharacterData(killer));
		}
	}
	
	protected void notifyDisconnect()
	{
		if (!((this._activeEvent == null) || this._isSpectator))
		{
			this._activeEvent.onDisconnect(this);
		}
		if (this._registeredMainEvent != null)
		{
			EventManager.getInstance().getMainEventManager().unregisterPlayer(this, true);
		}
		else if (this._registeredMiniEvent != null)
		{
			// empty if block
		}
		EventStatsManager.getInstance().onDisconnect(this);
		PlayerBase.getInstance().eventEnd(this);
	}
	
	protected boolean canAttack(L2Character target)
	{
		if (!((this._activeEvent == null) || this._isSpectator))
		{
			return this._activeEvent.canAttack(this, new CharacterData(target));
		}
		return true;
	}
	
	protected boolean canSupport(L2Character target)
	{
		if (!((this._activeEvent == null) || this._isSpectator))
		{
			return this._activeEvent.canSupport(this, new CharacterData(target));
		}
		return true;
	}
	
	public void onAction()
	{
		if (this._afkChecker != null)
		{
			this._afkChecker.onAction();
		}
	}
	
	protected void onDamageGive(L2Character target, int ammount, boolean isDOT)
	{
		if (!((this._activeEvent == null) || this._isSpectator))
		{
			this._activeEvent.onDamageGive(this.getCharacterData(), new CharacterData(target), ammount, isDOT);
		}
	}
	
	protected boolean notifySay(String text, int channel)
	{
		if (this._activeEvent != null)
		{
			return this._activeEvent.onSay(this, text, channel);
		}
		return true;
	}
	
	protected boolean notifyNpcAction(L2Npc npc)
	{
		if (this._isSpectator)
		{
			return true;
		}
		if (EventManager.getInstance().showNpcHtml(this, new NpcData(npc)))
		{
			return true;
		}
		if (this._activeEvent != null)
		{
			return this._activeEvent.onNpcAction(this, new NpcData(npc));
		}
		return false;
	}
	
	protected boolean canUseItem(L2ItemInstance item)
	{
		if (this._isSpectator)
		{
			return false;
		}
		if (this._activeEvent != null)
		{
			return this._activeEvent.canUseItem(this, new ItemData(item));
		}
		return true;
	}
	
	protected void notifyItemUse(L2ItemInstance item)
	{
		if (this._activeEvent != null)
		{
			this._activeEvent.onItemUse(this, new ItemData(item));
		}
	}
	
	protected boolean canUseSkill(Skill skill)
	{
		if (this._isSpectator)
		{
			return false;
		}
		if (this._activeEvent != null)
		{
			return _activeEvent.canUseSkill(this, new SkillDataEvent(skill));
		}
		return true;
	}
	
	protected void onUseSkill(Skill skill)
	{
		if (this._activeEvent != null)
		{
			this._activeEvent.onSkillUse(this, new SkillDataEvent(skill));
		}
	}
	
	protected boolean canShowToVillageWindow()
	{
		return false;
	}
	
	protected boolean canDestroyItem(L2ItemInstance item)
	{
		if (this._activeEvent != null)
		{
			return this._activeEvent.canDestroyItem(this, new ItemData(item));
		}
		return true;
	}
	
	protected boolean canInviteToParty(PlayerEventInfo player, PlayerEventInfo target)
	{
		if (this._activeEvent != null)
		{
			return this._activeEvent.canInviteToParty(player, target);
		}
		return true;
	}
	
	protected boolean canTransform(PlayerEventInfo player)
	{
		if (this._activeEvent != null)
		{
			return this._activeEvent.canTransform(player);
		}
		return true;
	}
	
	protected boolean canBeDisarmed(PlayerEventInfo player)
	{
		if (this._activeEvent != null)
		{
			return this._activeEvent.canBeDisarmed(player);
		}
		return true;
	}
	
	protected int allowTransformationSkill(Skill s)
	{
		if (this._activeEvent != null)
		{
			return this._activeEvent.allowTransformationSkill(this, new SkillDataEvent(s));
		}
		return 0;
	}
	
	protected boolean canSaveShortcuts()
	{
		if (this._activeEvent != null)
		{
			return this._activeEvent.canSaveShortcuts(this);
		}
		return true;
	}
	
	@Override
	public void setInstanceId(int id)
	{
		this._owner.setInstanceId(id);
	}
	
	@Override
	public void sendPacket(String html)
	{
		this.sendHtmlText(html);
	}
	
	@Override
	public void screenMessage(String message, String name, boolean special)
	{
		L2GameServerPacket packet = special ? new ExShowScreenMessage(message, 5000) : new CreatureSay(0, ChatType.PARTYROOM_COMMANDER, name, message);
		if (_owner != null)
		{
			_owner.sendPacket(packet);
		}
	}
	
	public void creatureSay(String message, String announcer, ChatType channel)
	{
		if (_owner != null)
		{
			_owner.sendPacket(new CreatureSay(0, channel, announcer, message));
		}
	}
	
	@Override
	public void sendMessage(String message)
	{
		if (_owner != null)
		{
			_owner.sendMessage(message);
		}
	}
	
	@Override
	public void sendEventScoreBar(String text)
	{
		if (this._owner != null)
		{
			this._owner.sendPacket(new ExShowScreenMessage(1, -1, 3, 0, 1, 0, 0, true, 2000, false, text));
		}
	}
	
	@Override
	public void broadcastUserInfo()
	{
		if (this._owner != null)
		{
			this._owner.broadcastUserInfo();
		}
	}
	
	@Override
	public void broadcastTitleInfo()
	{
		if (this._owner != null)
		{
			this._owner.broadcastTitleInfo();
		}
	}
	
	@Override
	public void sendSkillList()
	{
		this._owner.sendSkillList();
	}
	
	@Override
	public void transform(int transformId)
	{
		if (this._owner != null)
		{
			TransformData.getInstance().transformPlayer(transformId, _owner);
		}
	}
	
	@Override
	public boolean isTransformed()
	{
		if ((this._owner != null) && this._owner.isTransformed())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void untransform(boolean removeEffects)
	{
		if ((this._owner != null) && this._owner.isTransformed())
		{
			this._owner.stopTransformation(removeEffects);
		}
	}
	
	@Override
	public ItemData addItem(int id, int ammount, boolean msg)
	{
		return new ItemData(this._owner.addItem("Event Reward", id, ammount, null, msg));
	}
	
	@Override
	public void addExpAndSp(long exp, int sp)
	{
		this._owner.addExpAndSp(exp, sp);
	}
	
	@Override
	public void doDie()
	{
		this._owner.doDie(this._owner);
	}
	
	@Override
	public void doDie(CharacterData killer)
	{
		this._owner.doDie(killer.getOwner());
	}
	
	@Override
	public ItemData[] getItems()
	{
		FastList items = new FastList();
		for (L2ItemInstance item : this._owner.getInventory().getItems())
		{
			items.add(new ItemData(item));
		}
		return items.toArray(new ItemData[items.size()]);
	}
	
	@Override
	public void getPetSkillEffects(int skillId, int level)
	{
		Skill skill;
		if ((this._owner.getPet() != null) && ((skill = SkillData.getInstance().getSkill(skillId, level)) != null))
		{
			skill.applyEffects(this._owner.getPet(), this._owner.getPet());
		}
	}
	
	@Override
	public void getSkillEffects(int skillId, int level)
	{
		Skill skill = SkillData.getInstance().getSkill(skillId, level);
		if (skill != null)
		{
			skill.applyEffects(_owner, _owner);
		}
	}
	
	@Override
	public void addSkill(SkillDataEvent skill, boolean store)
	{
		getOwner().addSkill(SkillData.getInstance().getSkill(skill.getId(), skill.getLevel()), store);
	}
	
	@Override
	public void removeSkill(int id)
	{
		this.getOwner().removeSkill(id);
	}
	
	@Override
	public void removeCubics()
	{
		if (!this._owner.getCubics().isEmpty())
		{
			for (L2CubicInstance cubic : this._owner.getCubics().values())
			{
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			this._owner.getCubics().clear();
		}
	}
	
	@Override
	public void removeSummon()
	{
		if (this._owner.getPet() != null)
		{
			this._owner.getPet().unSummon(this._owner);
		}
	}
	
	@Override
	public boolean hasPet()
	{
		return this._owner.getPet() != null;
	}
	
	@Override
	public void removeBuffsFromPet()
	{
		if ((this._owner != null) && (this._owner.getPet() != null))
		{
			this._owner.getPet().stopAllEffects();
		}
	}
	
	@Override
	public void removeBuffs()
	{
		if (this._owner != null)
		{
			this._owner.stopAllEffects();
		}
	}
	
	@Override
	public int getBuffsCount()
	{
		return this._owner.getBuffCount();
	}
	
	@Override
	public int getDancesCount()
	{
		return this._owner.getDanceCount();
	}
	
	@Override
	public int getPetBuffCount()
	{
		if (this._owner.getPet() != null)
		{
			return this._owner.getPet().getBuffCount();
		}
		return 0;
	}
	
	@Override
	public int getPetDanceCount()
	{
		if (this._owner.getPet() != null)
		{
			return this._owner.getPet().getDanceCount();
		}
		return 0;
	}
	
	@Override
	public int getMaxBuffCount()
	{
		return this._owner.getBuffCount();
	}
	
	@Override
	public int getMaxDanceCount()
	{
		return Config.DANCES_MAX_AMOUNT;
	}
	
	@Override
	public void removeBuff(int id)
	{
		if (this._owner != null)
		{
			this._owner.stopSkillEffects(true, id);
		}
	}
	
	@Override
	public void abortCasting()
	{
		if (this._owner.isCastingNow())
		{
			this._owner.abortCast();
		}
		if (this._owner.isAttackingNow())
		{
			this._owner.abortAttack();
		}
	}
	
	@Override
	public void playSound(String file)
	{
		this._owner.sendPacket(new PlaySound(file));
	}
	
	@Override
	public void setVisible()
	{
		_owner.getAppearance().setGhostMode(isVisible());
	}
	
	@Override
	public void rebuffPlayer()
	{
	}
	
	@Override
	public void enableAllSkills()
	{
		for (Skill skill : this._owner.getAllSkills())
		{
			if (skill.getReuseDelay() > 900000)
			{
				continue;
			}
			this._owner.enableSkill(skill);
		}
		this._owner.sendPacket(new SkillCoolTime(this._owner));
	}
	
	@Override
	public void sendSetupGauge(int time)
	{
		SetupGauge sg = new SetupGauge(0, time);
		this._owner.sendPacket(sg);
	}
	
	@Override
	public void root()
	{
		this._owner.setIsImmobilized(true);
		this._owner.startAbnormalVisualEffect(AbnormalVisualEffect.STEALTH);
	}
	
	@Override
	public void unroot()
	{
		if (this._owner.isImmobilized())
		{
			this._owner.setIsImmobilized(false);
		}
		this._owner.stopAbnormalVisualEffect(AbnormalVisualEffect.STEALTH);
	}
	
	@Override
	public void paralizeEffect(boolean b)
	{
		if (b)
		{
			this.getOwner().startAbnormalVisualEffect(AbnormalVisualEffect.FREEZING);
		}
		else
		{
			this.getOwner().stopAbnormalVisualEffect(AbnormalVisualEffect.FREEZING);
		}
	}
	
	@Override
	public void setIsParalyzed(boolean b)
	{
		this._owner.setIsParalyzed(b);
	}
	
	@Override
	public void setIsInvul(boolean b)
	{
		this._owner.setIsInvul(b);
	}
	
	@Override
	public void setCanInviteToParty(boolean b)
	{
		this._canParty = b;
	}
	
	@Override
	public boolean canInviteToParty()
	{
		return this._canParty;
	}
	
	@Override
	public void showEventEscapeEffect()
	{
		this._owner.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		this._owner.setTarget(this._owner);
		this._owner.disableAllSkills();
		MagicSkillUse msk = new MagicSkillUse(this._owner, 1050, 1, 10000, 0);
		Broadcast.toSelfAndKnownPlayersInRadius(this._owner, msk, 810000);
		SetupGauge sg = new SetupGauge(0, 10000);
		this._owner.sendPacket(sg);
		this._owner.forceIsCasting(GameTimeController.getInstance().getGameTicks() + 100);
	}
	
	@Override
	public void startAntifeedProtection(boolean broadcast)
	{
		_owner.startAntifeedProtection(true, broadcast);
		_antifeedProtection = true;
		if (broadcast)
		{
			this.broadcastUserInfo();
		}
	}
	
	@Override
	public void stopAntifeedProtection(boolean broadcast)
	{
		_owner.startAntifeedProtection(false, broadcast);
		_antifeedProtection = false;
		if (broadcast)
		{
			this.broadcastUserInfo();
		}
	}
	
	@Override
	public boolean hasAntifeedProtection()
	{
		return this._antifeedProtection;
	}
	
	@Override
	public void broadcastSkillUse(CharacterData owner, CharacterData target, int skillId, int level)
	{
		Skill skill = SkillData.getInstance().getSkill(skillId, level);
		if (skill != null)
		{
			this.getOwner().broadcastPacket(new MagicSkillUse(owner == null ? this.getOwner() : owner.getOwner(), target == null ? this.getOwner() : target.getOwner(), skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
		}
	}
	
	@Override
	public void broadcastSkillLaunched(CharacterData owner, CharacterData target, int skillId, int level)
	{
		Skill skill = SkillData.getInstance().getSkill(skillId, level);
		if (skill != null)
		{
			this.getOwner().broadcastPacket(new MagicSkillLaunched(owner == null ? this.getOwner() : owner.getOwner(), skill.getId(), skill.getLevel(), new L2Object[]
			{
				target.getOwner()
			}));
		}
	}
	
	@Override
	public void enterObserverMode(int x, int y, int z)
	{
		this._owner.enterOlympiadObserverMode(new Location(x, y, z), 0);
	}
	
	@Override
	public void removeObserveMode()
	{
		this.setIsSpectator(false);
		this.setActiveGame(null);
		this._owner.leaveOlympiadObserverMode();
		this._owner.setInstanceId(0);
		this._owner.teleToLocation(this.getOrigLoc().getX(), this.getOrigLoc().getY(), this.getOrigLoc().getZ(), true);
	}
	
	@Override
	public void sendStaticPacket()
	{
		this._owner.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void sendHtmlText(String text)
	{
		NpcHtmlMessage msg = new NpcHtmlMessage(0);
		msg.setHtml(text);
		this._owner.sendPacket(msg);
	}
	
	@Override
	public void sendHtmlPage(String path)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile(null, path);
		this._owner.sendPacket(html);
		this.sendStaticPacket();
	}
	
	@Override
	public void startAbnormalEffect(int mask)
	{
		IValues val = CallBack.getInstance().getValues();
		if (mask == val.ABNORMAL_S_INVINCIBLE())
		{
			this._owner.startSpecialEffect(mask);
		}
		else
		{
			this._owner.startAbnormalEffect(mask);
		}
	}
	
	@Override
	public void stopAbnormalEffect(int mask)
	{
		IValues val = CallBack.getInstance().getValues();
		if (mask == val.ABNORMAL_S_INVINCIBLE())
		{
			this._owner.stopSpecialEffect(mask);
		}
		else
		{
			this._owner.stopAbnormalEffect(mask);
		}
	}
	
	@Override
	public void removeOriginalShortcuts()
	{
		if (this._owner == null)
		{
			return;
		}
		this._owner.removeAllShortcuts();
		this._owner.sendPacket(new ShortCutInit(this._owner));
	}
	
	@Override
	public void restoreOriginalShortcuts()
	{
		if (this._owner == null)
		{
			return;
		}
		this._owner.restoreShortCuts();
		this._owner.sendPacket(new ShortCutInit(this._owner));
	}
	
	@Override
	public void removeCustomShortcuts()
	{
		if (this._owner == null)
		{
			return;
		}
		for (ShortCutData sh : this._customShortcuts)
		{
			this._owner.deleteShortCut(sh.getSlot(), sh.getPage(), false);
		}
		this._customShortcuts.clear();
	}
	
	@Override
	public void registerShortcut(ShortCutData shortcut, boolean eventShortcut)
	{
		if (eventShortcut)
		{
			this._customShortcuts.add(shortcut);
		}
		if (this._owner != null)
		{
			L2ShortCut sh = new L2ShortCut(shortcut.getSlot(), shortcut.getPage(), shortcut.getType(), shortcut.getId(), shortcut.getLevel(), shortcut.getCharacterType());
			this._owner.sendPacket(new ShortCutRegister(sh));
			this._owner.registerShortCut(sh, !eventShortcut);
		}
	}
	
	@Override
	public void removeShortCut(ShortCutData shortcut, boolean eventShortcut)
	{
		if (eventShortcut && this._customShortcuts.contains(shortcut))
		{
			this._customShortcuts.remove(shortcut);
		}
		if (this._owner != null)
		{
			this._owner.deleteShortCut(shortcut.getSlot(), shortcut.getPage(), !eventShortcut);
		}
	}
	
	@Override
	public ShortCutData createItemShortcut(int slotId, int pageId, ItemData item)
	{
		ShortCutData shortcut = new ShortCutData(slotId, pageId, Values.getInstance().TYPE_ITEM(), item.getObjectId(), 0, 1);
		return shortcut;
	}
	
	public ShortCutData createSkillShortcut(int slotId, int pageId, SkillData skill)
	{
		ShortCutData shortcut = new ShortCutData(slotId, pageId, Values.getInstance().TYPE_SKILL(), skill.getId(), skill.getLevel(), 1);
		return shortcut;
	}
	
	@Override
	public ShortCutData createActionShortcut(int slotId, int pageId, int actionId)
	{
		ShortCutData shortcut = new ShortCutData(slotId, pageId, Values.getInstance().TYPE_ACTION(), actionId, 0, 1);
		return shortcut;
	}
	
	public L2PcInstance getOwner()
	{
		return this._owner;
	}
	
	@Override
	public boolean isOnline()
	{
		return this.isOnline(false);
	}
	
	@Override
	public boolean isOnline(boolean strict)
	{
		if (strict)
		{
			return (this._owner != null) && this._owner.isOnline();
		}
		return this._owner != null;
	}
	
	@Override
	public boolean isDead()
	{
		return this._owner.isDead();
	}
	
	@Override
	public boolean isVisible()
	{
		return this._owner.isVisible();
	}
	
	@Override
	public void doRevive()
	{
		this._owner.doRevive();
	}
	
	@Override
	public CharacterData getTarget()
	{
		if (!((this._owner.getTarget() != null) && (this._owner.getTarget() instanceof L2Character)))
		{
			return null;
		}
		return new CharacterData((L2Character) this._owner.getTarget());
	}
	
	@Override
	public String getPlayersName()
	{
		if (this._owner != null)
		{
			return this._owner.getName();
		}
		return "";
	}
	
	@Override
	public int getLevel()
	{
		if (this._owner != null)
		{
			return this._owner.getLevel();
		}
		return 0;
	}
	
	@Override
	public int getPvpKills()
	{
		return this._owner.getPvpKills();
	}
	
	@Override
	public int getPkKills()
	{
		return this._owner.getPkKills();
	}
	
	@Override
	public int getMaxHp()
	{
		return this._owner.getMaxHp();
	}
	
	@Override
	public int getMaxCp()
	{
		return this._owner.getMaxCp();
	}
	
	@Override
	public int getMaxMp()
	{
		return this._owner.getMaxMp();
	}
	
	@Override
	public void setCurrentHp(int hp)
	{
		this._owner.setCurrentHp(hp);
	}
	
	@Override
	public void setCurrentCp(int cp)
	{
		this._owner.setCurrentCp(cp);
	}
	
	@Override
	public void setCurrentMp(int mp)
	{
		this._owner.setCurrentMp(mp);
	}
	
	@Override
	public double getCurrentHp()
	{
		return this._owner.getCurrentHp();
	}
	
	@Override
	public double getCurrentCp()
	{
		return this._owner.getCurrentCp();
	}
	
	@Override
	public double getCurrentMp()
	{
		return this._owner.getCurrentMp();
	}
	
	@Override
	public void healPet()
	{
		if ((this._owner != null) && (this._owner.getPet() != null))
		{
			this._owner.getPet().setCurrentHp(this._owner.getPet().getMaxHp());
			this._owner.getPet().setCurrentMp(this._owner.getPet().getMaxMp());
		}
	}
	
	@Override
	public void setTitle(String title, boolean updateVisible)
	{
		this._owner.setTitle(title);
		if (updateVisible)
		{
			this._owner.getAppearance().setVisibleTitle(this._owner.getTitle());
		}
	}
	
	@Override
	public boolean isMageClass()
	{
		return this._owner.isMageClass();
	}
	
	@Override
	public int getClassIndex()
	{
		if (this._owner != null)
		{
			return this._owner.getClassIndex();
		}
		return 0;
	}
	
	@Override
	public int getActiveClass()
	{
		if (this._owner != null)
		{
			return this._owner.getActiveClass();
		}
		return 0;
	}
	
	@Override
	public String getClassName()
	{
		return this._owner.getTemplate().getClassId().name();
	}
	
	@Override
	public PartyData getParty()
	{
		if (this._owner.getParty() == null)
		{
			return null;
		}
		return new PartyData(this._owner.getParty());
	}
	
	@Override
	public boolean isFighter()
	{
		return PlayerClass.values()[this._owner.getActiveClass()].isOfType(com.l2jserver.gameserver.model.base.ClassType.Fighter);
	}
	
	@Override
	public boolean isPriest()
	{
		return PlayerClass.values()[this._owner.getActiveClass()].isOfType(com.l2jserver.gameserver.model.base.ClassType.Priest);
	}
	
	@Override
	public boolean isMystic()
	{
		return PlayerClass.values()[this._owner.getActiveClass()].isOfType(com.l2jserver.gameserver.model.base.ClassType.Mystic);
	}
	
	@Override
	public ClassType getClassType()
	{
		if (this.isFighter())
		{
			return ClassType.Fighter;
		}
		if (this.isMystic())
		{
			return ClassType.Mystic;
		}
		return ClassType.Priest;
	}
	
	@Override
	public int getX()
	{
		return this._owner.getX();
	}
	
	@Override
	public int getY()
	{
		return this._owner.getY();
	}
	
	@Override
	public int getZ()
	{
		return this._owner.getZ();
	}
	
	@Override
	public int getHeading()
	{
		return this._owner.getHeading();
	}
	
	@Override
	public int getInstanceId()
	{
		return this._owner.getInstanceId();
	}
	
	@Override
	public int getClanId()
	{
		return this._owner.getClanId();
	}
	
	@Override
	public boolean isGM()
	{
		return this._owner.isGM();
	}
	
	@Override
	public String getIp()
	{
		return this._owner.getClient().getConnection().getInetAddress().getHostAddress();
	}
	
	@Override
	public boolean isInJail()
	{
		return this._owner.isJailed();
	}
	
	@Override
	public boolean isInSiege()
	{
		return this._owner.isInSiege();
	}
	
	@Override
	public boolean isInDuel()
	{
		return this._owner.isInDuel();
	}
	
	@Override
	public boolean isInOlympiadMode()
	{
		return this._owner.isInOlympiadMode();
	}
	
	@Override
	public int getKarma()
	{
		return this._owner.getReputation();
	}
	
	@Override
	public boolean isCursedWeaponEquipped()
	{
		return this._owner.isCursedWeaponEquipped();
	}
	
	@Override
	public boolean isImmobilized()
	{
		return this._owner.isImmobilized();
	}
	
	@Override
	public boolean isParalyzed()
	{
		return this._owner.isParalyzed();
	}
	
	@Override
	public boolean isAfraid()
	{
		return this._owner.isAfraid();
	}
	
	@Override
	public boolean isOlympiadRegistered()
	{
		return OlympiadManager.getInstance().isRegistered(this._owner);
	}
	
	@Override
	public void sitDown()
	{
		if (this._owner == null)
		{
			return;
		}
		this._owner.sitDown();
		this._owner.getEventStatus().eventSitForced = true;
	}
	
	@Override
	public void standUp()
	{
		if (this._owner == null)
		{
			return;
		}
		this._owner.getEventStatus().eventSitForced = false;
		this._owner.standUp();
	}
	
	@Override
	public List<SkillDataEvent> getSkills()
	{
		FastList list = new FastList();
		for (Skill skill : this.getOwner().getAllSkills())
		{
			list.add(new SkillDataEvent(skill));
		}
		return list;
	}
	
	@Override
	public List<Integer> getSkillIds()
	{
		FastList list = new FastList();
		for (Skill skill : this.getOwner().getAllSkills())
		{
			list.add(skill.getId());
		}
		return list;
	}
	
	@Override
	public double getPlanDistanceSq(int targetX, int targetY)
	{
		return this._owner.getPlanDistanceSq(targetX, targetY);
	}
	
	@Override
	public double getDistanceSq(int targetX, int targetY, int targetZ)
	{
		return this._owner.getDistanceSq(targetX, targetY, targetZ);
	}
	
	@Override
	public boolean isRegistered()
	{
		return this._isRegistered;
	}
	
	@Override
	public boolean isInEvent()
	{
		return this._isInEvent;
	}
	
	@Override
	public EventPlayerData getEventData()
	{
		return this._eventData;
	}
	
	@Override
	public void setNameColor(int color)
	{
		this._owner.getAppearance().setNameColor(color);
		this._owner.broadcastUserInfo();
	}
	
	@Override
	public void setCanBuff(boolean canBuff)
	{
		this._canBuff = canBuff;
	}
	
	@Override
	public boolean canBuff()
	{
		return this._canBuff;
	}
	
	@Override
	public int getPlayersId()
	{
		return this._playersId;
	}
	
	@Override
	public int getKills()
	{
		return this._eventData instanceof PvPEventPlayerData ? ((PvPEventPlayerData) this._eventData).getKills() : 0;
	}
	
	@Override
	public int getDeaths()
	{
		return this._eventData instanceof PvPEventPlayerData ? ((PvPEventPlayerData) this._eventData).getDeaths() : 0;
	}
	
	@Override
	public int getScore()
	{
		return this._eventData.getScore();
	}
	
	@Override
	public int getStatus()
	{
		return this._status;
	}
	
	@Override
	public void raiseKills(int count)
	{
		if (this._eventData instanceof PvPEventPlayerData)
		{
			((PvPEventPlayerData) this._eventData).raiseKills(count);
		}
	}
	
	@Override
	public void raiseDeaths(int count)
	{
		if (this._eventData instanceof PvPEventPlayerData)
		{
			((PvPEventPlayerData) this._eventData).raiseDeaths(count);
		}
	}
	
	@Override
	public void raiseScore(int count)
	{
		this._eventData.raiseScore(count);
	}
	
	@Override
	public void setScore(int count)
	{
		this._eventData.setScore(count);
	}
	
	@Override
	public void setStatus(int count)
	{
		this._status = count;
	}
	
	@Override
	public void setKills(int count)
	{
		if (this._eventData instanceof PvPEventPlayerData)
		{
			((PvPEventPlayerData) this._eventData).setKills(count);
		}
	}
	
	@Override
	public void setDeaths(int count)
	{
		if (this._eventData instanceof PvPEventPlayerData)
		{
			((PvPEventPlayerData) this._eventData).setDeaths(count);
		}
	}
	
	@Override
	public boolean isInFFAEvent()
	{
		return this._isInFFAEvent;
	}
	
	@Override
	public void setIsRegisteredToMiniEvent(boolean b, MiniEventManager minievent)
	{
		this._isRegistered = b;
		this._registeredMiniEvent = minievent;
	}
	
	@Override
	public MiniEventManager getRegisteredMiniEvent()
	{
		return this._registeredMiniEvent;
	}
	
	@Override
	public void setIsRegisteredToMainEvent(boolean b, EventType event)
	{
		this._isRegistered = b;
		this._registeredMainEvent = event;
	}
	
	@Override
	public EventType getRegisteredMainEvent()
	{
		return this._registeredMainEvent;
	}
	
	@Override
	public MiniEventGame getActiveGame()
	{
		if (this._activeEvent instanceof MiniEventGame)
		{
			return (MiniEventGame) this._activeEvent;
		}
		return null;
	}
	
	@Override
	public AbstractMainEvent getActiveEvent()
	{
		if (this._activeEvent instanceof AbstractMainEvent)
		{
			return (AbstractMainEvent) this._activeEvent;
		}
		return null;
	}
	
	@Override
	public EventGame getEvent()
	{
		return this._activeEvent;
	}
	
	@Override
	public void setActiveGame(MiniEventGame game)
	{
		this._activeEvent = game;
	}
	
	@Override
	public void setEventTeam(EventTeam team)
	{
		this._eventTeam = team;
	}
	
	@Override
	public EventTeam getEventTeam()
	{
		return this._eventTeam;
	}
	
	@Override
	public int getTeamId()
	{
		if (this._eventTeam != null)
		{
			return this._eventTeam.getTeamId();
		}
		return -1;
	}
	
	@Override
	public Loc getOrigLoc()
	{
		return new Loc(this._origLoc.getX(), this._origLoc.getY(), this._origLoc.getZ());
	}
	
	@Override
	public void setIsSpectator(boolean _isSpectator)
	{
		this._isSpectator = _isSpectator;
	}
	
	@Override
	public boolean isSpectator()
	{
		return this._isSpectator;
	}
	
	@Override
	public boolean isEventRooted()
	{
		return this._disableAfkCheck;
	}
	
	@Override
	public boolean isTitleUpdated()
	{
		return this._titleUpdate;
	}
	
	@Override
	public void setTitleUpdated(boolean b)
	{
		this._titleUpdate = b;
	}
	
	@Override
	public ItemData getPaperdollItem(int slot)
	{
		return new ItemData(this.getOwner().getInventory().getPaperdollItem(slot));
	}
	
	@Override
	public void equipItem(ItemData item)
	{
		this.getOwner().getInventory().equipItemAndRecord(item.getOwner());
	}
	
	@Override
	public ItemData[] unEquipItemInBodySlotAndRecord(int slot)
	{
		L2ItemInstance[] is = this.getOwner().getInventory().unEquipItemInBodySlotAndRecord(slot);
		ItemData[] items = new ItemData[is.length];
		for (int i = 0; i < is.length; ++i)
		{
			items[i] = new ItemData(is[i]);
		}
		return items;
	}
	
	@Override
	public void destroyItemByItemId(int id, int count)
	{
		this.getOwner().getInventory().destroyItemByItemId("", id, count, null, (Object) null);
	}
	
	@Override
	public void inventoryUpdate(ItemData[] items)
	{
		InventoryUpdate iu = new InventoryUpdate();
		for (ItemData element : items)
		{
			iu.addModifiedItem(element.getOwner());
		}
		this.getOwner().sendPacket(iu);
		this.getOwner().sendPacket(new ItemList(this.getOwner(), false));
		this.getOwner().broadcastUserInfo();
	}
	
	@Override
	public Radar getRadar()
	{
		return this._radar;
	}
	
	@Override
	public void createRadar()
	{
		this._radar = new Radar(this);
	}
	
	@Override
	public void addRadarMarker(int x, int y, int z)
	{
		if (this._owner != null)
		{
			this._owner.getRadar().addMarker(x, y, z);
			this._hasMarkers = true;
		}
	}
	
	@Override
	public void removeRadarMarker(int x, int y, int z)
	{
		if (this._owner != null)
		{
			this._owner.getRadar().removeMarker(x, y, z);
		}
	}
	
	@Override
	public void removeRadarAllMarkers()
	{
		if ((this._owner != null) && this._hasMarkers)
		{
			this._owner.getRadar().removeAllMarkers();
			this._hasMarkers = false;
		}
	}
	
	@Override
	public void disableAfkCheck(boolean b)
	{
		this._disableAfkCheck = b;
		if (!(b || (this._afkChecker == null)))
		{
			this._afkChecker.check();
		}
	}
	
	@Override
	public int getTotalTimeAfk()
	{
		if (this._afkChecker == null)
		{
			return 0;
		}
		return Math.max(0, this._afkChecker.totalTimeAfk);
	}
	
	@Override
	public boolean isAfk()
	{
		if (this._afkChecker != null)
		{
			return this._afkChecker.isAfk;
		}
		return false;
	}
	
	@Override
	public AfkChecker getAfkChecker()
	{
		return this._afkChecker;
	}
	
	@Override
	public CharacterData getCharacterData()
	{
		return new CharacterData(this.getOwner());
	}
	
	public class AfkChecker implements Runnable
	{
		private final PlayerEventInfo player;
		private ScheduledFuture<?> _nextTask;
		protected boolean isAfk;
		protected int totalTimeAfk;
		private int tempTimeAfk;
		private boolean isWarned;
		
		public AfkChecker(PlayerEventInfo player)
		{
			this.player = player;
			this.isWarned = false;
			this.isAfk = false;
			this.totalTimeAfk = 0;
			this.tempTimeAfk = 0;
			this.check();
		}
		
		public void onAction()
		{
			if (!PlayerEventInfo.this.isInEvent())
			{
				return;
			}
			if (this._nextTask != null)
			{
				this._nextTask.cancel(false);
			}
			this.tempTimeAfk = 0;
			this.isWarned = false;
			if (this.isAfk)
			{
				PlayerEventInfo.this._owner.sendMessage("Welcome back. Total time spent AFK so far: " + this.totalTimeAfk);
				this.isAfk = false;
				if (PlayerEventInfo.this._activeEvent != null)
				{
					PlayerEventInfo.this._activeEvent.playerReturnedFromAfk(this.player);
				}
			}
			this.check();
		}
		
		@Override
		public synchronized void run()
		{
			if (!PlayerEventInfo.this.isInEvent())
			{
				return;
			}
			if (this.isWarned)
			{
				if (!(PlayerEventInfo.this._disableAfkCheck || PlayerEventInfo.this._owner.isDead()))
				{
					if (this.isAfk)
					{
						this.totalTimeAfk += 10;
						this.tempTimeAfk += 10;
					}
					else
					{
						this.isAfk = true;
					}
					if (PlayerEventInfo.this._activeEvent != null)
					{
						PlayerEventInfo.this._activeEvent.playerWentAfk(this.player, false, this.tempTimeAfk);
					}
				}
				this.check(10000);
			}
			else
			{
				if (!(PlayerEventInfo.this._disableAfkCheck || PlayerEventInfo.this._owner.isDead()))
				{
					this.isWarned = true;
					if (PlayerEventInfo.this.getActiveGame() != null)
					{
						PlayerEventInfo.this.getActiveGame().playerWentAfk(this.player, true, 0);
					}
					if (PlayerEventInfo.this.getActiveEvent() != null)
					{
						PlayerEventInfo.this.getActiveEvent().playerWentAfk(this.player, true, 0);
					}
				}
				this.check();
			}
		}
		
		protected synchronized void check()
		{
			if (PlayerEventInfo.this._disableAfkCheck)
			{
				return;
			}
			if (this._nextTask != null)
			{
				this._nextTask.cancel(false);
			}
			this._nextTask = ThreadPoolManager.getInstance().scheduleGeneral(this, this.isWarned ? (long) PlayerEventInfo.AFK_KICK_DELAY : (long) PlayerEventInfo.AFK_WARNING_DELAY);
		}
		
		private synchronized void check(long delay)
		{
			if (PlayerEventInfo.this._disableAfkCheck)
			{
				return;
			}
			if (this._nextTask != null)
			{
				this._nextTask.cancel(false);
			}
			if (this.isAfk)
			{
				this._nextTask = ThreadPoolManager.getInstance().scheduleGeneral(this, delay);
			}
		}
		
		public void stop()
		{
			if (this._nextTask != null)
			{
				this._nextTask.cancel(false);
			}
			this._nextTask = null;
			this.isAfk = false;
			this.isWarned = false;
			this.totalTimeAfk = 0;
			this.tempTimeAfk = 0;
		}
	}
	
	public class Radar
	{
		private final PlayerEventInfo player;
		private ScheduledFuture<?> refresh;
		private boolean enabled;
		private boolean repeat;
		private int newX;
		private int newY;
		private int newZ;
		private int currentX;
		private int currentY;
		private int currentZ;
		private boolean hasRadar;
		
		public Radar(PlayerEventInfo player)
		{
			this.repeat = false;
			this.player = player;
			this.refresh = null;
			this.enabled = false;
			this.hasRadar = false;
		}
		
		public void setLoc(int x, int y, int z)
		{
			this.newX = x;
			this.newY = y;
			this.newZ = z;
		}
		
		public void enable()
		{
			this.enabled = true;
			this.applyRadar();
		}
		
		public void disable()
		{
			this.enabled = false;
			if (this.hasRadar)
			{
				this.player.removeRadarMarker(this.currentX, this.currentY, this.currentZ);
				this.hasRadar = false;
			}
		}
		
		public void setRepeat(boolean nextRepeatPolicy)
		{
			if (!(this.enabled && (!this.repeat || nextRepeatPolicy)))
			{
				if (this.refresh != null)
				{
					this.refresh.cancel(false);
					this.refresh = null;
				}
			}
			else if (!this.repeat && nextRepeatPolicy)
			{
				if (this.refresh != null)
				{
					this.refresh.cancel(false);
					this.refresh = null;
				}
				this.refresh = CallBack.getInstance().getOut().scheduleGeneral(() -> Radar.this.applyRadar(), 10000);
			}
			this.repeat = nextRepeatPolicy;
		}
		
		private void applyRadar()
		{
			if (this.enabled)
			{
				if (this.hasRadar)
				{
					this.player.removeRadarMarker(this.currentX, this.currentY, this.currentZ);
					this.hasRadar = false;
				}
				this.player.addRadarMarker(this.newX, this.newY, this.newZ);
				this.currentX = this.newX;
				this.currentY = this.newY;
				this.currentZ = this.newZ;
				this.hasRadar = true;
				if (this.repeat)
				{
					this.schedule();
				}
			}
		}
		
		private void schedule()
		{
			this.refresh = CallBack.getInstance().getOut().scheduleGeneral(() -> Radar.this.applyRadar(), 10000);
		}
		
		public boolean isEnabled()
		{
			return this.enabled;
		}
		
		public boolean isRepeating()
		{
			return this.repeat;
		}
		
	}
	
	private class Teleport implements Runnable
	{
		final L2PcInstance owner;
		Loc loc;
		boolean randomOffset;
		int instanceId;
		
		Teleport(L2PcInstance owner, Loc loc, int delay, boolean randomOffset, int instanceId)
		{
			this.owner = owner;
			this.loc = loc;
			this.randomOffset = randomOffset;
			this.instanceId = instanceId;
			if (delay == 0)
			{
				CallBack.getInstance().getOut().executeTask(this);
			}
			else
			{
				CallBack.getInstance().getOut().scheduleGeneral(this, delay);
			}
		}
		
		@Override
		public void run()
		{
			L2PcInstance player = this.owner;
			if (player == null)
			{
				return;
			}
			L2Summon summon = player.getPet();
			player.abortCast();
			if (summon != null)
			{
				summon.unSummon(player);
			}
			if (player.isInDuel())
			{
				player.setDuelState(4);
			}
			player.doRevive();
			for (L2Effect e : player.getAllEffects())
			{
				if ((e == null) || (e.getSkill() == null) || !e.getSkill().isDebuff())
				{
					continue;
				}
				e.exit();
			}
			if (player.isSitting())
			{
				player.standUp();
			}
			player.teleToLocation(this.loc.getX(), this.loc.getY(), this.loc.getZ(), this.randomOffset);
			player.setTarget(null);
			if (this.instanceId != -1)
			{
				player.setInstanceId(this.instanceId);
			}
			player.setCurrentCp(player.getMaxCp());
			player.setCurrentHp(player.getMaxHp());
			player.setCurrentMp(player.getMaxMp());
			player.broadcastStatusUpdate();
			player.broadcastUserInfo();
		}
	}
	
}
