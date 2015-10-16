/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.L2Object
 *  com.l2jserver.gameserver.model.actor.L2Character
 *  com.l2jserver.gameserver.model.actor.L2Npc
 *  com.l2jserver.gameserver.model.actor.L2Playable
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  com.l2jserver.gameserver.model.items.L2Item
 *  com.l2jserver.gameserver.model.items.instance.L2ItemInstance
 *  com.l2jserver.gameserver.model.skills.L2Skill
 *  cz.nxs.events.EventGame
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.events.NexusLoader$NexusBranch
 *  cz.nxs.events.engine.EventBuffer
 *  cz.nxs.events.engine.EventConfig
 *  cz.nxs.events.engine.EventManagement
 *  cz.nxs.events.engine.EventManager
 *  cz.nxs.events.engine.html.EventHtmlManager
 *  cz.nxs.events.engine.main.events.AbstractMainEvent
 *  cz.nxs.events.engine.mini.EventMode
 *  cz.nxs.events.engine.mini.EventMode$FeatureType
 *  cz.nxs.events.engine.mini.MiniEventGame
 *  cz.nxs.events.engine.mini.MiniEventManager
 *  cz.nxs.events.engine.mini.features.AbstractFeature
 *  cz.nxs.events.engine.mini.features.EnchantFeature
 */
package cz.nxs.interf;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.L2Skill;
import cz.nxs.events.EventGame;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventBuffer;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManagement;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.html.EventHtmlManager;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.MiniEventGame;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.events.engine.mini.features.EnchantFeature;
import cz.nxs.interf.NexusOut;
import cz.nxs.interf.PlayerBase;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.Values;
import cz.nxs.interf.callback.HtmlManager;
import cz.nxs.interf.callback.api.DescriptionLoader;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.SkillData;
import java.util.List;

public class NexusEvents {
    public static final String _desc = "L2Jserver HighFive";
    public static final NexusLoader.NexusBranch _branch = NexusLoader.NexusBranch.Hi5;
    public static final double _interfaceVersion = 2.1;
    public static final boolean _allowInstances = true;
    public static final String _libsFolder = "../libs/";
    public static final String _serialPath = "config/nexus_serial.txt";
    public static final boolean _limitedHtml = false;

    public static void start() {
        NexusOut.getInstance().load();
        PlayerBase.getInstance().load();
        Values.getInstance().load();
        NexusLoader.init((NexusLoader.NexusBranch)_branch, (double)2.1, (String)"L2Jserver HighFive", (boolean)true, (String)"../libs/", (String)"config/nexus_serial.txt", (boolean)false);
    }

    public static void loadHtmlManager() {
        HtmlManager.load();
        DescriptionLoader.load();
    }

    public static void serverShutDown() {
    }

    public static void onLogin(L2PcInstance player) {
        EventBuffer.getInstance().loadPlayer(player.getEventInfo());
        EventManager.getInstance().onPlayerLogin(player.getEventInfo());
    }

    public static PlayerEventInfo getPlayer(L2PcInstance player) {
        return NexusLoader.loaded() ? PlayerBase.getInstance().getPlayer(player) : null;
    }

