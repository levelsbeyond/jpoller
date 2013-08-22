package org.sadun.util.polling;

import javax.management.ObjectName;
import java.io.File;

public class ExceptionDeletingTargetFileJMXNotification extends
	BaseJMXNotification {

	public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.exception.file.deleting";

	private File target;

	ExceptionDeletingTargetFileJMXNotification(
		ObjectName pollerName, SequenceNumberGenerator sqg,
		File target) {
		super(NOTIFICATION_TYPE, pollerName, sqg, System.currentTimeMillis());
		this.target = target;
	}

	public File getTarget() {
		return target;
	}

}
