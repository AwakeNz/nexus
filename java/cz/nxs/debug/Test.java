/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastList
 *  javolution.util.FastList$Node
 *  javolution.util.FastMap
 */
package cz.nxs.debug;

import cz.nxs.events.engine.main.base.MainEventInstanceType;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.ClassType;
import cz.nxs.l2j.INexusOut;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javolution.util.FastList;
import javolution.util.FastMap;

public class Test {
    private static int next = 0;
    public static FastMap<MainEventInstanceType, FastList<String>> _tempPlayers;
    public static List<EventTeam> _teams;
    public static int _vipsCount;
    public static int _chooseFromTopPercent;
    public static Comparator<PlayerEventInfo> compareByLevels;
    public static Comparator<PlayerEventInfo> compareByPvps;

    public static void main(String[] args) {
    }

    protected static void partyPlayers() {
    }

    protected static void partyPlayers(List<PlayerEventInfo> party) {
        System.out.println("... partying ");
        for (PlayerEventInfo player : party) {
            System.out.println(player.getPlayersName() + ", type = " + player.getClassType().toString());
        }
    }

    protected static void selectVips(int instanceId, int count) {
    }

    protected static void dividePlayersToTeams(int instanceId, FastList<PlayerEventInfo> players, int teamsCount) {
        int team1 = 0;
        int team2 = 0;
        int team3 = 0;
        int team4 = 0;
        int healers1 = 0;
        int healers2 = 0;
        int healers3 = 0;
        int healers4 = 0;
        int mages1 = 0;
        int mages2 = 0;
        int mages3 = 0;
        int mages4 = 0;
        int fighters1 = 0;
        int fighters2 = 0;
        int fighters3 = 0;
        int fighters4 = 0;
        int divided = 0;
        PlayerEventInfo player = null;
        String type = "LevelOnly";
        Collections.sort(players, compareByLevels);
        if (type.startsWith("PvPs")) {
            Collections.sort(players, compareByPvps);
        }
        FastMap sortedPlayers = new FastMap();
        for (ClassType classType : ClassType.values()) {
            sortedPlayers.put((Object)classType, (Object)new FastList());
        }
        for (PlayerEventInfo pi : players) {
            ((FastList)sortedPlayers.get((Object)pi.getClassType())).add((Object)pi);
        }
        for (Map.Entry e : sortedPlayers.entrySet()) {
            System.out.println(((ClassType)e.getKey()).toString() + " has " + ((FastList)e.getValue()).size() + " players");
        }
        int teamId = 0;
        for (int healersCount = ((FastList)sortedPlayers.get((Object)ClassType.Priest)).size(); healersCount > 0; --healersCount) {
            ++divided;
            switch (++teamId) {
                case 1: {
                    ++team1;
                    ++healers1;
                    break;
                }
                case 2: {
                    ++team2;
                    ++healers2;
                    break;
                }
                case 3: {
                    ++team3;
                    ++healers3;
                    break;
                }
                case 4: {
                    ++team4;
                    ++healers4;
                }
            }
            player = (PlayerEventInfo)((FastList)sortedPlayers.get((Object)ClassType.Priest)).head().getNext().getValue();
            System.out.println(player.getClassType().toString() + " " + player.getPlayersName() + " -  goes to team " + teamId);
            ((FastList)sortedPlayers.get((Object)ClassType.Priest)).remove((Object)player);
            if (teamId < teamsCount) continue;
            teamId = 0;
        }
        teamId = 0;
        for (Map.Entry e2 : sortedPlayers.entrySet()) {
            for (PlayerEventInfo pi2 : (FastList)e2.getValue()) {
                teamId = team1 < team2 ? 1 : (team2 < team1 ? 2 : CallBack.getInstance().getOut().random(1, 2));
                ++divided;
                switch (teamId) {
                    case 1: {
                        ++team1;
                        if (pi2.getClassType() == ClassType.Fighter) {
                            ++fighters1;
                            break;
                        }
                        ++mages1;
                        break;
                    }
                    case 2: {
                        ++team2;
                        if (pi2.getClassType() == ClassType.Fighter) {
                            ++fighters2;
                            break;
                        }
                        ++mages2;
                        break;
                    }
                    case 3: {
                        ++team3;
                        if (pi2.getClassType() == ClassType.Fighter) {
                            ++fighters3;
                            break;
                        }
                        ++mages3;
                        break;
                    }
                    case 4: {
                        ++team4;
                        if (pi2.getClassType() == ClassType.Fighter) {
                            ++fighters4;
                            break;
                        }
                        ++mages4;
                    }
                }
                System.out.println(pi2.getClassType().toString() + " " + pi2.getPlayersName() + " -  goes to team " + teamId);
                if (teamId != teamsCount) continue;
                teamId = 0;
            }
        }
        System.out.println("divided: " + divided);
        System.out.println("team1: " + team1 + " ::: " + healers1 + " healers, " + mages1 + " mages, " + fighters1 + " fighters.");
        System.out.println("team2: " + team2 + " ::: " + healers2 + " healers, " + mages2 + " mages, " + fighters2 + " fighters.");
        System.out.println("team3: " + team3 + " ::: " + healers3 + " healers, " + mages3 + " mages, " + fighters3 + " fighters.");
        System.out.println("team4: " + team4 + " ::: " + healers4 + " healers, " + mages4 + " mages, " + fighters4 + " fighters.");
    }

