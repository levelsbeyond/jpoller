package org.sadun.util.polling;

import java.io.File;

/**
 * Clients implementing this interface and registering with {@link
 * DirectoryPoller#addPollManager(org.sadun.util.polling.PollManager)
 * DirectoryPoller.addPollManager()} receive method calls
 * whenever a polling event occur.
 *
 * @author C. Sadun
 * @version 1.0
 */
public interface PollManager {

 /**
  * Invoked when a polling cycle start (the pooler has just awaken)
  * @param evt the cycle start event
  */
 public void cycleStarted(CycleStartEvent evt);

 /**
  * Invoked when a polling cycle has ended (the pooler is going to sleep)
  * @param evt the cycle end event
  */
 public void cycleEnded(CycleEndEvent evt);

 /**
  * Invoked when the poller is starting to look into a directory
  * @param evt the lookup start event
  */
 public void directoryLookupStarted(DirectoryLookupStartEvent evt);

 /**
  * Invoked when the poller has finished looking into a directory
  * @param evt the lookup end event
  */
 public void directoryLookupEnded(DirectoryLookupEndEvent evt);

 /**
  * Invoked when the poller has found a set of file matching the
  * current criteria
  * @param evt the file set found event
  */
 public void fileSetFound(FileSetFoundEvent evt);

 /**
  * If the automove mode is active, is invoked for each automatic move
  * operation executed by the poller.
  */
 public void fileMoved(FileMovedEvent evt);

 /**
  * Invoked for each of the files found, if {@link
  * DirectoryPoller#setSendSingleFileEvent(boolean) setSendSingleFileEvent()}
  * has been invoked before starting the poller thread.
  * @param evt the file set found event
  */
 public void fileFound(FileFoundEvent evt);

 /**
  * Invoked when an automove operation fails since the given file
  * cannot be deleted
  * @param target the file that caused the exception
  */
 public void exceptionDeletingTargetFile(File target);

 /**
  * Invoked when an automove operation fails since the given file
  * cannot be moved to the given destination
  * @param file the file that caused the exception
  * @param dest the expected destination
  */
 public void exceptionMovingFile(File file, File dest);

}