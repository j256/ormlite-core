package com.j256.ormlite.logger;

/**
 * Class which logs to java.util.log. This is not detected by default so is here for now as a reference implementation.
 * 
 * @author graywatson
 */
public class JavaUtilLog implements Log {

	private final java.util.logging.Logger logger;

	public JavaUtilLog(String className) {
		this.logger = java.util.logging.Logger.getLogger(className);
	}

	public boolean isLevelEnabled(com.j256.ormlite.logger.Log.Level level) {
		return logger.isLoggable(levelToJavaLevel(level));
	}

	public void log(com.j256.ormlite.logger.Log.Level level, String msg) {
		logger.log(levelToJavaLevel(level), msg);
	}

	public void log(com.j256.ormlite.logger.Log.Level level, String msg, Throwable throwable) {
		logger.log(levelToJavaLevel(level), msg, throwable);
	}

	private java.util.logging.Level levelToJavaLevel(com.j256.ormlite.logger.Log.Level level) {
		switch (level) {
			case TRACE :
				return java.util.logging.Level.FINER;
			case DEBUG :
				return java.util.logging.Level.FINE;
			case INFO :
				return java.util.logging.Level.INFO;
			case WARNING :
				return java.util.logging.Level.WARNING;
			case ERROR :
				return java.util.logging.Level.SEVERE;
			case FATAL :
				return java.util.logging.Level.SEVERE;
			default :
				return java.util.logging.Level.INFO;
		}
	}
}
