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

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Indicates that the poller has found a file. Note that an {@link FileSetFoundEvent FileSetFoundEvent} containing the
 * same file is always notified before this.
 * <p/>
 * <p>This event is signalled only if the {@link DirectoryPoller DirectoryPoller} returns <b>true</b> when invoking
 * {@link DirectoryPoller#isSendSingleFileEvent() isSendSingleFileEvent()}.
 *
 * @author C. Sadun
 * @version 1.0
 */
public class FileFoundEvent extends BaseDirectoryEvent {

	private File file;
	private static DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

	FileFoundEvent(DirectoryPoller poller, File file) {
		super(poller, file.getParentFile());
		this.file = file;
	}

	public File getFile() {
		return file;
	}

	public String toString() {
		return "File found in: " + getDirectory().getAbsolutePath() + ": "
			+ file.getName() + ", size: " + (int) (file.length() / 1024) + "K ,mod. "
			+ df.format(new Date(file.lastModified()));
	}
}
    