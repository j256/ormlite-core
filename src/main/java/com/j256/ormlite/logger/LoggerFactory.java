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
		Log log;
		if (checkClass("org.apache.commons.logging.LogFactory")) {
			log = new CommonsLoggingLog(className);
		} else if (checkClass("org.apache.log4j.Logger")) {
			log = new Log4jLog(className);
		} else {
			log = new LocalLog(className);
		}
		return new Logger(log);
	}

	private static boolean checkClass(String classPath) {
		try {
			Class.forName(classPath);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
