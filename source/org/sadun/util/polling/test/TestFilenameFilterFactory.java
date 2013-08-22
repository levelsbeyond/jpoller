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