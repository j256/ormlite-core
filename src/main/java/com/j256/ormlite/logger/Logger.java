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
	 * Return if logging level is enabled.
	 */
	public boolean isLevelEnabled(Level level) {
		return log.isLevelEnabled(level);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object... args) {
		log(Level.TRACE, null, msg, args);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object... args) {
		log(Level.TRACE, throwable, msg, args);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object... args) {
		log(Level.DEBUG, null, msg, args);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object... args) {
		log(Level.DEBUG, throwable, msg, args);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object... args) {
		log(Level.INFO, null, msg, args);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object... args) {
		log(Level.INFO, throwable, msg, args);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object... args) {
		log(Level.WARNING, null, msg, args);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object... args) {
		log(Level.WARNING, throwable, msg, args);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object... args) {
		log(Level.ERROR, null, msg, args);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object... args) {
		log(Level.ERROR, throwable, msg, args);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object... args) {
		log(Level.FATAL, null, msg, args);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object... args) {
		log(Level.FATAL, throwable, msg, args);
	}

	private void log(Level level, Throwable throwable, String msg, Object[] args) {
		if (log.isLevelEnabled(level)) {
			String fullMsg = buildFullMessage(msg, args);
			if (throwable == null) {
				log.log(level, fullMsg);
			} else {
				log.log(level, fullMsg, throwable);
			}
		}
	}

	/**
	 * Return a combined single message from the msg (with possible {}) and optional arguments.
	 */
	private String buildFullMessage(String msg, Object[] args) {
		StringBuilder sb = new StringBuilder(128);
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
