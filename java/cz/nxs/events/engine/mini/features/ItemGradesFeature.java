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
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;

public class ItemGradesFeature
extends AbstractFeature {
    private int[] allowedGrades;

    public ItemGradesFeature(EventType event, PlayerEventInfo gm, String parametersString) {
        super(event);
        this.addConfig("GradesAviable", "Write the letters of all allowed item grades here. Separate by SPACE. Eg. <font color=LEVEL>a s s80</font>.", 1);
        if (parametersString == null) {
            parametersString = "no d c b a s s80 s84";
        }
        this._params = parametersString;
        this.initValues();
    }

    @Override
    protected void initValues() {
        String[] params = this.splitParams(this._params);
        try {
            String[] splitted = params[0].split(" ");
            this.allowedGrades = new int[splitted.length];
            for (int i = 0; i < splitted.length; ++i) {
                this.allowedGrades[i] = CallBack.getInstance().getOut().getGradeFromFirstLetter(splitted[i]);
            }
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
    }

    public int[] getGrades() {
        return this.allowedGrades;
    }

    public boolean checkItem(PlayerEventInfo player, ItemData item) {
        int type = item.getCrystalType();
        boolean allowed = false;
        if (item.isArmor() || item.isWeapon()) {
            for (int grade : this.allowedGrades) {
                if (type != grade) continue;
                allowed = true;
            }
        } else {
            allowed = true;
        }
        return allowed;
    }

    @Override
    public boolean checkPlayer(PlayerEventInfo player) {
        boolean canJoin = true;
        for (ItemData item : player.getItems()) {
            if (this.checkItem(player, item)) continue;
            canJoin = false;
            player.sendMessage("(G)Please put item " + item.getItemName() + " to your warehouse before participating. It is not allowed for this event.");
        }
        return canJoin;
    }

    @Override
    public EventMode.FeatureType getType() {
        return EventMode.FeatureType.ItemGrades;
    }
}

