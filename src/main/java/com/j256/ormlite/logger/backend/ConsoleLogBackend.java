package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;

/**
 * Log backend that writes to the console.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class ConsoleLogBackend implements LogBackend {

	private static final String LINE_SEPARATOR = System.lineSeparator();

	private String className;

	public ConsoleLogBackend(String className) {
		this.className = className;
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		// always true so you should use Logger#setGlobalLogLevel() to set the level
		return true;
	}

	@Override
	public void log(Level level, String msg) {
		// we do this so the print is one IO operation and not 2 with the newline
		String output = className + ' ' + level + ' ' + msg + LINE_SEPARATOR;
		if (Level.WARNING.isEnabled(level)) {
			System.err.print(output);
		} else {
			System.out.print(output);
		}
	}

	@Override
	public void log(Level level, String msg, Throwable throwable) {
		log(level, msg);
		// we use this instead of printStackTrace() directly because we want one IO operation
		log(level, LogBackendUtil.throwableToString(throwable));
	}

	/**
	 * Factory for generating ConsoleLogBackend instances.
	 */
	public static class ConsoleLogBackendFactory implements LogBackendFactory {
		@Override
		public LogBackend createLogBackend(String classLabel) {
			return new ConsoleLogBackend(classLabel);
		}
	}
}
