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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.deltax.util.listener.BaseSignalSourceThread;
import com.deltax.util.listener.ExceptionSignal;

import org.sadun.util.BidirectionalComparator;
import org.sadun.util.PathNormalizer;
import org.sadun.util.Terminable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Referenced classes of package org.sadun.util.polling:
//            DefaultListener, CycleStartEvent, DirectoryLookupStartEvent, FileMovedEvent,
//            FileSetFoundEvent, FileFoundEvent, DirectoryLookupEndEvent, CycleEndEvent,
//            PollManager

public class DirectoryPoller extends BaseSignalSourceThread implements Terminable {

	private final static Logger logger = LoggerFactory.getLogger(DirectoryPoller.class);

	public static final String DEFAULT_AUTOMOVE_DIRECTORY = "received";
	private static int counter = 0;
	private volatile boolean shutdownRequested;
	private FilenameFilter filter;
	private File dirs[];
	private long baseTime[];
	private boolean verbose;
	private boolean timeBasedOnLastLookup;
	protected List<PollManager> pollManagersList;
	private boolean autoMove;
	private Map<File, File> autoMoveDirs;
	private FilenameFilter originalFilter;
	private long pollInterval;
	private boolean startBySleeping;
	private boolean sendSingleFileEvent;
	private int currentDir;
	private Comparator filesSortComparator;
	private boolean bypassLockedFiles;
	private volatile boolean sleeping;
	private volatile boolean debugExceptions;
	private Map<String, Long> fileSizeMap;

	public static class FileSizeComparator extends BidirectionalComparator {

		protected final long getComparisonValue(File f1, File f2) {
			return f1.length() - f2.length();
		}

		public FileSizeComparator(boolean ascending) {
			super(ascending);
		}
	}

	public static class ModificationTimeComparator extends BidirectionalComparator {

		protected final long getComparisonValue(File f1, File f2) {
			return f1.lastModified() - f2.lastModified();
		}

		public ModificationTimeComparator(boolean ascending) {
			super(ascending);
		}
	}

	public static final class NullFilenameFilter implements FilenameFilter {

		public boolean accept(File dir, String name) {
			return true;
		}

		public String toString() {
			return "null filter";
		}

		public NullFilenameFilter() {
		}
	}

	private class TimeFilter implements FilenameFilter {

		public boolean accept(File dir, String name) {
			File f = new File(dir, name);
			if (f.isDirectory())
				return false;
			if (f.lastModified() <= baseTime[currentDir]) {
				if (logger.isDebugEnabled())
					logger.debug((new StringBuilder()).append(name).append("(").append(f.lastModified()).append("): out of base time (")
							.append(baseTime[currentDir]).append("), ignoring").toString());
				return false;
			}
			if (logger.isDebugEnabled())
				logger.debug((new StringBuilder()).append(name).append("(").append(f.lastModified()).append("): older than base time (")
						.append(baseTime[currentDir]).append("), accepted").toString());
			return additionalFilter.accept(dir, name);
		}

		private FilenameFilter additionalFilter;
		final DirectoryPoller this$0;

		public TimeFilter(FilenameFilter additionalFilter) {
			super();
			this$0 = DirectoryPoller.this;
			this.additionalFilter = additionalFilter;
		}
	}

	private static class DirectoryFilter implements FilenameFilter {

		public boolean accept(File dir, String name) {
			if ((new File(dir, name)).isDirectory() && (systemDirectoryNames.length == 0 || Arrays.binarySearch(systemDirectoryNames, name) > 0))
				return false;
			if (additionalFilter != null) {
				return additionalFilter.accept(dir, name);
			}
			else {
				return true;
			}

		}

		public String toString() {
			return (new StringBuilder()).append("Directory filter over a ").append(additionalFilter).toString();
		}

		FilenameFilter getAdditionalFilter() {
			return additionalFilter;
		}

		String systemDirectoryNames[];
		FilenameFilter additionalFilter;

