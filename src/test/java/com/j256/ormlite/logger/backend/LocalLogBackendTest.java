package com.j256.ormlite.logger.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LoggerConstants;
import com.j256.ormlite.logger.backend.LocalLogBackend.LocalLogBackendFactory;

public class LocalLogBackendTest extends BaseLogBackendTest {

	public LocalLogBackendTest() {
		super(new LocalLogBackendFactory());
	}

	@Test
	public void testLevelProperty() {
		LogBackend log = new LocalLogBackend("foo");
		if (log.isLevelEnabled(Level.TRACE)) {
			return;
		}
		System.setProperty(LoggerConstants.LOCAL_LOG_LEVEL_PROPERTY, "TRACE");
		try {
			log = new LocalLogBackend("foo");
			assertTrue(log.isLevelEnabled(Level.TRACE));
		} finally {
			System.clearProperty(LoggerConstants.LOCAL_LOG_LEVEL_PROPERTY);
		}
	}

	@Test
	public void testInvalidLevelProperty() {
		System.setProperty(LoggerConstants.LOCAL_LOG_LEVEL_PROPERTY, "not a valid level");
		try {
			assertThrowsExactly(IllegalArgumentException.class, () -> {
				new LocalLogBackend("foo");
			});
		} finally {
			System.clearProperty(LoggerConstants.LOCAL_LOG_LEVEL_PROPERTY);
		}
	}

	@Test
	public void testFileProperty() {
		String logPath = "target/foo.txt";
		File logFile = new File(logPath);
		logFile.delete();
		LocalLogBackend.openLogFile(logPath);
		try {
			LocalLogBackend log = new LocalLogBackend("foo");
			assertTrue(log.isLevelEnabled(Level.FATAL));
			String msg = "fpjwefpwejfpwfjwe";
			log.log(Level.FATAL, msg);
			log.flush();
			assertTrue(logFile.exists());
			assertTimeout(Duration.ofSeconds(10), () -> {
				while (logFile.length() < msg.length()) {
					Thread.sleep(100);
				}
			});
		} finally {
			LocalLogBackend.openLogFile(null);
			logFile.delete();
		}
	}

	@Test
	public void testInvalidFileProperty() {
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			LocalLogBackend.openLogFile("not-a-proper-directory-name-we-hope/foo.txt");
		});
	}

	@Test
	public void testNotEnabled() {
		String logPath = "target/foo.txt";
		File logFile = new File(logPath);
		logFile.delete();
		LocalLogBackend.openLogFile(logPath);
		try {
			LocalLogBackend log = new LocalLogBackend("foo");
			if (log.isLevelEnabled(Level.TRACE)) {
				return;
			}
			String msg = "fpjwefpwejfpwfjwe";
			log.log(Level.TRACE, msg);
			log.flush();
			assertTrue(logFile.exists());
			assertEquals(0, logFile.length());
		} finally {
			LocalLogBackend.openLogFile(null);
			logFile.delete();
		}
	}

	@Test
	public void testMultipleLineMatches() {
		/*
		 * This depends on the contents of the simpleLoggingLocalLog.properties file.
		 */
		LocalLogBackend backend = new LocalLogBackend("com.j256.simplelogging.Something");
		assertTrue(backend.isLevelEnabled(Level.DEBUG));
		assertFalse(backend.isLevelEnabled(Level.TRACE));
		backend = new LocalLogBackend("com.j256.simplelogging.LocalLogBackendTest");
		assertTrue(backend.isLevelEnabled(Level.DEBUG));
		assertTrue(backend.isLevelEnabled(Level.TRACE));
	}
}
