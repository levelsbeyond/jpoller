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

package org.sadun.util.polling.pollmanagers;

import java.io.File;

import org.sadun.util.polling.CycleEndEvent;
import org.sadun.util.polling.CycleStartEvent;
import org.sadun.util.polling.DirectoryLookupEndEvent;
import org.sadun.util.polling.DirectoryLookupStartEvent;
import org.sadun.util.polling.FileFoundEvent;
import org.sadun.util.polling.FileMovedEvent;
import org.sadun.util.polling.FileSetFoundEvent;
import org.sadun.util.polling.PollManager;

/**
 * This pollmanager adds suspension capabilities to another pollmanager.
 *
 * @author Cristiano Sadun
 */
public class SuspendablePollManager implements PollManager {

	private PollManager pgmt;
	private boolean suspended;
	private boolean processExceptionEvents;

	public SuspendablePollManager(PollManager pgmt, boolean processExceptionEvents) {
		this.suspended = false;
		this.pgmt = pgmt;
		this.processExceptionEvents = processExceptionEvents;
	}

	public SuspendablePollManager(PollManager pgmt) {
		this(pgmt, false);
	}

	public void cycleStarted(CycleStartEvent evt) {
		if (suspended) return;
		pgmt.cycleStarted(evt);
	}

	public void cycleEnded(CycleEndEvent evt) {
		if (suspended) return;
		pgmt.cycleEnded(evt);
	}

	public void directoryLookupStarted(DirectoryLookupStartEvent evt) {
		if (suspended) return;
		pgmt.directoryLookupStarted(evt);

	}

	public void directoryLookupEnded(DirectoryLookupEndEvent evt) {
		if (suspended) return;
		pgmt.directoryLookupEnded(evt);
	}

	public void fileFound(FileFoundEvent evt) {
		if (suspended) return;
		pgmt.fileFound(evt);
	}

	public void fileMoved(FileMovedEvent evt) {
		if (suspended) return;
		pgmt.fileMoved(evt);
	}

	public void fileSetFound(FileSetFoundEvent evt) {
		if (suspended) return;
		pgmt.fileSetFound(evt);
	}

	public void exceptionMovingFile(File file, File dest) {
		if (suspended && !processExceptionEvents) return;
		pgmt.exceptionMovingFile(file, dest);
	}

	public void exceptionDeletingTargetFile(File target) {
		if (suspended && !processExceptionEvents) return;
		pgmt.exceptionDeletingTargetFile(target);
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

}
