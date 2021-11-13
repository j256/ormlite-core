package com.j256.ormlite.misc;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Utility class
 * 
 * @author graywatson
 */
public class IOUtils {

	/**
	 * Close the auto-closeable if not null and ignore any exceptions.
	 */
	public static void closeQuietly(AutoCloseable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				// ignored
			}
		}
	}

	/**
	 * Close the closeable if not null and ignore any exceptions.
	 */
	public static void closeQuietly(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// ignored
			}
		}
	}

	/**
	 * Close it and ignore any exceptions.
	 */
	public static void closeThrowSqlException(AutoCloseable closeable, String label) throws SQLException {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				throw new SQLException("could not close " + label, e);
			}
		}
	}

	/**
	 * Close it and ignore any exceptions.
	 */
	public static void closeThrowSqlException(Closeable closeable, String label) throws SQLException {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				throw new SQLException("could not close " + label, e);
			}
		}
	}
}
