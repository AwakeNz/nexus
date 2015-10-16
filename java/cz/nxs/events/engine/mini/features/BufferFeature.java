/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 */
package cz.nxs.events.engine.mini.features;

import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.interf.PlayerEventInfo;

public class BufferFeature
extends AbstractFeature {
    private boolean autoApplySchemeBuffs = true;
    private boolean spawnNpcBuffer = false;
    private int customNpcBuffer = 0;
    private int[] autoBuffsFighterIds = null;
    private int[] autoBuffsFighterLevels = null;
    private int[] autoBuffsMageIds = null;
    private int[] autoBuffsMageLevels = null;

    public BufferFeature(EventType event, PlayerEventInfo gm, String parametersString) {
        super(event);
        this.addConfig("ApplyEventBuffs", "If 'true', all players will be rebuffed on start of event/round by their specified scheme. Doesn't work if the auto scheme buffer is disabled from Events.xml.", 1);
        this.addConfig("SpawnNpcBuffer", "If 'true', then the event will spawn NPC Buffer to each spawn of type Buffer at start of the event/round and the Buffer disappears at the end of wait time.", 1);
        this.addConfig("CustomBufferId", "You can specify the ID of buffer (or another NPC which will be aviable near players during the wait-time) for this mode. Put '0' to disable.", 1);
        this.addConfig("AutoBuffIdsFighter", "Fighter classes will be buffed with those buffs at start of event/round. Format as 'BUFF_ID-Level'. Separate IDs by SPACE, Eg. <font color=LEVEL>312-1 256-3</font>. Put <font color=LEVEL>0-0</font> to disable this config.", 2);
        this.addConfig("AutoBuffIdsMage", "Mage classes will be buffed with those buffs at start of event/round.  Format as 'BUFF_ID-Level'. Separate IDs by SPACE, Eg. <font color=LEVEL>312-1 256-3</font>. Put <font color=LEVEL>0-0</font> to disable this config.", 2);
        if (parametersString == null) {
            parametersString = "true,true,0,0-0,0-0";
        }
        this._params = parametersString;
        this.initValues();
    }

    @Override
    protected void initValues() {
        String[] params = this.splitParams(this._params);
        try {
            this.autoApplySchemeBuffs = Boolean.parseBoolean(params[0]);
            this.spawnNpcBuffer = Boolean.parseBoolean(params[1]);
            this.customNpcBuffer = Integer.parseInt(params[2]);
            String[] splitted = params[3].split(" ");
            this.autoBuffsFighterIds = new int[splitted.length];
            this.autoBuffsFighterLevels = new int[splitted.length];
            int i = 0;
            for (String s2 : splitted) {
                this.autoBuffsFighterIds[i] = Integer.parseInt(s2.split("-")[0]);
                this.autoBuffsFighterLevels[i] = Integer.parseInt(s2.split("-")[1]);
                ++i;
            }
            splitted = params[4].split(" ");
            this.autoBuffsMageIds = new int[splitted.length];
            this.autoBuffsMageLevels = new int[splitted.length];
            i = 0;
            for (String s2 : splitted) {
                this.autoBuffsMageIds[i] = Integer.parseInt(s2.split("-")[0]);
                this.autoBuffsMageLevels[i] = Integer.parseInt(s2.split("-")[1]);
                ++i;
            }
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public void buffPlayer(PlayerEventInfo player) {
        if (player.isMageClass()) {
            if (this.autoBuffsMageIds[0] == 0) {
                return;
            }
            for (int i = 0; i < this.autoBuffsMageIds.length; ++i) {
                player.getSkillEffects(this.autoBuffsMageIds[i], this.autoBuffsMageLevels[i]);
            }
            return;
        }
        if (this.autoBuffsFighterIds[0] == 0) {
            return;
        }
        for (int i = 0; i < this.autoBuffsFighterIds.length; ++i) {
            player.getSkillEffects(this.autoBuffsFighterIds[i], this.autoBuffsFighterLevels[i]);
        }
    }

    public boolean canRebuff() {
        return this.autoApplySchemeBuffs;
    }

    public boolean canSpawnBuffer() {
        return this.spawnNpcBuffer;
    }

    public int getCustomNpcBufferId() {
        return this.customNpcBuffer;
    }

    @Override
    public boolean checkPlayer(PlayerEventInfo player) {
        return true;
    }

    @Override
    public EventMode.FeatureType getType() {
        return EventMode.FeatureType.Buffer;
    }
}

