package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;

/**
 * Log backend that delegates to Apache Log4j.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class Log4jLogBackend implements LogBackend {

	private final org.apache.log4j.Logger logger;

	public Log4jLogBackend(String className) {
		this.logger = org.apache.log4j.Logger.getLogger(className);
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		return logger.isEnabledFor(levelToLog4jLevel(level));
	}

	@Override
	public void log(Level level, String msg) {
		logger.log(levelToLog4jLevel(level), msg);
	}

	@Override
	public void log(Level level, String msg, Throwable t) {
		logger.log(levelToLog4jLevel(level), msg, t);
	}

	private org.apache.log4j.Level levelToLog4jLevel(Level level) {
		switch (level) {
			case TRACE:
				return org.apache.log4j.Level.TRACE;
			case DEBUG:
				return org.apache.log4j.Level.DEBUG;
			/* INFO below */
			case WARNING:
				return org.apache.log4j.Level.WARN;
			case ERROR:
				return org.apache.log4j.Level.ERROR;
			case FATAL:
				return org.apache.log4j.Level.FATAL;
			case INFO:
			default:
				return org.apache.log4j.Level.INFO;
		}
	}

	/**
	 * Factory for generating Log4jLogBackend instances.
	 */
	public static class Log4jLogBackendFactory implements LogBackendFactory {
		@Override
		public LogBackend createLogBackend(String classLabel) {
			return new Log4jLogBackend(classLabel);
		}
	}
}
