package com.j256.ormlite.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;

/**
 * A class which helps us verify that we are running symetric versions.
 * 
 * @author graywatson
 */
public class VersionUtils {

	private static final String CORE_VERSION_FILE = "/com/j256/ormlite/core/VERSION.txt";
	private static final String JDBC_VERSION_FILE = "/com/j256/ormlite/jdbc/VERSION.txt";
	private static final String ANDROID_VERSION_FILE = "/com/j256/ormlite/android/VERSION.txt";

	private static Logger logger;

	private VersionUtils() {
		// only for static methods
	}

	/**
	 * Verifies that the ormlite-core and -jdbc version files hold the same string.
	 */
	public static final void checkCoreVersusJdbcVersions() {
		String core = readCoreVersion();
		String jdbc = readJdbcVersion();
		logVersionErrors("core", core, "jdbc", jdbc);
	}

	/**
	 * Verifies that the ormlite-core and -android version files hold the same string.
	 */
	public static final void checkCoreVersusAndroidVersions() {
		String core = readCoreVersion();
		String android = readAndroidVersion();
		logVersionErrors("core", core, "android", android);
	}

	/**
	 * Log error information
	 */
	private static void logVersionErrors(String label1, String version1, String label2, String version2) {
		if (version1 == null) {
			if (version2 != null) {
				getLogger().error("Unknown version for {}, version for {} is {}", label1, label2, version2);
			}
		} else {
			if (version2 == null) {
				getLogger().error("Unknown version for {}, version for {} is {}", label2, label1, version1);
			} else if (!version1.equals(version2)) {
				getLogger().error("Mismatched versions: {} is {}, while {} is {}",
						new Object[] { label1, version1, label2, version2 });
			}
		}
	}

	/**
	 * Read and return the version for the core package.
	 */
	private static String readCoreVersion() {
		return getVersionFromFile(CORE_VERSION_FILE);
	}

	/**
	 * Read and return the version for the core package.
	 */
	private static String readJdbcVersion() {
		return getVersionFromFile(JDBC_VERSION_FILE);
	}

	/**
	 * Read and return the version for the core package.
	 */
	private static String readAndroidVersion() {
		return getVersionFromFile(ANDROID_VERSION_FILE);
	}

	private static String getVersionFromFile(String file) {
		InputStream inputStream = VersionUtils.class.getResourceAsStream(file);
		if (inputStream == null) {
			getLogger().error("Could not find version file {}", file);
			return null;
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		String version;
		try {
			version = reader.readLine();
		} catch (IOException e) {
			// exception ignored
			getLogger().error(e, "Could not read version from {}", file);
			return null;
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignored
			}
		}
		if (version == null) {
			getLogger().error("No version specified in {}", file);
		}
		return version;
	}

	private static Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(VersionUtils.class);
		}
		return logger;
	}
}
