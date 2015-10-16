package cz.nxs.events;

import java.util.Map;

import javolution.util.FastList;
import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.RewardPosition;
import cz.nxs.events.engine.base.SpawnType;

public interface Configurable
{
	public void loadConfigs();
	
	public void clearConfigs();
	
	public FastList<String> getCategories();
	
	public Map<String, ConfigModel> getConfigs();
	
	public Map<String, ConfigModel> getMapConfigs();
	
	public RewardPosition[] getRewardTypes();
	
	public Map<SpawnType, String> getAviableSpawnTypes();
	
	public void setConfig(String var1, String var2, boolean var3);
	
	public String getDescriptionForReward(RewardPosition var1);
	
	public int getTeamsCount();
	
	public boolean canRun(EventMap var1);
	
	public String getMissingSpawns(EventMap var1);
}
