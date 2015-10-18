/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.effects.AbnormalEffect
 *  com.l2jserver.gameserver.model.items.L2Item
 *  com.l2jserver.gameserver.model.items.L2Weapon
 *  com.l2jserver.gameserver.model.items.type.L2WeaponType
 *  cz.nxs.l2j.CallBack
 *  cz.nxs.l2j.IValues
 *  cz.nxs.l2j.WeaponType
 */
package cz.nxs.interf;

import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.type.WeaponType;
import com.l2jserver.gameserver.model.skills.AbnormalType;

import cz.nxs.interf.delegate.ItemData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.IValues;

public class Values implements IValues
{
	public void load()
	{
		CallBack.getInstance().setValues(this);
	}
	
	@Override
	public int PAPERDOLL_UNDER()
	{
		return 0;
	}
	
	@Override
	public int PAPERDOLL_HEAD()
	{
		return 1;
	}
	
	@Override
	public int PAPERDOLL_HAIR()
	{
		return 2;
	}
	
	@Override
	public int PAPERDOLL_HAIR2()
	{
		return 3;
	}
	
	@Override
	public int PAPERDOLL_NECK()
	{
		return 4;
	}
	
	@Override
	public int PAPERDOLL_RHAND()
	{
		return 5;
	}
	
	@Override
	public int PAPERDOLL_CHEST()
	{
		return 6;
	}
	
	@Override
	public int PAPERDOLL_LHAND()
	{
		return 7;
	}
	
	@Override
	public int PAPERDOLL_REAR()
	{
		return 8;
	}
	
	@Override
	public int PAPERDOLL_LEAR()
	{
		return 9;
	}
	
	@Override
	public int PAPERDOLL_GLOVES()
	{
		return 10;
	}
	
	@Override
	public int PAPERDOLL_LEGS()
	{
		return 11;
	}
	
	@Override
	public int PAPERDOLL_FEET()
	{
		return 12;
	}
	
	@Override
	public int PAPERDOLL_RFINGER()
	{
		return 13;
	}
	
	@Override
	public int PAPERDOLL_LFINGER()
	{
		return 14;
	}
	
	@Override
	public int PAPERDOLL_LBRACELET()
	{
		return 15;
	}
	
	@Override
	public int PAPERDOLL_RBRACELET()
	{
		return 16;
	}
	
	@Override
	public int PAPERDOLL_DECO1()
	{
		return 17;
	}
	
	@Override
	public int PAPERDOLL_DECO2()
	{
		return 18;
	}
	
	@Override
	public int PAPERDOLL_DECO3()
	{
		return 19;
	}
	
	@Override
	public int PAPERDOLL_DECO4()
	{
		return 20;
	}
	
	@Override
	public int PAPERDOLL_DECO5()
	{
		return 21;
	}
	
	@Override
	public int PAPERDOLL_DECO6()
	{
		return 22;
	}
	
	@Override
	public int PAPERDOLL_CLOAK()
	{
		return 23;
	}
	
	@Override
	public int PAPERDOLL_BELT()
	{
		return 24;
	}
	
	@Override
	public int PAPERDOLL_TOTALSLOTS()
	{
		return 25;
	}
	
	@Override
	public int SLOT_NONE()
	{
		return 0;
	}
	
	@Override
	public int SLOT_UNDERWEAR()
	{
		return 1;
	}
	
	@Override
	public int SLOT_R_EAR()
	{
		return 2;
	}
	
	@Override
	public int SLOT_L_EAR()
	{
		return 4;
	}
	
	@Override
	public int SLOT_LR_EAR()
	{
		return 6;
	}
	
	@Override
	public int SLOT_NECK()
	{
		return 8;
	}
	
	@Override
	public int SLOT_R_FINGER()
	{
		return 16;
	}
	
	@Override
	public int SLOT_L_FINGER()
	{
		return 32;
	}
	
	@Override
	public int SLOT_LR_FINGER()
	{
		return 48;
	}
	
	@Override
	public int SLOT_HEAD()
	{
		return 64;
	}
	
	@Override
	public int SLOT_R_HAND()
	{
		return 128;
	}
	
	@Override
	public int SLOT_L_HAND()
	{
		return 256;
	}
	
	@Override
	public int SLOT_GLOVES()
	{
		return 512;
	}
	
	@Override
	public int SLOT_CHEST()
	{
		return 1024;
	}
	
