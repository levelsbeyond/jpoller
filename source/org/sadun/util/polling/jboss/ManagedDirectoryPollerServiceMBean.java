package org.sadun.util.polling.jboss;

import org.sadun.util.polling.ManagedDirectoryPollerMBean;

/**
 * An extension to the {@link org.sadun.util.polling.ManagedDirectoryPollerMBean 
 * ManagedDirectoryPollerMBean} mbean interface which adds JBoss' service
 * interface to the set of exposed methods.
 * 
 * @author cris
 */
public interface ManagedDirectoryPollerServiceMBean
	extends ManagedDirectoryPollerMBean {
		
		public void start();
		public void stop();

}
