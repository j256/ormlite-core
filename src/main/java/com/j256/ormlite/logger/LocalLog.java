package com.j256.ormlite.logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class which implements our {@link Log} interface so we can bypass external logging classes if they are not available.
 * 
 * <p>
 * You can set the log level by setting the System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "trace"). Acceptable
 * values are: TRACE, DEBUG, INFO, WARN, ERROR, and FATAL. You can also redirect the log to a file by setting the
 * System.setProperty(LocalLog.LOCAL_LOG_FILE_PROPERTY, "log.out"). Otherwise, log output will go to stdout.
 * </p>
 * 
 * @author graywatson
 */
public class LocalLog implements Log {

	public final static String LOCAL_LOG_LEVEL_PROPERTY = "com.j256.ormlite.logger.level";
	public final static String LOCAL_LOG_FILE_PROPERTY = "com.j256.ormlite.logger.file";

	private final static Level DEFAULT_LEVEL = Level.DEBUG;
	private static ThreadLocal<DateFormat> dateFormatThreadLocal = new ThreadLocal<DateFormat>();

	private final String className;
	private final Level level;
	private final PrintStream printStream;

	public LocalLog(String className) {
		// get the last part of the class name
		this.className = LoggerFactory.getSimpleClassName(className);

		// see if we have a level set
		String levelName = System.getProperty(LOCAL_LOG_LEVEL_PROPERTY);
		if (levelName == null) {
			this.level = DEFAULT_LEVEL;
		} else {
			Level matchedLevel;
			try {
				matchedLevel = Level.valueOf(levelName.toUpperCase());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Level '" + levelName + "' was not found", e);
			}
			this.level = matchedLevel;
		}

		// see if stuff goes to stdout or a file
		String logPath = System.getProperty(LOCAL_LOG_FILE_PROPERTY);
		if (logPath == null) {
			this.printStream = System.out;
		} else {
			try {
				this.printStream = new PrintStream(new File(logPath));
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("Log file " + logPath + " was not found", e);
			}
		}
	}

	public boolean isLevelEnabled(Level level) {
		return this.level.isEnabled(level);
	}

	public void log(Level level, String msg) {
		printMessage(level, msg, null);
	}

	public void log(Level level, String msg, Throwable throwable) {
		printMessage(level, msg, throwable);
	}

	/**
	 * Flush any IO to disk. For testing purposes.
	 */
	void flush() {
		printStream.flush();
	}

	private void printMessage(Level level, String message, Throwable throwable) {
		if (!isLevelEnabled(level)) {
			return;
		}
		StringBuilder sb = new StringBuilder(128);
		DateFormat dateFormat = dateFormatThreadLocal.get();
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
			dateFormatThreadLocal.set(dateFormat);
		}
		sb.append(dateFormat.format(new Date()));
		sb.append(" [").append(level.name()).append("] ");
		sb.append(className).append(' ');
		sb.append(message);
		printStream.println(sb.toString());
		if (throwable != null) {
			throwable.printStackTrace(printStream);
		}
	}
}
