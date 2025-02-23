package com.j256.ormlite.logger.backend;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;

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
	 * LoggerFactory.setLogBackendFactory(LogBackendFactory) method to completely disable all logging.
	 */
	public static class NullLogBackendFactory implements LogBackendFactory {

		private static final NullLogBackendFactory singletonFactory = new NullLogBackendFactory();
		private static final NullLogBackend singletonBackend = new NullLogBackend();

		/**
		 * Return singleton of our factory.
		 */
		public static NullLogBackendFactory getSingleton() {
			return singletonFactory;
		}

		@Override
		public boolean isAvailable() {
			// always available
			return true;
		}

		@Override
		public LogBackend createLogBackend(String classLabel) {
			return singletonBackend;
		}
	}
}
