/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.model.L2Object
 *  com.l2jserver.gameserver.model.actor.L2Character
 *  com.l2jserver.gameserver.model.actor.L2Playable
 *  com.l2jserver.gameserver.model.actor.instance.L2DoorInstance
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  com.l2jserver.gameserver.model.skills.L2Skill
 *  com.l2jserver.gameserver.network.serverpackets.CreatureSay
 *  com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket
 *  cz.nxs.events.engine.base.Loc
 *  cz.nxs.l2j.delegate.ICharacterData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.network.serverpackets.CreatureSay;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.DoorData;
import cz.nxs.interf.delegate.ObjectData;
import cz.nxs.l2j.delegate.ICharacterData;

public class CharacterData
extends ObjectData
implements ICharacterData {
    protected L2Character _owner;

    public CharacterData(L2Character cha) {
        super((L2Object)cha);
        this._owner = cha;
    }

    public L2Character getOwner() {
        return this._owner;
    }

    public double getPlanDistanceSq(int targetX, int targetY) {
        return this._owner.getPlanDistanceSq(targetX, targetY);
    }

    public Loc getLoc() {
        return new Loc(this._owner.getX(), this._owner.getY(), this._owner.getZ(), this._owner.getHeading());
    }

    @Override
    public int getObjectId() {
        return this._owner.getObjectId();
    }

    public boolean isDoor() {
        return this._owner instanceof L2DoorInstance;
    }

    public DoorData getDoorData() {
        return this.isDoor() ? new DoorData((L2DoorInstance)this._owner) : null;
    }

    public void startAbnormalEffect(int mask) {
        this._owner.startAbnormalEffect(mask);
    }

    public void stopAbnormalEffect(int mask) {
        this._owner.stopAbnormalEffect(mask);
    }

    public PlayerEventInfo getEventInfo() {
        if (this._owner instanceof L2Playable) {
            return ((L2Playable)this._owner).getActingPlayer().getEventInfo();
        }
        return null;
    }

    public String getName() {
        return this._owner.getName();
    }

    public void creatureSay(int channel, String charName, String text) {
        this._owner.broadcastPacket((L2GameServerPacket)new CreatureSay(this._owner.getObjectId(), channel, charName, text));
    }

    public void doDie(CharacterData killer) {
        this._owner.reduceCurrentHp(this._owner.getCurrentHp() * 2.0, killer.getOwner(), null);
    }

    public boolean isDead() {
        return this._owner.isDead();
    }
}

