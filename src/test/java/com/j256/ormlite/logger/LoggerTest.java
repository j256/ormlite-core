package com.j256.ormlite.logger;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class LoggerTest {

	private Logger logger;
	private LogBackend mockBackend;
	private Throwable throwable = new Throwable();

	@Before
	public void before() {
		mockBackend = createMock(LogBackend.class);
		logger = new Logger(mockBackend);
		assertSame(mockBackend, logger.getLogBackend());
	}

	@Test
	public void testArgAtStart() {
		String arg = "x";
		String end = " yyy";
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockBackend.log(Level.TRACE, arg + end);
		replay(mockBackend);
		logger.trace("{}" + end, arg);
		verify(mockBackend);
	}

	@Test
	public void testArgAtEnd() {
		String start = "yyy ";
		String arg = "x";
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockBackend.log(Level.TRACE, start + arg);
		replay(mockBackend);
		logger.trace(start + "{}", arg);
		verify(mockBackend);
	}

	@Test
	public void testArgsNextToEachOther() {
		String arg1 = "x";
		String arg2 = "y";
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockBackend.log(Level.TRACE, arg1 + arg2);
		replay(mockBackend);
		logger.trace("{}{}", arg1, arg2);
		verify(mockBackend);
	}

	@Test
	public void testArgsApart() {
		String arg1 = "x";
		String middle = " middle ";
		String arg2 = "y";
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockBackend.log(Level.TRACE, arg1 + middle + arg2);
		replay(mockBackend);
		logger.trace("{}" + middle + "{}", arg1, arg2);
		verify(mockBackend);
	}

	@Test
	public void testToManyArgs() {
		String start = "yyy ";
		String arg = "x";
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(true).times(2);
		mockBackend.log(Level.TRACE, start + arg);
		mockBackend.log(Level.TRACE, start + arg);
		replay(mockBackend);
		logger.trace(start + "{}{}{}{}{}", arg);
		logger.trace(start + "{}{}{}{}{}", new Object[] { arg });
		verify(mockBackend);
	}

	@Test
	public void testNoArgs() {
		String start = "yyy {}";
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(true);
		// should get back the {} because no args specified
		mockBackend.log(Level.TRACE, start);
		replay(mockBackend);
		logger.trace(start);
		verify(mockBackend);
	}

	@Test
	public void testNotEnoughArgs() {
		String start = "yyy ";
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(true);
		// should get back the {} because no args specified
		String arg = "hello";
		mockBackend.log(Level.TRACE, start + arg);
		replay(mockBackend);
		// we have 2 {} but only one arg
		logger.trace(start + "{}{}", arg);
		verify(mockBackend);
	}

	@Test
	public void testNoCurliesButArgs() {
		String start = "yyy ";
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(true);
		// should get back the {} because no args specified
		String arg = "hello";
		mockBackend.log(Level.TRACE, start);
		replay(mockBackend);
		// we have 2 {} but only one arg
		logger.trace(start, arg);
		verify(mockBackend);
	}

	@Test
	public void testObjectToString() {
		Foo arg = new Foo();
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockBackend.log(Level.TRACE, Foo.TO_STRING);
		replay(mockBackend);
		logger.trace("{}", arg);
		verify(mockBackend);
	}

	@Test
	public void testNullArg() {
		expect(mockBackend.isLevelEnabled(Level.TRACE)).andReturn(true);
		String prefix = "a";
		String suffix = "b";
		mockBackend.log(Level.TRACE, prefix + "null" + suffix);
		replay(mockBackend);
		logger.trace(prefix + "{}" + suffix, (Object) null);
		verify(mockBackend);
	}

	@Test
	public void testMessage() throws Exception {
		String msg = "ooooooh";
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, msg);
			replay(mockBackend);
			method.invoke(logger, msg);
			verify(mockBackend);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, msg, throwable);
			replay(mockBackend);
			method.invoke(logger, throwable, msg);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, msg);
			replay(mockBackend);
			logger.log(level, msg);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, msg, throwable);
			replay(mockBackend);
			logger.log(level, throwable, msg);
			verify(mockBackend);
		}
	}

	@Test
	public void testMessageArg0() throws Exception {
		String msg = "123 ";
		Object arg0 = "wow";
		String result = msg + arg0;
		String pattern = msg + "{}";
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			method.invoke(logger, pattern, arg0);
			verify(mockBackend);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			method.invoke(logger, throwable, pattern, arg0);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			logger.log(level, pattern, arg0);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			logger.log(level, throwable, pattern, arg0);
			verify(mockBackend);
		}
	}

	@Test
	public void testMessageArg0Arg1() throws Exception {
		String msg = "123 ";
		Object arg0 = "wow";
		Object arg1 = "zebra";
		String result = msg + "0" + arg0 + "01" + arg1 + "1";
		String pattern = msg + "0{}01{}1";
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			method.invoke(logger, pattern, arg0, arg1);
			verify(mockBackend);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object.class,
					Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			method.invoke(logger, throwable, pattern, arg0, arg1);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			logger.log(level, pattern, arg0, arg1);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			logger.log(level, throwable, pattern, arg0, arg1);
			verify(mockBackend);
		}
	}

	@Test
	public void testMessageArg0Arg1Arg2() throws Exception {
		String msg = "123 ";
		Object arg0 = "wow";
		Object arg1 = "zebra";
		Object arg2 = "wanker";
		String result = msg + "0" + arg0 + "01" + arg1 + "12" + arg2 + "2";
		String pattern = msg + "0{}01{}12{}2";
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class, Object.class,
					Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			method.invoke(logger, pattern, arg0, arg1, arg2);
			verify(mockBackend);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object.class,
					Object.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			method.invoke(logger, throwable, pattern, arg0, arg1, arg2);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			logger.log(level, pattern, arg0, arg1, arg2);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			logger.log(level, throwable, pattern, arg0, arg1, arg2);
			verify(mockBackend);
		}
	}

	@Test
	public void testMessageArg0Arg1Arg2Arg3() throws Exception {
		String msg = "123 ";
		Object arg0 = "wow";
		Object arg1 = "zebra";
		Object arg2 = "wanker";
		Object arg3 = "yowza";
		String result = msg + "0" + arg0 + "01" + arg1 + "12" + arg2 + "23" + arg3 + "3";
		String pattern = msg + "0{}01{}12{}23{}3";
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class, Object.class,
					Object.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			method.invoke(logger, pattern, arg0, arg1, arg2, arg3);
			verify(mockBackend);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object.class,
					Object.class, Object.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			method.invoke(logger, throwable, pattern, arg0, arg1, arg2, arg3);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			logger.log(level, pattern, arg0, arg1, arg2, arg3);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			logger.log(level, throwable, pattern, arg0, arg1, arg2, arg3);
			verify(mockBackend);
		}
	}

	@Test
	public void testMessageArgArray() throws Exception {
		String msg = "123 ";
		Object arg0 = "wow";
		Object arg1 = "zebra";
		Object arg2 = "wanker";
		Object arg3 = "avatar";
		Object[] argArray = new Object[] { arg0, arg1, arg2, arg3 };
		String result = msg + "0" + arg0 + "01" + arg1 + "12" + arg2 + "23" + arg3 + "3";
		String pattern = msg + "0{}01{}12{}23{}3";
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object[].class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			method.invoke(logger, pattern, argArray);
			verify(mockBackend);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object[].class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			method.invoke(logger, throwable, pattern, argArray);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			logger.log(level, pattern, argArray);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			logger.log(level, throwable, pattern, argArray);
			verify(mockBackend);
		}
	}

	@Test
	public void testMessageVarargs() throws Exception {
		String msg = "123 ";
		Object arg0 = "wow";
		Object arg1 = "zebra";
		Object arg2 = "wanker";
		Object arg3 = "avatar";
		Object[] argArray = new Object[] { arg0, arg1, arg2, arg3 };
		String result = msg + "0" + arg0 + "01" + arg1 + "12" + arg2 + "23" + arg3 + "3";
		String pattern = msg + "0{}01{}12{}23{}3";
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level) + "Args", String.class, Object[].class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			method.invoke(logger, pattern, argArray);
			verify(mockBackend);

			method = Logger.class.getMethod(getNameFromLevel(level) + "Args", Throwable.class, String.class,
					Object[].class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			method.invoke(logger, throwable, pattern, argArray);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			logger.logArgs(level, pattern, argArray);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			logger.logArgs(level, throwable, pattern, argArray);
			verify(mockBackend);
		}
	}

	@Test
	public void testMessageArgPrimitiveArray() throws Exception {
		byte[] argArray = new byte[] { 1, 2, 30 };
		String result = "12" + Arrays.toString(argArray) + "34";
		String pattern = "12{}34";
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			method.invoke(logger, pattern, argArray);
			verify(mockBackend);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			method.invoke(logger, throwable, pattern, argArray);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			logger.log(level, pattern, argArray);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			logger.log(level, throwable, pattern, argArray);
			verify(mockBackend);
		}
	}

	@Test
	public void testMessageArrayOfArrays() throws Exception {
		Object[] argArray = new Object[] { new byte[] { 1 }, new byte[] { 1, 2 }, new byte[] { 1, 2, 30 } };
		String result = "12" + Arrays.deepToString(argArray) + "34";
		String pattern = "12{}34";
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			method.invoke(logger, pattern, argArray);
			verify(mockBackend);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			method.invoke(logger, throwable, pattern, argArray);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result);
			replay(mockBackend);
			logger.log(level, pattern, (Object) argArray);
			verify(mockBackend);

			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			mockBackend.log(level, result, throwable);
			replay(mockBackend);
			logger.log(level, throwable, pattern, (Object) argArray);
			verify(mockBackend);
		}
	}

	private String getNameFromLevel(Level level) {
		String name;
		switch (level) {
			case WARNING:
				name = "warn";
				break;
			default:
				name = level.name().toLowerCase();
				break;
		}
		return name;
	}

	@Test
	public void testIsEnabled() {
		for (Level level : Level.values()) {
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			expect(mockBackend.isLevelEnabled(level)).andReturn(false);
			replay(mockBackend);
			assertTrue(logger.isLevelEnabled(level));
			assertFalse(logger.isLevelEnabled(level));
			verify(mockBackend);
		}
	}

	@Test
	public void testShouldNotCallToString() throws Exception {
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(false);
			replay(mockBackend);
			method.invoke(logger, "msg {}", new ToStringThrow());
			verify(mockBackend);
		}
	}

	@Test
	public void testShouldCallToString() throws Exception {
		for (Level level : Level.values()) {
			if (level == Level.OFF) {
				continue;
			}
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class);
			reset(mockBackend);
			expect(mockBackend.isLevelEnabled(level)).andReturn(true);
			replay(mockBackend);
			try {
				method.invoke(logger, "msg {}", new ToStringThrow());
				fail("Should have thrown");
			} catch (InvocationTargetException e) {
				assertTrue("should have thrown an IllegalStateException",
						e.getCause() instanceof IllegalStateException);
			}
			verify(mockBackend);
		}
	}

	@Test
	public void testObjectArrayArg() {
		String msg = "123 ";
		Object arg0 = "wow";
		Object arg1 = "zebra";
		Object arg2 = "wanker";
		Object arg3 = "avatar";
		Object[] argArray = new Object[] { arg0, arg1, arg2, arg3 };
		String result = msg + "0" + Arrays.toString(argArray) + "0";
		String pattern = msg + "0{}0";
		reset(mockBackend);
		expect(mockBackend.isLevelEnabled(Level.INFO)).andReturn(true);
		mockBackend.log(Level.INFO, result, throwable);
		replay(mockBackend);
		logger.info(throwable, pattern, (Object) argArray);
		verify(mockBackend);
	}

	@Test
	public void testGlobalLevel() {
		String msg1 = "123";
		String msg2 = "this should not show up";
		String msg3 = "this should show up";
		String msg4 = "this should show up too";
		reset(mockBackend);
		expect(mockBackend.isLevelEnabled(Level.INFO)).andReturn(true);
		mockBackend.log(Level.INFO, msg1);
		// no msg2
		expect(mockBackend.isLevelEnabled(Level.INFO)).andReturn(true);
		mockBackend.log(Level.INFO, msg3);
		expect(mockBackend.isLevelEnabled(Level.DEBUG)).andReturn(true);
		mockBackend.log(Level.DEBUG, msg4);
		try {
			replay(mockBackend);
			logger.info(msg1);
			Logger.setGlobalLogLevel(Level.OFF);
			logger.fatal(msg2);
			Logger.setGlobalLogLevel(Level.INFO);
			// global log does not match this so it should not be shown
			logger.debug(msg2);
			// global log matches info so this should be shown
			logger.info(msg3);
			Logger.setGlobalLogLevel(null);
			logger.debug(msg4);
			verify(mockBackend);
		} finally {
			Logger.setGlobalLogLevel(null);
		}
	}

	@Test
	public void testNoMessage() {
		String arg1 = "wefewf";
		int arg2 = 123;
		reset(mockBackend);
		expect(mockBackend.isLevelEnabled(Level.INFO)).andReturn(true);
		mockBackend.log(Level.INFO, "'" + arg1 + "', '" + arg2 + "'");
		replay(mockBackend);
		logger.info((String) null, arg1, arg2);
		verify(mockBackend);

		reset(mockBackend);
		expect(mockBackend.isLevelEnabled(Level.INFO)).andReturn(true);
		mockBackend.log(Level.INFO, Logger.NO_MESSAGE_MESSAGE);
		replay(mockBackend);
		logger.info((String) null);
		verify(mockBackend);
	}

	private static class Foo {
		final static String TO_STRING = "foo to string";

		@Override
		public String toString() {
			return TO_STRING;
		}
	}

	private static class ToStringThrow {
		@Override
		public String toString() {
			throw new IllegalStateException("To string should not have been called");
		}
	}
}
