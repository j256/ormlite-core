package com.j256.ormlite.logger;

/**
 * Basically a copy of the org.apache.commons.logging.Log interface so we can replace it locally if we want.
 * 
 * @author graywatson
 */
public interface Log {

	/**
	 * Returns true if the log mode is in trace or higher.
	 */
	public boolean isTraceEnabled();

	/**
	 * Returns true if the log mode is in debug or higher.
	 */
	public boolean isDebugEnabled();

	/**
	 * Returns true if the log mode is in info or higher.
	 */
	public boolean isInfoEnabled();

	/**
	 * Returns true if the log mode is in warn or higher.
	 */
	public boolean isWarnEnabled();

	/**
	 * Returns true if the log mode is in error or higher.
	 */
	public boolean isErrorEnabled();

	/**
	 * Returns true if the log mode is in fatal or higher.
	 */
	public boolean isFatalEnabled();

	/**
	 * Log a trace message.
	 */
	public void trace(String message);

	/**
	 * Log a trace message with a throwable.
	 */
	public void trace(String message, Throwable t);

	/**
	 * Log a debug message.
	 */
	public void debug(String message);

	/**
	 * Log a debug message with a throwable.
	 */
	public void debug(String message, Throwable t);

	/**
	 * Log a info message.
	 */
	public void info(String message);

	/**
	 * Log a info message with a throwable.
	 */
	public void info(String message, Throwable t);

	/**
	 * Log a warn message.
	 */
	public void warn(String message);

	/**
	 * Log a warn message with a throwable.
	 */
	public void warn(String message, Throwable t);

	/**
	 * Log a error message.
	 */
	public void error(String message);

	/**
	 * Log a error message with a throwable.
	 */
	public void error(String message, Throwable t);

	/**
	 * Log a fatal message.
	 */
	public void fatal(String message);

	/**
	 * Log a fatal message with a throwable.
	 */
	public void fatal(String message, Throwable t);
}
