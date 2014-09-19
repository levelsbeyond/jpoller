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

package org.sadun.util.polling;

/**
 * A {@link org.sadun.util.polling.SequenceNumberGenerator} which keeps the sequence in core, on a per-jvm basis.
 * <p/>
 * This is used by default by {@link org.sadun.util.polling.ManagedDirectoryPoller}.
 *
 * @author Cristiano Sadun
 */
public class CoreSequenceNumberGenerator implements SequenceNumberGenerator {

	private volatile long seqNo = 0L;

	public synchronized long getNextSequenceNumber() {
		return seqNo++;
	}

}
