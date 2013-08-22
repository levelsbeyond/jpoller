package org.sadun.util.polling;

import java.io.File;

import org.sadun.util.MovedFile;

/**
 * Indicates that the poller has performed an automatic move of a
 * polled file. Can be signalled only if the autoMove move
 * is active (see {@link DirectoryPoller DirectoryPoller class description}).
 *
 * @version 1.0
 * @author C. Sadun
 */
public class FileMovedEvent extends BasePollerEvent {

    private File original;
    private File destination;

    FileMovedEvent(DirectoryPoller poller, File original, File destination) {
        super(poller);
        this.original=original;
        this.destination=destination;
    }
    
    /**
     * Return the original path of the file. Since the file has been moved,
     * this File object does not correspond anymore to a physical file on
     * the filesystem.
     * @return the original path of the file.
     */
    public File getOriginalPath() { return original; }

    /**
     * Return the new path of the file. Since the file has been moved,
     * this File object corresponds to the physical file on
     * the filesystem.
     * @return the new path of the file.
     */
    public File getPath() { return destination; }

    /**
     * Return a {@link MovedFile MovedFile} object encapsulating the
     * move operation.
     * @return a {@link MovedFile MovedFile} object encapsulating the
     *         move operation.
     */
    public MovedFile getMovedFile() {
        return new MovedFile(original, destination, false);
    }

}
    