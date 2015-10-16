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
import com.l2jserver.gameserver.model.items.L2Armor;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.items.type.CrystalType;
import com.l2jserver.gameserver.model.items.type.EtcItemType;

import cz.nxs.interf.Values;
import cz.nxs.l2j.WeaponType;
import cz.nxs.l2j.delegate.IItemData;

public class ItemData implements IItemData
{
	private final L2ItemInstance _item;
	private L2Item _itemTemplate;
	
	public ItemData(int id)
	{
		this._item = null;
		this._itemTemplate = ItemTable.getInstance().getTemplate(id);
	}
	
	public ItemData(L2ItemInstance cha)
	{
		this._item = cha;
		if (this._item != null)
		{
			this._itemTemplate = this._item.getItem();
		}
	}
	
	public ItemData(int itemId, int count)
	{
		this._item = ItemTable.getInstance().createItem("Event Engine ItemData", itemId, count, null);
		if (this._item != null)
		{
			this._itemTemplate = this._item.getItem();
		}
	}
	
	public L2ItemInstance getOwner()
	{
		return this._item;
	}
	
	@Override
	public int getObjectId()
	{
		if (this.exists())
		{
			return this.getOwner().getObjectId();
		}
		return -1;
	}
	
	public L2Item getTemplate()
	{
		return this._itemTemplate;
	}
	
	@Override
	public boolean exists()
	{
		return this._item != null;
	}
	
	@Override
	public boolean isEquipped()
	{
		if (this.exists() && this._item.isEquipped())
		{
			return true;
		}
		return false;
	}
	
	@Override
	public int getItemId()
	{
		return this._itemTemplate.getId();
	}
	
	@Override
	public String getItemName()
	{
		return this._itemTemplate.getName();
	}
	
	@Override
	public int getEnchantLevel()
	{
		return this._item != null ? this._item.getEnchantLevel() : 0;
	}
	
	@Override
	public CrystalType getCrystalType()
	{
		return this._itemTemplate.getCrystalType();
	}
	
	@Override
	public int getBodyPart()
	{
		return this._itemTemplate.getBodyPart();
	}
	
	@Override
	public boolean isArmor()
	{
		return this._itemTemplate instanceof L2Armor;
	}
	
	@Override
	public boolean isWeapon()
	{
		return this._itemTemplate instanceof L2Weapon;
	}
	
	@Override
	public WeaponType getWeaponType()
	{
		if (this.isWeapon())
		{
			return Values.getInstance().getWeaponType(this);
		}
		return null;
	}
	
	@Override
	public boolean isType2Armor()
	{
		return this._itemTemplate.getType2() == 1;
	}
	
	@Override
	public boolean isType2Weapon()
	{
		return this._itemTemplate.getType2() == 0;
	}
	
	@Override
	public boolean isType2Accessory()
	{
		return this._itemTemplate.getType2() == 2;
	}
	
	@Override
	public boolean isJewellery()
	{
		return this._itemTemplate.getType2() == 2;
	}
	
	@Override
	public boolean isPotion()
	{
		return this._itemTemplate.getItemType() == EtcItemType.POTION;
	}
	
	@Override
	public boolean isScroll()
	{
		return this._itemTemplate.getItemType() == EtcItemType.SCROLL;
	}
	
	@Override
	public boolean isPetCollar()
	{
		if ((this._item != null) && this._item.isEtcItem() && (this._item.getEtcItem().getItemType() == EtcItemType.PET_COLLAR))
		{
			return true;
		}
		return false;
	}
	
	@Override
	public String getTier()
	{
		String tier = null;
		return tier;
	}
	
	@Override
	public void setEnchantLevel(int level)
	{
		if (this._item.isArmor() || this._item.isWeapon())
		{
			// empty if block
		}
	}
	
	@Override
	public void restoreEnchantLevel()
	{
	}
}
