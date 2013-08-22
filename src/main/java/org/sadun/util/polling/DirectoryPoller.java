package org.sadun.util.polling;

import java.io.File;
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

import org.sadun.util.BidirectionalComparator;
import org.sadun.util.PathNormalizer;
import org.sadun.util.Terminable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deltax.util.listener.BaseSignalSourceThread;
import com.deltax.util.listener.ExceptionSignal;

/**
 * A Thread class to periodically poll the contents of one or more directories. External asynchronous processes may put
 * some files in one of the controlled directories, and the poller can be used to detect their arrival.
 * <p/>
 * Once started, this thread polls one or more directories every <i>x</i> milliseconds, (see {@link
 * DirectoryPoller#setPollInterval(long) setPollInterval()} and {@link DirectoryPoller#getPollInterval()
 * getPollInterval()}), looking for new files satisfying a  given filter. <br>Optionally, the thread may start by going
 * to sleep (if {@link DirectoryPoller#setStartBySleeping(boolean) setStartBySleeping()} is invoked).
 * <p/>
 * To communicate results, the thread notifies events to any registered listener (see below). Notably, if any files are
 * "found", a {@link FileSetFoundEvent FileSetFoundEvent} event is notified.
 * <p/>
 * A {@link java.io.FilenameFilter java.io.FilenameFilter} can be provided at construction to identify the files to look
 * for. Besides, the poller can be set to run in two different time-based filtering modes (disabled by default), which
 * select only files satisfying the user-given filter and
 * <p/>
 * <ul> <li> whose <i>last modification time</i><a href="#note"><sup>*</sup></a> (<b>LMF</b>) is greater than the time
 * of the last polling <i>(default)</i> <p><font size=-1>(Only files added to the directory after the last polling pass
 * are selected)</font> <p><i>or</i><p> <li> whose <b>LMF</b> is greater than the higher <b>LMF</b> rilevated in the
 * file set selected in the last polling pass (or 0 for the first pass). <p><font size=-1>(Only files which are newer
 * than the newer file already polled are selected)</font> </ul>
 * <p/>
 * Use {@link DirectoryPoller#setTimeBased(boolean) setTimeBased()} and {@link DirectoryPoller#isTimeBased()
 * isTimeBased()} to enable the time-based mode and {@link DirectoryPoller#setPollingTimeBased(boolean)
 * setPollingTimeBased()} and {@link DirectoryPoller#isPollingTimeBased() isPollingTimeBased()} to select the specific
 * subtype of time-based mode.
 * <p/>
 * Without time-based mode, on every pass the poller will select <i>all</i> the files in a controlled directory that
 * match the filter, so it's client code responsability to phisically move the files already processed somewhere else
 * (see also the <a href="#automove">automove</a> mode below).
 * <p/>
 * The poller always notifies an event for a set of file discovered in a particular directory. It may optionally notify
 * a separate event for each file (see {@link DirectoryPoller#setSendSingleFileEvent(boolean) setSendSingleFileEvent()}
 * and {@link DirectoryPoller#isSendSingleFileEvent() isSendSingleFileEvent()}).
 * <p/>
 * The following events (all time-stamped) are produced: <ul> <li> {@link CycleStartEvent CycleStartEvent} <br> The
 * poller has awaken, and is starting to look in the directory set <li> {@link DirectoryLookupStartEvent
 * DirectoryLookupStartEvent} <br> The poller is starting to look into a specific directory <li> {@link
 * FileSetFoundEvent FileSetFoundEvent} <br> The poller has found some files in a directory, which match the polling
 * criteria <li> {@link FileFoundEvent FileFoundEvent} (<i>optional</i>) <br> The poller has found one file in a
 * directory, which match the polling criteria <li> {@link DirectoryLookupEndEvent DirectoryLookupStartEvent} <br> The
 * poller has finished to look a specific directory <li> {@link CycleEndEvent CycleEndEvent} <br> The poller has
 * finished to look in the directory set, and is going to sleep </ul>
 * <p/>
 * Any object implementing the {@link com.deltax.util.lf.Listener com.deltax.Listener} interface can be registered and
 * be notified of these events. However, a predefined listener, {@link DefaultListener DefaultListener} is provided,
 * which dispatches events to an object implementing the {@link PollManager PollManager} interface, and a base
 * implementation of {@link PollManager PollManager}, {@link BasePollManager BasePollManager} is provided as well.
 * <p/>
 * The simplest way to receive events from the poller is therefore to create a class extending {@link BasePollManager
 * BasePollManager} overriding the proper method(s) and register it to the directory poller by using {@link
 * DirectoryPoller#addPollManager(PollManager) addPollManager()}.
 * <p/>
 * The order in which file events are received can be controlled by providing a {@link java.util.Comparator} object
 * which assigns an order to the file sets read on each cycle and assigning it by {@link
 * #setFilesSortComparator(Comparator)}. By default, no order is imposed, i.e. the file events will be fired in an
 * arbitrary order, depending on the operating system. (Two pre-packaged comparators {@link
 * DirectoryPoller.ModificationTimeComparator} and {@link DirectoryPoller.FileSizeComparator} are provided by this
 * class).
 * <p/>
 * <i>Note</i>: listeners will receive events <b><i>asynchronously</i></b> with respect to the poller thread. Therefore,
 * if file processing is performed without moving the file out of the polled directory, the polling interval should be
 * big enough to allow such processing to complete, or the same file may be notified more than once (expecially if
 * time-based polling is disabled). <a name="automove"><p> The version 1.2 of the class adds a new runtime mode - the
 * <b>autoMove</b> mode. When running in this mode, the poller automatically moves all the files found in a controlled
 * directory to an associated directory (which is <i>not</i> polled) <i>before</i> notifying the listeners and/or the
 * pollmanager. The events notified will refer to the <i>new</i> location of the file.
 * <p/>
 * This allows the user to set very short polling periods while ensuring that even slow file processing will not cause
 * the same file to be polled more than once (even if the time-based polling mode is disabled).
 * <p/>
 * If any exception occurs during the move operation, the relevant methods of {@link PollManager PollManager} are
 * invoked (as a consequence of the notification of an underlying {@link com.deltax.util.listener.ExceptionSignal
 * com.deltax.util.listener.ExceptionSignal}).
 * <p/>
 * If autoMove mode is enabled (see {@link DirectoryPoller#setAutoMove(boolean) setAutoMove()}) an arbitary directory
 * may be explicitly associated to one of the controlled directories by using {@link
 * DirectoryPoller#setAutoMoveDirectory(java.io.File, java.io.File) setAutoMoveDirectory()}.
 * <p/>
 * If autoMove mode is enabled and the byPassLockedFiles property is true (defaults to false), locked files will be
 * ignored. Otherwise, an exception signal will be raised if any file found in the controlled directory is locked.
 * <p/>
 * If not, the poller will automatically associate to each controlled directory a subdirectory whose name is defined by
 * the public constant {@link DirectoryPoller#DEFAULT_AUTOMOVE_DIRECTORY DirectoryPoller.DEFAULT_AUTOMOVE_DIRECTORY}.
 * The poller will attempt to create the associated directory, if necessary - and on failure, an ExceptionSignal is sent
 * to the listeners, and the poller shuts down.
 * <p/>
 * Note that ExceptionSignals are sent only when the auto-move mode is enabled. </a>
 * <p/>
 * <a name="note">(*) as returned by <tt>File.lastModified()</tt>.</a>
 *
 * @author C. Sadun (patched by Doug.Liao@fnf.com)
 * @version 1.4.4
 */
