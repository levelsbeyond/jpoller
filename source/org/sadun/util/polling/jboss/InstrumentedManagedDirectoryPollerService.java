/*
 * Created on Sep 13, 2004
 */
package org.sadun.util.polling.jboss;

import org.sadun.util.polling.InstrumentedManagedDirectoryPoller;

/**
 * @author Cristiano Sadun
 */
public class InstrumentedManagedDirectoryPollerService
		extends
			InstrumentedManagedDirectoryPoller
		implements
			InstrumentedManagedDirectoryPollerServiceMBean {

	private ManagedDirectoryPollerService mdps;

	public InstrumentedManagedDirectoryPollerService() {
		super(new ManagedDirectoryPollerService());
		mdps=(ManagedDirectoryPollerService)mdp;
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.jboss.ManagedDirectoryPollerServiceMBean#start()
	 */
	public void start() {
		mdps.start();

	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.jboss.ManagedDirectoryPollerServiceMBean#stop()
	 */
	public void stop() {
		mdps.stop();
	}

}
