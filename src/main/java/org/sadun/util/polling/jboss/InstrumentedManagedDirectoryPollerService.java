/*
 * Levels Beyond CONFIDENTIAL
 *
 * Copyright 2003 - 2014 Levels Beyond Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Levels Beyond Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Levels Beyond Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is unlawful and strictly forbidden unless prior written permission is obtained
 * from Levels Beyond Incorporated.
 */

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
		mdps = (ManagedDirectoryPollerService) mdp;
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
