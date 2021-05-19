package com.j256.ormlite.logger;

/**
 * Factory that creates {@link Logger} instances. It uses reflection to see what logging backends are available on the
 * classpath and tries to find the most appropriate one.
 * 
 * <p>
 * To set the logger to a particular type, set the system property ("com.j256.simplelogger.backend") contained in
 * {@link #LOG_TYPE_SYSTEM_PROPERTY} to be name of one of the enumerated types in {@link LogBackendType}.
 * </p>
 */
public class LoggerFactory {

	public static final String LOG_TYPE_SYSTEM_PROPERTY = "com.j256.simplelogger.backend";

	private static LogBackendFactory logBackendFactory;

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
		if (logBackendFactory == null) {
			logBackendFactory = findLogBackendFactory();
		}
		return new Logger(logBackendFactory.createLogBackend(className));
	}

	/**
	 * Get the currently assigned log factory or null if none.
	 */
	public static LogBackendFactory getLogBackendFactory() {
		return LoggerFactory.logBackendFactory;
	}

	/**
	 * Set the log backend factory to be a specific instance. This allows you to easily redirect log messages to your
	 * own {@link LogBackendFactory} implementation.
	 */
	public static void setLogBackendFactory(LogBackendFactory LogBackendFactory) {
		LoggerFactory.logBackendFactory = LogBackendFactory;
	}

	/**
	 * Set the log backend type to be a specific enum type. This will throw an exception if the classes involved with
	 * the type are not available from the classpath.
	 * 
	 * @throws IllegalArgumentException
	 *             If the logging type is not available most likely because classes are missing from the classpath.
	 */
	public static void setLogBackendType(LogBackendType type) {
		if (type.isAvailable()) {
			LoggerFactory.logBackendFactory = type;
		} else {
			throw new IllegalArgumentException("Logging backend type " + type + " is not available on the classpath");
		}
	}

	/**
	 * Return the single class name from a class-name string.
	 */
	public static String getSimpleClassName(String className) {
		// get the last part of the class name
		int index = className.lastIndexOf('.');
		if (index < 0 || index == className.length() - 1) {
			return className;
		} else {
			return className.substring(index + 1);
		}
	}

	/**
	 * Return the most appropriate log backend factory. This should _never_ return null.
	 */
	private static LogBackendFactory findLogBackendFactory() {

		// see if the log-type was specified as a system property
		String logTypeString = System.getProperty(LOG_TYPE_SYSTEM_PROPERTY);
		if (logTypeString != null) {
			try {
				return LogBackendType.valueOf(logTypeString);
			} catch (IllegalArgumentException e) {
				LogBackend backend = new LocalLogBackend(LoggerFactory.class.getName());
				backend.log(Level.WARNING, "Could not find valid log-type from system property '"
						+ LOG_TYPE_SYSTEM_PROPERTY + "', value '" + logTypeString + "'");
			}
		}

		for (LogBackendType logType : LogBackendType.values()) {
			if (logType.isAvailable()) {
				return logType;
			}
		}
		// fall back is always LOCAL, probably never reached
		return LogBackendType.LOCAL;
	}
}
