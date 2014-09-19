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

package org.sadun.util.polling;

import com.deltax.util.listener.TimeStampedSignal;

/**
 * A base class for polling-related events
 *
 * @author C. Sadun
 * @version 1.0
 */
public abstract class BasePollerEvent extends TimeStampedSignal {

	protected BasePollerEvent(DirectoryPoller poller) {
		super(poller);
	}

	public DirectoryPoller getPoller() {
		return (DirectoryPoller) getSource();
	}

}
    