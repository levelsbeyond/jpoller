package org.sadun.util.polling.pollmanagers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sadun.util.polling.BasePollerEvent;
import org.sadun.util.polling.DirectoryLookupEndEvent;
import org.sadun.util.polling.DirectoryLookupStartEvent;
import org.sadun.util.polling.DirectoryPoller;
import org.sadun.util.polling.FileFoundEvent;
import org.sadun.util.polling.FileSetFoundEvent;

import com.deltax.util.TimeInterval;

/**
 * @author Cristiano Sadun
 */
public class LoggerHistoryPollManager extends HistoryPollManager {

	private Logger logger;

	public LoggerHistoryPollManager(String loggerName) {
		this.logger = Logger.getLogger(loggerName);
	}

	public LoggerHistoryPollManager() {
		this(DirectoryPoller.class.getName());
	}

	public LoggerHistoryPollManager(Logger logger) {
		this.logger = logger;
	}

	protected void storeAutomoveException(Exception e, File target) {
		assert "delete".equals(e.getMessage()) || "move".equals(e.getMessage());
		boolean isDelete = "delete".equals(e.getMessage());
		try {
			logger
				.log(isDelete ? Level.WARNING : Level.SEVERE, e
					.getMessage()
					+ " operation failed for file "
					+ target.getCanonicalPath());
		} catch (IOException e1) {
			logger.log(isDelete ? Level.WARNING : Level.SEVERE, e.getMessage()
				+ " operation failed for file " + target.getAbsolutePath());
		}
	}

	protected void storeEvent(BasePollerEvent evt) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		boolean interesting = false;

		pw.print(evt.getTime() + "\t" + TimeInterval.describe(evt.getTime())
			+ "\t" + evt.getPoller().getName() + "\t");
		if (evt.getPoller().isSendSingleFileEvent()) {
			// Ignore file set found
			if (evt instanceof FileFoundEvent)
				pw.print("Found: ["
					+ ((FileFoundEvent) evt).getFile().getName() + "]");
			interesting = true;
		}
		else {
			if (evt instanceof FileSetFoundEvent) {
				pw.print("Found: [");
				File[] files = ((FileSetFoundEvent) evt).getFiles();
				for (int i = 0; i < files.length; i++) {
					pw.print(files[i].getName());
					if (i < files.length - 1)
						pw.print(",");
				}
				pw.print("]");
			}
			interesting = true;
		}
		if (evt instanceof DirectoryLookupStartEvent)
			try {
				pw.print("Looking up ["
					+ ((DirectoryLookupStartEvent) evt).getDirectory()
					.getCanonicalPath() + "]");
			} catch (IOException e) {
				pw.print("Looking up ["
					+ ((DirectoryLookupStartEvent) evt).getDirectory()
					.getAbsolutePath() + "]");
			} finally {
				interesting = true;
			}

		if (evt instanceof DirectoryLookupEndEvent)
			try {
				pw.print("Finished looking up ["
					+ ((DirectoryLookupEndEvent) evt).getDirectory()
					.getCanonicalPath() + "]");
			} catch (IOException e) {
				pw.print("Finished looking up ["
					+ ((DirectoryLookupEndEvent) evt).getDirectory()
					.getAbsolutePath() + "]");
			} finally {
				interesting = true;
			}

		if (interesting)
			logger.log(Level.INFO, sw.toString());
	}
}
