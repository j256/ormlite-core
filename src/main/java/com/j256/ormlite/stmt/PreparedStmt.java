package com.j256.ormlite.stmt;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.CompiledStatement;

/**
 * Interface returned by the {@link StatementBuilder#prepareStatement()} which supports custom queries. This should be in turn
 * passed to the {@link Dao#query(PreparedStmt)} or {@link Dao#iterator(PreparedStmt)} methods.
 * 
 * @param T
 *            The class that the code will be operating on.
 * @param ID
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public interface PreparedStmt<T> extends GenericRowMapper<T> {

	/**
	 * Create and return the associated compiled statement.
	 */
	public CompiledStatement compile(DatabaseConnection databaseConnection) throws SQLException;

	/**
	 * Return the associated SQL statement string for logging purposes.
	 */
	public String getStatement() throws SQLException;
}
