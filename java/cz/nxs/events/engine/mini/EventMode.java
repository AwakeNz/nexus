/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.PlayerEventInfo
 *  javolution.util.FastList
 */
package cz.nxs.events.engine.mini;

import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.base.EventType;
import cz.nxs.events.engine.mini.MiniEventManager;
import cz.nxs.events.engine.mini.ScheduleInfo;
import cz.nxs.events.engine.mini.features.AbstractFeature;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javolution.util.FastList;

public class EventMode
implements Runnable {
    private static final Logger _log = Logger.getLogger(EventMode.class.getName());
    private EventType _event;
    private boolean _gmAllowed;
    private String _name;
    private String _visibleName;
    private int _npcId;
    private List<AbstractFeature> _features;
    private FastList<Integer> _disallowedMaps;
    private ScheduleInfo _scheduleInfo;
    private boolean _running;
    private ScheduledFuture<?> _future;

    public EventMode(EventType event) {
        this._event = event;
        this._name = "Default";
        this._npcId = 0;
        this._visibleName = this._name;
        this._features = new FastList();
        this._disallowedMaps = new FastList();
        this._scheduleInfo = new ScheduleInfo(this._event, this._name);
        this.refreshScheduler();
    }

    @Override
    public void run() {
        if (this._running) {
            this._running = false;
            MiniEventManager manager = EventManager.getInstance().getMiniEvent(this._event, this.getModeId());
            if (manager != null) {
                manager.cleanMe(false);
            }
            this.scheduleRun();
        } else {
            this._running = true;
            this.scheduleStop();
        }
    }

    public void refreshScheduler() {
        if (this.isNonstopRun()) {
            this._running = true;
            return;
        }
        if (this._running) {
            boolean running = false;
            for (ScheduleInfo.RunTime time : this._scheduleInfo.getTimes().values()) {
                if (!time.isActual()) continue;
                running = true;
                this.run();
            }
            if (running) {
                this.scheduleStop();
            } else {
                this.run();
            }
        } else {
            boolean running = false;
            for (ScheduleInfo.RunTime time : this._scheduleInfo.getTimes().values()) {
                if (!time.isActual()) continue;
                running = true;
                this.run();
            }
            if (!running) {
                this.scheduleRun();
            }
        }
    }

    public void scheduleRun() {
        long runTime = this._scheduleInfo.getNextStart(false);
        if (!(this.isNonstopRun() || runTime <= -1)) {
            this._future = CallBack.getInstance().getOut().scheduleGeneral(this, runTime);
        } else {
            this._running = true;
        }
    }

    public void scheduleStop() {
        long endTime = this._scheduleInfo.getEnd(false);
        if (!(this.isNonstopRun() || endTime == -1)) {
            this._future = CallBack.getInstance().getOut().scheduleGeneral(this, endTime);
        }
    }

    public boolean isNonstopRun() {
        return this._scheduleInfo.isNonstopRun();
    }

    public List<AbstractFeature> getFeatures() {
        return this._features;
    }

    public void addFeature(PlayerEventInfo gm, FeatureType type, String parameters) {
        Constructor _constructor = null;
        AbstractFeature feature = null;
        Class[] classParams = new Class[]{EventType.class, PlayerEventInfo.class, String.class};
        try {
            _constructor = Class.forName("cz.nxs.events.engine.mini.features." + type.toString() + "Feature").getConstructor(classParams);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            Object[] objectParams = new Object[]{this._event, gm, parameters};
            Object tmp = _constructor.newInstance(objectParams);
            feature = (AbstractFeature)tmp;
            this._features.add(feature);
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public void addFeature(AbstractFeature feature) {
        this._features.add(feature);
    }

    public boolean checkPlayer(PlayerEventInfo player) {
        for (AbstractFeature feature : this._features) {
            if (feature.checkPlayer(player)) continue;
            return false;
        }
        return true;
    }

    public long getFuture() {
        return this._future == null ? -1 : this._future.getDelay(TimeUnit.MILLISECONDS);
    }

    public FastList<Integer> getDisMaps() {
        return this._disallowedMaps;
    }

    public String getModeName() {
        return this._name;
    }

    public String getVisibleName() {
        if (this._visibleName == null || this._visibleName.length() == 0) {
            return this._name;
        }
        return this._visibleName;
    }

    public int getNpcId() {
        return this._npcId;
    }

    public void setNpcId(int id) {
        this._npcId = id;
    }

    public void setVisibleName(String name) {
        this._visibleName = name;
    }

    public void setModeName(String s) {
        this._name = s;
    }

    public boolean isAllowed() {
        return this._gmAllowed;
    }

    public boolean isRunning() {
        return this._running;
    }

    public void setAllowed(boolean b) {
        this._gmAllowed = b;
    }

    public ScheduleInfo getScheduleInfo() {
        return this._scheduleInfo;
    }

    public int getModeId() {
        for (Map.Entry<Integer, MiniEventManager> e : EventManager.getInstance().getMiniEvents().get((Object)this._event).entrySet()) {
            if (!e.getValue().getMode().getModeName().equals(this.getModeName())) continue;
            return e.getKey();
        }
        return 0;
    }

    public static enum FeatureCategory {
        Configs,
        Items,
        Players;
        

        private FeatureCategory() {
        }
    }

    public static enum FeatureType {
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
        

        private FeatureType() {
        }
    }

}

