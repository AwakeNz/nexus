/*
 * Decompiled with CFR 0_102.
 */
package cz.nxs.debug;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.JTextArea;

public class OutputHandler extends Handler
{
	private final int[] a = new int[1024];
	private int i = 0;
	Formatter logFormatter;
	private final JTextArea textArea;
	
	public OutputHandler(JTextArea jtextarea)
	{
		this.logFormatter = new LogFormatter(this);
		this.setFormatter(this.logFormatter);
		this.textArea = jtextarea;
	}
	
	@Override
	public void close()
	{
	}
	
	@Override
	public void flush()
	{
	}
	
	@Override
	public void publish(LogRecord logrecord)
	{
		try
		{
			int length = this.textArea.getDocument().getLength();
			this.textArea.append(this.logFormatter.format(logrecord));
			this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
			int j = this.textArea.getDocument().getLength() - length;
			if (this.a[this.i] != 0)
			{
				this.textArea.replaceRange("", 0, this.a[this.i]);
			}
			this.a[this.i] = j;
			this.i = (this.i + 1) % 1024;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
