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

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.ObjectName;
import java.io.FilenameFilter;

/**
 * An MBean interface for the {@link DirectoryPoller directory poller}. Most methods are the one exposed by {@link
 * DirectoryPoller DirectoryPoller}; some additional methods are provided to ease management with consoles not treating
 * array values or nonprimitive (or String) objects
 *
 * @author cris
 * @version 1.4
 */
public interface ManagedDirectoryPollerMBean {

	public void setVerbose(boolean v);

	public boolean isVerbose();

	public void setAutoMove(boolean v);

	public boolean getAutoMove();

	public String getAutoMoveDirectoryPath(String directory) throws MBeanException;

	public void startUp() throws MBeanException;

	public boolean isRunning() throws MBeanException;

	public long getPollInterval();

	public void setPollInterval(long pollInterval);

	public void shutDown() throws MBeanException;

	public void removeControlledDirectory(String dir) throws MBeanException;

	public void addControlledDirectory(String dir) throws MBeanException;

	public String getControlledDirectory(int i) throws MBeanException;

	public int countControlledDirectories() throws MBeanException;

	public void setStartBySleeping(boolean v);

	public boolean isStartBySleeping();

	public void setSendSingleFileEvent(boolean v);

	public boolean isSendSingleFileEvent();

	public void setBaseTime(String directory, long time);

	public long getBaseTime(String directory);

	public String getControlledDirectories();

	public void setControlledDirectories(String commaSeparatedList);

	public String listControlledDirectories();

	public String listInstalledPollManagers();

	public boolean isTimeBased();

	public boolean isPollingTimeBased();

	public void setPollingTimeBased(boolean v);

	public void setPollManagerFactoryClass(String pollManagerFactoryClsName) throws InstantiationException, IllegalAccessException, ClassNotFoundException;

	public String getPollManagerFactoryClass();

	public String getPollManagerFactory();

	public void setUsingJMXTimer(boolean v);

	public boolean isUsingJMXTimer();

	public String getJMXTimerObjectName();

	public void setJMXTimerObjectName(String jMXTimerObjectName);

	public void setAutoMoveDirectoryPath(String directory, String automoveDirectory);

	public FilenameFilter getFilter();
	//public void setFilter(FilenameFilter filter);

	public void setFilenameFilterFactoryClass(String filenameFilterFactoryClsName) throws InstantiationException, IllegalAccessException, ClassNotFoundException;

	public String getFilenameFilterFactoryClass();

	public String getFilenameFilterFactory();

	public void setAcceptedFilenamePattern(String filenamePattern);

	public String getAcceptedFilenamePattern();

	public void setEventsOrdering(String expr);

	public String getEventsOrdering();

	public void setFilesSortComparatorClass(String fileComparatorClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException;

	public String getFilesSortComparatorClass();

	public void setJMXSequenceNumberGeneratorClass(String sequenceNumberGeneratorClass) throws MBeanException;

	public String getJMXSequenceNumberGeneratorClass();

	public void addNotificationListener(String mbeanServerName, ObjectName objectName) throws InstanceNotFoundException;

	public void addNotificationListener(ObjectName objectName) throws InstanceNotFoundException;

	public void removeNotificationListener(ObjectName objectName) throws InstanceNotFoundException, ListenerNotFoundException;

	public void removeNotificationListener(String mbeanServerName, ObjectName objectName) throws InstanceNotFoundException, ListenerNotFoundException;

	public boolean isBypassLockedFiles();

	public void setBypassLockedFiles(boolean supportSlowTransfer);

	public boolean isDebugExceptions();

	public void setDebugExceptions(boolean debugExceptions);

	public void setTimeBased(boolean v);


}

