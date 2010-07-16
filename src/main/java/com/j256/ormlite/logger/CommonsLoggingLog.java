package com.j256.ormlite.logger;

/**
 * Class which implements our {@link com.j256.ormlite.logger.Log} interface by delegating to the Apache commons logging
 * classes. Yes we are delegating to a delegating class.
 * 
 * <p>
 * <b>NOTE:</b> So this class will be red in your IDE if you don't have commons-log in your classpath. That's by design.
 * This class will not be instantiated unless the logging code detects the commons-log classes.
 * </p>
 * 
 * @author graywatson
 */
public class CommonsLoggingLog implements com.j256.ormlite.logger.Log {

	private org.apache.commons.logging.Log log;

	public CommonsLoggingLog(String className) {
		log = org.apache.commons.logging.LogFactory.getLog(className);
	}

	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	public boolean isWarnEnabled() {
		return log.isWarnEnabled();
	}

	public boolean isErrorEnabled() {
		return log.isErrorEnabled();
	}

	public boolean isFatalEnabled() {
		return log.isFatalEnabled();
	}

	public void trace(String msg) {
		log.trace(msg);
	}

	public void trace(String msg, Throwable throwable) {
		log.trace(msg, throwable);
	}

	public void debug(String msg) {
		log.debug(msg);
	}

	public void debug(String msg, Throwable throwable) {
		log.debug(msg, throwable);
	}

	public void info(String msg) {
		log.info(msg);
	}

	public void info(String msg, Throwable throwable) {
		log.info(msg, throwable);
	}

	public void warn(String msg) {
		log.warn(msg);
	}

	public void warn(String msg, Throwable throwable) {
		log.warn(msg, throwable);
	}

	public void error(String msg) {
		log.error(msg);
	}

	public void error(String msg, Throwable throwable) {
		log.error(msg, throwable);
	}

	public void fatal(String msg) {
		log.fatal(msg);
	}

	public void fatal(String msg, Throwable throwable) {
		log.fatal(msg, throwable);
	}
}
