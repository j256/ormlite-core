package com.j256.ormlite.logger;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class which wraps {@link Log} and provides {} argument features like slf4j. It will also allow us to plug in
 * additional log systems in the future if necessary.
 * 
 * @author graywatson
 */
public class Logger {

	private final static String ARG_STRING = "{}";
	private Log log;

	public Logger(String className) {
		log = LogFactory.getLog(className);
	}

	/**
	 * Return if trace logging is enabled.
	 */
	public boolean isTraceEnabled() {
		return log.isTraceEnabled();
	}

	/**
	 * Return if debug logging is enabled.
	 */
	public boolean isDebugEnabled() {
		return log.isDebugEnabled();
	}

	/**
	 * Return if info logging is enabled.
	 */
	public boolean isInfoEnabled() {
		return log.isInfoEnabled();
	}

	/**
	 * Return if warn logging is enabled.
	 */
	public boolean isWarnEnabled() {
		return log.isWarnEnabled();
	}

	/**
	 * Return if error logging is enabled.
	 */
	public boolean isErrorEnabled() {
		return log.isErrorEnabled();
	}

	/**
	 * Return if fatal logging is enabled.
	 */
	public boolean isFatalEnabled() {
		return log.isFatalEnabled();
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object... args) {
		trace((Throwable) null, msg, args);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object... args) {
		if (log.isTraceEnabled()) {
			log.trace(buildFullMessage(msg, args), throwable);
		}
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object... args) {
		debug((Throwable) null, msg, args);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object... args) {
		if (log.isDebugEnabled()) {
			log.debug(buildFullMessage(msg, args), throwable);
		}
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object... args) {
		info((Throwable) null, msg, args);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object... args) {
		if (log.isInfoEnabled()) {
			log.info(buildFullMessage(msg, args), throwable);
		}
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object... args) {
		warn((Throwable) null, msg, args);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object... args) {
		if (log.isWarnEnabled()) {
			log.warn(buildFullMessage(msg, args), throwable);
		}
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object... args) {
		error((Throwable) null, msg, args);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object... args) {
		if (log.isErrorEnabled()) {
			log.error(buildFullMessage(msg, args), throwable);
		}
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object... args) {
		fatal((Throwable) null, msg, args);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object... args) {
		if (log.isFatalEnabled()) {
			log.fatal(buildFullMessage(msg, args), throwable);
		}
	}

	/**
	 * Set the delagation {@link Log}. For testing purposes.
	 */
	void setLog(Log log) {
		this.log = log;
	}

	/**
	 * Return a combined single message from the msg (with possible {}) and optional arguments.
	 */
	private String buildFullMessage(String msg, Object[] args) {
		StringBuilder sb = new StringBuilder();
		int lastIndex = 0;
		int argC = 0;
		while (true) {
			int argIndex = msg.indexOf(ARG_STRING, lastIndex);
			// no more {} arguments?
			if (argIndex == -1) {
				break;
			}
			// add the string before the arg-string
			sb.append(msg.substring(lastIndex, argIndex));
			// shift our last-index past the arg-string
			lastIndex = argIndex + ARG_STRING.length();
			// add the argument, if we still have any
			if (argC < args.length) {
				Object arg = args[argC];
				if (arg != null && arg.getClass().isArray()) {
					sb.append(Arrays.toString((Object[]) arg));
				} else {
					sb.append(arg);
				}
				argC++;
			}
		}
		// spit out the end of the msg
		sb.append(msg.substring(lastIndex));
		return sb.toString();
	}
}
