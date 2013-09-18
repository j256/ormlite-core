package com.j256.ormlite.logger;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.logger.Log.Level;

public class LoggerTest {

	private Logger logger;
	private Log mockLog;
	private Throwable throwable = new Throwable();

	@Before
	public void before() {
		mockLog = createMock(Log.class);
		logger = new Logger(mockLog);
	}

	@Test
	public void testArgAtStart() {
		String arg = "x";
		String end = " yyy";
		expect(mockLog.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockLog.log(Level.TRACE, arg + end);
		replay(mockLog);
		logger.trace("{}" + end, arg);
		verify(mockLog);
	}

	@Test
	public void testArgAtEnd() {
		String start = "yyy ";
		String arg = "x";
		expect(mockLog.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockLog.log(Level.TRACE, start + arg);
		replay(mockLog);
		logger.trace(start + "{}", arg);
		verify(mockLog);
	}

	@Test
	public void testArgsNextToEachOther() {
		String arg1 = "x";
		String arg2 = "y";
		expect(mockLog.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockLog.log(Level.TRACE, arg1 + arg2);
		replay(mockLog);
		logger.trace("{}{}", arg1, arg2);
		verify(mockLog);
	}

	@Test
	public void testArgsApart() {
		String arg1 = "x";
		String middle = " middle ";
		String arg2 = "y";
		expect(mockLog.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockLog.log(Level.TRACE, arg1 + middle + arg2);
		replay(mockLog);
		logger.trace("{}" + middle + "{}", arg1, arg2);
		verify(mockLog);
	}

	@Test
	public void testToManyArgs() {
		String start = "yyy ";
		String arg = "x";
		expect(mockLog.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockLog.log(Level.TRACE, start + arg);
		replay(mockLog);
		logger.trace(start + "{}", arg);
		verify(mockLog);
	}

	@Test
	public void testNotEnoughArgs() {
		String start = "yyy ";
		expect(mockLog.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockLog.log(Level.TRACE, start);
		replay(mockLog);
		logger.trace(start + "{}");
		verify(mockLog);
	}

	@Test
	public void testObjectToString() {
		Foo arg = new Foo();
		expect(mockLog.isLevelEnabled(Level.TRACE)).andReturn(true);
		mockLog.log(Level.TRACE, Foo.TO_STRING);
		replay(mockLog);
		logger.trace("{}", arg);
		verify(mockLog);
	}

	@Test
	public void testNullArg() {
		expect(mockLog.isLevelEnabled(Level.TRACE)).andReturn(true);
		String prefix = "a";
		String suffix = "b";
		mockLog.log(Level.TRACE, prefix + "null" + suffix);
		replay(mockLog);
		logger.trace(prefix + "{}" + suffix, (Object) null);
		verify(mockLog);
	}

	@Test
	public void testMessage() throws Exception {
		String msg = "ooooooh";
		for (Level level : Level.values()) {
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, msg);
			replay(mockLog);
			method.invoke(logger, msg);
			verify(mockLog);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, msg, throwable);
			replay(mockLog);
			method.invoke(logger, throwable, msg);
			verify(mockLog);

			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, msg);
			replay(mockLog);
			logger.log(level, msg);
			verify(mockLog);

			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, msg, throwable);
			replay(mockLog);
			logger.log(level, throwable, msg);
			verify(mockLog);
		}
	}

	@Test
	public void testMessageArg0() throws Exception {
		String msg = "123 ";
		Object arg0 = "wow";
		String result = msg + arg0;
		String pattern = msg + "{}";
		for (Level level : Level.values()) {
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result);
			replay(mockLog);
			method.invoke(logger, pattern, arg0);
			verify(mockLog);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object.class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result, throwable);
			replay(mockLog);
			method.invoke(logger, throwable, pattern, arg0);
			verify(mockLog);

			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result);
			replay(mockLog);
			logger.log(level, pattern, arg0);
			verify(mockLog);

			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result, throwable);
			replay(mockLog);
			logger.log(level, throwable, pattern, arg0);
			verify(mockLog);
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
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class, Object.class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result);
			replay(mockLog);
			method.invoke(logger, pattern, arg0, arg1);
			verify(mockLog);

			method =
					Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object.class,
							Object.class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result, throwable);
			replay(mockLog);
			method.invoke(logger, throwable, pattern, arg0, arg1);
			verify(mockLog);

			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result);
			replay(mockLog);
			logger.log(level, pattern, arg0, arg1);
			verify(mockLog);

			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result, throwable);
			replay(mockLog);
			logger.log(level, throwable, pattern, arg0, arg1);
			verify(mockLog);
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
			Method method =
					Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class, Object.class,
							Object.class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result);
			replay(mockLog);
			method.invoke(logger, pattern, arg0, arg1, arg2);
			verify(mockLog);

			method =
					Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object.class,
							Object.class, Object.class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result, throwable);
			replay(mockLog);
			method.invoke(logger, throwable, pattern, arg0, arg1, arg2);
			verify(mockLog);

			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result);
			replay(mockLog);
			logger.log(level, pattern, arg0, arg1, arg2);
			verify(mockLog);

			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result, throwable);
			replay(mockLog);
			logger.log(level, throwable, pattern, arg0, arg1, arg2);
			verify(mockLog);
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
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object[].class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result);
			replay(mockLog);
			method.invoke(logger, pattern, argArray);
			verify(mockLog);

			method = Logger.class.getMethod(getNameFromLevel(level), Throwable.class, String.class, Object[].class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result, throwable);
			replay(mockLog);
			method.invoke(logger, throwable, pattern, argArray);
			verify(mockLog);

			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result);
			replay(mockLog);
			logger.log(level, pattern, argArray);
			verify(mockLog);

			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			mockLog.log(level, result, throwable);
			replay(mockLog);
			logger.log(level, throwable, pattern, argArray);
			verify(mockLog);
		}
	}

	private String getNameFromLevel(Level level) {
		String name;
		switch (level) {
			case WARNING :
				name = "warn";
				break;
			default :
				name = level.name().toLowerCase();
				break;
		}
		return name;
	}

	@Test
	public void testIsEnabled() {
		for (Level level : Level.values()) {
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			expect(mockLog.isLevelEnabled(level)).andReturn(false);
			replay(mockLog);
			assertTrue(logger.isLevelEnabled(level));
			assertFalse(logger.isLevelEnabled(level));
			verify(mockLog);
		}
	}

	@Test
	public void testShouldNotCallToString() throws Exception {
		for (Level level : Level.values()) {
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(false);
			replay(mockLog);
			method.invoke(logger, "msg {}", new ToStringThrow());
			verify(mockLog);
		}
	}

	@Test
	public void testShouldCallToString() throws Exception {
		for (Level level : Level.values()) {
			Method method = Logger.class.getMethod(getNameFromLevel(level), String.class, Object.class);
			reset(mockLog);
			expect(mockLog.isLevelEnabled(level)).andReturn(true);
			replay(mockLog);
			try {
				method.invoke(logger, "msg {}", new ToStringThrow());
				fail("Should have thrown");
			} catch (InvocationTargetException e) {
				assertTrue("should have thrown an IllegalStateException", e.getCause() instanceof IllegalStateException);
			}
			verify(mockLog);
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
		reset(mockLog);
		expect(mockLog.isLevelEnabled(Level.INFO)).andReturn(true);
		mockLog.log(Level.INFO, result, throwable);
		replay(mockLog);
		logger.info(throwable, pattern, (Object) argArray);
		verify(mockLog);
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
