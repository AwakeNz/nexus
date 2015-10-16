/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.debug;

import cz.nxs.debug.OutputHandler;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class LogFormatter
extends Formatter {
    public final OutputHandler outputHandler;

    public LogFormatter(OutputHandler guilogoutputhandler) {
        this.outputHandler = guilogoutputhandler;
    }

    @Override
    public String format(LogRecord logrecord) {
        StringBuilder stringbuilder = new StringBuilder();
        Level level = logrecord.getLevel();
        stringbuilder.append("[" + level.getLocalizedName().toUpperCase() + "] ");
        stringbuilder.append(logrecord.getMessage());
        stringbuilder.append('\n');
        Throwable throwable = logrecord.getThrown();
        if (throwable != null) {
            StringWriter stringwriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(stringwriter));
            stringbuilder.append(stringwriter.toString());
        }
        return stringbuilder.toString();
    }
}

