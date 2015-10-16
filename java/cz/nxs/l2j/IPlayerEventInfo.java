/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.PlayerEventInfo$AfkChecker
 *  cz.nxs.interf.PlayerEventInfo$Radar
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.ItemData
 *  cz.nxs.interf.delegate.PartyData
 *  cz.nxs.interf.delegate.ShortCutData
 *  cz.nxs.interf.delegate.SkillData
 */
package cz.nxs.l2j;

import cz.nxs.events.EventGame;
import cz.nxs.events.engine.base.EventPlayerData;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.mini.MiniEventGame;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.interf.delegate.PartyData;
import cz.nxs.interf.delegate.ShortCutData;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.ClassType;
import java.util.List;

public interface IPlayerEventInfo {
    public void initOrigInfo();

    public void restoreData();

    public void onEventStart(EventGame var1);

    public void clean();

    public void teleport(Loc var1, int var2, boolean var3, int var4);

    public void teleToLocation(Loc var1, boolean var2);

    public void teleToLocation(int var1, int var2, int var3, boolean var4);

    public void teleToLocation(int var1, int var2, int var3, int var4, boolean var5);

    public void setXYZInvisible(int var1, int var2, int var3);

    public void setFame(int var1);

    public int getFame();

    public void setInstanceId(int var1);

    public void sendPacket(String var1);

    public void screenMessage(String var1, String var2, boolean var3);

    public void creatureSay(String var1, String var2, int var3);

    public void sendMessage(String var1);

    public void sendEventScoreBar(String var1);

    public void broadcastUserInfo();

    public void broadcastTitleInfo();

    public void sendSkillList();

    public void transform(int var1);

    public boolean isTransformed();

    public void untransform(boolean var1);

    public ItemData addItem(int var1, int var2, boolean var3);

    public void addExpAndSp(long var1, int var3);

    public void doDie();

    public void doDie(CharacterData var1);

    public ItemData[] getItems();

    public void getSkillEffects(int var1, int var2);

    public void getPetSkillEffects(int var1, int var2);

    public void addSkill(SkillData var1, boolean var2);

    public void removeSkill(int var1);

    public void removeCubics();

    public void removeSummon();

    public boolean hasPet();

    public void removeBuffs();

    public void removeBuffsFromPet();

    public void removeBuff(int var1);

    public int getBuffsCount();

    public int getDancesCount();

    public int getMaxBuffCount();

    public int getMaxDanceCount();

    public int getPetBuffCount();

    public int getPetDanceCount();

    public void abortCasting();

    public void playSound(String var1);

    public void setVisible();

    public void rebuffPlayer();

    public void enableAllSkills();

    public void sendSetupGauge(int var1);

    public void root();

    public void unroot();

    public void paralizeEffect(boolean var1);

    public void setIsParalyzed(boolean var1);

    public void setIsInvul(boolean var1);

    public void setCanInviteToParty(boolean var1);

    public boolean canInviteToParty();

    public void showEventEscapeEffect();

    public void broadcastSkillUse(CharacterData var1, CharacterData var2, int var3, int var4);

    public void broadcastSkillLaunched(CharacterData var1, CharacterData var2, int var3, int var4);

    public void enterObserverMode(int var1, int var2, int var3);

    public void removeObserveMode();

    public void sendStaticPacket();

    public void sendHtmlText(String var1);

    public void sendHtmlPage(String var1);

    public void startAbnormalEffect(int var1);

    public void stopAbnormalEffect(int var1);

    public void startAntifeedProtection(boolean var1);

    public void stopAntifeedProtection(boolean var1);

    public boolean hasAntifeedProtection();

    public void removeOriginalShortcuts();

    public void restoreOriginalShortcuts();

    public void removeCustomShortcuts();

    public void registerShortcut(ShortCutData var1, boolean var2);

    public void removeShortCut(ShortCutData var1, boolean var2);

    public ShortCutData createItemShortcut(int var1, int var2, ItemData var3);

