package com.j256.ormlite.logger.backend;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackendFactory;

public class Log4jLogBackendTest extends BaseLogBackendTest {

	private static LogBackendFactory factory;

	static {
		// we have to do this because we want to use the log4j class but only if it's on the classpath
		try {
			factory = new Log4jLogBackend.Log4jLogBackendFactory("LOG4J ");
			// now we test the factory to make sure it works
			factory.createLogBackend("testing").isLevelEnabled(Level.TRACE);
		} catch (Throwable th) {
			// if there is an error then just delegate to log4j v2
			factory = new Log4j2LogBackend.Log4j2LogBackendFactory("LOG4J2 ");
		}
	}

	public Log4jLogBackendTest() {
		super(factory);
	}

	@Test
	public void testEnabled() {
		assertTrue(log.isLevelEnabled(Level.TRACE));
	}
}
