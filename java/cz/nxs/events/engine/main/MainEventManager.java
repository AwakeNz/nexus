/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  cz.nxs.interf.PlayerEventInfo
 *  cz.nxs.interf.delegate.NpcData
 *  cz.nxs.interf.delegate.NpcTemplateData
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main;

import cz.nxs.events.Configurable;
import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventBuffer;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.EventMapSystem;
import cz.nxs.events.engine.EventWarnings;
import cz.nxs.events.engine.base.EventMap;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.base.Loc;
import cz.nxs.events.engine.html.EventHtmlManager;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.interf.delegate.NpcData;
import cz.nxs.interf.delegate.NpcTemplateData;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IPlayerBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javolution.util.FastList;
import javolution.util.FastMap;

public class MainEventManager {
    EventManager _manager = EventManager.getInstance();
    private EventTaskScheduler _task;
    private AbstractMainEvent current;
    private EventMap activeMap;
    private List<PlayerEventInfo> _players;
    private State _state = State.IDLE;
    private int _counter;
    private long lastEvent;
    private RegistrationCountdown _regCountdown;
    private ScheduledFuture<?> _regCountdownFuture;
    private ScheduledFuture<?> taskFuture;
    public Map<Integer, RegNpcLoc> regNpcLocs;
    private RegNpcLoc regNpc;
    private NpcData regNpcInstance;
    private int eventRunTime;
    private boolean autoScheduler = false;
    private double pausedTimeLeft;
    private EventScheduler scheduler;
    private List<EventScheduleData> _eventScheduleData = new FastList();
    private EventType _lastEvent = null;

    public MainEventManager() {
        this._task = new EventTaskScheduler();
        this._regCountdown = new RegistrationCountdown();
        this._counter = 0;
        this.activeMap = null;
        this.eventRunTime = 0;
        this._players = new FastList();
        this.initRegNpcLocs();
        this.scheduler = new EventScheduler();
        this.scheduler.schedule(-1.0, true);
    }

    private void initRegNpcLocs() {
        this.regNpcLocs = new FastMap();
        this.regNpcLocs.put(1, new RegNpcLoc("Your cords", null));
        this.regNpcLocs.put(2, new RegNpcLoc("Hunters Village", new int[]{116541, 76077, -2730, 0}));
        this.regNpcLocs.put(3, new RegNpcLoc("Goddard Town", new int[]{147726, -56323, -2781, 0}));
        this.regNpcLocs.put(4, new RegNpcLoc("Ketra/Varka", new int[]{125176, -69204, -3260, 0}));
        this.regNpcLocs.put(5, new RegNpcLoc("Cemetery", new int[]{182297, 19407, -3174, 0}));
        this.regNpcLocs.put(6, new RegNpcLoc("Aden Town", new int[]{148083, 26983, -2205, 0}));
    }

    public synchronized void startEvent(PlayerEventInfo gm, EventType type, int regTime, String mapName, String npcLoc, int runTime) {
        AbstractMainEvent event;
        if (NexusLoader.detailedDebug) {
            this.print((gm == null ? "GM" : "Scheduler") + " starting an event");
        }
        if ((event = EventManager.getInstance().getMainEvent(type)) == null) {
            if (gm != null) {
                gm.sendMessage("This event is not finished yet (most likely cause it is being reworked to be a mini event).");
            }
            NexusLoader.debug((String)"An unfinished event is chosen to be run. Skipping to the next one...", (Level)Level.WARNING);
            this.scheduler.run();
            return;
        }
        EventMap map = EventMapSystem.getInstance().getMap(type, mapName);
        if (map == null) {
            if (gm != null) {
                gm.sendMessage("Map " + mapName + " doesn't exist or is not allowed for this event.");
            } else {
                NexusLoader.debug((String)("Map " + mapName + " doesn't exist for event " + type.getAltTitle()), (Level)Level.WARNING);
            }
            return;
        }
        RegNpcLoc npc = null;
        if (npcLoc != null) {
            for (Map.Entry<Integer, RegNpcLoc> e : this.regNpcLocs.entrySet()) {
                if (!e.getValue().name.equalsIgnoreCase(npcLoc)) continue;
                npc = e.getValue();
                break;
            }
        }
        if (npc == null && gm != null) {
            gm.sendMessage("Reg NPC location " + npcLoc + " is not registered in the engine.");
            return;
        }
        if (npc == null) {
            String configsCords = EventConfig.getInstance().getGlobalConfigValue("spawnRegNpcCords");
            int x = Integer.parseInt(configsCords.split(";")[0]);
            int y = Integer.parseInt(configsCords.split(";")[1]);
            int z = Integer.parseInt(configsCords.split(";")[2]);
            npc = new RegNpcLoc("From Configs", new int[]{x, y, z, 0});
        }
        if (NexusLoader.detailedDebug) {
            this.print("map " + map.getMapName() + ", event " + event.getEventName());
        }
        if (regTime <= 0 || regTime >= 1439) {
            if (gm != null) {
                gm.sendMessage("The minutes for registration must be within interval 1-1439 minutes.");
            } else {
                NexusLoader.debug((String)("Can't start main event (automatic scheduler) - regTime is too high or too low (" + regTime + ")."), (Level)Level.SEVERE);
            }
            return;
        }
        int eventsRunTime = event.getInt("runTime");
        if (gm == null && eventsRunTime > 0) {
            runTime = eventsRunTime;
        }
        if (runTime <= 0 || runTime >= 120) {
            if (gm != null) {
                gm.sendMessage("RunTime must be at least 1 minute and max. 120 minutes.");
            } else {
                NexusLoader.debug((String)("Can't start main event (automatic scheduler) - runTime is too high or too low (" + runTime + ")."), (Level)Level.SEVERE);
            }
            return;
        }
        this.eventRunTime = runTime * 60;
        if (NexusLoader.detailedDebug) {
            this.print("event runtime (in seconds) is " + eventsRunTime * 60 + "s, regtime is " + regTime * 60 + "s");
        }
        this.regNpc = npc;
        this._state = State.REGISTERING;
        this.current = event;
        this.current.startRegistration();
        if (NexusLoader.detailedDebug) {
            this.print("event registration started, state is now REGISTERING");
        }
        this.activeMap = map;
        this._counter = regTime * 60;
        this._regCountdownFuture = CallBack.getInstance().getOut().scheduleGeneral(this._regCountdown, 1);
        if (NexusLoader.detailedDebug) {
            this.print("scheduled registration countdown");
        }
        this.spawnRegNpc(gm);
        if (NexusLoader.detailedDebug) {
            this.print("regNpc finished spawn method");
        }
        this.announce(LanguageEngine.getMsg("announce_eventStarted", type.getHtmlTitle()));
        String announce = EventConfig.getInstance().getGlobalConfigValue("announceRegNpcPos");
        if (announce.equals("-")) {
            return;
        }
        if (gm != null) {
            if (!(npc.name.equals("Your cords") || npc.name.equals("From Configs"))) {
                this.announce(LanguageEngine.getMsg("announce_npcPos", npc.name));
                if (NexusLoader.detailedDebug) {
                    this.print("announcing registration cords (1 - gm != null)");
                }
            } else if (NexusLoader.detailedDebug) {
                this.print("not announcing registration cords (either Your Cords or From Configs chosen)");
            }
        } else {
            this.announce(LanguageEngine.getMsg("announce_npcPos", announce));
            if (NexusLoader.detailedDebug) {
                this.print("announcing registration cords (2 - gm == null)");
            }
        }
        if (EventConfig.getInstance().getGlobalConfigBoolean("announce_moreInfoInCb")) {
            this.announce(LanguageEngine.getMsg("announce_moreInfoInCb"));
        }
        NexusLoader.debug((String)("Started registration for event " + this.current.getEventName()));
        if (gm != null) {
            gm.sendMessage("The event has been started.");
        }
        if (NexusLoader.detailedDebug) {
            this.print("finished startEvent() method");
        }
    }

