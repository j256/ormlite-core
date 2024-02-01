package com.j256.ormlite.logger;

/**
 * Class which is called when you want to log messages via the {@link LogBackend} interface. It provides {} argument
 * features like slf4j.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * <p>
 * <b>NOTE:</b> We do the (msg, arg0), (msg, arg0, arg1), (msg, arg0, arg1, arg2), (msg, arg0, arg1, arg2, arg3), and
 * (msg, argArray) patterns because if we do ... for everything, we will get a new Object[] each log call which we don't
 * want -- even if the message is never logged because of the log level. If you must use variable arguments then you
 * need to call the explicit traceArgs(...), infoArgs(...), etc. we want to know <i>when</i> we are creating a new
 * Object[] so we can make sure it is what you want.
 * </p>
 * 
 * <p>
 * <b>NOTE:</b> When you are using the argArray methods or the traceArgs() pattern, you should consider wrapping the
 * call in an {@code if} testing the [@link {@link #isLevelEnabled(Level)}} so the {@code Object[]} won't be created
 * unnecessarily. For example:
 * </p>
 * 
 * <pre>
 * if (logger.isLevelEnabled(Level...)) ...
 * </pre>
 * 
 * @author graywatson
 */
public class Logger extends BaseLogger {

	final static String NO_MESSAGE_MESSAGE = "no log message";

