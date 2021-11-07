package com.j256.ormlite.logger;

import java.lang.reflect.Array;

/**
 * Class which wraps our {@link LogBackend} interface and provides {} argument features like slf4j. It allows us to plug
 * in additional log backends if necessary.
 *
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * <p>
 * <b>NOTE:</b> We do the (msg, arg0), (msg, arg0, arg1), (msg, arg0, arg1, arg2), (msg, arg0, arg1, arg2, arg3), and
 * (msg, argArray) patterns because if we do ... for everything, we will get a new Object[] each log call which we don't
 * want -- even if the message is never logged because of the log level. Also, we don't use ... at all because we want
 * to know <i>when</i> we are creating a new Object[] so we can make sure it is what we want. I thought ... was so much
 * better than slf4j but it turns out they were spot on. Sigh.
 * </p>
 * 
 * <p>
 * <b>NOTE:</b> When you are using the argArray methods, you should consider wrapping the call in an {@code if} so the
 * {@code Object[]} won't be created unnecessarily.
 * </p>
 * 
 * <pre>
 * if (logger.isLevelEnabled(Level...)) ...
 * </pre>
 * 
 * @author graywatson
 */
public class Logger {

	private static Level globalLevel;

	private final static String ARG_STRING = "{}";
	private final static int ARG_STRING_LENGTH = ARG_STRING.length();
	private final static Object UNKNOWN_ARG = new Object();
	private final static int DEFAULT_FULL_MESSAGE_LENGTH = 128;
	private final LogBackend backend;

	public Logger(LogBackend backend) {
		this.backend = backend;
	}

	/**
	 * Set the log level for all of the loggers. This should be done very early in an application's main or launch
	 * methods. It allows the caller to set a filter on all log messages. Set to null to disable any global log level
	 * filtering of messages and go back to the per-log level matching.
	 */
	public static void setGlobalLogLevel(Level level) {
		Logger.globalLevel = level;
	}

