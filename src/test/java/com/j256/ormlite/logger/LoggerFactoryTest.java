package com.j256.ormlite.logger;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Constructor;

import org.junit.Test;

import com.j256.ormlite.logger.Log.Level;
import com.j256.ormlite.logger.LoggerFactory.LogFactory;

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
	public void testLogTypeIsAvailable() {
		assertFalse(LoggerFactory.LogType.ANDROID.isAvailable());
		assertTrue(LoggerFactory.LogType.COMMONS_LOGGING.isAvailable());
		assertTrue(LoggerFactory.LogType.LOG4J.isAvailable());
		assertTrue(LoggerFactory.LogType.LOG4J2.isAvailable());
		assertTrue(LoggerFactory.LogType.LOCAL.isAvailable());
		assertTrue(LoggerFactory.LogType.LOCAL.isAvailableTestClass());
	}

	@Test
	public void testLogTypeUnknownLog() {
		Log log = LoggerFactory.LogType.ANDROID.createLog(getClass().getName());
		assertTrue(log instanceof LocalLog);
		log = LoggerFactory.LogType.ANDROID.createLog(getClass().getName());
		assertTrue(log instanceof LocalLog);
		log = LoggerFactory.LogType.COMMONS_LOGGING.createLog(getClass().getName());
		assertTrue(log instanceof CommonsLoggingLog);
		log = LoggerFactory.LogType.LOG4J.createLog(getClass().getName());
		assertTrue(log instanceof Log4jLog);
		log = LoggerFactory.LogType.LOG4J2.createLog(getClass().getName());
		assertTrue(log instanceof Log4j2Log);
		log = LoggerFactory.LogType.LOCAL.createLog(getClass().getName());
		assertTrue(log instanceof LocalLog);
		log = LoggerFactory.LogType.LOCAL.createLog(getClass().getName());
		assertTrue(log instanceof LocalLog);
	}

	@Test
	public void testLogTypeKnownLog() {
		Log log = LoggerFactory.LogType.LOCAL.createLog(getClass().getName());
		assertTrue(log instanceof LocalLog);
	}

	@Test
	public void testGetSimpleClassName() {
		String first = "foo";
		String name = LoggerFactory.getSimpleClassName(first);
		assertEquals(first, name);
		String second = "bar";
		String className = first + "." + second;
		name = LoggerFactory.getSimpleClassName(className);
		assertEquals(second, name);
	}

	@Test
	public void testSetLogFactory() {
		OurLogFactory ourLogFactory = new OurLogFactory();
		Log log = createMock(Log.class);
		ourLogFactory.log = log;
		LoggerFactory.setLogFactory(ourLogFactory);

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
		LoggerFactory.setLogFactory(null);
		String logTypeProp = System.getProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY);
		System.setProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY, "some.wrong.class");
		try {
			// this should work and not throw
			LoggerFactory.getLogger(getClass());
		} finally {
			if (logTypeProp == null) {
				System.clearProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY);
			} else {
				System.setProperty(LoggerFactory.LOG_TYPE_SYSTEM_PROPERTY, logTypeProp);
			}
		}
	}

	private static class OurLogFactory implements LogFactory {

		Log log;

		@Override
		public Log createLog(String classLabel) {
			return log;
		}
	}
}
