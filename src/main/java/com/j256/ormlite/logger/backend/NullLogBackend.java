package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;
import com.j256.ormlite.logger.LoggerFactory;

/**
 * Log backend that ignores all log requests.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class NullLogBackend implements LogBackend {

	public NullLogBackend() {
		// no-op
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		// never enabled
		return false;
	}

	@Override
	public void log(Level level, String msg) {
		// no-op
	}

	@Override
	public void log(Level level, String msg, Throwable throwable) {
		// no-op
	}

	/**
	 * Factory for generating NullLogBackend instances. This can be used with the
	 * {@link LoggerFactory#setLogBackendFactory(LogBackendFactory)} method to completely disable all logging.
	 */
	public static class NullLogBackendFactory implements LogBackendFactory {

		private static final NullLogBackend singleton = new NullLogBackend();

		@Override
		public LogBackend createLogBackend(String classLabel) {
			return singleton;
		}
	}
}
