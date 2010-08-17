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
	private static Object warnLevel;
	private static Object errorLevel;
	private static Object fatalLevel;

	private static Method traceMethod;
	private static Method traceThrowableMethod;
	private static Method debugMethod;
	private static Method debugThrowableMethod;
	private static Method infoMethod;
	private static Method infoThrowableMethod;
	private static Method warnMethod;
	private static Method warnThrowableMethod;
	private static Method errorMethod;
	private static Method errorThrowableMethod;
	private static Method fatalMethod;
	private static Method fatalThrowableMethod;

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

	public boolean isTraceEnabled() {
		return isEnabledFor(traceLevel);
	}

	public boolean isDebugEnabled() {
		return isEnabledFor(debugLevel);
	}

	public boolean isInfoEnabled() {
		return isEnabledFor(infoLevel);
	}

	public boolean isWarnEnabled() {
		return isEnabledFor(warnLevel);
	}

	public boolean isErrorEnabled() {
		return isEnabledFor(errorLevel);
	}

	public boolean isFatalEnabled() {
		return isEnabledFor(fatalLevel);
	}

	public void trace(String msg) {
		logMessage(traceMethod, msg);
	}

	public void trace(String msg, Throwable t) {
		logMessage(traceThrowableMethod, msg, t);
	}

	public void debug(String msg) {
		logMessage(debugMethod, msg);
	}

	public void debug(String msg, Throwable t) {
		logMessage(debugThrowableMethod, msg, t);
	}

	public void info(String msg) {
		logMessage(infoMethod, msg);
	}

	public void info(String msg, Throwable t) {
		logMessage(infoThrowableMethod, msg, t);
	}

	public void warn(String msg) {
		logMessage(warnMethod, msg);
	}

	public void warn(String msg, Throwable t) {
		logMessage(warnThrowableMethod, msg, t);
	}

	public void error(String msg) {
		logMessage(errorMethod, msg);
	}

	public void error(String msg, Throwable t) {
		logMessage(errorThrowableMethod, msg, t);
	}

	public void fatal(String msg) {
		logMessage(fatalMethod, msg);
	}

	public void fatal(String msg, Throwable t) {
		logMessage(fatalThrowableMethod, msg, t);
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
		warnLevel = getLevelField(levelClazz, "WARN");
		errorLevel = getLevelField(levelClazz, "ERROR");
		fatalLevel = getLevelField(levelClazz, "FATAL");

		traceMethod = getMethod(clazz, "trace", Object.class);
		traceThrowableMethod = getMethod(clazz, "trace", Object.class, Throwable.class);
		debugMethod = getMethod(clazz, "debug", Object.class);
		debugThrowableMethod = getMethod(clazz, "debug", Object.class, Throwable.class);
		infoMethod = getMethod(clazz, "info", Object.class);
		infoThrowableMethod = getMethod(clazz, "info", Object.class, Throwable.class);
		warnMethod = getMethod(clazz, "warn", Object.class);
		warnThrowableMethod = getMethod(clazz, "warn", Object.class, Throwable.class);
		errorMethod = getMethod(clazz, "error", Object.class);
		errorThrowableMethod = getMethod(clazz, "error", Object.class, Throwable.class);
		fatalMethod = getMethod(clazz, "fatal", Object.class);
		fatalThrowableMethod = getMethod(clazz, "fatal", Object.class, Throwable.class);
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

	private void logMessage(Method method, String message) {
		if (logger != null) {
			try {
				method.invoke(logger, message);
			} catch (Exception e) {
				// oh well, just skip it
			}
		}
	}

	private void logMessage(Method method, String message, Throwable t) {
		if (logger != null) {
			try {
				method.invoke(logger, message, (Throwable) t);
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
}
