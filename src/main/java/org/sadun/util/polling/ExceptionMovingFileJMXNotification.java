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

public class ExceptionMovingFileJMXNotification extends BaseJMXNotification {

	public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.exception.file.moving";

	private File origin;
	private File destination;

	ExceptionMovingFileJMXNotification(
		ObjectName pollerName, SequenceNumberGenerator sqg,
		File file, File dest) {
		super(NOTIFICATION_TYPE, pollerName, sqg, System.currentTimeMillis());
		this.origin = file;
		this.destination = dest;
	}

	/**
	 * Return the destination of the move operation which failed to execute.
	 *
	 * @return the destination of the move operation which failed to execute.
	 */
	public File getDestination() {
		return destination;
	}

	/**
	 * Return the file which has failed to move.
	 *
	 * @return the file which has failed to move.
	 */
	public File getOrigin() {
		return origin;
	}


}