	@Override
	public int SLOT_LEGS()
	{
		return 2048;
	}
	
	@Override
	public int SLOT_FEET()
	{
		return 4096;
	}
	
	@Override
	public int SLOT_BACK()
	{
		return 8192;
	}
	
	@Override
	public int SLOT_LR_HAND()
	{
		return 16384;
	}
	
	@Override
	public int SLOT_FULL_ARMOR()
	{
		return 32768;
	}
	
	@Override
	public int SLOT_HAIR()
	{
		return 65536;
	}
	
	@Override
	public int SLOT_ALLDRESS()
	{
		return 131072;
	}
	
	@Override
	public int SLOT_HAIR2()
	{
		return 262144;
	}
	
	@Override
	public int SLOT_HAIRALL()
	{
		return 524288;
	}
	
	@Override
	public int SLOT_R_BRACELET()
	{
		return 1048576;
	}
	
	@Override
	public int SLOT_L_BRACELET()
	{
		return 2097152;
	}
	
	@Override
	public int SLOT_DECO()
	{
		return 4194304;
	}
	
	@Override
	public int SLOT_BELT()
	{
		return 268435456;
	}
	
	@Override
	public int SLOT_WOLF()
	{
		return -100;
	}
	
	@Override
	public int SLOT_HATCHLING()
	{
		return -101;
	}
	
	@Override
	public int SLOT_STRIDER()
	{
		return -102;
	}
	
	@Override
	public int SLOT_BABYPET()
	{
		return -103;
	}
	
	@Override
	public int SLOT_GREATWOLF()
	{
		return -104;
	}
	
	@Override
	public int CRYSTAL_NONE()
	{
		return 0;
	}
	
	@Override
	public int CRYSTAL_D()
	{
		return 1;
	}
	
	@Override
	public int CRYSTAL_C()
	{
		return 2;
	}
	
	@Override
	public int CRYSTAL_B()
	{
		return 3;
	}
	
	@Override
	public int CRYSTAL_A()
	{
		return 4;
	}
	
	@Override
	public int CRYSTAL_S()
	{
		return 5;
	}
	
	@Override
	public int CRYSTAL_S80()
	{
		return 6;
	}
	
	@Override
	public int CRYSTAL_S84()
	{
		return 7;
	}
	
	@Override
	public int TYPE_ITEM()
	{
		return 1;
	}
	
	@Override
	public int TYPE_SKILL()
	{
		return 2;
	}
	
	@Override
	public int TYPE_ACTION()
	{
		return 3;
	}
	
	@Override
	public int TYPE_MACRO()
	{
		return 4;
	}
	
	@Override
	public int TYPE_RECIPE()
	{
		return 5;
	}
	
	@Override
	public int TYPE_TPBOOKMARK()
	{
		return 6;
	}
	
	public WeaponType getWeaponType(ItemData item)
	{
		WeaponType origType = ((L2Weapon) item.getTemplate()).getItemType();
		switch (origType)
		{
			case SWORD:
			{
				return WeaponType.SWORD;
			}
			case BLUNT:
			{
				return WeaponType.BLUNT;
			}
			case DAGGER:
			{
				return WeaponType.DAGGER;
			}
			case BOW:
			{
				return WeaponType.BOW;
			}
			case POLE:
			{
				return WeaponType.POLE;
			}
			case NONE:
			{
				return WeaponType.NONE;
			}
			case DUAL:
			{
				return WeaponType.DUAL;
			}
			case ETC:
			{
				return WeaponType.ETC;
			}
			case FIST:
			{
				return WeaponType.FIST;
			}
			case DUALFIST:
			{
				return WeaponType.DUALFIST;
			}
			case FISHINGROD:
			{
				return WeaponType.FISHINGROD;
			}
			case RAPIER:
			{
				return WeaponType.RAPIER;
			}
			case ANCIENTSWORD:
			{
				return WeaponType.ANCIENTSWORD;
			}
			case CROSSBOW:
			{
				return WeaponType.CROSSBOW;
			}
			case FLAG:
			{
				return WeaponType.FLAG;
			}
			case OWNTHING:
			{
				return WeaponType.OWNTHING;
			}
			case DUALDAGGER:
			{
				return WeaponType.DUALDAGGER;
			}
			/*
			 * case BIGBLUNT: { return WeaponType.BIGBLUNT; } case BIGSWORD: { return WeaponType.BIGSWORD; }
			 */
		}
		return null;
	}
	
