package org.sadun.util.polling;


/**
 * Indicates that the poller is going to sleep.
 *
 * @author C. Sadun
 * @version 1.0
 */
public class CycleStartEvent extends BasePollerEvent {

	CycleStartEvent(DirectoryPoller poller) {
		super(poller);
	}
}
    