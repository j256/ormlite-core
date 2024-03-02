package com.j256.ormlite.logger;

/**
 * Class to have one place in case you want to tweak the logging constants for your application.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 * 
 * @author graywatson
 */
public class LoggerConstants {

	/**
	 * The default log level for all loggers being created. If set to null, then there is no default global log setting
	 * and the logger backend will determine what log levels are active. Set to Level.OFF to disable all logging by
	 * default.
	 */
	public static final Level DEFAULT_GLOBAL_LOG_LEVEL = Level.OFF;

	/**
	 * System property used to set the global log level. It can also be set in the properties file and via a call to
	 * {@link Logger#setGlobalLogLevel(Level)}.
	 */
	public static final String GLOBAL_LOG_LEVEL_SYSTEM_PROPERTY = "com.j256.simplelogger.global.level";

	/**
	 * System property used to set the backend discovery order which is the order in which the various backend loggers
	 * will be tried and the first one working will be used by the system. The default order is the order in the enum
	 * {@link LogBackendType}.
	 */
	public static final String BACKEND_DISCOVERY_ORDER_SYSTEM_PROPERTY = "com.j256.simplelogger.dicovery.order";

	/**
	 * Default backend discovery order is the values in order from the {@link LogBackendType} enumerated type. You can
	 * change the order, remove some of the discovery entries, or insert your own custom factory here.
	 */
	public static final LogBackendFactory[] DEFAULT_BACKEND_DISCOVERY_ORDER = LogBackendType.values();

	/**
	 * System property used to set the logger backend. Can be one of the values of {@link LogBackendType} (such as
	 * "LOGBACK") or a class name that implements {@link LogBackendFactory} (such as
	 * "com.j256.simplelogging.backend.LogbackLogBackend$LogbackLogBackendFactory").
	 */
	public static final String LOG_BACKEND_SYSTEM_PROPERTY = "com.j256.simplelogger.backend";

	/**
	 * File path to the properties for the simplelogging library. Lines are in the form field = value. See the
	 * {@link PropertyUtils} for what properties can be set.
	 */
	public static final String PROPERTIES_CONFIG_FILE = "/simplelogging.properties";

	/**
	 * Name of the Android tag that is used with android.util.Log#isLoggable(String, int) to determine if the global
	 * logs are enabled.
	 */
	public static final String ANDROID_ALL_LOGS_NAME = "ORMLite";

	/**
	 * You can set the log level by setting the System.setProperty(LocalLogBackend.LOCAL_LOG_LEVEL_PROPERTY, "trace").
	 * Acceptable values are: TRACE, DEBUG, INFO, WARN, ERROR, and FATAL.
	 */
	public static final String LOCAL_LOG_LEVEL_PROPERTY = "com.j256.simplelogging.level";

	/**
	 * You can also redirect the log to a file by setting the
	 * System.setProperty(LocalLogBackend.LOCAL_LOG_FILE_PROPERTY, "log.out"). Otherwise, log output will go to stdout.
	 */
	public static final String LOCAL_LOG_FILE_PROPERTY = "com.j256.simplelogging.file";

	private LoggerConstants() {
		// only here for static usage
	}
}
