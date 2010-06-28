package com.j256.ormlite.support;

import java.sql.Connection;
import java.sql.SQLException;

import com.j256.ormlite.stmt.GenericRowMapper;

/**
 * Replacement for Spring's JdbcTemplate that hides the various calls to a SQL {@link Connection}.
 * 
 * @author graywatson
 */
public interface JdbcTemplate {

	/** returned by {@link #queryForOne} if more than one result was found by the query */
	public final static Object MORE_THAN_ONE = new Object();

	/**
	 * Perform a SQL update with the associated SQL statement, arguments, and types.
	 * 
	 * @return The number of rows affected by the update.
	 */
	public int update(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException;

	/**
	 * Perform a SQL update while returning generated keys with the associated SQL statement, arguments, and types.
	 * 
	 * @return The number of rows affected by the update.
	 */
	public int update(String statement, Object[] args, int[] argFieldTypeVals, GeneratedKeyHolder keyHolder)
			throws SQLException;

	/**
	 * Perform a SQL query with the associated SQL statement, arguments, and types and returns a single result.
	 * 
	 * @return The first data item returned by the query which can be cast to <T>, null if none, the object
	 *         {@link #MORE_THAN_ONE} if more than one result was found.
	 */
	public <T> Object queryForOne(String statement, Object[] args, int[] argFieldTypeVals, GenericRowMapper<T> rowMapper)
			throws SQLException;

	/**
	 * Perform a query whose result should be a single long-integer value.
	 */
	public long queryForLong(String statement) throws SQLException;
}
