package org.sadun.jmx;


public interface NotificationMonitorMBean {

	public String listNotifications();

	public String tail(int n);

	public String head(int n);

	public int getMaxLogSize();

	public void setMaxLogSize(int logSize);

	public int logSize();

	public void cleanUp();

}
