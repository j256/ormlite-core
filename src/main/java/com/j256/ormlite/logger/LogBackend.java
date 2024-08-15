package com.j256.ormlite.logger;

/**
 * Log backend classes which persist log messages. The implementations of this may or may not be in the classpath.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public interface LogBackend {

	/**
	 * Returns true if the log level argument is enabled meaning that the log messages should be assembled and shown.
	 */
	public boolean isLevelEnabled(Level level);

	/**
	 * Log a message.
	 */
	public void log(Level level, String message);

	/**
	 * Log a message with a throwable which is guaranteed to not be null.
	 */
	public void log(Level level, String message, Throwable throwable);
}
