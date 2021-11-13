package com.j256.ormlite.logger;

import org.slf4j.ILoggerFactory;

/**
 * Log backend that delegates to slf4j.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class Slf4jLoggingLogBackend implements LogBackend {

	private final org.slf4j.Logger logger;

	public Slf4jLoggingLogBackend(org.slf4j.Logger logger) {
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
	 * Factory for generating Slf4jLoggingLogBackend instances.
	 */
	public static class Slf4jLoggingLogBackendFactory implements LogBackendFactory {

		private final ILoggerFactory factory;

		public Slf4jLoggingLogBackendFactory() {
			this.factory = org.slf4j.LoggerFactory.getILoggerFactory();
		}

		@Override
		public LogBackend createLogBackend(String classLabel) {
			return new Slf4jLoggingLogBackend(factory.getLogger(classLabel));
		}
	}
}
