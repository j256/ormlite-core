package com.j256.ormlite.logger.backend;

import java.lang.reflect.Method;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;

/**
 * Log backend that delegates to Apache Log4j through reflection so there isn't a direct dependency it will only work if
 * log4j is already on the4 classpath.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class Log4jLogBackend implements LogBackend {

	private final static Class<?> LOGGER_CLASS;
	private final static Class<?> LEVEL_CLASS;
	private final static Class<?> PRIORITY_CLASS;
	private final static Method GET_LOGGER_METHOD;
	private final static Method IS_ENABLED_FOR_METHOD;
	private final static Method LOG_LEVEL_MESSAGE_METHOD;
	private final static Method LOG_LEVEL_MESSAGE_THROWABLE_METHOD;
	private static Object TRACE_LEVEL;
	private static Object DEBUG_LEVEL;
	private static Object INFO_LEVEL;
	private static Object WARN_LEVEL;
	private static Object ERROR_LEVEL;
	private static Object FATAL_LEVEL;

	private final Object logger;

	static {
		try {
			LOGGER_CLASS = Class.forName("org.apache.log4j.Logger");
			LEVEL_CLASS = Class.forName("org.apache.log4j.Level");
			PRIORITY_CLASS = Class.forName("org.apache.log4j.Priority");
		} catch (Exception e) {
			throw new RuntimeException("Problems finding log4j v1 classes via reflection", e);
		}

		try {
			GET_LOGGER_METHOD = LOGGER_CLASS.getMethod("getLogger", String.class);
			IS_ENABLED_FOR_METHOD = LOGGER_CLASS.getMethod("isEnabledFor", PRIORITY_CLASS);
			LOG_LEVEL_MESSAGE_METHOD = LOGGER_CLASS.getMethod("log", PRIORITY_CLASS, Object.class);
			LOG_LEVEL_MESSAGE_THROWABLE_METHOD =
					LOGGER_CLASS.getMethod("log", PRIORITY_CLASS, Object.class, Throwable.class);
		} catch (Exception e) {
			throw new RuntimeException("Problems finding log4j v1 methods via reflection", e);
		}

		try {
			// get our static fields
			TRACE_LEVEL = LEVEL_CLASS.getDeclaredField("TRACE").get(null);
			DEBUG_LEVEL = LEVEL_CLASS.getDeclaredField("DEBUG").get(null);
			INFO_LEVEL = LEVEL_CLASS.getDeclaredField("INFO").get(null);
			WARN_LEVEL = LEVEL_CLASS.getDeclaredField("WARN").get(null);
			ERROR_LEVEL = LEVEL_CLASS.getDeclaredField("ERROR").get(null);
			FATAL_LEVEL = LEVEL_CLASS.getDeclaredField("FATAL").get(null);
		} catch (Exception e) {
			throw new RuntimeException("Problems finding log4j v1 fields via reflection", e);
		}
	}

	public Log4jLogBackend(String className) {
		try {
			this.logger = GET_LOGGER_METHOD.invoke(null, className);
		} catch (Exception e) {
			throw new RuntimeException("Problems creating a log4j v1 instance via reflection", e);
		}
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		try {
			return (Boolean) IS_ENABLED_FOR_METHOD.invoke(logger, levelToLog4jLevel(level));
		} catch (Exception e) {
			// ignored I guess
			return false;
		}
	}

	@Override
	public void log(Level level, String msg) {
		try {
			LOG_LEVEL_MESSAGE_METHOD.invoke(logger, levelToLog4jLevel(level), msg);
		} catch (Exception e) {
			// ignored I guess
		}
	}

	@Override
	public void log(Level level, String msg, Throwable t) {
		try {
			LOG_LEVEL_MESSAGE_THROWABLE_METHOD.invoke(logger, levelToLog4jLevel(level), msg, t);
		} catch (Exception e) {
			// ignored I guess
		}
	}

	private Object levelToLog4jLevel(Level level) {
		switch (level) {
			case TRACE:
				return TRACE_LEVEL;
			case DEBUG:
				return DEBUG_LEVEL;
			/* INFO below */
			case WARNING:
				return WARN_LEVEL;
			case ERROR:
				return ERROR_LEVEL;
			case FATAL:
				return FATAL_LEVEL;
			case INFO:
			default:
				return INFO_LEVEL;
		}
	}

	/**
	 * Factory for generating Log4jLogBackend instances.
	 */
	public static class Log4jLogBackendFactory implements LogBackendFactory {
		
		private final String loggerNamePrefix;
		
		public Log4jLogBackendFactory() {
			this.loggerNamePrefix = null;
		}
		
		public Log4jLogBackendFactory(String loggerNamePrefix) {
			// this is used by the log4j reflection class to show if it is log4j or log4j2
			this.loggerNamePrefix = loggerNamePrefix; 
		}
		
		@Override
		public LogBackend createLogBackend(String classLabel) {
			if (loggerNamePrefix == null) {
				return new Log4jLogBackend(classLabel);
			} else {
				return new Log4jLogBackend(loggerNamePrefix + classLabel);
			}
		}
	}
}
