package com.j256.ormlite.logger;

/**
 * Class which implements our {@link com.j256.ormlite.logger.Log} interface by delegating to Apache Log4j2.
 * 
 * @author graywatson
 */
public class Log4j2Log implements Log {

	private final org.apache.logging.log4j.Logger logger;

	public Log4j2Log(String className) {
		this.logger = org.apache.logging.log4j.LogManager.getLogger(className);
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		switch (level) {
			case TRACE:
				return logger.isTraceEnabled();
			case DEBUG:
				return logger.isDebugEnabled();
			case INFO:
				return logger.isInfoEnabled();
			case WARNING:
				return logger.isWarnEnabled();
			case ERROR:
				return logger.isErrorEnabled();
			case FATAL:
				return logger.isFatalEnabled();
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
			case INFO:
				logger.info(msg);
				break;
			case WARNING:
				logger.warn(msg);
				break;
			case ERROR:
				logger.error(msg);
				break;
			case FATAL:
				logger.fatal(msg);
				break;
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
			case INFO:
				logger.info(msg, t);
				break;
			case WARNING:
				logger.warn(msg, t);
				break;
			case ERROR:
				logger.error(msg, t);
				break;
			case FATAL:
				logger.fatal(msg, t);
				break;
			default:
				logger.info(msg, t);
				break;
		}
	}
}
