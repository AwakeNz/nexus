/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  com.l2jserver.gameserver.datatables.NpcTable
 *  com.l2jserver.gameserver.model.L2Spawn
 *  com.l2jserver.gameserver.model.actor.L2Npc
 *  com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate
 *  cz.nxs.l2j.delegate.INpcTemplateData
 */
package cz.nxs.interf.delegate;

import com.l2jserver.gameserver.datatables.NpcTable;
import com.l2jserver.gameserver.model.L2Spawn;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.l2j.delegate.INpcTemplateData;

public class NpcTemplateData
implements INpcTemplateData {
    private L2NpcTemplate _template;
    private String _spawnName = null;
    private String _spawnTitle = null;

    public NpcTemplateData(int id) {
        this._template = NpcTable.getInstance().getTemplate(id);
    }

    public void setSpawnName(String name) {
        this._spawnName = name;
    }

    public void setSpawnTitle(String title) {
        this._spawnTitle = title;
    }

    public boolean exists() {
        return this._template != null;
    }

    public NpcData doSpawn(int x, int y, int z, int ammount, int instanceId) {
        return this.doSpawn(x, y, z, ammount, 0, instanceId);
    }

    public NpcData doSpawn(int x, int y, int z, int ammount, int heading, int instanceId) {
        return this.doSpawn(x, y, z, ammount, heading, 0, instanceId);
    }

    public NpcData doSpawn(int x, int y, int z, int ammount, int heading, int respawn, int instanceId) {
        if (this._template == null) {
            return null;
        }
        try {
            L2Spawn spawn = new L2Spawn(this._template);
            spawn.setLocx(x);
            spawn.setLocy(y);
            spawn.setLocz(z);
            spawn.setAmount(1);
            spawn.setHeading(heading);
            spawn.setRespawnDelay(respawn);
            spawn.setInstanceId(instanceId);
            L2Npc npc = spawn.doSpawn();
            NpcData npcData = new NpcData(npc);
            boolean update = false;
            if (this._spawnName != null) {
                npc.setName(this._spawnName);
                update = true;
            }
            if (this._spawnTitle != null) {
                npc.setTitle(this._spawnTitle);
                update = true;
            }
            if (update) {
                npcData.broadcastNpcInfo();
            }
            return npcData;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

