package com.j256.ormlite.logger;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.logger.backend.CommonsLoggingLogBackend;
import com.j256.ormlite.logger.backend.JavaUtilLogBackend;
import com.j256.ormlite.logger.backend.LocalLogBackend;
import com.j256.ormlite.logger.backend.Log4j2LogBackend;
import com.j256.ormlite.logger.backend.LogbackLogBackend;
import com.j256.ormlite.logger.backend.NullLogBackend;
import com.j256.ormlite.logger.backend.Slf4jLoggingLogBackend;

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
	public void testGetFluentLoggerClass() {
		LoggerFactory.setLogBackendFactory(null);
		assertNotNull(LoggerFactory.getFluentLogger(getClass()));
	}

	@Test
	public void testGetFluentLoggerString() {
		assertNotNull(LoggerFactory.getFluentLogger(getClass().getName()));
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
		Class<LocalLogBackend> backupBackend = LocalLogBackend.class;
		checkLog(LogBackendType.SLF4J, Slf4jLoggingLogBackend.class, true);
		checkLog(LogBackendType.ANDROID, backupBackend, false);
		checkLog(LogBackendType.COMMONS_LOGGING, CommonsLoggingLogBackend.class, true);
		checkLog(LogBackendType.LOG4J2, Log4j2LogBackend.class, true);
		checkLog(LogBackendType.LOG4J, backupBackend, false);
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
			System.setProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY, LogBackendType.NULL.name());
			LoggerFactory.getLogger("foo");
			assertEquals(LogBackendType.NULL, LoggerFactory.getLogBackendFactory());
		} finally {
			LoggerFactory.setLogBackendFactory(factory);
			System.clearProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY);
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
		System.setProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY, "some.wrong.class");
		try {
			// this should work and not throw
			LoggerFactory.getLogger(getClass());
		} finally {
			System.clearProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY);
		}
	}

	@Test
	public void testLogFactoryFind() {
		LoggerFactory.setLogBackendFactory(null);
		System.clearProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY);
		// this should work and not throw
		LoggerFactory.getLogger(getClass());
	}

	@Test
	public void testSetLoggerBackend() {
		try {
			LoggerFactory.setLogBackendType(LogBackendType.LOCAL);
			try {
				LoggerFactory.setLogBackendType(LogBackendType.ANDROID);
			} catch (Exception e) {
				// expected
			}
		} finally {
			LoggerFactory.setLogBackendFactory(null);
		}
	}

	private void checkLog(LogBackendType logType, Class<?> logClass, boolean available) {
		assertEquals(available, logType.isAvailable(), logType + " available should be " + available);
		LogBackend backend = logType.createLogBackend(getClass().getName());
		assertNotNull(backend, logType + " should not general null log");
		assertEquals(logClass, backend.getClass());
	}

	@Test
	public void testLogFactoryAsClass() {
		LoggerFactory.setLogBackendFactory(null);
		try {
			System.setProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY, OurLogFactory.class.getName());
			OurLogFactory.lastClassLabel = null;
			// this should work and not throw
			String label = "fopwejfwejfwe";
			LoggerFactory.getLogger(label);
			assertSame(label, OurLogFactory.lastClassLabel);
		} finally {
			System.clearProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY);
		}
	}

	@Test
	public void testLogFactoryExample() {
		LoggerFactory.setLogBackendFactory(null);
		try {
			System.setProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY,
					LogbackLogBackend.LogbackLogBackendFactory.class.getName());
			// this should work and not throw
			Logger logger = LoggerFactory.getLogger("fopwejfwejfwe");
			assertTrue(logger.getLogBackend() instanceof LogbackLogBackend);
		} finally {
			System.clearProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY);
		}
	}

	@Test
	public void testLogFactoryAsClassPrivateConstructor() {
		LoggerFactory.setLogBackendFactory(null);
		try {
			System.setProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY, OurLogFactoryPrivate.class.getName());
			OurLogFactoryPrivate.lastClassLabel = null;
			// this shouldn't use the factory because constructor not public
			String label = "fopwejfwejfwe";
			LoggerFactory.getLogger(label);
			assertNull(OurLogFactoryPrivate.lastClassLabel);
		} finally {
			System.clearProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY);
		}
	}

	@Test
	public void testLogFactoryAsClassNotLoggerFactoryBackend() {
		LoggerFactory.setLogBackendFactory(null);
		try {
			System.setProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY, Object.class.getName());
			// this shouldn't use the factory because class is not a LoggerFactoryBackend
			LoggerFactory.getLogger("fopwejfwejfwe");
		} finally {
			System.clearProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY);
		}
	}

	@Test
	public void testBackendPropertiesFile() {
		LoggerFactory.setLogBackendFactory(null);
		System.clearProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY);
		try {
			// this shouldn't use the factory because class is not a LoggerFactoryBackend
			Logger logger = LoggerFactory.getLogger("fopwejfwejfwe");
			logger.info("we are using the backend: " + logger.getLogBackend());
		} finally {
			System.clearProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY);
		}
	}

	@Test
	public void testGlobalProperty() {
		String restore = System.getProperty(LoggerConstants.GLOBAL_LOG_LEVEL_SYSTEM_PROPERTY);
		try {
			System.clearProperty(LoggerConstants.GLOBAL_LOG_LEVEL_SYSTEM_PROPERTY);
			Logger.setGlobalLogLevel(null);
			LoggerFactory.maybeAssignGlobalLogLevelFromProperty();
			assertNull(Logger.getGlobalLevel());
			Level level = Level.DEBUG;
			System.setProperty(LoggerConstants.GLOBAL_LOG_LEVEL_SYSTEM_PROPERTY, level.name());
			LoggerFactory.maybeAssignGlobalLogLevelFromProperty();
			assertEquals(level, Logger.getGlobalLevel());
			System.setProperty(LoggerConstants.GLOBAL_LOG_LEVEL_SYSTEM_PROPERTY, "unknown");
			LoggerFactory.maybeAssignGlobalLogLevelFromProperty();
			assertEquals(level, Logger.getGlobalLevel());
		} finally {
			Logger.setGlobalLogLevel(null);
			if (restore == null) {
				System.clearProperty(LoggerConstants.GLOBAL_LOG_LEVEL_SYSTEM_PROPERTY);
			} else {
				System.setProperty(LoggerConstants.GLOBAL_LOG_LEVEL_SYSTEM_PROPERTY, restore);
			}
		}
	}

	public static class OurLogFactory implements LogBackendFactory {

		LogBackend log;
		static String lastClassLabel;

		@Override
		public boolean isAvailable() {
			return true;
		}

		@Override
		public LogBackend createLogBackend(String classLabel) {
			OurLogFactory.lastClassLabel = classLabel;
			return log;
		}
	}

	private static class OurLogFactoryPrivate implements LogBackendFactory {

		LogBackend log;
		static String lastClassLabel;

		@Override
		public boolean isAvailable() {
			return true;
		}

		@Override
		public LogBackend createLogBackend(String classLabel) {
			OurLogFactory.lastClassLabel = classLabel;
			return log;
		}
	}
}
