package org.sadun.util.polling;


/**
 * Indicates that the poller is going to sleep.
 *
 * @version 1.0
 * @author C. Sadun
 */
public class CycleStartEvent extends BasePollerEvent {

    CycleStartEvent(DirectoryPoller poller) {
        super(poller);
    }
}
    