		DirectoryFilter(String systemDirectoryNames[], FilenameFilter additionalFilter) {
			this.systemDirectoryNames = systemDirectoryNames;
			this.additionalFilter = additionalFilter;
		}
	}

	public class AutomoveDeleteException extends AutomoveException {

		final DirectoryPoller this$0;

		AutomoveDeleteException(File origin, File dest, String msg) {
			super(origin, dest, msg);
			this$0 = DirectoryPoller.this;
		}
	}

	public class AutomoveException extends Exception {

		public DirectoryPoller getPoller() {
			return DirectoryPoller.this;
		}

		public File getOrigin() {
			return origin;
		}

		public File getDestination() {
			return dest;
		}

		private File origin;
		private File dest;
		final DirectoryPoller this$0;

		AutomoveException(File origin, File dest, String msg) {
			super(msg);
			this$0 = DirectoryPoller.this;
			this.origin = origin;
			this.dest = dest;
			if (logger.isDebugEnabled())
				logger.debug((new StringBuilder()).append("[Automove] Exception: ").append(msg).toString());
		}
	}

	public DirectoryPoller(File dirs[], FilenameFilter filter) {
		this(dirs, filter, false);
	}

	public DirectoryPoller(File dirs[]) {
		this(dirs, new NullFilenameFilter());
	}

	public DirectoryPoller(File directory, FilenameFilter filter) {
		this(new File[] {
				directory
		}, filter);
	}

	public DirectoryPoller(File directory) {
		this(new File[] {
				directory
		});
	}

	public DirectoryPoller(FilenameFilter filter) {
		this(filter, false);
	}

	public DirectoryPoller() {
		this(new NullFilenameFilter());
	}

	public DirectoryPoller(File dirs[], FilenameFilter filter, boolean timeBased) {
		this(dirs, filter, new String[0], timeBased);
	}

	public DirectoryPoller(File dirs[], FilenameFilter filter, String systemSubdirectoryNames[], boolean timeBased) {
		shutdownRequested = false;
		verbose = System.getProperty("org.sadun.verbose") != null;
		timeBasedOnLastLookup = true;
		pollManagersList = new ArrayList<>();
		autoMove = false;
		autoMoveDirs = new HashMap<>();
		pollInterval = 10000L;
		startBySleeping = false;
		sendSingleFileEvent = false;
		currentDir = -1;
		filesSortComparator = null;
		bypassLockedFiles = false;
		sleeping = false;
		fileSizeMap = new HashMap<>();
		setName((new StringBuilder()).append("directory-poller-").append(counter++).toString());
		setDirectories(dirs);
		originalFilter = new DirectoryFilter(systemSubdirectoryNames, filter);
		setTimeBased(timeBased);
		baseTime = new long[dirs.length];
	}

	public DirectoryPoller(File directory, FilenameFilter filter, boolean timeBased) {
		this(new File[] { directory }, filter, timeBased);
	}

	public DirectoryPoller(FilenameFilter filter, boolean timeBased) {
		this(new File[0], filter, timeBased);
	}

	public void addDirectory(File dir) {
		File originalDirs[] = getDirectories();
		for (final File originalDir : originalDirs) {
			if (originalDir.getAbsoluteFile().equals(dir.getAbsoluteFile())) {
				return;
			}
		}

		File dirs[] = new File[getDirectories().length + 1];
		System.arraycopy(originalDirs, 0, dirs, 0, originalDirs.length);
		dirs[originalDirs.length] = dir;
		setDirectories(dirs);
	}

	public void removeDirectory(File dir) {
		File originalDirs[] = getDirectories();
		File dirs[] = new File[originalDirs.length - 1];
		boolean removed = false;
		int c = 0;
		for (final File originalDir : originalDirs) {
			if (originalDir.equals(dir)) {
				removed = true;
			}
			else {
				dirs[c++] = originalDir;
			}
		}

		if (!removed) {
			throw new IllegalArgumentException((new StringBuilder()).append(dir).append(" is not a controlled directory").toString());
		}
		else {
			setDirectories(dirs);
		}
	}

