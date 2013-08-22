package org.sadun.util.polling;

import javax.management.ObjectName;
import java.io.File;

public class FileFoundJMXNotification extends BaseFileJMXNotification {

	public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.file.moved";

	public FileFoundJMXNotification(ObjectName pollerName, SequenceNumberGenerator sqg,
	                                BasePollerEvent evt) {
		super(NOTIFICATION_TYPE, pollerName, sqg, evt);
	}

	/**
	 * See {@link FileFoundEvent#getFile()}.
	 *
	 * @return the same as {@link FileFoundEvent#getFile()}.
	 */
	public File getFile() {
		return ((FileFoundEvent) event).getFile();
	}

	/**
	 * See {@link BaseDirectoryEvent#getDirectory()}.
	 *
	 * @return the same as {@link BaseDirectoryEvent#getDirectory()}.
	 */
	public File getDirectory() {
		return ((FileFoundEvent) event).getDirectory();
	}

}
     