/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.L2ShortCut
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.model.L2ShortCut;

public class ShortCutData {
    private L2ShortCut _shortcut;

    public ShortCutData(int slotId, int pageId, int shortcutType, int shortcutId, int shortcutLevel, int characterType) {
        this._shortcut = new L2ShortCut(slotId, pageId, shortcutType, shortcutId, shortcutLevel, characterType);
    }

    public int getId() {
        return this._shortcut.getId();
    }

    public int getLevel() {
        return this._shortcut.getLevel();
    }

    public int getPage() {
        return this._shortcut.getPage();
    }

    public int getSlot() {
        return this._shortcut.getSlot();
    }

    public int getType() {
        return this._shortcut.getType();
    }

    public int getCharacterType() {
        return this._shortcut.getCharacterType();
    }
}

