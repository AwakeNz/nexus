/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.events.engine.base;

import cz.nxs.l2j.CallBack;

public class Loc
{
	private int _x;
	private int _y;
	private final int _z;
	private int _heading;
	
	public Loc(int x, int y, int z)
	{
		this._x = x;
		this._y = y;
		this._z = z;
	}
	
	public Loc(int x, int y, int z, int heading)
	{
		this._x = x;
		this._y = y;
		this._z = z;
		this._heading = heading;
	}
	
	public void addRadius(int radius)
	{
		this._x += CallBack.getInstance().getOut().random(radius * 2) - radius;
		this._y += CallBack.getInstance().getOut().random(radius * 2) - radius;
	}
	
	public int getX()
	{
		return this._x;
	}
	
	public int getY()
	{
		return this._y;
	}
	
	public int getZ()
	{
		return this._z;
	}
	
	public int getHeading()
	{
		return this._heading;
	}
}
