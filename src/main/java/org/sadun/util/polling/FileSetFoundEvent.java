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

/**
 * Indicates that the poller has found a set of files matching the polling criteria.
 *
 * @author C. Sadun
 * @version 1.0
 */
public class FileSetFoundEvent extends BaseDirectoryEvent {

	private File[] files;

	FileSetFoundEvent(DirectoryPoller poller, File dir, String[] paths) {
		super(poller, dir);
		this.files = new File[paths.length];
		for (int i = 0; i < paths.length; i++)
			files[i] = new File(dir, paths[i]);
	}

	public File[] getFiles() {
		return files;
	}
}
    