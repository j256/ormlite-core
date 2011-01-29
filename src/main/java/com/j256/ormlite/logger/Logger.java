package com.j256.ormlite.logger;

import java.util.Arrays;

import com.j256.ormlite.logger.Log.Level;

/**
 * Class which wraps our {@link Log} interface and provides {} argument features like slf4j. It allows us to plug in
 * additional log systems if necessary.
 * 
 * @author graywatson
 */
public class Logger {

	private final static String ARG_STRING = "{}";
	private final static int ARG_STRING_LENGTH = ARG_STRING.length();
	private final Log log;

	public Logger(Log log) {
		this.log = log;
	}

	/**
	 * Return if trace logging is enabled.
	 */
	public boolean isTraceEnabled() {
		return log.isLevelEnabled(Level.TRACE);
	}

	/**
	 * Return if debug logging is enabled.
	 */
	public boolean isDebugEnabled() {
		return log.isLevelEnabled(Level.DEBUG);
	}

	/**
	 * Return if info logging is enabled.
	 */
	public boolean isInfoEnabled() {
		return log.isLevelEnabled(Level.INFO);
	}

	/**
	 * Return if warn logging is enabled.
	 */
	public boolean isWarnEnabled() {
		return log.isLevelEnabled(Level.WARNING);
	}

	/**
	 * Return if error logging is enabled.
	 */
	public boolean isErrorEnabled() {
		return log.isLevelEnabled(Level.ERROR);
	}

	/**
	 * Return if fatal logging is enabled.
	 */
	public boolean isFatalEnabled() {
		return log.isLevelEnabled(Level.FATAL);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object... args) {
		log(Level.TRACE, buildFullMessage(msg, args), null);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object... args) {
		log(Level.TRACE, buildFullMessage(msg, args), throwable);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object... args) {
		log(Level.DEBUG, buildFullMessage(msg, args), null);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object... args) {
		log(Level.DEBUG, buildFullMessage(msg, args), throwable);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object... args) {
		log(Level.INFO, buildFullMessage(msg, args), null);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object... args) {
		log(Level.INFO, buildFullMessage(msg, args), throwable);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object... args) {
		log(Level.WARNING, buildFullMessage(msg, args), null);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object... args) {
		log(Level.WARNING, buildFullMessage(msg, args), throwable);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object... args) {
		log(Level.ERROR, buildFullMessage(msg, args), null);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object... args) {
		log(Level.ERROR, buildFullMessage(msg, args), throwable);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object... args) {
		log(Level.FATAL, buildFullMessage(msg, args), null);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object... args) {
		log(Level.FATAL, buildFullMessage(msg, args), throwable);
	}

	private void log(Level level, String msg, Throwable throwable) {
		if (log.isLevelEnabled(level)) {
			if (throwable == null) {
				log.log(level, msg);
			} else {
				log.log(level, msg, throwable);
			}
		}
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
			lastIndex = argIndex + ARG_STRING_LENGTH;
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
