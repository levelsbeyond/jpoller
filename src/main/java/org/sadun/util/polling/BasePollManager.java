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
 * This abstract class implements {@link PollManager PollManager} and can be subclassed to receive of events of interest
 * signalled by a {@link DirectoryPoller DirectoryPoller}.
 * <p/>
 * The implementation of the methods is empty. Subclasses may override the methods they're interested in.
 *
 * @author Cris Sadun
 * @version 1.1
 */
public abstract class BasePollManager implements PollManager {

	public void cycleStarted(CycleStartEvent evt) {
	}

	public void cycleEnded(CycleEndEvent evt) {
	}

	public void directoryLookupStarted(DirectoryLookupStartEvent evt) {
	}

	public void directoryLookupEnded(DirectoryLookupEndEvent evt) {
	}

	public void fileMoved(FileMovedEvent evt) {
	}

	public void fileSetFound(FileSetFoundEvent evt) {
	}

	public void fileFound(FileFoundEvent evt) {
	}

	public void exceptionDeletingTargetFile(File target) {
	}

	public void exceptionMovingFile(File file, File dest) {
	}

}