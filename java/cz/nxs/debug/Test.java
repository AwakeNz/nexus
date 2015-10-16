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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;
import cz.nxs.events.engine.main.base.MainEventInstanceType;
import cz.nxs.events.engine.team.EventTeam;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.ClassType;

public class Test
{
	@SuppressWarnings("unused")
	private static int next = 0;
	public static FastMap<MainEventInstanceType, FastList<String>> _tempPlayers;
	public static List<EventTeam> _teams;
	public static int _vipsCount = 5;
	public static int _chooseFromTopPercent = 30;
	
	public static void main(String[] args)
	{
	}
	
	protected static void partyPlayers()
	{
	}
	
	protected static void partyPlayers(List<PlayerEventInfo> party)
	{
		System.out.println("... partying ");
		for (PlayerEventInfo player : party)
		{
			System.out.println(player.getPlayersName() + ", type = " + player.getClassType().toString());
		}
	}
	
	public static Comparator<PlayerEventInfo> compareByLevels = (o1, o2) ->
	{
		int level1 = o1.getLevel();
		int level2 = o2.getLevel();
		
		return level1 < level2 ? 1 : level1 == level2 ? 0 : -1;
	};
	public static Comparator<PlayerEventInfo> compareByPvps = (o1, o2) ->
	{
		int pvp1 = o1.getPvpKills();
		int pvp2 = o2.getPvpKills();
		
		return pvp1 < pvp2 ? 1 : pvp1 == pvp2 ? 0 : -1;
	};
	
	protected static void selectVips(int instanceId, int count)
	{
	}
	
	protected static void dividePlayersToTeams(int instanceId, FastList<PlayerEventInfo> players, int teamsCount)
	{
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
		if (type.startsWith("PvPs"))
		{
			Collections.sort(players, compareByPvps);
		}
		FastMap<ClassType, FastList<PlayerEventInfo>> sortedPlayers = new FastMap<>();
		for (ClassType classType : ClassType.values())
		{
			sortedPlayers.put(classType, new FastList<>());
		}
		for (PlayerEventInfo pi : players)
		{
			sortedPlayers.get(pi.getClassType()).add(pi);
		}
		for (Map.Entry<ClassType, FastList<PlayerEventInfo>> e : sortedPlayers.entrySet())
		{
			System.out.println((e.getKey()).toString() + " has " + e.getValue().size() + " players");
		}
		int healersCount = (sortedPlayers.get(ClassType.Priest)).size();
		int teamId = 0;
		while (healersCount > 0)
		{
			teamId++;
			divided++;
			switch (teamId)
			{
				case 1:
					team1++;
					healers1++;
					break;
				case 2:
					team2++;
					healers2++;
					break;
				case 3:
					team3++;
					healers3++;
					break;
				case 4:
					team4++;
					healers4++;
			}
			healersCount--;
			
			player = sortedPlayers.get(ClassType.Priest).head().getNext().getValue();
			
			System.out.println(player.getClassType().toString() + " " + player.getPlayersName() + " -  goes to team " + teamId);
			
			sortedPlayers.get(ClassType.Priest).remove(player);
			if (teamId < teamsCount)
			{
				continue;
			}
			teamId = 0;
		}
		teamId = 0;
		for (Map.Entry<ClassType, FastList<PlayerEventInfo>> e : sortedPlayers.entrySet())
		{
			for (PlayerEventInfo pi : e.getValue())
			{
				teamId = team1 < team2 ? 1 : (team2 < team1 ? 2 : CallBack.getInstance().getOut().random(1, 2));
				++divided;
				switch (teamId)
				{
					case 1:
						team1++;
						if (pi.getClassType() == ClassType.Fighter)
						{
							fighters1++;
						}
						else
						{
							mages1++;
						}
						break;
					case 2:
						team2++;
						if (pi.getClassType() == ClassType.Fighter)
						{
							fighters2++;
						}
						else
						{
							mages2++;
						}
						break;
					case 3:
						team3++;
						if (pi.getClassType() == ClassType.Fighter)
						{
							fighters3++;
						}
						else
						{
							mages3++;
						}
						break;
					case 4:
						team4++;
						if (pi.getClassType() == ClassType.Fighter)
						{
							fighters4++;
						}
						else
						{
							mages4++;
						}
						break;
				
				}
				System.out.println(pi.getClassType().toString() + " " + pi.getPlayersName() + " -  goes to team " + teamId);
				if (teamId == teamsCount)
				{
					continue;
				}
				teamId = 0;
			}
		}
		System.out.println("divided: " + divided);
		System.out.println("team1: " + team1 + " ::: " + healers1 + " healers, " + mages1 + " mages, " + fighters1 + " fighters.");
		System.out.println("team2: " + team2 + " ::: " + healers2 + " healers, " + mages2 + " mages, " + fighters2 + " fighters.");
		System.out.println("team3: " + team3 + " ::: " + healers3 + " healers, " + mages3 + " mages, " + fighters3 + " fighters.");
		System.out.println("team4: " + team4 + " ::: " + healers4 + " healers, " + mages4 + " mages, " + fighters4 + " fighters.");
	}
	
