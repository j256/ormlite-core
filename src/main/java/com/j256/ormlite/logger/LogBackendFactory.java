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
	 * Return true if the backend factory is available and can be used. If the backend available on the classpath and
	 * wired correctly. Typically the factory wouldn't have been able to be instantiated if the classes weren't there
	 * but sometimes there are some additional checks to see if the factory is fully available.
	 */
	public boolean isAvailable();

	/**
	 * Create a log backend implementation from the class-label.
	 */
	public LogBackend createLogBackend(String classLabel);
}
