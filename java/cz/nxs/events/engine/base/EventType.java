/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.base;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.logging.Level;

import javolution.util.FastList;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.EventMapSystem;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.events.Battlefield;
import cz.nxs.events.engine.main.events.CaptureTheFlag;
import cz.nxs.events.engine.main.events.Deathmatch;
import cz.nxs.events.engine.main.events.Domination;
import cz.nxs.events.engine.main.events.HuntingGrounds;
import cz.nxs.events.engine.main.events.LastManStanding;
import cz.nxs.events.engine.main.events.LuckyChests;
import cz.nxs.events.engine.main.events.MassDomination;
import cz.nxs.events.engine.main.events.Mutant;
import cz.nxs.events.engine.main.events.TeamVsTeam;
import cz.nxs.events.engine.main.events.TreasureHunt;
import cz.nxs.events.engine.main.events.TreasureHuntPvp;
import cz.nxs.events.engine.main.events.VIPTeamVsTeam;
import cz.nxs.events.engine.main.events.Zombies;
import cz.nxs.events.engine.mini.events.KoreanManager;
import cz.nxs.events.engine.mini.events.MiniTvTManager;
import cz.nxs.events.engine.mini.events.OnevsOneManager;
import cz.nxs.events.engine.mini.events.PartyvsPartyManager;

public enum EventType
{
	Unassigned(0, "", "", Category.MainTeam, true, false),
	TvT(1, "TvT", "Team vs Team", Category.MainTeam, true, false, TeamVsTeam.class),
	CTF(2, "CTF", "Capture the Flag", Category.MainTeam, true, false, CaptureTheFlag.class),
	Domination(3, "Domination", "Domination", Category.MainTeam, true, false, Domination.class),
	MassDomination(4, "MassDom", "Mass Domination", Category.MainTeam, true, false, MassDomination.class),
	DM(5, "DM", "Deathmatch", Category.MainFFA, true, false, Deathmatch.class),
	LastMan(6, "LastMan", "Last Man Standing", Category.MainFFA, true, false, LastManStanding.class),
	TvTAdv(7, "TvTAdv", "TvT Advanced", Category.MainTeam, true, false, VIPTeamVsTeam.class),
	LuckyChests(8, "Chests", "Lucky Chests", Category.MainFFA, true, false, LuckyChests.class),
	Zombies(9, "Zombies", "Zombies", Category.MainTeam, true, false, Zombies.class),
	Mutant(10, "Mutant", "Mutant", Category.MainTeam, true, false, Mutant.class),
	TreasureHunt(11, "THunt", "Treasure Hunt", Category.MainTeam, true, false, TreasureHunt.class),
	TreasureHuntPvp(12, "THuntPvP", "Treasure Hunt PvP", Category.MainTeam, true, false, TreasureHuntPvp.class),
	HuntingGround(13, "HuntGround", "Hunting Grounds", Category.MainTeam, true, false, HuntingGrounds.class),
	Battlefields(14, "Battlefields", "Battlefields", Category.MainTeam, true, false, Battlefield.class),
	Commanders(14, "Commanders", "Commanders", Category.MainTeam, true, false),
	BombFight(15, "Bomb", "Bomb Fight", Category.MainTeam, true, false),
	RussianRoulette(16, "Russian", "Russian Roulette", Category.MainTeam, true, false),
	Simon(17, "Simon", "Simon Says", Category.MainTeam, true, false),
	Classic_1v1(50, "1v1", "Single players fights", Category.Mini, true, false, OnevsOneManager.class),
	PartyvsParty(51, "PTvsPT", "Party fights", Category.Mini, true, false, PartyvsPartyManager.class),
	Korean(52, "Korean", "Korean Style", Category.Mini, true, false, KoreanManager.class),
	MiniTvT(53, "MiniTvT", "Mini TvT", Category.Mini, true, true, MiniTvTManager.class),
	LMS(54, "LMS", "Last Man", Category.Mini, true, false),
	LTS(55, "LTS", "Last Team", Category.Mini, true, false),
	Classic_2v2(56, "2v2", "2v2 event", Category.Mini, true, false),
	Tournament(57, "Tournament", "Tournament", Category.Mini, false, false),
	Underground_Coliseum(58, "UC", "Tower Crush", Category.Mini, true, false),
	Hitman(59, "Hitman", "Hitman", Category.Mini, false, false),
	RBHunt(60, "RBH", "Raid Hunt", Category.Mini, true, false),
	SurvivalArena(61, "Survival", "Survival Arena", Category.Mini, true, true);
	
	private int _order;
	private Category _category;
	private String _shortName;
	private String _longName;
	private boolean _allowEdits;
	private boolean _allowConfig;
	private Class<? extends Event> eventClass;
	public static int lastGivenEvent;
	