	@SuppressWarnings("deprecation")
	protected static void reorganizeInstances()
	{
		List<MainEventInstanceType> sameStrenghtInstances = new FastList<>();
		for (int currentStrenght = 1; currentStrenght <= 10; ++currentStrenght)
		{
			for (Map.Entry<MainEventInstanceType, FastList<String>> e : _tempPlayers.entrySet())
			{
				if (Test.isFull(e.getKey()) || (e.getKey().getTempRate() != currentStrenght))
				{
					continue;
				}
				sameStrenghtInstances.add(e.getKey());
			}
			Collections.sort(sameStrenghtInstances, (i1, i2) ->
			{
				int neededPlayers1 = i1.getMinPlayers() - (_tempPlayers.get(i1)).size();
				int neededPlayers2 = i2.getMinPlayers() - (_tempPlayers.get(i2)).size();
				
				return neededPlayers1 < neededPlayers2 ? -1 : neededPlayers1 == neededPlayers2 ? 0 : 1;
			});
			reorganize(sameStrenghtInstances);
			sameStrenghtInstances.clear();
		}
	}
	
	protected static void init()
	{
		int id = 0;
		++id;
		FastList<String> players = new FastList<>();
		for (int i = 0; i < 4; ++i)
		{
			players.add(createPlayer(id));
		}
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 15, 1), players);
		++id;
		players = new FastList<>();
		for (int i = 0; i < 6; ++i)
		{
			players.add(createPlayer(id));
		}
		_tempPlayers.put(new MainEventInstanceType(id, null, "INSTANCE " + id, "Instance " + id, "", 10, 16, 2), players);
	}
	
	protected static String createPlayer(int id)
	{
		return "Player " + (id + 1);
	}
	
	@SuppressWarnings(
	{
		"deprecation",
		"null"
	})
	protected static void reorganize(List<MainEventInstanceType> instances)
	{
		block0: for (MainEventInstanceType instance : instances)
		{
			System.out.println("======================");
			System.out.println("*** " + instance.getName() + " processing");
			if (Test.hasEnoughtPlayers(instance))
			{
				System.out.println("" + instance.getName() + " already full");
				instances.remove(instance);
				continue;
			}
			int count = (_tempPlayers.get(instance)).size();
			int toMove = instance.getMinPlayers() - count;
			System.out.println("" + instance.getName() + " not full - players (" + count + "), toMove (" + toMove + ")");
			for (MainEventInstanceType possibleInstance : instances)
			{
				if (possibleInstance == instance)
				{
					int moved = movePlayers(instance, possibleInstance, toMove);
					System.out.println("- from '" + possibleInstance.getName() + "' moved " + moved + ", still need to move " + (toMove -= moved));
					if (toMove == 0)
					{
						System.out.println("- '" + instance.getName() + "' ready to run!");
						instances.remove(instance);
						continue block0;
					}
					if (toMove <= 0)
					{
					}
				}
			}
		}
		if (!instances.isEmpty())
		{
			int minPlayers = Integer.MAX_VALUE;
			MainEventInstanceType inst = null;
			for (MainEventInstanceType instance : instances)
			{
				if (instance.getMinPlayers() < minPlayers)
				{
					minPlayers = instance.getMinPlayers();
					inst = instance;
				}
			}
			System.out.println("*** - moving all players to instance " + inst.getName());
			for (MainEventInstanceType instance : instances)
			{
				if (instance != inst)
				{
					movePlayers(inst, instance, -1);
				}
			}
			System.out.println("*** Done, instance " + inst.getName() + " has " + (_tempPlayers.get(inst)).size() + " players.");
		}
	}
	
	protected static int movePlayers(MainEventInstanceType target, MainEventInstanceType source, int count)
	{
		if (count == 0)
		{
			return 0;
		}
		int moved = 0;
		for (String player : _tempPlayers.get(source))
		{
			(_tempPlayers.get(target)).add(player);
			(_tempPlayers.get(source)).remove(player);
			moved++;
			
			if ((count != -1) || (++moved >= count))
			{
				break;
			}
			break;
		}
		return moved;
	}
	
	@SuppressWarnings("deprecation")
	protected static boolean hasEnoughtPlayers(MainEventInstanceType instance)
	{
		return (_tempPlayers.get(instance)).size() >= instance.getMinPlayers();
	}
	
	@SuppressWarnings("deprecation")
	protected static boolean isFull(MainEventInstanceType instance)
	{
		return (_tempPlayers.get(instance)).size() >= instance.getMaxPlayers();
	}
	
	@SuppressWarnings(
	{
		"deprecation"
	})
	protected static void dividePlayers()
	{
		int strenght;
		int playersCount;
		boolean joinStrongerInstIfNeeded;
		int maxDiff;
		
		System.out.println("********* Reorganize Players ************");
		reorganizeInstances();
		
		System.out.println("********* Divide Players ************");
		List<MainEventInstanceType> notEnoughtPlayersInstances = new FastList<>();
		
		for (Map.Entry<MainEventInstanceType, FastList<String>> e : _tempPlayers.entrySet())
		{
			if ((e.getValue()).size() < e.getKey().getMinPlayers())
			{
				System.out.println("/ adding instance " + e.getKey().getName() + " [" + e.getKey().getTempRate() + "] to notEnoughtPlayersInstance");
				notEnoughtPlayersInstances.add(e.getKey());
			}
			else
			{
				System.out.println("/ instance " + e.getKey().getName() + " [" + e.getKey().getTempRate() + "] has enought players");
			}
		}
		List<MainEventInstanceType> fixed = new FastList<>();
		
		for (MainEventInstanceType currentInstance : notEnoughtPlayersInstances)
		{
			if ((currentInstance != null) && (!fixed.contains(currentInstance)))
			{
				System.out.println("================== " + currentInstance.getName());
				
				strenght = currentInstance.getTempRate();
				playersCount = (_tempPlayers.get(currentInstance)).size();
				
				joinStrongerInstIfNeeded = true;
				maxDiff = 2;
				System.out.println("*** current instance " + currentInstance.getName() + "[" + currentInstance.getTempRate() + "] - playersCount (" + playersCount + "), strenght (" + strenght + ")");
				
				for (MainEventInstanceType possibleInstance : notEnoughtPlayersInstances)
				{
					if ((possibleInstance != null) && (!fixed.contains(possibleInstance)) && (possibleInstance != currentInstance))
					{
						playersCount = (_tempPlayers.get(currentInstance)).size();
						if (possibleInstance.getTempRate() == strenght)
						{
							if (((_tempPlayers.get(possibleInstance)).size() + playersCount) >= possibleInstance.getMinPlayers())
							{
								System.out.println("How could have this happened? (" + currentInstance.getName() + ", " + possibleInstance.getName() + ")");
							}
							else if ((joinStrongerInstIfNeeded) && (possibleInstance.getTempRate() > strenght))
							{
								if ((possibleInstance.getTempRate() - strenght) <= maxDiff)
								{
									System.out.println("/// possible instance " + possibleInstance.getName() + "[" + possibleInstance.getTempRate() + "] - playersCount (" + (_tempPlayers.get(possibleInstance)).size() + "), strenght (" + possibleInstance.getTempRate() + ")");
									
									int sumPlayers = (_tempPlayers.get(possibleInstance)).size() + playersCount;
									
									System.out.println("sum = " + sumPlayers);
									if (sumPlayers >= possibleInstance.getMinPlayers())
									{
										int max = possibleInstance.getMaxPlayers();
										int toMove;
										if (sumPlayers > max)
										{
											toMove = max - (_tempPlayers.get(possibleInstance)).size();
										}
										else
										{
											toMove = (_tempPlayers.get(currentInstance)).size();
										}
										System.out.println("moving " + toMove + " players from " + currentInstance.getName() + " to " + possibleInstance.getName());
										
										movePlayers(possibleInstance, currentInstance, toMove);
										
										System.out.println("size of " + possibleInstance.getName() + " is now " + (_tempPlayers.get(possibleInstance)).size());
										if ((_tempPlayers.get(possibleInstance)).size() >= possibleInstance.getMinPlayers())
										{
											System.out.println(possibleInstance.getName() + " removed from notEnoughtPlayersInstances.");
											fixed.add(possibleInstance);
										}
									}
								}
							}
							
						}
					}
				}
			}
			for (MainEventInstanceType currentInstance2 : notEnoughtPlayersInstances)
			{
				playersCount = (_tempPlayers.get(currentInstance2)).size();
				if (playersCount != 0)
				{
					strenght = currentInstance2.getTempRate();
					joinStrongerInstIfNeeded = true;
					maxDiff = 2;
					for (MainEventInstanceType fixedInstance : fixed)
					{
						if ((joinStrongerInstIfNeeded) && (fixedInstance.getTempRate() > strenght))
						{
							if ((fixedInstance.getTempRate() - strenght) <= maxDiff)
							{
								int sumPlayers = (_tempPlayers.get(fixedInstance)).size();
								if (sumPlayers < fixedInstance.getMaxPlayers())
								{
									int toMove = fixedInstance.getMaxPlayers() - (_tempPlayers.get(fixedInstance)).size();
									movePlayers(fixedInstance, currentInstance2, toMove);
								}
							}
						}
					}
					for (MainEventInstanceType toRemove : fixed)
					{
						notEnoughtPlayersInstances.remove(toRemove);
					}
					for (Map.Entry<MainEventInstanceType, FastList<String>> e : _tempPlayers.entrySet())
					{
						playersCount = (e.getValue()).size();
						
						if (playersCount != 0)
						{
							strenght = e.getKey().getTempRate();
							joinStrongerInstIfNeeded = true;
							maxDiff = 2;
							if (hasEnoughtPlayers(e.getKey()))
							{
								System.out.println(e.getKey().getName() + " has BEFORE SAME LEVEL DIVIDE " + playersCount + " players");
								for (Map.Entry<MainEventInstanceType, FastList<String>> inst : _tempPlayers.entrySet())
								{
									System.out.println("////// Instance " + inst.getKey().getName() + "[" + inst.getKey().getTempRate() + "] has " + (inst.getValue()).size());
								}
								System.out.println(" DIVIDED //////////");
								while (playersCount > 0)
								{
									int temp = playersCount;
									for (Map.Entry<MainEventInstanceType, FastList<String>> i : _tempPlayers.entrySet())
									{
										if (playersCount <= 0)
										{
											break;
										}
										if (hasEnoughtPlayers(i.getKey()))
										{
											if (i.getKey().getTempRate() == strenght)
											{
												int canMove = i.getKey().getMaxPlayers() - (i.getValue()).size();
												if (canMove > 0)
												{
													if (movePlayers(i.getKey(), e.getKey(), 1) == 1)
													{
														playersCount--;
													}
												}
											}
										}
									}
									if (playersCount == temp)
									{
										break;
									}
								}
								for (Map.Entry<MainEventInstanceType, FastList<String>> inst : _tempPlayers.entrySet())
								{
									System.out.println("////// Instance " + inst.getKey().getName() + "[" + inst.getKey().getTempRate() + "] has " + (inst.getValue()).size());
								}
								System.out.println(e.getKey().getName() + " has AFTER SAME LEVEL DIVIDE " + playersCount + " players");
								if ((playersCount > 0) && (joinStrongerInstIfNeeded))
								{
									while (playersCount > 0)
									{
										int temp = playersCount;
										for (Map.Entry<MainEventInstanceType, FastList<String>> i : _tempPlayers.entrySet())
										{
											if (playersCount <= 0)
											{
												break;
											}
											if (hasEnoughtPlayers(i.getKey()))
											{
												if (i.getKey().getTempRate() > strenght)
												{
													if ((i.getKey().getTempRate() - strenght) <= maxDiff)
													{
														int canMove = i.getKey().getMaxPlayers() - (i.getValue()).size();
														if (canMove > 0)
														{
															if (movePlayers(i.getKey(), e.getKey(), 1) == 1)
															{
																playersCount--;
															}
														}
													}
												}
											}
										}
										if (playersCount == temp)
										{
											break;
										}
									}
									System.out.println(e.getKey().getName() + " has IN THE END " + playersCount + " players");
								}
							}
						}
					}
					for (MainEventInstanceType inst : notEnoughtPlayersInstances)
					{
						System.out.println("Not enought players for instance " + inst.getName() + " (" + (_tempPlayers.get(inst)).size() + "), instance removed; " + (_tempPlayers.get(inst)).size() + " players unregistered");
						_tempPlayers.remove(inst);
					}
					for (Map.Entry<MainEventInstanceType, FastList<String>> inst : _tempPlayers.entrySet())
					{
						System.out.println("Instance " + inst.getKey().getName() + "[" + inst.getKey().getTempRate() + "] has " + (inst.getValue()).size());
					}
				}
			}
		}
	}
}
