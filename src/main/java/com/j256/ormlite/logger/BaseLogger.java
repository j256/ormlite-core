package com.j256.ormlite.logger;

import java.lang.reflect.Array;

/**
 * Base class which does the logging to the backend.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public abstract class BaseLogger {

	private final static String ARG_STRING = "{}";
	private final static int ARG_STRING_LENGTH = ARG_STRING.length();
	protected final static Object UNKNOWN_ARG = new Object();
	private final static int DEFAULT_FULL_MESSAGE_LENGTH = 128;
	final static String NO_MESSAGE_MESSAGE = "no log message";

	/**
	 * Global log level that overrides any backend configuration about the log level. You can set this to, for example,
	 * Level.INFO to show all info messages or Level.OFF to disable all log messages. Set it to null to have the log
	 * backend configuration determine whether to display log messages.
	 */
	private static Level globalLevel = LoggerConstants.DEFAULT_GLOBAL_LOG_LEVEL;

	private final LogBackend backend;

	public BaseLogger(LogBackend backend) {
		this.backend = backend;
	}

	/**
	 * Get the global log level.  For testing purposes. 
	 */
	public static Level getGlobalLevel() {
		return globalLevel;
	}

	/**
	 * Set the log level for all of the loggers. This should be done very early in an application's main or launch
	 * methods. It allows the caller to set a filter on all log messages. Set to null to disable any global log level
	 * filtering of messages and go back to the per-log level matching.
	 */
	public static void setGlobalLogLevel(Level level) {
		BaseLogger.globalLevel = level;
	}

	/**
	 * Return true if logging level is enabled else false.
	 */
	public boolean isLevelEnabled(Level level) {
		return backend.isLevelEnabled(level);
	}

	/**
	 * Return the count of the number of arg strings in the message.
	 */
	protected int countArgStrings(String msg) {
		int count = 0;
		int index = 0;
		while (true) {
			int found = msg.indexOf(ARG_STRING, index);
			if (found < 0) {
				return count;
			}
			count++;
			index = found + ARG_STRING_LENGTH;
		}
	}

	/**
	 * Get the underlying log backend implementation for testing purposes.
	 */
	LogBackend getLogBackend() {
		return backend;
	}

	/**
	 * Main log-if-enabled method with all argument combinations.
	 */
	protected void logIfEnabled(Level level, Throwable throwable, String msg, Object arg0, Object arg1, Object arg2,
			Object arg3, Object[] argArray, int argArrayLength) {
		if (globalLevel != null && !globalLevel.isEnabled(level)) {
			// don't log the message if the global-level is set and not enabled
		} else if (backend != null && backend.isLevelEnabled(level)) {
			doLog(level, throwable, msg, arg0, arg1, arg2, arg3, argArray, argArrayLength);
		}
	}

	/**
	 * Log msg, throwable, and args. If-enabled checks should have been done by this point.
	 */
	protected void doLog(Level level, Throwable throwable, String msg, Object[] argArray, int argArrayLength) {
		doLog(level, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray, argArrayLength);
	}

	/**
	 * Main log method with all argument combinations. If-enabled checks should have been done by this point.
	 */
	private void doLog(Level level, Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3,
			Object[] argArray, int argArrayLength) {
		String fullMsg;
		if (arg0 == UNKNOWN_ARG && argArray == null) {
			// this will just output the message without parsing any {}
			fullMsg = msg;
		} else if (msg == null) {
			// if msg is null then just spit out the arguments
			fullMsg = buildArgsMessage(arg0, arg1, arg2, arg3, argArray, argArrayLength);
		} else {
			// do the whole {} expansion thing
			fullMsg = buildFullMessage(msg, arg0, arg1, arg2, arg3, argArray, argArrayLength);
		}
		if (fullMsg == null) {
			fullMsg = NO_MESSAGE_MESSAGE;
		}
		if (throwable == null) {
			backend.log(level, fullMsg);
		} else {
			backend.log(level, fullMsg, throwable);
		}
	}

	/**
	 * Return a combined single message from the msg (with possible {}) and optional arguments.
	 */
	private String buildFullMessage(String msg, Object arg0, Object arg1, Object arg2, Object arg3, Object[] argArray,
			int argArrayLength) {
		StringBuilder sb = null;
		int lastIndex = 0;
		int argCount = 0;
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
			if (lastIndex < argIndex) {
				sb.append(msg, lastIndex, argIndex);
			}
			// shift our last-index past the arg-string
			lastIndex = argIndex + ARG_STRING_LENGTH;
			// add the argument, if we still have any
			appendArg(sb, argCount++, arg0, arg1, arg2, arg3, argArray, argArrayLength);
		}
		if (sb == null) {
			// if we have yet to create a StringBuilder then just return the msg which has no {}
			return msg;
		} else {
			// spit out the end of the msg
			if (lastIndex < msg.length()) {
				sb.append(msg, lastIndex, msg.length());
			}
			return sb.toString();
		}
	}

	/**
	 * Build a message just from the arguments like: 'arg0', 'arg1', ...
	 */
	private String buildArgsMessage(Object arg0, Object arg1, Object arg2, Object arg3, Object[] argArray,
			int argArrayLength) {
		StringBuilder sb = new StringBuilder(DEFAULT_FULL_MESSAGE_LENGTH);
		boolean first = true;
		int argCount = 0;
		sb.append('\'');
		while (true) {
			if (first) {
				first = false;
			} else {
				sb.append("', '");
			}
			if (!appendArg(sb, argCount, arg0, arg1, arg2, arg3, argArray, argArrayLength)) {
				break;
			}
			argCount++;
		}
		if (argCount == 0) {
			// might not get here but let's be careful out there
			return null;
		}
		// take off the ", '" at the end of the last arg because we can't tell ahead of time how many args there are
		sb.setLength(sb.length() - 3);
		return sb.toString();
	}

	/**
	 * Append an argument from the individual arguments or the array.
	 */
	private boolean appendArg(StringBuilder sb, int argCount, Object arg0, Object arg1, Object arg2, Object arg3,
			Object[] argArray, int argArrayLength) {
		if (argArray == null) {
			switch (argCount) {
				case 0:
					return appendArg(sb, arg0);
				case 1:
					return appendArg(sb, arg1);
				case 2:
					return appendArg(sb, arg2);
				case 3:
					return appendArg(sb, arg3);
				default:
					// we have too many {} so we just ignore them
					return false;
			}
		} else if (argCount < argArrayLength) {
			return appendArg(sb, argArray[argCount]);
		} else {
			// we have too many {} so we just ignore them
			return false;
		}
	}

	/**
	 * Append a particular argument object returning false if we are out of arguments.
	 */
	private boolean appendArg(StringBuilder sb, Object arg) {
		if (arg == UNKNOWN_ARG) {
			// ignore it
			return false;
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
				// may go recursive in case we have an array of arrays
				appendArg(sb, Array.get(arg, i));
			}
			sb.append(']');
		} else if (arg instanceof LogArgumentCreator) {
			// call the method to get the argument which can be null
			String str = ((LogArgumentCreator) arg).createLogArg();
			sb.append(str);
		} else {
			// might as well do the toString here because we know it isn't null
			sb.append(arg.toString());
		}
		return true;
	}
}
