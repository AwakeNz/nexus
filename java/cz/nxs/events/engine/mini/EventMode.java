/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.mini;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javolution.util.FastList;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;

public class EventMode implements Runnable
{
	private final EventType _event;
	private boolean _gmAllowed;
	private String _name;
	private String _visibleName;
	private int _npcId;
	private final List<AbstractFeature> _features;
	private final FastList<Integer> _disallowedMaps;
	private final ScheduleInfo _scheduleInfo;
	private boolean _running;
	private ScheduledFuture<?> _future;
	
	public EventMode(EventType event)
	{
		_event = event;
		_name = "Default";
		_npcId = 0;
		_visibleName = this._name;
		_features = new FastList<>();
		_disallowedMaps = new FastList<>();
		_scheduleInfo = new ScheduleInfo(_event, _name);
		refreshScheduler();
	}
	
	@Override
	public void run()
	{
		if (_running)
		{
			_running = false;
			MiniEventManager manager = EventManager.getInstance().getMiniEvent(this._event, this.getModeId());
			if (manager != null)
			{
				manager.cleanMe(false);
			}
			scheduleRun();
		}
		else
		{
			_running = true;
			scheduleStop();
		}
	}
	
	public void refreshScheduler()
	{
		if (this.isNonstopRun())
		{
			this._running = true;
			return;
		}
		if (this._running)
		{
			boolean running = false;
			for (ScheduleInfo.RunTime time : this._scheduleInfo.getTimes().values())
			{
				if (!time.isActual())
				{
					continue;
				}
				running = true;
				this.run();
			}
			if (running)
			{
				this.scheduleStop();
			}
			else
			{
				this.run();
			}
		}
		else
		{
			boolean running = false;
			for (ScheduleInfo.RunTime time : this._scheduleInfo.getTimes().values())
			{
				if (!time.isActual())
				{
					continue;
				}
				running = true;
				this.run();
			}
			if (!running)
			{
				this.scheduleRun();
			}
		}
	}
	
	public void scheduleRun()
	{
		long runTime = this._scheduleInfo.getNextStart(false);
		if (!(this.isNonstopRun() || (runTime <= -1)))
		{
			this._future = CallBack.getInstance().getOut().scheduleGeneral(this, runTime);
		}
		else
		{
			this._running = true;
		}
	}
	
	public void scheduleStop()
	{
		long endTime = this._scheduleInfo.getEnd(false);
		if (!(this.isNonstopRun() || (endTime == -1)))
		{
			this._future = CallBack.getInstance().getOut().scheduleGeneral(this, endTime);
		}
	}
	
	public boolean isNonstopRun()
	{
		return this._scheduleInfo.isNonstopRun();
	}
	
	public List<AbstractFeature> getFeatures()
	{
		return this._features;
	}
	
	public void addFeature(PlayerEventInfo gm, FeatureType type, String parameters)
	{
		Constructor _constructor = null;
		AbstractFeature feature = null;
		Class[] classParams = new Class[]
		{
			EventType.class,
			PlayerEventInfo.class,
			String.class
		};
		try
		{
			_constructor = Class.forName("cz.nxs.events.engine.mini.features." + type.toString() + "Feature").getConstructor(classParams);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		try
		{
			Object[] objectParams = new Object[]
			{
				this._event,
				gm,
				parameters
			};
			Object tmp = _constructor.newInstance(objectParams);
			feature = (AbstractFeature) tmp;
			this._features.add(feature);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
	}
	
	public void addFeature(AbstractFeature feature)
	{
		this._features.add(feature);
	}
	
	public boolean checkPlayer(PlayerEventInfo player)
	{
		for (AbstractFeature feature : this._features)
		{
			if (feature.checkPlayer(player))
			{
				continue;
			}
			return false;
		}
		return true;
	}
	
	public long getFuture()
	{
		return this._future == null ? -1 : this._future.getDelay(TimeUnit.MILLISECONDS);
	}
	
	public FastList<Integer> getDisMaps()
	{
		return this._disallowedMaps;
	}
	
	public String getModeName()
	{
		return this._name;
	}
	
	public String getVisibleName()
	{
		if ((this._visibleName == null) || (this._visibleName.length() == 0))
		{
			return this._name;
		}
		return this._visibleName;
	}
	
	public int getNpcId()
	{
		return this._npcId;
	}
	
	public void setNpcId(int id)
	{
		this._npcId = id;
	}
	
	public void setVisibleName(String name)
	{
		this._visibleName = name;
	}
	
	public void setModeName(String s)
	{
		this._name = s;
	}
	
	public boolean isAllowed()
	{
		return this._gmAllowed;
	}
	
	public boolean isRunning()
	{
		return this._running;
	}
	
	public void setAllowed(boolean b)
	{
		this._gmAllowed = b;
	}
	
	public ScheduleInfo getScheduleInfo()
	{
		return this._scheduleInfo;
	}
	
	public int getModeId()
	{
		for (Map.Entry<Integer, MiniEventManager> e : EventManager.getInstance().getMiniEvents().get(this._event).entrySet())
		{
			if (!e.getValue().getMode().getModeName().equals(this.getModeName()))
			{
				continue;
			}
			return e.getKey();
		}
		return 0;
	}
	
	public static enum FeatureCategory
	{
		Configs,
		Items,
		Players;
		
		private FeatureCategory()
		{
		}
	}
	
	public static enum FeatureType
	{
		Level,
		ItemGrades,
		Enchant,
		Items,
		Delays,
		TimeLimit,
		Skills,
		Buffer,
		StrenghtChecks,
		Rounds,
		TeamsAmmount,
		TeamSize;
		
		private FeatureType()
		{
		}
	}
	
}