	public Logger(LogBackend backend) {
		super(backend);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg) {
		logIfEnabled(Level.TRACE, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0) {
		logIfEnabled(Level.TRACE, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.TRACE, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.TRACE, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a trace message.
	 */
	public void trace(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.TRACE, null, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a trace message with an argument array.
	 */
	public void trace(String msg, Object[] argArray) {
		logIfEnabled(Level.TRACE, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a trace message with a variable number of arguments.
	 */
	public void traceArgs(String msg, Object... argArray) {
		logIfEnabled(Level.TRACE, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg) {
		logIfEnabled(Level.TRACE, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, null, 0);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.TRACE, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.TRACE, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.TRACE, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.TRACE, throwable, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a trace message with a throwable and an argument array.
	 */
	public void trace(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.TRACE, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a trace message with a throwable and a variable number of arguments.
	 */
	public void traceArgs(Throwable throwable, String msg, Object... argArray) {
		logIfEnabled(Level.TRACE, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg) {
		logIfEnabled(Level.DEBUG, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0) {
		logIfEnabled(Level.DEBUG, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.DEBUG, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.DEBUG, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a debug message.
	 */
	public void debug(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.DEBUG, null, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a debug message with an argument array.
	 */
	public void debug(String msg, Object[] argArray) {
		logIfEnabled(Level.DEBUG, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a debug message with a variable number of arguments.
	 */
	public void debugArgs(String msg, Object... argArray) {
		logIfEnabled(Level.DEBUG, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg) {
		logIfEnabled(Level.DEBUG, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.DEBUG, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.DEBUG, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.DEBUG, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.DEBUG, throwable, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a debug message with a throwable and an argument array.
	 */
	public void debug(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.DEBUG, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a debug message with a throwable and a variable number of arguments.
	 */
	public void debugArgs(Throwable throwable, String msg, Object... argArray) {
		logIfEnabled(Level.DEBUG, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg) {
		logIfEnabled(Level.INFO, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0) {
		logIfEnabled(Level.INFO, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.INFO, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.INFO, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a info message.
	 */
	public void info(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.INFO, null, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a info message with an argument array.
	 */
	public void info(String msg, Object[] argArray) {
		logIfEnabled(Level.INFO, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a info message with a variable number of arguments.
	 */
	public void infoArgs(String msg, Object... argArray) {
		logIfEnabled(Level.INFO, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg) {
		logIfEnabled(Level.INFO, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.INFO, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.INFO, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.INFO, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a info message with a throwable.
	 */
	public void info(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.INFO, throwable, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a info message with a throwable and an argument array.
	 */
	public void info(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.INFO, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a info message with a throwable and a variable number of arguments.
	 */
	public void infoArgs(Throwable throwable, String msg, Object... argArray) {
		logIfEnabled(Level.INFO, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg) {
		logIfEnabled(Level.WARNING, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0) {
		logIfEnabled(Level.WARNING, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.WARNING, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.WARNING, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a warning message.
	 */
	public void warn(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.WARNING, null, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a warning message with an argument array.
	 */
	public void warn(String msg, Object[] argArray) {
		logIfEnabled(Level.WARNING, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a warning message with a variable number of arguments.
	 */
	public void warnArgs(String msg, Object... argArray) {
		logIfEnabled(Level.WARNING, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg) {
		logIfEnabled(Level.WARNING, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.WARNING, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.WARNING, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.WARNING, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a warning message with a throwable.
	 */
	public void warn(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.WARNING, throwable, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a warning message with a throwable and an argument array.
	 */
	public void warn(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.WARNING, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a warning message with a throwable and a variable number of arguments.
	 */
	public void warnArgs(Throwable throwable, String msg, Object... argArray) {
		logIfEnabled(Level.WARNING, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg) {
		logIfEnabled(Level.ERROR, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0) {
		logIfEnabled(Level.ERROR, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.ERROR, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.ERROR, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a error message.
	 */
	public void error(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.ERROR, null, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a error message with an argument array.
	 */
	public void error(String msg, Object[] argArray) {
		logIfEnabled(Level.ERROR, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a error message with a variable number of arguments.
	 */
	public void errorArgs(String msg, Object... argArray) {
		logIfEnabled(Level.ERROR, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg) {
		logIfEnabled(Level.ERROR, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.ERROR, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.ERROR, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.ERROR, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a error message with a throwable.
	 */
	public void error(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.ERROR, throwable, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a error message with a throwable and an argument array.
	 */
	public void error(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.ERROR, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a error message with a throwable and a variable number of arguments.
	 */
	public void errorArgs(Throwable throwable, String msg, Object... argArray) {
		logIfEnabled(Level.ERROR, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg) {
		logIfEnabled(Level.FATAL, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0) {
		logIfEnabled(Level.FATAL, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.FATAL, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.FATAL, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a fatal message.
	 */
	public void fatal(String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.FATAL, null, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a fatal message with an argument array.
	 */
	public void fatal(String msg, Object[] argArray) {
		logIfEnabled(Level.FATAL, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a fatal message with a variable number of arguments.
	 */
	public void fatalArgs(String msg, Object... argArray) {
		logIfEnabled(Level.FATAL, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg) {
		logIfEnabled(Level.FATAL, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0) {
		logIfEnabled(Level.FATAL, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(Level.FATAL, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(Level.FATAL, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(Level.FATAL, throwable, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a fatal message with a throwable and an argument array.
	 */
	public void fatal(Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(Level.FATAL, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a fatal message with a throwable and a variable number of arguments.
	 */
	public void fatalArgs(Throwable throwable, String msg, Object... argArray) {
		logIfEnabled(Level.FATAL, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg) {
		logIfEnabled(level, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0) {
		logIfEnabled(level, null, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0, Object arg1) {
		logIfEnabled(level, null, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(level, null, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a message at the provided level.
	 */
	public void log(Level level, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(level, null, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a message at the provided level with an argument array.
	 */
	public void log(Level level, String msg, Object[] argArray) {
		logIfEnabled(level, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray, argArray.length);
	}

	/**
	 * Log a message at the provided level with a variable number of arguments.
	 */
	public void logArgs(Level level, String msg, Object... argArray) {
		logIfEnabled(level, null, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray, argArray.length);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg) {
		logIfEnabled(level, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0) {
		logIfEnabled(level, throwable, msg, arg0, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0, Object arg1) {
		logIfEnabled(level, throwable, msg, arg0, arg1, UNKNOWN_ARG, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0, Object arg1, Object arg2) {
		logIfEnabled(level, throwable, msg, arg0, arg1, arg2, UNKNOWN_ARG, null, 0);
	}

	/**
	 * Log a message with a throwable at the provided level.
	 */
	public void log(Level level, Throwable throwable, String msg, Object arg0, Object arg1, Object arg2, Object arg3) {
		logIfEnabled(level, throwable, msg, arg0, arg1, arg2, arg3, null, 0);
	}

	/**
	 * Log a message with a throwable at the provided level and an argument array.
	 */
	public void log(Level level, Throwable throwable, String msg, Object[] argArray) {
		logIfEnabled(level, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}

	/**
	 * Log a message with a throwable at the provided level and a variable number of arguments.
	 */
	public void logArgs(Level level, Throwable throwable, String msg, Object... argArray) {
		logIfEnabled(level, throwable, msg, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, UNKNOWN_ARG, argArray,
				argArray.length);
	}
}
