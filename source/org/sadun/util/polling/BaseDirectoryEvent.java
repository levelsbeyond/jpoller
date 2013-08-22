package org.sadun.util.polling;

import java.io.File;

abstract class BaseDirectoryEvent extends BasePollerEvent {

    protected File directory;

    public BaseDirectoryEvent(DirectoryPoller poller, File directory) {
        super(poller);
        this.directory=directory;
    }

    public File getDirectory() { return directory; }
}
    