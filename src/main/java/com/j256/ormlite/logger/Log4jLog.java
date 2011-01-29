package com.j256.ormlite.logger;

import java.lang.reflect.Method;

/**
 * Class which implements our {@link com.j256.ormlite.logger.Log} interface by delegating to Apache Log4j via
 * reflection. We use reflection so we can avoid the dependency.
 * 
 * @author graywatson
 */
public class Log4jLog implements Log {

	private Object logger;

	private static Method getLoggerMethod;
	private static Method isEnabledForMethod;
	private static Object traceLevel;
	private static Object debugLevel;
	private static Object infoLevel;
	private static Object warningLevel;
	private static Object errorLevel;
	private static Object fatalLevel;

	private static Method logMethod;
	private static Method logThrowableMethod;

	public Log4jLog(String className) {
		if (getLoggerMethod == null) {
			findMethods();
		}
		if (getLoggerMethod != null) {
			try {
				logger = getLoggerMethod.invoke(null, className);
			} catch (Exception e) {
				// oh well, ignore the rest I guess
				logger = null;
			}
		}
	}

	public boolean isLevelEnabled(Level level) {
		return isEnabledFor(levelToJavaLevel(level));
	}

	public void log(Level level, String msg) {
		logMessage(levelToJavaLevel(level), msg);
	}

	public void log(Level level, String msg, Throwable t) {
		logMessage(levelToJavaLevel(level), msg, t);
	}

	private static void findMethods() {
		Class<?> clazz;
		try {
			clazz = Class.forName("org.apache.log4j.Logger");
		} catch (ClassNotFoundException e) {
			// oh well, bail
			return;
		}
		getLoggerMethod = getMethod(clazz, "getLogger", String.class);

		Class<?> priorityClazz;
		try {
			priorityClazz = Class.forName("org.apache.log4j.Priority");
		} catch (ClassNotFoundException e) {
			// oh well, bail
			return;
		}
		isEnabledForMethod = getMethod(clazz, "isEnabledFor", priorityClazz);
		Class<?> levelClazz;
		try {
			levelClazz = Class.forName("org.apache.log4j.Level");
		} catch (ClassNotFoundException e) {
			// oh well, bail
			return;
		}
		traceLevel = getLevelField(levelClazz, "TRACE");
		debugLevel = getLevelField(levelClazz, "DEBUG");
		infoLevel = getLevelField(levelClazz, "INFO");
		warningLevel = getLevelField(levelClazz, "WARN");
		errorLevel = getLevelField(levelClazz, "ERROR");
		fatalLevel = getLevelField(levelClazz, "FATAL");

		logMethod = getMethod(clazz, "log", priorityClazz, Object.class);
		logThrowableMethod = getMethod(clazz, "log", priorityClazz, Object.class, Throwable.class);
	}

	private boolean isEnabledFor(Object level) {
		if (logger != null) {
			try {
				return (Boolean) isEnabledForMethod.invoke(logger, level);
			} catch (Exception e) {
				// oh well, return false
			}
		}
		return false;
	}

	private void logMessage(Object level, String message) {
		if (logger != null && logMethod != null) {
			try {
				logMethod.invoke(logger, level, message);
			} catch (Exception e) {
				// oh well, just skip it
			}
		}
	}

	private void logMessage(Object level, String message, Throwable t) {
		if (logger != null && logThrowableMethod != null) {
			try {
				logThrowableMethod.invoke(logger, level, message, (Throwable) t);
			} catch (Exception e) {
				// oh well, just skip it
			}
		}
	}

	private static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		try {
			return clazz.getMethod(methodName, parameterTypes);
		} catch (Exception e) {
			return null;
		}
	}

	private static Object getLevelField(Class<?> clazz, String fieldName) {
		try {
			return clazz.getField(fieldName).get(null);
		} catch (Exception e) {
			return null;
		}
	}

	private Object levelToJavaLevel(com.j256.ormlite.logger.Log.Level level) {
		switch (level) {
			case TRACE :
				return traceLevel;
			case DEBUG :
				return debugLevel;
			case INFO :
				return infoLevel;
			case WARNING :
				return warningLevel;
			case ERROR :
				return errorLevel;
			case FATAL :
				return fatalLevel;
			default :
				return infoLevel;
		}
	}
}
