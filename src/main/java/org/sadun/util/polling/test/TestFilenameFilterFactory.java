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

/*
 * Created on Sep 9, 2004
 */
package org.sadun.util.polling.test;

import java.io.File;
import java.io.FilenameFilter;

import org.sadun.util.polling.FilenameFilterFactory;

/**
 * An example of FilenameFilterFactory producing a sample FilenameFilter for any Mbean.
 *
 * @author Cristiano Sadun
 */
public class TestFilenameFilterFactory implements FilenameFilterFactory {

	public FilenameFilter createFilenameFilter(String mbeanName) {
		return new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".txt");
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.polling.FilenameFilterFactory#getDescription()
	 */
	public String getDescription() {
		return "A sample filename filter which produces a filter which accepts only .txt files";
	}

	public String toString() {
		return getDescription();
	}

}