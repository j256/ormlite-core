package com.j256.ormlite.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class which implements {@link Log} by delegating the Apache commons logging classes. Yes we are delegating to a
 * delegating class.
 * 
 * @author graywatson
 */
public class ApacheCommonsLog implements com.j256.ormlite.logger.Log {

	private Log log;

	public ApacheCommonsLog(String className) {
		log = LogFactory.getLog(className);
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
		trace(msg, null);
	}

	public void trace(String msg, Throwable throwable) {
		log.trace(msg, throwable);
	}

	public void debug(String msg) {
		debug(msg, null);
	}

	public void debug(String msg, Throwable throwable) {
		log.debug(msg, throwable);
	}

	public void info(String msg) {
		info(msg, null);
	}

	public void info(String msg, Throwable throwable) {
		log.info(msg, throwable);
	}

	public void warn(String msg) {
		warn(msg, null);
	}

	public void warn(String msg, Throwable throwable) {
		log.warn(msg, throwable);
	}

	public void error(String msg) {
		error(msg, null);
	}

	public void error(String msg, Throwable throwable) {
		log.error(msg, throwable);
	}

	public void fatal(String msg) {
		fatal(msg, null);
	}

	public void fatal(String msg, Throwable throwable) {
		log.fatal(msg, throwable);
	}
}
