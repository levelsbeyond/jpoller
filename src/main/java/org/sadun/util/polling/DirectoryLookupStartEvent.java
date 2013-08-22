package org.sadun.util.polling;

import java.io.File;

/**
 * Indicates that the poller is starting to look for files in a controlled directory.
 *
 * @author C. Sadun
 * @version 1.0
 */
public class DirectoryLookupStartEvent extends BaseDirectoryEvent {

	public DirectoryLookupStartEvent(DirectoryPoller poller, File dir) {
		super(poller, dir);
	}
}
    