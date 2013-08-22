package org.sadun.util.polling.test;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.sadun.util.polling.InstrumentedManagedDirectoryPoller;
import org.sadun.util.polling.ManagedDirectoryPollerMBean;

/**
 * A test for the Directory Pollet MLet
 *
 * @author cris
 */
public class DirectoryPollerMBeanTest {
	public static void main(String[] args) throws Exception {
		MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer();
		//URL mBeanURL = new URL("file:./jpollermlet.xml");
		//MLet mLet = new MLet();
		//mLet.getMBeansFromURL(mBeanURL);
		ManagedDirectoryPollerMBean poller = new InstrumentedManagedDirectoryPoller();
		poller.setControlledDirectories("/temp");
		ObjectName tempPollerName = new ObjectName("cris: type=DirectoryPoller,name=tempPoller");
		mBeanServer.registerMBean(poller, tempPollerName);
		MBeanInfo info = mBeanServer.getMBeanInfo(tempPollerName);
		System.out.println(info.getDescription());
	}
}
