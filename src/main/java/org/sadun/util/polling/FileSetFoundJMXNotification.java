package org.sadun.util.polling;

import javax.management.ObjectName;
import java.io.File;

public class FileSetFoundJMXNotification extends BaseJMXNotification {

	public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.file.set.found";

	public FileSetFoundJMXNotification(
		ObjectName pollerName,
		SequenceNumberGenerator sqg,
		FileSetFoundEvent evt) {
		super(NOTIFICATION_TYPE, pollerName, sqg, evt);
	}

	/**
	 * See {@link BaseDirectoryEvent#getDirectory()}.
	 *
	 * @return the same as {@link BaseDirectoryEvent#getDirectory()}.
	 */
	public File getDirectory() {
		return ((FileSetFoundEvent) event).getDirectory();
	}


	/**
	 * See {@link FileSetFoundEvent#getFiles()}.
	 *
	 * @return the same as {@link FileSetFoundEvent#getFiles()}.
	 */
	public File[] getFiles() {
		return ((FileSetFoundEvent) event).getFiles();
	}

}
