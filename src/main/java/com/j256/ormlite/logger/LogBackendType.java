package com.j256.ormlite.logger;

import com.j256.ormlite.logger.ConsoleLogBackend.ConsoleLogBackendFactory;
import com.j256.ormlite.logger.LocalLogBackend.LocalLogBackendFactory;
import com.j256.ormlite.logger.NullLogBackend.NullLogBackendFactory;

/**
 * Type of logging backends that are supported. The classes are specified as strings so there is not a direct dependency
 * placed on them since these classes may reference types not on the classpath.
 *
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public enum LogBackendType implements LogBackendFactory {
	/**
	 * SLF4J which is often paired with logback. See: http://www.slf4j.org/
	 */
	SLF4J("com.j256.ormlite.logger.Slf4jLoggingLogBackend$Slf4jLoggingLogBackendFactory"),
	/**
	 * Android Log mechanism. See: https://developer.android.com/reference/android/util/Log
	 * 
	 * <p>
	 * WARNING: Android log must be before commons logging since Android provides commons logging but logging messages
	 * are ignored that are sent there. Grrrrr.
	 * </p>
	 */
	ANDROID("com.j256.ormlite.android.AndroidLogBackend$AndroidLogBackendFactory"),
	/**
	 * Logback direct. See: http://logback.qos.ch/
	 */
	LOGBACK("com.j256.ormlite.logger.LogbackLogBackend$LogbackLogBackendFactory"),
	/**
	 * Apache commons logging. See https://commons.apache.org/proper/commons-logging/
	 */
	COMMONS_LOGGING("com.j256.ormlite.logger.CommonsLoggingLogBackend$CommonsLoggingLogBackendFactory"),
	/**
	 * Version 2 of the log4j package. See https://logging.apache.org/log4j/2.x/
	 */
	LOG4J2("com.j256.ormlite.logger.Log4j2LogBackend$Log4j2LogBackendFactory"),
	/**
	 * Old version of the log4j package. See https://logging.apache.org/log4j/2.x/
	 */
	LOG4J("com.j256.ormlite.logger.Log4jLogBackend$Log4jLogBackendFactory"),
	/**
	 * Local simple log backend that writes to a output file.
	 * 
	 * <p>
	 * NOTE: any loggers defined below this will not be auto-detected because this is always available.
	 * </p>
	 */
	LOCAL(new LocalLogBackendFactory()),
	/**
	 * Simple log backend that writes out to System.out or System.err.
	 */
	CONSOLE(new ConsoleLogBackendFactory()),
	/**
	 * Internal JVM logging implementation almost always available. We put this below the LOCAL log because it's always
	 * available but we don't want to auto-detect it. See:
	 * https://docs.oracle.com/javase/7/docs/api/java/util/logging/package-summary.html
	 */
	JAVA_UTIL("com.j256.ormlite.logger.JavaUtilLogBackend$JavaUtilLogBackendFactory"),
	/**
	 * Logging backend which ignores all messages. Used to disable all logging. This is never chosen automatically.
	 */
	NULL(new NullLogBackendFactory()),
	// end
	;

	private final LogBackendFactory factory;

	private LogBackendType(LogBackendFactory factory) {
		this.factory = factory;
	}

	private LogBackendType(String logBackendFactoryClassName) {
		this.factory = detectFactory(logBackendFactoryClassName);
	}

	@Override
	public LogBackend createLogBackend(String classLabel) {
		return factory.createLogBackend(classLabel);
	}

	/**
	 * Return true if the log class is available. This typically is testing to see if a class is available on the
	 * classpath.
	 */
	public boolean isAvailable() {
		/*
		 * If this is LogBackendType.LOCAL then it is always available. LogBackendType.NULL is never available. If it is
		 * another LogBackendType then we might have defaulted to using the local-log backend if it was not available.
		 */
		return (this == LogBackendType.LOCAL
				|| (this != LogBackendType.NULL && !(factory instanceof LocalLogBackendFactory)));
	}

	/**
	 * Try to detect if the logger class is available and if calling the factory to make a logger works.
	 */
	private LogBackendFactory detectFactory(String factoryClassName) {
		try {
			// sometimes the constructor works but it's not fully wired
			LogBackendFactory factory = (LogBackendFactory) Class.forName(factoryClassName).newInstance();
			// we may really need to use the class before we see issues
			factory.createLogBackend("test").isLevelEnabled(Level.INFO);
			return factory;
		} catch (Throwable th) {
			/*
			 * We catch throwable here because we could get linkage errors. We don't immediately report on this issue
			 * because this log factory will most likely never be used. If it is, the first thing that the factory will
			 * so is use the first LogBackend generated to log this warning.
			 */
			String queuedWarning = "Unable to create instance of class " + factoryClassName + " for log type " + this
					+ ", using local log: " + th.getMessage();
			return new LocalLogBackendFactory(queuedWarning);
		}
	}
}
