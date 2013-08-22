package org.sadun.util.polling;

import javax.management.ObjectName;
import java.io.File;

import org.sadun.util.MovedFile;

public class FileMovedJMXNotification extends BaseJMXNotification {

	public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.file.moved";

	public FileMovedJMXNotification(ObjectName pollerName,
	                                SequenceNumberGenerator sqg, BasePollerEvent evt) {
		super(NOTIFICATION_TYPE, pollerName, sqg, evt, mkMsg(evt, "MovedFile"));
	}

	/**
	 * See {@link FileMovedEvent#getOriginalPath()}.
	 *
	 * @return the same as {@link FileMovedEvent#getOriginalPath()}.
	 */
	public File getOriginalPath() {
		return ((FileMovedEvent) event).getOriginalPath();
	}

	/**
	 * See {@link FileMovedEvent#getPath()}.
	 *
	 * @return the same as {@link FileMovedEvent#getPath()}.
	 */
	public File getPath() {
		return ((FileMovedEvent) event).getPath();
	}

	/**
	 * See {@link FileMovedEvent#getPath()}.
	 *
	 * @return the same as {@link FileMovedEvent#getPath()}.
	 */
	public MovedFile getMovedFile() {
		return ((FileMovedEvent) event).getMovedFile();
	}

}