	private EventType(int order, String shortName, String longName, Category category, boolean allowEdits, boolean allowConfig, Class<? extends Event> eventClass)
	{
		this._order = order;
		this._category = category;
		this._shortName = shortName;
		this._longName = longName;
		this._allowEdits = allowEdits;
		this._allowConfig = allowConfig;
		this.eventClass = eventClass;
	}
	
	private EventType(int order, String shortName, String longName, Category category, boolean allowEdits, boolean allowConfig)
	{
		this(order, shortName, longName, category, allowEdits, allowConfig, null);
	}
	
	public Event loadEvent(MainEventManager manager)
	{
		if (this.eventClass != null)
		{
			try
			{
				if (this.isRegularEvent())
				{
					Constructor<? extends Event> constructor = this.eventClass.getConstructor(EventType.class, MainEventManager.class);
					if (constructor == null)
					{
						NexusLoader.debug("Wrong constructor for event " + this.getAltTitle() + ".", Level.SEVERE);
						return null;
					}
					return constructor.newInstance(new Object[]
					{
						this,
						manager
					});
				}
			}
			catch (Exception e)
			{
				System.out.println(this.getAltTitle() + " event load error");
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return null;
	}
	
	public int getId()
	{
		return this.getOrder();
	}
	
	public int getOrder()
	{
		return this._order;
	}
	
	public int getMainEventId()
	{
		return 0;
	}
	
	public boolean isRegularEvent()
	{
		return (this._category == Category.MainTeam) || (this._category == Category.MainFFA);
	}
	
	public boolean isMiniEvent()
	{
		return this._category == Category.Mini;
	}
	
	public boolean isGlobalEvent()
	{
		return this._category == Category.Global;
	}
	
	public boolean isFFAEvent()
	{
		return this._category == Category.MainFFA;
	}
	
	public Category getCategory()
	{
		return this._category;
	}
	
	public boolean allowConfig()
	{
		return this._allowConfig;
	}
	
	public boolean allowEdits()
	{
		return this._allowEdits;
	}
	
	public String getAltTitle()
	{
		return this._shortName;
	}
	
	public String getHtmlTitle()
	{
		return this._longName;
	}
	
	public static EventType getById(int id)
	{
		for (EventType t : EventType.values())
		{
			if (t.getId() != id)
			{
				continue;
			}
			return t;
		}
		return Unassigned;
	}
	
	public static EventType getType(String value)
	{
		for (EventType t : EventType.values())
		{
			if (!t.toString().equalsIgnoreCase(value) && !t.getAltTitle().equalsIgnoreCase(value) && !t.getHtmlTitle().equalsIgnoreCase(value) && !String.valueOf(t.getId()).equals(value))
			{
				continue;
			}
			return t;
		}
		return null;
	}
	
	public static EventType[] getMiniEvents()
	{
		List<EventType> types = new FastList<>();
		for (EventType t : values())
		{
			types.add(t);
		}
		return types.toArray(new EventType[types.size()]);
	}
	
	public static EventType getEventByMainId(int id)
	{
		for (EventType t : EventType.values())
		{
			if (t.getMainEventId() != id)
			{
				continue;
			}
			return t;
		}
		return null;
	}
	
	public static EventType getNextRegularEvent()
	{
		EventType t = EventManager.getInstance().getMainEventManager().nextAvailableEvent(false);
		if (t == null)
		{
			NexusLoader.debug("getNextRegularEvent() returned null, all events are disabled (or have no maps).", Level.WARNING);
			return null;
		}
		lastGivenEvent = t.getId();
		return t;
	}
	
	public static EventType getNextRegularEvent(int lastId)
	{
		int next = lastId + 1;
		for (int i = 0; i < EventType.values().length; ++i)
		{
			for (EventType t : EventType.values())
			{
				if ((t.getId() != next) || !t.isRegularEvent() || !EventConfig.getInstance().isEventAllowed(t) || (EventManager.getInstance().getMainEvent(t) == null) || (EventMapSystem.getInstance().getMapsCount(t) <= 0))
				{
					continue;
				}
				return t;
			}
			++next;
		}
		for (EventType t : EventType.values())
		{
			if (!t.isRegularEvent() || !EventConfig.getInstance().isEventAllowed(t) || (EventManager.getInstance().getMainEvent(t) == null) || (EventMapSystem.getInstance().getMapsCount(t) <= 0))
			{
				continue;
			}
			return t;
		}
		NexusLoader.debug("getNextRegularEvent(int lastId) returned null, all events are DISABLED (or have no maps).", Level.WARNING);
		return null;
	}
	
	static
	{
		lastGivenEvent = 0;
	}
	
	public static enum Category
	{
		MainTeam,
		MainFFA,
		Mini,
		Global;
		
		private Category()
		{
		}
	}
	
}
