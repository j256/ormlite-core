package com.j256.ormlite.logger;

/**
 * Implementation of our logger which delegates to the internal Android logger.
 * 
 * <p>
 * <b>NOTE:</b> So this class will be red in your IDE if you don't have Android in your classpath. That's by design.
 * This class will not be instantiated unless the logging code detects the commons-log classes.
 * </p>
 * 
 * @author graywatson
 */
public class AndroidLog implements Log {

	private String className;

	public AndroidLog(String className) {
		// get the last part of the class name
		this.className = LoggerFactory.getSimpleClassName(className);
	}

	public boolean isTraceEnabled() {
		return android.util.Log.isLoggable(className, android.util.Log.VERBOSE);
	}

	public boolean isDebugEnabled() {
		return android.util.Log.isLoggable(className, android.util.Log.DEBUG);
	}

	public boolean isInfoEnabled() {
		return android.util.Log.isLoggable(className, android.util.Log.INFO);
	}

	public boolean isWarnEnabled() {
		return android.util.Log.isLoggable(className, android.util.Log.WARN);
	}

	public boolean isErrorEnabled() {
		return android.util.Log.isLoggable(className, android.util.Log.ERROR);
	}

	public boolean isFatalEnabled() {
		return android.util.Log.isLoggable(className, android.util.Log.ERROR);
	}

	public void trace(String message) {
		android.util.Log.v(className, message);
	}

	public void trace(String message, Throwable t) {
		android.util.Log.v(className, message, t);
	}

	public void debug(String message) {
		android.util.Log.d(className, message);
	}

	public void debug(String message, Throwable t) {
		android.util.Log.d(className, message, t);
	}

	public void info(String message) {
		android.util.Log.i(className, message);
	}

	public void info(String message, Throwable t) {
		android.util.Log.i(className, message, t);
	}

	public void warn(String message) {
		android.util.Log.w(className, message);
	}

	public void warn(String message, Throwable t) {
		android.util.Log.w(className, message, t);
	}

	public void error(String message) {
		android.util.Log.e(className, message);
	}

	public void error(String message, Throwable t) {
		android.util.Log.e(className, message, t);
	}

	public void fatal(String message) {
		android.util.Log.e(className, message);
	}

	public void fatal(String message, Throwable t) {
		android.util.Log.e(className, message, t);
	}
}
