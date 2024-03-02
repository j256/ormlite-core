package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;

/**
 * Log backend that writes to java.util.log. This will never be chosen by default because it should always in the
 * classpath, but can be injected if someone really wants it.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class JavaUtilLogBackend implements LogBackend {

	private final java.util.logging.Logger logger;

	public JavaUtilLogBackend(String className) {
		this.logger = java.util.logging.Logger.getLogger(className);
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		return logger.isLoggable(levelToJavaLevel(level));
	}

	@Override
	public void log(Level level, String msg) {
		logger.log(levelToJavaLevel(level), msg);
	}

	@Override
	public void log(Level level, String msg, Throwable throwable) {
		logger.log(levelToJavaLevel(level), msg, throwable);
	}

	private java.util.logging.Level levelToJavaLevel(Level level) {
		switch (level) {
			case TRACE:
				return java.util.logging.Level.FINER;
			case DEBUG:
				return java.util.logging.Level.FINE;
			/* INFO below */
			case WARNING:
				return java.util.logging.Level.WARNING;
			case ERROR:
				// no ERROR level
				return java.util.logging.Level.SEVERE;
			case FATAL:
				return java.util.logging.Level.SEVERE;
			case INFO:
			default:
				return java.util.logging.Level.INFO;
		}
	}

	/**
	 * Factory for generating JavaUtilLogBackend instances.
	 */
	public static class JavaUtilLogBackendFactory implements LogBackendFactory {
		@Override
		public boolean isAvailable() {
			// probably always available
			return true;
		}

		@Override
		public LogBackend createLogBackend(String classLabel) {
			return new JavaUtilLogBackend(classLabel);
		}
	}
}
