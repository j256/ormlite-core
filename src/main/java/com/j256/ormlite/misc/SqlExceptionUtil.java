package com.j256.ormlite.misc;

import java.sql.SQLException;

/**
 * Utility class to help with SQLException throwing.
 * 
 * @author graywatson
 */
public class SqlExceptionUtil {

	/**
	 * Should be used in a static context only.
	 */
	private SqlExceptionUtil() {
	}

	/**
	 * Convenience method to allow a cause. Grrrr.
	 */
	public static SQLException create(String message, Throwable cause) {
		SQLException sqlException = new SQLException(message);
		sqlException.initCause(cause);
		return sqlException;
	}
}
