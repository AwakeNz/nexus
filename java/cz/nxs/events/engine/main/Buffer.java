/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.NexusOut
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.SkillData
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 *  javolution.util.FastMap$Entry
 */
package cz.nxs.events.engine.main;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.main.Config;
import cz.nxs.interf.NexusOut;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.SkillData;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class Buffer {
    private FastMap<String, FastList<Integer>> buffTemplates;
    private FastMap<String, Boolean> changes;
    private UpdateTask updateTask;

    public static Buffer getInstance() {
        return _instance;
    }

    private Buffer() {
        this.updateTask = new UpdateTask();
        this.changes = new FastMap();
        this.buffTemplates = new FastMap();
        this.loadSQL();
        NexusOut.scheduleGeneralAtFixedRate((Runnable)this.updateTask, (long)600000, (long)600000);
    }

    public void buffPlayer(PlayerEventInfo player) {
        String playerId = "" + player.getPlayersId() + player.getClassIndex();
        if (!this.buffTemplates.containsKey((Object)playerId)) {
            NexusLoader.debug((String)("The player : " + player.getPlayersName() + " (" + playerId + ") without template"));
            return;
        }
        Iterator i$ = ((FastList)this.buffTemplates.get((Object)playerId)).iterator();
        while (i$.hasNext()) {
            int skillId = (Integer)i$.next();
            player.getSkillEffects(skillId, 1);
        }
    }

    public void changeList(PlayerEventInfo player, int buff, boolean action) {
        String playerId = "" + player.getPlayersId() + player.getClassIndex();
        if (!this.buffTemplates.containsKey((Object)playerId)) {
            this.buffTemplates.put((Object)playerId, (Object)new FastList());
            this.changes.put((Object)playerId, (Object)true);
        } else {
            if (!this.changes.containsKey((Object)playerId)) {
                this.changes.put((Object)playerId, (Object)false);
            }
            if (action) {
                ((FastList)this.buffTemplates.get((Object)playerId)).add((Object)buff);
            } else {
                ((FastList)this.buffTemplates.get((Object)playerId)).remove(((FastList)this.buffTemplates.get((Object)playerId)).indexOf((Object)buff));
            }
        }
    }

    private void loadSQL() {
        if (!EventConfig.getInstance().getGlobalConfigBoolean("eventBufferEnabled")) {
            return;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = NexusOut.getConnection();
            statement = con.prepareStatement("SELECT * FROM event_buffs");
            ResultSet rset = statement.executeQuery();
            int count = 0;
            while (rset.next()) {
                ++count;
                this.buffTemplates.put((Object)rset.getString("player"), (Object)new FastList());
                StringTokenizer st = new StringTokenizer(rset.getString("buffs"), ",");
                FastList templist = new FastList();
                while (st.hasMoreTokens()) {
                    templist.add((Object)Integer.parseInt(st.nextToken()));
                }
                this.buffTemplates.getEntry((Object)rset.getString("player")).setValue((Object)templist);
            }
            rset.close();
            statement.close();
            NexusLoader.debug((String)("Buffer loaded: " + count + " players template."));
        }
        catch (Exception e) {
            System.out.println("EventBuffs SQL catch");
        }
        finally {
            try {
                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean playerHaveTemplate(PlayerEventInfo player) {
        String playerId = "" + player.getPlayersId() + player.getClassIndex();
        if (this.buffTemplates.containsKey((Object)playerId)) {
            return true;
        }
        return false;
    }

    public void showHtml(PlayerEventInfo player) {
        try {
            SkillData skill;
            int skillId;
            String skillStr;
            String playerId = "" + player.getPlayersId() + player.getClassIndex();
            if (!this.buffTemplates.containsKey((Object)playerId)) {
                this.buffTemplates.put((Object)playerId, (Object)new FastList());
                this.changes.put((Object)playerId, (Object)true);
            }
            StringTokenizer st = new StringTokenizer(Config.getInstance().getString(0, "allowedBuffsList"), ",");
            FastList skillList = new FastList();
            while (st.hasMoreTokens()) {
                skillList.add((Object)Integer.parseInt(st.nextToken()));
            }
            TextBuilder sb = new TextBuilder();
            sb.append("<html><title>Event Manager - Buffer</title><body>");
            sb.append("<table width=270 border=0 bgcolor=666666><tr>");
            sb.append("<td><button value=\"Mini Events\" action=\"bypass -h minievents_mini_menu\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
            sb.append("<td><button value=\"Buffer\" action=\"bypass -h eventbuffershow\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
            sb.append("<td><button value=\"Statistics\" action=\"bypass -h eventstats 1\" width=90 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
            sb.append("</tr></table>");
            sb.append("<br>");
            sb.append("<center><table width=270 bgcolor=4f4f4f><tr><td width=70><font color=ac9775>Edit Buffs</font></td><td width=80></td><td width=120><font color=9f9f9f>Remaining slots:</font> <font color=ac9775>" + (Config.getInstance().getInt(0, "maxBuffNum") - ((FastList)this.buffTemplates.get((Object)playerId)).size()) + "</font></td></tr></table><br><br>");
            sb.append("<center><table width=270 bgcolor=4f4f4f><tr><td><font color=ac9775>Added buffs:</font></td></tr></table><br>");
            sb.append("<center><table width=270>");
            int c = 0;
            Iterator i$ = ((FastList)this.buffTemplates.get((Object)playerId)).iterator();
            while (i$.hasNext()) {
                skillId = (Integer)i$.next();
                skillStr = "0000";
                skillStr = skillId < 100 ? "00" + skillId : (skillId > 99 && skillId < 1000 ? "0" + skillId : (skillId > 4698 && skillId < 4701 ? "1331" : (skillId > 4701 && skillId < 4704 ? "1332" : "" + skillId)));
                skill = new SkillData(skillId, 1);
                if (++c % 2 == 1) {
                    sb.append("<tr><td width=33><img src=\"Icon.skill" + skillStr + "\" width=32 height=32></td><td width=100><a action=\"bypass -h eventbuffer " + skillId + " 0\"><font color=9f9f9f>" + skill.getName() + "</font></a></td>");
                }
                if (c % 2 != 0) continue;
                sb.append("<td width=33><img src=\"Icon.skill" + skillStr + "\" width=32 height=32></td><td width=100><a action=\"bypass -h eventbuffer " + skillId + " 0\"><font color=9f9f9f>" + skill.getName() + "</font></a></td></tr>");
            }
            if (c % 2 == 1) {
                sb.append("<td width=33></td><td width=100></td></tr>");
            }
            sb.append("</table><br>");
            sb.append("<br><br><center><table width=270 bgcolor=5A5A5A><tr><td><font color=ac9775>Available buffs:</font></td></tr></table><br>");
            sb.append("<center><table width=270>");
            c = 0;
            i$ = skillList.iterator();
            while (i$.hasNext()) {
                skillId = (Integer)i$.next();
                skillStr = "0000";
                skillStr = skillId < 100 ? "00" + skillId : (skillId > 99 && skillId < 1000 ? "0" + skillId : (skillId > 4698 && skillId < 4701 ? "1331" : (skillId > 4701 && skillId < 4704 ? "1332" : "" + skillId)));
                skill = new SkillData(skillId, 1);
                if (((FastList)this.buffTemplates.get((Object)playerId)).contains((Object)skillId)) continue;
                if (++c % 2 == 1) {
                    sb.append("<tr><td width=32><img src=\"Icon.skill" + skillStr + "\" width=32 height=32></td><td width=100>" + (Config.getInstance().getInt(0, "maxBuffNum") - ((FastList)this.buffTemplates.get((Object)playerId)).size() != 0 ? new StringBuilder().append("<a action=\"bypass -h eventbuffer ").append(skillId).append(" 1\"><font color=9f9f9f>").toString() : "") + skill.getName() + (Config.getInstance().getInt(0, "maxBuffNum") - ((FastList)this.buffTemplates.get((Object)playerId)).size() != 0 ? "</font></a>" : "") + "</td>");
                }
                if (c % 2 != 0) continue;
                sb.append("<td width=32><img src=\"Icon.skill" + skillStr + "\" width=32 height=32></td><td width=100>" + (Config.getInstance().getInt(0, "maxBuffNum") - ((FastList)this.buffTemplates.get((Object)playerId)).size() != 0 ? new StringBuilder().append("<a action=\"bypass -h eventbuffer ").append(skillId).append(" 1\"><font color=9f9f9f>").toString() : "") + skill.getName() + (Config.getInstance().getInt(0, "maxBuffNum") - ((FastList)this.buffTemplates.get((Object)playerId)).size() != 0 ? "</font></a>" : "") + "</td></tr>");
            }
            if (c % 2 == 1) {
                sb.append("<td width=33></td><td width=100></td></tr>");
            }
            sb.append("</table>");
            sb.append("</body></html>");
            String html = sb.toString();
            player.sendHtmlText(html);
            player.sendStaticPacket();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void updateSQL() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = NexusOut.getConnection();
            for (Map.Entry player : this.changes.entrySet()) {
                TextBuilder sb = new TextBuilder();
                int c = 0;
                Iterator i$ = ((FastList)this.buffTemplates.get(player.getKey())).iterator();
                while (i$.hasNext()) {
                    int buffid = (Integer)i$.next();
                    if (c == 0) {
                        sb.append(buffid);
                        ++c;
                        continue;
                    }
                    sb.append("," + buffid);
                }
                if (((Boolean)player.getValue()).booleanValue()) {
                    statement = con.prepareStatement("INSERT INTO event_buffs(player,buffs) VALUES (?,?)");
                    statement.setString(1, (String)player.getKey());
                    statement.setString(2, sb.toString());
                    statement.executeUpdate();
                    statement.close();
                    continue;
                }
                statement = con.prepareStatement("UPDATE event_buffs SET buffs=? WHERE player=?");
                statement.setString(1, sb.toString());
                statement.setString(2, (String)player.getKey());
                statement.executeUpdate();
                statement.close();
            }
        }
        catch (Exception e) {
            System.out.println("EventBuffs SQL catch");
        }
        finally {
            try {
                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        this.changes.clear();
    }

    private class UpdateTask
    implements Runnable {
        private UpdateTask() {
        }

        @Override
        public void run() {
            Buffer.this.updateSQL();
        }
    }

    private static class SingletonHolder {
        private static final Buffer _instance = new Buffer();

        private SingletonHolder() {
        }
    }

}

