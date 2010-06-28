package com.j256.ormlite.stmt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.j256.ormlite.dao.BaseJdbcDao;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;

/**
 * Internal iterator so we can page through the class. This is used by the {@link Dao#iterator} methods.
 * 
 * @param T
 *            The class that the code will be operating on.
 * @param ID
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class SelectIterator<T, ID> implements CloseableIterator<T> {

	private final static Logger logger = LoggerFactory.getLogger(SelectIterator.class);

	private final Class<T> dataClass;
	private BaseJdbcDao<T, ID> classDao;
	private final PreparedStatement stmt;
	private final ResultSet resultSet;
	private final GenericRowMapper<T> rowMapper;
	private final String statement;
	private boolean closed = false;
	private T last = null;
	private int rowC = 0;

	public SelectIterator(Class<T> dataClass, BaseJdbcDao<T, ID> classDao, GenericRowMapper<T> rowMapper,
			PreparedStatement preparedStatement, String statement) throws SQLException {
		this.dataClass = dataClass;
		this.classDao = classDao;
		this.rowMapper = rowMapper;
		this.stmt = preparedStatement;
		if (!stmt.execute()) {
			throw new SQLException("Could not execute select iterator on " + dataClass + " with warnings: "
					+ stmt.getWarnings());
		}
		this.resultSet = stmt.getResultSet();
		this.statement = statement;
		if (statement != null) {
			logger.debug("starting iterator @{} for '{}'", hashCode(), statement);
		}
	}

	/**
	 * Returns whether or not there are any remaining objects in the table. Must be called before next().
	 * 
	 * @throws SQLException
	 *             If there was a problem getting more results via SQL.
	 */
	public boolean hasNextThrow() throws SQLException {
		if (closed) {
			return false;
		}
		if (!resultSet.next()) {
			if (!stmt.getMoreResults()) {
				close();
				return false;
			}
			if (!resultSet.next()) {
				// may never get here but let's be careful out there
				close();
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns whether or not there are any remaining objects in the table. Must be called before next().
	 * 
	 * @throws IllegalStateException
	 *             If there was a problem getting more results via SQL.
	 */
	public boolean hasNext() {
		try {
			return hasNextThrow();
		} catch (SQLException e) {
			last = null;
			try {
				close();
			} catch (SQLException e1) {
				// ignore it
			}
			// unfortunately, can't propagate back the SQLException
			throw new IllegalStateException("Errors getting more results of " + dataClass, e);
		}
	}

	/**
	 * Returns the next() object in the table.
	 * 
	 * @throws SQLException
	 *             If there was a problem extracting the object from SQL.
	 */
	public T nextThrow() throws SQLException {
		if (closed) {
			return null;
		}
		last = rowMapper.mapRow(resultSet, rowC);
		rowC++;
		return last;
	}

	/**
	 * Returns the next() object in the table.
	 * 
	 * @throws IllegalStateException
	 *             If there was a problem extracting the object from SQL.
	 */
	public T next() {
		try {
			return nextThrow();
		} catch (SQLException e) {
			last = null;
			try {
				close();
			} catch (SQLException e1) {
				// ignore it
			}
			// unfortunately, can't propagate back the SQLException
			throw new IllegalStateException("Errors getting more results of " + dataClass, e);
		}
	}

	/**
	 * Removes the last object returned by next() by calling delete on the dao associated with the object.
	 * 
	 * @throws IllegalStateException
	 *             If there was no previous next() call.
	 * @throws SQLException
	 *             If the delete failed.
	 */
	public void removeThrow() throws SQLException {
		if (last == null) {
			throw new IllegalStateException("No last " + dataClass
					+ " object to remove. Must be called after a call to next.");
		}
		if (classDao == null) {
			// we may never be able to get here since it should only be null for queryForAll methods
			throw new IllegalStateException("Cannot remove " + dataClass + " object because classDao not initialized");
		}
		try {
			classDao.delete(last);
		} finally {
			// if we've try to delete it, clear the last marker
			last = null;
		}
	}

	/**
	 * Removes the last object returned by next() by calling delete on the dao associated with the object.
	 * 
	 * @throws IllegalStateException
	 *             If there was no previous next() call or if delete() throws a SQLException (set as the cause).
	 */
	public void remove() {
		try {
			removeThrow();
		} catch (SQLException e) {
			try {
				close();
			} catch (SQLException e1) {
				// ignore it
			}
			// unfortunately, can't propagate back the SQLException
			throw new IllegalStateException("Errors trying to delete " + dataClass + " object " + last, e);
		}
	}

	/**
	 * Close the underlying statement.
	 */
	public void close() throws SQLException {
		if (!closed) {
			stmt.close();
			closed = true;
			last = null;
			if (statement != null) {
				logger.debug("closed iterator @{} after {} rows", hashCode(), rowC);
			}
		}
	}

	/**
	 * Internal method for getting information about the result set.
	 */
	ResultSetMetaData getResultSetMetaData() throws SQLException {
		return resultSet.getMetaData();
	}
}
