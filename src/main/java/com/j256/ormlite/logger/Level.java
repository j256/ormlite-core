package com.j256.ormlite.logger;

/**
 * Level of log messages being sent.
 *
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public enum Level {
	/** for tracing messages that are very verbose, such as those from the protocol level */
	TRACE(1),
	/** messages suitable for debugging purposes that aren't protocol verbose but provide more than info level */
	DEBUG(2),
	/** information messages for tracking normal application progress */
	INFO(3),
	/** warning messages about conditions (maybe recoverable) that should be noticed but aren't errors */
	WARNING(4),
	/** error messages */
	ERROR(5),
	/** severe error messages from which the application can't recover */
	FATAL(6),
	/** for turning off all log messages */
	OFF(7),
	// end
	;

	private final int value;

	private Level(int value) {
		this.value = value;
	}

	/**
	 * Return whether or not a level argument is enabled for this level value. So,
	 * {@code Level.INFO.isEnabled(Level.WARN)} returns true because if INFO level is enabled, WARN messages are
	 * displayed but {@code Level.INFO.isEnabled(Level.DEBUG)} returns false because if INFO level is enabled, DEBUG
	 * messages are not displayed. If this or the other level is OFF then false is returned.
	 */
	public boolean isEnabled(Level otherLevel) {
		return (this != Level.OFF && otherLevel != Level.OFF && value <= otherLevel.value);
	}
}
