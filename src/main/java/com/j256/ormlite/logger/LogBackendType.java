package com.j256.ormlite.logger;

import com.j256.ormlite.logger.backend.ConsoleLogBackend.ConsoleLogBackendFactory;
import com.j256.ormlite.logger.backend.LocalLogBackend.LocalLogBackendFactory;
import com.j256.ormlite.logger.backend.NullLogBackend.NullLogBackendFactory;

/**
 * Default logging backends that are supported. The class names are specified as strings in the constructor so there is
 * not a direct dependency placed on them since these classes may reference types not on the classpath.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public enum LogBackendType implements LogBackendFactory {
	/**
	 * SLF4J which is often paired with logback. See: http://www.slf4j.org/
	 */
	SLF4J("Slf4jLoggingLogBackend$Slf4jLoggingLogBackendFactory"),
	/**
	 * Android Log mechanism. See: https://developer.android.com/reference/android/util/Log
	 * 
	 * <p>
	 * WARNING: Android log must be before commons logging since Android provides commons logging but logging messages
	 * are ignored that are sent there. Grrrrr.
	 * </p>
	 */
	ANDROID("AndroidLogBackend$AndroidLogBackendFactory"),
	/**
	 * Logback direct. See: http://logback.qos.ch/
	 */
	LOGBACK("LogbackLogBackend$LogbackLogBackendFactory"),
	/**
	 * Version 2 of the log4j package. See https://logging.apache.org/log4j/2.x/
	 */
	LOG4J2("Log4j2LogBackend$Log4j2LogBackendFactory"),
	/**
	 * Old version of the log4j package. See https://logging.apache.org/log4j/2.x/
	 */
	LOG4J("Log4jLogBackend$Log4jLogBackendFactory"),
	/**
	 * Support for the logger available inside AWS lambda SDK.
	 */
	LAMBDA("LambdaLoggerLogBackend$LambdaLoggerLogBackendFactory"),
	/**
	 * Apache commons logging. See https://commons.apache.org/proper/commons-logging/
	 */
	COMMONS_LOGGING("CommonsLoggingLogBackend$CommonsLoggingLogBackendFactory"),
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
	JAVA_UTIL("JavaUtilLogBackend$JavaUtilLogBackendFactory"),
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

	private LogBackendType(String factoryClassName) {
		if (factoryClassName.contains(".")) {
			// NOTE: may not get here but others could add full class names to this list
			this.factory = detectFactory(factoryClassName);
		} else {
			// the name is a suffix and we tack on the package from the local log factory
			this.factory = detectFactory(LocalLogBackendFactory.class.getPackage().getName() + '.' + factoryClassName);
		}
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
					+ ", using local log: " + th;
			return new LocalLogBackendFactory(queuedWarning);
		}
	}
}
