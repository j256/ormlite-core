package com.j256.ormlite.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.GeneratedKeyHolder;
import com.j256.ormlite.support.PreparedStmt;
import com.j256.ormlite.support.Results;

public class JdbcDatabaseConnection implements DatabaseConnection {

	private static Object[] noArgs = new Object[0];
	private static int[] noArgTypes = new int[0];
	private static GenericRowMapper<Long> longWrapper = new OneLongWrapper();

	private Connection connection;
	private DatabaseMetaData metaData = null;

	public JdbcDatabaseConnection(Connection connection) {
		this.connection = connection;
	}

	public boolean isSupportsSavepoints() throws SQLException {
		if (metaData == null) {
			metaData = connection.getMetaData();
		}
		return metaData.supportsSavepoints();
	}

	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return connection.setSavepoint(name);
	}

	public void commit() throws SQLException {
		connection.commit();
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		connection.releaseSavepoint(savepoint);
	}

	public void rollback() throws SQLException {
		connection.rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		connection.rollback(savepoint);
	}

	public PreparedStmt prepareStatement(String sql) throws SQLException {
		return new JdbcPreparedStmt(connection.prepareStatement(sql));
	}

	public void close() throws SQLException {
		connection.close();
	}

	/**
	 * Returns whether the connection has already been closed. Used by {@link JdbcConnectionSource}.
	 */
	boolean isClosed() throws SQLException {
		return connection.isClosed();
	}

	public int insert(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException {
		// it's a call to executeUpdate
		return update(statement, args, argFieldTypeVals);
	}

	public int insert(String statement, Object[] args, int[] argFieldTypeVals, GeneratedKeyHolder keyHolder)
			throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
		statementSetArgs(stmt, args, argFieldTypeVals);
		int rowN = stmt.executeUpdate();
		Results results = new JdbcResults(stmt, stmt.getGeneratedKeys());
		if (results == null) {
			// may never happen but let's be careful
			throw new SQLException("No generated key results returned from update: " + statement);
		}
		int colN = results.getColumnCount();
		while (results.next()) {
			for (int colC = 1; colC <= colN; colC++) {
				String colName = results.getColumnName(colC);
				DataType dataType = results.getColumnDataType(colC);
				Number id = dataType.resultToId(results, colC);
				if (id == null) {
					// may never happen but let's be careful
					throw new SQLException("Generated column " + colName + " is invalid type " + dataType);
				} else {
					keyHolder.addKey(id);
				}
			}
		}
		return rowN;
	}

	public int update(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException {
		PreparedStatement stmt = connection.prepareStatement(statement);
		statementSetArgs(stmt, args, argFieldTypeVals);
		return stmt.executeUpdate();
	}

	public int delete(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException {
		// it's a call to executeUpdate
		return update(statement, args, argFieldTypeVals);
	}

	public <T> Object queryForOne(String statement, Object[] args, int[] argFieldTypeVals, GenericRowMapper<T> rowMapper)
			throws SQLException {
		PreparedStatement stmt =
				connection.prepareStatement(statement, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		statementSetArgs(stmt, args, argFieldTypeVals);
		Results results = new JdbcResults(stmt, stmt.executeQuery());
		if (!results.next()) {
			// no results at all
			return null;
		}
		T first = rowMapper.mapRow(results);
		if (results.next()) {
			return MORE_THAN_ONE;
		} else {
			return first;
		}
	}

	public long queryForLong(String statement) throws SQLException {
		Object result = queryForOne(statement, noArgs, noArgTypes, longWrapper);
		if (result == null) {
			throw new SQLException("No results returned in query-for-long: " + statement);
		} else if (result == MORE_THAN_ONE) {
			throw new SQLException("More than 1 result returned in query-for-long: " + statement);
		} else {
			return (Long) result;
		}
	}

	private void statementSetArgs(PreparedStatement stmt, Object[] args, int[] argFieldTypeVals) throws SQLException {
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg == null) {
				stmt.setNull(i + 1, argFieldTypeVals[i]);
			} else {
				stmt.setObject(i + 1, arg, argFieldTypeVals[i]);
			}
		}
	}

	/**
	 * Row mapper that handles a single long result.
	 */
	private static class OneLongWrapper implements GenericRowMapper<Long> {
		public Long mapRow(Results rs) throws SQLException {
			// maps the first column (sql #1)
			return rs.getLong(1);
		}
	}
}