public class DirectoryPoller extends BaseSignalSourceThread implements Terminable {

	private final static Logger logger = LoggerFactory.getLogger(DirectoryPoller.class);

	/**
	 * An exception raised by the poller when auto-move mode is enabled, but the move operation failed.
	 */
	public class AutomoveException extends Exception {

		private File origin;
		private File dest;

		AutomoveException(File origin, File dest, String msg) {
			super(msg);
			this.origin = origin;
			this.dest = dest;
			if (verbose)
				System.out.println("[Automove] Exception: " + msg);
		}

		/**
		 * Return the poller associated to this exception.
		 *
		 * @return the poller associated to this exception.
		 */
		public DirectoryPoller getPoller() {
			return DirectoryPoller.this;
		}

		/**
		 * Return the file to be moved
		 *
		 * @return the file to be moved
		 */
		public File getOrigin() {
			return origin;
		}

		/**
		 * Return the destination file
		 *
		 * @return the destination file
		 */
		public File getDestination() {
			return dest;
		}
	}

	/**
	 * An exception raised by the poller when auto-move mode is enabled, but the target file of the move operation exists
	 * and cannot be deleted.
	 */
	public class AutomoveDeleteException extends AutomoveException {
		AutomoveDeleteException(File origin, File dest, String msg) {
			super(origin, dest, msg);
		}
	}

	/**
	 * The name of subdirectory automatically associated by the poller to any controlled directory for the autoMode mode,
	 * unless {@link DirectoryPoller#setAutoMoveDirectory(java.io.File, java.io.File) setAutoMoveDirectory()} is explicitly
	 * called before starting the poller.
	 * <p/>
	 * The current value is &quot;<b><tt>received</tt></b>&quot;
	 */
	public static final String DEFAULT_AUTOMOVE_DIRECTORY = "received";

	private static int counter = 0;
	private volatile boolean shutdownRequested = false;
	private FilenameFilter filter;
	private File[] dirs;
	private long[] baseTime;

	private boolean verbose = (System.getProperty("org.sadun.verbose") != null);
	private boolean timeBasedOnLastLookup = true;

	protected List pollManagersList = new ArrayList();

	private boolean autoMove = false;
	private Map autoMoveDirs = new HashMap();

	private FilenameFilter originalFilter;
	private long pollInterval = 10000;
	private boolean startBySleeping = false;
	private boolean sendSingleFileEvent = false;

	private int currentDir = -1;

	private Comparator filesSortComparator = null;

	private boolean bypassLockedFiles = false;

	private volatile boolean sleeping = false;

