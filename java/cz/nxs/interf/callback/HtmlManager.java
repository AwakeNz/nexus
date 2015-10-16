/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.L2Object
 *  com.l2jserver.gameserver.model.actor.L2Character
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  cz.nxs.events.engine.EventBuffer
 *  cz.nxs.events.engine.EventConfig
 *  cz.nxs.events.engine.html.EventHtmlManager
 *  cz.nxs.l2j.CallBack
 *  javolution.text.TextBuilder
 *  javolution.util.FastMap
 */
package cz.nxs.interf.callback;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import cz.nxs.events.engine.EventBuffer;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.html.EventHtmlManager;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.SkillData;
import cz.nxs.l2j.CallBack;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javolution.text.TextBuilder;
import javolution.util.FastMap;

public class HtmlManager
extends EventHtmlManager {
    private Map<Integer, Long> _healTimes = new FastMap();
    private Map<Integer, Long> _healTimesPets = new FastMap();
    public static int HEAL_DELAY_MS = EventConfig.getInstance().getGlobalConfigInt("bufferHealDelay") * 1000;
    public static int MAX_BUFFS_COUNT = EventConfig.getInstance().getGlobalConfigInt("maxBuffsCount");
    public static int MAX_DANCES_COUNT = EventConfig.getInstance().getGlobalConfigInt("maxDancesCount");

    public static void load() {
        CallBack.getInstance().setHtmlManager((EventHtmlManager)new HtmlManager());
    }

    public boolean showNpcHtml(PlayerEventInfo player, NpcData npc) {
        if (npc.getNpcId() == EventConfig.getInstance().getGlobalConfigInt("assignedNpcId")) {
            this.showCustomBufferMenu(player);
            return true;
        }
        return super.showNpcHtml(player, npc);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean onBypass(PlayerEventInfo player, String bypass) {
        if (!bypass.startsWith("npcbuffer")) return super.onBypass(player, bypass);
        String action = bypass.substring(10);
        if (player.getTarget() == null || !player.getTarget().isNpc() || player.getTarget().getNpc().getNpcId() != EventConfig.getInstance().getGlobalConfigInt("assignedNpcId") || !player.getOwner().isInsideRadius((L2Object)player.getTarget().getOwner(), 150, false, false)) return super.onBypass(player, bypass);
        if (action.startsWith("menu")) {
            StringTokenizer st = new StringTokenizer(action);
            st.nextToken();
            this.showCustomBufferMenu(player);
            return true;
        }
        if (action.startsWith("bufferinfo")) {
            this.showBufferIntoPage(player);
            return true;
        }
        if (action.startsWith("reload")) {
            if (!player.isGM()) return super.onBypass(player, bypass);
            HEAL_DELAY_MS = EventConfig.getInstance().getGlobalConfigInt("bufferHealDelay") * 1000;
            MAX_BUFFS_COUNT = EventConfig.getInstance().getGlobalConfigInt("maxBuffsCount");
            MAX_DANCES_COUNT = EventConfig.getInstance().getGlobalConfigInt("maxDancesCount");
            player.sendMessage("Reloaded.");
            return true;
        }
        if (action.startsWith("singlemenu")) {
            StringTokenizer st = new StringTokenizer(action);
            st.nextToken();
            String target = st.nextToken();
            String category = st.nextToken();
            this.showSingleBuffsMenu(player, target, category);
            return true;
        }
        if (action.startsWith("singlebuff")) {
            StringTokenizer st = new StringTokenizer(action);
            st.nextToken();
            String target = st.nextToken();
            String category = st.nextToken();
            try {
                int buffId = Integer.parseInt(st.nextToken());
                this.giveBuff(player, target, buffId);
            }
            catch (Exception e) {
                player.sendMessage("Wrong buff.");
                this.showSingleBuffsMenu(player, target, category);
                return true;
            }
            this.showSingleBuffsMenu(player, target, category);
            return true;
        }
        if (action.startsWith("single_selectcategory")) {
            StringTokenizer st = new StringTokenizer(action);
            st.nextToken();
            String target = st.nextToken();
            String category = st.nextToken();
            this.showSingleBuffsMenu(player, target, category);
            return true;
        }
        if (action.startsWith("edit_schemes")) {
            StringTokenizer st = new StringTokenizer(action);
            st.nextToken();
            String returnPage = st.nextToken();
            this.showSelectSchemeMenu(player, returnPage, 0, null);
            return true;
        }
        if (action.startsWith("buffmenu")) {
            StringTokenizer st = new StringTokenizer(action);
            st.nextToken();
            String target = st.nextToken();
            this.showBuffMeWindow(player, target);
            return true;
        }
        if (action.startsWith("cancelbuffs")) {
            StringTokenizer st = new StringTokenizer(action);
            st.nextToken();
            String target = st.nextToken();
            String returnPage = "buffmenu";
            if (st.hasMoreTokens()) {
                returnPage = st.nextToken();
            }
            if (target.equals("player")) {
                player.removeBuffs();
            } else if (target.equals("pet")) {
                player.removeBuffsFromPet();
            }
            if (returnPage.equals("buffmenu")) {
                this.showBuffMeWindow(player, target);
                return true;
            } else {
                if (!returnPage.equals("menu")) return true;
                this.showCustomBufferMenu(player);
            }
            return true;
        }
        if (action.startsWith("heal")) {
            StringTokenizer st = new StringTokenizer(action);
            st.nextToken();
            String target = st.nextToken();
            String returnPage = "buffmenu";
            if (st.hasMoreTokens()) {
                returnPage = st.nextToken();
            }
            this.healPlayer(player, target);
            if (returnPage.equals("buffmenu")) {
                this.showBuffMeWindow(player, target);
                return true;
            } else {
                if (!returnPage.equals("menu")) return true;
                this.showCustomBufferMenu(player);
            }
            return true;
        }
        if (!action.startsWith("buff")) return super.onBypass(player, bypass);
        StringTokenizer st = new StringTokenizer(action);
        st.nextToken();
        String target = st.nextToken();
        if (!st.hasMoreTokens()) {
            player.sendMessage("You need to specify a scheme.");
            return true;
        }
        String scheme = st.nextToken();
        this.buffPlayer(player, scheme, target);
        return true;
    }

    private void showSelectSchemeMenu(PlayerEventInfo player, String returnPage, int page, String selectedCategory) {
        String scheme = EventBuffer.getInstance().getPlayersCurrentScheme(player.getPlayersId());
        TextBuilder tb = new TextBuilder();
        tb.append("<html><title>Event Buffer</title><body>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
        tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
        tb.append("<table width=280 border=0 bgcolor=484848><tr>");
        tb.append("<td width=220 align=center> <font color=8f8f8f>Scheme management menu</font></td>");
        tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_" + returnPage + "_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
        tb.append("</tr></table>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
        tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=5>");
        tb.append("<br>");
        tb.append("<br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
        if (!EventBuffer.getInstance().getSchemes(player).isEmpty()) {
            tb.append("<table width=283 bgcolor=2E2E2E>");
            for (Map.Entry schemes : EventBuffer.getInstance().getSchemes(player)) {
                tb.append("<tr>");
                tb.append("<td width=150 align=left><font color=ac9887>" + (scheme != null && scheme.equals(schemes.getKey()) ? "*" : "") + " " + (String)schemes.getKey() + " </font><font color=7f7f7f>(" + ((List)schemes.getValue()).size() + " buffs)</font></td>");
                tb.append("<td width=65 align=center><font color=B04F51><a action=\"bypass -h nxs_buffer_delete_scheme " + (String)schemes.getKey() + " " + returnPage + "\">Delete</a></font></td>");
                tb.append("<td width=75 align=right><font color=9f9f9f><a action=\"bypass -h nxs_buffer_select_scheme " + (String)schemes.getKey() + " " + returnPage + "\">Edit scheme</a></font></td>");
                tb.append("</tr>");
            }
            tb.append("</table>");
        } else {
            tb.append("<table width=283 bgcolor=2E2E2E>");
            tb.append("<tr><td width=280 align=center><font color=ac9887>You don't have any scheme.</font></td></tr>");
            tb.append("</table>");
        }
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
        tb.append("<table width=283 bgcolor=2E2E2E");
        tb.append("<tr><td width=115><edit var=\"name\" width=115 height=15></td><td width=150 align=right><button value=\"Create scheme\" action=\"bypass -h nxs_buffer_create_scheme " + returnPage + " $name\" width=105 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td></tr>");
        tb.append("</table>");
        tb.append("<br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
        tb.append("</body></html>");
        String html = tb.toString();
        player.sendHtmlText(html);
        player.sendStaticPacket();
    }

    protected void showBuffMeWindow(PlayerEventInfo player, String target) {
        if (target.equals("pet") && !player.hasPet()) {
            player.sendMessage("You have no pet/summon.");
            return;
        }
        TextBuilder tb = new TextBuilder();
        tb.append("<html><title>Event Buffer</title><body>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
        tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
        tb.append("<table width=280 border=0 bgcolor=484848><tr>");
        tb.append("<td width=220 align=center> <font color=8f8f8f>Buff " + target + "</font></td>");
        tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_npcbuffer_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
        tb.append("</tr></table>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
        tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=5>");
        int playerBuffs = player.getBuffsCount();
        int playerDances = player.getDancesCount();
        int petBuffs = player.getPetBuffCount();
        int petDances = player.getPetDanceCount();
        int maxBuffs = this.getMaxBuffs(player);
        int maxDances = this.getMaxDances(player);
        tb.append("<br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
        if (!EventBuffer.getInstance().getSchemes(player).isEmpty()) {
            tb.append("<table width=283 bgcolor=2E2E2E>");
            for (Map.Entry schemes : EventBuffer.getInstance().getSchemes(player)) {
                tb.append("<tr>");
                tb.append("<td width=150 align=left><font color=ac9887>" + (String)schemes.getKey() + " </font><font color=7f7f7f>(" + ((List)schemes.getValue()).size() + " buffs)</font></td>");
                tb.append("<td width=65 align=center><font color=B04F51><a action=\"bypass -h nxs_npcbuffer_buff " + target + " " + (String)schemes.getKey() + " \">Buff " + target + "</a></font></td>");
                tb.append("</tr>");
            }
            tb.append("</table>");
        } else {
            tb.append("<table width=283 bgcolor=2E2E2E>");
            tb.append("<tr><td width=280 align=center><font color=ac9887>You don't have any scheme.</font></td></tr>");
            tb.append("<tr><td width=280 align=center><font color=BF8380>You must make one first.</font></td></tr>");
            tb.append("</table>");
        }
        tb.append("<br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
        tb.append("<center><table width=280>");
        if (target.equals("player")) {
            if (maxDances > 0) {
                tb.append("<tr><td width=120 align=left><font color=9f9f9f>You have:</font></td><td width=100 align=left><font color=ac9887>" + playerBuffs + " buffs / " + playerDances + " dances</font></td></tr>");
            } else {
                tb.append("<tr><td width=120 align=center><font color=9f9f9f>You currently have:</font></td><td width=140 align=center><font color=ac9887>" + (playerBuffs+=playerDances) + " buffs</font></td></tr>");
            }
        }
        if (target.equals("pet")) {
            if (maxDances > 0) {
                tb.append("<tr><td width=120 align=left><font color=9f9f9f>Your pet has:</font></td><td width=140 align=left><font color=ac9887>" + petBuffs + " buffs / " + petDances + " dances</font></td></tr>");
            } else {
                tb.append("<tr><td width=120 align=center><font color=9f9f9f>Your pet currently has:</font></td><td width=140 align=center><font color=ac9887>" + (petBuffs+=petDances) + " buffs</font></td></tr>");
            }
        }
        if (maxDances > 0) {
            tb.append("<tr><td width=120 align=left><font color=9f9f9f>Max buffs #:</font></td><td width=140 align=left><font color=ac9887>" + maxBuffs + " buffs</font></td></tr>");
            tb.append("<tr><td width=120 align=left><font color=9f9f9f>Max dances #:</font></td><td width=140 align=left><font color=ac9887>" + maxDances + " dances</font></td></tr>");
        } else {
            tb.append("<tr><td width=140 align=center><font color=9f9f9f>Max buffs #:</font></td><td width=140 align=center><font color=ac9887>" + maxBuffs + " buffs</font></td></tr>");
        }
        tb.append("</table></center><br>");
        tb.append("<table width=280><tr>");
        tb.append("<td width=140 align=left><center><button value=\"Heal " + target + "\" action=\"bypass -h nxs_npcbuffer_heal " + target + "\" width=130 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td>");
        tb.append("<td width=140 align=right><center><button value=\"Cancel buffs\" action=\"bypass -h nxs_npcbuffer_cancelbuffs " + target + "\" width=130 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></center></td>");
        tb.append("</tr></table>");
        tb.append("</body></html>");
        String html = tb.toString();
        player.sendHtmlText(html);
        player.sendStaticPacket();
    }

    protected int getMaxBuffs(PlayerEventInfo player) {
        if (MAX_BUFFS_COUNT == -1) {
            return player.getMaxBuffCount();
        }
        return MAX_BUFFS_COUNT;
    }

    protected int getMaxDances(PlayerEventInfo player) {
        if (MAX_DANCES_COUNT == -1) {
            return player.getMaxDanceCount();
        }
        return MAX_DANCES_COUNT;
    }

    protected void showSingleBuffsMenu(PlayerEventInfo player, String target, String selectedCategory) {
        TextBuilder tb = new TextBuilder();
        tb.append("<html><title>Nexus Buffer</title><body>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
        tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
        String status = " - ";
        if (target.equalsIgnoreCase("player")) {
            status = status + "<font color=9f9f9f>" + player.getBuffsCount() + "/" + (this.getMaxBuffs(player) + Math.max(0, this.getMaxDances(player))) + " buffs</font>";
        } else if (target.equalsIgnoreCase("pet")) {
            status = status + "<font color=9f9f9f>" + player.getPetBuffCount() + "/" + (this.getMaxBuffs(player) + Math.max(0, this.getMaxDances(player))) + " buffs</font>";
        }
        tb.append("<table width=280 border=0 bgcolor=484848><tr>");
        tb.append("<td width=220 align=center> <font color=ac9887>Buff " + target + "" + status + "</font></td>");
        tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_npcbuffer_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
        tb.append("</tr></table>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
        tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=5>");
        tb.append("<table width=281 bgcolor=2E2E2E>");
        tb.append("<tr>");
        String form = "";
        form = target.equalsIgnoreCase("player") ? (player.hasPet() ? "<combobox width=75 height=17 var=target list=\"Player;Pet\">" : "<combobox width=75 height=17 var=target list=\"Player\">") : (player.hasPet() ? "<combobox width=75 height=17 var=target list=\"Pet;Player\">" : "<combobox width=75 height=17 var=target list=\"Player\">");
        tb.append("<td width=170><font color=696969>Buff target:</font></td><td align=left width=80>" + form + "</td><td width=45 align=right><button value=\"Set\" action=\"bypass -h nxs_npcbuffer_singlemenu $target " + selectedCategory + "\" width=45 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_CT1.button_df\"></td>");
        tb.append("</tr>");
        tb.append("</table>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=4>");
        tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=4>");
        tb.append("<table width=283 bgcolor=2E2E2E");
        int i = 0;
        for (String category : EventBuffer.getInstance().getAviableBuffs().keySet()) {
            if (i == 0) {
                tb.append("<tr>");
            }
            if (selectedCategory != null && selectedCategory.equals(category)) {
                tb.append("<td align=center width=93><button value=\"" + category + "\" action=\"bypass -h nxs_npcbuffer_singlemenu " + target + " " + category + "\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_CT1.Button_DF_Down\"></td>");
            } else {
                tb.append("<td align=center width=93><button value=\"" + category + "\" action=\"bypass -h nxs_npcbuffer_singlemenu " + target + " " + category + "\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
            }
            if (++i != 3) continue;
            tb.append("</tr>");
            i = 0;
        }
        tb.append("</table>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
        tb.append("<img src=\"L2UI.SquareGray\" width=280 height=1>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=6>");
        tb.append("<table width=281 bgcolor=2E2E2E");
        int count = 0;
        i = 0;
        for (Map.Entry e : EventBuffer.getInstance().getAviableBuffs().entrySet()) {
            String category2 = (String)e.getKey();
            if (!category2.equals(selectedCategory)) continue;
            for (Map.Entry buff : ((Map)e.getValue()).entrySet()) {
                ++count;
                int id = (Integer)buff.getKey();
                int level = (Integer)buff.getValue();
                String name = new SkillData(id, level).getName();
                name = this.trimName(name);
                if (EventBuffer.getInstance().containsSkill(id, player)) continue;
                if (i == 0) {
                    tb.append("<tr>");
                }
                String icon = this.formatSkillIcon("0000", id);
                tb.append("<td width=33 align=left><img src=\"icon.skill" + icon + "\" width=32 height=32></td>");
                tb.append("<td width=95 align=left><button action=\"bypass -h nxs_npcbuffer_singlebuff " + target + " " + selectedCategory + " " + id + "\" value=\"" + name + "\" width=95 height=32 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
                if (++i != 2) continue;
                tb.append("</tr>");
                i = 0;
            }
        }
        tb.append("</table>");
        if (count == 0) {
            tb.append("<br><center><font color=AB7878>Please select a category first.</font></center>");
        }
        tb.append("<br>");
        tb.append("<br><br><img src=\"L2UI_CH3.onscrmsg_pattern01_1\" width=300 height=32 align=left><br>");
        tb.append("</body></html>");
        String html = tb.toString();
        player.sendHtmlText(html);
        player.sendStaticPacket();
    }

    protected void showCustomBufferMenu(PlayerEventInfo player) {
        TextBuilder tb = new TextBuilder();
        tb.append("<html><title>NPC Buffer</title><body>");
        tb.append("<table width=280 border=0 bgcolor=383838><tr>");
        tb.append("<td width=280 align=center> <font color=ac9887><a action=\"bypass -h nxs_npcbuffer_bufferinfo\">Nexus Engine Buffer</a></font></td>");
        tb.append("</tr></table>");
        tb.append("<br1><center>");
        tb.append("<br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");
        tb.append("<button value=\"Buff - Player\" action=\"bypass -h nxs_npcbuffer_buffmenu player\" width=130 height=23 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1>");
        tb.append("<button value=\"Buff - Summon\" action=\"bypass -h nxs_npcbuffer_buffmenu pet\" width=130 height=23 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br><br>");
        tb.append("<button value=\"Edit Schemes\" action=\"bypass -h nxs_npcbuffer_edit_schemes npcbuffer\" width=130 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br><br>");
        tb.append("<button value=\"Single buffs\" action=\"bypass -h nxs_npcbuffer_singlemenu player null\" width=130 height=23 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br>");
        tb.append("<button value=\"Heal me\" action=\"bypass -h nxs_npcbuffer_heal player menu\" width=130 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1>");
        tb.append("<button value=\"Cancel buffs\" action=\"bypass -h nxs_npcbuffer_cancelbuffs player menu\" width=130 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1>");
        if (player.hasPet()) {
            tb.append("<button value=\"Cancel summon buffs\" action=\"bypass -h nxs_npcbuffer_cancelbuffs pet menu\" width=130 height=21 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"><br1>");
        }
        tb.append("<br><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>");
        tb.append("</center>");
        tb.append("</body></html>");
        String html = tb.toString();
        player.sendHtmlText(html);
        player.sendStaticPacket();
    }

    protected void showBufferIntoPage(PlayerEventInfo player) {
        TextBuilder tb = new TextBuilder();
        tb.append("<html><title>Mini Events</title><body>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
        tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=3>");
        tb.append("<table width=280 border=0 bgcolor=484848><tr>");
        tb.append("<td width=90 align=left> <font color=696969> Powered by:</font></td>");
        tb.append("<td width=130 align=left><font color=63AA1C><a action=\"bypass -h nxs_npcbuffer_bufferinfo\">Nexus Event Engine</a></font></td>");
        tb.append("<td width=65 align=right><button value=\"Back\" action=\"bypass -h nxs_npcbuffer_menu\" width=65 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
        tb.append("</tr></table>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=280 height=3>");
        tb.append("<img src=\"L2UI.SquareGray\" width=280 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=270 height=1>");
        tb.append("<br><br><br><br><br><br><br><br><br><br>");
        tb.append("<center>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=1>");
        tb.append("<img src=\"L2UI.SquareGray\" width=278 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=3>");
        tb.append("<table width=100% bgcolor=3f3f3f>");
        tb.append("<tr><td width=100% align=center><font color=9f9f9f>This server is using <font color=9FBF80>Nexus Event engine</font></font></td></tr>");
        tb.append("<tr><td width=100% align=center><font color=9f9f9f>of version <font color=797979>2.21</font>, developed by <font color=BEA481>hNoke</font>.</font><br></td></tr>");
        tb.append("<tr><td width=100% align=center><font color=9f9f9f>For more informations visit <font color=BEA481>www.nexus-engine.net</font></font></td></tr>");
        tb.append("</table>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=3>");
        tb.append("<img src=\"L2UI.SquareGray\" width=278 height=2>");
        tb.append("<img src=\"L2UI.SquareBlank\" width=278 height=1>");
        tb.append("<br><br><br><br><br><br><br><br><br><br><br><br>");
        tb.append("<center><font color=5F5F5F>If you find any problems, <br1>please contact me on my website.</font></center>");
        tb.append("</center>");
        tb.append("</body></html>");
        String html = tb.toString();
        player.sendHtmlText(html);
        player.sendStaticPacket();
    }

    protected void buffPlayer(PlayerEventInfo player, String scheme, String target) {
        if (target.equals("player")) {
            try {
                Iterator i$ = EventBuffer.getInstance().getBuffs(player, scheme).iterator();
                while (i$.hasNext()) {
                    int buffId = (Integer)i$.next();
                    player.getSkillEffects(buffId, EventBuffer.getInstance().getLevelFor(buffId));
                }
            }
            catch (Exception e) {
                player.sendMessage("wrong scheme");
            }
        } else if (target.equals("pet")) {
            try {
                Iterator i$ = EventBuffer.getInstance().getBuffs(player, scheme).iterator();
                while (i$.hasNext()) {
                    int buffId = (Integer)i$.next();
                    player.getPetSkillEffects(buffId, EventBuffer.getInstance().getLevelFor(buffId));
                }
            }
            catch (Exception e) {
                player.sendMessage("wrong scheme");
            }
        }
    }

    protected void giveBuff(PlayerEventInfo player, String target, int id) {
        if (target.equalsIgnoreCase("player")) {
            try {
                player.getSkillEffects(id, EventBuffer.getInstance().getLevelFor(id));
            }
            catch (Exception e) {
                player.sendMessage("wrong scheme");
            }
        } else if (target.equalsIgnoreCase("pet")) {
            try {
                player.getPetSkillEffects(id, EventBuffer.getInstance().getLevelFor(id));
            }
            catch (Exception e) {
                player.sendMessage("wrong scheme");
            }
        }
    }

    protected void healPlayer(PlayerEventInfo player, String target) {
        if (target.equals("player")) {
            if (this.canHeal(player, false)) {
                player.sendMessage("You've been healed.");
                player.setCurrentHp(player.getMaxHp());
                player.setCurrentMp(player.getMaxMp());
                player.setCurrentCp(player.getMaxCp());
            }
        } else if (target.equals("pet") && this.canHeal(player, true)) {
            player.sendMessage("Your pet has been healed.");
            player.healPet();
        }
    }

    private boolean canHeal(PlayerEventInfo player, boolean pet) {
        long time = System.currentTimeMillis();
        int id = player.getPlayersId();
        if (!pet) {
            if (this._healTimes.containsKey(id)) {
                long healedTime = this._healTimes.get(id);
                if (healedTime + (long)HEAL_DELAY_MS > time) {
                    long toWait = (healedTime + (long)HEAL_DELAY_MS - time) / 1000;
                    if (toWait > 60) {
                        player.sendMessage("You must still wait about " + (toWait / 60 + 1) + " minutes.");
                    } else {
                        player.sendMessage("You must still wait " + toWait + " seconds.");
                    }
                    return false;
                }
                this._healTimes.put(id, time);
                return true;
            }
            this._healTimes.put(id, time);
            return true;
        }
        if (this._healTimesPets.containsKey(id)) {
            long healedTime = this._healTimesPets.get(id);
            if (healedTime + (long)HEAL_DELAY_MS > time) {
                long toWait = (healedTime + (long)HEAL_DELAY_MS - time) / 1000;
                if (toWait > 60) {
                    player.sendMessage("You must still wait about " + (toWait / 60 + 1) + " minutes.");
                } else {
                    player.sendMessage("You must still wait " + toWait + " seconds.");
                }
                return false;
            }
            this._healTimesPets.put(id, time);
            return true;
        }
        this._healTimesPets.put(id, time);
        return true;
    }

    public boolean onCbBypass(PlayerEventInfo player, String bypass) {
        return super.onCbBypass(player, bypass);
    }
}

