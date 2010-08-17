package com.j256.ormlite.logger;

/**
 * Factory that creates {@link Logger} instances.
 */
public class LoggerFactory {

	private static LogType logType;

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
		Log log = null;

		if (logType == null) {
			logType = findLogType();
		}

		switch (logType) {
			case COMMONS_LOGGING :
				// you can comment out this line if you need to remove the CommonsLoggingLog class
				log = new CommonsLoggingLog(className);
				break;
			case LOG4J :
				// you can comment out this line if you need to remove the Log4jLog class
				log = new Log4jLog(className);
				break;
			case ANDROID :
				// you can comment out this line if you need to remove the AndroidLog class
				log = new AndroidLog(className);
				break;
			case LOCAL :
				log = new LocalLog(className);
				break;
		}
		return new Logger(log);
	}

	public static String getSimpleClassName(String className) {
		// get the last part of the class name
		String[] parts = className.split("\\.");
		if (parts.length == 0) {
			return className;
		} else {
			return parts[parts.length - 1];
		}
	}

	private static LogType findLogType() {
		if (checkClass("org.apache.commons.logging.LogFactory")) {
			return LogType.COMMONS_LOGGING;
		} else if (checkClass("org.apache.log4j.Logger")) {
			return LogType.LOG4J;
		} else if (checkClass("android.util.Log")) {
			return LogType.ANDROID;
		} else {
			return LogType.LOCAL;
		}
	}

	private static boolean checkClass(String classPath) {
		try {
			Class.forName(classPath);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	private enum LogType {
		COMMONS_LOGGING,
		LOG4J,
		ANDROID,
		LOCAL,
		// end
		;
	}
}
