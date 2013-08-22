package org.sadun.util.polling;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.sadun.util.RegexpFilenameFilter;

/**
 * A JMX managed subclass of directory poller, exposing the
 * {@link ManagedDirectoryPollerMBean ManagedDirectoryPollerMBean}
 * interface.
 * <p>
 * From v1.5, JMX notifications are also emitted together with the 
 * existing {@link org.sadun.util.polling.BasePollerEvent events}. 
 * <p>
 * One or more listeners may be registered to the Poller, allowing MBeans in 
 * @author cris
 */
public class ManagedDirectoryPoller
    extends DirectoryPoller
	implements ManagedDirectoryPollerMBean, MBeanRegistration, NotificationListener, NotificationBroadcaster  {
	
	/**
	 * The thread in which the {@link DirectoryPoller DirectoryPoller}
	 * is actually run.	 */	
	private Thread managedThread;
	
	private boolean usingJMXTimer;
	
	private String JMXTimerObjectName;
	
	/**
	 * The MBean name, as notified in preRegister();	 */
	private ObjectName pollerMBeanName;
	
    /**
	 * If JMX timing is used, there's no real thread running - the
	 * cycles are triggered by receiving timer events.
	 * <p>
	 * In that case, "running" just means "listening to timing events"	 */
	private boolean isListeningToTimingEvents;
	
	/**
	 * The poll manager factory class name, if any	 */
	private String pollManagerFactoryClsName;

	
	/**
	 * The poll manager factory instance, if any
	 */
	private PollManagerFactory pollManagerFactory;

	
	/**
	 * The filename filter factory class name, if any
	 */
	private String filenameFilterFactoryClsName;
	
	
	/**
	 * The filename filter factory instance, if any
	 */
	private FilenameFilterFactory filenameFilterFactory;

	private String currentPattern;
	
    
    /**
     * The sequence number generator for JMX events.
     */
    private SequenceNumberGenerator sqg;
    
    /**
     * The sequence number generator for JMX events.
     */
    private String JMXsequenceNumberGeneratorClass = CoreSequenceNumberGenerator.class.getName();
    
    private JMXNotificationsPollManager jmxNotificationsPollManager;
    
	/**
	 * Constructor for ManagedDirectoryPoller.
	 * @param dirs
	 * @param filter
	 */
	public ManagedDirectoryPoller(File[] dirs, FilenameFilter filter) {
		super(dirs, filter);
	}

	/**
	 * Constructor for ManagedDirectoryPoller.
	 * @param dirs
	 */
	public ManagedDirectoryPoller(File[] dirs) {
		super(dirs);
	}

	/**
	 * Constructor for ManagedDirectoryPoller.
	 * @param directory
	 * @param filter
	 */
	public ManagedDirectoryPoller(File directory, FilenameFilter filter) {
		super(directory, filter);
	}

	/**
	 * Constructor for ManagedDirectoryPoller.
	 * @param directory
	 */
	public ManagedDirectoryPoller(File directory) {
		super(directory);
	}

	/**
	 * Constructor for ManagedDirectoryPoller.
	 * @param filter
	 */
	public ManagedDirectoryPoller(FilenameFilter filter) {
		super(filter);
	}

	/**
	 * Constructor for ManagedDirectoryPoller.
	 */
	public ManagedDirectoryPoller() {
		super();
	}

	/**
	 * Constructor for ManagedDirectoryPoller.
	 * @param dirs
	 * @param filter
	 * @param timeBased
	 */
	public ManagedDirectoryPoller(
		File[] dirs,
		FilenameFilter filter,
		boolean timeBased) {
		super(dirs, filter, timeBased);
	}

	/**
	 * Constructor for ManagedDirectoryPoller.
	 * @param directory
	 * @param filter
	 * @param timeBased
	 */
	public ManagedDirectoryPoller(
		File directory,
		FilenameFilter filter,
		boolean timeBased) {
		super(directory, filter, timeBased);
	}

	/**
	 * Constructor for ManagedDirectoryPoller.
	 * @param filter
	 * @param timeBased
	 */
	public ManagedDirectoryPoller(FilenameFilter filter, boolean timeBased) {
		super(filter, timeBased);
	}

	/**
	 * @see javax.management.MBeanRegistration#postDeregister()
	 */
	public void postDeregister() {
	}

	/**
	 * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
	 */
	public void postRegister(Boolean registrationDone) {
        try {
            sqg =(SequenceNumberGenerator)Class.forName(JMXsequenceNumberGeneratorClass).newInstance();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally  {
            // Ensure we have a valid sqg
            if (sqg==null) {
                sqg=new CoreSequenceNumberGenerator();
                JMXsequenceNumberGeneratorClass=CoreSequenceNumberGenerator.class.getName();
            }
        }
        
        // Set up the pollmanager in charge of JMX event notifications
        //System.out.println("Adding JMX notifications pollmanager");
        jmxNotificationsPollManager = new JMXNotificationsPollManager(pollerMBeanName, sqg);
        addPollManager(jmxNotificationsPollManager);
	}

	/**
	 * @see javax.management.MBeanRegistration#preDeregister()
	 */
	public void preDeregister() throws Exception {
		if (isAlive()) shutdown();
	}

	/**
	 * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
	 */
	public ObjectName preRegister(MBeanServer server, ObjectName name)
		throws Exception {
		pollerMBeanName=name;
		return name;
	}

	/**
	 * Starts a thread for the poller
	 * @see java.lang.Runnable#run()
	 */ 
	public void startUp() {
		if (getDirectories().length==0) 
			throw new IllegalStateException("No directories to poll");
		if (managedThread != null) 
			throw new IllegalStateException("the directory poller is already running");
			
		if (usingJMXTimer) {
			isListeningToTimingEvents=true;
		} else {	
			managedThread = new Thread(this, "Managed directory poller");
			managedThread.setDaemon(true);
			managedThread.start();
		}
	}	
	
	public void shutDown() {
		if (usingJMXTimer) {
			isListeningToTimingEvents=false;
		} else {	
			if (managedThread==null) 
				throw new IllegalStateException("Directory poller is not started");
			super.shutdown();
			managedThread=null;
		}
	}
	
	public String getAutoMoveDirectoryPath(String directory) {
		if(directory==null)
			throw new IllegalArgumentException("No directory specified");
		if("".equals(directory.trim()))
			throw new IllegalArgumentException("No directory specified");	
		try {
			return this.getAutoMoveDirectory(new File(directory)).getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
    /**
	 * Remove one directory from the controlled set. It can be called
     * only if the poller thread hasn't started yet.
	 * @param dir the directory to remove 
	 * @exception IllegalStateException if the poller has already started.
     * @exception IllegalArgumentException if the directory is not among the controlled ones
	 */    
    public void removeControlledDirectory(String dir) {
	    	super.removeDirectory(new File(dir));
    }
     
   	/**
	 * Add one directory to the controlled set. It can be called
     * only if the poller thread hasn't started yet.
	 * @param dir the directory to add
	 * @exception IllegalStateException if the poller has already started.
     * @exception IllegalArgumentException if String does not contain a directory path
	 */    
    public void addControlledDirectory(String dir) {
    	super.addDirectory(new File(dir));
    }
	
	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#countControlledDirectories()
	 */
	public int countControlledDirectories() {
		return getDirectories().length;
	}
	
	public boolean isRunning() {
		if (managedThread==null) return false;
		if (usingJMXTimer) return isListeningToTimingEvents;
		return managedThread.isAlive();
	}
	
	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getControlledDirectory(int)
	 */
	public String getControlledDirectory(int i) {
		if (i < 0 || i >= getDirectories().length) 
			throw new IllegalArgumentException("Index out of range");
		try {
			return getDirectories()[i].getCanonicalPath();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void setBaseTime(String directory, long time) {
		File dir = new File(directory);
		super.setBaseTime(dir, time);
	}
	
	public long getBaseTime(String directory) {
		File dir = new File(directory);
		return super.getBaseTime(dir);
	}
	
	public String listControlledDirectories() {
		StringWriter sw=new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		File [] dirs = getDirectories();
		pw.println("Total of "+dirs.length+" controlled directories");
		pw.println();
		for(int i=0;i<dirs.length;i++) {
			pw.print("["+i+"] ");
			pw.println(dirs[i]);
		}
		return sw.toString();
	}
	
	public String getControlledDirectories() {
		StringWriter sw=new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		File [] dirs = getDirectories();
		for(int i=0;i<dirs.length;i++) {
			pw.print(dirs[i]);
			if (i<dirs.length-1) pw.print(",");
		}
		return sw.toString();
	}
	
	public void setControlledDirectories(String dirList) {
		StringTokenizer st = new StringTokenizer(dirList,",");
		while(st.hasMoreTokens()) {
			addControlledDirectory(st.nextToken());
		}
	}
	
	public String listInstalledPollManagers() {
		if (pollManagersList.size()==0)
			return "No poll managers installed";
		StringWriter sw=new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for(Iterator i=pollManagersList.iterator();i.hasNext();) {
		 	PollManager pm = (PollManager)i.next();
            // Hide the JMX notification pollmanager
            if (pm instanceof JMXNotificationsPollManager) continue;
		 	pw.println(pm.toString());
		}
		return sw.toString();
	}
    
    public void addPollManager(PollManager pm) {
        // Allow only one JMXPollmanager
        if (pm instanceof JMXNotificationsPollManager)
            for(Iterator i=pollManagersList.iterator();i.hasNext();) {
                if (i.next() instanceof JMXNotificationsPollManager) {
                    throw new RuntimeException("JMXNotificationsPollManager is an internal-use only PollManager");
                }
            }
        super.addPollManager(pm);
        if (isVerbose()) 
            System.out.println("Added PollManager: "+pm);

    }
    

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getPollManagerFactory()
	 */
	public String getPollManagerFactoryClass() {
		if (pollManagerFactoryClsName==null) return "";
		return pollManagerFactoryClsName;
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setPollManagerFactoryClass(java.lang.String)
	 */
	public void setPollManagerFactoryClass(String newFactoryClsName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		if (isRunning()) throw new RuntimeException("The directory poller is already running");
		if (this.pollManagerFactoryClsName!=null) {
			if (this.pollManagerFactoryClsName.equals(newFactoryClsName)) return;
			// Clear the pollManager list
			super.pollManagersList.clear();
			// Clear both existing factory name and instance
			this.pollManagerFactory=null;
			this.pollManagerFactoryClsName=null;
		}
		
		if (newFactoryClsName==null) return;
		if ("".equals("newFactoryClsName")) {
			this.pollManagerFactory=null;
			this.pollManagerFactoryClsName=null;
			return;
		}
		
		// Attempt to install the poll managers
		this.pollManagerFactory=(PollManagerFactory)Class.forName(newFactoryClsName).newInstance();
		this.pollManagerFactoryClsName=newFactoryClsName;
		
		if (pollerMBeanName==null) throw new RuntimeException("PreRegister hasn't been called on this bean - poller bean name is null");
		
		PollManager[] set = pollManagerFactory.createPollManagers(pollerMBeanName.getCanonicalName());
		for(int i=0;i<set.length;i++)
			addPollManager(set[i]);
	}

	/**
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getPollManagerFactory()
	 */
	public String getPollManagerFactory() {
		if (pollManagerFactory==null) return "(No PollManagerFactory set)";
		return pollManagerFactory.getDescription();
	}
	
	/**
	 * Returns the usingJMXTimer.
	 * @return boolean
	 */
	public boolean isUsingJMXTimer() {
		return usingJMXTimer;
	}

	/**
	 * Sets the usingJMXTimer.
	 * @param usingJMXTimer The usingJMXTimer to set
	 */
	public void setUsingJMXTimer(boolean usingJMXTimer_NewValue) {
		if (usingJMXTimer) { // Using timer previously
			if (usingJMXTimer_NewValue) {
				// Nothing to do
			} else {
				// deregister the hook
			}
		} else { // Not using timer previously
			if (usingJMXTimer_NewValue) {
			} else {
				// Is the poller running already? 
				if (isAlive()) 
					throw new IllegalStateException("Can't set the use of JMX timer while the poller is running. Please shut it down first.");
			}
		}
		this.usingJMXTimer = usingJMXTimer_NewValue;
	}

	/**
	 * Returns the jMXTimerObjectName.
	 * @return String
	 */
	public String getJMXTimerObjectName() {
		if (JMXTimerObjectName==null) return "";
		return JMXTimerObjectName;
	}

	/**
	 * Sets the jMXTimerObjectName.
	 * @param jMXTimerObjectName The jMXTimerObjectName to set
	 */
	public void setJMXTimerObjectName(String jMXTimerObjectName) {
		if ("".equals(jMXTimerObjectName)) jMXTimerObjectName=null;
		JMXTimerObjectName = jMXTimerObjectName;
	}

	/**
	 * Handles timer notifications, if a JMXTimer object is used
	 * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
	 */
	public void handleNotification(Notification arg0, Object arg1) {
		
	}
	
	/**
	 * @param filenameFilterClsName The filenameFilterClsName to set.
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void setFilenameFilterFactoryClass(String newFilenameFilterFactoryClsName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		if (isRunning()) throw new RuntimeException("The directory poller is already running");
		if (newFilenameFilterFactoryClsName==null) return;
		
		if (newFilenameFilterFactoryClsName==null || "".equals(newFilenameFilterFactoryClsName)) {
			setFilter(new NullFilenameFilter());
			this.filenameFilterFactoryClsName=null;
			return;
		}
		
		this.filenameFilterFactory=(FilenameFilterFactory) Class.forName(newFilenameFilterFactoryClsName).newInstance();
		this.filenameFilterFactoryClsName=newFilenameFilterFactoryClsName;
	
		if (pollerMBeanName==null) throw new RuntimeException("PreRegister hasn't been called on this bean - poller bean name is null");
		setFilter(filenameFilterFactory.createFilenameFilter(pollerMBeanName.getCanonicalName()));
	}
	
	public String getFilenameFilterFactory() {
		if (filenameFilterFactory==null) return "(No FilenameFilterFactory set)";
		return filenameFilterFactory.getDescription();
	}
	
	public String getFilenameFilterFactoryClass() {
		if (filenameFilterFactoryClsName==null) return "";
		return filenameFilterFactoryClsName;
	}
	
	
	public void setAcceptedFilenamePattern(String filenamePattern) {
		if (filenameFilterFactoryClsName!=null && !"".equals(filenamePattern)) {
			try {
				setFilenameFilterFactoryClass("");
			} catch (InstantiationException e) {
				// Ignore - can't happen
				throw new RuntimeException("This shouldn't happen ("+e.getClass().getName()+":"+e.getMessage()+") - please report", e);
			} catch (IllegalAccessException e) {
				// Ignore - can't happen
				throw new RuntimeException("This shouldn't happen ("+e.getClass().getName()+":"+e.getMessage()+") - please report", e);
			} catch (ClassNotFoundException e) {
				// Ignore - can't happen
				throw new RuntimeException("This shouldn't happen ("+e.getClass().getName()+":"+e.getMessage()+") - please report", e);
			}
		}
		
		if ("".equals(filenamePattern)) setFilter(new NullFilenameFilter());
		else setFilter(new RegexpFilenameFilter(filenamePattern));
	}
	
	/* (non-Javadoc)
	 * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setFilter(java.io.FilenameFilter)
	 */
	public void setFilter(FilenameFilter filter) {
		
		if (filter instanceof RegexpFilenameFilter) 
			currentPattern=((RegexpFilenameFilter)filter).getPatternString();
		else
			currentPattern=null;
		
		super.setFilter(filter);
		
	}
		
    public String getAcceptedFilenamePattern() {
    	if (currentPattern==null) return "";
    	else return currentPattern;
    }
    
    public void setAutoMoveDirectoryPath(String directory, String automoveDirectory) {
        setAutoMoveDirectory(new File(directory), new File(automoveDirectory));
    }
    
    /* (non-Javadoc)
     * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getFileComparatorClass()
     */
    public String getFilesSortComparatorClass() {
        if (getFilesSortComparator()!=null) return getFilesSortComparator().getClass().getName();
        else return "";
    }
    
    /* (non-Javadoc)
     * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setFileComparatorClass(java.lang.String)
     */
    public void setFilesSortComparatorClass(String fileComparatorClassName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if ("".equals(fileComparatorClassName)) {
            //System.out.println("Setting comparator class name to null");
            setFilesSortComparator(null);
        } else if (fileComparatorClassName.equals(GenericFileComparator.class.getName())) {
            // Ignore
            //System.out.println("Ignoring comparator class name "+fileComparatorClassName);
        } else {
            //System.out.println("Setting comparator class name "+fileComparatorClassName);
            Class cls = Class.forName(fileComparatorClassName);
            try {
                Constructor ctor = cls.getConstructor(new Class[0]);
                if (!Modifier.isPublic(ctor.getModifiers()))
                    throw new RuntimeException(
                            "The specified file comparator class "
                                    + fileComparatorClassName
                                    + " has a zero-parameters constructor, but it is not public");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                        "The specified file comparator class "
                                + fileComparatorClassName
                                + " does not have a default constructor");
            }
            setFilesSortComparator((Comparator)cls.newInstance());
        }
    }
    
    /* (non-Javadoc)
     * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#setEventsOrdering(java.lang.String)
     */
    public void setEventsOrdering(String expr) {
        if ("by comparator class".equals(expr.trim().toLowerCase())) {
            // Ignore
            return;
        }
        if ("-".equals(expr.trim()) || "none".equals(expr.trim().toLowerCase()) || "".equals(expr.trim())) {
            setFilesSortComparator(null);
            //System.out.println("Setting comparator to null");
            return;
        }
        Comparator c = new GenericFileComparator(expr);
        setFilesSortComparator(c);
        //System.out.println("Setting event ordering to "+expr+" (GenericFileComparator "+c+")");
    }
    
    /* (non-Javadoc)
     * @see org.sadun.util.polling.ManagedDirectoryPollerMBean#getEventsOrdering()
     */
    public String getEventsOrdering() {
        Comparator c = getFilesSortComparator();
        if (c==null) return "none";
        if (c instanceof GenericFileComparator)
            return ((GenericFileComparator)c).getSpecification();
        else return "by comparator class";
    }
    
    public String getJMXSequenceNumberGeneratorClass() {
        return JMXsequenceNumberGeneratorClass;
    }

    public void setJMXSequenceNumberGeneratorClass(String sequenceNumberGeneratorClass) throws MBeanException {
        try {
            sqg = (SequenceNumberGenerator)Class.forName(sequenceNumberGeneratorClass).newInstance();
            this.JMXsequenceNumberGeneratorClass = sequenceNumberGeneratorClass;
        } catch(Exception e) {
            throw new MBeanException(e);
        }
    }
    
    public MBeanNotificationInfo[] getNotificationInfo() {
        MBeanNotificationInfo[] mbv = new MBeanNotificationInfo[1];
        mbv[0]=new MBeanNotificationInfo(new String [] {
          CycleStartJMXNotification.NOTIFICATION_TYPE,
          CycleEndJMXNotification.NOTIFICATION_TYPE,
          DirectoryLookupStartJMXNotification.NOTIFICATION_TYPE,
          DirectoryLookupEndJMXNotification.NOTIFICATION_TYPE,
          FileSetFoundJMXNotification.NOTIFICATION_TYPE,
          FileMovedJMXNotification.NOTIFICATION_TYPE,
          FileFoundJMXNotification.NOTIFICATION_TYPE, 
          ExceptionMovingFileJMXNotification.NOTIFICATION_TYPE,
          ExceptionDeletingTargetFileJMXNotification.NOTIFICATION_TYPE
        }, BaseJMXNotification.class.getName(), "Poller notification events"
        );
        return mbv;
    }
    
    /**
     * Implement the NotificationBroadcaster interface, by registering the listener at the
     * internal {@link JMXNotificationsPollManager}.  
     */
    public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException {
        jmxNotificationsPollManager.addListener(listener,filter,handback);
    }
    
    /**
     * Implement the NotificationBroadcaster interface, by removing the listener from the
     * internal {@link JMXNotificationsPollManager}.  
     */
    public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException {
        jmxNotificationsPollManager.removeListener(listener);
        
    }
    
    /**
     * Register a listening MBean (found in the only existing MBean server) via its ObjectName. Only one MBean server must
     * be present.
     *  
     * @param listenerMBeanName the object name of the mbean which listens to to the poller's JMX notifications  
     */
    public void addNotificationListener(ObjectName listenerMBeanName) throws InstanceNotFoundException {
        addNotificationListener(null, listenerMBeanName);
    }
        
    /**
     * Register a listening MBean via its ObjectName.
     * 
     * @param the name of the MBean server containing the listener MBean
     * @param listenerMBeanName the object name of the mbean which listens to to the poller's JMX notifications  
     */
    public void addNotificationListener(String mbeanServerName, ObjectName listenerMBeanName) throws InstanceNotFoundException {
        MBeanServer srv = getMBeanServer(mbeanServerName);
        if (isVerbose())
            System.out.println("Adding notification listener "+listenerMBeanName+" to "+pollerMBeanName+" on server "+srv);
        srv.addNotificationListener(this.pollerMBeanName, listenerMBeanName, null, null);
        if (isVerbose())
            System.out.println("Added notification listener "+listenerMBeanName+" to "+pollerMBeanName+" on server "+srv);
    }

    /**
     * Remove a listening MBean (found in the only existing MBean server) via its ObjectName. Only one MBean server must
     * be present.
     * @param listenerMBeanName the object name of the mbean which is not to listen anymore to the poller's JMX notifications  
     */
    public void removeNotificationListener(ObjectName listenerMBeanName) throws InstanceNotFoundException, ListenerNotFoundException {
        removeNotificationListener(null, listenerMBeanName);
    }
    
    /**
     * Remove a listening MBean via its ObjectName.
     * 
     * @param the name of the MBean server containing the listener MBean
     * @param listenerMBeanName the object name of the mbean which is not to listen anymore to the poller's JMX notifications  
     */
    public void removeNotificationListener(String mbeanServerName, ObjectName listenerMBeanName) throws InstanceNotFoundException, ListenerNotFoundException {    
        MBeanServer srv = getMBeanServer(mbeanServerName);
        if (isVerbose())
            System.out.println("Removing notification listener "+listenerMBeanName+" to "+pollerMBeanName+" on server "+srv);
        srv.removeNotificationListener(this.pollerMBeanName, listenerMBeanName);
        if (isVerbose())
            System.out.println("Removed notification listener "+listenerMBeanName+" to "+pollerMBeanName+" on server "+srv);
    }
    
    /**
     * @return
     */
    private MBeanServer getMBeanServer(String mbeanServerName) {
        MBeanServer srv=null;
        ArrayList al = MBeanServerFactory.findMBeanServer(mbeanServerName);
        if (al.isEmpty()) throw new RuntimeException("Could not find any MBean server"+mbeanServerName!=null ? " named \""+mbeanServerName+"\"" : "");
        else if (al.size()!=1) throw new RuntimeException("More than one MBean server "+(mbeanServerName!=null ? "named \""+mbeanServerName+"\" " : "")+"registered?");
        else srv=(MBeanServer)al.get(0);
        return srv;
    }
	
}

