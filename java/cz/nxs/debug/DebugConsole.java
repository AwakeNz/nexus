/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.debug;

import cz.nxs.debug.CommandListener;
import cz.nxs.debug.OutputHandler;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionListener;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class DebugConsole
extends JComponent {
    private static final long serialVersionUID = 1;
    public static Logger logger = Logger.getLogger("NexusDebug");
    public static JTextArea jtextarea;

    public static void initGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception exception) {
            // empty catch block
        }
        DebugConsole console = new DebugConsole();
        JFrame jframe = new JFrame("Nexus Events 1.50b_p1");
        jframe.add(console);
        jframe.pack();
        jframe.setLocationRelativeTo(null);
        jframe.setVisible(true);
    }

    public DebugConsole() {
        this.setPreferredSize(new Dimension(520, 648));
        this.setLayout(new BorderLayout());
        try {
            this.add((Component)this.getLogComponent(), "Center");
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private JComponent getLogComponent() {
        JPanel jpanel = new JPanel(new BorderLayout());
        jtextarea = new JTextArea();
        logger.addHandler(new OutputHandler(jtextarea));
        JScrollPane jscrollpane = new JScrollPane(jtextarea, 22, 30);
        jtextarea.setEditable(false);
        JTextField jtextfield = new JTextField();
        jtextfield.addActionListener(new CommandListener(this, jtextfield));
        jpanel.add((Component)jscrollpane, "Center");
        jpanel.add((Component)jtextfield, "South");
        jpanel.setBorder(new TitledBorder(new EtchedBorder(), "Logs"));
        return jpanel;
    }

    public static void userCmd(String s) {
        jtextarea.append(s);
    }

    public static void log(Level level, String s) {
        logger.log(level, s);
    }

    public static void info(String s) {
        logger.info(s);
    }

    public static void warning(String s) {
        logger.warning(s);
    }

    public static void severe(String s) {
        logger.severe(s);
    }
}

