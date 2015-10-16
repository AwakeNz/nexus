package cz.nxs.playervalue;

import java.util.List;

import javolution.util.FastList;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.playervalue.criteria.GearScore;
import cz.nxs.playervalue.criteria.ICriteria;
import cz.nxs.playervalue.criteria.PlayerClass;
import cz.nxs.playervalue.criteria.PlayerLevel;
import cz.nxs.playervalue.criteria.PlayerSkills;

public class PlayerValueEngine
{
	private final List<ICriteria> criterias = new FastList<>();
	
	public PlayerValueEngine()
	{
		load();
		NexusLoader.debug("Nexus Engine: Loaded PlayerValue engine.");
	}
	
	private void load()
	{
		this.criterias.add(GearScore.getInstance());
		this.criterias.add(PlayerClass.getInstance());
		this.criterias.add(PlayerLevel.getInstance());
		this.criterias.add(PlayerSkills.getInstance());
	}
	
	public void addCriteria(ICriteria c)
	{
		this.criterias.add(c);
	}
	
	public int getPlayerValue(PlayerEventInfo player)
	{
		if (!EventConfig.getInstance().getGlobalConfigBoolean("GearScore", "enableGearScore"))
		{
			return 0;
		}
		int value = 0;
		for (ICriteria i : this.criterias)
		{
			value += i.getPoints(player);
		}
		return value;
	}
	
	public static final PlayerValueEngine getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final PlayerValueEngine _instance = new PlayerValueEngine();
	}
}
