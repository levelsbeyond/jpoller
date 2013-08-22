package org.sadun.util.polling;

import java.io.File;

import javax.management.ObjectName;

public class ExceptionMovingFileJMXNotification extends BaseJMXNotification {

    public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.exception.file.moving";
    
    private File origin;
    private File destination;
    
    ExceptionMovingFileJMXNotification(
            ObjectName pollerName, SequenceNumberGenerator sqg,
            File file, File dest) {
        super(NOTIFICATION_TYPE, pollerName, sqg, System.currentTimeMillis());
        this.origin=file;
        this.destination=dest;
    }

    /**
     * Return the destination of the move operation which failed to execute.
     * @return the destination of the move operation which failed to execute.
     */
    public File getDestination() {
        return destination;
    }

    /**
     * Return the file which has failed to move.
     * @return the file which has failed to move.
     */
    public File getOrigin() {
        return origin;
    }

    
}
