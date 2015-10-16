package cz.nxs.events;

import cz.nxs.debug.DebugConsole;
import cz.nxs.events.engine.EventConfig;
import cz.nxs.events.engine.EventManager;
import cz.nxs.events.engine.EventMapSystem;
import cz.nxs.events.engine.EventRewardSystem;
import cz.nxs.events.engine.EventWarnings;
import cz.nxs.events.engine.lang.LanguageEngine;
import cz.nxs.events.engine.main.MainEventManager;
import cz.nxs.events.engine.main.OldStats;
import cz.nxs.events.engine.main.base.MainEventInstanceTypeManager;
import cz.nxs.events.engine.stats.EventStatsManager;
import cz.nxs.interf.NexusEvents;
import cz.nxs.interf.PlayerEventInfo;
import cz.nxs.l2j.CallBack;
import cz.nxs.l2j.INexusOut;
import cz.nxs.playervalue.PlayerValueEngine;
import java.awt.GraphicsEnvironment;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import javolution.text.TextBuilder;
import javolution.util.FastSet;

public class NexusLoader
{
    public static final class NexusBranch
    {
        public static final NexusBranch Freya;
        public static final NexusBranch Hi5;
        public static final NexusBranch Hi5Priv;
        public static final NexusBranch Final;
        public double _newestVersion;
        private static final NexusBranch $VALUES[];

        public static NexusBranch[] values()
        {
            return (NexusBranch[])$VALUES.clone();
        }

        public static NexusBranch valueOf(String name)
        {
            return NexusBranch.Freya;
        }

        static 
        {
            Freya = new NexusBranch("Freya", 0, 2.1000000000000001D);
            Hi5 = new NexusBranch("Hi5", 1, 2.1000000000000001D);
            Hi5Priv = new NexusBranch("Hi5Priv", 2, 2.1000000000000001D);
            Final = new NexusBranch("Final", 3, 2.1000000000000001D);
            $VALUES = (new NexusBranch[] {
                Freya, Hi5, Hi5Priv, Final
            });
        }

        private NexusBranch(String s, int i, double interfaceVersion)
        {
            _newestVersion = interfaceVersion;
        }
    }


    public static final String version = "2.21";
    private static FileWriter fileWriter;
    private static final SimpleDateFormat _toFileFormat = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
    public static boolean debugConsole = false;
    public static boolean detailedDebug = false;
    public static boolean detailedDebugToConsole = false;
    public static boolean logToFile = false;
    public static DebugConsole debug;
    private static NexusBranch _branch;
    private static String _desc;
    private static String _serialPath;
    private static double _interfaceVersion;
    private static String _key;
    private static boolean loaded = false;
    private static boolean loading = false;
    private static boolean tryReconnect = false;
    private static boolean _instances;
    private static String _libsFolder;
    private static boolean _limitedHtml;
    private static Socket commandSocket = null;
    private static PrintWriter commandOut = null;
    private static BufferedReader commandIn = null;
    private static boolean _gmsDebugging = false;
    private static Set _gmsDebuggingSet = new FastSet();
    public static int DEBUG_CHAT_CHANNEL_CLASSIC = 7;
    public static int DEBUG_CHAT_CHANNEL = 6;
    private static File debugFile;
    private static File detailedDebugFile;

    public NexusLoader()
    {
    }

