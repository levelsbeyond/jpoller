package org.sadun.util.polling;

import javax.management.ObjectName;

public class BaseFileJMXNotification extends BaseJMXNotification {

	public BaseFileJMXNotification(String notificationType,
	                               ObjectName pollerName, SequenceNumberGenerator sqg,
	                               BasePollerEvent evt) {
		super(notificationType, pollerName, sqg, evt, mkMsg(evt, "File"));
	}


}
