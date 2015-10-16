/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.datatables.ItemTable
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  com.l2jserver.gameserver.model.items.L2Armor
 *  com.l2jserver.gameserver.model.items.L2EtcItem
 *  com.l2jserver.gameserver.model.items.L2Item
 *  com.l2jserver.gameserver.model.items.L2Weapon
 *  com.l2jserver.gameserver.model.items.instance.L2ItemInstance
 *  com.l2jserver.gameserver.model.items.type.L2EtcItemType
 *  com.l2jserver.gameserver.model.items.type.L2ItemType
 *  cz.nxs.l2j.WeaponType
 *  cz.nxs.l2j.delegate.IItemData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.L2Armor;
import com.l2jserver.gameserver.model.items.L2EtcItem;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.items.type.L2EtcItemType;
import com.l2jserver.gameserver.model.items.type.L2ItemType;
import cz.nxs.interf.Values;
import cz.nxs.l2j.WeaponType;
import cz.nxs.l2j.delegate.IItemData;

public class ItemData
implements IItemData {
    private L2ItemInstance _item;
    private L2Item _itemTemplate;

    public ItemData(int id) {
        this._item = null;
        this._itemTemplate = ItemTable.getInstance().getTemplate(id);
    }

    public ItemData(L2ItemInstance cha) {
        this._item = cha;
        if (this._item != null) {
            this._itemTemplate = this._item.getItem();
        }
    }

    public ItemData(int itemId, int count) {
        this._item = ItemTable.getInstance().createItem("Event Engine ItemData", itemId, count, null);
        if (this._item != null) {
            this._itemTemplate = this._item.getItem();
        }
    }

    public L2ItemInstance getOwner() {
        return this._item;
    }

    public int getObjectId() {
        if (this.exists()) {
            return this.getOwner().getObjectId();
        }
        return -1;
    }

    public L2Item getTemplate() {
        return this._itemTemplate;
    }

    public boolean exists() {
        return this._item != null;
    }

    public boolean isEquipped() {
        if (this.exists() && this._item.isEquipped()) {
            return true;
        }
        return false;
    }

    public int getItemId() {
        return this._itemTemplate.getItemId();
    }

    public String getItemName() {
        return this._itemTemplate.getName();
    }

    public int getEnchantLevel() {
        return this._item != null ? this._item.getEnchantLevel() : 0;
    }

    public int getCrystalType() {
        return this._itemTemplate.getCrystalType();
    }

    public int getBodyPart() {
        return this._itemTemplate.getBodyPart();
    }

    public boolean isArmor() {
        return this._itemTemplate instanceof L2Armor;
    }

    public boolean isWeapon() {
        return this._itemTemplate instanceof L2Weapon;
    }

    public WeaponType getWeaponType() {
        if (this.isWeapon()) {
            return Values.getInstance().getWeaponType(this);
        }
        return null;
    }

    public boolean isType2Armor() {
        return this._itemTemplate.getType2() == 1;
    }

    public boolean isType2Weapon() {
        return this._itemTemplate.getType2() == 0;
    }

    public boolean isType2Accessory() {
        return this._itemTemplate.getType2() == 2;
    }

    public boolean isJewellery() {
        return this._itemTemplate.getType2() == 2;
    }

    public boolean isPotion() {
        return this._itemTemplate.getItemType() == L2EtcItemType.POTION;
    }

    public boolean isScroll() {
        return this._itemTemplate.getItemType() == L2EtcItemType.SCROLL;
    }

    public boolean isPetCollar() {
        if (this._item != null && this._item.isEtcItem() && this._item.getEtcItem().getItemType() == L2EtcItemType.PET_COLLAR) {
            return true;
        }
        return false;
    }

    public String getTier() {
        String tier = null;
        return tier;
    }

    public void setEnchantLevel(int level) {
        if (this._item.isArmor() || this._item.isWeapon()) {
            // empty if block
        }
    }

    public void restoreEnchantLevel() {
    }
}

