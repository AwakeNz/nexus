/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.callback.CallbackManager
 *  cz.nxs.interf.delegate.CharacterData
 *  cz.nxs.interf.delegate.InstanceData
 *  cz.nxs.interf.delegate.NpcData
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main.events;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventSpawn;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.base.PvPEventPlayerData;
import cz.nxs.events.engine.base.SpawnType;
import cz.nxs.events.engine.base.description.EventDescription;
import cz.nxs.events.engine.base.description.EventDescriptionSystem;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.main.events.Domination;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.callback.CallbackManager;
import cz.nxs.interf.delegate.CharacterData;
import cz.nxs.interf.delegate.InstanceData;
import cz.nxs.interf.delegate.NpcData;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.util.FastMap;

public class MassDomination
extends Domination {
    private int _zonesCount;
    private int _zonesToOwn;
    private int _holdZonesFor;

    public MassDomination(EventType type, MainEventManager manager) {
        super(type, manager);
    }

    @Override
    public void loadConfigs() {
        super.loadConfigs();
        this.addConfig(new ConfigModel("countOfZones", "2", "Specifies how many zones will be in the event. In order to get score, one team must own all zones.", ConfigModel.InputType.Enum).addEnumOptions(new String[]{"2", "3", "4", "5"}));
        this.addConfig(new ConfigModel("zonesToOwnToScore", "2", "Count of zones the team needs to own in order to score. Obviously must be lower or equal to <font color=LEVEL>countOfZones</font>."));
        this.removeConfig("holdZoneFor");
        this.removeConfig("scoreForCapturingZone");
        this.removeConfig("percentMajorityToScore");
        this.addConfig(new ConfigModel("holdZonesFor", "10", "In seconds. The team needs to own <font color=LEVEL>zonesToOwnToScore</font> zones for this time to get <font color=LEVEL>scoreForCapturingZone</font> points. "));
        this.addConfig(new ConfigModel("scoreForCapturingZone", "1", "The ammount of points team gets each <font color=LEVEL>scoreCheckInterval</font> seconds if owns required zone(s)."));
        this.addConfig(new ConfigModel("percentMajorityToScore", "50", "In percent. In order to score a point, the team must have more players near at least <font color=LEVEL>zonesToOwnToScore</font> zones, than the other team(s). The ammount of players from the scoring team must be higher than ammount of players from the other team(s) by this percent value. Put 100 to make that all other team(s)' players in <font color=LEVEL>zoneRadius</font> must be dead to score; or put 0 to make that it will give score to the team that has more players and not care about any percent counting (eg. if team A has 15 players and team B has 16, it will simply reward team B)."));
    }

    @Override
    public void initEvent() {
        super.initEvent();
        this._zonesCount = this.getInt("countOfZones");
        this._zonesToOwn = this.getInt("zonesToOwnToScore");
        this._holdZonesFor = this.getInt("holdZonesFor");
    }

    @Override
    protected void spawnZone(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: spawning zones for instanceId " + instanceId);
        }
        this.clearMapHistory(-1, SpawnType.Zone);
        for (int i = 0; i < this._zonesCount; ++i) {
            EventSpawn sp = this.getSpawn(SpawnType.Zone, -1);
            NpcData zone = this.spawnNPC(sp.getLoc().getX(), sp.getLoc().getY(), sp.getLoc().getZ(), this._zoneNpcId, instanceId, "Zone " + (i + 1), "Domination event");
            this.getEventData(instanceId).addZone(zone, sp.getRadius());
        }
    }

    @Override
    protected void unspawnZone(int instanceId) {
        if (NexusLoader.detailedDebug) {
            this.print("Event: unspawning zones for instanceId " + instanceId);
        }
        for (NpcData zoneNpc : this.getEventData(instanceId)._zones) {
            if (zoneNpc == null) continue;
            zoneNpc.deleteMe();
        }
    }

    @Override
    protected void clockTick() throws Exception {
        ++this._tick;
        if (this._tick % this._zoneCheckInterval != 0) {
            return;
        }
        for (Domination.DominationEventInstance match : this._matches.values()) {
            reference i$;
            int instanceId = match.getInstance().getId();
            MultipleZoneData zoneData = this.getEventData(instanceId);
            FastMap ownedZones = new FastMap();
            FastMap playersNearZones = new FastMap(this._teamsCount);
            FastList playersWithEffects = new FastList();
            for (int i = 0; i < zoneData._zones.length; ++i) {
                FastMap players = new FastMap(this._teamsCount);
                NpcData zoneNpc = zoneData._zones[i];
                int radius = zoneData._radiuses[i];
                int zoneX = zoneNpc.getLoc().getX();
                int zoneY = zoneNpc.getLoc().getY();
                int zoneZ = zoneNpc.getLoc().getZ();
                for (PlayerEventInfo player : this.getPlayers(instanceId)) {
                    if (player.getDistanceSq(zoneX, zoneY, zoneZ) > (double)radius || !player.isVisible() || player.isDead()) continue;
                    if (!players.containsKey(player.getTeamId())) {
                        players.put(player.getTeamId(), new FastList());
                    }
                    if (!playersNearZones.containsKey(player.getTeamId())) {
                        playersNearZones.put(player.getTeamId(), new FastList());
                    }
                    ((List)players.get(player.getTeamId())).add(player);
                    ((List)playersNearZones.get(player.getTeamId())).add(player);
                }
                int highestCount = 0;
                int team = 0;
                boolean isThereMajorityTeam = true;
                for (Map.Entry teamData : players.entrySet()) {
                    if (((List)teamData.getValue()).size() > highestCount) {
                        highestCount = ((List)teamData.getValue()).size();
                        team = (Integer)teamData.getKey();
                        continue;
                    }
                    if (highestCount == 0 || ((List)teamData.getValue()).size() != highestCount) continue;
                    isThereMajorityTeam = false;
                    break;
                }
                if (isThereMajorityTeam && team != 0) {
                    boolean ownsZone = false;
                    if (this._percentMajorityToScore == 0) {
                        ownsZone = true;
                    } else if (this._percentMajorityToScore == 100) {
                        boolean teamWithMorePlayers = false;
                        for (Map.Entry teamData2 : players.entrySet()) {
                            if ((Integer)teamData2.getKey() == team || ((List)teamData2.getValue()).size() <= 0) continue;
                            teamWithMorePlayers = true;
                            break;
                        }
                        if (!teamWithMorePlayers) {
                            ownsZone = true;
                        }
                    } else {
                        int majorityTeamPlayers = ((List)players.get(team)).size();
                        boolean teamWithMorePlayers = false;
                        for (Map.Entry teamData3 : players.entrySet()) {
                            int percent;
                            double d;
                            if ((Integer)teamData3.getKey() == team || (percent = 100 - (int)((d = (double)((List)teamData3.getValue()).size() / (double)majorityTeamPlayers) * 100.0)) >= this._percentMajorityToScore) continue;
                            teamWithMorePlayers = true;
                            break;
                        }
                        if (!teamWithMorePlayers) {
                            ownsZone = true;
                        }
                    }
                    if (ownsZone) {
                        if (zoneData._holdingTeams[i] != team) {
                            this.announce(instanceId, LanguageEngine.getMsg("mDom_gainedZone", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getFullName(), i + 1));
                            zoneNpc.getNpc().setTitle(LanguageEngine.getMsg("dom_npcTitle_owner", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getTeamName()));
                            zoneNpc.getNpc().broadcastNpcInfo();
                            MultipleZoneData.access$300((MultipleZoneData)zoneData)[i] = team;
                            MultipleZoneData.access$400((MultipleZoneData)zoneData)[i] = 0;
                            this.setZoneEffects(team, zoneNpc);
                        } else {
                            int[] arrn = zoneData._holdingTimes;
                            int n = i;
                            arrn[n] = arrn[n] + this._zoneCheckInterval;
                        }
                        if (this.getBoolean("allowPlayerEffects") && this._teamsCount == 2) {
                            i$ = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)team)).getPlayers().iterator();
                            while (i$.hasNext()) {
                                PlayerEventInfo player2 = (PlayerEventInfo)i$.next();
                                if (player2.getDistanceSq(zoneX, zoneY, zoneZ) > (double)radius || !player2.isVisible() || player2.isDead()) continue;
                                playersWithEffects.add(player2);
                            }
                        }
                        if (!ownedZones.containsKey(team)) {
                            ownedZones.put(team, new FastList());
                        }
                        ((List)ownedZones.get(team)).add(zoneNpc);
                        continue;
                    }
                    if (zoneData._holdingTeams[i] != 0) {
                        this.announce(instanceId, LanguageEngine.getMsg("mDom_lostZone", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)zoneData._holdingTeams[i])).getFullName(), i + 1));
                        zoneNpc.getNpc().setTitle(LanguageEngine.getMsg("dom_npcTitle_noOwner"));
                        zoneNpc.getNpc().broadcastNpcInfo();
                        this.setZoneEffects(0, zoneNpc);
                    }
                    MultipleZoneData.access$400((MultipleZoneData)zoneData)[i] = 0;
                    MultipleZoneData.access$300((MultipleZoneData)zoneData)[i] = 0;
                    continue;
                }
                if (zoneData._holdingTeams[i] != 0) {
                    this.announce(instanceId, LanguageEngine.getMsg("mDom_lostZone", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)zoneData._holdingTeams[i])).getFullName(), i + 1));
                    zoneNpc.getNpc().setTitle(LanguageEngine.getMsg("dom_npcTitle_noOwner"));
                    zoneNpc.getNpc().broadcastNpcInfo();
                    this.setZoneEffects(0, zoneNpc);
                }
                MultipleZoneData.access$400((MultipleZoneData)zoneData)[i] = 0;
                MultipleZoneData.access$300((MultipleZoneData)zoneData)[i] = 0;
            }
            if (this.getBoolean("allowPlayerEffects") && this._teamsCount == 2) {
                for (PlayerEventInfo player : this.getPlayers(instanceId)) {
                    if (playersWithEffects.contains((Object)player)) {
                        player.startAbnormalEffect(player.getTeamId() == 1 ? 2097152 : 4);
                        continue;
                    }
                    player.stopAbnormalEffect(player.getTeamId() == 1 ? 2097152 : 4);
                }
            }
            boolean ownsAllZones = true;
            int teamWithMostZones = 0;
            int mostZones = 0;
            for (Map.Entry e : ownedZones.entrySet()) {
                if (((List)e.getValue()).size() > mostZones) {
                    teamWithMostZones = (Integer)e.getKey();
                    mostZones = ((List)e.getValue()).size();
                    continue;
                }
                if (((List)e.getValue()).size() == 0 || ((List)e.getValue()).size() != mostZones) continue;
                ownsAllZones = false;
                break;
            }
            if (ownsAllZones) {
                boolean bl = ownsAllZones = mostZones >= this._zonesToOwn;
            }
            if (ownsAllZones) {
                if (teamWithMostZones != zoneData._dominatingTeam) {
                    this.announce(instanceId, "++ " + LanguageEngine.getMsg("mDom_dominating", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)teamWithMostZones)).getFullName(), mostZones));
                    zoneData._dominatingTeam = teamWithMostZones;
                    zoneData._holdingAllZonesFor = 0;
                } else {
                    MultipleZoneData.access$612(zoneData, this._zoneCheckInterval);
                }
                if (zoneData._holdingAllZonesFor >= this._holdZonesFor) {
                    ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)teamWithMostZones)).raiseScore(this._scoreForCapturingZone);
                    for (PlayerEventInfo player : (List)playersNearZones.get(teamWithMostZones)) {
                        this.getPlayerData(player).raiseScore(this._scoreForCapturingZone);
                        this.setScoreStats(player, this.getPlayerData(player).getScore());
                        if (player.isTitleUpdated()) {
                            player.setTitle(this.getTitle(player), true);
                            player.broadcastTitleInfo();
                        }
                        CallbackManager.getInstance().playerScores(this.getEventType(), player, this._scoreForCapturingZone);
                    }
                    zoneData._holdingAllZonesFor = 0;
                    if (this._holdZonesFor <= 5) continue;
                    this.announce(instanceId, "*** " + LanguageEngine.getMsg("mDom_score", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)teamWithMostZones)).getTeamName(), mostZones));
                    if (!this.getBoolean("allowFireworkOnScore")) continue;
                    NpcData[] arr$ = zoneData._zones;
                    int len$ = arr$.length;
                    for (i$ = (reference)false ? 1 : 0; i$ < len$; ++i$) {
                        NpcData npc = arr$[i$];
                        npc.broadcastSkillUse((CharacterData)npc, (CharacterData)npc, 2024, 1);
                    }
                    continue;
                }
                int toHold = this._holdZonesFor - zoneData._holdingAllZonesFor;
                boolean announce = false;
                if (zoneData._holdingAllZonesFor == 0) {
                    announce = true;
                } else if (toHold >= 60 && toHold % 60 == 0) {
                    announce = true;
                } else {
                    switch (toHold) {
                        case 5: 
                        case 10: 
                        case 20: 
                        case 30: 
                        case 45: {
                            announce = true;
                        }
                    }
                }
                if (!announce) continue;
                boolean min = false;
                Object[] arrobject = new Object[3];
                arrobject[0] = toHold;
                arrobject[1] = min ? "minutes" : "seconds";
                arrobject[2] = ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)teamWithMostZones)).getFullName();
                this.announce(instanceId, "* " + LanguageEngine.getMsg("mDom_leftToScore", arrobject));
                continue;
            }
            if (zoneData._dominatingTeam != 0 && zoneData._holdingAllZonesFor > 0) {
                this.announce(instanceId, "-- " + LanguageEngine.getMsg("mDom_lostDomination", ((EventTeam)((FastMap)this._teams.get((Object)instanceId)).get((Object)zoneData._dominatingTeam)).getFullName()));
            }
            zoneData._dominatingTeam = 0;
            zoneData._holdingAllZonesFor = 0;
        }
    }

    @Override
    protected String addExtraEventInfoCb(int instance) {
        int owningTeam = this.getEventData(instance)._dominatingTeam;
        int max = this.getEventData(instance)._holdingTeams.length;
        int count = 0;
        for (int zone : this.getEventData(instance)._holdingTeams) {
            if (zone != owningTeam) continue;
            ++count;
        }
        String status = "<font color=ac9887>Zones dominated by:</font> <font color=" + EventManager.getInstance().getDarkColorForHtml(owningTeam) + ">" + EventManager.getInstance().getTeamName(owningTeam) + " team</font>" + (owningTeam > 0 ? new StringBuilder().append(" <font color=7f7f7f>(").append(count).append("/").append(max).append(" zones)</font>").toString() : "");
        return "<table width=510 bgcolor=3E3E3E><tr><td width=510 align=center>" + status + "</td></tr></table>";
    }

    @Override
    public String getHtmlDescription() {
        EventDescription desc = EventDescriptionSystem.getInstance().getDescription(this.getEventType());
        if (desc != null) {
            this._htmlDescription = desc.getDescription(this.getConfigs());
        } else {
            this._htmlDescription = "" + this.getInt("teamsCount") + " teams fighting against each other. ";
            this._htmlDescription = this._htmlDescription + "There are " + this.getInt("countOfZones") + " zones, each represented by an NPC. ";
            this._htmlDescription = this._htmlDescription + "In order to gain a score, your team must own at least " + this.getInt("zonesToOwnToScore") + " zones. ";
            this._htmlDescription = this._htmlDescription + "To own a zone, your team must get close to each of these zones and kill all other enemies standing near the zone too. ";
            if (this.getInt("killsForReward") > 0) {
                this._htmlDescription = this._htmlDescription + "At least " + this.getInt("killsForReward") + " kill(s) is required to receive a reward. ";
            }
            if (this.getInt("scoreForReward") > 0) {
                this._htmlDescription = this._htmlDescription + "At least " + this.getInt("scoreForReward") + " score (obtained when your team owns the zone and you stand near it) is required to receive a reward. ";
            }
            this._htmlDescription = this.getBoolean("waweRespawn") ? this._htmlDescription + "Dead players are resurrected by an advanced wawe-spawn engine each " + this.getInt("resDelay") + " seconds. " : this._htmlDescription + "If you die, you will get resurrected in " + this.getInt("resDelay") + " seconds. ";
            if (this.getBoolean("createParties")) {
                this._htmlDescription = this._htmlDescription + "The event automatically creates parties on start.";
            }
        }
        return this._htmlDescription;
    }

    @Override
    public String getMissingSpawns(EventMap map) {
        TextBuilder tb = new TextBuilder();
        for (int i = 0; i < this.getTeamsCount(); ++i) {
            if (map.checkForSpawns(SpawnType.Regular, i + 1, 1)) continue;
            tb.append(this.addMissingSpawn(SpawnType.Regular, i + 1, 1));
        }
        int count = this.getInt("countOfZones");
        if (!map.checkForSpawns(SpawnType.Zone, -1, count)) {
            tb.append(this.addMissingSpawn(SpawnType.Zone, 0, count));
        }
        return tb.toString();
    }

    @Override
    protected Domination.ZoneData createEventData(int instance) {
        return new MultipleZoneData(instance, this._zonesCount);
    }

    @Override
    protected MultipleZoneData getEventData(int instance) {
        return (MultipleZoneData)((Domination.DominationEventInstance)this._matches.get((Object)Integer.valueOf((int)instance)))._zoneData;
    }

    protected class MultipleZoneData
    extends Domination.ZoneData {
        private int _order;
        private NpcData[] _zones;
        private int[] _radiuses;
        private int[] _holdingTeams;
        private int[] _holdingTimes;
        private int _holdingAllZonesFor;
        private int _dominatingTeam;

        protected MultipleZoneData(int instance, int zonesCount) {
            super(instance);
            this._zones = new NpcData[zonesCount];
            this._radiuses = new int[zonesCount];
            this._holdingTeams = new int[zonesCount];
            this._holdingTimes = new int[zonesCount];
            this._dominatingTeam = 0;
            this._holdingAllZonesFor = 0;
            this._order = 0;
        }

        @Override
        protected void addZone(NpcData zone, int radius) {
            if (this._order < MassDomination.this._zonesCount) {
                this._zones[this._order] = zone;
                this._radiuses[this._order] = radius > 0 ? (int)Math.pow(radius, 2.0) : MassDomination.this._zoneRadius;
                this._holdingTeams[this._order] = 0;
                this._holdingTimes[this._order] = 0;
                ++this._order;
            } else {
                NexusLoader.debug((String)("too many zones for MultipleZoneData (" + this._order + "; " + MassDomination.this._zonesCount + ")"));
            }
        }

        static /* synthetic */ int access$612(MultipleZoneData x0, int x1) {
            return x0._holdingAllZonesFor+=x1;
        }
    }

}

