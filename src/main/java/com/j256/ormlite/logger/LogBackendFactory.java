package com.j256.ormlite.logger;

/**
 * Factory for generating LogBackend instances.
 *
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public interface LogBackendFactory {

	/**
	 * Create a log backend implementation from the class-label.
	 */
	public LogBackend createLogBackend(String classLabel);
}
