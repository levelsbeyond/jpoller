package org.sadun.util.polling;

/**
 * A {@link org.sadun.util.polling.SequenceNumberGenerator} which keeps the sequence
 * in core, on a per-jvm basis.
 * <p>
 * This is used by default by {@link org.sadun.util.polling.ManagedDirectoryPoller}.
 * 
 * @author Cristiano Sadun
 */
public class CoreSequenceNumberGenerator implements SequenceNumberGenerator {

    private volatile long seqNo = 0L; 

    public synchronized long getNextSequenceNumber() {
        return seqNo++;
    }

}
