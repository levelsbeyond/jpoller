/*
 * Levels Beyond CONFIDENTIAL
 *
 * Copyright 2003 - 2018 Levels Beyond Incorporated
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

import com.deltax.util.listener.BaseSignalSourceThread;
import com.deltax.util.listener.ExceptionSignal;
import com.deltax.util.listener.Signal;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private long postProcessDelayMinutes;
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
			if (f.isDirectory()) {
				return false;
			}
			if (f.lastModified() <= baseTime[currentDir]) {
				if (logger.isDebugEnabled()) {
					logger.debug((new StringBuilder()).append(name).append("(").append(f.lastModified()).append("): out of base time (")
									 .append(baseTime[currentDir]).append("), ignoring").toString());
				}
				return false;
			}
			if (logger.isDebugEnabled()) {
				logger.debug((new StringBuilder()).append(name).append("(").append(f.lastModified()).append("): older than base time (")
								 .append(baseTime[currentDir]).append("), accepted").toString());
			}
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
			if ((new File(dir, name)).isDirectory() && (systemDirectoryNames.length == 0 || Arrays.binarySearch(systemDirectoryNames, name) > 0)) {
				return false;
			}
			if (additionalFilter != null) {
				return additionalFilter.accept(dir, name);
			} else {
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
			if (logger.isDebugEnabled()) {
				logger.debug((new StringBuilder()).append("[Automove] Exception: ").append(msg).toString());
			}
		}
	}

	public DirectoryPoller(File dirs[], FilenameFilter filter) {
		this(dirs, filter, false);
	}

	public DirectoryPoller(File dirs[]) {
		this(dirs, new NullFilenameFilter());
	}

	public DirectoryPoller(File directory, FilenameFilter filter) {
		this(new File[]{
			directory
		}, filter);
	}

	public DirectoryPoller(File directory) {
		this(new File[]{
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
		this(new File[]{directory}, filter, timeBased);
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
			} else {
				dirs[c++] = originalDir;
			}
		}

		if (!removed) {
			throw new IllegalArgumentException((new StringBuilder()).append(dir).append(" is not a controlled directory").toString());
		} else {
			setDirectories(dirs);
		}
	}

	public void setDirectories(File dirs[]) {
		if (isAlive() && !isSleeping()) {
			throw new IllegalStateException("Can't call setDirectories when the poller is running and not sleeping");
		}
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
		} else {
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
			if (filter != null && isTimeBased()) {
				return;
			}
			filter = new TimeFilter(originalFilter);
		} else {
			if (filter != null && !isTimeBased()) {
				return;
			}
			filter = originalFilter;
		}
	}

	public boolean isTimeBased() {
		return filter instanceof TimeFilter;
	}

	public void setBaseTime(File directory, long time) {
		for (int i = 0; i < dirs.length; i++) {
			if (dirs[i].getAbsolutePath().equals(directory.getAbsolutePath())) {
				baseTime[i] = time;
				return;
			}
		}

		throw new IllegalArgumentException((new StringBuilder()).append("'").append(directory).append("' is not under control of the poller").toString());
	}

	public void setBaseTime(long time) {
		for (final File dir : dirs) {
			setBaseTime(dir, time);
		}

	}

	public long getBaseTime(File directory) {
		for (int i = 0; i < dirs.length; i++) {
			if (dirs[i].getAbsolutePath().equals(directory.getAbsolutePath())) {
				return baseTime[i];
			}
		}

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

	public long getPostProcessDelayMinutes() {
		return postProcessDelayMinutes;
	}

	public void setPostProcessDelayMinutes(long delayMinutes) {
		postProcessDelayMinutes = delayMinutes;
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
		if (logger.isDebugEnabled()) {
			logger.debug("Polling shutdown requested");
		}
	}

	public boolean isShuttingDown() {
		return shutdownRequested;
	}

	public synchronized void run() {
		shutdownRequested = false;
		if (dirs == null) {
			throw new IllegalStateException("Programming error: no directories to poll specified");
		}
		if (logger.isDebugEnabled()) {
			logger.debug((new StringBuilder()).append("Polling started, interval is ").append(pollInterval).append("ms").toString());
		}
		if (autoMove) {
			for (final File dir : dirs) {
				File automoveDir = PathNormalizer.normalize(getAutoMoveDirectory(dir));
				if (automoveDir.exists()) {
					continue;
				}
				if (logger.isDebugEnabled()) {
					logger.debug((new StringBuilder()).append("Automove directory ").append(automoveDir).append(" does not exist, attempting to create.")
									 .toString());
				}
				if (!automoveDir.mkdirs()) {
					throw new RuntimeException((new StringBuilder()).append("Could not create the directory ").append(automoveDir.getAbsolutePath()).toString());
				}
				if (logger.isDebugEnabled()) {
					logger.debug((new StringBuilder()).append("Automove directory ").append(automoveDir).append(" created successfully.").toString());
				}
			}

		}
		do {
			if (startBySleeping) {
				startBySleeping = false;
			} else {
				runCycle();
			}
			if (!shutdownRequested) {
				try {
					sleeping = true;
					sleep(pollInterval);
					sleeping = false;
					if (logger.isDebugEnabled()) {
						logger.debug("Poller waking up");
					}
				} catch (InterruptedException e) {
					// no-op
				}
			}
		}
		while (!shutdownRequested);

		if (logger.isDebugEnabled()) {
			logger.debug("Poller terminated.");
		}
	}

	void notifyEvent(Signal signal) {
		notify(signal);
	}

	void runCycle() {
		if (!shutdownRequested) {
			notifyEvent(new CycleStartEvent(this));
		}

		if (!shutdownRequested) {
			for (currentDir = 0; currentDir < dirs.length; currentDir++) {
				File dir = PathNormalizer.normalize(dirs[currentDir]);

				notifyEvent(new DirectoryLookupStartEvent(this, dir));

				if (shutdownRequested) {
					return;
				}

				long filesLookupTime = System.currentTimeMillis();

				File fls[] = dir.listFiles(filter);
				if (fls == null) {
					logger.warn("Warning: directory {} does not exist", dir.toString());
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
				if (autoMove) {

					// the 'received' directory
					File autoMoveDir = getAutoMoveDirectory(dir);

					final File[] markerFiles = autoMoveDir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File d, String name) {
							return name.length() > 3 && name.startsWith(".~") && name.endsWith("~");
						}
					});
					if (markerFiles != null) {
						for (File markerFile : markerFiles) {
							String origName = markerFile.getName().substring(2);
							origName = origName.substring(0, origName.length() - 1);
							final File orig = new File(autoMoveDir, origName);
							if (!orig.exists()) {
								logger.debug("Deleting orphaned marker file {}", markerFile.getAbsolutePath());
								markerFile.delete();
								continue;
							}
							if (isPostProcessFileExpired(markerFile)) {
								final Map<String, String> post = readPostProcessFile(markerFile);
								StringBuilder builder = new StringBuilder();
								builder.append("{");
								for (Map.Entry<String, String> entry : post.entrySet()) {
									builder.append(String.format("\"%s\":\"%s\",", entry.getKey(), entry.getValue()));
								}
								builder.append("}");
								switch (post.get("action")) {
									case "delete":
										logger.debug("Deleting file {} per marker {}: {}", orig.getName(), markerFile.getAbsolutePath(), builder);
										if (!orig.delete()) {
											logger.warn("Failed to delete file {} per post process file {}: {}", orig.getAbsolutePath(), markerFile.getAbsolutePath(), builder);
										}
										break;
									case "move":
										final String destination = post.get("dest");
										logger.debug("Moving file {} to {} per marker {}: {}", orig.getName(), destination, markerFile.getAbsolutePath(), builder);
										if (!orig.renameTo(new File(destination))) {
											logger.warn("Failed to move file {} to {} per post process file {}: {}", orig.getAbsolutePath(), destination, markerFile.getAbsolutePath(), builder);
										}
										break;
									default:
										logger.warn("Unexpected marker placeholder {}: {}", markerFile.getAbsolutePath(), builder);
								}
								markerFile.delete();
							}
						}
					}
					// iterate through all files to see if they can be moved into the 'received' dir
					for (int j = 0; j < files.length; j++) {
						final File orig = new File(dir, files[j]);
						final File dest = new File(autoMoveDir, files[j]);
						if (dest.exists()) {
							if (logger.isDebugEnabled()) {
								logger.debug((new StringBuilder()).append("[Automove] Attempting to delete existing ").append(dest.getAbsolutePath())
												 .toString());
							}
							if (!dest.delete()) {
								notifyEvent(new ExceptionSignal(new AutomoveDeleteException(orig, dest, (new StringBuilder()).append("Could not delete ")
									.append(dest.getAbsolutePath()).toString()), this));
								continue;
							}
							if (logger.isDebugEnabled()) {
								logger.debug((new StringBuilder()).append("[Automove] Deleted ").append(dest.getAbsolutePath()).toString());
							}
						}

						autoMoveDir.mkdirs();

						try {
							boolean proceed = true;

							// if hidden or it's one of the directories we are scanning later, skip
							logger.debug("{} - skip: {}, scandir: {}", orig, skip(orig), isScanDir(orig));
							if (skip(orig) || isScanDir(orig) || postProcessDelayPending(orig)) {
								continue;
							}

							if (logger.isDebugEnabled()) {
								logger.debug((new StringBuilder()).append("[Automove] Checking to see if ").append(orig.getAbsolutePath())
												 .append(" can be moved to ").append(autoMoveDir.getAbsolutePath()).append(File.separator).toString());
							}

							// if not hidden and we're bypass locking
							if (orig.isFile()) {
								if (bypassLockedFiles) {
									RandomAccessFile raf = new RandomAccessFile(orig, "rw");
									FileChannel channel = raf.getChannel();
									if (channel.tryLock() == null) {
										if (logger.isDebugEnabled()) {
											logger.debug((new StringBuilder()).append("[Automove] File ").append(orig.getAbsolutePath())
															 .append(" is locked, ignoring").toString());
										}
										proceed = false;
									} else {
										proceed = true;
									}
									channel.close();
								}
							}

							// if we can still proceed
							if (proceed) {
								final List<File> filesToCheck = new ArrayList<File>();
								// Add all files in child directory tree for testing.
								if (orig.isDirectory()) {
									new Object() {
										void addAllFiles(final File scanDir) {
											for (final File pFile : scanDir.listFiles(new FileFilter() {
												public boolean accept(final File file) {
													return !skip(file) && !isScanDir(file) && !postProcessDelayPending(file) && (file.isDirectory() || file.isFile());
												}
											})) {
												if (pFile.isFile()) {
													filesToCheck.add(pFile);
												} else {
													addAllFiles(pFile);
												}
											}
										}
									}.addAllFiles(orig);
								} else {
									filesToCheck.add(orig);
								}
								for (final File fileToCheck : filesToCheck) {
									Long lastFileSize = fileSizeMap.remove(fileToCheck.getAbsolutePath());
									if (logger.isDebugEnabled()) {
										logger.debug((new StringBuilder()).append("[Automove] Checking file ").append(fileToCheck.getAbsolutePath())
														 .append(" stability, last check = ").append(lastFileSize).append(", current = ").append(fileToCheck.length())
														 .toString());
									}
									if (lastFileSize != null && lastFileSize == fileToCheck.length()) {
										if (logger.isDebugEnabled()) {
											logger.debug((new StringBuilder()).append("[Automove] file ").append(fileToCheck.getAbsolutePath())
															 .append(" is stable, will move.").toString());
										}
									} else if (postProcessDelayPending(fileToCheck)) {
										if (logger.isDebugEnabled()) {
											logger.debug((new StringBuilder()).append("[Automove] file ").append(fileToCheck.getAbsolutePath())
															 .append(" is waiting for delay, ignoring.").toString());
										}
										proceed = false;
									} else {
										if (logger.isDebugEnabled()) {
											logger.debug((new StringBuilder()).append("[Automove] file ").append(fileToCheck.getAbsolutePath())
															 .append(" is not stable, ignoring.").toString());
										}
										fileSizeMap.put(fileToCheck.getAbsolutePath(), fileToCheck.length());
										proceed = false;
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
								notifyEvent(new ExceptionSignal(new AutomoveException(orig, dest, (new StringBuilder()).append("Could not move ")
									.append(orig.getName()).append(" to ").append(dest.getAbsolutePath()).toString()), this));
								continue;
							}
							if (!removePostProcessMarker(orig)) {
								notifyEvent(new ExceptionSignal(new AutomoveException(orig, dest, (new StringBuilder()).append("Could not remove ")
									.append(getPostProcessMarkerFile(orig).getName()).append(" after moving file ").append(orig.getName())
									.append(" to ").append(dest.getAbsolutePath()).toString()), this));
							}
							notifyEvent(new FileMovedEvent(this, orig, dest));

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
								} else {
									if (logger.isTraceEnabled()) {
										logger.trace("Deleted AppleDouble file: " + appleDouble.getAbsolutePath());
									}
								}
							}

							if (logger.isDebugEnabled()) {
								logger.debug((new StringBuilder()).append("[Automove] Moved ").append(orig.getAbsolutePath()).append(" to ")
												 .append(autoMoveDir.getAbsolutePath()).append(File.separator).toString());
							}
						} catch (FileNotFoundException e) {
							notifyEvent(new ExceptionSignal(new AutomoveException(orig, dest, (new StringBuilder()).append("Could not verify lock on ")
								.append(orig.getName()).toString()), this));
							if (logger.isWarnEnabled()) {
								logger.warn("Unable to move file", e);
							}
						} catch (IOException e) {
							notifyEvent(new ExceptionSignal(new AutomoveException(orig, dest, (new StringBuilder()).append("Tentative lock attempt failed on ")
								.append(orig.getName()).toString()), this));
						}
					}
				}
				if (autoMove) {
					final List<String> movedFileList = new ArrayList<>();
					for (String movedFile : movedFiles) {
						if (movedFile != null) {
							movedFileList.add(movedFile);
						}
					}
					files = movedFileList.toArray(new String[0]);
				}

				if (files.length > 0) {
					// STUD:497: DLamy (8/21/13):  Make sure FileSetFoundEvent is pointing at the file in the received folder.
					File baseDir = (autoMove ? getAutoMoveDirectory(dir) : dir);
					notifyEvent(new FileSetFoundEvent(this, baseDir, files));
				}

				if (shutdownRequested) {
					return;
				}
				if (sendSingleFileEvent) {
					for (final String file1 : files) {
						File file = new File(dir, file1);
						notifyEvent(new FileFoundEvent(this, file));
						if (shutdownRequested) {
							return;
						}
					}

					if (shutdownRequested) {
						return;
					}
				}
				if (isTimeBased()) {
					if (logger.isDebugEnabled()) {
						logger.debug("Computing new base time");
					}
					if (timeBasedOnLastLookup) {
						baseTime[currentDir] = filesLookupTime;
					} else {
						for (final String file1 : files) {
							File file = new File(dir, file1);
							long lastModifiedTime = file.lastModified();
							if (lastModifiedTime > baseTime[currentDir]) {
								baseTime[currentDir] = lastModifiedTime;
							}
						}

						if (logger.isDebugEnabled()) {
							logger.debug((new StringBuilder()).append("Basetime for ").append(dirs[currentDir]).append(" is ").append(baseTime[currentDir])
											 .toString());
						}
					}
				}
				notifyEvent(new DirectoryLookupEndEvent(this, dir));
			}

			if (!shutdownRequested) {
				notifyEvent(new CycleEndEvent(this, baseTime));
			}
		}
	}

	/**
	 * True if we should skip the file.. false otherwise
	 *
	 * @return true or false
	 */
	private boolean skip(File file) {
		return !(file.isFile() || file.isDirectory())
			|| file.isHidden()
			|| (file.isFile() && file.length() == 0)
			|| file.getName().startsWith(".");
	}

	private File getPostProcessMarkerFile(File file) {
		return new File(file.getParent(), String.format(".~%s~", file.getName()));
	}

	private boolean postProcessDelayPending(File file) {
		// File contains the destination path to move the file to
		final File processedMarkerFile = getPostProcessMarkerFile(file);
		return isPostProcessFileExpired(file);
	}

	private boolean isPostProcessFileExpired(final File processedMarkerFile) {
		final long delayMillis = getPostProcessDelayMinutes() * 60000;
		return delayMillis > 0 && processedMarkerFile.exists() && processedMarkerFile.isFile() && processedMarkerFile.canRead() && processedMarkerFile.length() > 0
			&& System.currentTimeMillis() - processedMarkerFile.lastModified() < delayMillis;
	}

	Map<String, String> readPostProcessFile(final File processedMarkerFile) {
		Map<String, String> rval = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(processedMarkerFile))) {
			for (String line; (line = reader.readLine()) != null; ) {
				String[] row = line.split("=", 2);
				if (row.length == 2) {
					rval.put(row[0], row[1]);
				}
			}
		} catch (IOException e) {
			logger.error(String.format("Error parsing file %s", processedMarkerFile.getAbsolutePath()), e);
		}
		return rval;
	}

	private boolean removePostProcessMarker(File file) {
		final File processedMarkerFile = getPostProcessMarkerFile(file);
		if (!processedMarkerFile.exists()) {
			return true;
		}
		return processedMarkerFile.delete();
	}

	private boolean isScanDir(File file) {
		if (file.isDirectory()) {
			try {
				for (File scanDir : dirs) {
					if (Files.isSameFile(PathNormalizer.normalize(scanDir).toPath(), file.toPath())) {
						return true;
					}
				}
			} catch (IOException e) {
				logger.error(String.format("Error while checking %s", file.getAbsolutePath()), e);
			}
		}
		return false;
	}

	public FilenameFilter getFilter() {
		return filter;
	}

	public void setFilter(FilenameFilter filter) {
		if (isAlive()) {
			throw new IllegalStateException("Can't call setFilter when the poller has already started");
		} else {
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
