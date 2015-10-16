/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  javolution.util.FastMap
 */
package cz.nxs.events.engine.lang;

import cz.nxs.events.NexusLoader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javolution.util.FastMap;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class LanguageEngine {
    private static final String DIRECTORY = "config/nexus language";
    private static Map<String, String> _msgMap = new FastMap();
    private static Map<String, String> _languages = new FastMap();
    private static String _currentLang = "en";

    public static void init() {
        try {
            LanguageEngine.prepare();
            LanguageEngine.load();
        }
        catch (Exception e) {
            NexusLoader.debug((String)"Error while loading language files", (Level)Level.SEVERE);
            e.printStackTrace();
        }
    }

    public static void prepare() throws IOException {
        File folder = new File("config/nexus language");
        if (!folder.exists() || folder.isDirectory()) {
            folder.mkdir();
        }
    }

    public static void load() throws IOException {
        File dir = new File("config/nexus language");
        for (File file : dir.listFiles(new FileFilter(){

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        })) {
            if (!file.getName().startsWith("nexus_lang_")) continue;
            LanguageEngine.loadXml(file, file.getName().substring(11, file.getName().indexOf(".xml")));
        }
        NexusLoader.debug((String)("Loaded " + _languages.size() + " languages."));
    }

    private static void loadXml(File file, String lang) {
        int count = 0;
        String version = "";
        String langName = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setIgnoringComments(true);
        Document doc = null;
        if (file.exists()) {
            try {
                doc = factory.newDocumentBuilder().parse(file);
            }
            catch (Exception e) {
                NexusLoader.debug((String)("Could not load language file for nexus engine - " + lang), (Level)Level.WARNING);
            }
            Node n = doc.getFirstChild();
            NamedNodeMap docAttr = n.getAttributes();
            if (docAttr.getNamedItem("version") != null) {
                version = docAttr.getNamedItem("version").getNodeValue();
            }
            if (docAttr.getNamedItem("lang") != null) {
                langName = docAttr.getNamedItem("lang").getNodeValue();
            }
            if (version != null) {
                NexusLoader.debug((String)("Processing language file for language - " + lang + "; version " + version), (Level)Level.INFO);
            }
            if (!version.equals("2.0")) {
                NexusLoader.debug((String)("Language file for language " + lang + " is not up-to-date with latest version of the engine (" + "2.0" + "). Some newly added messages might not be translated."), (Level)Level.WARNING);
            }
            if (!_languages.containsKey(lang)) {
                _languages.put(lang, langName);
            }
            for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                if (!d.getNodeName().equals("message")) continue;
                NamedNodeMap attrs = d.getAttributes();
                String id = attrs.getNamedItem("id").getNodeValue();
                String text = attrs.getNamedItem("text").getNodeValue();
                _msgMap.put(lang + "_" + id, text);
                ++count;
            }
        }
        NexusLoader.debug((String)("Loaded language file for language " + lang + " " + count + " messages."), (Level)Level.INFO);
    }

    public static String getMsgByLang(String lang, String id) {
        String msg = _msgMap.get(lang + "_" + id);
        if (msg == null) {
            msg = _msgMap.get("en_" + id);
        }
        if (msg == null) {
            NexusLoader.debug((String)("No Msg found: ID " + id + " lang = " + lang), (Level)Level.WARNING);
        }
        return msg;
    }

    public static String getMsg(String id) {
        String lang = LanguageEngine.getLanguage();
        if (lang == null) {
            lang = "en";
        }
        return LanguageEngine.getMsgByLang(lang, id);
    }

    public static /* varargs */ String getMsg(String id, Object ... obs) {
        String msg = LanguageEngine.getMsg(id);
        return LanguageEngine.fillMsg(msg, obs);
    }

    public static /* varargs */ String fillMsg(String msg, Object ... obs) {
        String newMsg = msg;
        for (Object o : obs) {
            int first;
            if (o instanceof Integer || o instanceof Long) {
                first = newMsg.indexOf("%i");
                if (first == -1) continue;
                if (o instanceof Integer) {
                    newMsg = newMsg.replaceFirst("%i", ((Integer)o).toString());
                    continue;
                }
                newMsg = newMsg.replaceFirst("%i", ((Long)o).toString());
                continue;
            }
            if (o instanceof Double) {
                first = newMsg.indexOf("%d");
                if (first == -1) continue;
                newMsg = newMsg.replaceFirst("%d", ((Double)o).toString());
                continue;
            }
            if (!(o instanceof String)) continue;
            first = newMsg.indexOf("%s");
            if (first == -1) continue;
            newMsg = newMsg.replaceFirst("%s", (String)o);
        }
        return newMsg;
    }

    public static void setLanguage(String lang) {
        _currentLang = lang;
    }

    public static String getLanguage() {
        return _currentLang;
    }

    public static Map<String, String> getLanguages() {
        return _languages;
    }

}