    private void spawnRegNpc(PlayerEventInfo gm) {
        if (!(gm != null || EventConfig.getInstance().getGlobalConfigBoolean("allowSpawnRegNpc"))) {
            this.print("configs permitted spawning regNpc");
            return;
        }
        if (this.regNpc != null) {
            int id = EventConfig.getInstance().getGlobalConfigInt("mainEventManagerId");
            NpcTemplateData template = new NpcTemplateData(id);
            this.print("spawning npc id " + id + ", template exists = " + template.exists());
            try {
                NpcData data = this.regNpc.cords == null ? template.doSpawn(gm.getX(), gm.getY(), gm.getZ(), 1, gm.getHeading(), 0) : template.doSpawn(this.regNpc.cords[0], this.regNpc.cords[1], this.regNpc.cords[2], 1, this.regNpc.cords[3], 0);
                this.regNpcInstance = data;
                this.regNpcInstance.setTitle(this.current.getEventType().getHtmlTitle());
                this.regNpcInstance.broadcastNpcInfo();
                this.print("NPC spawned to cords " + data.getLoc().getX() + ", " + data.getLoc().getY() + ", " + data.getLoc().getZ() + "; objId = " + data.getObjectId());
            }
            catch (Exception e) {
                e.printStackTrace();
                this.print("error spawning NPC, " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
            }
        }
    }

    public void unspawnRegNpc() {
        if (NexusLoader.detailedDebug) {
            this.print("unspawnRegNpc()");
        }
        if (this.regNpcInstance != null) {
            if (NexusLoader.detailedDebug) {
                this.print("regNpcInstance is not null, unspawning it...");
            }
            this.regNpcInstance.deleteMe();
            this.regNpcInstance = null;
        } else if (NexusLoader.detailedDebug) {
            this.print("regNpcInstance is NULL!");
        }
        this.regNpc = null;
    }

    public synchronized void skipDelay(PlayerEventInfo gm) {
        if (NexusLoader.detailedDebug) {
            this.print("skipping event delay... ");
        }
        if (this._state == State.IDLE) {
            if (NexusLoader.detailedDebug) {
                this.print("state is idle, can't skip delay");
            }
            gm.sendMessage("There's no active event atm.");
            return;
        }
        if (this._state == State.REGISTERING) {
            if (NexusLoader.detailedDebug) {
                this.print("state is registering, skipping delay...");
            }
            if (this._regCountdownFuture != null) {
                this._regCountdownFuture.cancel(false);
            }
            if (this.taskFuture != null) {
                this.taskFuture.cancel(false);
            }
            this._counter = 0;
            this._regCountdownFuture = CallBack.getInstance().getOut().scheduleGeneral(this._regCountdown, 1);
            if (NexusLoader.detailedDebug) {
                this.print("delay successfully skipped");
            }
        } else {
            gm.sendMessage("The event can skip waiting delay only when it's in the registration state.");
            if (NexusLoader.detailedDebug) {
                this.print("can't skip delay, state is " + this._state.toString());
            }
        }
    }

    public void watchEvent(PlayerEventInfo gm, int instanceId) {
        AbstractMainEvent event = this.current;
        if (event == null) {
            gm.sendMessage("No event is available now.");
            return;
        }
        try {
            event.addSpectator(gm, instanceId);
        }
        catch (Exception e) {
            e.printStackTrace();
            gm.sendMessage("Event cannot be spectated now. Please try it again later.");
        }
    }

    public void stopWatching(PlayerEventInfo gm) {
        AbstractMainEvent event = this.current;
        if (event == null) {
            gm.sendMessage("No event is available now.");
            return;
        }
        event.removeSpectator(gm);
    }

    public synchronized void abort(PlayerEventInfo gm, boolean error) {
        if (NexusLoader.detailedDebug) {
            this.print("MainEventManager.abort(), error = " + error);
        }
        if (error) {
            if (NexusLoader.detailedDebug) {
                this.print("aborting due to error...");
            }
            this.unspawnRegNpc();
            try {
                this.current.clearEvent();
            }
            catch (Exception e) {
                e.printStackTrace();
                this.clean(null);
                if (NexusLoader.detailedDebug) {
                    this.print("error while aborting - " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
                }
            }
        } else {
            if (NexusLoader.detailedDebug) {
                this.print("aborting due to GM... - _state = " + this._state.toString());
            }
            if (this._state == State.REGISTERING) {
                if (NexusLoader.detailedDebug) {
                    this.print("aborting while in registering state");
                }
                NexusLoader.debug((String)"Event aborted by GM");
                this.unspawnRegNpc();
                this.current.clearEvent();
                this.announce(LanguageEngine.getMsg("announce_regAborted"));
                this._regCountdown.abort();
                if (NexusLoader.detailedDebug) {
                    this.print("event (in registration) successfully aborted");
                }
            } else if (this._state == State.RUNNING) {
                if (NexusLoader.detailedDebug) {
                    this.print("aborting while in running state");
                }
                this.unspawnRegNpc();
                if (this.current != null) {
                    this.current.clearEvent();
                } else {
                    this.clean("in RUNNING state after current was null!!!");
                }
                this.announce(LanguageEngine.getMsg("announce_eventAborted"));
                if (NexusLoader.detailedDebug) {
                    this.print("event (in runtime) successfully aborted");
                }
            } else {
                if (NexusLoader.detailedDebug) {
                    this.print("can't abort event now!");
                }
                gm.sendMessage("Event cannot be aborted now.");
                return;
            }
        }
        if (NexusLoader.detailedDebug) {
            this.print("MainEventManager.abort() finished");
        }
        if (!this.autoSchedulerPaused() && this.autoSchedulerEnabled()) {
            this.scheduler.schedule(-1.0, false);
            if (NexusLoader.detailedDebug) {
                this.print("scheduler enabled, scheduling next event...");
            }
        }
    }

    public void endDueToError(String text) {
        if (NexusLoader.detailedDebug) {
            this.print("starting MainEventManager.endDueToError(): " + text);
        }
        this.announce(text);
        this.abort(null, true);
        if (NexusLoader.detailedDebug) {
            this.print("finished MainEventManager.endDueToError()");
        }
    }

    public void end() {
        if (NexusLoader.detailedDebug) {
            this.print("started MainEventManager.end()");
        }
        this._state = State.TELE_BACK;
        this.schedule(1);
        if (NexusLoader.detailedDebug) {
            this.print("finished MainEventManager.end()");
        }
    }

    private void schedule(int time) {
        if (NexusLoader.detailedDebug) {
            this.print("MainEventManager.schedule(): " + time);
        }
        this.taskFuture = CallBack.getInstance().getOut().scheduleGeneral(this._task, time);
    }

    public void announce(String text) {
        String announcer = "Event Engine";
        if (this.current != null) {
            announcer = this.current.getEventType().getAltTitle();
        }
        if (NexusLoader.detailedDebug) {
            this.print("MainEventManager.announce(): '" + text + "' announcer = " + announcer);
        }
        CallBack.getInstance().getOut().announceToAllScreenMessage(text, announcer);
    }

    public List<PlayerEventInfo> getPlayers() {
        return this._players;
    }

    public int getCounter() {
        return this._counter;
    }

    public String getTimeLeft(boolean digitalClockFormat) {
        try {
            if (this._state == State.REGISTERING) {
                if (digitalClockFormat) {
                    return this._regCountdown.getTimeAdmin();
                }
                return this._regCountdown.getTime();
            }
            if (this._state == State.RUNNING) {
                return this.current.getEstimatedTimeLeft();
            }
            return "N/A";
        }
        catch (Exception e) {
            e.printStackTrace();
            return "<font color=AE0000>Event error</font>";
        }
    }

    public String getMapName() {
        if (this.activeMap == null) {
            return "N/A";
        }
        return this.activeMap.getMapName();
    }

    public String getMapDesc() {
        if (this.activeMap == null) {
            return "N/A";
        }
        if (this.activeMap.getMapDesc() == null || this.activeMap.getMapDesc().length() == 0) {
            return "This map has no description.";
        }
        return this.activeMap.getMapDesc();
    }

    public EventMap getMap() {
        return this.activeMap;
    }

    public int getRunTime() {
        return this.eventRunTime == 0 ? 120 : this.eventRunTime;
    }

    public State getState() {
        return this._state;
    }

    private void msgToAll(String text) {
        if (NexusLoader.detailedDebug) {
            this.print("MainEventManager.msgToAll(): " + text);
        }
        for (PlayerEventInfo player : this._players) {
            player.sendMessage(text);
        }
    }

    public void paralizeAll(boolean para) {
        block4 : {
            try {
                if (NexusLoader.detailedDebug) {
                    this.print("paralyze all called, para = " + para);
                }
                for (PlayerEventInfo player : this._players) {
                    if (!player.isOnline()) continue;
                    player.setIsParalyzed(para);
                    player.setIsInvul(para);
                    player.paralizeEffect(para);
                }
            }
            catch (NullPointerException e) {
                if (!NexusLoader.detailedDebug) break block4;
                this.print("error while paralyzing, " + NexusLoader.getTraceString((StackTraceElement[])e.getStackTrace()));
            }
        }
    }

    public boolean canRegister(PlayerEventInfo player, boolean start) {
        if (player.getLevel() > this.current.getInt("maxLvl")) {
            player.sendMessage(LanguageEngine.getMsg("registering_highLevel"));
            return false;
        }
        if (player.getLevel() < this.current.getInt("minLvl")) {
            player.sendMessage(LanguageEngine.getMsg("registering_lowLevel"));
            return false;
        }
        if (!player.isGM() && start && this.current.getBoolean("dualboxCheck") && this.dualboxDetected(player, this.current.getInt("maxPlayersPerIp"))) {
            player.sendMessage(LanguageEngine.getMsg("registering_sameIp"));
            return false;
        }
        if (!EventManager.getInstance().canRegister(player)) {
            player.sendMessage(LanguageEngine.getMsg("registering_status"));
            return false;
        }
        return true;
    }

    public boolean registerPlayer(PlayerEventInfo player) {
        if (NexusLoader.detailedDebug) {
            this.print(". starting registerPlayer() for " + player.getPlayersName());
        }
        if (this._state != State.REGISTERING) {
            player.sendMessage(LanguageEngine.getMsg("registering_notRegState"));
            return false;
        }
        if (player.isRegistered()) {
            player.sendMessage(LanguageEngine.getMsg("registering_alreadyRegistered"));
            return false;
        }
        int i = EventWarnings.getInstance().getPoints(player);
        if (!(i < EventWarnings.MAX_WARNINGS || player.isGM())) {
            player.sendMessage(LanguageEngine.getMsg("registering_warningPoints", EventWarnings.MAX_WARNINGS, i));
            if (NexusLoader.detailedDebug) {
                this.print("... registerPlayer() for " + player.getPlayersName() + ", player has too many warnings! (" + i + ")");
            }
            return false;
        }
        if (this.canRegister(player, true)) {
            if (!this.getCurrent().canRegister(player)) {
                if (NexusLoader.detailedDebug) {
                    this.print("... registerPlayer() for " + player.getPlayersName() + ", player failed to register on event, event itself didn't allow so!");
                }
                player.sendMessage(LanguageEngine.getMsg("registering_notAllowed"));
                return false;
            }
            if (EventConfig.getInstance().getGlobalConfigBoolean("eventSchemeBuffer")) {
                if (!EventBuffer.getInstance().hasBuffs(player)) {
                    player.sendMessage(LanguageEngine.getMsg("registering_buffs"));
                }
                EventManager.getInstance().getHtmlManager().showSelectSchemeForEventWindow(player, "main", this.getCurrent().getEventType().getAltTitle());
            }
            player.sendMessage(LanguageEngine.getMsg("registering_registered"));
            PlayerEventInfo pi = CallBack.getInstance().getPlayerBase().addInfo(player);
            pi.setIsRegisteredToMainEvent(true, this.current.getEventType());
            List<PlayerEventInfo> list = this._players;
            synchronized (list) {
                this._players.add(pi);
            }
            if (NexusLoader.detailedDebug) {
                this.print("... registerPlayer() for " + player.getPlayersName() + ", player has been registered!");
            }
            return true;
        }
        if (NexusLoader.detailedDebug) {
            this.print("... registerPlayer() for " + player.getPlayersName() + ", player failed to register on event, manager didn't allow so!");
        }
        player.sendMessage(LanguageEngine.getMsg("registering_fail"));
        return false;
    }

    public boolean unregisterPlayer(PlayerEventInfo player, boolean force) {
        if (player == null) {
            return false;
        }
        if (NexusLoader.detailedDebug) {
            this.print(". starting unregisterPlayer() for " + player.getPlayersName() + ", force = " + force);
        }
        if (!EventConfig.getInstance().getGlobalConfigBoolean("enableUnregistrations")) {
            if (NexusLoader.detailedDebug) {
                this.print("... unregisterPlayer()  - unregistrations are not allowed here!");
            }
            if (!force) {
                player.sendMessage(LanguageEngine.getMsg("unregistering_cantUnregister"));
            }
            return false;
        }
        if (!this._players.contains((Object)player)) {
            if (NexusLoader.detailedDebug) {
                this.print("... unregisterPlayer() for " + player.getPlayersName() + " player is not registered");
            }
            if (!force) {
                player.sendMessage(LanguageEngine.getMsg("unregistering_notRegistered"));
            }
            return false;
        }
        if (!(this._state == State.REGISTERING || force)) {
            if (NexusLoader.detailedDebug) {
                this.print("... unregisterPlayer() for " + player.getPlayersName() + " player can't unregister now, becuase _state = " + this._state.toString());
            }
            player.sendMessage(LanguageEngine.getMsg("unregistering_cant"));
            return false;
        }
        player.sendMessage(LanguageEngine.getMsg("unregistering_unregistered"));
        player.setIsRegisteredToMainEvent(false, null);
        CallBack.getInstance().getPlayerBase().eventEnd(player);
        List<PlayerEventInfo> list = this._players;
        synchronized (list) {
            this._players.remove((Object)player);
        }
        if (NexusLoader.detailedDebug) {
            this.print("... unregisterPlayer() for " + player.getPlayersName() + " player has been unregistered");
        }
        if (this.current != null) {
            this.current.playerUnregistered(player);
        }
        return true;
    }

    public boolean dualboxDetected(PlayerEventInfo player) {
        if (!player.isOnline(true)) {
            return false;
        }
        String ip1 = player.getIp();
        if (ip1 == null) {
            return false;
        }
        for (PlayerEventInfo p : this._players) {
            String ip2 = p.getIp();
            if (!ip1.equals(ip2)) continue;
            if (NexusLoader.detailedDebug) {
                this.print("... MainEventManager.dualboxDetected() for " + player.getPlayersName() + ", found dualbox for IP " + player.getIp());
            }
            return true;
        }
        return false;
    }

    public boolean dualboxDetected(PlayerEventInfo player, int maxPerIp) {
        if (!player.isOnline(true)) {
            return false;
        }
        int occurences = 0;
        String ip1 = player.getIp();
        if (ip1 == null) {
            return false;
        }
        for (PlayerEventInfo p : this._players) {
            if (!ip1.equals(p.getIp())) continue;
            ++occurences;
        }
        if (occurences >= maxPerIp) {
            if (NexusLoader.detailedDebug) {
                this.print("... MainEventManager.dualboxDetected() for " + player.getPlayersName() + ", found dualbox for IP (method 2) " + player.getIp() + " maxPerIp " + maxPerIp + " occurences = " + occurences);
            }
            return true;
        }
        return false;
    }

    public AbstractMainEvent getCurrent() {
        return this.current;
    }

    public int getPlayersCount() {
        return this._players.size();
    }

    public void abortAutoScheduler(PlayerEventInfo gm) {
        if (this.autoSchedulerPaused()) {
            this.unpauseAutoScheduler(gm, false);
        }
        if (this.scheduler.abort()) {
            if (gm != null) {
                gm.sendMessage("Automatic event scheduling has been disabled");
            }
            NexusLoader.debug((String)("Automatic scheduler disabled" + (gm != null ? " by a GM." : ".")), (Level)Level.INFO);
        }
        if (NexusLoader.detailedDebug) {
            this.print("aborting auto scheduler, gm is null? " + (gm == null));
        }
        this.autoScheduler = false;
    }

    public void pauseAutoScheduler(PlayerEventInfo gm) {
        if (!EventConfig.getInstance().getGlobalConfigBoolean("enableAutomaticScheduler")) {
            gm.sendMessage("The automatic event scheduler has been disabled in configs.");
            return;
        }
        if (this.scheduler == null) {
            return;
        }
        if (this.getCurrent() != null) {
            gm.sendMessage("There's no pausable delay. Wait till the event ends.");
            return;
        }
        if (!this.autoSchedulerPaused() && this.autoSchedulerEnabled()) {
            if (this.scheduler._future == null) {
                gm.sendMessage("Cannot pause the scheduler now.");
                return;
            }
            if (this.scheduler._future.getDelay(TimeUnit.SECONDS) < 2) {
                gm.sendMessage("Cannot pause now. Event starts in less than 2 seconds.");
                return;
            }
            this.pausedTimeLeft = this.scheduler._future.getDelay(TimeUnit.SECONDS);
            this.scheduler.abort();
            NexusLoader.debug((String)("Automatic scheduler paused" + (gm != null ? " by a GM." : ".")), (Level)Level.INFO);
        } else {
            gm.sendMessage("The scheduler must be enabled.");
        }
    }

    public void unpauseAutoScheduler(PlayerEventInfo gm, boolean run) {
        if (!EventConfig.getInstance().getGlobalConfigBoolean("enableAutomaticScheduler")) {
            gm.sendMessage("The automatic event scheduler has been disabled in configs.");
            return;
        }
        if (this.scheduler == null) {
            return;
        }
        if (this.getCurrent() != null) {
            gm.sendMessage("An event is already running.");
            return;
        }
        if (this.autoSchedulerPaused()) {
            if (run) {
                this.scheduler.schedule(this.pausedTimeLeft, false);
                NexusLoader.debug((String)("Automatic scheduler continues (event in " + this.pausedTimeLeft + " seconds) again after being paused" + (gm != null ? " by a GM." : ".")), (Level)Level.INFO);
            } else {
                NexusLoader.debug((String)("Automatic scheduler unpaused " + (gm != null ? " by a GM." : ".")), (Level)Level.INFO);
            }
            this.pausedTimeLeft = 0.0;
        } else if (gm != null) {
            gm.sendMessage("The scheduler is not paused.");
        }
    }

    public void restartAutoScheduler(PlayerEventInfo gm) {
        if (!EventConfig.getInstance().getGlobalConfigBoolean("enableAutomaticScheduler")) {
            gm.sendMessage("The automatic event scheduler has been disabled in configs.");
            return;
        }
        if (this.autoSchedulerPaused()) {
            this.unpauseAutoScheduler(gm, true);
        } else {
            NexusLoader.debug((String)("Automatic scheduler enabled" + (gm != null ? " by a GM." : ".")), (Level)Level.INFO);
            this.scheduler.schedule(-1.0, false);
        }
        if (gm != null && this.current == null) {
            gm.sendMessage("Automatic event scheduling has been enabled. Next event in " + EventConfig.getInstance().getGlobalConfigInt("delayBetweenEvents") + " minutes.");
        }
    }

    public boolean autoSchedulerEnabled() {
        return this.autoScheduler;
    }

    public boolean autoSchedulerPaused() {
        return this.pausedTimeLeft > 0.0;
    }

    public String getAutoSchedulerDelay() {
        double d = 0.0;
        if (!(this.scheduler._future == null || this.scheduler._future.isDone())) {
            d = this.scheduler._future.getDelay(TimeUnit.SECONDS);
        }
        if (this.autoSchedulerPaused()) {
            d = this.pausedTimeLeft;
        }
        if (d == 0.0) {
            return "N/A";
        }
        if (d >= 60.0) {
            return "" + (int)d / 60 + " min";
        }
        return "" + (int)d + " sec";
    }

    public String getLastEventTime() {
        if (this.lastEvent == 0) {
            return "N/A";
        }
        long time = System.currentTimeMillis();
        long diff = time - this.lastEvent;
        if (diff <= 1000) {
            return "< 1 sec ago";
        }
        if ((diff/=1000) > 60) {
            if ((diff/=60) <= 60) {
                return "" + diff + " min ago";
            }
        } else {
            return "" + diff + " sec ago";
        }
        return "" + (diff/=60) + " hours ago";
    }

    public List<EventScheduleData> getEventScheduleData() {
        return this._eventScheduleData;
    }

    public EventType nextAvailableEvent(boolean testOnly) {
        EventType event = null;
        int lastOrder = 0;
        if (this._lastEvent != null) {
            for (EventScheduleData d : this._eventScheduleData) {
                if (d.getEvent() != this._lastEvent) continue;
                lastOrder = d.getOrder();
            }
        }
        int limit = this._eventScheduleData.size() * 2;
        if (this._eventScheduleData.isEmpty()) {
            return null;
        }
        while (event == null) {
            for (EventScheduleData d : this._eventScheduleData) {
                if (d.getOrder() != lastOrder + 1 || !d.getEvent().isRegularEvent() || !EventConfig.getInstance().isEventAllowed(d.getEvent()) || EventManager.getInstance().getMainEvent(d.getEvent()) == null || EventMapSystem.getInstance().getMapsCount(d.getEvent()) <= 0 || !testOnly && CallBack.getInstance().getOut().random(100) >= d.getChance()) continue;
                event = d.getEvent();
                if (testOnly) break;
                this._lastEvent = event;
                break;
            }
            if (--limit <= 0) break;
            if (lastOrder > this._eventScheduleData.size()) {
                lastOrder = 0;
                continue;
            }
            ++lastOrder;
        }
        return event;
    }

    public EventScheduleData getScheduleData(EventType type) {
        for (EventScheduleData d : this._eventScheduleData) {
            if (!d.getEvent().equals((Object)type)) continue;
            return d;
        }
        return null;
    }

    public EventType getLastEventOrder() {
        return this._lastEvent;
    }

    public EventType getGuessedNextEvent() {
        return this.nextAvailableEvent(true);
    }

    private void addScheduleData(EventType type, int order, int chance, boolean updateInDb) {
        if (type == null) {
            return;
        }
        boolean selectOrder = false;
        if (order == -1 || order > this._eventScheduleData.size()) {
            selectOrder = true;
        } else {
            for (EventScheduleData d : this._eventScheduleData) {
                if (d.getOrder() != order) continue;
                selectOrder = true;
                break;
            }
        }
        if (selectOrder) {
            int freeOrder = -1;
            for (int i = 0; i < this._eventScheduleData.size(); ++i) {
                boolean found = false;
                for (EventScheduleData d : this._eventScheduleData) {
                    if (d.getOrder() != i + 1) continue;
                    found = true;
                    break;
                }
                if (found) continue;
                freeOrder = i + 1;
                break;
            }
            if (freeOrder == -1) {
                int highest = 0;
                for (EventScheduleData d : this._eventScheduleData) {
                    if (d.getOrder() <= highest) continue;
                    highest = d.getOrder();
                }
                order = highest + 1;
            } else {
                order = freeOrder;
            }
        }
        boolean add = true;
        for (EventScheduleData d : this._eventScheduleData) {
            if (d.getEvent() != type) continue;
            add = false;
            break;
        }
        if (add) {
            EventScheduleData data = new EventScheduleData(type, order, chance);
            this._eventScheduleData.add(data);
        }
        if (selectOrder) {
            this.saveScheduleData(type);
            if (updateInDb) {
                if (order != -1) {
                    NexusLoader.debug((String)("Adding wrong-configured/missing " + type.getAltTitle() + " event to EventOrder system with order " + order));
                } else {
                    NexusLoader.debug((String)("Error adding " + type.getAltTitle() + " event to EventOrder system"));
                }
            }
        }
    }

    public void loadScheduleData() {
        Connection con = null;
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("SELECT * FROM nexus_eventorder ORDER BY eventOrder ASC");
            ResultSet rset = statement.executeQuery();
            while (rset.next()) {
                String event = rset.getString("event");
                int order = rset.getInt("eventOrder");
                int chance = rset.getInt("chance");
                for (EventScheduleData d : this._eventScheduleData) {
                    if (d.getOrder() != order) continue;
                    NexusLoader.debug((String)("Duplicate order in EventOrder system for event " + event), (Level)Level.WARNING);
                    order = -1;
                }
                this.addScheduleData(EventType.getType(event), order, chance, false);
            }
            rset.close();
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {}
        }
        for (EventType type : EventType.values()) {
            if (!type.isRegularEvent() || type == EventType.Unassigned || EventManager.getInstance().getEvent(type) == null || this.getScheduleData(type) != null) continue;
            this.addScheduleData(type, -1, 100, true);
        }
    }

    public int saveScheduleData(EventType event) {
        Connection con = null;
        EventScheduleData data = this.getScheduleData(event);
        if (data == null) {
            return -1;
        }
        try {
            con = CallBack.getInstance().getOut().getConnection();
            PreparedStatement statement = con.prepareStatement("REPLACE INTO nexus_eventorder VALUES (?,?,?)");
            statement.setString(1, data.getEvent().getAltTitle());
            statement.setInt(2, data.getOrder());
            statement.setInt(3, data.getChance());
            statement.execute();
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                con.close();
            }
            catch (Exception e) {}
        }
        return data._order;
    }

