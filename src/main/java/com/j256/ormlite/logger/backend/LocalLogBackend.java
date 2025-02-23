package com.j256.ormlite.logger.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;
import com.j256.ormlite.logger.LogBackendType;
import com.j256.ormlite.logger.LoggerConstants;
import com.j256.ormlite.logger.PropertyUtils;
import com.j256.ormlite.logger.PropertyUtils.PatternLevel;

/**
 * Simple log backend that uses logging classes if they are not available.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * <p>
 * You can set the log level by setting the System.setProperty({@link LoggerConstants#LOCAL_LOG_LEVEL_PROPERTY},
 * "TRACE"). Acceptable values are: TRACE, DEBUG, INFO, WARN, ERROR, and FATAL. You can also redirect the log to a file
 * by setting the System.setProperty(LocalLogBackend.LOCAL_LOG_FILE_PROPERTY, "log.out"). Otherwise, log output will go
 * to stdout.
 * </p>
 * 
 * <p>
 * You can also set the log levels for your code packages with the simplelogging properties file (name defined in
 * {@link LoggerConstants#PROPERTIES_CONFIG_FILE}. The level values are: TRACE, DEBUG, INFO, WARN, ERROR, and FATAL. The
 * lines in the config file are in the form:
 * </p>
 * 
 * <pre>
 * # line format: regex-pattern = Level
 * localog.com\.foo\.yourclass.*=DEBUG
 * localog.com\.foo\.yourclass\.BaseMappedStatement=TRACE
 * localog.com\.foo\.yourclass\.MappedCreate=TRACE
 * localog.com\.foo\.yourclass\.StatementExecutor = TRACE
 * </pre>
 * 
 * @author graywatson
 */
public class LocalLogBackend implements LogBackend {

	private static final Level DEFAULT_LEVEL = Level.DEBUG;
	// used with clone()
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
	private static PrintStream printStream;
	private static final List<PatternLevel> classLevels;

	private final String className;
	private final Level level;

	static {
		classLevels = PropertyUtils.readLocalLogPatterns(LogBackendType.NULL);

		/*
		 * We need to do this here otherwise each logger has their own open PrintStream to the file and the messages can
		 * overlap. Not good.
		 */
		String logPath = System.getProperty(LoggerConstants.LOCAL_LOG_FILE_PROPERTY);
		openLogFile(logPath);
	}

	public LocalLogBackend(String className) {
		// get the last part of the class name
		int index = className.lastIndexOf('.');
		if (index < 0 || index == className.length() - 1) {
			this.className = className;
		} else {
			this.className = className.substring(index + 1);
		}

		Level level = null;
		if (classLevels != null) {
			for (PatternLevel patternLevel : classLevels) {
				if (patternLevel.getPattern().matcher(className).matches()) {
					Level levelWithPattern = patternLevel.getLevel();
					// if level has not been set or the level does not cover the pattern level
					if (level == null || !levelWithPattern.isEnabled(level)) {
						level = levelWithPattern;
					}
				}
			}
		}

		if (level == null) {
			// see if we have a level set
			String levelName = System.getProperty(LoggerConstants.LOCAL_LOG_LEVEL_PROPERTY);
			if (levelName == null) {
				level = DEFAULT_LEVEL;
			} else {
				level = Level.fromString(levelName);
				if (level == null) {
					throw new IllegalArgumentException("Level '" + levelName + "' set in '"
							+ LoggerConstants.LOCAL_LOG_LEVEL_PROPERTY + "' system property is invalid");
				}
			}
		}
		this.level = level;
	}

	/**
	 * Reopen the associated static logging stream. Set to null to redirect to System.out.
	 */
	public static void openLogFile(String logPath) {
		if (logPath == null) {
			printStream = System.out;
		} else {
			try {
				printStream = new PrintStream(new File(logPath));
			} catch (FileNotFoundException e) {
				throw new IllegalArgumentException("Log file " + logPath + " was not found", e);
			}
		}
	}

	@Override
	public boolean isLevelEnabled(Level level) {
		return this.level.isEnabled(level);
	}

	@Override
	public void log(Level level, String msg) {
		printMessage(level, msg, null);
	}

	@Override
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
		DateFormat dateFormat = (DateFormat) DATE_FORMAT.clone();
		sb.append(dateFormat.format(new Date()));
		sb.append(" [").append(level.name()).append("] ");
		sb.append(className).append(' ');
		sb.append(message);
		printStream.println(sb.toString());
		if (throwable != null) {
			throwable.printStackTrace(printStream);
		}
	}

	/**
	 * Internal factory for LocalLogBackend instances. This can be used with the
	 * LoggerFactory.setLogBackendFactory(LogBackendFactory) method to send all log messages to a file.
	 */
	public static class LocalLogBackendFactory implements LogBackendFactory {

		private final AtomicReference<String> queuedWarning = new AtomicReference<String>();

		public LocalLogBackendFactory() {
			// no-arg
		}

		public LocalLogBackendFactory(String queuedWarning) {
			this.queuedWarning.set(queuedWarning);
		}

		@Override
		public boolean isAvailable() {
			// always available
			return true;
		}

		@Override
		public LogBackend createLogBackend(String classLabel) {
			LocalLogBackend backend = new LocalLogBackend(classLabel);
			String queuedWarning = this.queuedWarning.getAndSet(null);
			if (queuedWarning != null) {
				// if we had a queued warning message then emit it the first time we create a backend
				backend.log(Level.WARNING, queuedWarning);
			}
			return backend;
		}
	}
}
