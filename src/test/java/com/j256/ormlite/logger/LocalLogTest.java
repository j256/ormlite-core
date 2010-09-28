package com.j256.ormlite.logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

public class LocalLogTest extends BaseLogTest {

	public LocalLogTest() {
		super(new LocalLog("CommonsLoggingLogTest"));
	}

	@Test
	public void testLevelProperty() {
		Log log = new LocalLog("foo");
		if (log.isTraceEnabled()) {
			return;
		}
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "TRACE");
		try {
			log = new LocalLog("foo");
			assertTrue(log.isTraceEnabled());
		} finally {
			System.clearProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidLevelProperty() {
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "not a valid level");
		try {
			new LocalLog("foo");
		} finally {
			System.clearProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY);
		}
	}

	@Test(timeout = 10000)
	public void testFileProperty() throws Exception {
		String logPath = "target/foo.txt";
		File logFile = new File(logPath);
		logFile.delete();
		System.setProperty(LocalLog.LOCAL_LOG_FILE_PROPERTY, logPath);
		try {
			LocalLog log = new LocalLog("foo");
			assertTrue(log.isFatalEnabled());
			String msg = "fpjwefpwejfpwfjwe";
			log.fatal(msg);
			log.flush();
			assertTrue(logFile.exists());
			while (logFile.length() < msg.length()) {
				Thread.sleep(100);
			}
		} finally {
			System.clearProperty(LocalLog.LOCAL_LOG_FILE_PROPERTY);
			logFile.delete();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidFileProperty() {
		String logPath = "not-a-proper-directory-name-we-hope/foo.txt";
		System.setProperty(LocalLog.LOCAL_LOG_FILE_PROPERTY, logPath);
		try {
			new LocalLog("foo");
		} finally {
			System.clearProperty(LocalLog.LOCAL_LOG_FILE_PROPERTY);
		}
	}

	@Test
	public void testNotEnabled() {
		String logPath = "target/foo.txt";
		File logFile = new File(logPath);
		logFile.delete();
		System.setProperty(LocalLog.LOCAL_LOG_FILE_PROPERTY, logPath);
		try {
			LocalLog log = new LocalLog("foo");
			if (log.isTraceEnabled()) {
				return;
			}
			String msg = "fpjwefpwejfpwfjwe";
			log.trace(msg);
			log.flush();
			assertTrue(logFile.exists());
			assertEquals(0, logFile.length());
		} finally {
			System.clearProperty(LocalLog.LOCAL_LOG_FILE_PROPERTY);
			logFile.delete();
		}
	}
}
