/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.ItemData
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.playervalue.criteria;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.base.GlobalConfigModel;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IValues;
import cz.nxs.l2j.WeaponType;
import cz.nxs.playervalue.criteria.ICriteria;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class GearScore
implements ICriteria {
    private FastMap<Integer, Integer> scores;
    private FastList<Integer> changed = new FastList();

    public GearScore() {
        this.loadData();
    }

    private void loadData() {
        FastMap data;
        Connection con = null;
        int size = 0;
        try {
            int id;
            int def;
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT itemId, score FROM nexus_playervalue_items");
            ResultSet rset = statement.executeQuery();
            data = new FastMap();
            while (rset.next()) {
                int itemId = rset.getInt("itemId");
                int score = rset.getInt("score");
                data.put((Object)itemId, (Object)score);
            }
            rset.close();
            statement.close();
            this.scores = new FastMap();
            FastMap missing = new FastMap();
            Iterator<Integer> i$ = CallBack.getInstance().getOut().getAllArmorsId().iterator();
            while (i$.hasNext()) {
                id = i$.next();
                ++size;
                if (data.containsKey((Object)id)) {
                    this.scores.put((Object)id, data.get((Object)id));
                    continue;
                }
                def = this.getDefaultValue(id);
                this.scores.put((Object)id, (Object)def);
                missing.put((Object)id, (Object)def);
            }
            i$ = CallBack.getInstance().getOut().getAllWeaponsId().iterator();
            while (i$.hasNext()) {
                id = i$.next();
                ++size;
                if (data.containsKey((Object)id)) {
                    this.scores.put((Object)id, data.get((Object)id));
                    continue;
                }
                def = this.getDefaultValue(id);
                this.scores.put((Object)id, (Object)def);
                missing.put((Object)id, (Object)def);
            }
            if (!missing.isEmpty()) {
                TextBuilder tb = new TextBuilder();
                for (Map.Entry e : missing.entrySet()) {
                    tb.append("(" + e.getKey() + "," + e.getValue() + "),");
                }
                String values = tb.toString();
                statement = con.prepareStatement("INSERT INTO nexus_playervalue_items (itemId,score) VALUES " + values.substring(0, values.length() - 1) + ";");
                statement.execute();
                missing = null;
                statement.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            EventConfig.getInstance().getGlobalConfig("GearScore", "enableGearScore").setValue("false");
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {}
        }
        data = null;
        NexusLoader.debug((String)("Nexus Engine: Gear score engine - loaded " + size + " items."));
    }

    public void saveAll() {
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            TextBuilder tb = new TextBuilder();
            Iterator i$ = this.changed.iterator();
            while (i$.hasNext()) {
                int i = (Integer)i$.next();
                tb.append("(" + i + "," + this.getScore(i) + "),");
            }
            String values = tb.toString();
            PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_playervalue_items (itemId,score) VALUES " + values.substring(0, values.length() - 1) + ";");
            statement.execute();
            statement.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {}
        }
    }

    public int getScore(int itemId) {
        return (Integer)this.scores.get((Object)itemId);
    }

    public void setScore(int itemId, int value) {
        this.scores.put((Object)itemId, (Object)value);
        this.changed.add((Object)itemId);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public int getDefaultValue(int itemId) {
        ItemData item = new ItemData(itemId);
        int score = 0;
        String configName = "defVal_";
        if (item.getCrystalType() == CallBack.getInstance().getValues().CRYSTAL_NONE()) {
            configName = configName + "N-Grade_";
        } else if (item.getCrystalType() == CallBack.getInstance().getValues().CRYSTAL_D()) {
            configName = configName + "D-Grade_";
        } else if (item.getCrystalType() == CallBack.getInstance().getValues().CRYSTAL_C()) {
            configName = configName + "C-Grade_";
        } else if (item.getCrystalType() == CallBack.getInstance().getValues().CRYSTAL_B()) {
            configName = configName + "B-Grade_";
        } else if (item.getCrystalType() == CallBack.getInstance().getValues().CRYSTAL_A()) {
            configName = configName + "A-Grade_";
        } else if (item.getCrystalType() == CallBack.getInstance().getValues().CRYSTAL_S()) {
            configName = configName + "S-Grade_";
        } else if (item.getCrystalType() == CallBack.getInstance().getValues().CRYSTAL_S80()) {
            configName = configName + "S80-Grade_";
        } else if (item.getCrystalType() == CallBack.getInstance().getValues().CRYSTAL_S84()) {
            configName = configName + "S84-Grade_";
        }
        if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_UNDERWEAR()) {
            configName = configName + "Underwear";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_L_EAR() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_LR_EAR() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_R_EAR()) {
            configName = configName + "Earring";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_NECK()) {
            configName = configName + "Necklace";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_R_FINGER() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_L_FINGER() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_LR_FINGER()) {
            configName = configName + "Ring";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_HEAD()) {
            configName = configName + "Helmet";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_R_HAND() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_LR_HAND()) {
            if (!item.isWeapon()) return 0;
            if (item.getWeaponType() == null) {
                return 0;
            }
            String first = item.getWeaponType().toString();
            if (first.length() > 1) {
                first = first.substring(0, 1);
                String name = item.getWeaponType().toString();
                name = name.substring(1, name.length()).toLowerCase();
                configName = configName + first + name;
            } else {
                configName = configName + item.getWeaponType().toString();
            }
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_L_HAND()) {
            configName = configName + "Shield";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_GLOVES()) {
            configName = configName + "Gloves";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_CHEST()) {
            configName = configName + "Chest";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_LEGS()) {
            configName = configName + "Gaiters";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_FEET()) {
            configName = configName + "Boots";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_BACK()) {
            configName = configName + "Cloak";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_FULL_ARMOR()) {
            configName = configName + "FullArmor";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_HAIR() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_HAIR2() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_HAIRALL()) {
            configName = configName + "Hair";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_R_BRACELET() || item.getBodyPart() == CallBack.getInstance().getValues().SLOT_L_BRACELET()) {
            configName = configName + "Bracelet";
        } else if (item.getBodyPart() == CallBack.getInstance().getValues().SLOT_DECO()) {
            configName = configName + "Talisman";
        } else {
            if (item.getBodyPart() != CallBack.getInstance().getValues().SLOT_BELT()) return 0;
            configName = configName + "Belt";
        }
        if (EventConfig.getInstance().globalConfigExists(configName)) return EventConfig.getInstance().getGlobalConfigInt(configName);
        GlobalConfigModel gc = EventConfig.getInstance().addGlobalConfig("GearScore", configName, "Gear score default value for " + configName + " equippable item type.", "0", 1);
        EventConfig.getInstance().saveGlobalConfig(gc);
        return 0;
    }

    @Override
    public int getPoints(PlayerEventInfo player) {
        int points = 0;
        for (ItemData item : player.getItems()) {
            if (!item.isEquipped() || !item.isArmor() && !item.isJewellery() && !item.isWeapon()) continue;
            points+=this.getScore(item.getItemId());
        }
        return points;
    }

    public static final GearScore getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final GearScore _instance = new GearScore();

        private SingletonHolder() {
        }
    }

}

