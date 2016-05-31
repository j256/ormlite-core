package com.j256.ormlite.logger;

/**
 * Class which implements our {@link com.j256.ormlite.logger.Log} interface by delegating to slf4j.
 * 
 * @author graywatson
 */
public class Slf4jLoggingLog implements Log {

	private final org.slf4j.Logger logger;

	public Slf4jLoggingLog(String className) {
		this.logger = org.slf4j.LoggerFactory.getLogger(className);
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		switch (level) {
			case TRACE :
				return logger.isTraceEnabled();
			case DEBUG :
				return logger.isDebugEnabled();
			case INFO :
				return logger.isInfoEnabled();
			case WARNING :
				return logger.isWarnEnabled();
			case ERROR :
				return logger.isErrorEnabled();
			case FATAL :
				return logger.isErrorEnabled();
			default :
				return logger.isInfoEnabled();
		}
	}

	@Override
	public void log(Level level, String msg) {
		switch (level) {
			case TRACE :
				logger.trace(msg);
				break;
			case DEBUG :
				logger.debug(msg);
				break;
			case INFO :
				logger.info(msg);
				break;
			case WARNING :
				logger.warn(msg);
				break;
			case ERROR :
				logger.error(msg);
				break;
			case FATAL :
				logger.error(msg);
				break;
			default :
				logger.info(msg);
				break;
		}
	}

	@Override
	public void log(Level level, String msg, Throwable t) {
		switch (level) {
			case TRACE :
				logger.trace(msg, t);
				break;
			case DEBUG :
				logger.debug(msg, t);
				break;
			case INFO :
				logger.info(msg, t);
				break;
			case WARNING :
				logger.warn(msg, t);
				break;
			case ERROR :
				logger.error(msg, t);
				break;
			case FATAL :
				logger.error(msg, t);
				break;
			default :
				logger.info(msg, t);
				break;
		}
	}
}
