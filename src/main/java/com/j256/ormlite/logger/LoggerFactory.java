package com.j256.ormlite.logger;

import java.lang.reflect.Constructor;

import com.j256.ormlite.logger.Log.Level;

/**
 * Factory that creates {@link Logger} instances.
 */
public class LoggerFactory {

	private static LogType logType;

	/**
	 * For static calls only.
	 */
	private LoggerFactory() {
	}

	/**
	 * Return a logger associated with a particular class.
	 */
	public static Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

	/**
	 * Return a logger associated with a particular class name.
	 */
	public static Logger getLogger(String className) {
		if (logType == null) {
			logType = findLogType();
		}
		return new Logger(logType.createLog(className));
	}

	public static String getSimpleClassName(String className) {
		// get the last part of the class name
		String[] parts = className.split("\\.");
		if (parts.length <= 1) {
			return className;
		} else {
			return parts[parts.length - 1];
		}
	}

	/**
	 * Return the most appropriate log type. This should _never_ return null.
	 */
	private static LogType findLogType() {
		for (LogType logType : LogType.values()) {
			if (logType.isAvailable()) {
				return logType;
			}
		}
		// fall back is always LOCAL, never reached
		return LogType.LOCAL;
	}

	/**
	 * Type of internal logs supported. This is package permissions for testing.
	 */
	enum LogType {
		/**
		 * WARNING: Android log must be _before_ commons logging since Android provides commons logging but logging
		 * messages are ignored that are sent there. Grrrrr.
		 */
		ANDROID("android.util.Log", "com.j256.ormlite.android.AndroidLog"),
		COMMONS_LOGGING("org.apache.commons.logging.LogFactory", "com.j256.ormlite.logger.CommonsLoggingLog"),
		LOG4J("org.apache.log4j.Logger", "com.j256.ormlite.logger.Log4jLog"),
		// this should always be at the end
		LOCAL("com.j256.ormlite.logger.LocalLog", "com.j256.ormlite.logger.LocalLog") {
			@Override
			public Log createLog(String classLabel) {
				return new LocalLog(classLabel);
			}
			@Override
			public boolean isAvailable() {
				// always available
				return true;
			}
		},
		// end
		;

		private final String detectClassName;
		private final String logClassName;

		private LogType(String detectClassName, String logClassName) {
			this.detectClassName = detectClassName;
			this.logClassName = logClassName;
		}

		/**
		 * Create and return a Log class for this type.
		 */
		public Log createLog(String classLabel) {
			return createLogFromClassName(classLabel);
		}

		/**
		 * We do this for testing purposes.
		 */
		Log createLogFromClassName(String classLabel) {
			try {
				Class<?> clazz = Class.forName(logClassName);
				@SuppressWarnings("unchecked")
				Constructor<Log> constructor = (Constructor<Log>) clazz.getConstructor(String.class);
				return constructor.newInstance(classLabel);
			} catch (Exception e) {
				// oh well, fallback to the local log
				Log log = new LocalLog(classLabel);
				log.log(Level.WARNING, "Unable to call constructor for class " + logClassName
						+ ", so had to use local log", e);
				return log;
			}
		}

		/**
		 * Return true if the log class is available.
		 */
		public boolean isAvailable() {
			return isAvailableTestClass();
		}

		/**
		 * We do this for testing purposes.
		 */
		boolean isAvailableTestClass() {
			try {
				Class.forName(detectClassName);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
	}
}
