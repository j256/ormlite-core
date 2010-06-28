package com.j256.ormlite.logger;

/**
 * Factory that creates {@link Logger} instances.
 */
public class LoggerFactory {

	/**
	 * For static calls only.
	 */
	private LoggerFactory() {
	}

	/**
	 * Return a logger associated with a particular class.
	 */
	public static Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

	/**
	 * Return a logger associated with a particular class name.
	 */
	public static Logger getLogger(String className) {
		return new Logger(className);
	}
}