    protected static void reorganizeInstances() {
        FastList sameStrenghtInstances = new FastList();
        for (int currentStrenght = 1; currentStrenght <= 10; ++currentStrenght) {
            for (Map.Entry e : _tempPlayers.entrySet()) {
                if (Test.isFull((MainEventInstanceType)e.getKey()) || ((MainEventInstanceType)e.getKey()).getTempRate() != currentStrenght) continue;
                sameStrenghtInstances.add(e.getKey());
            }
            Collections.sort(sameStrenghtInstances, new Comparator<MainEventInstanceType>(){

                @Override
                public int compare(MainEventInstanceType i1, MainEventInstanceType i2) {
                    int neededPlayers2;
                    int neededPlayers1 = i1.getMinPlayers() - ((FastList)Test._tempPlayers.get((Object)i1)).size();
                    return neededPlayers1 == (neededPlayers2 = i2.getMinPlayers() - ((FastList)Test._tempPlayers.get((Object)i2)).size()) ? 0 : (neededPlayers1 < neededPlayers2 ? -1 : 1);
                }
            });
            Test.reorganize(sameStrenghtInstances);
            sameStrenghtInstances.clear();
        }
    }

    protected static void init() {
        int i;
        int id = 0;
        ++id;
        FastList players = new FastList();
        for (i = 0; i < 4; ++i) {
            players.add((Object)Test.createPlayer(id));
        }
        _tempPlayers.put((Object)new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 15, 1), (Object)players);
        ++id;
        players = new FastList();
        for (i = 0; i < 6; ++i) {
            players.add((Object)Test.createPlayer(id));
        }
        _tempPlayers.put((Object)new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 16, 2), (Object)players);
    }

    protected static String createPlayer(int id) {
        return "Player " + (id + 1);
    }

    protected static void reorganize(List<MainEventInstanceType> instances) {
        block0 : for (MainEventInstanceType instance : instances) {
            System.out.println("======================");
            System.out.println("*** " + instance.getName() + " processing");
            if (Test.hasEnoughtPlayers(instance)) {
                System.out.println("" + instance.getName() + " already full");
                instances.remove(instance);
                continue;
            }
            int count = ((FastList)_tempPlayers.get((Object)instance)).size();
            int toMove = instance.getMinPlayers() - count;
            System.out.println("" + instance.getName() + " not full - players (" + count + "), toMove (" + toMove + ")");
            for (MainEventInstanceType possibleInstance : instances) {
                if (possibleInstance == instance) continue;
                int moved = Test.movePlayers(instance, possibleInstance, toMove);
                System.out.println("- from '" + possibleInstance.getName() + "' moved " + moved + ", still need to move " + (toMove-=moved));
                if (toMove == 0) {
                    System.out.println("- '" + instance.getName() + "' ready to run!");
                    instances.remove(instance);
                    continue block0;
                }
                if (toMove <= 0) continue;
            }
        }
        if (!instances.isEmpty()) {
            int minPlayers = Integer.MAX_VALUE;
            MainEventInstanceType inst = null;
            for (MainEventInstanceType instance22 : instances) {
                if (instance22.getMinPlayers() >= minPlayers) continue;
                minPlayers = instance22.getMinPlayers();
                inst = instance22;
            }
            System.out.println("*** - moving all players to instance " + inst.getName());
            for (MainEventInstanceType instance22 : instances) {
                if (instance22 == inst) continue;
                Test.movePlayers(inst, instance22, -1);
            }
            System.out.println("*** Done, instance " + inst.getName() + " has " + ((FastList)_tempPlayers.get((Object)inst)).size() + " players.");
        }
    }

    protected static int movePlayers(MainEventInstanceType target, MainEventInstanceType source, int count) {
        if (count == 0) {
            return 0;
        }
        int moved = 0;
        for (String player : (FastList)_tempPlayers.get((Object)source)) {
            ((FastList)_tempPlayers.get((Object)target)).add((Object)player);
            ((FastList)_tempPlayers.get((Object)source)).remove((Object)player);
            if (count == -1 || ++moved < count) continue;
            break;
        }
        return moved;
    }

    protected static boolean hasEnoughtPlayers(MainEventInstanceType instance) {
        return ((FastList)_tempPlayers.get((Object)instance)).size() >= instance.getMinPlayers();
    }

    protected static boolean isFull(MainEventInstanceType instance) {
        return ((FastList)_tempPlayers.get((Object)instance)).size() >= instance.getMaxPlayers();
    }

    protected static void dividePlayers() {
        int strenght;
        int toMove;
        int playersCount;
        int sumPlayers;
        System.out.println("********* Reorganize Players ************");
        Test.reorganizeInstances();
        System.out.println("********* Divide Players ************");
        FastList notEnoughtPlayersInstances = new FastList();
        for (Map.Entry e : _tempPlayers.entrySet()) {
            if (((FastList)e.getValue()).size() < ((MainEventInstanceType)e.getKey()).getMinPlayers()) {
                System.out.println("/ adding instance " + ((MainEventInstanceType)e.getKey()).getName() + " [" + ((MainEventInstanceType)e.getKey()).getTempRate() + "] to notEnoughtPlayersInstance");
                notEnoughtPlayersInstances.add(e.getKey());
                continue;
            }
            System.out.println("/ instance " + ((MainEventInstanceType)e.getKey()).getName() + " [" + ((MainEventInstanceType)e.getKey()).getTempRate() + "] has enought players");
        }
        FastList fixed = new FastList();
        for (MainEventInstanceType currentInstance2 : notEnoughtPlayersInstances) {
            if (currentInstance2 == null) continue;
            if (fixed.contains(currentInstance2)) continue;
            System.out.println("================== " + currentInstance2.getName());
            strenght = currentInstance2.getTempRate();
            playersCount = ((FastList)_tempPlayers.get((Object)currentInstance2)).size();
            boolean joinStrongerInstIfNeeded = true;
            int maxDiff = 2;
            System.out.println("*** current instance " + currentInstance2.getName() + "[" + currentInstance2.getTempRate() + "] - playersCount (" + playersCount + "), strenght (" + strenght + ")");
            for (MainEventInstanceType possibleInstance : notEnoughtPlayersInstances) {
                if (possibleInstance == null || fixed.contains(possibleInstance)) continue;
                if (possibleInstance == currentInstance2) continue;
                playersCount = ((FastList)_tempPlayers.get((Object)currentInstance2)).size();
                if (possibleInstance.getTempRate() == strenght) {
                    if (((FastList)_tempPlayers.get((Object)possibleInstance)).size() + playersCount < possibleInstance.getMinPlayers()) continue;
                    System.out.println("How could have this happened? (" + currentInstance2.getName() + ", " + possibleInstance.getName() + ")");
                    continue;
                }
                if (!joinStrongerInstIfNeeded || possibleInstance.getTempRate() <= strenght || possibleInstance.getTempRate() - strenght > maxDiff) continue;
                System.out.println("/// possible instance " + possibleInstance.getName() + "[" + possibleInstance.getTempRate() + "] - playersCount (" + ((FastList)_tempPlayers.get((Object)possibleInstance)).size() + "), strenght (" + possibleInstance.getTempRate() + ")");
                sumPlayers = ((FastList)_tempPlayers.get((Object)possibleInstance)).size() + playersCount;
                System.out.println("sum = " + sumPlayers);
                if (sumPlayers < possibleInstance.getMinPlayers()) continue;
                int max = possibleInstance.getMaxPlayers();
                toMove = sumPlayers > max ? max - ((FastList)_tempPlayers.get((Object)possibleInstance)).size() : ((FastList)_tempPlayers.get((Object)currentInstance2)).size();
                System.out.println("moving " + toMove + " players from " + currentInstance2.getName() + " to " + possibleInstance.getName());
                Test.movePlayers(possibleInstance, currentInstance2, toMove);
                System.out.println("size of " + possibleInstance.getName() + " is now " + ((FastList)_tempPlayers.get((Object)possibleInstance)).size());
                if (((FastList)_tempPlayers.get((Object)possibleInstance)).size() < possibleInstance.getMinPlayers()) continue;
                System.out.println(possibleInstance.getName() + " removed from notEnoughtPlayersInstances.");
                fixed.add(possibleInstance);
            }
        }
        for (MainEventInstanceType currentInstance2 : notEnoughtPlayersInstances) {
            playersCount = ((FastList)_tempPlayers.get((Object)currentInstance2)).size();
            if (playersCount == 0) continue;
            strenght = currentInstance2.getTempRate();
            boolean joinStrongerInstIfNeeded = true;
            int maxDiff = 2;
            for (MainEventInstanceType fixedInstance : fixed) {
                if (!joinStrongerInstIfNeeded || fixedInstance.getTempRate() <= strenght || fixedInstance.getTempRate() - strenght > maxDiff || (sumPlayers = ((FastList)_tempPlayers.get((Object)fixedInstance)).size()) >= fixedInstance.getMaxPlayers()) continue;
                toMove = fixedInstance.getMaxPlayers() - ((FastList)_tempPlayers.get((Object)fixedInstance)).size();
                Test.movePlayers(fixedInstance, currentInstance2, toMove);
            }
        }
        for (MainEventInstanceType toRemove : fixed) {
            notEnoughtPlayersInstances.remove(toRemove);
        }
        for (Map.Entry e2 : _tempPlayers.entrySet()) {
            int canMove;
            playersCount = ((FastList)e2.getValue()).size();
            if (playersCount == 0) continue;
            strenght = ((MainEventInstanceType)e2.getKey()).getTempRate();
            boolean joinStrongerInstIfNeeded = true;
            int maxDiff = 2;
            if (Test.hasEnoughtPlayers((MainEventInstanceType)e2.getKey())) continue;
            System.out.println(((MainEventInstanceType)e2.getKey()).getName() + " has BEFORE SAME LEVEL DIVIDE " + playersCount + " players");
            for (Map.Entry inst : _tempPlayers.entrySet()) {
                System.out.println("////// Instance " + ((MainEventInstanceType)inst.getKey()).getName() + "[" + ((MainEventInstanceType)inst.getKey()).getTempRate() + "] has " + ((FastList)inst.getValue()).size());
            }
            System.out.println(" DIVIDED //////////");
            while (playersCount > 0) {
                int temp = playersCount;
                for (Map.Entry i : _tempPlayers.entrySet()) {
                    if (playersCount <= 0) break;
                    if (!Test.hasEnoughtPlayers((MainEventInstanceType)i.getKey()) || ((MainEventInstanceType)i.getKey()).getTempRate() != strenght || (canMove = ((MainEventInstanceType)i.getKey()).getMaxPlayers() - ((FastList)i.getValue()).size()) <= 0 || Test.movePlayers((MainEventInstanceType)i.getKey(), (MainEventInstanceType)e2.getKey(), 1) != 1) continue;
                    --playersCount;
                }
                if (playersCount != temp) continue;
            }
            for (Map.Entry inst2 : _tempPlayers.entrySet()) {
                System.out.println("////// Instance " + ((MainEventInstanceType)inst2.getKey()).getName() + "[" + ((MainEventInstanceType)inst2.getKey()).getTempRate() + "] has " + ((FastList)inst2.getValue()).size());
            }
            System.out.println(((MainEventInstanceType)e2.getKey()).getName() + " has AFTER SAME LEVEL DIVIDE " + playersCount + " players");
            if (playersCount <= 0 || !joinStrongerInstIfNeeded) continue;
            while (playersCount > 0) {
                int temp = playersCount;
                for (Map.Entry i : _tempPlayers.entrySet()) {
                    if (playersCount <= 0) break;
                    if (!Test.hasEnoughtPlayers((MainEventInstanceType)i.getKey()) || ((MainEventInstanceType)i.getKey()).getTempRate() <= strenght || ((MainEventInstanceType)i.getKey()).getTempRate() - strenght > maxDiff || (canMove = ((MainEventInstanceType)i.getKey()).getMaxPlayers() - ((FastList)i.getValue()).size()) <= 0 || Test.movePlayers((MainEventInstanceType)i.getKey(), (MainEventInstanceType)e2.getKey(), 1) != 1) continue;
                    --playersCount;
                }
                if (playersCount != temp) continue;
            }
            System.out.println(((MainEventInstanceType)e2.getKey()).getName() + " has IN THE END " + playersCount + " players");
        }
        for (Object inst3 : notEnoughtPlayersInstances) {
            System.out.println("Not enought players for instance " + inst3.getName() + " (" + ((FastList)_tempPlayers.get(inst3)).size() + "), instance removed; " + ((FastList)_tempPlayers.get(inst3)).size() + " players unregistered");
            _tempPlayers.remove(inst3);
        }
        for (Object inst3 : _tempPlayers.entrySet()) {
            System.out.println("Instance " + ((MainEventInstanceType)inst3.getKey()).getName() + "[" + ((MainEventInstanceType)inst3.getKey()).getTempRate() + "] has " + ((FastList)inst3.getValue()).size());
        }
    }

    static {
        _vipsCount = 5;
        _chooseFromTopPercent = 30;
        compareByLevels = new Comparator<PlayerEventInfo>(){

            @Override
            public int compare(PlayerEventInfo o1, PlayerEventInfo o2) {
                int level2;
                int level1 = o1.getLevel();
                return level1 == (level2 = o2.getLevel()) ? 0 : (level1 < level2 ? 1 : -1);
            }
        };
        compareByPvps = new Comparator<PlayerEventInfo>(){

            @Override
            public int compare(PlayerEventInfo o1, PlayerEventInfo o2) {
                int pvp2;
                int pvp1 = o1.getPvpKills();
                return pvp1 == (pvp2 = o2.getPvpKills()) ? 0 : (pvp1 < pvp2 ? 1 : -1);
            }
        };
    }

}

