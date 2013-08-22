package org.sadun.util.polling.test;

import java.io.File;

import org.sadun.util.polling.BasePollManager;
import org.sadun.util.polling.CycleEndEvent;
import org.sadun.util.polling.CycleStartEvent;
import org.sadun.util.polling.DirectoryLookupEndEvent;
import org.sadun.util.polling.DirectoryLookupStartEvent;
import org.sadun.util.polling.FileFoundEvent;
import org.sadun.util.polling.FileSetFoundEvent;

import com.deltax.util.TimeInterval;

class TestPollManager extends BasePollManager {

	private boolean delay = false;
	private boolean useBusyWaitingForDelay = false;
	private Object lock = new Object();

	public void cycleStarted(CycleStartEvent evt) {
		System.out.println("Poller awakened " + (evt.getPoller().getFilter() == null ? "(no filtering)" : evt.getPoller().getFilter().toString()));
	}

	public void cycleEnded(CycleEndEvent evt) {
		System.out.println("Poller going to sleep");
	}

	public void directoryLookupStarted(DirectoryLookupStartEvent evt) {
		System.out.println("Scanning " + evt.getDirectory());
	}

	public void directoryLookupEnded(DirectoryLookupEndEvent evt) {
		System.out.println("Finished scanning " + evt.getDirectory());
	}

	public void fileSetFound(FileSetFoundEvent evt) {
		File[] files = evt.getFiles();
		for (int i = 0; i < files.length; i++) {
			System.out.println("Found " + files[i].getAbsolutePath());
		}
		// Add artificial delay
		if (delay)
			delay(20000, useBusyWaitingForDelay);

	}

	public void fileFound(FileFoundEvent evt) {
		System.out.println(evt);
		// Add artificial delay
		if (delay)
			delay(20000, useBusyWaitingForDelay);
	}

	public void exceptionDeletingTargetFile(File target) {
		System.out.println("Exception deleting " + target);
	}

	public void exceptionMovingFile(File file, File dest) {
		System.out.println("Could not move " + file + " to " + dest);
	}

	public String toString() {
		return "TestPollManager - a pollmanager which just echoes the received events";
	}

	private void delay(long timeout, boolean busyWaiting) {
		System.out.print("Delaying " + TimeInterval.describe(timeout));
		long start = System.currentTimeMillis(), now = start;
		if (busyWaiting) {
			// Delay with busy waiting - wait is not used on purpose
			System.out.println(" (busy waiting)");
			do {
				now = System.currentTimeMillis();
			} while (now < start + timeout);
		}
		else {
			System.out.println(" (normal waiting)");
			try {
				System.out.println("Current thread: " + Thread.currentThread().getName());
				synchronized (lock) {
					lock.wait(timeout);
				}
			} catch (InterruptedException e) {
				e.printStackTrace(System.out);

			} finally {
				now = System.currentTimeMillis();
			}
		}
		System.out.println();
		System.out
			.println("Delayed " + TimeInterval.describe(now - start)
				+ " (should be approx. "
				+ TimeInterval.describe(timeout) + ")");
		if (now - start < timeout - 500) System.out.println("WARNING: delay is noticeably smaller than expected");

	}
}