	@Override
	public AbnormalType ABNORMAL_NULL()
	{
		return AbnormalType.NONE;
	}
	
	@Override
	public AbnormalType ABNORMAL_BLEEDING()
	{
		return AbnormalType.BLEEDING;
	}
	
	@Override
	public AbnormalType ABNORMAL_POISON()
	{
		return AbnormalType.POISON;
	}
	
	@Override
	public int ABNORMAL_REDCIRCLE()
	{
		return 0;
		// return AbnormalType.REDCIRCLE;
	}
	
	@Override
	public int ABNORMAL_ICE()
	{
		return 0;
		// return AbnormalType.ICE.getMask();
	}
	
	@Override
	public int ABNORMAL_WIND()
	{
		return 0;
		// return AbnormalType.WIND.getMask();
	}
	
	@Override
	public int ABNORMAL_FEAR()
	{
		return 0;
		// return AbnormalEffect.FEAR.getMask();
	}
	
	@Override
	public AbnormalType ABNORMAL_STUN()
	{
		return AbnormalType.STUN;
	}
	
	@Override
	public AbnormalType ABNORMAL_SLEEP()
	{
		return AbnormalType.SLEEP;
	}
	
	@Override
	public int ABNORMAL_MUTED()
	{
		return 0;
		// return AbnormalEffect.MUTED.getMask();
	}
	
	@Override
	public int ABNORMAL_ROOT()
	{
		return 0;
		// return AbnormalEffect.ROOT.getMask();
	}
	
	@Override
	public int ABNORMAL_HOLD_1()
	{
		return 0;
		// return AbnormalEffect.HOLD_1.getMask();
	}
	
	@Override
	public int ABNORMAL_HOLD_2()
	{
		return 0;
		// return AbnormalEffect.HOLD_2.getMask();
	}
	
	@Override
	public int ABNORMAL_UNKNOWN_13()
	{
		return 0;
		// return AbnormalEffect.UNKNOWN_13.getMask();
	}
	
	@Override
	public AbnormalType ABNORMAL_BIG_HEAD()
	{
		return AbnormalType.BIG_HEAD;
	}
	
	@Override
	public int ABNORMAL_FLAME()
	{
		return 0;
		// return AbnormalEffect.FLAME.getMask();
	}
	
	@Override
	public int ABNORMAL_UNKNOWN_16()
	{
		return 0;
		// return AbnormalEffect.UNKNOWN_16.getMask();
	}
	
	@Override
	public int ABNORMAL_GROW()
	{
		return 0;
		// return AbnormalEffect.GROW.getMask();
	}
	
	@Override
	public int ABNORMAL_FLOATING_ROOT()
	{
		return 0;
		// return AbnormalEffect.FLOATING_ROOT.getMask();
	}
	
	@Override
	public int ABNORMAL_DANCE_STUNNED()
	{
		return 0;
		// return AbnormalEffect.DANCE_STUNNED.getMask();
	}
	
	@Override
	public int ABNORMAL_FIREROOT_STUN()
	{
		return 0;
		// return AbnormalEffect.FIREROOT_STUN.getMask();
	}
	
	@Override
	public int ABNORMAL_STEALTH()
	{
		return 0;
		// return AbnormalEffect.STEALTH.getMask();
	}
	
	@Override
	public int ABNORMAL_IMPRISIONING_1()
	{
		return 0;
		// return AbnormalEffect.IMPRISIONING_1.getMask();
	}
	
	@Override
	public int ABNORMAL_IMPRISIONING_2()
	{
		return 0;
		// return AbnormalEffect.IMPRISIONING_2.getMask();
	}
	
	@Override
	public int ABNORMAL_MAGIC_CIRCLE()
	{
		return 0;
		// return AbnormalEffect.MAGIC_CIRCLE.getMask();
	}
	
	@Override
	public int ABNORMAL_ICE2()
	{
		return 0;
		// return AbnormalEffect.ICE2.getMask();
	}
	
	@Override
	public int ABNORMAL_EARTHQUAKE()
	{
		return 0;
		// return AbnormalEffect.EARTHQUAKE.getMask();
	}
	
	@Override
	public int ABNORMAL_UNKNOWN_27()
	{
		return 0;
		// return AbnormalEffect.UNKNOWN_27.getMask();
	}
	
