package com.j256.ormlite.logger.backend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import com.j256.ormlite.logger.Level;
import com.j256.ormlite.logger.LogBackend;
import com.j256.ormlite.logger.LogBackendFactory;
import com.j256.ormlite.logger.LoggerConstants;
import com.j256.ormlite.logger.LoggerFactory;

/**
 * Log backend that uses logging classes if they are not available.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * <p>
 * You can set the log level by setting the System.setProperty(LocalLogBackend.LOCAL_LOG_LEVEL_PROPERTY, "trace").
 * Acceptable values are: TRACE, DEBUG, INFO, WARN, ERROR, and FATAL. You can also redirect the log to a file by setting
 * the System.setProperty(LocalLogBackend.LOCAL_LOG_FILE_PROPERTY, "log.out"). Otherwise, log output will go to stdout.
 * </p>
 * 
 * <p>
 * It also supports a properties file (name defined in {@link LoggerConstants#LOCAL_LOG_PROPERTIES_FILE} which contains
 * lines such as:
 * </p>
 * 
 * <pre>
 * # line format: regex-pattern = Level
 * com\.foo\.yourclass.*=DEBUG
 * com\.foo\.yourclass\.BaseMappedStatement=TRACE
 * com\.foo\.yourclass\.MappedCreate=TRACE
 * com\.foo\.yourclass\.StatementExecutor=TRACE
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
		InputStream stream = LocalLogBackend.class.getResourceAsStream(LoggerConstants.LOCAL_LOG_PROPERTIES_FILE);
		List<PatternLevel> levels;
		try {
			levels = readLevelResourceFile(stream);
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// ignored
			}
		}
		classLevels = levels;

		/*
		 * We need to do this here otherwise each logger has their own open PrintStream to the file and the messages can
		 * overlap. Not good.
		 */
		String logPath = System.getProperty(LoggerConstants.LOCAL_LOG_FILE_PROPERTY);
		openLogFile(logPath);
	}

	public LocalLogBackend(String className) {
		// get the last part of the class name
		this.className = LoggerFactory.getSimpleClassName(className);

		Level level = null;
		if (classLevels != null) {
			for (PatternLevel patternLevel : classLevels) {
				if (patternLevel.pattern.matcher(className).matches()) {
					// if level has not been set or the level is lower...
					if (level == null || patternLevel.level.ordinal() < level.ordinal()) {
						level = patternLevel.level;
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
				Level matchedLevel;
				try {
					// try default locale first
					matchedLevel = Level.valueOf(levelName.toUpperCase());
				} catch (IllegalArgumentException e1) {
					try {
						// then try english locale
						matchedLevel = Level.valueOf(levelName.toUpperCase(Locale.ENGLISH));
					} catch (IllegalArgumentException e2) {
						throw new IllegalArgumentException("Level '" + levelName + "' was not found", e2);
					}
				}
				level = matchedLevel;
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

	/**
	 * Read in our levels from our configuration file.
	 */
	static List<PatternLevel> readLevelResourceFile(InputStream stream) {
		if (stream == null) {
			return null;
		}
		try {
			return configureClassLevels(stream);
		} catch (IOException e) {
			System.err.println("IO exception reading the log properties file '"
					+ LoggerConstants.LOCAL_LOG_PROPERTIES_FILE + "': " + e);
			return null;
		}

	}

	private static List<PatternLevel> configureClassLevels(InputStream stream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		List<PatternLevel> list = new ArrayList<PatternLevel>();
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			// skip empty lines or comments
			if (line.length() == 0 || line.charAt(0) == '#') {
				continue;
			}
			String[] parts = line.split("=");
			if (parts.length != 2) {
				System.err.println("Line is not in the format of 'pattern = level': " + line);
				continue;
			}
			Pattern pattern = Pattern.compile(parts[0].trim());
			Level level;
			try {
				level = Level.valueOf(parts[1].trim());
			} catch (IllegalArgumentException e) {
				System.err.println("Level '" + parts[1] + "' was not found");
				continue;
			}
			list.add(new PatternLevel(pattern, level));
		}
		return list;
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
	 * {@link LoggerFactory#setLogBackendFactory(LogBackendFactory)} method to send all log messages to a file.
	 */
	public static class LocalLogBackendFactory implements LogBackendFactory {

		private final AtomicReference<String> queuedWarning = new AtomicReference<String>();

		public LocalLogBackendFactory() {
		}

		public LocalLogBackendFactory(String queuedWarning) {
			this.queuedWarning.set(queuedWarning);
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

	private static class PatternLevel {
		final Pattern pattern;
		final Level level;

		public PatternLevel(Pattern pattern, Level level) {
			this.pattern = pattern;
			this.level = level;
		}
	}
}
