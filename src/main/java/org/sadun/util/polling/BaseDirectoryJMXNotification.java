package org.sadun.util.polling;

import javax.management.ObjectName;
import java.io.File;

/**
 * A base class for directory-related JMX notifications.
 *
 * @author Cristiano Sadun
 */
abstract class BaseDirectoryJMXNotification extends BaseJMXNotification {

	public BaseDirectoryJMXNotification(String notificationType, ObjectName pollerName,
	                                    SequenceNumberGenerator sqg, BasePollerEvent evt) {
		super(notificationType,
			pollerName,
			sqg,
			evt,
			mkMsg(evt, "Directory"));
	}

	/**
	 * See {@link BaseDirectoryEvent#getDirectory()}.
	 *
	 * @return the same as {@link BaseDirectoryEvent#getDirectory()}.
	 */
	public File getDirectory() {
		return ((BaseDirectoryEvent) event).getDirectory();
	}

}
