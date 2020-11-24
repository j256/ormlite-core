package com.j256.ormlite.logger;

/**
 * Class which implements our {@link com.j256.ormlite.logger.Log} interface by delegating to Apache Log4j.
 * 
 * @author graywatson
 */
public class Log4jLog implements Log {

	private final org.apache.log4j.Logger logger;

	public Log4jLog(String className) {
		this.logger = org.apache.log4j.Logger.getLogger(className);
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		return logger.isEnabledFor(levelToLog4jLevel(level));
	}

	@Override
	public void log(Level level, String msg) {
		logger.log(levelToLog4jLevel(level), msg);
	}

	@Override
	public void log(Level level, String msg, Throwable t) {
		logger.log(levelToLog4jLevel(level), msg, t);
	}

	private org.apache.log4j.Level levelToLog4jLevel(com.j256.ormlite.logger.Log.Level level) {
		switch (level) {
			case TRACE:
				return org.apache.log4j.Level.TRACE;
			case DEBUG:
				return org.apache.log4j.Level.DEBUG;
			case INFO:
				return org.apache.log4j.Level.INFO;
			case WARNING:
				return org.apache.log4j.Level.WARN;
			case ERROR:
				return org.apache.log4j.Level.ERROR;
			case FATAL:
				return org.apache.log4j.Level.FATAL;
			default:
				return org.apache.log4j.Level.INFO;
		}
	}
}