	public void setDirectories(File dirs[]) {
		if (isAlive() && !isSleeping())
			throw new IllegalStateException("Can't call setDirectories when the poller is running and not sleeping");
		if (dirs != null) {
			for (final File dir : dirs) {
				if (!dir.isDirectory()) {
					throw new IllegalArgumentException((new StringBuilder()).append(dir).append(" is not a directory").toString());
				}
			}

			this.dirs = dirs;
			baseTime = new long[dirs.length];
		}
	}

	public File[] getDirectories() {
		return dirs;
	}

	public void setAutoMove(boolean v) {
		autoMove = v;
	}

	public boolean getAutoMove() {
		return autoMove;
	}

	public File getAutoMoveDirectory(File directory) throws IllegalArgumentException {
		directory = PathNormalizer.normalize(directory);
		File f = autoMoveDirs.get(directory);
		if (f == null) {
			f = new File(directory, "received");
			setAutoMoveDirecetory(directory, f);
		}
		return f;
	}

	public void setAutoMoveDirectory(File directory, File autoMoveDirectory)
			throws IllegalArgumentException, IllegalStateException {
		if (isAlive()) {
			throw new IllegalStateException("auto-move directories cannot be set once the poller has started");
		}
		else {
			setAutoMoveDirecetory(directory, autoMoveDirectory);
		}
	}

	private void setAutoMoveDirecetory(File directory, File autoMoveDirectory)
			throws IllegalArgumentException {
		File normalizedDirectory = PathNormalizer.normalize(directory);
		checkIfManaged(normalizedDirectory);
		autoMoveDirs.put(normalizedDirectory, PathNormalizer.normalize(autoMoveDirectory));
	}

	protected void checkIfManaged(File directory) {
		for (final File dir : dirs) {
			if (PathNormalizer.normalize(dir).equals(directory)) {
				return;
			}
		}

		throw new IllegalArgumentException((new StringBuilder()).append("The directory ").append(directory)
				.append(" is not under control of the directory poller").toString());
	}

