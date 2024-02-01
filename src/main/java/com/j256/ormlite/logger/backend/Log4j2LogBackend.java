package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;

/**
 * Log backend that delegates to Apache Log4j2.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class Log4j2LogBackend implements LogBackend {

	private final org.apache.logging.log4j.Logger logger;

	public Log4j2LogBackend(String className) {
		this.logger = org.apache.logging.log4j.LogManager.getLogger(className);
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
				return logger.isFatalEnabled();
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
				logger.fatal(msg);
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
				logger.fatal(msg, t);
				break;
			case INFO:
			default:
				logger.info(msg, t);
				break;
		}
	}

	/**
	 * Factory for generating Log4j2LogBackend instances.
	 */
	public static class Log4j2LogBackendFactory implements LogBackendFactory {

		private final String loggerNamePrefix;

		public Log4j2LogBackendFactory() {
			this.loggerNamePrefix = null;
		}

		public Log4j2LogBackendFactory(String loggerNamePrefix) {
			// this is used by the log4j reflection class to show if it is log4j or log4j2
			this.loggerNamePrefix = loggerNamePrefix;
		}

		@Override
		public LogBackend createLogBackend(String classLabel) {
			if (loggerNamePrefix == null) {
				return new Log4j2LogBackend(classLabel);
			} else {
				return new Log4j2LogBackend(loggerNamePrefix + classLabel);
			}
		}
	}
}
