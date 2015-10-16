/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.l2j;

import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.html.EventHtmlManager;
import cz.nxs.l2j.INexusOut;
import cz.nxs.l2j.IPlayerBase;
import cz.nxs.l2j.IValues;

public class CallBack {
    private INexusOut _out = null;
    private IPlayerBase _playerBase = null;
    private IValues _values = null;

    public INexusOut getOut() {
        return this._out;
    }

    public IPlayerBase getPlayerBase() {
        return this._playerBase;
    }

    public IValues getValues() {
        return this._values;
    }

    public void setHtmlManager(EventHtmlManager manager) {
        EventManager.getInstance().setHtmlManager(manager);
    }

    public void setNexusOut(INexusOut out) {
        this._out = out;
    }

    public void setPlayerBase(IPlayerBase base) {
        this._playerBase = base;
    }

    public void setValues(IValues values) {
        this._values = values;
    }

    public static final CallBack getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final CallBack _instance = new CallBack();

        private SingletonHolder() {
        }
    }

}

