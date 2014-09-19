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

import javax.management.ObjectName;
import java.io.File;

/**
 * A base class for directory-related JMX notifications.
 *
 * @author Cristiano Sadun
 */
abstract class BaseDirectoryJMXNotification extends BaseJMXNotification {

	public BaseDirectoryJMXNotification(String notificationType, ObjectName pollerName,
	                                    SequenceNumberGenerator sqg, BasePollerEvent evt) {
		super(notificationType,
			pollerName,
			sqg,
			evt,
			mkMsg(evt, "Directory"));
	}

	/**
	 * See {@link BaseDirectoryEvent#getDirectory()}.
	 *
	 * @return the same as {@link BaseDirectoryEvent#getDirectory()}.
	 */
	public File getDirectory() {
		return ((BaseDirectoryEvent) event).getDirectory();
	}

}
