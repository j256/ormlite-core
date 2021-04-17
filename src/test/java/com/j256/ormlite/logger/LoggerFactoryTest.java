package com.j256.ormlite.logger;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;

import org.junit.Test;

public class LoggerFactoryTest {

	@Test
	public void testGetLoggerClass() {
		assertNotNull(LoggerFactory.getLogger(getClass()));
	}

	@Test
	public void testGetLoggerString() {
		assertNotNull(LoggerFactory.getLogger(getClass().getName()));
	}

	@Test
	public void testConstructor() throws Exception {
		@SuppressWarnings("rawtypes")
		Constructor[] constructors = LoggerFactory.class.getDeclaredConstructors();
		assertEquals(1, constructors.length);
		constructors[0].setAccessible(true);
		constructors[0].newInstance();
	}

	@Test
	public void testLogTypes() {
		checkLog(LogBackendType.SLF4J, Slf4jLoggingLogBackend.class, true);
		checkLog(LogBackendType.ANDROID, LocalLogBackend.class, false);
		checkLog(LogBackendType.COMMONS_LOGGING, CommonsLoggingLogBackend.class, true);
		checkLog(LogBackendType.LOG4J2, Log4j2LogBackend.class, true);
		checkLog(LogBackendType.LOG4J, Log4jLogBackend.class, true);
		checkLog(LogBackendType.LOCAL, LocalLogBackend.class, true);
		checkLog(LogBackendType.JAVA_UTIL, JavaUtilLogBackend.class, true);
		checkLog(LogBackendType.NULL, NullLogBackend.class, false);
	}

	@Test
	public void testLogTypeKnownLog() {
		LogBackend backend = LogBackendType.LOCAL.createLogBackend(getClass().getName());
		assertTrue(backend instanceof LocalLogBackend);
	}

	@Test
	public void testGetSimpleClassName() {
		String first = "foo";
		assertEquals(first, LoggerFactory.getSimpleClassName(first));
		String second = "bar";
		String className = first + "." + second;
		assertEquals(second, LoggerFactory.getSimpleClassName(className));
		className = first + ".";
		assertEquals(className, LoggerFactory.getSimpleClassName(className));
	}

	@Test
	public void testLogTypeProperty() {
		LogBackendFactory factory = LoggerFactory.getLogBackendFactory();
		try {
			LoggerFactory.setLogBackendFactory(null);
			System.setProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY, LogBackendType.NULL.name());
			LoggerFactory.getLogger("foo");
			assertEquals(LogBackendType.NULL, LoggerFactory.getLogBackendFactory());
		} finally {
			LoggerFactory.setLogBackendFactory(factory);
			System.clearProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY);
		}
	}

	@Test
	public void testSetLogFactory() {
		OurLogFactory ourLogFactory = new OurLogFactory();
		LogBackend log = createMock(LogBackend.class);
		ourLogFactory.log = log;
		LoggerFactory.setLogBackendFactory(ourLogFactory);

		String message = "hello";
		expect(log.isLevelEnabled(Level.INFO)).andReturn(true);
		log.log(Level.INFO, message);

		replay(log);
		Logger logger = LoggerFactory.getLogger("test");
		logger.info(message);
		verify(log);
	}

	@Test
	public void testLogFactoryProperty() {
		LoggerFactory.setLogBackendFactory(null);
		System.setProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY, "some.wrong.class");
		try {
			// this should work and not throw
			LoggerFactory.getLogger(getClass());
		} finally {
			System.clearProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY);
		}
	}

	@Test
	public void testLogFactoryFind() {
		LoggerFactory.setLogBackendFactory(null);
		System.clearProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY);
		// this should work and not throw
		LoggerFactory.getLogger(getClass());
	}

	private void checkLog(LogBackendType logType, Class<?> logClass, boolean available) {
		assertEquals(logType + " available should be " + available, available, logType.isAvailable());
		LogBackend backend = logType.createLogBackend(getClass().getName());
		assertNotNull(logType + " should not general null log", backend);
		assertEquals(logClass, backend.getClass());
	}

	private static class OurLogFactory implements LogBackendFactory {

		LogBackend log;

		@Override
		public LogBackend createLogBackend(String classLabel) {
			return log;
		}
	}
}
