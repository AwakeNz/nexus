/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.ItemData
 */
package cz.nxs.events.engine.mini.features;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.ItemData;

public class EnchantFeature
extends AbstractFeature {
    private int maxEnchantWeapon = 0;
    private int maxEnchantArmor = 0;
    private int maxEnchantJewel = 0;
    private int minEnchantWeapon = 0;
    private int minEnchantArmor = 0;
    private int minEnchantJewel = 0;
    private int autosetEnchantWeapon = -1;
    private int autosetEnchantArmor = -1;
    private int autosetEnchantJewel = -1;

    public EnchantFeature(EventType event, PlayerEventInfo gm, String parametersString) {
        super(event);
        this.addConfig("MaxEnchantWeapon", "The max enchant limi for weapons. If the item has bigger enchant level, then the player will not be able to participate till he puts the item to his WH. -1 to disable this config, 0 to disallow enchants.", 1);
        this.addConfig("MaxEnchantArmor", "The max enchant limit for armors. If the item has bigger enchant level, then the player will not be able to participate till he puts the item to his WH. -1 to disable this config, 0 to disallow enchants.", 1);
        this.addConfig("MaxEnchantJewel", "The max enchant limit for jewels. If the item has bigger enchant level, then the player will not be able to participate till he puts the item to his WH. -1 to disable this config, 0 to disallow enchants.", 1);
        this.addConfig("MinEnchantWeapon", "The min enchant weapon must have otherwise it will be unusable during the event. 0 to disable this config.", 1);
        this.addConfig("MinEnchantArmor", "The min enchant armor must have otherwise it will be unusable during the event. 0 to disable this config.", 1);
        this.addConfig("MinEnchantJewel", "The min enchant jewel must have otherwise it will be unusable during the event. 0 to disable this config.", 1);
        this.addConfig("AutoEnchantWeap", "All weapons, if their enchant is higher than this value, will be lowered to this value. -1 to disable this config.", 1);
        this.addConfig("AutoEnchantArmor", "All armors, if their enchant is higher than this value, will be lowered to this value. -1 to disable this config.", 1);
        this.addConfig("AutoEnchantJewel", "All jewels, if their enchant is higher than this value, will be lowered to this value. -1 to disable this config.", 1);
        if (parametersString == null) {
            parametersString = "-1,-1,-1,0,0,0,-1,-1,-1";
        }
        this._params = parametersString;
        this.initValues();
    }

    @Override
    protected void initValues() {
        String[] params = this.splitParams(this._params);
        try {
            this.maxEnchantWeapon = Integer.parseInt(params[0]);
            this.maxEnchantArmor = Integer.parseInt(params[1]);
            this.maxEnchantJewel = Integer.parseInt(params[2]);
            this.minEnchantWeapon = Integer.parseInt(params[3]);
            this.minEnchantArmor = Integer.parseInt(params[4]);
            this.minEnchantJewel = Integer.parseInt(params[5]);
            this.autosetEnchantWeapon = Integer.parseInt(params[6]);
            this.autosetEnchantArmor = Integer.parseInt(params[7]);
            this.autosetEnchantJewel = Integer.parseInt(params[8]);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
    }

    public int getMaxEnchantWeapon() {
        return this.maxEnchantWeapon;
    }

    public int getMaxEnchantArmor() {
        return this.maxEnchantArmor;
    }

    public int getMaxEnchantJewel() {
        return this.maxEnchantJewel;
    }

    public int getMinEnchantWeapon() {
        return this.minEnchantWeapon;
    }

    public int getMinEnchantArmor() {
        return this.minEnchantArmor;
    }

    public int getMinEnchantJewel() {
        return this.minEnchantJewel;
    }

    public int getAutoEnchantWeapon() {
        return this.autosetEnchantWeapon;
    }

    public int getAutoEnchantArmor() {
        return this.autosetEnchantArmor;
    }

    public int getAutoEnchantJewel() {
        return this.autosetEnchantJewel;
    }

    public boolean checkItem(PlayerEventInfo player, ItemData item) {
        if (item.isType2Accessory()) {
            if (this.maxEnchantJewel > -1 && item.getEnchantLevel() > this.maxEnchantJewel) {
                return false;
            }
            if (this.minEnchantJewel > item.getEnchantLevel()) {
                return false;
            }
        } else if (item.isType2Armor()) {
            if (this.maxEnchantArmor > -1 && item.getEnchantLevel() > this.maxEnchantArmor) {
                return false;
            }
            if (this.minEnchantArmor > item.getEnchantLevel()) {
                return false;
            }
        } else if (item.isType2Weapon()) {
            if (this.maxEnchantWeapon > -1 && item.getEnchantLevel() > this.maxEnchantWeapon) {
                return false;
            }
            if (this.minEnchantWeapon > item.getEnchantLevel()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean checkPlayer(PlayerEventInfo player) {
        boolean canJoin = true;
        for (ItemData item : player.getItems()) {
            if (this.checkItem(player, item)) continue;
            canJoin = false;
            player.sendMessage("Please put item " + item.getItemName() + " to your warehouse before participating. It is not allowed for this event.");
        }
        return canJoin;
    }

    @Override
    public EventMode.FeatureType getType() {
        return EventMode.FeatureType.Enchant;
    }
}

