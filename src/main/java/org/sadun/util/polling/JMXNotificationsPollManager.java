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

import javax.management.ListenerNotFoundException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.io.File;

/**
 * A PollManager which produces JMX notifications corresponding to base DirectoryPoller events.
 *
 * @author Cristiano Sadun
 */
class JMXNotificationsPollManager implements PollManager {

	private NotificationBroadcasterSupport ns = new NotificationBroadcasterSupport();

	private final static boolean debug = false;

	private SequenceNumberGenerator sqg;
	private ObjectName pollerName;


	public JMXNotificationsPollManager(ObjectName pollerName, SequenceNumberGenerator sqg) {
		this.pollerName = pollerName;
		this.sqg = sqg;
	}

	public void cycleEnded(CycleEndEvent evt) {
		if (debug) logEvent(evt);
		ns.sendNotification(new CycleEndJMXNotification(pollerName, sqg, evt));
	}

	public void cycleStarted(CycleStartEvent evt) {
		if (debug) logEvent(evt);
		ns.sendNotification(new CycleStartJMXNotification(pollerName, sqg, evt));
	}

	public void directoryLookupEnded(DirectoryLookupEndEvent evt) {
		if (debug) logEvent(evt);
		ns.sendNotification(new DirectoryLookupEndJMXNotification(pollerName, sqg, evt));
	}

	public void directoryLookupStarted(DirectoryLookupStartEvent evt) {
		if (debug) logEvent(evt);
		ns.sendNotification(new DirectoryLookupStartJMXNotification(pollerName, sqg, evt));
	}

	public void exceptionDeletingTargetFile(File target) {
		if (debug) logException(target, null);
		ns.sendNotification(new ExceptionDeletingTargetFileJMXNotification(pollerName, sqg, target));
	}

	public void exceptionMovingFile(File file, File dest) {
		if (debug) logException(file, dest);
		ns.sendNotification(new ExceptionMovingFileJMXNotification(pollerName, sqg, file, dest));
	}

	public void fileFound(FileFoundEvent evt) {
		if (debug) logEvent(evt);
		ns.sendNotification(new FileFoundJMXNotification(pollerName, sqg, evt));
	}

	public void fileMoved(FileMovedEvent evt) {
		if (debug) logEvent(evt);
		ns.sendNotification(new FileMovedJMXNotification(pollerName, sqg, evt));
	}

	public void fileSetFound(FileSetFoundEvent evt) {
		if (debug) logEvent(evt);
		ns.sendNotification(new FileSetFoundJMXNotification(pollerName, sqg, evt));
	}

	public void addListener(NotificationListener listener, NotificationFilter filter, Object handback) {
		ns.addNotificationListener(listener, filter, handback);
	}

	public void removeListener(NotificationListener listener) throws ListenerNotFoundException {
		ns.removeNotificationListener(listener);

	}

	private static void logEvent(BasePollerEvent evt) {
		System.out.println(evt);
	}

	private static void logException(File target, File dest) {
		System.out.println("Exception " + (dest != null ? "moving" : "deleting") + " " + target);
	}


	public String toString() {
		return "An internal pollmanager dispatching JMX notifications corresponding to basic events";
	}

}
