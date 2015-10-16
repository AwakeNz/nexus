/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.delegate.InstanceData
 *  javolution.text.TextBuilder
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main.base;

import cz.nxs.events.engine.base.ConfigModel;
import cz.nxs.events.engine.main.events.AbstractMainEvent;
import cz.nxs.interf.delegate.InstanceData;
import java.util.Map;
import java.util.Set;
import javolution.text.TextBuilder;
import javolution.util.FastMap;

public class MainEventInstanceType {
    private int _id;
    private String _name;
    private String _visibleName;
    private AbstractMainEvent _event;
    private String _params;
    private InstanceData _tempInstance;
    private FastMap<String, ConfigModel> _configs;
    private int _min;
    private int _max;
    private int _rate;

    public MainEventInstanceType(int id, AbstractMainEvent event, String name, String visibleName, String params) {
        this._id = id;
        this._name = name;
        this._visibleName = visibleName;
        this._event = event;
        this._params = params == null ? "" : params;
        this._configs = new FastMap();
    }

    public MainEventInstanceType(int id, AbstractMainEvent event, String name, String visibleName, String params, int min, int max, int rate) {
        this._id = id;
        this._name = name;
        this._visibleName = visibleName;
        this._event = event;
        this._params = params == null ? "" : params;
        this._configs = new FastMap();
        this._min = min;
        this._max = max;
        this._rate = rate;
    }

    @Deprecated
    public int getMinPlayers() {
        return this._min;
    }

    @Deprecated
    public int getMaxPlayers() {
        return this._max;
    }

    public void loadConfigs() {
        if (this._params.length() == 0) {
            return;
        }
        for (String criteria : this._params.split(";")) {
            String name = criteria.split(":")[0];
            String value = criteria.split(":").length > 1 ? criteria.split(":")[1] : "";
            this.setConfig(name, value, false);
        }
    }

    public String encodeParams() {
        TextBuilder tb = new TextBuilder();
        for (Map.Entry e : this._configs.entrySet()) {
            tb.append(((ConfigModel)e.getValue()).encode());
        }
        String result = tb.toString();
        return result.length() > 0 ? result.substring(0, result.length() - 1) : result;
    }

    public void addDefaultConfig(String name, String value, String desc, String defaultVal, ConfigModel.InputType input, String inputParams) {
        this.addParam(name, value, desc, defaultVal, input, inputParams, true);
    }

    public void addParam(String name, String value, String desc, String defaultVal, ConfigModel.InputType input, String inputParams, boolean override) {
        if (this._configs.containsKey((Object)name) && !override) {
            return;
        }
        ConfigModel config = new ConfigModel(name, value, desc, defaultVal, input, inputParams);
        this._configs.put((Object)name, (Object)config);
    }

    public int getId() {
        return this._id;
    }

    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getVisibleName() {
        return this._visibleName;
    }

    public AbstractMainEvent getEvent() {
        return this._event;
    }

    public String getParams() {
        return this._params;
    }

    public void setParams(String p) {
        this._params = p;
    }

    public InstanceData getInstance() {
        return this._tempInstance;
    }

    public void setInstance(InstanceData instance) {
        this._tempInstance = instance;
    }

    public FastMap<String, ConfigModel> getConfigs() {
        return this._configs;
    }

    public void setConfig(String name, String value, boolean addToValue) {
        if (!this._configs.containsKey((Object)name)) {
            return;
        }
        if (!addToValue) {
            ((ConfigModel)this._configs.get((Object)name)).setValue(value);
        } else {
            ((ConfigModel)this._configs.get((Object)name)).addToValue(value);
        }
    }

    public String getConfig(String name) {
        return ((ConfigModel)this._configs.get((Object)name)).getValue();
    }

    public int getConfigInt(String name) {
        String v = this.getConfig(name);
        try {
            return Integer.parseInt(v);
        }
        catch (Exception e) {
            return 0;
        }
    }

    public boolean getConfigBoolean(String name) {
        String v = this.getConfig(name);
        try {
            return Boolean.parseBoolean(v);
        }
        catch (Exception e) {
            return false;
        }
    }

    @Deprecated
    public int getTempRate() {
        return this._rate;
    }

    public int getStrenghtRate() {
        return this.getConfigInt("strenghtRate");
    }
}

