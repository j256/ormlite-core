package com.j256.ormlite.logger;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class LoggerTest {

	private Logger logger;
	private Log mockLog;
	private Throwable throwable = new Throwable();

	@Before
	public void before() {
		logger = new Logger(getClass().getName());
		mockLog = createMock(Log.class);
		logger.setLog(mockLog);
	}

	@Test
	public void testArgAtStart() {
		String arg = "x";
		String end = " yyy";
		expect(mockLog.isTraceEnabled()).andReturn(true);
		mockLog.trace(arg + end, null);
		replay(mockLog);
		logger.trace("{}" + end, arg);
		verify(mockLog);
	}

	@Test
	public void testArgAtEnd() {
		String start = "yyy ";
		String arg = "x";
		expect(mockLog.isTraceEnabled()).andReturn(true);
		mockLog.trace(start + arg, null);
		replay(mockLog);
		logger.trace(start + "{}", arg);
		verify(mockLog);
	}

	@Test
	public void testArgsNextToEachOther() {
		String arg1 = "x";
		String arg2 = "y";
		expect(mockLog.isTraceEnabled()).andReturn(true);
		mockLog.trace(arg1 + arg2, null);
		replay(mockLog);
		logger.trace("{}{}", arg1, arg2);
		verify(mockLog);
	}

	@Test
	public void testArgsApart() {
		String arg1 = "x";
		String middle = " middle ";
		String arg2 = "y";
		expect(mockLog.isTraceEnabled()).andReturn(true);
		mockLog.trace(arg1 + middle + arg2, null);
		replay(mockLog);
		logger.trace("{}" + middle + "{}", arg1, arg2);
		verify(mockLog);
	}

	@Test
	public void testToManyArgs() {
		String start = "yyy ";
		String arg = "x";
		expect(mockLog.isTraceEnabled()).andReturn(true);
		mockLog.trace(start + arg, null);
		replay(mockLog);
		logger.trace(start + "{}", arg);
		verify(mockLog);
	}

	@Test
	public void testNotEnoughArgs() {
		String start = "yyy ";
		expect(mockLog.isTraceEnabled()).andReturn(true);
		mockLog.trace(start, null);
		replay(mockLog);
		logger.trace(start + "{}");
		verify(mockLog);
	}

	@Test
	public void testObjectToString() {
		Foo arg = new Foo();
		expect(mockLog.isTraceEnabled()).andReturn(true);
		mockLog.trace(Foo.TO_STRING, null);
		replay(mockLog);
		logger.trace("{}", arg);
		verify(mockLog);
	}

	@Test
	public void testTraceThrowable() {
		String msg = "ooooooh";
		expect(mockLog.isTraceEnabled()).andReturn(true);
		mockLog.trace(msg, throwable);
		replay(mockLog);
		logger.trace(throwable, msg);
		verify(mockLog);
	}

	@Test
	public void testDebug() {
		String msg = "ooooooh";
		expect(mockLog.isDebugEnabled()).andReturn(true);
		mockLog.debug(msg, null);
		replay(mockLog);
		logger.debug(msg);
		verify(mockLog);
	}

	@Test
	public void testDebugThrowable() {
		String msg = "ooooooh";
		expect(mockLog.isDebugEnabled()).andReturn(true);
		mockLog.debug(msg, throwable);
		replay(mockLog);
		logger.debug(throwable, msg);
		verify(mockLog);
	}

	@Test
	public void testInfo() {
		String msg = "ooooooh";
		expect(mockLog.isInfoEnabled()).andReturn(true);
		mockLog.info(msg, null);
		replay(mockLog);
		logger.info(msg);
		verify(mockLog);
	}

	@Test
	public void testInfoThrowable() {
		String msg = "ooooooh";
		expect(mockLog.isInfoEnabled()).andReturn(true);
		mockLog.info(msg, throwable);
		replay(mockLog);
		logger.info(throwable, msg);
		verify(mockLog);
	}

	@Test
	public void testWarn() {
		String msg = "ooooooh";
		expect(mockLog.isWarnEnabled()).andReturn(true);
		mockLog.warn(msg, null);
		replay(mockLog);
		logger.warn(msg);
		verify(mockLog);
	}

	@Test
	public void testWarnThrowable() {
		String msg = "ooooooh";
		expect(mockLog.isWarnEnabled()).andReturn(true);
		mockLog.warn(msg, throwable);
		replay(mockLog);
		logger.warn(throwable, msg);
		verify(mockLog);
	}

	@Test
	public void testError() {
		String msg = "ooooooh";
		expect(mockLog.isErrorEnabled()).andReturn(true);
		mockLog.error(msg, null);
		replay(mockLog);
		logger.error(msg);
		verify(mockLog);
	}

	@Test
	public void testErrorThrowable() {
		String msg = "ooooooh";
		expect(mockLog.isErrorEnabled()).andReturn(true);
		mockLog.error(msg, throwable);
		replay(mockLog);
		logger.error(throwable, msg);
		verify(mockLog);
	}

	@Test
	public void testFatal() {
		String msg = "ooooooh";
		expect(mockLog.isFatalEnabled()).andReturn(true);
		mockLog.fatal(msg, null);
		replay(mockLog);
		logger.fatal(msg);
		verify(mockLog);
	}

	@Test
	public void testFatalThrowable() {
		String msg = "ooooooh";
		expect(mockLog.isFatalEnabled()).andReturn(true);
		mockLog.fatal(msg, throwable);
		replay(mockLog);
		logger.fatal(throwable, msg);
		verify(mockLog);
	}

	@Test
	public void testIsTraceEnabled() {
		expect(mockLog.isTraceEnabled()).andReturn(true);
		expect(mockLog.isTraceEnabled()).andReturn(false);
		replay(mockLog);
		assertTrue(logger.isTraceEnabled());
		assertFalse(logger.isTraceEnabled());
		verify(mockLog);
	}

	@Test
	public void testIsDebugEnabled() {
		expect(mockLog.isDebugEnabled()).andReturn(true);
		expect(mockLog.isDebugEnabled()).andReturn(false);
		replay(mockLog);
		assertTrue(logger.isDebugEnabled());
		assertFalse(logger.isDebugEnabled());
		verify(mockLog);
	}

	@Test
	public void testIsInfoEnabled() {
		expect(mockLog.isInfoEnabled()).andReturn(true);
		expect(mockLog.isInfoEnabled()).andReturn(false);
		replay(mockLog);
		assertTrue(logger.isInfoEnabled());
		assertFalse(logger.isInfoEnabled());
		verify(mockLog);
	}

	@Test
	public void testIsWarnEnabled() {
		expect(mockLog.isWarnEnabled()).andReturn(true);
		expect(mockLog.isWarnEnabled()).andReturn(false);
		replay(mockLog);
		assertTrue(logger.isWarnEnabled());
		assertFalse(logger.isWarnEnabled());
		verify(mockLog);
	}

	@Test
	public void testIsErrorEnabled() {
		expect(mockLog.isErrorEnabled()).andReturn(true);
		expect(mockLog.isErrorEnabled()).andReturn(false);
		replay(mockLog);
		assertTrue(logger.isErrorEnabled());
		assertFalse(logger.isErrorEnabled());
		verify(mockLog);
	}

	@Test
	public void testIsFatalEnabled() {
		expect(mockLog.isFatalEnabled()).andReturn(true);
		expect(mockLog.isFatalEnabled()).andReturn(false);
		replay(mockLog);
		assertTrue(logger.isFatalEnabled());
		assertFalse(logger.isFatalEnabled());
		verify(mockLog);
	}

	private class Foo {
		final static String TO_STRING = "foo to string";
		@Override
		public String toString() {
			return TO_STRING;
		}
	}
}