	@Override
	public int ABNORMAL_INVULNERABLE()
	{
		return 0;
		// return AbnormalEffect.INVULNERABLE.getMask();
	}
	
	@Override
	public int ABNORMAL_VITALITY()
	{
		return 0;
		// return AbnormalEffect.VITALITY.getMask();
	}
	
	@Override
	public int ABNORMAL_REAL_TARGET()
	{
		return 0;
		// return AbnormalEffect.REAL_TARGET.getMask();
	}
	
	@Override
	public int ABNORMAL_DEATH_MARK()
	{
		return 0;
		// return AbnormalEffect.DEATH_MARK.getMask();
	}
	
	@Override
	public int ABNORMAL_SKULL_FEAR()
	{
		return 0;
		// return AbnormalEffect.SKULL_FEAR.getMask();
	}
	
	@Override
	public int ABNORMAL_S_INVINCIBLE()
	{
		return 0;
		// return AbnormalType.S_INVINCIBLE;
	}
	
	@Override
	public int ABNORMAL_S_AIR_STUN()
	{
		return 0;
		// return AbnormalEffect.S_AIR_STUN.getMask();
	}
	
	@Override
	public int ABNORMAL_S_AIR_ROOT()
	{
		return 0;
		// return AbnormalEffect.S_AIR_ROOT.getMask();
	}
	
	@Override
	public int ABNORMAL_S_BAGUETTE_SWORD()
	{
		return 0;
		// return AbnormalEffect.S_BAGUETTE_SWORD.getMask();
	}
	
	@Override
	public int ABNORMAL_S_YELLOW_AFFRO()
	{
		return 0;
		// return AbnormalEffect.S_YELLOW_AFFRO.getMask();
	}
	
	@Override
	public int ABNORMAL_S_PINK_AFFRO()
	{
		return 0;
		// return AbnormalEffect.S_PINK_AFFRO.getMask();
	}
	
	@Override
	public int ABNORMAL_S_BLACK_AFFRO()
	{
		return 0;
		// return AbnormalEffect.S_BLACK_AFFRO.getMask();
	}
	
	@Override
	public int ABNORMAL_S_UNKNOWN8()
	{
		return 0;
		// return AbnormalEffect.S_UNKNOWN8.getMask();
	}
	
	@Override
	public int ABNORMAL_S_STIGMA_SHILIEN()
	{
		return 0;
		// return AbnormalEffect.S_STIGMA_SHILIEN.getMask();
	}
	
	@Override
	public int ABNORMAL_S_STAKATOROOT()
	{
		return 0;
		// return AbnormalEffect.S_STAKATOROOT.getMask();
	}
	
	@Override
	public int ABNORMAL_S_FREEZING()
	{
		return 0;
		// return AbnormalEffect.S_FREEZING.getMask();
	}
	
	@Override
	public int ABNORMAL_S_VESPER()
	{
		return 0;
		// return AbnormalEffect.S_VESPER_S.getMask();
	}
	
	@Override
	public int ABNORMAL_E_AFRO_1()
	{
		return 0;
		// return AbnormalEffect.E_AFRO_1.getMask();
	}
	
	@Override
	public int ABNORMAL_E_AFRO_2()
	{
		return 0;
		// return AbnormalEffect.E_AFRO_2.getMask();
	}
	
	@Override
	public int ABNORMAL_E_AFRO_3()
	{
		return 0;
		// return AbnormalEffect.E_AFRO_3.getMask();
	}
	
	@Override
	public int ABNORMAL_E_EVASWRATH()
	{
		return 0;
		// return AbnormalEffect.E_EVASWRATH.getMask();
	}
	
	@Override
	public int ABNORMAL_E_HEADPHONE()
	{
		return 0;
		// return AbnormalEffect.E_HEADPHONE.getMask();
	}
	
	@Override
	public int ABNORMAL_E_VESPER_1()
	{
		return 0;
		// return AbnormalEffect.E_VESPER_1.getMask();
	}
	
	@Override
	public int ABNORMAL_E_VESPER_2()
	{
		return 0;
		// return AbnormalEffect.E_VESPER_2.getMask();
	}
	
	@Override
	public int ABNORMAL_E_VESPER_3()
	{
		return 0;
		// return AbnormalEffect.E_VESPER_3.getMask();
	}
	
	public static final Values getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final Values _instance = new Values();
		
		private SingletonHolder()
		{
		}
	}
	
}
