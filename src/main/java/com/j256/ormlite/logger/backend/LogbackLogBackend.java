package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;

/**
 * Log backend that delegates to logback directly. The org.slf4j classes are part of the slf4j-api but not the actual
 * logger.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class LogbackLogBackend implements LogBackend {

	private final org.slf4j.Logger logger;

	public LogbackLogBackend(org.slf4j.Logger logger) {
		this.logger = logger;
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		switch (level) {
			case TRACE:
				return logger.isTraceEnabled();
			case DEBUG:
				return logger.isDebugEnabled();
			/* INFO below */
			case WARNING:
				return logger.isWarnEnabled();
			case ERROR:
				return logger.isErrorEnabled();
			case FATAL:
				return logger.isErrorEnabled();
			case INFO:
			default:
				return logger.isInfoEnabled();
		}
	}

	@Override
	public void log(Level level, String msg) {
		switch (level) {
			case TRACE:
				logger.trace(msg);
				break;
			case DEBUG:
				logger.debug(msg);
				break;
			/* INFO below */
			case WARNING:
				logger.warn(msg);
				break;
			case ERROR:
				logger.error(msg);
				break;
			case FATAL:
				logger.error(msg);
				break;
			case INFO:
			default:
				logger.info(msg);
				break;
		}
	}

	@Override
	public void log(Level level, String msg, Throwable t) {
		switch (level) {
			case TRACE:
				logger.trace(msg, t);
				break;
			case DEBUG:
				logger.debug(msg, t);
				break;
			/* INFO below */
			case WARNING:
				logger.warn(msg, t);
				break;
			case ERROR:
				logger.error(msg, t);
				break;
			case FATAL:
				// no level higher than error
				logger.error(msg, t);
				break;
			case INFO:
			default:
				logger.info(msg, t);
				break;
		}
	}

	/**
	 * Factory for generating LogbackLogBackend instances.
	 */
	public static class LogbackLogBackendFactory implements LogBackendFactory {

		private final org.slf4j.ILoggerFactory factory;

		public LogbackLogBackendFactory() {
			this.factory = org.slf4j.impl.StaticLoggerBinder.getSingleton().getLoggerFactory();
		}

		@Override
		public boolean isAvailable() {
			// if we were able to load the classes here then it is available.
			return true;
		}

		@Override
		public LogBackend createLogBackend(String classLabel) {
			return new LogbackLogBackend(factory.getLogger(classLabel));
		}
	}
}
