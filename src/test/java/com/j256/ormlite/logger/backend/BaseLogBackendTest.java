package com.j256.ormlite.logger.backend;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;

public abstract class BaseLogBackendTest {

	protected final LogBackend log;

	protected BaseLogBackendTest(LogBackendFactory factory) {
		this.log = factory.createLogBackend(getClass().getSimpleName());
	}

	@Test
	public void testLogStuff() {
		log.log(Level.INFO, "hello there");
	}

	@Test
	public void testLevelEnabled() {
		boolean shouldBeEnabled = false;
		shouldBeEnabled = checkEnabled(log.isLevelEnabled(Level.TRACE), shouldBeEnabled);
		shouldBeEnabled = checkEnabled(log.isLevelEnabled(Level.DEBUG), shouldBeEnabled);
		shouldBeEnabled = checkEnabled(log.isLevelEnabled(Level.INFO), shouldBeEnabled);
		shouldBeEnabled = checkEnabled(log.isLevelEnabled(Level.WARNING), shouldBeEnabled);
		shouldBeEnabled = checkEnabled(log.isLevelEnabled(Level.ERROR), shouldBeEnabled);
		shouldBeEnabled = checkEnabled(log.isLevelEnabled(Level.FATAL), shouldBeEnabled);
	}

	@Test
	public void testTraceString() {
		log.log(Level.TRACE, "trace message");
	}

	@Test
	public void testTraceStringThrowable() {
		log.log(Level.TRACE, "trace message", new Throwable("trace throwable"));
	}

	@Test
	public void testDebugString() {
		log.log(Level.DEBUG, "debug message");
	}

	@Test
	public void testDebugStringThrowable() {
		log.log(Level.DEBUG, "debug message", new Throwable("debug throwable"));
	}

	@Test
	public void testInfoString() {
		log.log(Level.INFO, "info message");
	}

	@Test
	public void testInfoStringThrowable() {
		log.log(Level.INFO, "info message", new Throwable("info throwable"));
	}

	@Test
	public void testWarningString() {
		log.log(Level.WARNING, "warning message");
	}

	@Test
	public void testWarningStringThrowable() {
		log.log(Level.WARNING, "warning message", new Throwable("warning throwable"));
	}

	@Test
	public void testErrorString() {
		log.log(Level.ERROR, "error message");
	}

	@Test
	public void testErrorStringThrowable() {
		log.log(Level.ERROR, "error message", new Throwable("error throwable"));
	}

	@Test
	public void testFatalString() {
		log.log(Level.FATAL, "fatal message");
	}

	@Test
	public void testFatalStringThrowable() {
		log.log(Level.FATAL, "fatal message", new Throwable("fatal throwable"));
	}

	private boolean checkEnabled(boolean enabled, boolean shouldBeEnabled) {
		if (shouldBeEnabled) {
			assertTrue(enabled);
		}
		return enabled;
	}
}
