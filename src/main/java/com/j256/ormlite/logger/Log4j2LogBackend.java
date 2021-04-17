package com.j256.ormlite.logger;

/**
 * Log backend that delegates to Apache Log4j2.
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
		@Override
		public LogBackend createLogBackend(String classLabel) {
			return new Log4j2LogBackend(classLabel);
		}
	}
}