	/**
	 * Return if logging level is enabled.
	 */
	public boolean isLevelEnabled(Level level) {
		return backend.isLevelEnabled(level);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg) {
		logIfEnabled(Level.TRACE, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0) {
		logIfEnabled(Level.TRACE, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.TRACE, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.TRACE, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.TRACE, null, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object[] argArray) {
		logIfEnabled(Level.TRACE, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg) {
		logIfEnabled(Level.TRACE, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, null);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.TRACE, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.TRACE, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.TRACE, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.TRACE, throwable, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.TRACE, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg) {
		logIfEnabled(Level.DEBUG, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0) {
		logIfEnabled(Level.DEBUG, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.DEBUG, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.DEBUG, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.DEBUG, null, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object[] argArray) {
		logIfEnabled(Level.DEBUG, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg) {
		logIfEnabled(Level.DEBUG, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.DEBUG, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.DEBUG, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.DEBUG, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.DEBUG, throwable, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.DEBUG, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg) {
		logIfEnabled(Level.INFO, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0) {
		logIfEnabled(Level.INFO, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.INFO, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.INFO, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.INFO, null, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object[] argArray) {
		logIfEnabled(Level.INFO, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg) {
		logIfEnabled(Level.INFO, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.INFO, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.INFO, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.INFO, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.INFO, throwable, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.INFO, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg) {
		logIfEnabled(Level.WARNING, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0) {
		logIfEnabled(Level.WARNING, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.WARNING, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.WARNING, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.WARNING, null, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object[] argArray) {
		logIfEnabled(Level.WARNING, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg) {
		logIfEnabled(Level.WARNING, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.WARNING, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.WARNING, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.WARNING, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.WARNING, throwable, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.WARNING, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg) {
		logIfEnabled(Level.ERROR, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0) {
		logIfEnabled(Level.ERROR, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.ERROR, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.ERROR, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.ERROR, null, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object[] argArray) {
		logIfEnabled(Level.ERROR, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg) {
		logIfEnabled(Level.ERROR, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.ERROR, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.ERROR, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.ERROR, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.ERROR, throwable, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.ERROR, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg) {
		logIfEnabled(Level.FATAL, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0) {
		logIfEnabled(Level.FATAL, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.FATAL, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.FATAL, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.FATAL, null, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object[] argArray) {
		logIfEnabled(Level.FATAL, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg) {
		logIfEnabled(Level.FATAL, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.FATAL, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.FATAL, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.FATAL, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.FATAL, throwable, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.FATAL, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg) {
		logIfEnabled(level, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0) {
		logIfEnabled(level, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0, Object arg1) {
		logIfEnabled(level, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(level, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(level, null, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object[] argArray) {
		logIfEnabled(level, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg) {
		logIfEnabled(level, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0) {
		logIfEnabled(level, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(level, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(level, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(level, throwable, msg, arg0, arg1, arg2, arg3, null);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(level, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Get the underlying log backend implementation for testing purposes.
	 */
	public LogBackend getLogBackend() {
		return backend;
	}

	private void logIfEnabled(Level level, Throwable throwable, String msg, Object arg0, Object arg1, Object arg2,
			Object arg3, Object[] argArray) {
		if (globalLevel != null && !globalLevel.isEnabled(level)) {
			// don't log the message if the global-level is set and not enabled
		} else if (backend.isLevelEnabled(level)) {
			String fullMsg = buildFullMessage(msg, arg0, arg1, arg2, arg3, argArray);
			if (throwable == null) {
				backend.log(level, fullMsg);
			} else {
				backend.log(level, fullMsg, throwable);
			}
		}
	}

	/**
	 * Return a combined single message from the msg (with possible {}) and optional arguments.
	 */
	private String buildFullMessage(String msg, Object arg0, Object arg1, Object arg2, Object arg3, Object[] argArray) {
		StringBuilder sb = null;
		int lastIndex = 0;
		int argC = 0;
		while (true) {
			int argIndex = msg.indexOf(ARG_STRING, lastIndex);
			// no more {} arguments?
			if (argIndex == -1) {
				break;
			}
			if (sb == null) {
				// we build this lazily in case there is no {} in the msg
				sb = new StringBuilder(DEFAULT_FULL_MESSAGE_LENGTH);
			}
			// add the string before the arg-string
			sb.append(msg, lastIndex, argIndex);
			// shift our last-index past the arg-string
			lastIndex = argIndex + ARG_STRING_LENGTH;
			// add the argument, if we still have any
			if (argArray == null) {
				if (argC == 0) {
					appendArg(sb, arg0);
				} else if (argC == 1) {
					appendArg(sb, arg1);
				} else if (argC == 2) {
					appendArg(sb, arg2);
				} else if (argC == 3) {
					appendArg(sb, arg3);
				} else {
					// we have more than 4 {} so we just ignore them
				}
			} else if (argC < argArray.length) {
				appendArg(sb, argArray[argC]);
			} else {
				// we have too many {} than in the argArray so we just ignore them
			}
			argC++;
		}
		if (sb == null) {
			// if we have yet to create a StringBuilder then just append the msg which has no {}
			return msg;
		} else {
			// spit out the end of the msg
			sb.append(msg, lastIndex, msg.length());
			return sb.toString();
		}
	}

	private void appendArg(StringBuilder sb, Object arg) {
		if (arg == UNKNOWN_ARG) {
			// ignore it
		} else if (arg == null) {
			// this is what sb.append(null) does
			sb.append("null");
		} else if (arg.getClass().isArray()) {
			// we do a special thing if we have an array argument
			sb.append('[');
			int length = Array.getLength(arg);
			for (int i = 0; i < length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				// goes recursive in case we have an array of arrays
				appendArg(sb, Array.get(arg, i));
			}
			sb.append(']');
		} else {
			// might as well do the toString here because we know it isn't null
			sb.append(arg.toString());
		}
	}
}
