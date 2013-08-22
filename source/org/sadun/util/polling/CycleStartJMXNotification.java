package org.sadun.util.polling;

import javax.management.ObjectName;

public class CycleStartJMXNotification extends BaseJMXNotification {
   
    public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.cycle.start";
    
    CycleStartJMXNotification(ObjectName pollerName, 
            SequenceNumberGenerator sqg, 
            CycleStartEvent evt) {
        super(NOTIFICATION_TYPE,
                pollerName,
                sqg,
                evt);
    }
    
    

}
