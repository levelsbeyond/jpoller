package org.sadun.util.polling.pollmanagers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.sadun.util.polling.BasePollerEvent;
import org.sadun.util.polling.CycleEndEvent;
import org.sadun.util.polling.CycleStartEvent;
import org.sadun.util.polling.DirectoryLookupEndEvent;
import org.sadun.util.polling.DirectoryLookupStartEvent;
import org.sadun.util.polling.FileFoundEvent;
import org.sadun.util.polling.FileSetFoundEvent;

import com.deltax.util.TimeInterval;

/**
 * @author Cristiano Sadun
 */
public class FileHistoryPollManager extends HistoryPollManager {

	private File file;
	private PrintWriter writer;
	private SimpleDateFormat sdf;
	private boolean firstWrite;
	private long startTime = -1;

	public FileHistoryPollManager() throws IOException {
		this(new File(".", "pollmanager.journal.txt"));
	}

	public FileHistoryPollManager(File file) throws IOException {
		this.file = file;
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	}

	private void open() throws IOException {
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file, false)));
		} catch (IOException e) {
			writer = null;
			throw e;
		} finally {
			firstWrite = true;
		}
	}

	protected void storeAutomoveException(Exception e, File target) {
		assert "delete".equals(e.getMessage()) || "move".equals(e.getMessage());
		boolean isDelete = "delete".equals(e.getMessage());
		try {

			log("FAILURE: " + e.getMessage()
				+ " operation failed for file " + target.getCanonicalPath());
		} catch (IOException e1) {
			log("FAILURE: " + e.getMessage()
				+ " operation failed for file " + target.getAbsolutePath());
		}
	}

	protected void storeEvent(BasePollerEvent evt) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		boolean interesting = false;

		if (startTime == -1 && evt instanceof CycleStartEvent)
			startTime = evt.getTime();

		pw.print(sdf.format(new Date(evt.getTime())) + "\t(" + TimeInterval.describe(evt.getTime() - startTime)
			+ ")\t" + evt.getPoller().getName() + "\t");
		if (evt.getPoller().isSendSingleFileEvent()) {
			// Ignore file set found
			if (evt instanceof FileFoundEvent) {
				pw.print("Found file: ["
					+ ((FileFoundEvent) evt).getFile().getName());
				pw.print("]");
				interesting = true;
			}
		}
		else {
			if (evt instanceof FileSetFoundEvent) {
				try {
					pw.println("Found files in " + ((FileSetFoundEvent) evt).getDirectory().getCanonicalPath() + ": [");
				} catch (IOException e1) {
					pw.println("Found files in " + ((FileSetFoundEvent) evt).getDirectory().getAbsolutePath() + ": [");
				}
				File[] files = ((FileSetFoundEvent) evt).getFiles();
				for (int i = 0; i < files.length; i++) {
					pw.println("  " + files[i].getName());
				}
				pw.print("]");
				interesting = true;
			}
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
		if (evt instanceof CycleEndEvent) newLine();

		if (interesting)
			log(sw.toString());
	}

	/**
	 *
	 */
	private synchronized void newLine() {
		log(System.getProperty("line.separator"), false);

	}

	/**
	 * @param info
	 * @param string
	 */
	private synchronized void log(String msg) {
		log(msg, true);
	}

	private synchronized void log(String msg, boolean considerFirstWrite) {
		if (writer == null)
			try {
				open();
			} catch (IOException e1) {
				e1.printStackTrace();
				return;
			}
		if (considerFirstWrite) {
			if (!firstWrite) {
				writer.println();
			}
			else firstWrite = false;
		}
		writer.print(msg);
		writer.flush();
		if (writer.checkError()) {
			try {
				open();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