    public static final void init(NexusBranch l2branch, double interfaceVersion, String desc, boolean allowInstances, String libsFolder, String serialPath, boolean limitedHtml)
    {
        String key;
        InputStreamReader reader;
        BufferedReader bReader;
        if(_branch == null)
        {
            _branch = l2branch;
            _interfaceVersion = interfaceVersion;
            _serialPath = serialPath;
        }
        if(_key != null)
        {
        }
        key = null;
        reader = null;
        bReader = null;
        InputStream is = null;
		try
		{
			is = new FileInputStream(new File(_serialPath));
		}
		catch (FileNotFoundException e3)
		{
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
        reader = new InputStreamReader(is, Charset.defaultCharset());
        bReader = new BufferedReader(reader);
        String line;
        try
		{
			if((line = bReader.readLine()) != null)
			{
			    key = line;
			}
		}
		catch (IOException e2)
		{
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
        IOException e;
        try
        {
            is.close();
            if(reader != null)
            {
                reader.close();
            }
            if(bReader != null)
            {
                bReader.close();
            }
        }
        // Misplaced declaration of an exception variable
        catch(IOException e1)
        {
            e1.printStackTrace();
        }
        try
        {
            is.close();
            if(reader != null)
            {
                reader.close();
            }
            if(bReader != null)
            {
                bReader.close();
            }
        }
        // Misplaced declaration of an exception variable
        catch(IOException e1)
        {
            e1.printStackTrace();
        }
        try
        {
            is.close();
            if(reader != null)
            {
                reader.close();
            }
            if(bReader != null)
            {
                bReader.close();
            }
        }
        catch(IOException e1)
        {
            e1.printStackTrace();
        }
        _key = key;
        debug((new StringBuilder()).append("License key: ").append(_key != null ? _key : "-").toString());
        if(_key != null && isValid(_key))
        {
            loading = true;
            EventConfig.getInstance().loadGlobalConfigs();
            debugConsole = EventConfig.getInstance().getGlobalConfigBoolean("debug");
            if(!GraphicsEnvironment.isHeadless())
            {
                debugConsole = false;
            }
            if(debugConsole)
            {
                loadDebugConsole(true);
            }
            String fileName = createDebugFile();
            if(fileName != null)
            {
                debug((new StringBuilder()).append("Nexus Engine: Debug messages are stored in '").append(fileName).append("'").toString());
            }
            debug("Nexus Engine: Thanks for using a legal version of the engine.");
            _desc = desc;
            _instances = allowInstances;
            _libsFolder = libsFolder;
            _limitedHtml = limitedHtml;
            debug("Nexus Engine: Loading engine version 2.21...");
            debug((new StringBuilder()).append("Nexus Engine: Using ").append(_desc).append(" interface (for engine of v").append(interfaceVersion).append(").").toString());
            if(interfaceVersion != l2branch._newestVersion)
            {
                debug("Nexus Engine: Your interface is outdated for this engine!!! Please update it.", Level.SEVERE);
            }
            OldStats.getInstance();
            NexusEvents.loadHtmlManager();
            logToFile = EventConfig.getInstance().getGlobalConfigBoolean("logToFile");
            detailedDebug = EventConfig.getInstance().getGlobalConfigBoolean("detailedDebug");
            detailedDebugToConsole = EventConfig.getInstance().getGlobalConfigBoolean("detailedDebugToConsole");
            LanguageEngine.init();
            EventManager.getInstance();
            EventConfig.getInstance().loadEventConfigs();
            EventMapSystem.getInstance().loadMaps();
            EventRewardSystem.getInstance();
            EventManager.getInstance().getMainEventManager().loadScheduleData();
            MainEventInstanceTypeManager.getInstance();
            EventStatsManager.getInstance();
            EventWarnings.getInstance();
            PlayerValueEngine.getInstance();
            loaded = true;
            debug("Nexus Engine: Version 2.21 successfully loaded.");
        } else
        {
            loaded = false;
        }
        return;
    }

    private static final boolean isValid(String key)
    {
        return true;
    }

    public static final void loadDebugConsole(boolean onServerStart)
    {
        if(!GraphicsEnvironment.isHeadless())
        {
            DebugConsole.initGui();
            DebugConsole.info("Nexus Engine: Debug console initialized.");
        } else
        if(!onServerStart)
        {
            System.out.println("Debug console can't be opened in this environment.");
        }
    }

    public static final boolean isDebugging(PlayerEventInfo gm)
    {
        if(!_gmsDebugging)
        {
            return false;
        } else
        {
            return _gmsDebuggingSet.contains(gm);
        }
    }

    public static final void addGmDebug(PlayerEventInfo gm)
    {
        if(!_gmsDebugging)
        {
            _gmsDebugging = true;
        }
        _gmsDebuggingSet.add(gm);
    }

    public static final void removeGmDebug(PlayerEventInfo gm)
    {
        if(!_gmsDebugging)
        {
            return;
        }
        _gmsDebuggingSet.remove(gm);
        if(_gmsDebuggingSet.isEmpty())
        {
            _gmsDebugging = false;
        }
    }

    public static final void debug(String msg, Level level)
    {
        if(!msg.startsWith("Nexus ") && !msg.startsWith("nexus"))
        {
            msg = (new StringBuilder()).append("Nexus Engine: ").append(msg).toString();
        }
        if(debugConsole)
        {
            DebugConsole.log(level, msg);
        } else
        {
            System.out.println(msg);
        }
        if(_gmsDebugging)
        {
            sendToGms(msg, level, false);
        }
        writeToFile(level, msg, false);
    }

    public static final void debug(String msg)
    {
        if(!msg.startsWith("Nexus ") && !msg.startsWith("nexus"))
        {
            msg = (new StringBuilder()).append("Nexus Engine: ").append(msg).toString();
        }
        try
        {
            if(debugConsole)
            {
                DebugConsole.info(msg);
            } else
            {
                System.out.println(msg);
            }
        }
        catch(Exception e) { }
        try
        {
            if(_gmsDebugging)
            {
                sendToGms(msg, Level.INFO, false);
            }
        }
        catch(Exception e) { }
        writeToFile(Level.INFO, msg, false);
    }

    public static final void sendToGms(String msg, Level level, boolean detailed)
    {
        try
        {
            PlayerEventInfo gm;
            for(Iterator i$ = _gmsDebuggingSet.iterator(); i$.hasNext(); gm.creatureSay((new StringBuilder()).append("*").append(detailed ? msg : msg.substring(14)).append("  (").append(level.toString()).append(")").toString(), detailed ? "DD" : "DEBUG", detailed ? DEBUG_CHAT_CHANNEL : DEBUG_CHAT_CHANNEL_CLASSIC))
            {
                gm = (PlayerEventInfo)i$.next();
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static final void detailedDebug(String msg)
    {
        if(!msg.startsWith("DD "))
        {
            msg = (new StringBuilder()).append("DD:  ").append(msg).toString();
        }
        try
        {
            if(_gmsDebugging)
            {
                sendToGms(msg, Level.INFO, true);
            }
        }
        catch(Exception e) { }
        try
        {
            if(detailedDebugToConsole && debugConsole)
            {
                DebugConsole.log(Level.INFO, msg);
            }
        }
        catch(Exception e) { }
        writeToFile(Level.INFO, msg, true);
    }

    public static final boolean allowInstances()
    {
        return _instances;
    }

    public static final String getLibsFolderName()
    {
        return _libsFolder;
    }

    public static final boolean isLimitedHtml()
    {
        return _limitedHtml;
    }

    private static final String createDebugFile()
    {
        String path = "log/nexus";
        File folder = new File(path);
        if(!folder.exists() && !folder.mkdir())
        {
            path = "log";
        }
        debugFile = new File((new StringBuilder()).append(path).append("/NexusEvents.log").toString());
        if(!debugFile.exists())
        {
            try
            {
                debugFile.createNewFile();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        int id = 0;
        File arr$[] = folder.listFiles();
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            File f = arr$[i$];
            if(!f.getName().startsWith("NexusEvents_detailed"))
            {
                continue;
            }
            try
            {
                String name = f.getName().substring(0, f.getName().length() - 4);
                int id2 = Integer.getInteger(name.substring(21)).intValue();
                if(id2 > id)
                {
                    id = id2;
                }
            }
            catch(Exception e) { }
        }

        id++;
        detailedDebugFile = new File((new StringBuilder()).append(path).append("/NexusEvents_detailed_").append(id).append(".log").toString());
        if(detailedDebugFile.exists())
        {
            try
            {
                detailedDebugFile.delete();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        if(!detailedDebugFile.exists())
        {
            try
            {
                detailedDebugFile.createNewFile();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        return detailedDebugFile.getAbsolutePath();
    }

    public static void writeToFile(Level level, String msg, boolean detailed)
    {
        if(!detailed && !logToFile)
        {
            return;
        }
        if(!detailed)
        {
            try
			{
				fileWriter = new FileWriter(debugFile, true);
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        } else
        {
            try
			{
				fileWriter = new FileWriter(detailedDebugFile, true);
			}
			catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
        try
		{
			fileWriter.write((new StringBuilder()).append(_toFileFormat.format(new Date())).append(":  ").append(msg).append(" (").append(level.getLocalizedName()).append(")\r\n").toString());
		}
		catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        Exception e;
        try
        {
            fileWriter.close();
        }
        // Misplaced declaration of an exception variable
        catch(Exception e2) { }
        if(debugConsole)
        {
        }
        try
        {
            fileWriter.close();
        }
        // Misplaced declaration of an exception variable
        catch(Exception e3) { }
        try
        {
            fileWriter.close();
        }
        catch(Exception e4) { }
    }

    public static String getTraceString(StackTraceElement trace[])
    {
        TextBuilder sbString = TextBuilder.newInstance();
        StackTraceElement arr$[] = trace;
        int len$ = arr$.length;
        for(int i$ = 0; i$ < len$; i$++)
        {
            StackTraceElement element = arr$[i$];
            sbString.append(element.toString()).append("\n");
        }

        String result = sbString.toString();
        TextBuilder.recycle(sbString);
        return result;
    }

    public static void shutdown()
    {
        EventWarnings.getInstance().saveData();
    }

    public static boolean loaded()
    {
        return loaded;
    }

    public static boolean loadedOrBeingLoaded()
    {
        return loading;
    }


}
