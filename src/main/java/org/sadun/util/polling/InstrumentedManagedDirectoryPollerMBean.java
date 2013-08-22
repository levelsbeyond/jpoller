/*
 * Created on Sep 13, 2004
 */
package org.sadun.util.polling;

/**
 * An extension of ManagedDirectoryPollerMBean that adds a read-only property listing the other property values. Useful
 * only for debugging purposes.
 *
 * @author Cristiano Sadun
 */
public interface InstrumentedManagedDirectoryPollerMBean
	extends
	ManagedDirectoryPollerMBean {

	/**
	 * Read the (other) property values in the MBean.
	 *
	 * @return a String containing the (other) property values in the MBean.
	 */
	public String getPropertyValues();

}
