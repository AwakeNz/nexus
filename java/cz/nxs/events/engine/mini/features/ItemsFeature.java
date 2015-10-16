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
import java.util.Arrays;

public class ItemsFeature
extends AbstractFeature {
    private boolean allowPotions = true;
    private boolean allowScrolls = true;
    private int[] disabledItems = null;
    private String[] enabledTiers = null;

    public ItemsFeature(EventType event, PlayerEventInfo gm, String parametersString) {
        super(event);
        this.addConfig("AllowPotions", "Will the potions be enabled for this mode?", 1);
        this.addConfig("AllowScrolls", "Will the scrolls be enabled for this mode?", 1);
        this.addConfig("DisabledItems", "Specify here which items will be disabled (not usable/equipable) for this mode. Write their IDs and separate by SPACE. Eg. <font color=LEVEL>111 222 525</font>. Put <font color=LEVEL>0</font> to disable this config.", 2);
        this.addConfig("EnabledTiers", "This config is not fully implemented. Requires gameserver support.", 2);
        if (parametersString == null || parametersString.split(",").length != 3) {
            parametersString = "true,false,0,Allitems";
        }
        this._params = parametersString;
        this.initValues();
    }

    @Override
    protected void initValues() {
        String[] params = this.splitParams(this._params);
        try {
            this.allowPotions = Boolean.parseBoolean(params[0]);
            this.allowScrolls = Boolean.parseBoolean(params[1]);
            String[] splitted = params[2].split(" ");
            this.disabledItems = new int[splitted.length];
            for (int i = 0; i < splitted.length; ++i) {
                this.disabledItems[i] = Integer.parseInt(splitted[i]);
            }
            Arrays.sort(this.disabledItems);
            String[] splitted2 = params[3].split(" ");
            this.enabledTiers = new String[splitted2.length];
            for (int i2 = 0; i2 < splitted2.length; ++i2) {
                if (splitted2[i2].length() <= 0) continue;
                this.enabledTiers[i2] = splitted2[i2];
            }
            Arrays.sort(this.enabledTiers);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
    }

    public boolean checkItem(PlayerEventInfo player, ItemData item) {
        if (!this.allowPotions && item.isPotion()) {
            return false;
        }
        if (!this.allowScrolls && item.isScroll()) {
            return false;
        }
        if (Arrays.binarySearch(this.disabledItems, item.getItemId()) >= 0) {
            return false;
        }
        if (!this.checkIfAllowed(item)) {
            return false;
        }
        return true;
    }

    private boolean checkIfAllowed(ItemData item) {
        return true;
    }

    @Override
    public boolean checkPlayer(PlayerEventInfo player) {
        boolean canJoin = true;
        for (ItemData item : player.getItems()) {
            if (!item.isWeapon() && !item.isArmor() || this.checkItem(player, item)) continue;
            canJoin = false;
            player.sendMessage("(I) Please put item " + item.getItemName() + " to your warehouse. It is not allowed in this event.");
        }
        return canJoin;
    }

    @Override
    public EventMode.FeatureType getType() {
        return EventMode.FeatureType.Items;
    }
}

