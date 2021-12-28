package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;

/**
 * Log backend that delegating to the Apache commons logging classes.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class CommonsLoggingLogBackend implements LogBackend {

	private final org.apache.commons.logging.Log log;

	public CommonsLoggingLogBackend(String className) {
		this.log = org.apache.commons.logging.LogFactory.getLog(className);
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		switch (level) {
			case TRACE:
				return log.isTraceEnabled();
			case DEBUG:
				return log.isDebugEnabled();
			/* INFO below */
			case WARNING:
				return log.isWarnEnabled();
			case ERROR:
				return log.isErrorEnabled();
			case FATAL:
				return log.isFatalEnabled();
			case INFO:
			default:
				return log.isInfoEnabled();
		}
	}

	@Override
	public void log(Level level, String msg) {
		switch (level) {
			case TRACE:
				log.trace(msg);
				break;
			case DEBUG:
				log.debug(msg);
				break;
			/* INFO below */
			case WARNING:
				log.warn(msg);
				break;
			case ERROR:
				log.error(msg);
				break;
			case FATAL:
				log.fatal(msg);
				break;
			case INFO:
			default:
				log.info(msg);
				break;
		}
	}

	@Override
	public void log(Level level, String msg, Throwable t) {
		switch (level) {
			case TRACE:
				log.trace(msg, t);
				break;
			case DEBUG:
				log.debug(msg, t);
				break;
			/* INFO below */
			case WARNING:
				log.warn(msg, t);
				break;
			case ERROR:
				log.error(msg, t);
				break;
			case FATAL:
				log.fatal(msg, t);
				break;
			case INFO:
			default:
				log.info(msg, t);
				break;
		}
	}

	/**
	 * Factory for generating CommonsLoggingLogBackend instances.
	 */
	public static class CommonsLoggingLogBackendFactory implements LogBackendFactory {
		@Override
		public LogBackend createLogBackend(String classLabel) {
			return new CommonsLoggingLogBackend(classLabel);
		}
	}
}
