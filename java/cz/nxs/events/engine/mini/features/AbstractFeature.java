/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.text.TextBuilder
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.mini.features;

import java.util.List;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.mini.EventMode;
import cz.nxs.interf.PlayerEventInfo;

public abstract class AbstractFeature
{
	protected EventType _event;
	protected String _params;
	protected List<FeatureConfig> _configs = new FastList<>();
	
	public abstract EventMode.FeatureType getType();
	
	protected abstract void initValues();
	
	public AbstractFeature(EventType event)
	{
		_event = event;
	}
	
	public abstract boolean checkPlayer(PlayerEventInfo var1);
	
	protected String[] splitParams(String params)
	{
		return params.split(",");
	}
	
	public String getParams()
	{
		return _params;
	}
	
	protected void addConfig(String name, String desc, int inputFormType)
	{
		_configs.add(new FeatureConfig(name, desc, inputFormType));
	}
	
	public FeatureConfig getConfig(String name)
	{
		for (FeatureConfig c : _configs)
		{
			if (!c.name.equals(name))
			{
				continue;
			}
			return c;
		}
		return null;
	}
	
	public void setValueFor(String configName, String value)
	{
		String[] splitted = _params.split(",");
		int index = 0;
		for (FeatureConfig c : _configs)
		{
			if (c.name.equals(configName))
			{
				break;
			}
			++index;
		}
		if (splitted.length < index)
		{
			return;
		}
		splitted[index] = value;
		TextBuilder tb = new TextBuilder();
		for (String s : splitted)
		{
			tb.append(s + ",");
		}
		String result = tb.toString();
		_params = result.substring(0, result.length() - 1);
		initValues();
	}
	
	public String getValueFor(String configName)
	{
		String[] splitted = _params.split(",");
		int index = 0;
		for (FeatureConfig c : _configs)
		{
			if (c.name.equals(configName))
			{
				break;
			}
			++index;
		}
		if (splitted.length < index)
		{
			return "N/A";
		}
		return splitted[index];
	}
	
	public List<FeatureConfig> getConfigs()
	{
		return _configs;
	}
	
	public class FeatureConfig
	{
		public String name;
		public String desc;
		public int inputFormType;
		
		protected FeatureConfig(String name, String desc, int inputFormType)
		{
			this.name = name;
			this.desc = desc;
			this.inputFormType = inputFormType;
		}
	}
	
}