	public void setVerbose(boolean v) {
		verbose = v;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setTimeBased(boolean v) {
		if (v) {
			if (filter != null && isTimeBased())
				return;
			filter = new TimeFilter(originalFilter);
		}
		else {
			if (filter != null && !isTimeBased())
				return;
			filter = originalFilter;
		}
	}

	public boolean isTimeBased() {
		return filter instanceof TimeFilter;
	}

	public void setBaseTime(File directory, long time) {
		for (int i = 0; i < dirs.length; i++)
			if (dirs[i].getAbsolutePath().equals(directory.getAbsolutePath())) {
				baseTime[i] = time;
				return;
			}

		throw new IllegalArgumentException((new StringBuilder()).append("'").append(directory).append("' is not under control of the poller").toString());
	}

	public void setBaseTime(long time) {
		for (final File dir : dirs) {
			setBaseTime(dir, time);
		}

	}

	public long getBaseTime(File directory) {
		for (int i = 0; i < dirs.length; i++)
			if (dirs[i].getAbsolutePath().equals(directory.getAbsolutePath()))
				return baseTime[i];

		throw new IllegalArgumentException((new StringBuilder()).append("'").append(directory).append("' is not under control of the poller").toString());
	}

	public boolean isPollingTimeBased() {
		return isTimeBased() && timeBasedOnLastLookup;
	}

	public void setPollingTimeBased(boolean v) {
		timeBasedOnLastLookup = v;
	}

	public void setSendSingleFileEvent(boolean v) {
		sendSingleFileEvent = v;
	}

	public boolean isSendSingleFileEvent() {
		return sendSingleFileEvent;
	}

	public long getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(long pollInterval) {
		this.pollInterval = pollInterval;
	}

	public void setStartBySleeping(boolean v) {
		startBySleeping = v;
	}

	public boolean isStartBySleeping() {
		return startBySleeping;
	}

	public void addPollManager(PollManager pm) {
		pollManagersList.add(pm);
		addListener(new DefaultListener(this, pm));
	}

	public void shutdown() {
		shutdownRequested = true;
		interrupt();
		if (logger.isDebugEnabled())
			logger.debug("Polling shutdown requested");
	}

	public boolean isShuttingDown() {
		return shutdownRequested;
	}

	public synchronized void run() {
		shutdownRequested = false;
		if (dirs == null)
			throw new IllegalStateException("Programming error: no directories to poll specified");
		if (logger.isDebugEnabled())
			logger.debug((new StringBuilder()).append("Polling started, interval is ").append(pollInterval).append("ms").toString());
		if (autoMove) {
			for (final File dir : dirs) {
				File automoveDir = PathNormalizer.normalize(getAutoMoveDirectory(dir));
				if (automoveDir.exists()) {
					continue;
				}
				if (logger.isDebugEnabled())
					logger.debug((new StringBuilder()).append("Automove directory ").append(automoveDir).append(" does not exist, attempting to create.")
							.toString());
				if (!automoveDir.mkdirs())
					throw new RuntimeException((new StringBuilder()).append("Could not create the directory ").append(automoveDir.getAbsolutePath()).toString());
				if (logger.isDebugEnabled())
					logger.debug((new StringBuilder()).append("Automove directory ").append(automoveDir).append(" created successfully.").toString());
			}

		}
		do {
			if (startBySleeping) {
				startBySleeping = false;
			}
			else {
				runCycle();
			}
			if (!shutdownRequested) {
				try {
					sleeping = true;
					sleep(pollInterval);
					sleeping = false;
					if (logger.isDebugEnabled())
						logger.debug("Poller waking up");
				}
				catch (InterruptedException e) {
					// no-op
				}
			}
		}
		while (!shutdownRequested);

		if (logger.isDebugEnabled()) {
			logger.debug("Poller terminated.");
		}
	}

	private void runCycle() {
		if (!shutdownRequested) {
			notify(new CycleStartEvent(this));
		}

		if (!shutdownRequested) {
			for (currentDir = 0; currentDir < dirs.length; currentDir++) {
				File dir = PathNormalizer.normalize(dirs[currentDir]);

				notify(new DirectoryLookupStartEvent(this, dir));

				if (shutdownRequested) {
					return;
				}

				long filesLookupTime = System.currentTimeMillis();

				File fls[] = dir.listFiles(filter);
				if (fls == null) {
					System.err.println((new StringBuilder()).append("Warning: directory ").append(dir).append(" does not exist").toString());
					fls = new File[0];
				}

				if (filesSortComparator != null) {
					if (logger.isDebugEnabled()) {
						logger.debug((new StringBuilder()).append("Sorting files by  ").append(filesSortComparator).toString());
					}
					Arrays.sort(fls, filesSortComparator);
				}

				String files[] = new String[fls.length];

				for (int i = 0; i < files.length; i++) {
					files[i] = fls[i].getName();
				}

				String movedFiles[] = new String[files.length];
				int failedToMoveCount = 0;
				if (autoMove) {

					// the 'received' directory
					File autoMoveDir = getAutoMoveDirectory(dir);

					// iterate through all files to see if they can be moved into the 'received' dir
					for (int j = 0; j < files.length; j++) {
						File orig = new File(dir, files[j]);
						File dest = new File(autoMoveDir, files[j]);
						if (dest.exists()) {
							if (logger.isDebugEnabled()) {
								logger.debug((new StringBuilder()).append("[Automove] Attempting to delete existing ").append(dest.getAbsolutePath())
										.toString());
							}
							if (!dest.delete()) {
								notify(new ExceptionSignal(new AutomoveDeleteException(orig, dest, (new StringBuilder()).append("Could not delete ")
										.append(dest.getAbsolutePath()).toString()), this));
								failedToMoveCount++;
								continue;
							}
							if (logger.isDebugEnabled()) {
								logger.debug((new StringBuilder()).append("[Automove] Deleted ").append(dest.getAbsolutePath()).toString());
							}
						}

						autoMoveDir.mkdirs();

						try {
							boolean proceed = true;

							// if hidden, skip
							if (skip(orig)) {
								failedToMoveCount++;
								continue;
							}

							if (logger.isDebugEnabled()) {
								logger.debug((new StringBuilder()).append("[Automove] Checking to see if ").append(orig.getAbsolutePath())
										.append(" can be moved to ").append(autoMoveDir.getAbsolutePath()).append(File.separator).toString());
							}

							// if not hidden and we're bypass locking
							if (bypassLockedFiles) {
								RandomAccessFile raf = new RandomAccessFile(orig, "rw");
								FileChannel channel = raf.getChannel();
								if (channel.tryLock() == null) {
									if (logger.isDebugEnabled())
										logger.debug((new StringBuilder()).append("[Automove] File ").append(orig.getAbsolutePath())
												.append(" is locked, ignoring").toString());
									failedToMoveCount++;
									proceed = false;
								}
								else {
									proceed = true;
								}
								channel.close();
							}

							// if we can still proceed
							if (proceed) {
								List<File> filesToCheck = new ArrayList<File>();
								if (orig.isDirectory()) {
									filesToCheck.addAll(Arrays.asList(orig.listFiles(new FileFilter() {

										public boolean accept(File file) {
											return !file.isDirectory() && !skip(file);
										}
									}
											)));
								}
								else {
									filesToCheck.add(orig);
								}

								for (File fileToCheck : filesToCheck) {
									Long lastFileSize = fileSizeMap.remove(fileToCheck.getAbsolutePath());
									if (logger.isDebugEnabled()) {
										logger.debug((new StringBuilder()).append("[Automove] Checking file ").append(orig.getAbsolutePath())
												.append(" stability, last check = ").append(lastFileSize).append(", current = ").append(fileToCheck.length())
												.toString());
									}
									if (lastFileSize != null && lastFileSize == fileToCheck.length()) {
										if (logger.isDebugEnabled()) {
											logger.debug((new StringBuilder()).append("[Automove] file ").append(orig.getAbsolutePath())
													.append(" is stable, will move.").toString());
										}
									}
									else {
										if (logger.isDebugEnabled()) {
											logger.debug((new StringBuilder()).append("[Automove] file ").append(orig.getAbsolutePath())
													.append(" is not stable, ignoring.").toString());
										}
										fileSizeMap.put(orig.getAbsolutePath(), orig.length());
										proceed = false;
										failedToMoveCount++;
									}
								}
							}
							if (!proceed) {
								continue;
							}

							if (logger.isDebugEnabled()) {
								logger.debug((new StringBuilder()).append("[Automove] moving file ").append(orig.getAbsolutePath()).append(" to ")
										.append(dest.getAbsolutePath()).toString());
							}
							if (!orig.renameTo(dest)) {
								notify(new ExceptionSignal(new AutomoveException(orig, dest, (new StringBuilder()).append("Could not move ")
										.append(orig.getName()).append(" to ").append(dest.getAbsolutePath()).toString()), this));
								failedToMoveCount++;
								continue;
							}
							notify(new FileMovedEvent(this, orig, dest));

							movedFiles[j] = dest.getName();

							// dlmay removed this for a reason, commenting out for now
//							if (j + 1 == files.length) {
//								dir = autoMoveDir;
//							}

							// STUD-267: jhumphrey:
							//
							// Deletes AppleDouble files after ingestion
							//
							// AppleDouble files are hidden files on OSX that are of the format "._<FILENAME>".
							// These files get created on by OSX whenever you transfer a file into a networked drive.
							//
							// For example:
							//  - file: foo.mov
							//  - AppleDouble File: ._foo.xml
							//
							// These files are linked to the original and cause issues when moving the original media file from/to
							// the ingestion workflow directories.
							//
							// The assumption is that it's safe to delete any file that has the prefix '._' (you lose some functionality
							// in apple's finder but we're OK with that
							File appleDouble = new File(orig.getParent(), "._" + orig.getName());
							if (appleDouble.exists()) {
								if (!appleDouble.delete()) {
									logger.warn("Unable to delete AppleDouble file: " + appleDouble.getAbsolutePath());
								}
								else {
									if (logger.isTraceEnabled()) {
										logger.trace("Deleted AppleDouble file: " + appleDouble.getAbsolutePath());
									}
								}
							}

							if (logger.isDebugEnabled())
								logger.debug((new StringBuilder()).append("[Automove] Moved ").append(orig.getAbsolutePath()).append(" to ")
										.append(autoMoveDir.getAbsolutePath()).append(File.separator).toString());
							continue;
						}
						catch (FileNotFoundException e) {
							notify(new ExceptionSignal(new AutomoveException(orig, dest, (new StringBuilder()).append("Could not verify lock on ")
									.append(orig.getName()).toString()), this));
							failedToMoveCount++;
							if (logger.isWarnEnabled()) {
								logger.warn("Unable to move file", e);
							}
							continue;
						}
						catch (IOException e) {
							notify(new ExceptionSignal(new AutomoveException(orig, dest, (new StringBuilder()).append("Tentative lock attempt failed on ")
									.append(orig.getName()).toString()), this));
						}
						failedToMoveCount++;
					}

				}
				if (autoMove) {
					String tmp[] = new String[files.length - failedToMoveCount];
					int c = 0;
					for (final String movedFile : movedFiles) {
						if (movedFile != null) {
							tmp[c++] = movedFile;
						}
					}

					files = tmp;
				}

				if (files.length > 0) {
					// STUD:497: DLamy (8/21/13):  Make sure FileSetFoundEvent is pointing at the file in the received folder.
					File baseDir = (autoMove ? getAutoMoveDirectory(dir) : dir);
					notify(new FileSetFoundEvent(this, baseDir, files));
				}

				if (shutdownRequested) {
					return;
				}
				if (sendSingleFileEvent) {
					for (final String file1 : files) {
						File file = new File(dir, file1);
						notify(new FileFoundEvent(this, file));
						if (shutdownRequested) {
							return;
						}
					}

					if (shutdownRequested)
						return;
				}
				if (isTimeBased()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Computing new base time");
					}
					if (timeBasedOnLastLookup) {
						baseTime[currentDir] = filesLookupTime;
					}
					else {
						for (final String file1 : files) {
							File file = new File(dir, file1);
							long lastModifiedTime = file.lastModified();
							if (lastModifiedTime > baseTime[currentDir])
								baseTime[currentDir] = lastModifiedTime;
						}

						if (logger.isDebugEnabled())
							logger.debug((new StringBuilder()).append("Basetime for ").append(dirs[currentDir]).append(" is ").append(baseTime[currentDir])
									.toString());
					}
				}
				notify(new DirectoryLookupEndEvent(this, dir));
			}

			if (!shutdownRequested) {
				notify(new CycleEndEvent(this, baseTime));
			}
		}
	}

	/**
	 * True if we should skip the file.. false otherwise
	 *
	 * @return true or false
	 */
	private boolean skip(File file) {
		return file.isHidden() || file.length() == 0 || file.getName().startsWith(".");

	}

	public FilenameFilter getFilter() {
		return filter;
	}

	public void setFilter(FilenameFilter filter) {
		if (isAlive()) {
			throw new IllegalStateException("Can't call setFilter when the poller has already started");
		}
		else {
			this.filter = filter;
		}
	}

	public Comparator getFilesSortComparator() {
		return filesSortComparator;
	}

	public void setFilesSortComparator(Comparator filesSortComparator) {
		this.filesSortComparator = filesSortComparator;
	}

	public boolean isBypassLockedFiles() {
		return bypassLockedFiles;
	}

	public void setBypassLockedFiles(boolean supportSlowTransfer) {
		bypassLockedFiles = supportSlowTransfer;
	}

	public boolean isSleeping() {
		return sleeping;
	}

	public boolean isDebugExceptions() {
		return debugExceptions;
	}

	public void setDebugExceptions(boolean debugExceptions) {
		this.debugExceptions = debugExceptions;
	}
}
