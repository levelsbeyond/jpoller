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

import javax.management.Notification;
import javax.management.ObjectName;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.sadun.util.MovedFile;

/**
 * A base class for JMX notifications, containing the original event.
 *
 * @author Cristiano Sadun
 */
abstract class BaseJMXNotification extends Notification {

	protected BasePollerEvent event;

	/**
	 * Open constructor - requires specific initialization since no event is provide
	 *
	 * @param notificationType
	 * @param pollerName
	 * @param sqg
	 */
	protected BaseJMXNotification(String notificationType, ObjectName pollerName,
	                              SequenceNumberGenerator sqg, long timestamp) {
		super(notificationType, pollerName, sqg.getNextSequenceNumber(), timestamp);
	}

	protected BaseJMXNotification(String notificationType, ObjectName pollerName,
	                              SequenceNumberGenerator sqg, BasePollerEvent evt) {
		super(notificationType, pollerName, sqg.getNextSequenceNumber(), evt
			.getTime());
		commonInit(evt);
	}

	protected BaseJMXNotification(String notificationType, ObjectName pollerName,
	                              SequenceNumberGenerator sqg, BasePollerEvent evt, String message) {
		super(notificationType, pollerName, sqg.getNextSequenceNumber(), evt
			.getTime(), message);
		commonInit(evt);
	}

	private void commonInit(BasePollerEvent evt) {
		this.event = evt;
	}

	/**
	 * Note: this is null for exception notifications.
	 *
	 * @return the event underlying this notification, or null
	 */
	protected BasePollerEvent getEvent() {
		return event;
	}

	protected static String mkMsg(BasePollerEvent evt, String propertyName) {
		try {
			Method m = evt.getClass().getMethod("get" + propertyName, new Class[0]);

			Object obj = m.invoke(evt, new Object[0]);

			File file;
			if (obj instanceof File) file = (File) obj;
			else if (obj instanceof MovedFile) file = ((MovedFile) obj).getDestinationPath().getCanonicalFile();
			else
				throw new RuntimeException("Internal error: only File or MovedFile objects are expected by this method");
			try {
				return file.getCanonicalPath();
			} catch (IOException e) {
				return file.getAbsolutePath();
			}
		} catch (Exception e) {
			throw new RuntimeException("Internal error: only events with a get" + propertyName + "() method should be passed to this class. The passed event has class " + evt.getClass(), e);
		}
	}

	/**
	 * Return the time of the original poller event.
	 *
	 * @return the time of the original poller event.
	 */
	public long getPollerEventTime() {
		return event.getTime();
	}

}
