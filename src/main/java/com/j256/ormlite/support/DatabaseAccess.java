package com.j256.ormlite.support;

import java.sql.SQLException;
import java.sql.Types;

import com.j256.ormlite.stmt.GenericRowMapper;

/**
 * Interface that hides the various calls to a SQL database.
 * 
 * @author graywatson
 */
public interface DatabaseAccess {

	/** returned by {@link #queryForOne} if more than one result was found by the query */
	public final static Object MORE_THAN_ONE = new Object();

	/**
	 * Perform a SQL insert with the associated SQL statement, arguments, and types.
	 * 
	 * @param statement
	 *            SQL statement to use for inserting.
	 * @param args
	 *            Object arguments for the SQL '?'s.
	 * @param argSqlTypeVals
	 *            SQL type values from {@link Types}.
	 * @return The number of rows affected by the update.
	 */
	public int insert(String statement, Object[] args, int[] argSqlTypeVals) throws SQLException;

	/**
	 * Perform a SQL update while returning generated keys with the associated SQL statement, arguments, and types.
	 * 
	 * @param statement
	 *            SQL statement to use for inserting.
	 * @param args
	 *            Object arguments for the SQL '?'s.
	 * @param argSqlTypeVals
	 *            SQL type values from {@link Types}.
	 * @param keyHolder
	 *            The holder that gets set with the generated key value.
	 * @return The number of rows affected by the update.
	 */
	public int insert(String statement, Object[] args, int[] argSqlTypeVals, GeneratedKeyHolder keyHolder)
			throws SQLException;

	/**
	 * Perform a SQL update with the associated SQL statement, arguments, and types.
	 * 
	 * @param statement
	 *            SQL statement to use for updating.
	 * @param args
	 *            Object arguments for the SQL '?'s.
	 * @param argSqlTypeVals
	 *            SQL type values from {@link Types}.
	 * @return The number of rows affected by the update.
	 */
	public int update(String statement, Object[] args, int[] argSqlTypeVals) throws SQLException;

	/**
	 * Perform a SQL delete with the associated SQL statement, arguments, and types.
	 * 
	 * @param statement
	 *            SQL statement to use for deleting.
	 * @param args
	 *            Object arguments for the SQL '?'s.
	 * @param argSqlTypeVals
	 *            SQL type values from {@link Types}.
	 * @return The number of rows affected by the update.
	 */
	public int delete(String statement, Object[] args, int[] argSqlTypeVals) throws SQLException;

	/**
	 * Perform a SQL query with the associated SQL statement, arguments, and types and returns a single result.
	 * 
	 * @param statement
	 *            SQL statement to use for deleting.
	 * @param args
	 *            Object arguments for the SQL '?'s.
	 * @param argSqlTypeVals
	 *            SQL type values from {@link Types}.
	 * @param rowMapper
	 *            The mapper to use to convert the row into the returned object.
	 * @return The first data item returned by the query which can be cast to <T>, null if none, the object
	 *         {@link #MORE_THAN_ONE} if more than one result was found.
	 */
	public <T> Object queryForOne(String statement, Object[] args, int[] argSqlTypeVals, GenericRowMapper<T> rowMapper)
			throws SQLException;

	/**
	 * Perform a query whose result should be a single long-integer value.
	 * 
	 * @param statement
	 *            SQL statement to use for the query.
	 */
	public long queryForLong(String statement) throws SQLException;

	/**
	 * Generate a prepared statement from a SQL statement.
	 * 
	 * @param statement
	 *            SQL statement to be prepared.
	 */
	public PreparedStmt prepareStatement(String statement) throws SQLException;
}
