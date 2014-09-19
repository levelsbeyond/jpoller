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

package org.sadun.util.polling.test;

import org.sadun.util.polling.PollManager;
import org.sadun.util.polling.PollManagerFactory;
import org.sadun.util.polling.pollmanagers.LoggerHistoryPollManager;

/**
 * A {@link org.sadun.util.polling.PollManagerFactory PollManagerFactory} which produces one instance of {@link
 * org.sadun.util.polling.test.TestPollManager TestPollManager}
 *
 * @author cris
 */
public class TestPollManagerFactory implements PollManagerFactory {

	/**
	 * @see org.sadun.util.polling.PollManagerFactory#createPollManagers()
	 */
	public PollManager[] createPollManagers(String name) {
		// In this particular factory, the MBean name is not used
		System.out.println("Creating testPollManager for MBean <" + name + ">");
		return new PollManager[]{new TestPollManager(), new LoggerHistoryPollManager("test_f")};
	}

	/**
	 * @see org.sadun.util.polling.PollManagerFactory#getDescription()
	 */
	public String getDescription() {
		return "A sample factory producing one instance of org.sadun.util.polling.test.TestPollManager";
	}

}
