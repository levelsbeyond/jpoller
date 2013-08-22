package org.sadun.util.polling;

import org.sadun.util.polling.DirectoryPoller.ModificationTimeComparator;

/**
 * A comparator which produces a FIFO ordering on the files appearing
 * in a directory (using the last modification time).
 *
 * @author Cristiano Sadun
 */
public class FIFOFileComparator extends ModificationTimeComparator {
    
    public FIFOFileComparator() {
        super(true);
    }

}