	private volatile boolean debugExceptions;

	private static class DirectoryFilter implements FilenameFilter {

		FilenameFilter additionalFilter;

		DirectoryFilter(FilenameFilter additionalFilter) {
			this.additionalFilter = additionalFilter;
		}


		public boolean accept(File dir, String name) {
			if (new File(dir, name).isDirectory()) return false;
			return additionalFilter.accept(dir, name);
		}

		public String toString() {
			return "Directory filter over a " + additionalFilter;
		}

		FilenameFilter getAdditionalFilter() {
			return additionalFilter;
		}

	}

	private class TimeFilter implements FilenameFilter {

		private FilenameFilter additionalFilter;

		public TimeFilter(FilenameFilter additionalFilter) {
			this.additionalFilter = additionalFilter;
		}

		public boolean accept(File dir, String name) {
			File f = new File(dir, name);
			if (f.isDirectory()) return false;
			if (f.lastModified() <= baseTime[currentDir]) {
				if (verbose)
					System.out.println(name + "(" + f.lastModified() + "): out of base time (" + baseTime[currentDir] + "), ignoring");
				return false;
			}
			else {
				if (verbose)
					System.out.println(name + "(" + f.lastModified() + "): older than base time (" + baseTime[currentDir] + "), accepted");
			}

			return additionalFilter.accept(dir, name);
		}
	}

