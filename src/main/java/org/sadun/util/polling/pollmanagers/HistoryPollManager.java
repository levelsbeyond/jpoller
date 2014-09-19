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

import org.sadun.util.polling.BasePollerEvent;
import org.sadun.util.polling.CycleEndEvent;
import org.sadun.util.polling.CycleStartEvent;
import org.sadun.util.polling.DirectoryLookupEndEvent;
import org.sadun.util.polling.DirectoryLookupStartEvent;
import org.sadun.util.polling.FileFoundEvent;
import org.sadun.util.polling.FileMovedEvent;
import org.sadun.util.polling.FileSetFoundEvent;
import org.sadun.util.polling.PollManager;

/**
 * A PollManager which simply keeps a journal of events and processed files.
 * <p/>
 * This class is abstract as it doesn't identify the device where to store the information.
 *
 * @author Cristiano Sadun
 */
public abstract class HistoryPollManager implements PollManager {


	public void cycleEnded(CycleEndEvent evt) {
		storeEvent(evt);

	}

	public void cycleStarted(CycleStartEvent evt) {
		storeEvent(evt);

	}

	public void directoryLookupEnded(DirectoryLookupEndEvent evt) {
		storeEvent(evt);

	}

	public void directoryLookupStarted(DirectoryLookupStartEvent evt) {
		storeEvent(evt);

	}

	public void exceptionDeletingTargetFile(File target) {
		storeAutomoveException(new Exception("delete"), target);

	}

	public void exceptionMovingFile(File file, File dest) {
		storeAutomoveException(new Exception("move"), dest);

	}

	public void fileFound(FileFoundEvent evt) {
		storeEvent(evt);

	}

	public void fileMoved(FileMovedEvent evt) {
		storeEvent(evt);

	}

	public void fileSetFound(FileSetFoundEvent evt) {
		storeEvent(evt);
	}

	protected abstract void storeEvent(BasePollerEvent evt);

	protected abstract void storeAutomoveException(Exception e, File target);
}
