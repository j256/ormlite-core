package com.j256.ormlite.logger;

import java.util.Arrays;

/**
 * Factory that creates {@link Logger} and {@link FluentLogger} instances. It uses reflection to see what logging
 * backends are available on the classpath and tries to find the most appropriate one.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * <p>
 * To set the logger to a particular type, set the system property ("com.j256.simplelogger.backend") contained in to be
 * name of one of the enumerated types in {@link LogBackendType}. You can also call
 * {@link LoggerFactory#setLogBackendType(LogBackendType)} or
 * {@link LoggerFactory#setLogBackendFactory(LogBackendFactory)} if you want to set it to a particular class which can
 * be a custom backend.
 * </p>
 */
public class LoggerFactory {

	private static LogBackendFactory logBackendFactory;

	private LoggerFactory() {
		// only here for static usage
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
	 * Return a logger associated with a particular class.
	 */
	public static FluentLogger getFluentLogger(Class<?> clazz) {
		return getFluentLogger(clazz.getName());
	}

	/**
	 * Return a logger associated with a particular class name.
	 */
	public static FluentLogger getFluentLogger(String className) {
		if (logBackendFactory == null) {
			logBackendFactory = findLogBackendFactory();
		}
		return new FluentLogger(logBackendFactory.createLogBackend(className));
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
	 *             If the logging type is not available, most likely because classes are missing from the classpath.
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

		LogBackendType defaultBackend = chooseDefaultBackend();

		// see if the log-type was specified as a system property
		String logTypeString = System.getProperty(LoggerConstants.LOG_TYPE_SYSTEM_PROPERTY);
		if (logTypeString != null) {
			try {
				// first we see if the log-type is an enum value
				return LogBackendType.valueOf(logTypeString);
			} catch (IllegalArgumentException iae) {
				// next we see if it is factory class
				LogBackendFactory factory = constructFactoryFromClassName(defaultBackend, logTypeString);
				if (factory != null) {
					return factory;
				}
				LogBackend backend = defaultBackend.createLogBackend(LoggerFactory.class.getName());
				backend.log(Level.WARNING,
						"Could not find valid log-type from system property '"
								+ LoggerConstants.LOG_TYPE_SYSTEM_PROPERTY + "', value '" + logTypeString
								+ "' not one of " + Arrays.toString(LogBackendType.values())
								+ " nor a class name that implements LogBackendFactory");
			}
		}

		return defaultBackend;
	}

	/**
	 * See if the log-type-name is a class name of a factory. If so then construct it and return it.
	 */
	private static LogBackendFactory constructFactoryFromClassName(LogBackendType defaultBackend,
			String logTypeString) {
		// next we see if it is factory class
		Class<?> clazz;
		try {
			clazz = Class.forName(logTypeString);
		} catch (ClassNotFoundException cnfe) {
			// probably not a class name so ignore the exception
			return null;
		}

		if (!LogBackendFactory.class.isAssignableFrom(clazz)) {
			LogBackend backend = defaultBackend.createLogBackend(LoggerFactory.class.getName());
			backend.log(Level.WARNING,
					"Was expecting the name of a class that implements LogBackendFactory from " + "system property '"
							+ LoggerConstants.LOG_TYPE_SYSTEM_PROPERTY + "', value '" + logTypeString + "'");
			return null;
		}

		try {
			// construct the factory by calling the no-arg contructor
			Object instance = clazz.newInstance();
			return (LogBackendFactory) instance;
		} catch (Exception e) {
			LogBackend backend = defaultBackend.createLogBackend(LoggerFactory.class.getName());
			backend.log(Level.WARNING, "Could not construct an instance of class from system property '"
					+ LoggerConstants.LOG_TYPE_SYSTEM_PROPERTY + "', value '" + logTypeString + "'", e);
			return null;
		}
	}

	private static LogBackendType chooseDefaultBackend() {
		for (LogBackendType logType : LogBackendType.values()) {
			if (logType.isAvailable()) {
				return logType;
			}
		}
		// fall back is always LOCAL, probably never reached because local is in the list above
		return LogBackendType.LOCAL;
	}
}
