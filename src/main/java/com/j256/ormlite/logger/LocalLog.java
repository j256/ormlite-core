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
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");

	private final String className;
	private final Level level;
	private final PrintStream printStream;

	public LocalLog(String className) {
		// get the last part of the class name
		String[] parts = className.split("\\.");
		if (parts.length == 0) {
			this.className = className;
		} else {
			this.className = parts[parts.length - 1];
		}

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

	public boolean isTraceEnabled() {
		return isEnabled(Level.TRACE);
	}

	public boolean isDebugEnabled() {
		return isEnabled(Level.DEBUG);
	}

	public boolean isInfoEnabled() {
		return isEnabled(Level.INFO);
	}

	public boolean isWarnEnabled() {
		return isEnabled(Level.WARN);
	}

	public boolean isErrorEnabled() {
		return isEnabled(Level.ERROR);
	}

	public boolean isFatalEnabled() {
		return isEnabled(Level.FATAL);
	}

	public void trace(String msg) {
		printMessage(Level.TRACE, msg, null);
	}

	public void trace(String msg, Throwable throwable) {
		printMessage(Level.TRACE, msg, throwable);
	}

	public void debug(String msg) {
		printMessage(Level.DEBUG, msg, null);
	}

	public void debug(String msg, Throwable throwable) {
		printMessage(Level.DEBUG, msg, throwable);
	}

	public void info(String msg) {
		printMessage(Level.INFO, msg, null);
	}

	public void info(String msg, Throwable throwable) {
		printMessage(Level.INFO, msg, throwable);
	}

	public void warn(String msg) {
		printMessage(Level.WARN, msg, null);
	}

	public void warn(String msg, Throwable throwable) {
		printMessage(Level.WARN, msg, throwable);
	}

	public void error(String msg) {
		printMessage(Level.ERROR, msg, null);
	}

	public void error(String msg, Throwable throwable) {
		printMessage(Level.ERROR, msg, throwable);
	}

	public void fatal(String msg) {
		printMessage(Level.FATAL, msg, null);
	}

	public void fatal(String msg, Throwable throwable) {
		printMessage(Level.FATAL, msg, throwable);
	}

	/**
	 * Flush any IO to disk. For testing purposes.
	 */
	void flush() {
		printStream.flush();
	}

	private void printMessage(Level level, String message, Throwable throwable) {
		if (!this.level.isEnabled(level)) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(dateFormat.format(new Date()));
		sb.append(" [").append(level.name()).append("] ");
		sb.append(className).append(' ');
		sb.append(message);
		printStream.println(sb.toString());
		if (throwable != null) {
			throwable.printStackTrace(printStream);
		}
	}

	private boolean isEnabled(Level level) {
		return this.level.isEnabled(level);
	}

	/**
	 * Our log levels.
	 */
	public enum Level {
		TRACE(1),
		DEBUG(2),
		INFO(3),
		WARN(4),
		ERROR(5),
		FATAL(6),
		// end
		;

		private int level;

		private Level(int level) {
			this.level = level;
		}

		/**
		 * Return whether or not a level argument is enabled for this level value. So, Level.INFO.isEnabled(Level.WARN)
		 * returns true but Level.INFO.isEnabled(Level.DEBUG) returns false.
		 */
		public boolean isEnabled(Level otherLevel) {
			return level <= otherLevel.level;
		}
	}
}
