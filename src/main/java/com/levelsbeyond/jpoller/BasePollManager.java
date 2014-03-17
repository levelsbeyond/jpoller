package com.levelsbeyond.jpoller;

import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.sadun.util.polling.FileFoundEvent;
import org.sadun.util.polling.FileSetFoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Versus {@link org.sadun.util.polling.BasePollManager}, this manager class is pretty opinionated.
 * It has concurrency control and can be configured to save or discard successfully processed files.
 * It understands the idea of successful vs failed files and will move the file into the correct
 * folder based on what happened.  If a failure occurs, it will write a log file with the detected
 * exception data.
 *
 * For subclasses, the methods to override are doValidate and doProcess.  doValidate optionally provides
 * the subclass the opportunity to review the configuration and fail if something is amiss.  doProcess
 * is the actual processing portion-- the subclass will only be called if maxConcurrent has not been met.
 * The return is a Future<Result>, which the calling thread will block on until a value is present, so
 * concurrency is possible here.
*/
public abstract class BasePollManager extends org.sadun.util.polling.BasePollManager {
	private static final Logger log = LoggerFactory.getLogger(BasePollManager.class);

	private CompletionService<Result> executorService;
	Thread resultSnifferThread;

	private File baseFolder;

	public void setWatchFolder(File watchFolder) {
		this.baseFolder = watchFolder;
	}

	public File getWatchFolder() {
		return baseFolder;
	}

	private int maxConcurrent = 1;

	public void setMaxConcurrent(int max) {
		this.maxConcurrent = max;
	}

	public int getMaxConcurrent() {
		return maxConcurrent;
	}

	private File completedFolder = null;

	public synchronized File getCompletedFolder() {
		if (completedFolder == null) {
			completedFolder = new File(baseFolder, "completed");
			completedFolder.mkdirs();
		}

		return completedFolder;
	}

	private File failedFolder = null;

	public synchronized File getFailedFolder() {
		if (failedFolder == null) {
			failedFolder = new File(baseFolder, "failed");
			failedFolder.mkdirs();
		}

		return failedFolder;
	}

	private File workingFolder = null;

	public synchronized File getWorkingFolder() {
		if (workingFolder == null) {
			workingFolder = new File(baseFolder, "working");
			workingFolder.mkdirs();
		}

		return workingFolder;
	}

	private boolean deleteOnSuccess = false;

	/**
	 * deleteOnSuccess may be specified in the spring config, if true then a source file will be deleted when a subclass
	 * calls complete( file ), otherwise the source file will be moved to completeFolder
	 */
	public void setDeleteOnSuccess(boolean inDeleteOnSuccess) {
		deleteOnSuccess = inDeleteOnSuccess;
	}

	private String[] validExtensions = new String[0];

	public void setValidExtensions(String[] extensions) {
		this.validExtensions = extensions;
	}

	public String[] getValidExtensions() {
		return validExtensions;
	}

	private int pollIntervalSeconds = 30;

	public void setPollIntervalSeconds(int seconds) {
		pollIntervalSeconds = seconds;
	}

	public int getPollIntervalSeconds() {
		return pollIntervalSeconds;
	}

	public final void validateAndStart() throws Exception {
		if (baseFolder == null) {
			throw new Exception("Watch folder is null");
		}

		// validate that this folder is readable
		if (!baseFolder.isDirectory()) {
			throw new Exception("Watch folder " + baseFolder.getAbsolutePath() + " is not a valid directory.");
		}

		doValidate();

		// configure thread pool
		executorService = new ExecutorCompletionService<Result>(Executors.newFixedThreadPool(maxConcurrent));

		resultSnifferThread = new Thread(new ResultSniffer(), "resultSniffer-" + baseFolder.getAbsolutePath());
		resultSnifferThread.setDaemon(true);
		resultSnifferThread.start();
	}

	/**
	 * subclasses or spawned processes may call complete to take the default action on a file that has been successfully
	 * processed. either delete it, or move it to a completed folder
	 */
	public void complete(File completedFile) {
		if (!completedFile.exists()) {
			log.warn("DUDE WTF?  The completed file has vanished.  Maybe this method has been called twice..?");
			return;
		}
		if (deleteOnSuccess) {
			log.debug("File " + completedFile.getAbsolutePath() + " is completed, deleting.");
			completedFile.delete();
		}
		else {
			File destDir = getCompletedFolder();
			log.debug("File " + completedFile.getAbsolutePath() + " is completed, moving to completed folder " + destDir.getAbsolutePath());
			File destFile = new File(destDir, completedFile.getName());
			if (destFile.exists()) {
				log.debug("Deleting existing completed folder file " + destFile.getAbsolutePath() + " before moving completed file in there.");
				destFile.delete();
			}
			boolean moved = completedFile.renameTo(destFile);
			if (!moved) {
				log.warn("Warning!  Could not move completed file " + completedFile.getAbsolutePath() + " to completed folder " + destDir.getAbsolutePath());
			}
		}
	}

