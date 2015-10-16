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

import com.l2jserver.gameserver.model.effects.AbnormalEffect;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.type.L2WeaponType;
import cz.nxs.interf.delegate.ItemData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.IValues;
import cz.nxs.l2j.WeaponType;

public class Values
implements IValues {
    public void load() {
        CallBack.getInstance().setValues((IValues)this);
    }

    public int PAPERDOLL_UNDER() {
        return 0;
    }

    public int PAPERDOLL_HEAD() {
        return 1;
    }

    public int PAPERDOLL_HAIR() {
        return 2;
    }

    public int PAPERDOLL_HAIR2() {
        return 3;
    }

    public int PAPERDOLL_NECK() {
        return 4;
    }

    public int PAPERDOLL_RHAND() {
        return 5;
    }

    public int PAPERDOLL_CHEST() {
        return 6;
    }

    public int PAPERDOLL_LHAND() {
        return 7;
    }

    public int PAPERDOLL_REAR() {
        return 8;
    }

    public int PAPERDOLL_LEAR() {
        return 9;
    }

    public int PAPERDOLL_GLOVES() {
        return 10;
    }

    public int PAPERDOLL_LEGS() {
        return 11;
    }

    public int PAPERDOLL_FEET() {
        return 12;
    }

    public int PAPERDOLL_RFINGER() {
        return 13;
    }

    public int PAPERDOLL_LFINGER() {
        return 14;
    }

    public int PAPERDOLL_LBRACELET() {
        return 15;
    }

    public int PAPERDOLL_RBRACELET() {
        return 16;
    }

    public int PAPERDOLL_DECO1() {
        return 17;
    }

    public int PAPERDOLL_DECO2() {
        return 18;
    }

    public int PAPERDOLL_DECO3() {
        return 19;
    }

    public int PAPERDOLL_DECO4() {
        return 20;
    }

    public int PAPERDOLL_DECO5() {
        return 21;
    }

    public int PAPERDOLL_DECO6() {
        return 22;
    }

    public int PAPERDOLL_CLOAK() {
        return 23;
    }

    public int PAPERDOLL_BELT() {
        return 24;
    }

    public int PAPERDOLL_TOTALSLOTS() {
        return 25;
    }

    public int SLOT_NONE() {
        return 0;
    }

    public int SLOT_UNDERWEAR() {
        return 1;
    }

    public int SLOT_R_EAR() {
        return 2;
    }

    public int SLOT_L_EAR() {
        return 4;
    }

    public int SLOT_LR_EAR() {
        return 6;
    }

    public int SLOT_NECK() {
        return 8;
    }

    public int SLOT_R_FINGER() {
        return 16;
    }

    public int SLOT_L_FINGER() {
        return 32;
    }

    public int SLOT_LR_FINGER() {
        return 48;
    }

    public int SLOT_HEAD() {
        return 64;
    }

    public int SLOT_R_HAND() {
        return 128;
    }

    public int SLOT_L_HAND() {
        return 256;
    }

    public int SLOT_GLOVES() {
        return 512;
    }

    public int SLOT_CHEST() {
        return 1024;
    }

    public int SLOT_LEGS() {
        return 2048;
    }

    public int SLOT_FEET() {
        return 4096;
    }

    public int SLOT_BACK() {
        return 8192;
    }

    public int SLOT_LR_HAND() {
        return 16384;
    }

    public int SLOT_FULL_ARMOR() {
        return 32768;
    }

    public int SLOT_HAIR() {
        return 65536;
    }

    public int SLOT_ALLDRESS() {
        return 131072;
    }

    public int SLOT_HAIR2() {
        return 262144;
    }

    public int SLOT_HAIRALL() {
        return 524288;
    }

    public int SLOT_R_BRACELET() {
        return 1048576;
    }

    public int SLOT_L_BRACELET() {
        return 2097152;
    }

    public int SLOT_DECO() {
        return 4194304;
    }

    public int SLOT_BELT() {
        return 268435456;
    }

    public int SLOT_WOLF() {
        return -100;
    }

    public int SLOT_HATCHLING() {
        return -101;
    }

    public int SLOT_STRIDER() {
        return -102;
    }

    public int SLOT_BABYPET() {
        return -103;
    }

    public int SLOT_GREATWOLF() {
        return -104;
    }

    public int CRYSTAL_NONE() {
        return 0;
    }

    public int CRYSTAL_D() {
        return 1;
    }

    public int CRYSTAL_C() {
        return 2;
    }

    public int CRYSTAL_B() {
        return 3;
    }

    public int CRYSTAL_A() {
        return 4;
    }

    public int CRYSTAL_S() {
        return 5;
    }

    public int CRYSTAL_S80() {
        return 6;
    }

    public int CRYSTAL_S84() {
        return 7;
    }

    public int TYPE_ITEM() {
        return 1;
    }

    public int TYPE_SKILL() {
        return 2;
    }

    public int TYPE_ACTION() {
        return 3;
    }

    public int TYPE_MACRO() {
        return 4;
    }

    public int TYPE_RECIPE() {
        return 5;
    }

    public int TYPE_TPBOOKMARK() {
        return 6;
    }

    public WeaponType getWeaponType(ItemData item) {
        L2WeaponType origType = ((L2Weapon)item.getTemplate()).getItemType();
        switch (origType) {
            case SWORD: {
                return WeaponType.SWORD;
            }
            case BLUNT: {
                return WeaponType.BLUNT;
            }
            case DAGGER: {
                return WeaponType.DAGGER;
            }
            case BOW: {
                return WeaponType.BOW;
            }
            case POLE: {
                return WeaponType.POLE;
            }
            case NONE: {
                return WeaponType.NONE;
            }
            case DUAL: {
                return WeaponType.DUAL;
            }
            case ETC: {
                return WeaponType.ETC;
            }
            case FIST: {
                return WeaponType.FIST;
            }
            case DUALFIST: {
                return WeaponType.DUALFIST;
            }
            case FISHINGROD: {
                return WeaponType.FISHINGROD;
            }
            case RAPIER: {
                return WeaponType.RAPIER;
            }
            case ANCIENTSWORD: {
                return WeaponType.ANCIENTSWORD;
            }
            case CROSSBOW: {
                return WeaponType.CROSSBOW;
            }
            case FLAG: {
                return WeaponType.FLAG;
            }
            case OWNTHING: {
                return WeaponType.OWNTHING;
            }
            case DUALDAGGER: {
                return WeaponType.DUALDAGGER;
            }
            case BIGBLUNT: {
                return WeaponType.BIGBLUNT;
            }
            case BIGSWORD: {
                return WeaponType.BIGSWORD;
            }
        }
        return null;
    }

    public int ABNORMAL_NULL() {
        return AbnormalEffect.NULL.getMask();
    }

    public int ABNORMAL_BLEEDING() {
        return AbnormalEffect.BLEEDING.getMask();
    }

    public int ABNORMAL_POISON() {
        return AbnormalEffect.POISON.getMask();
    }

    public int ABNORMAL_REDCIRCLE() {
        return AbnormalEffect.REDCIRCLE.getMask();
    }

    public int ABNORMAL_ICE() {
        return AbnormalEffect.ICE.getMask();
    }

    public int ABNORMAL_WIND() {
        return AbnormalEffect.WIND.getMask();
    }

    public int ABNORMAL_FEAR() {
        return AbnormalEffect.FEAR.getMask();
    }

    public int ABNORMAL_STUN() {
        return AbnormalEffect.STUN.getMask();
    }

    public int ABNORMAL_SLEEP() {
        return AbnormalEffect.SLEEP.getMask();
    }

    public int ABNORMAL_MUTED() {
        return AbnormalEffect.MUTED.getMask();
    }

    public int ABNORMAL_ROOT() {
        return AbnormalEffect.ROOT.getMask();
    }

    public int ABNORMAL_HOLD_1() {
        return AbnormalEffect.HOLD_1.getMask();
    }

    public int ABNORMAL_HOLD_2() {
        return AbnormalEffect.HOLD_2.getMask();
    }

    public int ABNORMAL_UNKNOWN_13() {
        return AbnormalEffect.UNKNOWN_13.getMask();
    }

    public int ABNORMAL_BIG_HEAD() {
        return AbnormalEffect.BIG_HEAD.getMask();
    }

    public int ABNORMAL_FLAME() {
        return AbnormalEffect.FLAME.getMask();
    }

    public int ABNORMAL_UNKNOWN_16() {
        return AbnormalEffect.UNKNOWN_16.getMask();
    }

    public int ABNORMAL_GROW() {
        return AbnormalEffect.GROW.getMask();
    }

    public int ABNORMAL_FLOATING_ROOT() {
        return AbnormalEffect.FLOATING_ROOT.getMask();
    }

    public int ABNORMAL_DANCE_STUNNED() {
        return AbnormalEffect.DANCE_STUNNED.getMask();
    }

    public int ABNORMAL_FIREROOT_STUN() {
        return AbnormalEffect.FIREROOT_STUN.getMask();
    }

    public int ABNORMAL_STEALTH() {
        return AbnormalEffect.STEALTH.getMask();
    }

    public int ABNORMAL_IMPRISIONING_1() {
        return AbnormalEffect.IMPRISIONING_1.getMask();
    }

    public int ABNORMAL_IMPRISIONING_2() {
        return AbnormalEffect.IMPRISIONING_2.getMask();
    }

    public int ABNORMAL_MAGIC_CIRCLE() {
        return AbnormalEffect.MAGIC_CIRCLE.getMask();
    }

    public int ABNORMAL_ICE2() {
        return AbnormalEffect.ICE2.getMask();
    }

    public int ABNORMAL_EARTHQUAKE() {
        return AbnormalEffect.EARTHQUAKE.getMask();
    }

    public int ABNORMAL_UNKNOWN_27() {
        return AbnormalEffect.UNKNOWN_27.getMask();
    }

    public int ABNORMAL_INVULNERABLE() {
        return AbnormalEffect.INVULNERABLE.getMask();
    }

    public int ABNORMAL_VITALITY() {
        return AbnormalEffect.VITALITY.getMask();
    }

    public int ABNORMAL_REAL_TARGET() {
        return AbnormalEffect.REAL_TARGET.getMask();
    }

    public int ABNORMAL_DEATH_MARK() {
        return AbnormalEffect.DEATH_MARK.getMask();
    }

    public int ABNORMAL_SKULL_FEAR() {
        return AbnormalEffect.SKULL_FEAR.getMask();
    }

    public int ABNORMAL_S_INVINCIBLE() {
        return AbnormalEffect.S_INVINCIBLE.getMask();
    }

    public int ABNORMAL_S_AIR_STUN() {
        return AbnormalEffect.S_AIR_STUN.getMask();
    }

    public int ABNORMAL_S_AIR_ROOT() {
        return AbnormalEffect.S_AIR_ROOT.getMask();
    }

    public int ABNORMAL_S_BAGUETTE_SWORD() {
        return AbnormalEffect.S_BAGUETTE_SWORD.getMask();
    }

    public int ABNORMAL_S_YELLOW_AFFRO() {
        return AbnormalEffect.S_YELLOW_AFFRO.getMask();
    }

    public int ABNORMAL_S_PINK_AFFRO() {
        return AbnormalEffect.S_PINK_AFFRO.getMask();
    }

    public int ABNORMAL_S_BLACK_AFFRO() {
        return AbnormalEffect.S_BLACK_AFFRO.getMask();
    }

    public int ABNORMAL_S_UNKNOWN8() {
        return AbnormalEffect.S_UNKNOWN8.getMask();
    }

    public int ABNORMAL_S_STIGMA_SHILIEN() {
        return AbnormalEffect.S_STIGMA_SHILIEN.getMask();
    }

    public int ABNORMAL_S_STAKATOROOT() {
        return AbnormalEffect.S_STAKATOROOT.getMask();
    }

    public int ABNORMAL_S_FREEZING() {
        return AbnormalEffect.S_FREEZING.getMask();
    }

    public int ABNORMAL_S_VESPER() {
        return AbnormalEffect.S_VESPER_S.getMask();
    }

    public int ABNORMAL_E_AFRO_1() {
        return AbnormalEffect.E_AFRO_1.getMask();
    }

    public int ABNORMAL_E_AFRO_2() {
        return AbnormalEffect.E_AFRO_2.getMask();
    }

    public int ABNORMAL_E_AFRO_3() {
        return AbnormalEffect.E_AFRO_3.getMask();
    }

    public int ABNORMAL_E_EVASWRATH() {
        return AbnormalEffect.E_EVASWRATH.getMask();
    }

    public int ABNORMAL_E_HEADPHONE() {
        return AbnormalEffect.E_HEADPHONE.getMask();
    }

    public int ABNORMAL_E_VESPER_1() {
        return AbnormalEffect.E_VESPER_1.getMask();
    }

    public int ABNORMAL_E_VESPER_2() {
        return AbnormalEffect.E_VESPER_2.getMask();
    }

    public int ABNORMAL_E_VESPER_3() {
        return AbnormalEffect.E_VESPER_3.getMask();
    }

    public static final Values getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final Values _instance = new Values();

        private SingletonHolder() {
        }
    }

}

