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
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.sadun.util.polling.jboss.ManagedDirectoryPollerService;

/*
 * Created on Sep 13, 2004
 */

/**
 * An instrumentation of {@link ManagedDirectoryPoller} which implements {@link InstrumentedManagedDirectoryPollerMBean}
 * and prints every invoked method on standard output. Useful only for debugging purposes.
 *
 * @author Cristiano Sadun
 */
public class InstrumentedManagedDirectoryPoller
	implements
	InstrumentedManagedDirectoryPollerMBean, MBeanRegistration, NotificationListener {


	protected ManagedDirectoryPollerMBean mdp;
	private ManagedDirectoryPoller mdpImpl;

	public InstrumentedManagedDirectoryPoller() {
		this.mdpImpl = new ManagedDirectoryPoller();
		this.mdp = this.mdpImpl;
		System.out.println("InstrumentedManagedDirectoryPoller created");
	}

	protected InstrumentedManagedDirectoryPoller(ManagedDirectoryPollerMBean mdp) {
		this.mdp = mdp;
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setVerbose(boolean)
	 */
	public void setVerbose(boolean v) {
		printMethod();
		mdp.setVerbose(v);

	}

	private void printMethod() {
		StackTraceElement[] ste = new Exception().getStackTrace();
		System.out.println("Invoking " + ste[1].getMethodName());
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isVerbose()
	 */
	public boolean isVerbose() {
		printMethod();
		return mdp.isVerbose();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setAutoMove(boolean)
	 */
	public void setAutoMove(boolean v) {
		printMethod();
		mdp.setAutoMove(v);

	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getAutoMove()
	 */
	public boolean getAutoMove() {
		printMethod();
		return mdp.getAutoMove();

	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getAutoMoveDirectoryPath(java.lang.String)
	 */
	public String getAutoMoveDirectoryPath(String directory)
		throws MBeanException {
		printMethod();
		return mdp.getAutoMoveDirectoryPath(directory);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#startUp()
	 */
	public void startUp() throws MBeanException {
		printMethod();
		mdp.startUp();

	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isRunning()
	 */
	public boolean isRunning() throws MBeanException {
		printMethod();
		return mdp.isRunning();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getPollInterval()
	 */
	public long getPollInterval() {
		printMethod();
		return mdp.getPollInterval();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setPollInterval(long)
	 */
	public void setPollInterval(long pollInterval) {
		printMethod();
		mdp.setPollInterval(pollInterval);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#shutDown()
	 */
	public void shutDown() throws MBeanException {
		printMethod();
		mdp.shutDown();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#removeControlledDirectory(java.lang.String)
	 */
	public void removeControlledDirectory(String dir) throws MBeanException {
		printMethod();
		mdp.removeControlledDirectory(dir);

	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#addControlledDirectory(java.lang.String)
	 */
	public void addControlledDirectory(String dir) throws MBeanException {
		printMethod();
		mdp.addControlledDirectory(dir);

	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getControlledDirectory(int)
	 */
	public String getControlledDirectory(int i) throws MBeanException {
		printMethod();
		return mdp.getControlledDirectory(i);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#countControlledDirectories()
	 */
	public int countControlledDirectories() throws MBeanException {
		printMethod();
		return mdp.countControlledDirectories();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setStartBySleeping(boolean)
	 */
	public void setStartBySleeping(boolean v) {
		printMethod();
		mdp.setStartBySleeping(v);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isStartBySleeping()
	 */
	public boolean isStartBySleeping() {
		printMethod();
		return mdp.isStartBySleeping();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setSendSingleFileEvent(boolean)
	 */
	public void setSendSingleFileEvent(boolean v) {
		printMethod();
		mdp.setSendSingleFileEvent(v);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isSendSingleFileEvent()
	 */
	public boolean isSendSingleFileEvent() {
		printMethod();
		return mdp.isSendSingleFileEvent();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setBaseTime(java.lang.String, long)
	 */
	public void setBaseTime(String directory, long time) {
		printMethod();
		mdp.setBaseTime(directory, time);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getBaseTime(java.lang.String)
	 */
	public long getBaseTime(String directory) {
		printMethod();
		return mdp.getBaseTime(directory);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getControlledDirectories()
	 */
	public String getControlledDirectories() {
		printMethod();
		return mdp.getControlledDirectories();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setControlledDirectories(java.lang.String)
	 */
	public void setControlledDirectories(String commaSeparatedList) {
		printMethod();
		mdp.setControlledDirectories(commaSeparatedList);

	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#listControlledDirectories()
	 */
	public String listControlledDirectories() {
		printMethod();
		return mdp.listControlledDirectories();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#listInstalledPollManagers()
	 */
	public String listInstalledPollManagers() {
		printMethod();
		return mdp.listInstalledPollManagers();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isTimeBased()
	 */
	public boolean isTimeBased() {
		printMethod();
		return mdp.isTimeBased();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isPollingTimeBased()
	 */
	public boolean isPollingTimeBased() {
		printMethod();
		return mdp.isPollingTimeBased();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setPollingTimeBased(boolean)
	 */
	public void setPollingTimeBased(boolean v) {
		printMethod();
		mdp.setPollingTimeBased(v);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setPollManagerFactoryClass(java.lang.String)
	 */
	public void setPollManagerFactoryClass(String pollManagerFactoryClsName)
		throws InstantiationException, IllegalAccessException,
		ClassNotFoundException {
		printMethod();
		mdp.setPollManagerFactoryClass(pollManagerFactoryClsName);

	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getPollManagerFactoryClass()
	 */
	public String getPollManagerFactoryClass() {
		printMethod();
		return mdp.getPollManagerFactoryClass();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getPollManagerFactory()
	 */
	public String getPollManagerFactory() {
		printMethod();
		return mdp.getPollManagerFactory();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setUsingJMXTimer(boolean)
	 */
	public void setUsingJMXTimer(boolean v) {
		printMethod();
		mdp.setUsingJMXTimer(v);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isUsingJMXTimer()
	 */
	public boolean isUsingJMXTimer() {
		printMethod();
		return mdp.isUsingJMXTimer();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getJMXTimerObjectName()
	 */
	public String getJMXTimerObjectName() {
		printMethod();
		return mdp.getJMXTimerObjectName();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setJMXTimerObjectName(java.lang.String)
	 */
	public void setJMXTimerObjectName(String jMXTimerObjectName) {
		printMethod();
		mdp.setJMXTimerObjectName(jMXTimerObjectName);
	}


	public FilenameFilter getFilter() {
		printMethod();
		return mdp.getFilter();
	}

	/*	
	public void setFilter(FilenameFilter filter) {
		printMethod();
		mdp.setFilter(filter);
	}
	
	*/

	public String getAcceptedFilenamePattern() {
		printMethod();
		return mdp.getAcceptedFilenamePattern();
	}

	public String getFilenameFilterFactory() {
		printMethod();
		return mdp.getFilenameFilterFactory();
	}

	public String getFilenameFilterFactoryClass() {
		printMethod();
		return mdp.getFilenameFilterFactoryClass();
	}

	public void setAcceptedFilenamePattern(String filenamePattern) {
		printMethod();
		mdp.setAcceptedFilenamePattern(filenamePattern);
	}

	public void setFilenameFilterFactoryClass(
		String filenameFilterFactoryClsName) throws InstantiationException,
		IllegalAccessException, ClassNotFoundException {
		printMethod();
		mdp.setFilenameFilterFactoryClass(filenameFilterFactoryClsName);
	}

	public void postDeregister() {
		mdpImpl.postDeregister();

	}

	public void postRegister(Boolean arg0) {
		mdpImpl.postRegister(arg0);
	}

	public void preDeregister() throws Exception {
		mdpImpl.preDeregister();
	}

	public ObjectName preRegister(MBeanServer arg0, ObjectName arg1)
		throws Exception {

		//throw new RuntimeException("Fake exception MBS: "+arg0+", ON: "+arg1+", MDP: "+mdp+", MDPIMPL: "+mdpImpl);

		return ((ManagedDirectoryPollerService) mdp).preRegister(arg0, arg1);
	}

	public void handleNotification(Notification arg0, Object arg1) {
		mdpImpl.handleNotification(arg0, arg1);
	}

	public String getPropertyValues() {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		try {
			PropertyDescriptor[] pd = Introspector.getBeanInfo(getClass()).getPropertyDescriptors();
			for (int i = 0; i < pd.length; i++) {
				if (pd[i].getReadMethod() != null) {
					if (!pd[i].getName().equalsIgnoreCase("PropertyValues")) {
						Object result = pd[i].getReadMethod().invoke(this, new Object[0]);
						if (result != null)
							pw.println(pd[i].getName() + ": " + result.toString());
						else
							pw.println(pd[i].getName() + ": null");
					}
				}
			}
			return sw.toString();
		} catch (Exception e) {
			if (e instanceof RuntimeException) throw (RuntimeException) e;
			else throw new RuntimeException(e);
		}
	}

	public void setAutoMoveDirectoryPath(String directory, String automoveDirectory) {
		printMethod();
		mdp.setAutoMoveDirectoryPath(directory, automoveDirectory);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getFilesSortComparatorClass()
     */
	public String getFilesSortComparatorClass() {
		printMethod();
		return mdp.getFilesSortComparatorClass();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setFilesSortComparatorClass(java.lang.String)
	 */
	public void setFilesSortComparatorClass(String fileComparatorClassName)
		throws InstantiationException, IllegalAccessException,
		ClassNotFoundException {
		printMethod();
		mdp.setFilesSortComparatorClass(fileComparatorClassName);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setEventsOrdering(java.lang.String)
	 */
	public void setEventsOrdering(String expr) {
		printMethod();
		mdp.setEventsOrdering(expr);

	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getEventsOrdering()
	 */
	public String getEventsOrdering() {
		printMethod();
		return mdp.getEventsOrdering();
	}

	public String getJMXSequenceNumberGeneratorClass() {
		printMethod();
		return mdp.getJMXSequenceNumberGeneratorClass();
	}

	public void setJMXSequenceNumberGeneratorClass(String sequenceNumberGeneratorClass) throws MBeanException {
		printMethod();
		mdp.setJMXSequenceNumberGeneratorClass(sequenceNumberGeneratorClass);

	}

	public void addNotificationListener(ObjectName objectName) throws InstanceNotFoundException {
		printMethod();
		mdp.addNotificationListener(objectName);
	}

	public void addNotificationListener(String mbeanServerName, ObjectName objectName) throws InstanceNotFoundException {
		printMethod();
		mdp.addNotificationListener(mbeanServerName, objectName);
	}

	public void removeNotificationListener(ObjectName objectName) throws InstanceNotFoundException, ListenerNotFoundException {
		printMethod();
		mdp.removeNotificationListener(objectName);
	}

	public void removeNotificationListener(String mbeanServerName, ObjectName objectName) throws InstanceNotFoundException, ListenerNotFoundException {
		printMethod();
		mdp.removeNotificationListener(mbeanServerName, objectName);
	}

	public boolean isBypassLockedFiles() {
		printMethod();
		return mdp.isBypassLockedFiles();
	}

	public void setBypassLockedFiles(boolean supportSlowTransfer) {
		printMethod();
		mdp.setBypassLockedFiles(supportSlowTransfer);
	}

	public boolean isDebugExceptions() {
		printMethod();
		return mdp.isDebugExceptions();
	}

	public void setDebugExceptions(boolean debugExceptions) {
		printMethod();
		mdp.setDebugExceptions(debugExceptions);
	}

	public void setTimeBased(boolean v) {
		printMethod();
		mdp.setTimeBased(v);
	}

}
