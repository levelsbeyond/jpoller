package org.sadun.util.polling;

import javax.management.ObjectName;

public class DirectoryLookupEndJMXNotification extends BaseDirectoryJMXNotification {

    public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.directory.lookup.end";
    
    DirectoryLookupEndJMXNotification(ObjectName pollerName, 
            SequenceNumberGenerator sqg, 
            DirectoryLookupEndEvent evt) {
        super(NOTIFICATION_TYPE,
                pollerName,
                sqg, evt);
    }

    
}
