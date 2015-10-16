/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.interf.NexusEvents
 */
package cz.nxs.debug;

import cz.nxs.debug.DebugConsole;
import cz.nxs.interf.NexusEvents;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextField;

public class CommandListener
implements ActionListener {
    private final JTextField textField;

    public CommandListener(DebugConsole servergui, JTextField jtextfield) {
        this.textField = jtextfield;
    }

    @Override
    public void actionPerformed(ActionEvent actionevent) {
        String s = this.textField.getText().trim();
        if (s.length() > 0) {
            NexusEvents.consoleCommand((String)s);
            DebugConsole.userCmd("[COMMAND] " + s + "\n");
        }
        this.textField.setText("");
    }
}

