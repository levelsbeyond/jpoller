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

package org.sadun.util;

import java.io.File;

/**
 * A class to encapsulate the concept of a file which has been moved from a path to another, which simplify inversion of
 * the operation.
 * <p/>
 * The class provides an auto-synchronization mode (disable by default) which checks the paths with actual files on the
 * filesystem before performing an operation.
 * <p/>
 * The check will fail, however, if files for both original and destination path do exist (i.e. if a copy has taken
 * place).
 *
 * @author C. Sadun
 * @version 1.0
 */
public class MovedFile {

	private File original;
	private File destination;
	private boolean isOriginal;
	private boolean autoSync;

	/**
	 * Create a MovedFile object, with the given original and destination paths, and whose state is the given one.
	 *
	 * @param original    the original path for the move operation
	 * @param destination the destination path for the move operation
	 * @param isOriginal  <b>true</b> if the move operation has not occurred yet, <b>false</b> otherwise
	 */
	public MovedFile(File original, File destination, boolean isOriginal) {
		this.original = original;
		this.destination = destination;
		this.isOriginal = isOriginal;
	}

	/**
	 * Create a MovedFile object, with the given original and destination paths, and whose state is automatically
	 * detected.
	 *
	 * @param original    the original path for the move operation
	 * @param destination the destination path for the move operation
	 */
	public MovedFile(File original, File destination) {
		this(original, destination, detectIsOriginal(original, destination));
	}

	/**
	 * Set the autosync mode on or off
	 *
	 * @param v if <b>true</b> the autosync mode is turned on
	 */
	public void setAutosync(boolean v) {
		autoSync = v;
	}

	/**
	 * Get the autosync mode
	 *
	 * @return <b>true</b> if the autosync mode is on
	 */
	public boolean getAutosync() {
		return autoSync;
	}

	/**
	 * Invert the move operation. If the file had been moved, it's restored in its original position, and viceversa.
	 *
	 * @return <b>true</b> if the operation is successful.
	 */
	public synchronized boolean invert() {
		if (autoSync) sync();
		return invert();
	}

	private boolean invert0() {
		File f1, f2;
		f1 = (isOriginal ? original : destination);
		f2 = (isOriginal ? destination : original);
		return f1.renameTo(f2);
	}

	/**
	 * Revert the move operation. If the file had been moved, it's restored in its original position. If it's already
	 * reverted, a RuntimeException will be raised.
	 *
	 * @return <b>true</b> if the operation is successful.
	 * @throws RuntimeException if the file is in wrong state
	 */
	public synchronized boolean revert() {
		if (autoSync) sync();
		if (isOriginal) throw new RuntimeException("File already reverted:" + original.getAbsolutePath());
		return invert0();
	}

	/**
	 * Perform again the move operation. If the file had been reverted, it's moved in its previous "moved" position. If
	 * it's already moved, a RuntimeException will be raised.
	 *
	 * @return <b>true</b> if the operation is successful.
	 * @throws RuntimeException if the file is in wrong state
	 */
	public synchronized boolean moveAgain() {
		if (autoSync) sync();
		if (!isOriginal) throw new RuntimeException("File already moved:" + destination.getAbsolutePath());
		return invert0();
	}

	/**
	 * Synchronize the state of the object with the filesystem
	 *
	 * @throws RuntimeException if a filesystem file exists for both original and destination paths.
	 */
	public void sync() {
		isOriginal = detectIsOriginal(original, destination);
	}

	/**
	 * Return <b>true</b> if the file is in the "moved" path.
	 *
	 * @return <b>true</b> if the file is in the "moved" path.
	 */
	public boolean isMoved() {
		if (autoSync) sync();
		return !isOriginal;
	}

	/**
	 * Return the original path of the file. Since the file has been moved, this File object does not correspond anymore to
	 * a physical file on the filesystem.
	 *
	 * @return the original path of the file.
	 */
	public File getOriginalPath() {
		return original;
	}

	/**
	 * Return the destination path of the file. Since the file has been moved, this File object corresponds to the physical
	 * file on the filesystem.
	 *
	 * @return the destination path of the file.
	 */
	public File getDestinationPath() {
		return destination;
	}

	private static boolean detectIsOriginal(File original, File destination) {
		boolean e1, e2;
		if ((e1 = original.exists()) && (e2 = destination.exists()))
			throw new RuntimeException(
				"Both " + original.getAbsolutePath() + " and " +
					destination.getAbsolutePath() +
					" exist. Can't auto detect state for MovedFile object"
			);
		return e1;
	}


}