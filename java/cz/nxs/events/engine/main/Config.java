/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.NexusOut
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.main;

import cz.nxs.interf.NexusOut;
import java.io.File;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Config {
    private Logger _log = Logger.getLogger(Config.class.getName());
    private FastMap<Integer, FastMap<String, String>> config = new FastMap();
    private FastMap<Integer, FastMap<String, FastMap<Integer, int[]>>> positions = new FastMap();
    private FastMap<Integer, FastMap<String, int[]>> colors = new FastMap();
    private FastMap<Integer, FastMap<String, FastList<Integer>>> restrictions = new FastMap();
    private FastMap<Integer, FastMap<String, String>> messages = new FastMap();

    public static Config getInstance() {
        return SingletonHolder._instance;
    }

    private Config() {
        this.loadConfigs();
    }

    private void addColor(int id, String owner, int[] color) {
        if (!this.colors.containsKey((Object)id)) {
            this.colors.put((Object)id, (Object)new FastMap());
        }
        ((FastMap)this.colors.get((Object)id)).put((Object)owner, (Object)color);
    }

    private void addPosition(int id, String owner, int x, int y, int z, int radius) {
        if (!this.positions.containsKey((Object)id)) {
            this.positions.put((Object)id, (Object)new FastMap());
        }
        if (!((FastMap)this.positions.get((Object)id)).containsKey((Object)owner)) {
            ((FastMap)this.positions.get((Object)id)).put((Object)owner, (Object)new FastMap());
        }
        ((FastMap)((FastMap)this.positions.get((Object)id)).get((Object)owner)).put((Object)(((FastMap)((FastMap)this.positions.get((Object)id)).get((Object)owner)).size() + 1), (Object)new int[]{x, y, z, radius});
    }

    private void addProperty(int id, String propName, String value) {
        if (!this.config.containsKey((Object)id)) {
            this.config.put((Object)id, (Object)new FastMap());
        }
        ((FastMap)this.config.get((Object)id)).put((Object)propName, (Object)value);
    }

    private void addRestriction(int id, String type, String ids) {
        if (!this.restrictions.containsKey((Object)id)) {
            this.restrictions.put((Object)id, (Object)new FastMap());
        }
        FastList idlist = new FastList();
        StringTokenizer st = new StringTokenizer(ids, ",");
        while (st.hasMoreTokens()) {
            idlist.add((Object)Integer.parseInt(st.nextToken()));
        }
        ((FastMap)this.restrictions.get((Object)id)).put((Object)type, (Object)idlist);
    }

    private void addMessage(int id, String name, String value) {
        if (!this.messages.containsKey((Object)id)) {
            this.messages.put((Object)id, (Object)new FastMap());
        }
        ((FastMap)this.messages.get((Object)id)).put((Object)name, (Object)value);
    }

    public boolean getBoolean(int event, String propName) {
        try {
            if (!this.config.containsKey((Object)event)) {
                this._log.warning("Event: Try to get a property of a non existing event: ID: " + event);
                return false;
            }
            if (((FastMap)this.config.get((Object)event)).containsKey((Object)propName)) {
                return Boolean.parseBoolean((String)((FastMap)this.config.get((Object)event)).get((Object)propName));
            }
            this._log.warning("Event: Try to get a non existing property: " + propName);
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int[] getColor(int event, String owner) {
        if (!this.colors.containsKey((Object)event)) {
            this._log.warning("Event: Try to get a color of a non existing event: ID: " + event);
            return new int[]{255, 255, 255};
        }
        if (((FastMap)this.colors.get((Object)event)).containsKey((Object)owner)) {
            return (int[])((FastMap)this.colors.get((Object)event)).get((Object)owner);
        }
        this._log.warning("Event: Try to get a non existing color: " + owner);
        return new int[]{255, 255, 255};
    }

    public int getInt(int event, String propName) {
        if (!this.config.containsKey((Object)event)) {
            this._log.warning("Event: Try to get a property of a non existing event: ID: " + event);
            return -1;
        }
        if (((FastMap)this.config.get((Object)event)).containsKey((Object)propName)) {
            return Integer.parseInt((String)((FastMap)this.config.get((Object)event)).get((Object)propName));
        }
        this._log.warning("Event: Try to get a non existing property: " + propName);
        return -1;
    }

    public int[] getPosition(int event, String owner, int num) {
        if (!this.positions.containsKey((Object)event)) {
            this._log.warning("Event: Try to get a position of a non existing event: ID: " + event);
            return new int[0];
        }
        if (!((FastMap)this.positions.get((Object)event)).containsKey((Object)owner)) {
            this._log.warning("Event: Try to get a position of a non existing owner: " + owner);
            return new int[0];
        }
        if (!(((FastMap)((FastMap)this.positions.get((Object)event)).get((Object)owner)).containsKey((Object)num) || num == 0)) {
            this._log.warning("Event: Try to get a non existing position: " + num);
            return new int[0];
        }
        if (num == 0) {
            return (int[])((FastMap)((FastMap)this.positions.get((Object)event)).get((Object)owner)).get((Object)(NexusOut.random((int)((FastMap)((FastMap)this.positions.get((Object)event)).get((Object)owner)).size()) + 1));
        }
        return (int[])((FastMap)((FastMap)this.positions.get((Object)event)).get((Object)owner)).get((Object)num);
    }

    public FastList<Integer> getRestriction(int event, String type) {
        if (!this.restrictions.containsKey((Object)event)) {
            this._log.warning("Event: Try to get a restriction of a non existing event: ID: " + event);
            return null;
        }
        if (((FastMap)this.restrictions.get((Object)event)).containsKey((Object)type)) {
            return (FastList)((FastMap)this.restrictions.get((Object)event)).get((Object)type);
        }
        this._log.warning("Event: Try to get a non existing restriction: " + type);
        return null;
    }

    public String getString(int event, String propName) {
        if (!this.config.containsKey((Object)event)) {
            this._log.warning("Event: Try to get a property of a non existing event: ID: " + event);
            return null;
        }
        if (((FastMap)this.config.get((Object)event)).containsKey((Object)propName)) {
            return (String)((FastMap)this.config.get((Object)event)).get((Object)propName);
        }
        this._log.warning("Event: Try to get a non existing property: " + propName);
        return null;
    }

    protected String getMessage(int event, String name) {
        if (!this.messages.containsKey((Object)event)) {
            this._log.warning("Event: Try to get a message of a non existing event: ID: " + event);
            return null;
        }
        if (((FastMap)this.messages.get((Object)event)).containsKey((Object)name)) {
            return (String)((FastMap)this.messages.get((Object)event)).get((Object)name);
        }
        this._log.warning("Event: Try to get a non existing message: " + name);
        return null;
    }

    private void loadConfigs() {
        File configFile = new File("./config/Events.xml");
        Document doc = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringComments(true);
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(configFile);
            for (Node root = doc.getFirstChild(); root != null; root = root.getNextSibling()) {
                if (!"events".equalsIgnoreCase(root.getNodeName())) continue;
                for (Node event = root.getFirstChild(); event != null; event = event.getNextSibling()) {
                    if (!"event".equalsIgnoreCase(event.getNodeName())) continue;
                    NamedNodeMap eventAttrs = event.getAttributes();
                    int eventId = Integer.parseInt(eventAttrs.getNamedItem("id").getNodeValue());
                    for (Node property = event.getFirstChild(); property != null; property = property.getNextSibling()) {
                        String name;
                        String value;
                        NamedNodeMap propAttrs;
                        String owner;
                        if ("property".equalsIgnoreCase(property.getNodeName())) {
                            propAttrs = property.getAttributes();
                            name = propAttrs.getNamedItem("name").getNodeValue();
                            value = propAttrs.getNamedItem("value").getNodeValue();
                            this.addProperty(eventId, name, value);
                        }
                        if ("position".equalsIgnoreCase(property.getNodeName())) {
                            propAttrs = property.getAttributes();
                            owner = propAttrs.getNamedItem("owner").getNodeValue();
                            String x = propAttrs.getNamedItem("x").getNodeValue();
                            String y = propAttrs.getNamedItem("y").getNodeValue();
                            String z = propAttrs.getNamedItem("z").getNodeValue();
                            String radius = propAttrs.getNamedItem("radius").getNodeValue();
                            this.addPosition(eventId, owner, Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z), Integer.parseInt(radius));
                        }
                        if ("color".equalsIgnoreCase(property.getNodeName())) {
                            propAttrs = property.getAttributes();
                            owner = propAttrs.getNamedItem("owner").getNodeValue();
                            int r = Integer.parseInt(propAttrs.getNamedItem("r").getNodeValue());
                            int g = Integer.parseInt(propAttrs.getNamedItem("g").getNodeValue());
                            int b = Integer.parseInt(propAttrs.getNamedItem("b").getNodeValue());
                            this.addColor(eventId, owner, new int[]{r, g, b});
                        }
                        if ("restriction".equalsIgnoreCase(property.getNodeName())) {
                            propAttrs = property.getAttributes();
                            String type = propAttrs.getNamedItem("type").getNodeValue();
                            String ids = propAttrs.getNamedItem("ids").getNodeValue();
                            this.addRestriction(eventId, type, ids);
                        }
                        if (!"message".equalsIgnoreCase(property.getNodeName())) continue;
                        propAttrs = property.getAttributes();
                        name = propAttrs.getNamedItem("name").getNodeValue();
                        value = propAttrs.getNamedItem("value").getNodeValue();
                        this.addMessage(eventId, name, value);
                    }
                }
            }
        }
        catch (Exception e) {
            // empty catch block
        }
    }

    private static class SingletonHolder {
        protected static final Config _instance = new Config();

        private SingletonHolder() {
        }
    }

}

