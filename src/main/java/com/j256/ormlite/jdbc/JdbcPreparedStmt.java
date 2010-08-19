package com.j256.ormlite.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.j256.ormlite.support.PreparedStmt;
import com.j256.ormlite.support.Results;

/**
 * Wrapper around a {@link PreparedStatement} object which we delegate to.
 * 
 * @author graywatson
 */
public class JdbcPreparedStmt implements PreparedStmt {

	private final PreparedStatement preparedStatement;
	private ResultSetMetaData metaData = null;

	public JdbcPreparedStmt(PreparedStatement preparedStatement) {
		this.preparedStatement = preparedStatement;
	}

	public int getColumnCount() throws SQLException {
		if (metaData == null) {
			metaData = preparedStatement.getMetaData();
		}
		return metaData.getColumnCount();
	}

	public String getColumnName(int column) throws SQLException {
		if (metaData == null) {
			metaData = preparedStatement.getMetaData();
		}
		return metaData.getColumnName(column);
	}

	public int executeUpdate() throws SQLException {
		return preparedStatement.executeUpdate();
	}

	public Results executeQuery() throws SQLException {
		return new JdbcResults(preparedStatement, preparedStatement.executeQuery());
	}

	public Results getGeneratedKeys() throws SQLException {
		return new JdbcResults(preparedStatement, preparedStatement.getGeneratedKeys());
	}

	public void close() throws SQLException {
		preparedStatement.close();
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		preparedStatement.setNull(parameterIndex, sqlType);
	}

	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
		preparedStatement.setObject(parameterIndex, x, targetSqlType);
	}

	public void setMaxRows(int max) throws SQLException {
		preparedStatement.setMaxRows(max);
	}

	/**
	 * Called by {@link JdbcResults} to get more results into the existing ResultSet.
	 */
	boolean getMoreResults() throws SQLException {
		return preparedStatement.getMoreResults();
	}
}
