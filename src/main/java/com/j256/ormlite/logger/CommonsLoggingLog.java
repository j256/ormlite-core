package com.j256.ormlite.logger;

import java.lang.reflect.Method;

/**
 * Class which implements our {@link com.j256.ormlite.logger.Log} interface by delegating to the Apache commons logging
 * classes via reflection. We use reflection so we can avoid the dependency. Yes we are delegating to a delegating
 * class.
 * 
 * @author graywatson
 */
public class CommonsLoggingLog implements Log {

	private Object log;

	private static Method getLogMethod;
	private static Method isTraceEnabledMethod;
	private static Method isDebugEnabledMethod;
	private static Method isInfoEnabledMethod;
	private static Method isWarnEnabledMethod;
	private static Method isErrorEnabledMethod;
	private static Method isFatalEnabledMethod;

	private static Method traceMethod;
	private static Method traceThrowableMethod;
	private static Method debugMethod;
	private static Method debugThrowableMethod;
	private static Method infoMethod;
	private static Method infoThrowableMethod;
	private static Method warningMethod;
	private static Method warningThrowableMethod;
	private static Method errorMethod;
	private static Method errorThrowableMethod;
	private static Method fatalMethod;
	private static Method fatalThrowableMethod;

	public CommonsLoggingLog(String className) {
		if (getLogMethod == null) {
			findMethods();
		}
		if (getLogMethod != null) {
			try {
				log = getLogMethod.invoke(null, className);
			} catch (Exception e) {
				// oh well, ignore the rest I guess
				log = null;
			}
		}
	}

	public boolean isLevelEnabled(Level level) {
		switch (level) {
			case TRACE :
				return isLevelEnabled(isTraceEnabledMethod);
			case DEBUG :
				return isLevelEnabled(isDebugEnabledMethod);
			case INFO :
				return isLevelEnabled(isInfoEnabledMethod);
			case WARNING :
				return isLevelEnabled(isWarnEnabledMethod);
			case ERROR :
				return isLevelEnabled(isErrorEnabledMethod);
			case FATAL :
				return isLevelEnabled(isFatalEnabledMethod);
			default :
				return isLevelEnabled(isInfoEnabledMethod);
		}
	}

	public void log(Level level, String msg) {
		switch (level) {
			case TRACE :
				logMessage(traceMethod, msg);
				break;
			case DEBUG :
				logMessage(debugMethod, msg);
				break;
			case INFO :
				logMessage(infoMethod, msg);
				break;
			case WARNING :
				logMessage(warningMethod, msg);
				break;
			case ERROR :
				logMessage(errorMethod, msg);
				break;
			case FATAL :
				logMessage(fatalMethod, msg);
				break;
			default :
				logMessage(infoMethod, msg);
				break;
		}
	}

	public void log(Level level, String msg, Throwable t) {
		switch (level) {
			case TRACE :
				logMessage(traceThrowableMethod, msg, t);
				break;
			case DEBUG :
				logMessage(debugThrowableMethod, msg, t);
				break;
			case INFO :
				logMessage(infoThrowableMethod, msg, t);
				break;
			case WARNING :
				logMessage(warningThrowableMethod, msg, t);
				break;
			case ERROR :
				logMessage(errorThrowableMethod, msg, t);
				break;
			case FATAL :
				logMessage(fatalThrowableMethod, msg, t);
				break;
			default :
				logMessage(infoMethod, msg, t);
				break;
		}
	}

	private static void findMethods() {
		Class<?> clazz;
		try {
			clazz = Class.forName("org.apache.commons.logging.LogFactory");
		} catch (ClassNotFoundException e) {
			// oh well, bail
			return;
		}
		getLogMethod = getMethod(clazz, "getLog", String.class);

		try {
			clazz = Class.forName("org.apache.commons.logging.Log");
		} catch (ClassNotFoundException e) {
			// oh well, bail
			return;
		}
		isTraceEnabledMethod = getMethod(clazz, "isTraceEnabled");
		isDebugEnabledMethod = getMethod(clazz, "isDebugEnabled");
		isInfoEnabledMethod = getMethod(clazz, "isInfoEnabled");
		isWarnEnabledMethod = getMethod(clazz, "isWarnEnabled");
		isErrorEnabledMethod = getMethod(clazz, "isErrorEnabled");
		isFatalEnabledMethod = getMethod(clazz, "isFatalEnabled");

		traceMethod = getMethod(clazz, "trace", Object.class);
		traceThrowableMethod = getMethod(clazz, "trace", Object.class, Throwable.class);
		debugMethod = getMethod(clazz, "debug", Object.class);
		debugThrowableMethod = getMethod(clazz, "debug", Object.class, Throwable.class);
		infoMethod = getMethod(clazz, "info", Object.class);
		infoThrowableMethod = getMethod(clazz, "info", Object.class, Throwable.class);
		warningMethod = getMethod(clazz, "warn", Object.class);
		warningThrowableMethod = getMethod(clazz, "warn", Object.class, Throwable.class);
		errorMethod = getMethod(clazz, "error", Object.class);
		errorThrowableMethod = getMethod(clazz, "error", Object.class, Throwable.class);
		fatalMethod = getMethod(clazz, "fatal", Object.class);
		fatalThrowableMethod = getMethod(clazz, "fatal", Object.class, Throwable.class);
	}

	private boolean isLevelEnabled(Method method) {
		if (log != null) {
			try {
				return (Boolean) method.invoke(log);
			} catch (Exception e) {
				// oh well
			}
		}
		return false;
	}

	private void logMessage(Method method, String message) {
		if (log != null) {
			try {
				method.invoke(log, message);
			} catch (Exception e) {
				// oh well, just skip it
			}
		}
	}

	private void logMessage(Method method, String message, Throwable t) {
		if (log != null) {
			try {
				method.invoke(log, message, (Throwable) t);
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
}
