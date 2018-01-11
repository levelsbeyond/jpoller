
/*
 * Levels Beyond CONFIDENTIAL
 *
 * Copyright 2003 - 2018 Levels Beyond Incorporated
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
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.APPEND;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.sadun.util.PathNormalizer;
import org.testng.annotations.Test;

@Test
public class DirectoryPollerTest {

    @Test(enabled = false)
    void testRunCycleFile_basic_unix() throws IOException {
        // todo: These aren't working since toFile() is not implemented by Jimfs.  To use Jimfs, we'll have to migrate to 100% java.nio
        // which would be a big win.
        testRunCycleFile_basic(Configuration.unix());
    }

    @Test(enabled = false)
    void testRunCycleFile_basic_osX() throws IOException {
        // todo: These aren't working since toFile() is not implemented by Jimfs.  To use Jimfs, we'll have to migrate to 100% java.nio
        // which would be a big win.
        testRunCycleFile_basic(Configuration.osX());
    }

    @Test(enabled = false)
    void testRunCycleFile_basic_windows() throws IOException {
        // todo: These aren't working since toFile() is not implemented by Jimfs.  To use Jimfs, we'll have to migrate to 100% java.nio
        // which would be a big win.
        testRunCycleFile_basic(Configuration.windows());
    }

    @Test
    void testRunCycleFile_basic_native() throws IOException {
        testRunCycleFile_basic(null);
    }

    void testRunCycleFile_basic(Configuration configuration) throws IOException {
        TestHarness testHarness = configuration == null ? new TestHarness() : new TestHarness(configuration);
        testHarness.runCycle();

        ArgumentCaptor<CycleStartEvent> startArgument = ArgumentCaptor.forClass(CycleStartEvent.class);
        verify(testHarness.pollManager, times(1)).cycleStarted(startArgument.capture());
        assertThat(startArgument.getValue().getPoller()).isEqualTo(testHarness.poller);

        ArgumentCaptor<DirectoryLookupStartEvent> dirStartArgument = ArgumentCaptor.forClass(DirectoryLookupStartEvent.class);
        verify(testHarness.pollManager, times(1)).directoryLookupStarted(dirStartArgument.capture());
        assertThat(dirStartArgument.getValue().getPoller()).isEqualTo(testHarness.poller);
        assertThat(dirStartArgument.getValue().getDirectory()).isEqualTo(testHarness.testdir.toFile());

        verify(testHarness.pollManager, times(0)).exceptionDeletingTargetFile(any(File.class));
        verify(testHarness.pollManager, times(0)).exceptionMovingFile(any(File.class), any(File.class));
        verify(testHarness.pollManager, times(0)).fileMoved(any(FileMovedEvent.class));
        verify(testHarness.pollManager, times(0)).fileSetFound(any(FileSetFoundEvent.class));
        verify(testHarness.pollManager, times(0)).fileFound(any(FileFoundEvent.class));

        ArgumentCaptor<DirectoryLookupEndEvent> dirEndArgument = ArgumentCaptor.forClass(DirectoryLookupEndEvent.class);
        verify(testHarness.pollManager, times(1)).directoryLookupEnded(dirEndArgument.capture());
        assertThat(dirEndArgument.getValue().getPoller()).isEqualTo(testHarness.poller);
        assertThat(dirEndArgument.getValue().getDirectory()).isEqualTo(testHarness.testdir.toFile());

        ArgumentCaptor<CycleEndEvent> endArgument = ArgumentCaptor.forClass(CycleEndEvent.class);
        verify(testHarness.pollManager, times(1)).cycleEnded(endArgument.capture());
        assertThat(endArgument.getValue().getPoller()).isEqualTo(testHarness.poller);

        // Call a second time and verify it doesn't pick up any files or directories.
        testHarness.runCycle();
        verify(testHarness.pollManager, times(2)).cycleStarted(any(CycleStartEvent.class));
        verify(testHarness.pollManager, times(2)).directoryLookupStarted(any(DirectoryLookupStartEvent.class));
        verify(testHarness.pollManager, times(0)).exceptionDeletingTargetFile(any(File.class));
        verify(testHarness.pollManager, times(0)).exceptionMovingFile(any(File.class), any(File.class));
        verify(testHarness.pollManager, times(0)).fileMoved(any(FileMovedEvent.class));
        verify(testHarness.pollManager, times(0)).fileSetFound(any(FileSetFoundEvent.class));
        verify(testHarness.pollManager, times(0)).fileFound(any(FileFoundEvent.class));
        verify(testHarness.pollManager, times(2)).directoryLookupEnded(any(DirectoryLookupEndEvent.class));
        verify(testHarness.pollManager, times(2)).cycleEnded(any(CycleEndEvent.class));
    }

    @Test
    void testRunCycleFile_file() throws IOException {
        TestHarness testHarness = new TestHarness();
        OutputStreamWriter testStream = new OutputStreamWriter(
            new BufferedOutputStream(Files.newOutputStream(testHarness.testdir.resolve("testfile.txt"), CREATE, APPEND)));
        testStream.write("this is a test\n");
        testStream.flush();

        testHarness.runCycle();
        verify(testHarness.pollManager, times(1)).cycleStarted(any(CycleStartEvent.class));
        verify(testHarness.pollManager, times(1)).directoryLookupStarted(any(DirectoryLookupStartEvent.class));
        verify(testHarness.pollManager, times(0)).exceptionDeletingTargetFile(any(File.class));
        verify(testHarness.pollManager, times(0)).exceptionMovingFile(any(File.class), any(File.class));
        verify(testHarness.pollManager, times(0)).fileMoved(any(FileMovedEvent.class));
        verify(testHarness.pollManager, times(0)).fileSetFound(any(FileSetFoundEvent.class));
        verify(testHarness.pollManager, times(0)).fileFound(any(FileFoundEvent.class));
        verify(testHarness.pollManager, times(1)).directoryLookupEnded(any(DirectoryLookupEndEvent.class));
        verify(testHarness.pollManager, times(1)).cycleEnded(any(CycleEndEvent.class));

        testStream.write("this is another test\n");
        testStream.close();

        testHarness.runCycle();
        verify(testHarness.pollManager, times(2)).cycleStarted(any(CycleStartEvent.class));
        verify(testHarness.pollManager, times(2)).directoryLookupStarted(any(DirectoryLookupStartEvent.class));
        verify(testHarness.pollManager, times(0)).exceptionDeletingTargetFile(any(File.class));
        verify(testHarness.pollManager, times(0)).exceptionMovingFile(any(File.class), any(File.class));
        verify(testHarness.pollManager, times(0)).fileMoved(any(FileMovedEvent.class));
        verify(testHarness.pollManager, times(0)).fileSetFound(any(FileSetFoundEvent.class));
        verify(testHarness.pollManager, times(0)).fileFound(any(FileFoundEvent.class));
        verify(testHarness.pollManager, times(2)).directoryLookupEnded(any(DirectoryLookupEndEvent.class));
        verify(testHarness.pollManager, times(2)).cycleEnded(any(CycleEndEvent.class));

        testHarness.runCycle();
        verify(testHarness.pollManager, times(3)).cycleStarted(any(CycleStartEvent.class));
        verify(testHarness.pollManager, times(3)).directoryLookupStarted(any(DirectoryLookupStartEvent.class));
        verify(testHarness.pollManager, times(0)).exceptionDeletingTargetFile(any(File.class));
        verify(testHarness.pollManager, times(0)).exceptionMovingFile(any(File.class), any(File.class));

        ArgumentCaptor<FileMovedEvent> fileMovedCaptor = ArgumentCaptor.forClass(FileMovedEvent.class);
        verify(testHarness.pollManager, times(1)).fileMoved(fileMovedCaptor.capture());
        assertThat(fileMovedCaptor.getValue().getPath().getName()).isEqualTo("testfile.txt");

        ArgumentCaptor<FileSetFoundEvent> fileSetFoundCaptor = ArgumentCaptor.forClass(FileSetFoundEvent.class);
        verify(testHarness.pollManager, times(1)).fileSetFound(fileSetFoundCaptor.capture());
        assertThat(fileSetFoundCaptor.getValue().getFiles()[0].getName()).isEqualTo("testfile.txt");

        ArgumentCaptor<FileFoundEvent> fileFoundEventCaptor = ArgumentCaptor.forClass(FileFoundEvent.class);
        verify(testHarness.pollManager, times(1)).fileFound(fileFoundEventCaptor.capture());
        assertThat(fileFoundEventCaptor.getValue().getFile().getName()).isEqualTo("testfile.txt");

        verify(testHarness.pollManager, times(3)).directoryLookupEnded(any(DirectoryLookupEndEvent.class));
        verify(testHarness.pollManager, times(3)).cycleEnded(any(CycleEndEvent.class));
    }

    @Test
    void testRunCycleFile_directory() throws IOException {
        TestHarness testHarness = new TestHarness();
        testHarness.poller.setFilter(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name.equalsIgnoreCase("received")) {
                    return false;
                }
                File file = new File(dir, name);
                return file.isDirectory() || file.isFile();
            }
        });

        // Create nested directories to make sure the entire directory tree is being monitored.
        Path testDir1 = Files.createDirectory(testHarness.testdir.resolve("testdir1"));
        Path testDir2 = Files.createDirectory(testDir1.resolve("testdir2"));

        OutputStreamWriter testStream1 = new OutputStreamWriter(
            new BufferedOutputStream(Files.newOutputStream(testDir1.resolve("testfile1.txt"), CREATE, APPEND)));
        testStream1.write("this is a test\n");
        testStream1.flush();

        testHarness.runCycle();

        verify(testHarness.pollManager, times(1)).cycleStarted(any(CycleStartEvent.class));
        verify(testHarness.pollManager, times(1)).directoryLookupStarted(any(DirectoryLookupStartEvent.class));
        verify(testHarness.pollManager, times(0)).exceptionDeletingTargetFile(any(File.class));
        verify(testHarness.pollManager, times(0)).exceptionMovingFile(any(File.class), any(File.class));
        verify(testHarness.pollManager, times(0)).fileMoved(any(FileMovedEvent.class));
        verify(testHarness.pollManager, times(0)).fileSetFound(any(FileSetFoundEvent.class));
        verify(testHarness.pollManager, times(0)).fileFound(any(FileFoundEvent.class));
        verify(testHarness.pollManager, times(1)).directoryLookupEnded(any(DirectoryLookupEndEvent.class));
        verify(testHarness.pollManager, times(1)).cycleEnded(any(CycleEndEvent.class));

        testStream1.write("this is another test\n");
        testStream1.close();

        testHarness.runCycle();
        verify(testHarness.pollManager, times(2)).cycleStarted(any(CycleStartEvent.class));
        verify(testHarness.pollManager, times(2)).directoryLookupStarted(any(DirectoryLookupStartEvent.class));
        verify(testHarness.pollManager, times(0)).exceptionDeletingTargetFile(any(File.class));
        verify(testHarness.pollManager, times(0)).exceptionMovingFile(any(File.class), any(File.class));
        verify(testHarness.pollManager, times(0)).fileMoved(any(FileMovedEvent.class));
        verify(testHarness.pollManager, times(0)).fileSetFound(any(FileSetFoundEvent.class));
        verify(testHarness.pollManager, times(0)).fileFound(any(FileFoundEvent.class));
        verify(testHarness.pollManager, times(2)).directoryLookupEnded(any(DirectoryLookupEndEvent.class));
        verify(testHarness.pollManager, times(2)).cycleEnded(any(CycleEndEvent.class));

        OutputStreamWriter testStream2 = new OutputStreamWriter(
            new BufferedOutputStream(Files.newOutputStream(testDir2.resolve("testfile2.txt"), CREATE, APPEND)));
        testStream2.write("this is a test\n");
        testStream2.flush();

        testHarness.runCycle();
        verify(testHarness.pollManager, times(3)).cycleStarted(any(CycleStartEvent.class));
        verify(testHarness.pollManager, times(3)).directoryLookupStarted(any(DirectoryLookupStartEvent.class));
        verify(testHarness.pollManager, times(0)).exceptionDeletingTargetFile(any(File.class));
        verify(testHarness.pollManager, times(0)).exceptionMovingFile(any(File.class), any(File.class));
        verify(testHarness.pollManager, times(0)).fileMoved(any(FileMovedEvent.class));
        verify(testHarness.pollManager, times(0)).fileSetFound(any(FileSetFoundEvent.class));
        verify(testHarness.pollManager, times(0)).fileFound(any(FileFoundEvent.class));
        verify(testHarness.pollManager, times(3)).directoryLookupEnded(any(DirectoryLookupEndEvent.class));
        verify(testHarness.pollManager, times(3)).cycleEnded(any(CycleEndEvent.class));

        testStream2.write("this is another test\n");
        testStream2.close();

        testHarness.runCycle();
        verify(testHarness.pollManager, times(4)).cycleStarted(any(CycleStartEvent.class));
        verify(testHarness.pollManager, times(4)).directoryLookupStarted(any(DirectoryLookupStartEvent.class));
        verify(testHarness.pollManager, times(0)).exceptionDeletingTargetFile(any(File.class));
        verify(testHarness.pollManager, times(0)).exceptionMovingFile(any(File.class), any(File.class));
        verify(testHarness.pollManager, times(0)).fileMoved(any(FileMovedEvent.class));
        verify(testHarness.pollManager, times(0)).fileSetFound(any(FileSetFoundEvent.class));
        verify(testHarness.pollManager, times(0)).fileFound(any(FileFoundEvent.class));
        verify(testHarness.pollManager, times(4)).directoryLookupEnded(any(DirectoryLookupEndEvent.class));
        verify(testHarness.pollManager, times(4)).cycleEnded(any(CycleEndEvent.class));

        testHarness.runCycle();
        verify(testHarness.pollManager, times(5)).cycleStarted(any(CycleStartEvent.class));
        verify(testHarness.pollManager, times(5)).directoryLookupStarted(any(DirectoryLookupStartEvent.class));
        verify(testHarness.pollManager, times(0)).exceptionDeletingTargetFile(any(File.class));
        verify(testHarness.pollManager, times(0)).exceptionMovingFile(any(File.class), any(File.class));

        ArgumentCaptor<FileMovedEvent> fileMovedCaptor = ArgumentCaptor.forClass(FileMovedEvent.class);
        verify(testHarness.pollManager, times(1)).fileMoved(fileMovedCaptor.capture());
        assertThat(fileMovedCaptor.getValue().getPath().getName()).isEqualTo("testdir1");

        ArgumentCaptor<FileSetFoundEvent> fileSetFoundCaptor = ArgumentCaptor.forClass(FileSetFoundEvent.class);
        verify(testHarness.pollManager, times(1)).fileSetFound(fileSetFoundCaptor.capture());
        assertThat(fileSetFoundCaptor.getValue().getFiles()[0].getName()).isEqualTo("testdir1");

        ArgumentCaptor<FileFoundEvent> fileFoundEventCaptor = ArgumentCaptor.forClass(FileFoundEvent.class);
        verify(testHarness.pollManager, times(1)).fileFound(fileFoundEventCaptor.capture());
        assertThat(fileFoundEventCaptor.getValue().getFile().getName()).isEqualTo("testdir1");

        verify(testHarness.pollManager, times(5)).directoryLookupEnded(any(DirectoryLookupEndEvent.class));
        verify(testHarness.pollManager, times(5)).cycleEnded(any(CycleEndEvent.class));
    }


    class TestHarness {
        final TemporaryFolder temporaryFolder;
        final FileSystem fs;
        final Path testdir;
        final DirectoryPoller poller;
        final TestPollManager pollManager;
        final AtomicBoolean locked = new AtomicBoolean(false);

        class TestPollManager implements PollManager {

			@Override
			public void cycleStarted(CycleStartEvent evt) {

			}

			@Override
			public void directoryLookupStarted(DirectoryLookupStartEvent evt) {

			}

			@Override
			public void directoryLookupEnded(DirectoryLookupEndEvent evt) {

			}

			@Override
			public void fileSetFound(FileSetFoundEvent evt) {

			}

			@Override
			public void fileMoved(FileMovedEvent evt) {

			}

			@Override
			public void fileFound(FileFoundEvent evt) {

			}

			@Override
			public void exceptionDeletingTargetFile(File target) {

			}

			@Override
			public void exceptionMovingFile(File file, File dest) {

			}

			@Override
			public void cycleEnded(CycleEndEvent evt)
			{
				unlock();
			}
		}

		void unlock() {
			synchronized (locked) {
				locked.set(false);
				locked.notifyAll();
			}
		}

		void lock() {
 	       	synchronized (locked) {
				locked.set(true);
			}
		}

		void waitForLock() {
        	try {
				synchronized (locked) {
					if (locked.get()) {
						locked.wait(3L);
					}
				}
			}
			catch (InterruptedException ie) {
        		// ignore
			}
		}

		void runCycle() {
        	lock();
        	poller.runCycle();
        	waitForLock();
		}

        TestHarness() throws IOException {
            // Use temporaryFolder
            fs = null;
            temporaryFolder = new TemporaryFolder();
            temporaryFolder.create();
            testdir = temporaryFolder.newFolder("testdir").toPath();
            pollManager = spy(new TestPollManager());
            doCallRealMethod().when(pollManager).cycleEnded(any(CycleEndEvent.class));
            if (!testdir.toFile().exists()) {
                Files.createDirectory(testdir);
            }
            poller = new DirectoryPoller();
            poller.addDirectory(testdir.toFile());
            poller.setAutoMove(true);
            poller.setSendSingleFileEvent(true);
            poller.setPollInterval(1L);
            poller.addPollManager(pollManager);
            // Since we're bypassing run, create the automove directory.
            File automoveDir = PathNormalizer.normalize(poller.getAutoMoveDirectory(testdir.toFile()));
            if (!automoveDir.exists()) {
                if (!automoveDir.mkdirs()) {
                    throw new IOException(String.format("Creation of %s failed.", automoveDir.getAbsolutePath()));
                }
            }
        }

        TestHarness(Configuration configuration) throws IOException {
            // Use Jimfs virtual filesystem
            temporaryFolder = null;
            fs = Jimfs.newFileSystem(configuration);
            testdir = fs.getPath("/testdir");
			pollManager = spy(new TestPollManager());
			doCallRealMethod().when(pollManager).cycleEnded(any(CycleEndEvent.class));
            Files.createDirectory(testdir);
            poller = new DirectoryPoller();
            poller.addDirectory(testdir.toFile());
            poller.setAutoMove(true);
            poller.setSendSingleFileEvent(true);
            poller.setPollInterval(1L);
            poller.addPollManager(pollManager);
            // Since we're bypassing run, create the automove directory.
            Files.createDirectory(
                fs.getPath(
                    PathNormalizer.normalize(
                        poller.getAutoMoveDirectory(testdir.toFile())).getAbsolutePath()));
        }
    }
}
