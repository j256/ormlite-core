package com.j256.ormlite.support;

import java.sql.SQLException;

/**
 * A reduction of the SQL PreparedStatment so we can implement its functionality outside of JDBC.
 * 
 * @author graywatson
 */
public interface PreparedStmt {

	/**
	 * Returns the number of columns in this statement.
	 */
	public int getColumnCount() throws SQLException;

	/**
	 * Get the designated column's name.
	 * 
	 * @param column
	 *            The first column is 1, the second is 2, ...
	 */
	public String getColumnName(int column) throws SQLException;

	/**
	 * Execute the prepared update statement returning the number of rows affected.
	 */
	public int executeUpdate() throws SQLException;

	/**
	 * Execute the prepared query statement returning the results.
	 */
	public Results executeQuery() throws SQLException;

	/**
	 * Get the generated key results.
	 */
	public Results getGeneratedKeys() throws SQLException;

	/**
	 * Put more results in the results object.
	 */
	public boolean getMoreResults() throws SQLException;

	/**
	 * Close the statement.
	 */
	public void close() throws SQLException;

	/**
	 * Set the parameter specified by the index and type to be null.
	 * 
	 * @param parameterIndex
	 *            Index of the parameter with 1 being the first parameter, etc..
	 * @param sqlType
	 *            SQL type of the parameter.
	 */
	public void setNull(int parameterIndex, int sqlType) throws SQLException;

	/**
	 * Set the parameter specified by the index and type to be an object.
	 * 
	 * @param parameterIndex
	 *            Index of the parameter with 1 being the first parameter, etc..
	 * @param obj
	 *            Object that we are setting.
	 * @param sqlType
	 *            SQL type of the parameter.
	 */
	public void setObject(int parameterIndex, Object obj, int sqlType) throws SQLException;

	/**
	 * Set the number of rows to return in the results.
	 */
	public void setMaxRows(int max) throws SQLException;
}