    public ShortCutData createSkillShortcut(int var1, int var2, SkillData var3);

    public ShortCutData createActionShortcut(int var1, int var2, int var3);

    public boolean isOnline();

    public boolean isOnline(boolean var1);

    public boolean isDead();

    public boolean isVisible();

    public void doRevive();

    public CharacterData getTarget();

    public String getPlayersName();

    public int getLevel();

    public int getPvpKills();

    public int getPkKills();

    public int getMaxHp();

    public int getMaxCp();

    public int getMaxMp();

    public void setCurrentHp(int var1);

    public void setCurrentCp(int var1);

    public void setCurrentMp(int var1);

    public double getCurrentHp();

    public double getCurrentCp();

    public double getCurrentMp();

    public void healPet();

    public void setTitle(String var1, boolean var2);

    public boolean isMageClass();

    public int getClassIndex();

    public int getActiveClass();

    public String getClassName();

    public PartyData getParty();

    public boolean isFighter();

    public boolean isPriest();

    public boolean isMystic();

    public ClassType getClassType();

    public int getX();

    public int getY();

    public int getZ();

    public int getHeading();

    public int getInstanceId();

    public int getClanId();

    public boolean isGM();

    public String getIp();

    public boolean isInJail();

    public boolean isInSiege();

    public boolean isInDuel();

    public boolean isInOlympiadMode();

    public int getKarma();

    public boolean isCursedWeaponEquipped();

    public boolean isImmobilized();

    public boolean isParalyzed();

    public boolean isAfraid();

    public boolean isOlympiadRegistered();

    public void sitDown();

    public void standUp();

    public List<SkillData> getSkills();

    public List<Integer> getSkillIds();

    public double getPlanDistanceSq(int var1, int var2);

    public double getDistanceSq(int var1, int var2, int var3);

    public boolean isRegistered();

    public boolean isInEvent();

    public EventPlayerData getEventData();

    public void setNameColor(int var1);

    public void setCanBuff(boolean var1);

    public boolean canBuff();

    public int getPlayersId();

    public int getKills();

    public int getDeaths();

    public int getScore();

    public int getStatus();

    public void raiseKills(int var1);

    public void raiseDeaths(int var1);

    public void raiseScore(int var1);

    public void setScore(int var1);

    public void setStatus(int var1);

    public void setKills(int var1);

    public void setDeaths(int var1);

    public boolean isInFFAEvent();

    public void setIsRegisteredToMiniEvent(boolean var1, MiniEventManager var2);

    public MiniEventManager getRegisteredMiniEvent();

    public void setIsRegisteredToMainEvent(boolean var1, EventType var2);

    public EventType getRegisteredMainEvent();

    public MiniEventGame getActiveGame();

    public AbstractMainEvent getActiveEvent();

    public EventGame getEvent();

    public void setActiveGame(MiniEventGame var1);

    public void setEventTeam(EventTeam var1);

    public EventTeam getEventTeam();

    public int getTeamId();

    public Loc getOrigLoc();

    public void setIsSpectator(boolean var1);

    public boolean isSpectator();

    public boolean isEventRooted();

    public boolean isTitleUpdated();

    public void setTitleUpdated(boolean var1);

    public ItemData getPaperdollItem(int var1);

    public void equipItem(ItemData var1);

    public ItemData[] unEquipItemInBodySlotAndRecord(int var1);

    public void destroyItemByItemId(int var1, int var2);

    public void inventoryUpdate(ItemData[] var1);

    public void addRadarMarker(int var1, int var2, int var3);

    public void removeRadarMarker(int var1, int var2, int var3);

    public void removeRadarAllMarkers();

    public void createRadar();

    public PlayerEventInfo.Radar getRadar();

    public void disableAfkCheck(boolean var1);

    public int getTotalTimeAfk();

    public boolean isAfk();

    public PlayerEventInfo.AfkChecker getAfkChecker();

    public CharacterData getCharacterData();
}

