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