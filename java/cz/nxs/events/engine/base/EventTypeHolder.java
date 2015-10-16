/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.base;


public class EventTypeHolder
{
	public EventType _type;
	
	public EventTypeHolder(EventType type)
	{
		this._type = type;
	}
	
	public EventTypeHolder register()
	{
		EventType.addHolder(this);
		return this;
	}
	
	public EventType getType()
	{
		return this._type;
	}
}
