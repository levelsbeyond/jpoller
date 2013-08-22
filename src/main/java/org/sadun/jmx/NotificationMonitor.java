package org.sadun.jmx;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * A minimal notification monitor keeping core log of JMX notifications.
 *
 * @author Cristiano Sadun
 */
public class NotificationMonitor implements MBeanRegistration, NotificationMonitorMBean,
	NotificationListener {

	private List notifications = new LinkedList();
	private int logSize = 100;
	private ObjectName mbeanName;

	public NotificationMonitor() {
		System.out.println("Notifications monitor v1.0 instance created");
	}

	public synchronized void handleNotification(Notification notification,
	                                            Object handback) {
		System.out.println("Notification received: " + notification + " (total of " + notifications.size() + " registered so far)");
		if (notifications.size() > logSize)
			notifications.remove(notifications.size());
		notifications.add(0, notification);
	}

	public int getMaxLogSize() {
		return logSize;
	}

	public void setMaxLogSize(int logSize) {
		this.logSize = logSize;
	}

	public String listNotifications() {
		return head(-1);
	}

	public String tail(int n) {
		return list(n, false);
	}

	public int logSize() {
		return notifications.size();
	}

	public synchronized void cleanUp() {
		notifications.removeAll(notifications);
	}

    /*
    public void listenTo(String mbeanName) throws MBeanException {
        ArrayList a = MBeanServerFactory.findMBeanServer(null);
        MBeanServer mbs = (MBeanServer)a.get(0);
        try {
            mbs.addNotificationListener(new ObjectName(mbeanName), this, null, null );
        } catch (InstanceNotFoundException e) {
            throw new MBeanException(e);
        } catch (MalformedObjectNameException e) {
            throw new MBeanException(e);
        }
    }
    
    public void listenToDirectoryPoller() throws MBeanException {
        listenTo("user:service=DirectoryPoller");
    }*/

	private synchronized String list(int n, boolean forward) {
		int l = notifications.size();
		if (n == -1) {
			n = l;
		}

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (int i = 0; i < n; i++) {
			pw.println(notifications.get(forward ? i : l - i));
		}
		return sw.toString();
	}

	public String head(int n) {
		return list(n, true);
	}

	public void postDeregister() {
		// Do nothing

	}

	public void postRegister(Boolean arg0) {

	}

	public void preDeregister() throws Exception {
		// Do nothing

	}

	public ObjectName preRegister(MBeanServer arg0, ObjectName name) throws Exception {
		this.mbeanName = name;
		return name;
	}

}
