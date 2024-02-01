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
	 * System property used to set the logger backend. Can be one of the values of {@link LogBackendType} (such as
	 * "LOGBACK") or a class name that implements {@link LogBackendFactory} (such as
	 * "com.j256.simplelogging.backend.LogbackLogBackend$LogbackLogBackendFactory").
	 */
	public static final String LOG_TYPE_SYSTEM_PROPERTY = "com.j256.simplelogger.backend";

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

	/**
	 * It also supports a file simpleLoggingLocalLog.properties file which contains lines such as:
	 * 
	 * <pre>
	 * # regex-pattern = Level
	 * com\.foo\.yourclass.*=DEBUG
	 * com\.foo\.yourclass\.BaseMappedStatement=TRACE
	 * com\.foo\.yourclass\.MappedCreate=TRACE
	 * com\.foo\.yourclass\.StatementExecutor=TRACE
	 * </pre>
	 */
	public static final String LOCAL_LOG_PROPERTIES_FILE = "/ormliteLocalLog.properties";

	private LoggerConstants() {
		// only here for static usage
	}
}
