package com.j256.ormlite.logger;

import java.util.Arrays;

import com.j256.ormlite.logger.backend.NullLogBackend.NullLogBackendFactory;

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

	{
		PropertyUtils.assignGlobalLevelFromProperty(LogBackendType.NULL);
		// system property overrides property setting
		maybeAssignGlobalLogLevelFromProperty();
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
	 * Return a fluent logger associated with a particular class.
	 */
	public static FluentLogger getFluentLogger(Class<?> clazz) {
		return getFluentLogger(clazz.getName());
	}

	/**
	 * Return a fluent logger associated with a particular class name.
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
	 * Maybe assign the global log level based on the system property. Exposed for testing purposes.
	 */
	static void maybeAssignGlobalLogLevelFromProperty() {
		if (LoggerConstants.GLOBAL_LOG_LEVEL_SYSTEM_PROPERTY != null) {
			String value = System.getProperty(LoggerConstants.GLOBAL_LOG_LEVEL_SYSTEM_PROPERTY);
			if (value != null) {
				Level level = Level.fromString(value);
				if (level != null) {
					Logger.setGlobalLogLevel(level);
				}
			}
		}
	}

	/**
	 * Return the most appropriate log backend factory. This should _never_ return null. Exposed for testing.
	 */
	static LogBackendFactory findLogBackendFactory() {

		// see if the propertied specify the discovery order
		LogBackendFactory[] discoveryOrder = PropertyUtils.readDiscoveryOrderProperty(LogBackendType.NULL);
		// system property overrides property setting
		if (LoggerConstants.BACKEND_DISCOVERY_ORDER_SYSTEM_PROPERTY != null) {
			String value = System.getProperty(LoggerConstants.BACKEND_DISCOVERY_ORDER_SYSTEM_PROPERTY);
			LogBackendType[] order =
					PropertyUtils.processDiscoveryOrderValue(value, NullLogBackendFactory.getSingleton());
			if (order != null) {
				discoveryOrder = order;
			}
		}
		if (discoveryOrder == null) {
			discoveryOrder = LoggerConstants.DEFAULT_BACKEND_DISCOVERY_ORDER;
		}

		LogBackendFactory defaultBackendFactory = chooseDefaultBackendFactory(discoveryOrder);

		// see if the log-type was specified as a system property
		String logTypeString = System.getProperty(LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY);
		LogBackendFactory backend = constructFactoryFromProperty(defaultBackendFactory,
				"system property '" + LoggerConstants.LOG_BACKEND_SYSTEM_PROPERTY + "'", logTypeString);
		if (backend != null) {
			return backend;
		}

		// load in the log-type property from the config-file
		logTypeString = PropertyUtils.readBackendTypeClassProperty(defaultBackendFactory);
		backend = constructFactoryFromProperty(defaultBackendFactory,
				"config-file property '" + PropertyUtils.BACKEND_TYPE_CLASS_PROPERTY + "'", logTypeString);
		if (backend != null) {
			return backend;
		}

		return defaultBackendFactory;
	}

	private static LogBackendFactory constructFactoryFromProperty(LogBackendFactory defaultBackendFactory, String label,
			String value) {
		if (value == null) {
			return null;
		}
		try {
			// first we see if the log-type is an enum value
			return LogBackendType.valueOf(value);
		} catch (IllegalArgumentException iae) {
			// next we see if it is factory class
			LogBackendFactory factory = constructFactoryFromClassName(defaultBackendFactory, label, value);
			if (factory != null) {
				return factory;
			}
			LogBackend backend = defaultBackendFactory.createLogBackend(LoggerFactory.class.getName());
			backend.log(Level.WARNING,
					"Could not find valid log-type from " + label + ", value '" + value + "' not one of "
							+ Arrays.toString(LogBackendType.values())
							+ " nor a class name that implements LogBackendFactory");
			return null;
		}
	}

	/**
	 * See if the log-type-name is a class name of a factory. If so then construct it and return it.
	 */
	private static LogBackendFactory constructFactoryFromClassName(LogBackendFactory defaultBackendFactory,
			String label, String logTypeString) {
		// next we see if it is factory class
		Class<?> clazz;
		try {
			clazz = Class.forName(logTypeString);
		} catch (ClassNotFoundException cnfe) {
			// probably not a class name so ignore the exception
			return null;
		}

		if (!LogBackendFactory.class.isAssignableFrom(clazz)) {
			LogBackend backend = defaultBackendFactory.createLogBackend(LoggerFactory.class.getName());
			backend.log(Level.WARNING, "Was expecting the name of a class that implements LogBackendFactory from "
					+ label + ", value '" + logTypeString + "'");
			return null;
		}

		try {
			// construct the factory by calling the no-arg contructor
			Object instance = clazz.newInstance();
			return (LogBackendFactory) instance;
		} catch (Exception e) {
			LogBackend backend = defaultBackendFactory.createLogBackend(LoggerFactory.class.getName());
			backend.log(Level.WARNING,
					"Could not construct an instance of class from " + label + ", value '" + logTypeString + "'", e);
			return null;
		}
	}

	private static LogBackendFactory chooseDefaultBackendFactory(LogBackendFactory[] discoveryOrder) {
		for (LogBackendFactory logType : discoveryOrder) {
			if (logType.isAvailable()) {
				return logType;
			}
		}
		// fall back is always LOCAL
		return LogBackendType.LOCAL;
	}
}
