/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.base.description;

import java.util.Map;
import java.util.logging.Level;

import javolution.util.FastMap;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.base.EventType;

public class EventDescriptionSystem
{
	private final Map<EventType, EventDescription> _descriptions = new FastMap<>();
	
	public EventDescriptionSystem()
	{
		NexusLoader.debug("Loaded editable Event Description system.", Level.INFO);
	}
	
	public void addDescription(EventType type, EventDescription description)
	{
		this._descriptions.put(type, description);
	}
	
	public EventDescription getDescription(EventType type)
	{
		if (this._descriptions.containsKey(type))
		{
			return this._descriptions.get(type);
		}
		return null;
	}
	
	public static final EventDescriptionSystem getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final EventDescriptionSystem _instance = new EventDescriptionSystem();
		
		private SingletonHolder()
		{
		}
	}
	
}
