package com.j256.ormlite.support;

import java.sql.SQLException;

/**
 * A reduction of the SQL DataSource so we can implement its functionality outside of JDBC.
 * 
 * @author graywatson
 */
public interface ConnectionSource {

	/**
	 * Return a database connection suitable for read-only operations.
	 */
	public DatabaseConnection getReadOnlyConnection() throws SQLException;

	/**
	 * Return a database connection suitable for read or write operations.
	 */
	public DatabaseConnection getReadWriteConnection() throws SQLException;

	/**
	 * Close any outstanding database connections.
	 */
	public void close() throws SQLException;
}