	/**
	 * subclasses or spawned job processes may call fail( file, exception ) in order to move the processed file to the
	 * failed folder, and create an exception report containing the message & stack trace in a log file named )after the
	 * failed file
	 */

	public void fail(File failedFile, Throwable exception) {
		File failFolder = getFailedFolder();
		File destFile = new File(failFolder, failedFile.getName());
		File logFile = new File(failFolder, failedFile.getName() + ".log");
		if (destFile.exists()) {
			destFile.delete();
		}
		if (logFile.exists()) {
			logFile.delete();
		}

		failedFile.renameTo(destFile);
		if (exception != null) {
			try {
				PrintStream ps = new PrintStream(logFile);
				ps.println(exception.getMessage());
				exception.printStackTrace(ps);
				ps.close();
			}
			catch (Exception e) {
				log.error("Caught:", e);
				log.error("While logging:", exception);
			}
		}
	}

	@Override
	public void fileSetFound(FileSetFoundEvent evt) {
		log.debug("fileSetFound called:");
		for (File file : evt.getFiles()) {
			log.debug("\tGot file " + file.getAbsolutePath());
			addProcessingTask(file);
		}
	}

	@Override
	public void fileFound(FileFoundEvent evt) {
		log.debug("fileFound called, file = " + evt.getFile().getAbsolutePath());
		addProcessingTask(evt.getFile());
	}

	@Override
	public void exceptionMovingFile(File file, File dest) {
		log.warn("Couldn't automove file from " + file.getAbsolutePath() + " to " + dest.getAbsolutePath());
	}

	private void addProcessingTask(File file) {
		FileCallable callable = new FileCallable(file);
		executorService.submit(callable);
	}

	private Result process(File file) {
		Result retVal;
		try {
			retVal = doProcess(file).get();
		}
		catch (Exception ex) {
			retVal = Result.fail(file, ex);
		}

		return retVal;
	}

	private void processingCompleted(Future<Result> finishedTask) throws Exception {
		Result result = finishedTask.get();
		File file = result.sourceFile;

		if (file != null) {
			if (result.isSuccessful()) {
				log.debug("Successfully completed processing for file " + file.getAbsolutePath());
				complete(file);
			}
			else {
				log.warn("Failed to process file " + file.getAbsolutePath(), result.exception);
				fail(file, result.exception);
			}
		}

	}

	protected abstract Future<Result> doProcess(File file);

	protected abstract void doValidate() throws Exception;

	private class FileCallable implements Callable<Result> {
		private File sourceFile;

		public FileCallable(File file) {
			this.sourceFile = file;
		}

		@Override
		public Result call() throws Exception {
			Result result = null;

			// move the file to the working folder
			File destDir = getWorkingFolder();
			File destFile = new File(destDir, sourceFile.getName());
			if (destFile.exists()) {
				destFile.delete();
			}
			boolean moved = sourceFile.renameTo(destFile);
			if (!moved) {
				result = Result.fail(sourceFile,
						new Exception("Unable to move " + sourceFile.getAbsolutePath() + " to " + destFile.getAbsolutePath()));
			}
			else {
				result = process(destFile);
			}

			return result;
		}
	}

	public static class Result {
		private File sourceFile;
		private boolean successful;
		private Throwable exception;

		private Result(File file, boolean successful, Throwable exception) {
			this.sourceFile = file;
			this.successful = successful;
			this.exception = exception;
		}

		public File getSourceFile() {
			return sourceFile;
		}

		public boolean isSuccessful() {
			return successful;
		}

		public Throwable getException() {
			return exception;
		}

		public static Result success(File processedFile) {
			return new Result(processedFile, true, null);
		}

		public static Result fail(File processedFile, Throwable exception) {
			return new Result(processedFile, false, exception);
		}
	}

	private class ResultSniffer implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					Future<Result> finishedTask = executorService.take();
					processingCompleted(finishedTask);
				}
				catch (InterruptedException ex) {
					return;
				}
				catch (Exception ex) {
					log.warn("Received exeption while processing finished task:", ex);
				}
			}

		}

	}

}
