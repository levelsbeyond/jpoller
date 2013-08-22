package org.sadun.util.polling;


/**
 * Indicates that the poller has awakened.
 *
 * @author C. Sadun
 * @version 1.0
 */
public class CycleEndEvent extends BasePollerEvent {

	long[] newBaseTimes;

	CycleEndEvent(DirectoryPoller poller, long[] newBaseTimes) {
		super(poller);
		this.newBaseTimes = newBaseTimes;
	}

	public long[] getNewBaseTime() {
		return newBaseTimes;
	}
}
    