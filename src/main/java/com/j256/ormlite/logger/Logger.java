package com.j256.ormlite.logger;

import java.util.Arrays;

import com.j256.ormlite.logger.Log.Level;

/**
 * Class which wraps our {@link Log} interface and provides {} argument features like slf4j. It allows us to plug in
 * additional log systems if necessary.
 * 
 * <p>
 * <b>NOTE:</b> We do the (msg, arg0), (msg, arg0, arg1), (msg, arg0, arg1, arg2), and (msg, argArray) patterns because
 * if we do ... for everything, we will get a new Object[] each log call which we don't want -- even if the message is
 * never logged because of the log level. Also, we don't use ... at all because we want to know <i>when</i> we are
 * creating a new Object[] so we can make sure it is what we want. I thought this was so much better than slf4j but it
 * turns out they were spot on. Sigh.
 * </p>
 * 
 * @author graywatson
 */
public class Logger {

	private final static String ARG_STRING = "{}";
	private final static int ARG_STRING_LENGTH = ARG_STRING.length();
	private final static Object UNKNOWN_ARG = new Object();
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
	public void trace(String msg) {
		innerLog(Level.TRACE, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0) {
		innerLog(Level.TRACE, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0, Object arg1) {
		innerLog(Level.TRACE, null, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.TRACE, null, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a trace message. Should be protected with a:
	 * 
	 * <pre>
	 * if (logger.isLevelEnabled(Level...)) ...
	 * </pre>
	 */
	public void trace(String msg, Object[] argArray) {
		innerLog(Level.TRACE, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg) {
		innerLog(Level.TRACE, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0) {
		innerLog(Level.TRACE, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0, Object arg1) {
		innerLog(Level.TRACE, throwable, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.TRACE, throwable, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a trace message with a throwable.
	 * 
	 * <pre>
	 * if (logger.isLevelEnabled(Level...)) ...
	 * </pre>
	 */
	public void trace(Throwable throwable, String msg, Object[] argArray) {
		innerLog(Level.TRACE, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg) {
		innerLog(Level.DEBUG, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0) {
		innerLog(Level.DEBUG, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0, Object arg1) {
		innerLog(Level.DEBUG, null, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.DEBUG, null, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a debug message.
	 * 
	 * <pre>
	 * if (logger.isLevelEnabled(Level...)) ...
	 * </pre>
	 */
	public void debug(String msg, Object[] argArray) {
		innerLog(Level.DEBUG, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg) {
		innerLog(Level.DEBUG, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0) {
		innerLog(Level.DEBUG, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0, Object arg1) {
		innerLog(Level.DEBUG, throwable, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.DEBUG, throwable, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a debug message with a throwable.
	 * 
	 * <pre>
	 * if (logger.isLevelEnabled(Level...)) ...
	 * </pre>
	 */
	public void debug(Throwable throwable, String msg, Object[] argArray) {
		innerLog(Level.DEBUG, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg) {
		innerLog(Level.INFO, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0) {
		innerLog(Level.INFO, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0, Object arg1) {
		innerLog(Level.INFO, null, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.INFO, null, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object[] argArray) {
		innerLog(Level.INFO, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg) {
		innerLog(Level.INFO, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0) {
		innerLog(Level.INFO, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0, Object arg1) {
		innerLog(Level.INFO, throwable, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.INFO, throwable, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object[] argArray) {
		innerLog(Level.INFO, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg) {
		innerLog(Level.WARNING, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0) {
		innerLog(Level.WARNING, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0, Object arg1) {
		innerLog(Level.WARNING, null, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.WARNING, null, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object[] argArray) {
		innerLog(Level.WARNING, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg) {
		innerLog(Level.WARNING, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0) {
		innerLog(Level.WARNING, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0, Object arg1) {
		innerLog(Level.WARNING, throwable, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.WARNING, throwable, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object[] argArray) {
		innerLog(Level.WARNING, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg) {
		innerLog(Level.ERROR, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0) {
		innerLog(Level.ERROR, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0, Object arg1) {
		innerLog(Level.ERROR, null, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.ERROR, null, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object[] argArray) {
		innerLog(Level.ERROR, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg) {
		innerLog(Level.ERROR, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0) {
		innerLog(Level.ERROR, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0, Object arg1) {
		innerLog(Level.ERROR, throwable, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.ERROR, throwable, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object[] argArray) {
		innerLog(Level.ERROR, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg) {
		innerLog(Level.FATAL, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0) {
		innerLog(Level.FATAL, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0, Object arg1) {
		innerLog(Level.FATAL, null, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.FATAL, null, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object[] argArray) {
		innerLog(Level.FATAL, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg) {
		innerLog(Level.FATAL, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0) {
		innerLog(Level.FATAL, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0, Object arg1) {
		innerLog(Level.FATAL, throwable, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(Level.FATAL, throwable, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object[] argArray) {
		innerLog(Level.FATAL, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg) {
		innerLog(level, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0) {
		innerLog(level, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0, Object arg1) {
		innerLog(level, null, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(level, null, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object[] argArray) {
		innerLog(level, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg) {
		innerLog(level, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0) {
		innerLog(level, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0, Object arg1) {
		innerLog(level, throwable, msg, arg0, arg1, UNKNOWN_ARG, null);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		innerLog(level, throwable, msg, arg0, arg1, arg2, null);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object[] argArray) {
		innerLog(level, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray);
	}

	private void innerLog(Level level, Throwable throwable, String msg, Object arg0, Object arg1, Object arg2,
			Object[] argArray) {
		if (log.isLevelEnabled(level)) {
			String fullMsg = buildFullMessage(msg, arg0, arg1, arg2, argArray);
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
	private String buildFullMessage(String msg, Object arg0, Object arg1, Object arg2, Object[] argArray) {
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
				sb = new StringBuilder(128);
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
				} else {
					// we have too many {} so we just ignore them
				}
			} else if (argC < argArray.length) {
				appendArg(sb, argArray[argC]);
			} else {
				// we have too many {} so we just ignore them
			}
			argC++;
		}
		if (sb == null) {
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
			sb.append(Arrays.toString((Object[]) arg));
		} else {
			sb.append(arg);
		}
	}
}
