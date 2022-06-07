package org.sadun.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostProcessMarkerManager {
	private final static Logger logger = LoggerFactory.getLogger(PostProcessMarkerManager.class);

	int postProcessDelayMinutes;

	public PostProcessMarkerManager() {
		setPostProcessDelayMinutes(0);
	}


	public PostProcessMarkerManager(final int postProcessDelayMinutes) {
		setPostProcessDelayMinutes(postProcessDelayMinutes);
	}


	public void setPostProcessDelayMinutes(final int delay) {

		postProcessDelayMinutes = delay;
	}

	public int getPostProcessDelayMinutes() {
		return postProcessDelayMinutes;
	}

	public File getPostProcessMarkerFile(File file) {
		return Paths.get(file.getParent(), String.format(".~%s~", file.getName())).toFile();
	}

	public boolean postProcessDelayPending(File file) {
		// File contains the destination path to move the file to
		final File processedMarkerFile = getPostProcessMarkerFile(file);
		return !isPostProcessFileExpired(processedMarkerFile);
	}

	public boolean isPostProcessFileExpired(final File processedMarkerFile) {
		final long delayMillis = postProcessDelayMinutes * 60000L;
		return delayMillis < 1 ||
			(processedMarkerFile.exists() && processedMarkerFile.isFile() && processedMarkerFile.canRead() && processedMarkerFile.length() > 0
			&& System.currentTimeMillis() - processedMarkerFile.lastModified() > delayMillis);
	}

	public Map<String, String> readPostProcessFile(final File processedMarkerFile) {
		Map<String, String> rval = new HashMap<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(processedMarkerFile))) {
			for (String line; (line = reader.readLine()) != null; ) {
				String[] row = line.split("=", 2);
				if (row.length == 2) {
					rval.put(row[0], row[1]);
				}
			}
		} catch (IOException e) {
			logger.error(String.format("Error parsing file %s", processedMarkerFile.getAbsolutePath()), e);
		}
		return rval;
	}

	public boolean removePostProcessMarker(File file) {
		final File processedMarkerFile = getPostProcessMarkerFile(file);
		if (!processedMarkerFile.exists()) {
			return true;
		}
		return processedMarkerFile.delete();
	}
}
