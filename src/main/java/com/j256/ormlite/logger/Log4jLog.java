package com.j256.ormlite.logger;

import org.apache.log4j.Level;

/**
 * Class which implements our {@link com.j256.ormlite.logger.Log} interface by delegating to Apache Log4j.
 * 
 * <p>
 * <b>NOTE:</b> So this class will be red in your IDE if you don't have Log4j in your classpath. That's by design. This
 * class will not be instantiated unless the logging code detects the Log4j classes.
 * </p>
 * 
 * @author graywatson
 */
public class Log4jLog implements com.j256.ormlite.logger.Log {

	private org.apache.log4j.Logger logger;

	public Log4jLog(String className) {
		logger = org.apache.log4j.Logger.getLogger(className);
	}

	public boolean isTraceEnabled() {
		return logger.isEnabledFor(Level.TRACE);
	}

	public boolean isDebugEnabled() {
		return logger.isEnabledFor(Level.DEBUG);
	}

	public boolean isInfoEnabled() {
		return logger.isEnabledFor(Level.INFO);
	}

	public boolean isWarnEnabled() {
		return logger.isEnabledFor(Level.WARN);
	}

	public boolean isErrorEnabled() {
		return logger.isEnabledFor(Level.ERROR);
	}

	public boolean isFatalEnabled() {
		return logger.isEnabledFor(Level.FATAL);
	}

	public void trace(String msg) {
		logger.trace(msg);
	}

	public void trace(String msg, Throwable throwable) {
		logger.trace(msg, throwable);
	}

	public void debug(String msg) {
		logger.debug(msg);
	}

	public void debug(String msg, Throwable throwable) {
		logger.debug(msg, throwable);
	}

	public void info(String msg) {
		logger.info(msg);
	}

	public void info(String msg, Throwable throwable) {
		logger.info(msg, throwable);
	}

	public void warn(String msg) {
		logger.warn(msg);
	}

	public void warn(String msg, Throwable throwable) {
		logger.warn(msg, throwable);
	}

	public void error(String msg) {
		logger.error(msg);
	}

	public void error(String msg, Throwable throwable) {
		logger.error(msg, throwable);
	}

	public void fatal(String msg) {
		logger.fatal(msg);
	}

	public void fatal(String msg, Throwable throwable) {
		logger.fatal(msg, throwable);
	}
}