    private void abortCast() {
        if (NexusLoader.detailedDebug) {
            this.print("aborting cast of all players on the event");
        }
        for (PlayerEventInfo p : this._players) {
            p.abortCasting();
        }
    }

    public void clean(String message) {
        if (NexusLoader.detailedDebug) {
            this.print("MainEventManager() clean: " + message);
        }
        this.current = null;
        this.activeMap = null;
        this.eventRunTime = 0;
        this._players.clear();
        if (message != null) {
            this.announce(message);
        }
        this._state = State.IDLE;
        if (this.regNpcInstance != null) {
            this.regNpcInstance.deleteMe();
            this.regNpcInstance = null;
        }
        this.regNpc = null;
        this.lastEvent = System.currentTimeMillis();
    }

    protected void print(String msg) {
        NexusLoader.detailedDebug((String)msg);
    }

    private class EventTaskScheduler
    implements Runnable {
        private EventTaskScheduler() {
        }

        @Override
        public void run() {
            switch (MainEventManager.this._state) {
                case REGISTERING: {
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - ending registration");
                    }
                    MainEventManager.this.announce(LanguageEngine.getMsg("announce_regClosed"));
                    NexusLoader.debug((String)"Registration phase ended.");
                    for (PlayerEventInfo p : MainEventManager.this._players) {
                        if (MainEventManager.this.canRegister(p, false)) continue;
                        MainEventManager.this.unregisterPlayer(p, true);
                    }
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - players that can't participate were unregistered");
                    }
                    if (!MainEventManager.this.current.canStart()) {
                        if (NexusLoader.detailedDebug) {
                            MainEventManager.this.print("eventtask - can't start - not enought players - " + MainEventManager.this._players.size());
                        }
                        NexusLoader.debug((String)"Not enought participants.");
                        MainEventManager.this.unspawnRegNpc();
                        MainEventManager.this.current.clearEvent();
                        MainEventManager.this.announce(LanguageEngine.getMsg("announce_lackOfParticipants"));
                        if (MainEventManager.this.autoSchedulerPaused() || !MainEventManager.this.autoSchedulerEnabled()) break;
                        MainEventManager.this.scheduler.schedule(-1.0, false);
                        break;
                    }
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - event started");
                    }
                    NexusLoader.debug((String)"Event starts.");
                    MainEventManager.this.announce(LanguageEngine.getMsg("announce_started"));
                    MainEventManager.this.current.initEvent();
                    MainEventManager.this._state = State.RUNNING;
                    MainEventManager.this.msgToAll(LanguageEngine.getMsg("announce_teleport10sec"));
                    int delay = EventConfig.getInstance().getGlobalConfigInt("teleToEventDelay");
                    if (delay <= 0 || delay > 60000) {
                        delay = 10000;
                    }
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - event started, teletoevent delay " + delay);
                    }
                    if (EventConfig.getInstance().getGlobalConfigBoolean("antistuckProtection")) {
                        if (NexusLoader.detailedDebug) {
                            MainEventManager.this.print("eventtask - anti stuck protection ON");
                        }
                        MainEventManager.this.abortCast();
                        if (NexusLoader.detailedDebug) {
                            MainEventManager.this.print("eventtask - aborted cast...");
                        }
                        final int fDelay = delay;
                        CallBack.getInstance().getOut().scheduleGeneral(new Runnable(){

                            @Override
                            public void run() {
                                MainEventManager.this.paralizeAll(true);
                                MainEventManager.this.schedule(fDelay - 1000);
                            }
                        }, 1000);
                        break;
                    }
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - anti stuck protection OFF");
                    }
                    MainEventManager.this.paralizeAll(true);
                    MainEventManager.this.schedule(delay);
                    if (!NexusLoader.detailedDebug) break;
                    MainEventManager.this.print("eventtask - scheduled for next state in " + delay);
                    break;
                }
                case RUNNING: {
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - event started, players teleported");
                    }
                    MainEventManager.this.paralizeAll(false);
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - event started, players unparalyzed");
                    }
                    MainEventManager.this.current.runEvent();
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - event started, event runned");
                    }
                    MainEventManager.this.current.initMap();
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - event started, map initialized");
                    }
                    if (!NexusLoader.detailedDebug) break;
                    MainEventManager.this.print("eventtask - event started, stats given");
                    break;
                }
                case TELE_BACK: {
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - event ending, teleporting back in 10 sec");
                    }
                    MainEventManager.this.current.onEventEnd();
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - on event end");
                    }
                    MainEventManager.this.msgToAll(LanguageEngine.getMsg("announce_teleportBack10sec"));
                    MainEventManager.this._state = State.END;
                    NexusLoader.debug((String)"Teleporting back.");
                    MainEventManager.this.schedule(10000);
                    break;
                }
                case END: {
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - event ended, teleporting back NOW!");
                    }
                    MainEventManager.this.unspawnRegNpc();
                    MainEventManager.this.current.clearEvent();
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - event ended, event cleared!");
                    }
                    MainEventManager.this.announce(LanguageEngine.getMsg("announce_end"));
                    CallBack.getInstance().getOut().purge();
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("eventtask - event ended, after purge");
                    }
                    if (!MainEventManager.this.autoSchedulerPaused() && MainEventManager.this.autoSchedulerEnabled()) {
                        MainEventManager.this.scheduler.schedule(-1.0, false);
                    }
                    NexusLoader.debug((String)"Event ended.");
                }
            }
        }

    }

    private class RegistrationCountdown
    implements Runnable {
        private RegistrationCountdown() {
        }

        private String getTimeAdmin() {
            String mins = "" + MainEventManager.this._counter / 60;
            String secs = MainEventManager.this._counter % 60 < 10 ? "0" + MainEventManager.this._counter % 60 : "" + MainEventManager.this._counter % 60;
            return "" + mins + ":" + secs + "";
        }

        private String getTime() {
            if (MainEventManager.this._counter > 60) {
                int min = MainEventManager.this._counter / 60;
                if (min < 1) {
                    min = 1;
                }
                return "" + min + " minutes";
            }
            return "" + MainEventManager.this._counter + " seconds";
        }

        @Override
        public void run() {
            if (MainEventManager.this._state == State.REGISTERING) {
                switch (MainEventManager.this._counter) {
                    case 60: 
                    case 300: 
                    case 600: 
                    case 1200: 
                    case 1800: {
                        MainEventManager.this.announce(LanguageEngine.getMsg("announce_timeleft_min", MainEventManager.this._counter / 60));
                        break;
                    }
                    case 5: 
                    case 10: 
                    case 30: {
                        MainEventManager.this.announce(LanguageEngine.getMsg("announce_timeleft_sec", MainEventManager.this._counter));
                    }
                }
            }
            if (MainEventManager.this._counter == 0) {
                if (NexusLoader.detailedDebug) {
                    MainEventManager.this.print("registration coutndown counter 0, scheduling next action");
                }
                MainEventManager.this.schedule(1);
            } else {
                MainEventManager.this._counter--;
                MainEventManager.this._regCountdownFuture = CallBack.getInstance().getOut().scheduleGeneral(MainEventManager.this._regCountdown, 1000);
            }
        }

        private void abort() {
            if (NexusLoader.detailedDebug) {
                MainEventManager.this.print("aborting regcoutndown... ");
            }
            if (MainEventManager.this._regCountdownFuture != null) {
                if (NexusLoader.detailedDebug) {
                    MainEventManager.this.print("... regCount is not null");
                }
                MainEventManager.this._regCountdownFuture.cancel(false);
                MainEventManager.this._regCountdownFuture = null;
            } else if (NexusLoader.detailedDebug) {
                MainEventManager.this.print("... regCount is NULL!");
            }
            MainEventManager.this._counter = 0;
        }
    }

    public class EventScheduleData {
        private final EventType _event;
        private int _order;
        private int _chance;

        private EventScheduleData(EventType event, int order, int chance) {
            this._event = event;
            this._order = order;
            this._chance = chance;
        }

        public EventType getEvent() {
            return this._event;
        }

        public int getOrder() {
            return this._order;
        }

        public void setOrder(int c) {
            this._order = c;
        }

        public int getChance() {
            return this._chance;
        }

        public void setChance(int c) {
            this._chance = c;
        }

        public boolean decreaseOrder() {
            boolean done = false;
            for (EventScheduleData d : MainEventManager.this._eventScheduleData) {
                if (d.getEvent() == this.getEvent() || d.getOrder() != this._order + 1) continue;
                d.setOrder(this._order);
                ++this._order;
                MainEventManager.this.saveScheduleData(d.getEvent());
                MainEventManager.this.saveScheduleData(this.getEvent());
                done = true;
                break;
            }
            return done;
        }

        public boolean raiseOrder() {
            boolean done = false;
            for (EventScheduleData d : MainEventManager.this._eventScheduleData) {
                if (d.getEvent() == this.getEvent() || d.getOrder() != this._order - 1) continue;
                d.setOrder(this._order);
                --this._order;
                MainEventManager.this.saveScheduleData(d.getEvent());
                MainEventManager.this.saveScheduleData(this.getEvent());
                done = true;
                break;
            }
            return done;
        }
    }

    public class EventScheduler
    implements Runnable {
        private ScheduledFuture<?> _future;

        @Override
        public void run() {
            try {
                boolean selected = false;
                if (NexusLoader.detailedDebug) {
                    MainEventManager.this.print("starting EventScheduler.run()");
                }
                for (int i = 0; i < EventType.values().length; ++i) {
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("trying to find an event to be started...");
                    }
                    NexusLoader.debug((String)"Trying to find an event to be started:", (Level)Level.INFO);
                    EventType next = EventType.getNextRegularEvent();
                    if (next == null) {
                        if (NexusLoader.detailedDebug) {
                            MainEventManager.this.print("no next event is available. stopping it here, pausing scheduler");
                        }
                        NexusLoader.debug((String)"No next event is aviaible!", (Level)Level.INFO);
                        if (MainEventManager.this.autoSchedulerPaused()) break;
                        this.schedule(-1.0, false);
                        break;
                    }
                    EventMap nextMap = null;
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("next selected event is " + next.getAltTitle());
                    }
                    AbstractMainEvent event = EventManager.getInstance().getMainEvent(next);
                    FastList maps = new FastList();
                    maps.addAll(EventMapSystem.getInstance().getMaps(next).values());
                    Collections.shuffle(maps);
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("no available map for event " + next.getAltTitle());
                    }
                    for (EventMap map : maps) {
                        if (!event.canRun(map)) continue;
                        nextMap = map;
                        break;
                    }
                    if (nextMap == null) {
                        if (!NexusLoader.detailedDebug) continue;
                        MainEventManager.this.print("no available map for event " + next.getAltTitle());
                        continue;
                    }
                    selected = true;
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("selected and starting next event via automatic scheduler");
                    }
                    MainEventManager.this.startEvent(null, next, EventConfig.getInstance().getGlobalConfigInt("defaultRegTime"), nextMap.getMapName(), null, EventConfig.getInstance().getGlobalConfigInt("defaultRunTime"));
                    break;
                }
                if (!selected) {
                    NexusLoader.debug((String)"No event could be started. Check if you have any maps for them and if they are configured properly.");
                    if (NexusLoader.detailedDebug) {
                        MainEventManager.this.print("no event could be started...");
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean abort() {
            if (NexusLoader.detailedDebug) {
                MainEventManager.this.print("aborting event scheduler");
            }
            if (this._future != null) {
                this._future.cancel(false);
                this._future = null;
                return true;
            }
            return false;
        }

        public void schedule(double delay, boolean firstStart) {
            if (!EventConfig.getInstance().getGlobalConfigBoolean("enableAutomaticScheduler")) {
                return;
            }
            if (this._future != null) {
                this._future.cancel(false);
                this._future = null;
            }
            MainEventManager.this.autoScheduler = true;
            if (MainEventManager.this.current == null) {
                if (firstStart) {
                    delay = EventConfig.getInstance().getGlobalConfigInt("firstEventDelay") * 60000;
                    this._future = CallBack.getInstance().getOut().scheduleGeneral(this, (long)delay);
                } else if (delay > -1.0) {
                    this._future = CallBack.getInstance().getOut().scheduleGeneral(this, (long)delay * 1000);
                } else {
                    delay = EventConfig.getInstance().getGlobalConfigInt("delayBetweenEvents") * 60000;
                    this._future = CallBack.getInstance().getOut().scheduleGeneral(this, (long)delay);
                }
                if (NexusLoader.detailedDebug) {
                    MainEventManager.this.print("scheduling next event in " + Math.round(delay / 60000.0) + " minutes.");
                }
                NexusLoader.debug((String)("Next event in " + Math.round(delay / 60000.0) + " minutes."), (Level)Level.INFO);
            } else {
                NexusLoader.debug((String)"Automatic scheduler reeanbled.");
                if (NexusLoader.detailedDebug) {
                    MainEventManager.this.print("reenabling automatic scheduler");
                }
            }
        }
    }

    public class RegNpcLoc {
        public String name;
        public int[] cords;

        public RegNpcLoc(String name, int[] cords) {
            this.name = name;
            this.cords = cords;
        }
    }

    public static enum State {
        IDLE,
        REGISTERING,
        RUNNING,
        TELE_BACK,
        END;
        

        private State() {
        }
    }

}

