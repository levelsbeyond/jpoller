package org.sadun.util.polling;



/**
 * Indicates that the poller has awakened.
 *
 * @version 1.0
 * @author C. Sadun
 */
public class CycleEndEvent extends BasePollerEvent {

    long [] newBaseTimes;

    CycleEndEvent(DirectoryPoller poller, long [] newBaseTimes) {
        super(poller);
        this.newBaseTimes=newBaseTimes;
    }

    public long [] getNewBaseTime() { return newBaseTimes; }
}
    