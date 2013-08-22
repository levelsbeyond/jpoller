package org.sadun.util.polling.jboss;

import java.io.File;
import java.io.FilenameFilter;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.sadun.util.polling.ManagedDirectoryPoller;

/**
 * An extension to the {@link org.sadun.util.polling.ManagedDirectoryPoller 
 * ManagedDirectoryPoller} mbean implementation which exposes JBoss' service
 * interface.
 * 
 * @author cris
 */
public class ManagedDirectoryPollerService
	implements ManagedDirectoryPollerServiceMBean, MBeanRegistration, NotificationListener {
		
	ManagedDirectoryPoller mPoller;

	/**
	 * Constructor for ManagedDirectoryPollerService.
	 * @param dirs
	 * @param filter
	 */
	public ManagedDirectoryPollerService(File[] dirs, FilenameFilter filter) {
		mPoller=new ManagedDirectoryPoller(dirs, filter);
		mPoller.setJMXTimerObjectName("DefaultDomain:service=timer");
	}

	/**
	 * Constructor for ManagedDirectoryPollerService.
	 * @param dirs
	 */
	public ManagedDirectoryPollerService(File[] dirs) {
		mPoller=new ManagedDirectoryPoller(dirs);
		mPoller.setJMXTimerObjectName("DefaultDomain:service=timer");
	}

	/**
	 * Constructor for ManagedDirectoryPollerService.
	 * @param directory
	 * @param filter
	 */
	public ManagedDirectoryPollerService(
		File directory,
		FilenameFilter filter) {
		mPoller=new ManagedDirectoryPoller(directory, filter);
		mPoller.setJMXTimerObjectName("DefaultDomain:service=timer");
	}

	/**
	 * Constructor for ManagedDirectoryPollerService.
	 * @param directory
	 */
	public ManagedDirectoryPollerService(File directory) {
		mPoller=new ManagedDirectoryPoller(directory);
		mPoller.setJMXTimerObjectName("DefaultDomain:service=timer");
	}

	/**
	 * Constructor for ManagedDirectoryPollerService.
	 * @param filter
	 */
	public ManagedDirectoryPollerService(FilenameFilter filter) {
		mPoller=new ManagedDirectoryPoller(filter);
		mPoller.setJMXTimerObjectName("DefaultDomain:service=timer");
	}

	/**
	 * Constructor for ManagedDirectoryPollerService.
	 */
	public ManagedDirectoryPollerService() {
		mPoller=new ManagedDirectoryPoller();
		mPoller.setJMXTimerObjectName("DefaultDomain:service=timer");
	}

	/**
	 * Constructor for ManagedDirectoryPollerService.
	 * @param dirs
	 * @param filter
	 * @param timeBased
	 */
	public ManagedDirectoryPollerService(
		File[] dirs,
		FilenameFilter filter,
		boolean timeBased) {
		mPoller=new ManagedDirectoryPoller(dirs, filter, timeBased);
		mPoller.setJMXTimerObjectName("DefaultDomain:service=timer");
	}

	/**
	 * Constructor for ManagedDirectoryPollerService.
	 * @param directory
	 * @param filter
	 * @param timeBased
	 */
	public ManagedDirectoryPollerService(
		File directory,
		FilenameFilter filter,
		boolean timeBased) {
		mPoller=new ManagedDirectoryPoller(directory, filter, timeBased);
		mPoller.setJMXTimerObjectName("DefaultDomain:service=timer");
	}

	/**
	 * Constructor for ManagedDirectoryPollerService.
	 * @param filter
	 * @param timeBased
	 */
	public ManagedDirectoryPollerService(
		FilenameFilter filter,
		boolean timeBased) {
		mPoller=new ManagedDirectoryPoller(filter, timeBased);
		mPoller.setJMXTimerObjectName("DefaultDomain:service=timer");
	}
	
	public void start() {
		mPoller.startUp();
	}
	/**
	 * @see org.sadun.util.polling.jboss.ManagedDirectoryPollerServiceMBean#stop()
	 */
	public void stop() {
		mPoller.shutdown();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#addControlledDirectory(java.lang.String)
	 */
	public void addControlledDirectory(String dir) throws MBeanException {
		mPoller.addControlledDirectory(dir);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#countControlledDirectories()
	 */
	public int countControlledDirectories() throws MBeanException {
		return mPoller.countControlledDirectories();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getAutoMove()
	 */
	public boolean getAutoMove() {
		return mPoller.getAutoMove();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getAutoMoveDirectoryPath(java.lang.String)
	 */
	public String getAutoMoveDirectoryPath(String directory)
		throws MBeanException {
		return mPoller.getAutoMoveDirectoryPath(directory);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getBaseTime(java.lang.String)
	 */
	public long getBaseTime(String directory) {
		return mPoller.getBaseTime(directory);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getControlledDirectories()
	 */
	public String getControlledDirectories() {
		return mPoller.getControlledDirectories();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getControlledDirectory(int)
	 */
	public String getControlledDirectory(int i) throws MBeanException {
		return mPoller.getControlledDirectory(i);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getPollInterval()
	 */
	public long getPollInterval() {
		return mPoller.getPollInterval();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isPollingTimeBased()
	 */
	public boolean isPollingTimeBased() {
		return mPoller.isPollingTimeBased();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isRunning()
	 */
	public boolean isRunning() throws MBeanException {
		return mPoller.isRunning();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isSendSingleFileEvent()
	 */
	public boolean isSendSingleFileEvent() {
		return mPoller.isSendSingleFileEvent();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isStartBySleeping()
	 */
	public boolean isStartBySleeping() {
		return mPoller.isStartBySleeping();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isTimeBased()
	 */
	public boolean isTimeBased() {
		return mPoller.isTimeBased();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isVerbose()
	 */
	public boolean isVerbose() {
		return mPoller.isVerbose();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#listControlledDirectories()
	 */
	public String listControlledDirectories() {
		return mPoller.listControlledDirectories();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#listInstalledPollManagers()
	 */
	public String listInstalledPollManagers() {
		return mPoller.listInstalledPollManagers();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#removeControlledDirectory(java.lang.String)
	 */
	public void removeControlledDirectory(String dir) throws MBeanException {
		mPoller.removeControlledDirectory(dir);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setAutoMove(boolean)
	 */
	public void setAutoMove(boolean v) {
		mPoller.setAutoMove(v);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setBaseTime(java.lang.String, long)
	 */
	public void setBaseTime(String directory, long time) {
		mPoller.setBaseTime(directory, time);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setControlledDirectories(java.lang.String)
	 */
	public void setControlledDirectories(String commaSeparatedList) {
		mPoller.setControlledDirectories(commaSeparatedList);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setPollingTimeBased(boolean)
	 */
	public void setPollingTimeBased(boolean v) {
		mPoller.setPollingTimeBased(v);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setPollInterval(long)
	 */
	public void setPollInterval(long pollInterval) {
		mPoller.setPollInterval(pollInterval);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setSendSingleFileEvent(boolean)
	 */
	public void setSendSingleFileEvent(boolean v) {
		mPoller.setSendSingleFileEvent(v);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setStartBySleeping(boolean)
	 */
	public void setStartBySleeping(boolean v) {
		mPoller.setStartBySleeping(v);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setVerbose(boolean)
	 */
	public void setVerbose(boolean v) {
		mPoller.setVerbose(v);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#shutDown()
	 */
	public void shutDown() throws MBeanException {
		mPoller.shutDown();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#startUp()
	 */
	public void startUp() throws MBeanException {
		mPoller.startUp();		
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getPollManagerFactory()
	 */
	public String getPollManagerFactoryClass() {
		return mPoller.getPollManagerFactoryClass();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setPollManagerFactoryClass(java.lang.String)
	 */
	public void setPollManagerFactoryClass(String factoryClass) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
		mPoller.setPollManagerFactoryClass(factoryClass);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getPollManagerFactory()
	 */
	public String getPollManagerFactory() {
		return mPoller.getPollManagerFactory();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getJMXTimerObjectName()
	 */
	public String getJMXTimerObjectName() {
		return mPoller.getJMXTimerObjectName();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#isUsingJMXTimer()
	 */
	public boolean isUsingJMXTimer() {
		return mPoller.isUsingJMXTimer();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setJMXTimerObjectName(java.lang.String)
	 */
	public void setJMXTimerObjectName(String jMXTimerObjectName) {
		mPoller.setJMXTimerObjectName(jMXTimerObjectName);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setUsingJMXTimer(boolean)
	 */
	public void setUsingJMXTimer(boolean v) {
		mPoller.setUsingJMXTimer(v);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getFilter()
	 */
	public FilenameFilter getFilter() {
		return mPoller.getFilter();
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setFilter(java.io.FilenameFilter)
	 */
	public void setFilter(FilenameFilter filter) {
		mPoller.setFilter(filter);
	}

	/**
	 * @see javax.management.MBeanRegistration#postDeregister()
	 */
	public void postDeregister() {
		mPoller.postDeregister();
	}

	/**
	 * @see javax.management.MBeanRegistration#postRegister(Boolean)
	 */
	public void postRegister(Boolean arg0) {
		mPoller.postRegister(arg0);
	}

	/**
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	public void preDeregister() throws Exception {
		mPoller.preDeregister();
	}

	/**
	 * @see javax.management.MBeanRegistration#preRegister(MBeanServer, ObjectName)
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName name)
		throws Exception {
	    
		return mPoller.preRegister(server, name);
	}

	/**
	 * @see javax.management.NotificationListener#handleNotification(Notification, Object)
	 */
	public void handleNotification(Notification arg0, Object arg1) {
		mPoller.handleNotification(arg0, arg1);
	}
	
	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setFilenameFilterFactory(java.lang.String)
	 */
	public void setFilenameFilterFactoryClass(String filenameFilterFactoryClsName)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		mPoller.setFilenameFilterFactoryClass(filenameFilterFactoryClsName);
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getFilenameFilterFactory()
	 */
	public String getFilenameFilterFactory() {
		return mPoller.getFilenameFilterFactory();
	}
	
	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getFilenameFilterFactoryClass()
	 */
	public String getFilenameFilterFactoryClass() {
		return mPoller.getFilenameFilterFactoryClass();
	}
	
	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getAcceptedFilenamePattern()
	 */
	public String getAcceptedFilenamePattern() {
		return mPoller.getAcceptedFilenamePattern();
	}
	
	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setAcceptedFilenamePattern(java.lang.String)
	 */
	public void setAcceptedFilenamePattern(String filenamePattern) {
		mPoller.setAcceptedFilenamePattern(filenamePattern);
	}
	
	/* (non-Javadoc)
     * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setAutomoveDirectory(java.lang.String, java.lang.String)
     */
    public void setAutoMoveDirectoryPath(String directory, String automoveDirectory) {
        mPoller.setAutoMoveDirectoryPath(directory, automoveDirectory);

    }
    
    /* (non-Javadoc)
     * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getFilesSortComparatorClass()
     */
    public String getFilesSortComparatorClass() {
        return mPoller.getFilesSortComparatorClass();
    }
    
    /* (non-Javadoc)
     * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setFilesSortComparatorClass(java.lang.String)
     */
    public void setFilesSortComparatorClass(String fileComparatorClassName)
            throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        mPoller.setFilesSortComparatorClass(fileComparatorClassName);
    }
    
    /* (non-Javadoc)
     * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getEventsOrdering()
     */
    public String getEventsOrdering() {
        return mPoller.getEventsOrdering();
    }
    
    /* (non-Javadoc)
     * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setEventsOrdering(java.lang.String)
     */
    public void setEventsOrdering(String expr) {
       mPoller.setEventsOrdering(expr);
    }
	
    public String getJMXSequenceNumberGeneratorClass() {
        return mPoller.getJMXSequenceNumberGeneratorClass();
    }
    
    public void setJMXSequenceNumberGeneratorClass(String sequenceNumberGeneratorClass) throws MBeanException {
        mPoller.setJMXSequenceNumberGeneratorClass(sequenceNumberGeneratorClass);
    }

    public void addNotificationListener(ObjectName objectName) throws InstanceNotFoundException {
        mPoller.addNotificationListener(objectName);
    }
    
    public void addNotificationListener(String mbeanServerName, ObjectName objectName) throws InstanceNotFoundException {
        mPoller.addNotificationListener(mbeanServerName, objectName);
    }
    
    public void removeNotificationListener(ObjectName objectName) throws InstanceNotFoundException, ListenerNotFoundException {
        mPoller.removeNotificationListener(objectName);
    }
    
    public void removeNotificationListener(String mbeanServerName, ObjectName objectName) throws InstanceNotFoundException, ListenerNotFoundException {
        mPoller.removeNotificationListener(mbeanServerName, objectName);
        
    }
    
    public boolean isBypassLockedFiles() {
        return mPoller.isBypassLockedFiles();
    }
    
    public void setBypassLockedFiles(boolean supportSlowTransfer) {
        mPoller.setBypassLockedFiles(supportSlowTransfer);
    }
    
    public boolean isDebugExceptions() {
        return mPoller.isDebugExceptions();
    }
    
    public void setDebugExceptions(boolean debugExceptions) {
        mPoller.setDebugExceptions(debugExceptions);
    }
    
    public void setTimeBased(boolean v) {
        mPoller.setTimeBased(v);
    }
    
}
