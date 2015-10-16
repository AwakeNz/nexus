/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  javolution.util.FastList
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine;

import cz.nxs.events.NexusLoader;
import cz.nxs.events.engine.EventMapSystem;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class EventItems {
    public Map<Integer, List<Item>> _items;

    private void load() {
        this._items = new FastMap();
        this.loadXml();
    }

    public void reload() {
        this._items.clear();
        NexusLoader.debug((String)"reloading nexus items");
        this.load();
        NexusLoader.debug((String)"reloading nexus items done");
    }

    public void loadXml() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            File file = new File("/data/NexusItems.xml");
            if (!file.exists()) {
                throw new IOException();
            }
            Document doc = factory.newDocumentBuilder().parse(file);
            for (Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling()) {
                if (!"list".equalsIgnoreCase(rift.getNodeName())) continue;
                for (Node set = rift.getFirstChild(); set != null; set = set.getNextSibling()) {
                    if (!"set".equalsIgnoreCase(set.getNodeName())) continue;
                    NamedNodeMap attrs = set.getAttributes();
                    int setId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                    String setName = attrs.getNamedItem("name").getNodeValue();
                    FastList items = new FastList();
                    for (Node item = set.getFirstChild(); item != null; item = item.getNextSibling()) {
                        if (!"item".equalsIgnoreCase(item.getNodeName())) continue;
                        attrs = item.getAttributes();
                        int itemId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                        String itemName = attrs.getNamedItem("name").getNodeValue();
                        Item itemData = new Item(itemId, itemName, setName);
                        items.add((Object)itemData);
                    }
                    this.addItemSet(setId, setName, items);
                }
            }
        }
        catch (Exception e) {
            NexusLoader.debug((String)"error while loading nexus items xml");
            e.printStackTrace();
        }
    }

    public void addItemSet(int id, String name, FastList<Item> items) {
        this._items.put(id, (List<Item>)items);
        NexusLoader.debug((String)("added item set of id " + id + ", name " + name));
    }

    public List<Item> getItemSet(int id) {
        return this._items.get(id);
    }

    public static final EventMapSystem getInstance() {
        return SingletonHolder._instance;
    }

    private static class SingletonHolder {
        protected static final EventMapSystem _instance = new EventMapSystem();

        private SingletonHolder() {
        }
    }

    public class Item {
        int id;
        String itemName;
        String setName;

        Item(int id, String itemName, String setName) {
            this.id = id;
            if (itemName != null) {
                this.itemName = itemName;
            }
            this.setName = setName;
        }
    }

}