	public static final class NullFilenameFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return true;
		}

		public String toString() {
			return "null filter";
		}
	}


	/**
	 * This comparator can be used to list files based on modification time.
	 *
	 * @author Cristiano Sadun
	 */
	public static class ModificationTimeComparator extends BidirectionalComparator {

		/**
		 * Create a comparator which will impose an ascending or descending order on modification times depending on the value
		 * of the parameter
		 *
		 * @param ascending if <b>true</b>, older files will be ordered before newer files.
		 */
		public ModificationTimeComparator(boolean ascending) {
			super(ascending);
		}

		protected final long getComparisonValue(File f1, File f2) {
			return f1.lastModified() - f2.lastModified();
		}

	}

	/**
	 * This comparator can be used to list files based on size.
	 *
	 * @author Cristiano Sadun
	 */
	public static class FileSizeComparator extends BidirectionalComparator {

		/**
		 * Create a comparator which will impose an ascending or descending order on modification times depending on the value
		 * of the parameter
		 *
		 * @param ascending if <b>true</b>, older files will be ordered before newer files.
		 */
		public FileSizeComparator(boolean ascending) {
			super(ascending);
		}


		protected final long getComparisonValue(File f1, File f2) {
			return f1.length() - f2.length();
		}

	}


	/**
	 * Create a poller over the given directories, using the given filter.
	 * <p/>
	 * SubDirectories are automatically filtered.
	 *
	 * @param dirs   an array of directories
	 * @param filter a filter for files to look up
	 */
	public DirectoryPoller(File[] dirs, FilenameFilter filter) {
		this(dirs, filter, false);
	}

	/**
	 * Create a poller over the given directories, which will match any file.
	 * <p/>
	 * SubDirectories are automatically filtered.
	 *
	 * @param dirs an array of directories
	 */
	public DirectoryPoller(File[] dirs) {
		this(dirs, new NullFilenameFilter());
	}


	/**
	 * Create a poller over the given directory, using the given filter.
	 * <p/>
	 * SubDirectories are automatically filtered.
	 *
	 * @param dir    a directory
	 * @param filter a filter for files to look up
	 */
	public DirectoryPoller(File directory, FilenameFilter filter) {
		this(new File[]{directory}, filter);
	}

	/**
	 * Create a poller over the given directory, which will match any file.
	 * <p/>
	 * SubDirectories are automatically filtered.
	 *
	 * @param dir a directory
	 */
	public DirectoryPoller(File directory) {
		this(new File[]{directory});
	}

	/**
	 * Create a poller initially not bound to any directory, which uses the given filter.
	 * <p/>
	 * SubDirectories are automatically filtered.
	 * <p/>
	 * Before starting the poller, a single call to {@link DirectoryPoller#setDirectories(java.io.File[])} must be done to
	 * bind the poller to a specific directory.
	 *
	 * @param filter a filter for files to look up
	 */
	public DirectoryPoller(FilenameFilter filter) {
		this(filter, false);
	}


	/**
	 * Create a poller initially not bound to any directory, which will match any file.
	 * <p/>
	 * SubDirectories are automatically filtered.
	 * <p/>
	 * Before starting the poller, a single call to {@link DirectoryPoller#setDirectories(java.io.File[])} must be done to
	 * bind the poller to a specific directory.
	 *
	 * @param filter a filter for files to look up
	 */
	public DirectoryPoller() {
		this(new NullFilenameFilter());
	}


	/**
	 * Create a poller over the given directories, using the given filter and time-based filtering as well (see class
	 * comment).
	 * <p/>
	 * SubDirectories are automatically filtered.
	 *
	 * @param dirs      an array of directories
	 * @param filter    a filter for files to look up
	 * @param timeBased if <b>true</b>, the poller uses time-based lookup
	 */
	public DirectoryPoller(File[] dirs, FilenameFilter filter, boolean timeBased) {
		setName("directory-poller-" + (counter++));
		setDirectories(dirs);
		this.originalFilter = new DirectoryFilter(filter);
		setTimeBased(timeBased);
		this.baseTime = new long[dirs.length];
	}

	/**
	 * Create a poller over the given directory, using the given filter and time-based filtering as well (see class
	 * comment).
	 * <p/>
	 * SubDirectories are automatically filtered.
	 *
	 * @param dir    a directory
	 * @param filter a filter for files to look up
	 */
	public DirectoryPoller(File directory, FilenameFilter filter, boolean timeBased) {

		this(new File[]{directory}, filter, timeBased);
	}

	/**
	 * Create a poller initially not bound to any directory, which uses the given filter and time-based filtering as well
	 * (see class comment).
	 * <p/>
	 * SubDirectories are automatically filtered.
	 * <p/>
	 * Before starting the poller, a single call to {@link DirectoryPoller#setDirectories(java.io.File[])} must be done to
	 * bind the poller to a specific directory.
	 *
	 * @param filter a filter for files to look up
	 */
	public DirectoryPoller(FilenameFilter filter, boolean timeBased) {
		this(new File[0], filter, timeBased);
	}

	/**
	 * Add one directory to the controlled set. It can be called only if the poller thread hasn't started yet.
	 *
	 * @param dir the directory to add
	 * @throws IllegalStateException    if the poller has already started.
	 * @throws IllegalArgumentException if String does not contain a directory path
	 */
	public void addDirectory(File dir) {
		File[] originalDirs = getDirectories();

		// Ignore duplicated directorues
		for (int i = 0; i < originalDirs.length; i++)
			if (originalDirs[i].getAbsoluteFile().equals(dir.getAbsoluteFile())) return;

		File[] dirs = new File[getDirectories().length + 1];
		System.arraycopy(originalDirs, 0, dirs, 0, originalDirs.length);
		dirs[originalDirs.length] = dir;
		setDirectories(dirs);
	}

	/**
	 * Remove one directory from the controlled set. It can be called only if the poller thread hasn't started yet.
	 *
	 * @param dir the directory to remove
	 * @throws IllegalStateException    if the poller has already started.
	 * @throws IllegalArgumentException if the directory is not among the controlled ones
	 */
	public void removeDirectory(File dir) {
		File[] originalDirs = getDirectories();
		File[] dirs = new File[originalDirs.length - 1];
		boolean removed = false;
		int c = 0;
		for (int i = 0; i < originalDirs.length; i++) {
			if (originalDirs[i].equals(dir)) {
				removed = true;
			}
			else {
				dirs[c++] = originalDirs[i];
			}
		}
		if (!removed)
			throw new IllegalArgumentException(dir + " is not a controlled directory");
		setDirectories(dirs);
	}


	/**
	 * Set the directories controlled by the poller. It can be called only if the poller thread hasn't started yet.
	 *
	 * @param dirs the directories to be controlled by the poller
	 * @throws IllegalStateException    if the poller has already started.
	 * @throws IllegalArgumentException if any of the File objects is not a directory
	 */
	public void setDirectories(File[] dirs) {
		if (isAlive())
			if (!isSleeping())
				throw new IllegalStateException("Can't call setDirectories when the poller is running and not sleeping");
		if (dirs != null) {
			for (int i = 0; i < dirs.length; i++) {
				if (!dirs[i].isDirectory())
					throw new IllegalArgumentException(dirs[i] + " is not a directory");
			}
		}
		this.dirs = dirs;
		baseTime = new long[dirs.length];
	}

	/**
	 * Return the directories controlled by the poller.
	 *
	 * @return the directories controlled by the poller
	 */
	public File[] getDirectories() {
		return dirs;
	}

	/**
	 * Instruct the poller to automatically move the file to the directory associated to each directory under control,
	 * which can be set/retrieved by {@link DirectoryPoller#setAutoMoveDirectory(java.io.File, java.io.File)
	 * setAutoMoveDirectory()}/{@link DirectoryPoller#getAutoMoveDirectory(java.io.File) getAutoMoveDirectory()} (see also
	 * class description).
	 *
	 * @param v if <b>true</b>, the poller will automatically move selected files in the "received" directory associated to
	 *          each directory under control
	 */
	public void setAutoMove(boolean v) {
		autoMove = v;
	}

	/**
	 * Verify the autoMove mode (see {@link DirectoryPoller#setAutoMove(boolean) setAutoMove()} and class description).
	 *
	 * @return <b>true</b> if autoMove mode is active
	 */
	public boolean getAutoMove() {
		return autoMove;
	}

	/**
	 * Returns the directory associated to the given controlled directory, where files polled are automatically moved if
	 * the autoMove mode is active (see {@link DirectoryPoller#setAutoMove(boolean) setAutoMove()} and class description).
	 * <p/>
	 * If no directory is associated by {@link DirectoryPoller#setAutoMoveDirectory(java.io.File, java.io.File)
	 * setAutoMoveDirectory()}, the subdirectory {@link DirectoryPoller#DEFAULT_AUTOMOVE_DIRECTORY
	 * DEFAULT_AUTOMOVE_DIRECTORY} is associated automatically.
	 *
	 * @param directory the directory for which the associated "automove" directory is requested
	 * @throws IllegalArgumentException if <tt>directory</tt> is not under control of the poller
	 */
	public File getAutoMoveDirectory(File directory) throws IllegalArgumentException {
		directory = PathNormalizer.normalize(directory);
		File f = (File) autoMoveDirs.get(directory);
		if (f == null) {
			f = new File(directory, DEFAULT_AUTOMOVE_DIRECTORY);
			setAutoMoveDirectory0(directory, f);
		}
		return f;
	}

	/**
	 * Associate a directory to one of the controlled directories, for the autoMove mode (see class description).
	 * <p/>
	 * This method can be called only if the poller has not started yet.
	 *
	 * @param directory         the controlled directory
	 * @param autoMoveDirectory the directory associated to the controlled directory
	 * @throws IllegalArgumentException if <tt>directory</tt> is not a controlled directory
	 * @throws IllegalStateException
	 */
	public void setAutoMoveDirectory(File directory,
	                                 File autoMoveDirectory)
		throws IllegalArgumentException, IllegalStateException {
		if (isAlive())
			throw new IllegalStateException("auto-move directories cannot be set once the poller has started");
		setAutoMoveDirectory0(directory, autoMoveDirectory);
	}

	// This version is called internally from run(), when the thread is alive
	private void setAutoMoveDirectory0(File directory, File autoMoveDirectory)
		throws IllegalArgumentException {
		directory = PathNormalizer.normalize(directory);
		checkIfManaged(directory);
		autoMoveDirs.put(directory, directory = PathNormalizer.normalize(autoMoveDirectory));
	}


	// Throw an IllegalArgumentException if the given directory is not
	// managed by the poller
	protected void checkIfManaged(File directory) {
		for (int i = 0; i < dirs.length; i++)
			if (PathNormalizer.normalize(dirs[i]).equals(
				directory)
				) return;

		throw new IllegalArgumentException(
			"The directory " + directory +
				" is not under control of the directory poller");
	}


	/**
	 * Set the verbose level. Verbosity is mainly for debugging/tracing purposes, since the poller delivers events to any
	 * listener, which can therefore perform the tracing.
	 *
	 * @param v if true, the poller logs to system out.
	 */
	public void setVerbose(boolean v) {
		this.verbose = v;
	}

	/**
	 * Return the verbose level. See {@link DirectoryPoller#setVerbose(boolean)}.
	 *
	 * @return the verbose level
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * Sets the usage of time-based filtering (besides the normal filtering). It can be called only if the poller thread
	 * hasn't started yet.
	 *
	 * @param v if <b>true</b> the poller will use time-based filtering
	 * @throws IllegalStateException if the poller has already started.
	 */
	public void setTimeBased(boolean v) {
		if (v) {
			if (filter != null && isTimeBased()) return;
			this.filter = new TimeFilter(originalFilter);
		}
		else {
			if (filter != null && !isTimeBased()) return;
			this.filter = originalFilter;
		}
	}

	/**
	 * Return <b>true</b> if the poller is using time-based filtering (see class comment).
	 *
	 * @return <b>true</b> if the poller is using time-based filtering (see class comment)
	 */
	public boolean isTimeBased() {
		return (filter instanceof TimeFilter);
	}

	/**
	 * Reset the base time for the given directory.
	 * <p/>
	 * It's irrelevant if time-based filtering is not enabled (see class comment).
	 *
	 * @param the  directory for which to set the base time
	 * @param time the new base time
	 * @throws IllegalArgumentException if the given directory is not under control of the poller
	 */
	public void setBaseTime(File directory, long time) {
		for (int i = 0; i < dirs.length; i++)
			if (dirs[i].getAbsolutePath().equals(directory.getAbsolutePath())) {
				baseTime[i] = time;
				return;
			}
		throw new IllegalArgumentException("'" + directory + "' is not under control of the poller");
	}

	/**
	 * Reset the base time for the all the directories under control of the poller.
	 * <p/>
	 * It's irrelevant if time-based filtering is not enabled (see class comment).
	 *
	 * @param time the new base time
	 */
	public void setBaseTime(long time) {
		for (int i = 0; i < dirs.length; i++)
			setBaseTime(dirs[i], time);
	}

	/**
	 * Return the current base time for the given directory.
	 * <p/>
	 * The returned value is unpredictable if time-based filtering is not enabled (see class comment).
	 *
	 * @param the directory for which to get the base time
	 * @return the current base time, if time-based filtering is enabled
	 * @throws IllegalArgumentException if the given directory is not under control of the poller
	 */
	public long getBaseTime(File directory) {
		for (int i = 0; i < dirs.length; i++) {
			if (dirs[i].getAbsolutePath().equals(directory.getAbsolutePath())) return baseTime[i];
		}

		throw new IllegalArgumentException("'" + directory + "' is not under control of the poller");
	}


	/**
	 * Return <b>true</b> if the poller is time based, and uses last-polling time as a basis for the lookup (see class
	 * comment)
	 */
	public boolean isPollingTimeBased() {
		return isTimeBased() && timeBasedOnLastLookup;
	}

	/**
	 * Sets the subtype of time-based filtering used by the poller.
	 * <p/>
	 * For the call to have any meaning, the poller must be in time-based mode, that is, {@link
	 * DirectoryPoller#setTimeBased(boolean) setTimeBased()} must have been called with <b>true</b> as parameter (se class
	 * comment): if the parameter is <b>true</b>, the poller will select only files older than the last polling time
	 * (besides applying any user-defined filter); if the parameter is <b>false</b>, the poller will select only files
	 * <i>whose last modification time is higher than the higher last modification time found in the last polling
	 * cycle</i>.
	 *
	 * @param v determines the time-based filtering subtype
	 */
	public void setPollingTimeBased(boolean v) {
		timeBasedOnLastLookup = v;
	}

	/**
	 * Instruct the poller whether to notify per-file events to the listeners or not (see class comment).
	 *
	 * @param v if <b>true</b>, the poller will notify each file to the listeners
	 */
	public void setSendSingleFileEvent(boolean v) {
		sendSingleFileEvent = v;
	}

	/**
	 * Return <b>true</b> if the poller is currently instructed to send per-file events to the listeners (see class
	 * comment).
	 *
	 * @return <b>true</b> if the poller is currently instructed to send per-file events to the listeners
	 */
	public boolean isSendSingleFileEvent() {
		return sendSingleFileEvent;
	}


	/**
	 * Return the current poll interval. See class comments for notes.
	 *
	 * @return the current poll interval
	 */
	public long getPollInterval() {
		return pollInterval;
	}

	/**
	 * Set the poll interval. The poller sleeps for <tt>pollInterval</tt> milliseconds and then performs a lookup in the
	 * bound directories. See class comments for notes.
	 *
	 * @param pollInterfval the poll interval
	 */
	public void setPollInterval(long pollInterval) {
		this.pollInterval = pollInterval;
	}

	/**
	 * Instruct the poller whether to start by sweeping the controlled directories, or by going immediatly to sleep.
	 *
	 * @param v if <b>true</b> the poller starts by going immediatly to sleep
	 */
	public void setStartBySleeping(boolean v) {
		startBySleeping = v;
	}


	/**
	 * Return if the poller starts by sweeping the controlled directories, or going immediatly to sleep.
	 *
	 * @return if the poller starts by sweeping the controlled directories, or going immediatly to sleep.
	 */
	public boolean isStartBySleeping() {
		return startBySleeping;
	}

	/**
	 * Adds on {@link PollManager PollManager} to the poller, which will be notified on polling events.
	 * <p/>
	 * You may add many poll managers, but there is no support for inter-poll manager coordination, so if a PollManager
	 * deletes/moves a polled file, the others will still receive the associated event, but may not be able to perform
	 * proper processing.
	 */
	public void addPollManager(PollManager pm) {
		pollManagersList.add(pm);
		addListener(new DefaultListener(this, pm));
	}

	/**
	 * Request the poller to shut down.
	 * <p/>
	 * If a notification is in course, the notification is completed before shutting down. Therefore, the event handler may
	 * want to check whether a shutdown has been requested before reacting to an event.
	 */
	public void shutdown() {
		shutdownRequested = true;
		this.interrupt();
		if (verbose)
			System.out.println("Polling shutdown requested");
	}

	/**
	 * Return true if a shutdown has been requested.
	 *
	 * @return true if a shutdown has been requested.
	 */
	public boolean isShuttingDown() {
		return shutdownRequested;
	}

	/**
	 * Invoked when the thread is started.
	 * <p/>
	 * Performs a polling, notifying the registered listeners of related events. After each nofication, a check is done of
	 * whether a shutdown has been requested or not.
	 */
	public synchronized void run() {

		System.out.println("********************************************************");
		System.out.println("* DirectoryPoller 1.5.2 (C) Cristiano Sadun under LGPL *");
		System.out.println("********************************************************");

		shutdownRequested = false;
		if (dirs == null) throw new IllegalStateException("Programming error: no directories to poll specified");
		if (verbose)
			System.out.println("Polling started, interval is " + pollInterval + "ms");

		if (autoMove) {
			// Try to create the automove dirs
			for (int j = 0; j < dirs.length; j++) {
				File automoveDir = PathNormalizer.normalize(getAutoMoveDirectory(dirs[j]));
				if (!automoveDir.exists()) {
					if (verbose)
						System.out.println("Automove directory " + automoveDir + " does not exist, attempting to create.");
					if (!automoveDir.mkdirs())
						throw new RuntimeException("Could not create the directory " + automoveDir.getAbsolutePath());
					if (verbose)
						System.out.println("Automove directory " + automoveDir + " created successfully.");
				}
			}
		}

		// Main loop
		do {
			// Immediatly go to sleep if startBySleeping==true...
			if (startBySleeping) {
				startBySleeping = false; // ..and reset it so we dont *only* sleep.
			}
			else {
				runCycle();
			}

			// Go to sleep
			if (!shutdownRequested)
				try {
					sleeping = true;
					sleep(pollInterval);
					sleeping = false;
					if (verbose) {
						System.out.println("Poller waking up");
					}
				} catch (InterruptedException e) {
					//System.out.println("Sleep interrupted");
				}
		} while (!shutdownRequested);
		if (verbose) System.out.println("Poller terminated.");
	}

	private void runCycle() {

		// Notify wakeup
		if (!shutdownRequested)
			notify(new CycleStartEvent(this));

		// Initiate directories lookup. currentDir is a member
		// used also by the TimeFilenameFilter
		if (!shutdownRequested)
			for (currentDir = 0; currentDir < dirs.length; currentDir++) {
				File dir = PathNormalizer.normalize(dirs[currentDir]);
				File originalDir = dir;

				// Notify directory lookup start
				notify(new DirectoryLookupStartEvent(this, dir));
				if (shutdownRequested) return;

				long filesLookupTime = System.currentTimeMillis();
				// Get the files
				File[] fls = dir.listFiles(filter);
				if (fls == null) {
					System.err.println("Warning: directory " + dir + " does not exist");
					fls = new File[0];
				}

				// DLamy:  Additionally filter out hidden files
				List<File> visibleFiles = new ArrayList<File>();
				for (File f : fls) {
					if (!f.isHidden()) {
						visibleFiles.add(f);
					}
				}

				// STUD-419:
				//
				// Deletes AppleDouble files prior to ingestion
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
				File[] appleDoubles = dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.startsWith("._");
					}
				});
				for (File appleDouble : appleDoubles) {
					boolean deleted = appleDouble.delete();
					if (!deleted) {
						logger.warn("Unable to delete AppleDouble file: " + appleDouble.getAbsolutePath());
					}
					else {
						if (logger.isTraceEnabled()) {
							logger.trace("Deleted AppleDouble file: " + appleDouble.getAbsolutePath());
						}
					}
				}

				fls = visibleFiles.toArray(new File[0]);

				// Sort if required
				if (filesSortComparator != null) {
					if (verbose)
						System.out.println("Sorting files by  " + filesSortComparator);
					Arrays.sort(fls, filesSortComparator);
				}

				// Extract names (this could be factored out if it affects perfomance, my original code
				// was using just the file names)
				String[] files = new String[fls.length];
				for (int i = 0; i < files.length; i++) files[i] = fls[i].getName();

				// If autoMove, then move the files in their destination
				// directory
				String[] movedFiles = new String[files.length]; // Only for autoMove mode
				int failedToMoveCount = 0;
				if (autoMove) {
					File autoMoveDir = getAutoMoveDirectory(dir);
					for (int j = 0; j < files.length; j++) {
						File orig = new File(dir, files[j]);
						File dest = new File(autoMoveDir, files[j]);

						if (dest.exists()) {
							// Delete the existing file. Notify if failed.
							if (verbose)
								System.out.println("[Automove] Attempting to delete existing " + dest.getAbsolutePath());
							if (!dest.delete()) {
								notify(new ExceptionSignal(new AutomoveDeleteException(orig, dest, "Could not delete " + dest.getAbsolutePath()), this));
								failedToMoveCount++;
								continue;
							}
							else if (verbose)
								System.out.println("[Automove] Deleted " + dest.getAbsolutePath());
						}

						// Move the file - notify the listeners if an exception occurs
						if (verbose)
							System.out.println("[Automove] Moving " + orig.getAbsolutePath() + " to " + autoMoveDir.getAbsolutePath() + File.separator);

						autoMoveDir.mkdirs();

						try {
							boolean proceed;

							// Check for locks if necessary
							if (bypassLockedFiles) {
								RandomAccessFile raf = new RandomAccessFile(orig, "rw");
								FileChannel channel = raf.getChannel();
								if (channel.tryLock() == null) { // File is locked
									if (verbose)
										System.out.println("[Automove] File " + orig.getAbsolutePath() + " is locked, ignoring");
									failedToMoveCount++;
									proceed = false;
								}
								else {
									// A lock was acquired, so file transfer was complete: proceed.
									proceed = true;
								}
								channel.close(); // Unlock in either case
							}
							else proceed = true;

							// "proceed" is true only if file was not locked, or lock check is disabled
							if (proceed) {
								if (!orig.renameTo(dest)) {
									notify(new ExceptionSignal(new AutomoveException(orig, dest, "Could not move " + orig.getName() + " to " + dest.getAbsolutePath()), this));
									failedToMoveCount++;
								}
								else {
									//movedFiles[j]=autoMoveDir.getAbsolutePath()+File.separator+dest.getName();
									notify(new FileMovedEvent(this, orig, dest));
									movedFiles[j] = dest.getName();
									if (verbose)
										System.out.println("[Automove] Moved " + orig.getAbsolutePath() + " to " + autoMoveDir.getAbsolutePath() + File.separator);
								}
							}

						} catch (FileNotFoundException e) {
							notify(new ExceptionSignal(new AutomoveException(orig, dest, "Could not verify lock on " + orig.getName()), this));
							failedToMoveCount++;
						} catch (IOException e) {
							notify(new ExceptionSignal(new AutomoveException(orig, dest, "Tentative lock attempt failed on " + orig.getName()), this));
							failedToMoveCount++;
						}


					}
				}

				// Notify the file set. (1.2.2b fix by Doug.Liao@fnf.com)
				if (autoMove) {
					// Shrink the array if needed, to avoid nulls due to files which
					// have failed to move
					String[] tmp = new String[files.length - failedToMoveCount];
					int c = 0;
					for (int i = 0; i < movedFiles.length; i++)
						if (movedFiles[i] != null)
							tmp[c++] = movedFiles[i];
					files = tmp;
				}

				// dl: 6/19/03
				if (files.length > 0) {
					// DLamy (8/21/13):  Make sure FileSetFoundEvent is pointing at the file in the received folder.
					File baseDir = (autoMove ? getAutoMoveDirectory(dir) : dir);
					notify(new FileSetFoundEvent(this, baseDir, files));
				}
				else {
					//System.out.println("No file found");
				}
				if (shutdownRequested) return;


				if (sendSingleFileEvent) {
					// Notify each file
					for (int j = 0; j < files.length; j++) {
						File file;

                        /*if (autoMove) file=new File(files[j]);
                        else */
						file = new File(dir, files[j]);
						// Notify file found
						notify(new FileFoundEvent(this, file));
						if (shutdownRequested) return;
					}
					if (shutdownRequested) return;


				}

				// Make sure that baseTime is set to the higher modified time
				// of the files being read

				if (isTimeBased()) {

					if (verbose) System.out.println("Computing new base time");
					// compute new base time, depending on the working mode
					if (timeBasedOnLastLookup) {

						baseTime[currentDir] = filesLookupTime; // Last lookup time
					}
					else {

						for (int j = 0; j < files.length; j++) { // Highest file time
							File file = new File(dir, files[j]);
							long lastModifiedTime = file.lastModified();
							if (lastModifiedTime > baseTime[currentDir]) {
								baseTime[currentDir] = lastModifiedTime;
							}
						}
						if (verbose)
							System.out.println("Basetime for " + dirs[currentDir] + " is " + baseTime[currentDir]);
					}

				}

				// Notify directory lookup end
				notify(new DirectoryLookupEndEvent(this, originalDir));
			}

		// Notify go to sleep
		if (!shutdownRequested)
			notify(new CycleEndEvent(this, baseTime));
	}

	/**
	 * Get the current filter
	 */
	public FilenameFilter getFilter() {
		return filter;
	}

	/**
	 * Set the current filter. This can be invoked only when the poller is not running.
	 *
	 * @param filter the new filename filter to use.
	 */
	public void setFilter(FilenameFilter filter) {
		if (isAlive())
			throw new IllegalStateException("Can't call setFilter when the poller has already started");
		this.filter = filter;
	}

	/**
	 * Return the comparator to use to order file found events. Returns <b>null</b> if no comparator is set.
	 *
	 * @return the comparator to use to order file found events. Returns <b>null</b> if no comparator is set.
	 */
	public Comparator getFilesSortComparator() {
		return filesSortComparator;
	}

	/**
	 * Set the comparator to use to order file found events. Use <b>null</b> to set no comparator.
	 * <p/>
	 * The comparator receives File objects and decides on their respective order.
	 * <p/>
	 * Two pre-packaged comparators {@link DirectoryPoller.ModificationTimeComparator} and {@link
	 * DirectoryPoller.FileSizeComparator} are provided by this class.
	 *
	 * @param filesSortComparator
	 */
	public void setFilesSortComparator(Comparator filesSortComparator) {
		this.filesSortComparator = filesSortComparator;
	}

	/**
	 * Get the value of the BypassLockedFiles property. If the property is true, locked files will be ignored in automove
	 * mode.
	 *
	 * @return the value of the BypassLockedFiles property.
	 */
	public boolean isBypassLockedFiles() {
		return bypassLockedFiles;
	}

	/**
	 * Set the value of the BypassLockedFiles property. If the property is true, locked files will be ignored in automove
	 * mode.
	 */
	public void setBypassLockedFiles(boolean supportSlowTransfer) {
		this.bypassLockedFiles = supportSlowTransfer;
	}

	public boolean isSleeping() {
		return sleeping;
	}

	/**
	 * Turns on exception debugging. In case of exception signals are raised by the poller, they will be printed on
	 * standard output
	 *
	 * @return true if exception debugging is active, false otherwise
	 */
	public boolean isDebugExceptions() {
		return debugExceptions;
	}

	/**
	 * Turns on exception debugging. In case of exception signals are raised by the poller, they will be printed on
	 * standard output
	 *
	 * @param debugExceptions true to enable exception printing, false otherwise
	 */
	public void setDebugExceptions(boolean debugExceptions) {
		this.debugExceptions = debugExceptions;
	}

}