    public static boolean isRegistered(L2PcInstance player) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        return pi != null && pi.isRegistered();
    }

    public static boolean isInEvent(L2PcInstance player) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        return pi != null && pi.isInEvent();
    }

    public static boolean isInEvent(L2Character ch) {
        if (ch instanceof L2Playable) {
            return NexusEvents.isInEvent(ch.getActingPlayer());
        }
        return EventManager.getInstance().isInEvent(new CharacterData(ch));
    }

    public static boolean allowDie(L2Character ch, L2Character attacker) {
        if (NexusEvents.isInEvent(ch) && NexusEvents.isInEvent(attacker)) {
            return EventManager.getInstance().allowDie(new CharacterData(ch), new CharacterData(attacker));
        }
        return true;
    }

    public static boolean isInMiniEvent(L2PcInstance player) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        return pi != null && pi.getActiveGame() != null;
    }

    public static boolean isInMainEvent(L2PcInstance player) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        return pi != null && pi.getActiveEvent() != null;
    }

    public static boolean canShowToVillageWindow(L2PcInstance player) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            return pi.canShowToVillageWindow();
        }
        return true;
    }

    public static boolean canAttack(L2PcInstance player, L2Character target) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            return pi.canAttack(target);
        }
        return true;
    }

    public static boolean onAttack(L2Character cha, L2Character target) {
        return EventManager.getInstance().onAttack(new CharacterData(cha), new CharacterData(target));
    }

    public static boolean canSupport(L2PcInstance player, L2Character target) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            return pi.canSupport(target);
        }
        return true;
    }

    public static boolean canTarget(L2PcInstance player, L2Object target) {
        return true;
    }

    public static void onHit(L2PcInstance player, L2Character target, int damage, boolean isDOT) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            pi.onDamageGive(target, damage, isDOT);
        }
    }

    public static void onDamageGive(L2Character cha, L2Character target, int damage, boolean isDOT) {
        EventManager.getInstance().onDamageGive(new CharacterData(cha), new CharacterData(target), damage, isDOT);
    }

    public static void onKill(L2PcInstance player, L2Character target) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            pi.notifyKill(target);
        }
    }

    public static void onDie(L2PcInstance player, L2Character killer) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            pi.notifyDie(killer);
        }
    }

    public static boolean onNpcAction(L2PcInstance player, L2Npc target) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            return pi.notifyNpcAction(target);
        }
        return false;
    }

    public static boolean canUseItem(L2PcInstance player, L2ItemInstance item) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            return pi.canUseItem(item);
        }
        return true;
    }

    public static void onUseItem(L2PcInstance player, L2ItemInstance item) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            pi.notifyItemUse(item);
        }
    }

    public static boolean onSay(L2PcInstance player, String text, int channel) {
        PlayerEventInfo pi;
        try {
            if (text.startsWith(".")) {
                if (EventManager.getInstance().tryVoicedCommand(player.getEventInfo(), text)) {
                    return false;
                }
                return true;
            }
        }
        catch (Exception e) {
            // empty catch block
        }
        if ((pi = NexusEvents.getPlayer(player)) != null) {
            return pi.notifySay(text, channel);
        }
        return true;
    }

    public static boolean canUseSkill(L2PcInstance player, L2Skill skill) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            return pi.canUseSkill(skill);
        }
        return true;
    }

    public static void onUseSkill(L2PcInstance player, L2Skill skill) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            pi.onUseSkill(skill);
        }
    }

    public static boolean canDestroyItem(L2PcInstance player, L2ItemInstance item) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            return pi.canDestroyItem(item);
        }
        return true;
    }

    public static boolean canInviteToParty(L2PcInstance player, L2PcInstance target) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        PlayerEventInfo targetPi = NexusEvents.getPlayer(target);
        if (pi != null) {
            if (targetPi == null) {
                return false;
            }
            return pi.canInviteToParty(pi, targetPi);
        }
        return true;
    }

    public static boolean canTransform(L2PcInstance player) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            return pi.canTransform(pi);
        }
        return true;
    }

    public static int allowTransformationSkill(L2PcInstance player, L2Skill s) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            return pi.allowTransformationSkill(s);
        }
        return 0;
    }

    public static boolean canBeDisarmed(L2PcInstance player) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            return pi.canBeDisarmed(pi);
        }
        return true;
    }

    public static boolean onBypass(L2PcInstance player, String command) {
        if (command.startsWith("nxs_")) {
            return EventManager.getInstance().onBypass(player.getEventInfo(), command.substring(4));
        }
        return false;
    }

    public static void onAdminBypass(PlayerEventInfo player, String command) {
        EventManagement.getInstance().onBypass(player, command);
    }

    public static boolean canLogout(L2PcInstance player) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        return pi == null || !pi.isInEvent();
    }

    public static void onLogout(L2PcInstance player) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null) {
            pi.notifyDisconnect();
        }
    }

    public static boolean isObserving(L2PcInstance player) {
        return player.getEventInfo().isSpectator();
    }

    public static void endObserving(L2PcInstance player) {
        EventManager.getInstance().removePlayerFromObserverMode(player.getEventInfo());
    }

    public static boolean canSaveShortcuts(L2PcInstance activeChar) {
        PlayerEventInfo pi = NexusEvents.getPlayer(activeChar);
        if (pi != null) {
            pi.canSaveShortcuts();
        }
        return true;
    }

    public static int getItemAutoEnchantValue(L2PcInstance player, L2ItemInstance item) {
        if (NexusEvents.isInEvent(player)) {
            PlayerEventInfo pi = PlayerBase.getInstance().getPlayer(player);
            MiniEventManager event = pi.getRegisteredMiniEvent();
            if (event == null) {
                return 0;
            }
            for (AbstractFeature f : event.getMode().getFeatures()) {
                if (f.getType() != EventMode.FeatureType.Enchant) continue;
                switch (item.getItem().getType2()) {
                    case 0: {
                        return ((EnchantFeature)f).getAutoEnchantWeapon();
                    }
                    case 1: {
                        return ((EnchantFeature)f).getAutoEnchantArmor();
                    }
                    case 2: {
                        return ((EnchantFeature)f).getAutoEnchantJewel();
                    }
                }
            }
            return 0;
        }
        return 0;
    }

    public static boolean removeCubics() {
        return EventConfig.getInstance().getGlobalConfigBoolean("removeCubicsOnDie");
    }

    public static boolean gainPvpPointsOnEvents() {
        return EventConfig.getInstance().getGlobalConfigBoolean("pvpPointsOnKill");
    }

    public static boolean cbBypass(L2PcInstance player, String command) {
        PlayerEventInfo pi = NexusEvents.getPlayer(player);
        if (pi != null && command != null) {
            return EventManager.getInstance().getHtmlManager().onCbBypass(pi, command);
        }
        return false;
    }

    public static String consoleCommand(String cmd) {
        if (cmd.startsWith("reload_globalconfig")) {
            EventConfig.getInstance().loadGlobalConfigs();
            return "Global configs reloaded.";
        }
        return "This command doesn't exist.";
    }

    public static boolean adminCommandRequiresConfirm(String cmd) {
        if (cmd.split(" ").length > 1) {
            String command = cmd.split(" ")[1];
            return EventManagement.getInstance().commandRequiresConfirm(command);
        }
        return false;
    }

    public static boolean isSkillOffensive(L2PcInstance activeChar, L2Skill skill) {
        PlayerEventInfo pi = NexusEvents.getPlayer(activeChar);
        if (pi != null && pi.isInEvent()) {
            EventGame game = pi.getEvent();
            int val = game.isSkillOffensive(new SkillData(skill));
            if (val == 1) {
                return true;
            }
            if (val == 0) {
                return false;
            }
        }
        return skill.isOffensive();
    }

    public static boolean isSkillNeutral(L2PcInstance activeChar, L2Skill skill) {
        PlayerEventInfo pi = NexusEvents.getPlayer(activeChar);
        if (pi != null && pi.isInEvent()) {
            EventGame game = pi.getEvent();
            return game.isSkillNeutral(new SkillData(skill));
        }
        return false;
    }
}

