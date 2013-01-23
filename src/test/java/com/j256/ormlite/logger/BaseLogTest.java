package com.j256.ormlite.logger;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.logger.Log.Level;

public abstract class BaseLogTest {

	private final Log log;

	protected BaseLogTest(Log log) {
		this.log = log;
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
		log.log(Level.TRACE, "message");
	}

	@Test
	public void testTraceStringThrowable() {
		log.log(Level.TRACE, "message", new Throwable("log throwable"));
	}

	@Test
	public void testDebugString() {
		log.log(Level.DEBUG, "message");
	}

	@Test
	public void testDebugStringThrowable() {
		log.log(Level.DEBUG, "message", new Throwable("log throwable"));
	}

	@Test
	public void testInfoString() {
		log.log(Level.INFO, "message");
	}

	@Test
	public void testInfoStringThrowable() {
		log.log(Level.INFO, "message", new Throwable("log throwable"));
	}

	@Test
	public void testWarnString() {
		log.log(Level.WARNING, "message");
	}

	@Test
	public void testWarnStringThrowable() {
		log.log(Level.WARNING, "message", new Throwable("log throwable"));
	}

	@Test
	public void testErrorString() {
		log.log(Level.ERROR, "message");
	}

	@Test
	public void testErrorStringThrowable() {
		log.log(Level.ERROR, "message", new Throwable("log throwable"));
	}

	@Test
	public void testFatalString() {
		log.log(Level.FATAL, "message");
	}

	@Test
	public void testFatalStringThrowable() {
		log.log(Level.FATAL, "message", new Throwable("log throwable"));
	}

	private boolean checkEnabled(boolean enabled, boolean shouldBeEnabled) {
		if (shouldBeEnabled) {
			assertTrue(enabled);
		}
		return enabled;
	}
}
