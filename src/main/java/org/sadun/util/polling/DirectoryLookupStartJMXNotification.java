package org.sadun.util.polling;

import javax.management.ObjectName;

public class DirectoryLookupStartJMXNotification extends BaseDirectoryJMXNotification {

	public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.directory.lookup.start";

	DirectoryLookupStartJMXNotification(ObjectName pollerName,
	                                    SequenceNumberGenerator sqg,
	                                    DirectoryLookupStartEvent evt) {
		super(NOTIFICATION_TYPE,
			pollerName,
			sqg, evt);
	}


}
