package com.j256.ormlite.logger;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public abstract class BaseLogTest {

	private final Log log;

	protected BaseLogTest(Log log) {
		this.log = log;
	}

	@Test
	public void testLevelEnabled() {
		boolean shouldBeEnabled = false;
		shouldBeEnabled = checkEnabled(log.isTraceEnabled(), shouldBeEnabled);
		shouldBeEnabled = checkEnabled(log.isDebugEnabled(), shouldBeEnabled);
		shouldBeEnabled = checkEnabled(log.isInfoEnabled(), shouldBeEnabled);
		shouldBeEnabled = checkEnabled(log.isWarnEnabled(), shouldBeEnabled);
		shouldBeEnabled = checkEnabled(log.isErrorEnabled(), shouldBeEnabled);
		shouldBeEnabled = checkEnabled(log.isFatalEnabled(), shouldBeEnabled);
	}

	@Test
	public void testTraceString() {
		log.trace("message");
	}

	@Test
	public void testTraceStringThrowable() {
		log.trace("message", new Throwable("log throwable"));
	}

	@Test
	public void testDebugString() {
		log.debug("message");
	}

	@Test
	public void testDebugStringThrowable() {
		log.debug("message", new Throwable("log throwable"));
	}

	@Test
	public void testInfoString() {
		log.info("message");
	}

	@Test
	public void testInfoStringThrowable() {
		log.info("message", new Throwable("log throwable"));
	}

	@Test
	public void testWarnString() {
		log.warn("message");
	}

	@Test
	public void testWarnStringThrowable() {
		log.warn("message", new Throwable("log throwable"));
	}

	@Test
	public void testErrorString() {
		log.error("message");
	}

	@Test
	public void testErrorStringThrowable() {
		log.error("message", new Throwable("log throwable"));
	}

	@Test
	public void testFatalString() {
		log.fatal("message");
	}

	@Test
	public void testFatalStringThrowable() {
		log.fatal("message", new Throwable("log throwable"));
	}

	private boolean checkEnabled(boolean enabled, boolean shouldBeEnabled) {
		if (shouldBeEnabled) {
			assertTrue(enabled);
		}
		return enabled;
	}
}
