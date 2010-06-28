package com.j256.ormlite.stmt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;

/**
 * Interface returned by the {@link QueryBuilder#prepareQuery()} which supports custom queries. This should be in turn
 * passed to the {@link Dao#query(PreparedQuery)} or {@link Dao#iterator(PreparedQuery)} methods.
 * 
 * @param T
 *            The class that the code will be operating on.
 * @param ID
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public interface PreparedQuery<T> extends GenericRowMapper<T> {

	/**
	 * Create and return the associated SQL {@link PreparedStatement} for the SQL {@link Connection}.
	 */
	public PreparedStatement prepareSqlStatement(Connection sqlConnection) throws SQLException;

	/**
	 * Return the associated SQL statement string for logging purposes.
	 */
	public String getStatement() throws SQLException;
}
