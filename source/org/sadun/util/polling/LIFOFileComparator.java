package org.sadun.util.polling;

import org.sadun.util.polling.DirectoryPoller.ModificationTimeComparator;

/**
 * A comparator which produces a LIFO ordering on the files appearing
 * in a directory (using the last modification time).
 *
 * @author Cristiano Sadun
 */
public class LIFOFileComparator extends ModificationTimeComparator {
    
    public LIFOFileComparator() {
        super(false);
    }

}
