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
