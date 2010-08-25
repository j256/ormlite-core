package com.j256.ormlite.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Wrapper around a {@link PreparedStatement} object which we delegate to.
 * 
 * @author graywatson
 */
public class JdbcCompiledStatement implements CompiledStatement {

	private final PreparedStatement preparedStatement;
	private ResultSetMetaData metaData = null;

	public JdbcCompiledStatement(PreparedStatement preparedStatement) {
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

	public DatabaseResults executeQuery() throws SQLException {
		return new JdbcDatabaseResults(preparedStatement, preparedStatement.executeQuery());
	}

	public DatabaseResults getGeneratedKeys() throws SQLException {
		return new JdbcDatabaseResults(preparedStatement, preparedStatement.getGeneratedKeys());
	}

	public void close() throws SQLException {
		preparedStatement.close();
	}

	public void setNull(int parameterIndex, SqlType sqlType) throws SQLException {
		preparedStatement.setNull(parameterIndex, sqlType.getTypeVal());
	}

	public void setObject(int parameterIndex, Object obj, SqlType sqlType) throws SQLException {
		preparedStatement.setObject(parameterIndex, obj, sqlType.getTypeVal());
	}

	public void setMaxRows(int max) throws SQLException {
		preparedStatement.setMaxRows(max);
	}

	/**
	 * Called by {@link JdbcDatabaseResults#next()} to get more results into the existing ResultSet.
	 */
	boolean getMoreResults() throws SQLException {
		return preparedStatement.getMoreResults();
	}
}
