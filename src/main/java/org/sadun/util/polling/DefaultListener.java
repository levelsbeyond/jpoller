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

import com.deltax.util.listener.ExceptionListener;
import com.deltax.util.listener.ExceptionSignal;
import com.deltax.util.listener.Signal;

class DefaultListener implements ExceptionListener {

	private PollManager mgt;
	DirectoryPoller poller;

	DefaultListener(DirectoryPoller poller, PollManager mgt) {
		this.poller = poller;
		this.mgt = mgt;
	}

	public void receive(Signal evt) {

		if (poller.isShuttingDown()) {
			//System.out.println("(Poller shutting down: ignoring event "+evt+")");
			return; // Ignore events if the poller is shutting down
		}

		if (evt instanceof CycleStartEvent) {
			mgt.cycleStarted((CycleStartEvent) evt);
			return;
		}
		else if (evt instanceof CycleEndEvent) {
			mgt.cycleEnded((CycleEndEvent) evt);
			return;
		}
		else if (evt instanceof DirectoryLookupStartEvent) {
			mgt.directoryLookupStarted((DirectoryLookupStartEvent) evt);
			return;
		}
		else if (evt instanceof DirectoryLookupEndEvent) {
			mgt.directoryLookupEnded((DirectoryLookupEndEvent) evt);
			return;
		}
		else if (evt instanceof FileMovedEvent) {
			mgt.fileMoved((FileMovedEvent) evt);
			return;
		}
		else if (evt instanceof FileSetFoundEvent) {
			mgt.fileSetFound((FileSetFoundEvent) evt);
			return;
		}
		else if (evt instanceof FileFoundEvent) {
			mgt.fileFound((FileFoundEvent) evt);
			return;
		}
		else
			throw new RuntimeException("Unexpected signal " + evt);
	}

	public void receiveException(ExceptionSignal evt) {
		if (poller.isDebugExceptions()) {
			System.out.println("Exception incurred by directory poller:");
			System.out.println();
			evt.getException().printStackTrace(System.out);
			System.out.println();
			System.out.println();
		}
		if (evt.getException() instanceof DirectoryPoller.AutomoveDeleteException) {
			DirectoryPoller.AutomoveException e = (DirectoryPoller.AutomoveException) evt.getException();
			mgt.exceptionDeletingTargetFile(e.getDestination());
		}
		else if (evt.getException() instanceof DirectoryPoller.AutomoveException) {
			DirectoryPoller.AutomoveException e = (DirectoryPoller.AutomoveException) evt.getException();
			mgt.exceptionMovingFile(e.getOrigin(), e.getDestination());
		}
		else
			throw new RuntimeException("Unexpected exception " + evt.getException());
	}

}