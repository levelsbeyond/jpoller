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
 * Created on Sep 10, 2004
 */
package org.sadun.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * A regular-expression based filename filter, which accepts files whose name matches regular expression provided at
 * construction.
 *
 * @author Cristiano Sadun
 * @version 1.0
 */
public class RegexpFilenameFilter implements FilenameFilter {

	private Pattern pattern;
	private String regexp;

	/**
	 * Create a filename filter based on the given regular expression string.
	 *
	 * @param regexp the regular expression to use when accepting file names.
	 */
	public RegexpFilenameFilter(String regexp) {
		this.regexp = regexp;
		pattern = Pattern.compile(regexp);
	}

	/**
	 * Accept the files whose name matches regular expression provided at construction.
	 *
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File dir, String name) {
		return pattern.matcher(name).matches();
	}

	/**
	 * Return the regular expression string used by this filter.
	 *
	 * @return the regular expression string used by this filter.
	 */
	public String getPatternString() {
		return regexp;
	}

	public String toString() {
		return "filter matching the regular expression " + regexp;
	}

}
