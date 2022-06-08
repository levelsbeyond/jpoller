package org.sadun.util;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.sadun.util.polling.DirectoryPoller;
import org.testng.annotations.Test;

public class PostProcessMarkerManagerTest {

	@Test
	public void testPostProcessMarkerManagerDefault() {
		final PostProcessMarkerManager manager = new PostProcessMarkerManager();
		assertThat(manager.getPostProcessDelayMinutes()).isZero();
	}

	@Test
	public void testPostProcessMarkerManagerWithDelay() {
		final PostProcessMarkerManager manager = new PostProcessMarkerManager(42);
		assertThat(manager.getPostProcessDelayMinutes()).isEqualTo(42);
	}

	@Test
	public void testSetPostProcessDelayMinutes() {
		final PostProcessMarkerManager manager = new PostProcessMarkerManager();
		assertThat(manager.getPostProcessDelayMinutes()).isZero();
		manager.setPostProcessDelayMinutes(42);
		assertThat(manager.getPostProcessDelayMinutes()).isEqualTo(42);
		manager.setPostProcessDelayMinutes(99);
		assertThat(manager.getPostProcessDelayMinutes()).isEqualTo(99);
	}


	@Test
	public void testGetPostProcessMarkerFile() {
		final TestHarness testHarness = new TestHarness();
		try {
			final String fileName = "testFile.dat";
			final File testFile = testHarness.getPath().resolve(fileName).toFile();
			final File markerFile = testHarness.manager.getPostProcessMarkerFile(testFile);
			assertThat(markerFile.getAbsolutePath()).isEqualTo(Paths.get(testFile.getParent(), String.format(".~%s~", testFile.getName())).toAbsolutePath().toString());
		} finally {
			testHarness.deleteTempDir();
		}
	}

	@Test
	public void testPostProcessDelayPending() throws IOException {
		final TestHarness testHarness = new TestHarness();
		try {
			final String fileName = "testFile.dat";
			final File testFile = testHarness.getPath().resolve(fileName).toFile();
			final File markerFile = testHarness.manager.getPostProcessMarkerFile(testFile);
			assertThat(testHarness.manager.postProcessDelayPending(testFile)).isFalse();
			testHarness.manager.setPostProcessDelayMinutes(30);
			assertThat(testHarness.manager.getPostProcessDelayMinutes()).isEqualTo(30);
			Files.write(Paths.get(testFile.getAbsolutePath()), "This is a test file\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
			Files.write(Paths.get(markerFile.getAbsolutePath()), "action=delete\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
			assertThat(testFile).exists();
			assertThat(markerFile).exists();
			assertThat(testHarness.manager.getPostProcessDelayMinutes()).isEqualTo(30);
			assertThat(testHarness.manager.postProcessDelayPending(testFile)).isTrue();
			// Set the last modified time to 1 ms expired.
			Files.setLastModifiedTime(
				Paths.get(markerFile.getAbsolutePath()),
				FileTime.from(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(testHarness.manager.getPostProcessDelayMinutes()) - 1, TimeUnit.MILLISECONDS)
			);
			assertThat(testHarness.manager.isPostProcessFileExpired(markerFile)).isTrue();
			assertThat(testHarness.manager.postProcessDelayPending(testFile)).isFalse();
		} finally {
			testHarness.deleteTempDir();
		}
	}

	@Test
	public void testReadPostProcessFile() throws IOException {
		final TestHarness testHarness = new TestHarness();
		try {
			final String fileName = "testFile.dat";
			final File testFile = testHarness.getPath().resolve(fileName).toFile();
			final File markerFile = testHarness.manager.getPostProcessMarkerFile(testFile);
			assertThat(testHarness.manager.postProcessDelayPending(testFile)).isFalse();
			testHarness.manager.setPostProcessDelayMinutes(30);
			Files.write(Paths.get(testFile.getAbsolutePath()), "This is a test file\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
			Files.write(Paths.get(markerFile.getAbsolutePath()), "action=delete\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
			assertThat(testFile).exists();
			assertThat(markerFile).exists();
			assertThat(testHarness.manager.postProcessDelayPending(testFile)).isTrue();
			// Parse test file
			Map<String, String> result = testHarness.manager.readPostProcessFile(markerFile);
			Map<String, String> expected = new HashMap<>();
			expected.put("action", "delete");
			assertThat(result).isEqualTo(expected);
			Files.write(Paths.get(markerFile.getAbsolutePath()),
						String.format("action=move\ndest=%s\n", testFile.getAbsolutePath()).getBytes(StandardCharsets.UTF_8),
						StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
			result = testHarness.manager.readPostProcessFile(markerFile);
			expected = new HashMap<>();
			expected.put("action", "move");
			expected.put("dest", testFile.getAbsolutePath());
			assertThat(result).isEqualTo(expected);
		} finally {
			testHarness.deleteTempDir();
		}
	}

	@Test
	public void tesRemovePostProcessFile() throws IOException {
		final TestHarness testHarness = new TestHarness();
		try {
			final String fileName = "testFile.dat";
			final File testFile = testHarness.getPath().resolve(fileName).toFile();
			final File markerFile = testHarness.manager.getPostProcessMarkerFile(testFile);
			assertThat(testHarness.manager.postProcessDelayPending(testFile)).isFalse();
			testHarness.manager.setPostProcessDelayMinutes(30);
			Files.write(Paths.get(testFile.getAbsolutePath()), "This is a test file\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
			Files.write(Paths.get(markerFile.getAbsolutePath()), "action=delete\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.SYNC);
			assertThat(testFile).exists();
			assertThat(markerFile).exists();
			assertThat(testHarness.manager.postProcessDelayPending(testFile)).isTrue();
			// Parse test file
			assertThat(testHarness.manager.removePostProcessMarker(testFile)).isTrue();
			assertThat(testFile).exists();
			assertThat(markerFile).doesNotExist();
			// Do it again.  If it doesn't exist, it should still return true.
			assertThat(testHarness.manager.removePostProcessMarker(testFile)).isTrue();
			assertThat(testFile).exists();
			assertThat(markerFile).doesNotExist();
		} finally {
			testHarness.deleteTempDir();
		}
	}

	class TestHarness {

		@Spy
		@InjectMocks
		PostProcessMarkerManager manager;

		Path path = null;

		TestHarness() {
			initMocks(this);
		}

		Path getPath() {
			if (path == null) {
				path = createTempDir();
			}
			return path;
		}

		Path createTempDir() {
			try {
				return Files.createTempDirectory(this.getClass().getName());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		boolean deleteTempDir() {
			final boolean rval;
			if (path == null) {
				rval = false;
			} else {
				rval = DirectoryPoller.deleteDir(path.toFile());
				path = null;
			}
			return rval;
		}

	}

}
