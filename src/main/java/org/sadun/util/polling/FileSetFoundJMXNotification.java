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

public class FileSetFoundJMXNotification extends BaseJMXNotification {

	public static final String NOTIFICATION_TYPE = "org.sadun.polling.jmx.file.set.found";

	public FileSetFoundJMXNotification(
		ObjectName pollerName,
		SequenceNumberGenerator sqg,
		FileSetFoundEvent evt) {
		super(NOTIFICATION_TYPE, pollerName, sqg, evt);
	}

	/**
	 * See {@link BaseDirectoryEvent#getDirectory()}.
	 *
	 * @return the same as {@link BaseDirectoryEvent#getDirectory()}.
	 */
	public File getDirectory() {
		return ((FileSetFoundEvent) event).getDirectory();
	}


	/**
	 * See {@link FileSetFoundEvent#getFiles()}.
	 *
	 * @return the same as {@link FileSetFoundEvent#getFiles()}.
	 */
	public File[] getFiles() {
		return ((FileSetFoundEvent) event).getFiles();
	}

}
