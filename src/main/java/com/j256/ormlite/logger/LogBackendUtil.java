package com.j256.ormlite.logger;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Some common utility methods.
 * 
 * From SimpleLogging: https://github.com/j256/simplelogging
 *
 * @author graywatson
 */
public class LogBackendUtil {

	/**
	 * Return a string equivalent to the throwable for logging.
	 */
	public static String throwableToString(Throwable throwable) {
		StringWriter writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
}
