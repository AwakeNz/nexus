/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.datatables.SkillTable
 *  com.l2jserver.gameserver.model.L2Object
 *  com.l2jserver.gameserver.model.actor.L2Character
 *  com.l2jserver.gameserver.model.actor.L2Npc
 *  com.l2jserver.gameserver.model.actor.instance.L2PcInstance
 *  com.l2jserver.gameserver.model.actor.knownlist.CharKnownList
 *  com.l2jserver.gameserver.model.skills.L2Skill
 *  com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket
 *  com.l2jserver.gameserver.network.serverpackets.MagicSkillUse
 *  cz.nxs.l2j.delegate.INpcData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.datatables.SkillTable;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.knownlist.CharKnownList;
import com.l2jserver.gameserver.model.skills.L2Skill;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.ObjectData;
import cz.nxs.l2j.delegate.INpcData;
import java.util.Collection;
import java.util.Map;

public class NpcData
extends CharacterData
implements INpcData {
    private int _team;
    private boolean deleted = false;

    public NpcData(L2Npc npc) {
        super((L2Character)npc);
    }

    public void deleteMe() {
        if (!this.deleted) {
            ((L2Npc)this._owner).deleteMe();
        }
        this.deleted = true;
    }

    public ObjectData getObjectData() {
        return new ObjectData((L2Object)this._owner);
    }

    public void setName(String name) {
        this._owner.setName(name);
    }

    public void setTitle(String t) {
        this._owner.setTitle(t);
    }

    public int getNpcId() {
        return ((L2Npc)this._owner).getNpcId();
    }

    public void setEventTeam(int team) {
        this._team = team;
    }

    public int getEventTeam() {
        return this._team;
    }

    public void broadcastNpcInfo() {
        Collection plrs = this._owner.getKnownList().getKnownPlayers().values();
        for (L2PcInstance player : plrs) {
            ((L2Npc)this._owner).sendInfo(player);
        }
    }

    public void broadcastSkillUse(CharacterData owner, CharacterData target, int skillId, int level) {
        L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
        if (skill != null) {
            this.getOwner().broadcastPacket((L2GameServerPacket)new MagicSkillUse(owner.getOwner(), target.getOwner(), skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
        }
    }
}

