package com.j256.ormlite.logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utilities class for dealing with the simplelogging properties file.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 * 
 * @author graywatson
 */
public class PropertyUtils {

	// properties that can be set
	static final String BACKEND_TYPE_CLASS_PROPERTY = "backend";
	static final String DISCOVERY_ORDER_PROPERTY = "dicovery.order";
	static final String GLOBAL_LEVEL_PROPERTY = "global.level";
	static final String LOCAL_LOG_PROPERTY_PREFIX = "locallog.";

	// other constants
	static final String GLOBAL_LEVEL_NULL_VALUE = "NULL";

	private static volatile List<String[]> propertyEntries;

	/** properties path that we will read from, exposed for testing purposes */
	private static InputStream propertiesInputStream;

	/**
	 * Read the backend property from the properties file returning the backend type or null if none.
	 */
	public static String readBackendTypeClassProperty(LogBackendFactory defaultBackendFactory) {
		List<String[]> props = getProperties(defaultBackendFactory);
		for (String[] entry : props) {
			if (BACKEND_TYPE_CLASS_PROPERTY.equals(entry[0])) {
				return entry[1];
			}
		}
		return null;
	}

	/**
	 * Read the backend type order property from the properties file. Returns null if none.
	 */
	public static LogBackendType[] readDiscoveryOrderProperty(LogBackendFactory defaultBackendFactory) {
		List<String[]> props = getProperties(defaultBackendFactory);
		for (String[] entry : props) {
			if (DISCOVERY_ORDER_PROPERTY.equals(entry[0])) {
				return processDiscoveryOrderValue(entry[1], defaultBackendFactory);
			}
		}
		return null;
	}

	/**
	 * Process a list of the discovery order backend types (enum names) and return them as an array.
	 * 
	 * @return null if none configured.
	 */
	static LogBackendType[] processDiscoveryOrderValue(String value, LogBackendFactory defaultBackendFactory) {
		if (value == null) {
			return null;
		}
		List<LogBackendType> typeList = null;
		String[] parts = value.split(",");
		for (String part : parts) {
			if (part.isEmpty()) {
				continue;
			}
			try {
				if (typeList == null) {
					typeList = new ArrayList<>(parts.length);
				}
				typeList.add(LogBackendType.valueOf(part));
			} catch (IllegalArgumentException iae) {
				logWarning(defaultBackendFactory,
						"unknown backend type value '" + part + "' in discovery order property", null);
			}
		}
		if (typeList == null || typeList.isEmpty()) {
			return null;
		} else {
			return typeList.toArray(new LogBackendType[typeList.size()]);
		}
	}

	/**
	 * Read the global level property from the properties file if available and call
	 * {@link Logger#setGlobalLogLevel(Level)} if available.
	 */
	public static void assignGlobalLevelFromProperty(LogBackendFactory defaultBackendFactory) {
		List<String[]> props = getProperties(defaultBackendFactory);
		for (String[] entry : props) {
			if (!GLOBAL_LEVEL_PROPERTY.equals(entry[0])) {
				continue;
			}
			String levelStr = entry[1];
			Level level;
			if (GLOBAL_LEVEL_NULL_VALUE.equals(levelStr)) {
				level = null;
			} else {
				level = Level.fromString(levelStr);
				if (level == null) {
					logWarning(defaultBackendFactory,
							"Global level '" + levelStr + "' was not valid in properties file", null);
					continue;
				}
			}
			Logger.setGlobalLogLevel(level);
		}
	}

	/**
	 * Read the local log level patterns for the loggers or null if none.
	 */
	public static List<PatternLevel> readLocalLogPatterns(LogBackendFactory defaultBackendFactory) {
		List<String[]> props = getProperties(defaultBackendFactory);
		List<PatternLevel> patternLevels = null;
		for (String[] entry : props) {
			if (!entry[0].startsWith(LOCAL_LOG_PROPERTY_PREFIX)) {
				continue;
			}
			String patternStr = entry[0].substring(LOCAL_LOG_PROPERTY_PREFIX.length());
			if (patternStr.isEmpty()) {
				logWarning(defaultBackendFactory, "log pattern is empty in local-log properties: " + entry[0], null);
				continue;
			}
			Level level = Level.fromString(entry[1]);
			if (level == null) {
				logWarning(defaultBackendFactory, "level '" + entry[1] + "' is not valid in local-log properties",
						null);
				continue;
			}
			Pattern pattern = Pattern.compile(patternStr);
			if (patternLevels == null) {
				patternLevels = new ArrayList<>();
			}
			patternLevels.add(new PatternLevel(pattern, level));
		}
		return patternLevels;
	}

	/**
	 * For testing purposes.
	 */
	public static void setPropertiesInputStream(InputStream propertiesInputStream) {
		PropertyUtils.propertiesInputStream = propertiesInputStream;
	}

	private static List<String[]> getProperties(LogBackendFactory defaultBackend) {
		if (propertyEntries == null) {
			propertyEntries = readPropertiesFile(defaultBackend);
		}
		return propertyEntries;
	}

	/**
	 * Clear the loaded properties. Here for testing purposes.
	 */
	static void clearProperties() {
		propertyEntries = null;
	}

	/**
	 * Read in the properties.
	 */
	static List<String[]> readPropertiesFile(LogBackendFactory defaultBackendFactory) {
		List<String[]> propertyEntries = new ArrayList<>();
		// now try to load in properties
		InputStream stream = propertiesInputStream;
		if (stream == null) {
			stream = LoggerFactory.class.getResourceAsStream(LoggerConstants.PROPERTIES_CONFIG_FILE);
			if (stream == null) {
				// file not found
				return Collections.emptyList();
			}
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream));) {
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				if (line.length() == 0 || line.charAt(0) == '#') {
					continue;
				}
				String[] parts = line.split("=");
				if (parts.length != 2) {
					logWarning(defaultBackendFactory,
							"Line from properties file " + LoggerConstants.PROPERTIES_CONFIG_FILE
									+ " is not in the format of 'field = value': " + line,
							null);
					continue;
				}
				parts[0] = trimString(parts[0]);
				parts[1] = trimString(parts[1]);
				propertyEntries.add(parts);
			}
			return propertyEntries;
		} catch (IOException ioe) {
			logWarning(defaultBackendFactory,
					"exception thrown while loading in properties from " + LoggerConstants.PROPERTIES_CONFIG_FILE, ioe);
			return Collections.emptyList();
		}
	}

	private static String trimString(String str) {
		if (str == null || str.isEmpty()) {
			// may not get here but let's be careful out there
			return str;
		} else {
			return str.trim();
		}
	}

	private static void logWarning(LogBackendFactory defaultBackendFactory, String msg, Throwable th) {
		LogBackend backend = defaultBackendFactory.createLogBackend(LoggerFactory.class.getName());
		if (th == null) {
			backend.log(Level.WARNING, msg);
		} else {
			backend.log(Level.WARNING, msg, th);
		}
	}

	/**
	 * Holder for the regex pattern and the associated level.
	 */
	public static class PatternLevel {
		private final Pattern pattern;
		private final Level level;

		public PatternLevel(Pattern pattern, Level level) {
			this.pattern = pattern;
			this.level = level;
		}

		public Pattern getPattern() {
			return pattern;
		}

		public Level getLevel() {
			return level;
		}
	}
}
