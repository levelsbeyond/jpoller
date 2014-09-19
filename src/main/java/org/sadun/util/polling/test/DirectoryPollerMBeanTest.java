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
