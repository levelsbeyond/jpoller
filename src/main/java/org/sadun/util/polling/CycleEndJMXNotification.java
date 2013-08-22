package org.sadun.util.polling;

import javax.management.ObjectName;

/**
 * A JMX notification class equivalent to a {@link org.sadun.util.polling.CycleEndEvent}
 *
 * @author Cristiano Sadun
 */
public class CycleEndJMXNotification extends BaseJMXNotification {


	public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.cycle.end";

	CycleEndJMXNotification(ObjectName pollerName,
	                        SequenceNumberGenerator sqg,
	                        CycleEndEvent evt) {
		super(NOTIFICATION_TYPE,
			pollerName,
			sqg,
			evt,
			"New base time: " + evt.getNewBaseTime());
	}

	/**
	 * Return the new base time (see {@link CycleEndEvent#getNewBaseTime()}.
	 *
	 * @return the new base time (see {@link CycleEndEvent#getNewBaseTime()}.
	 */
	public long[] getNewBaseTime() {
		return ((CycleEndEvent) event).getNewBaseTime();
	}

}
