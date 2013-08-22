package org.sadun.util.polling;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Indicates that the poller has found a file. Note that an {@link FileSetFoundEvent
 * FileSetFoundEvent} containing the same file is always notified before this.
 *
 * <p>This event is signalled only if the {@link DirectoryPoller DirectoryPoller}
 * returns <b>true</b> when invoking {@link DirectoryPoller#isSendSingleFileEvent()
 * isSendSingleFileEvent()}.
 *
 * @version 1.0
 * @author C. Sadun
 */
public class FileFoundEvent extends BaseDirectoryEvent {

    private File file;
    private static DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    FileFoundEvent(DirectoryPoller poller, File file) {
        super(poller, file.getParentFile());
        this.file=file;
    }

    public File getFile() { return file; }
    
    public String toString() {
        return "File found in: " + getDirectory().getAbsolutePath() + ": "
                + file.getName() + ", size: "+(int)(file.length()/1024)+"K ,mod. "
                + df.format(new Date(file.lastModified()));
    }
}
    