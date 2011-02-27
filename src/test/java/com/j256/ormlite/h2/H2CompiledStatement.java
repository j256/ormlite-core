package com.j256.ormlite.h2;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;

/**
 * H2 compiled statement.
 * 
 * @author graywatson
 */
public class H2CompiledStatement implements CompiledStatement {

	private PreparedStatement preparedStatement;

	public H2CompiledStatement(PreparedStatement preparedStatement) {
		this.preparedStatement = preparedStatement;
	}

	public int getColumnCount() throws SQLException {
		return preparedStatement.getMetaData().getColumnCount();
	}

	public String getColumnName(int column) throws SQLException {
		return preparedStatement.getMetaData().getColumnName(column);
	}

	public int runUpdate() throws SQLException {
		return preparedStatement.executeUpdate();
	}

	public DatabaseResults runQuery() throws SQLException {
		return new H2DatabaseResults(preparedStatement.executeQuery());
	}

	public int runExecute() throws SQLException {
		preparedStatement.execute();
		return preparedStatement.getUpdateCount();
	}

	public DatabaseResults getGeneratedKeys() throws SQLException {
		return new H2DatabaseResults(preparedStatement.getGeneratedKeys());
	}

	public void close() throws SQLException {
		preparedStatement.close();
	}

	public void setNull(int parameterIndex, SqlType sqlType) throws SQLException {
		preparedStatement.setNull(parameterIndex, sqlTypeToJdbcInt(sqlType));
	}

	public void setObject(int parameterIndex, Object obj, SqlType sqlType) throws SQLException {
		preparedStatement.setObject(parameterIndex, obj, sqlTypeToJdbcInt(sqlType));
	}

	public void setMaxRows(int max) throws SQLException {
		preparedStatement.setMaxRows(max);
	}

	public static int sqlTypeToJdbcInt(SqlType sqlType) {
		switch (sqlType) {
			case STRING :
				return Types.VARCHAR;
			case LONG_STRING :
				return Types.LONGVARCHAR;
			case DATE :
				return Types.TIMESTAMP;
			case BOOLEAN :
				return Types.BOOLEAN;
			case BYTE :
				return Types.TINYINT;
			case BYTE_ARRAY :
				return Types.VARBINARY;
			case SHORT :
				return Types.SMALLINT;
			case INTEGER :
				return Types.INTEGER;
			case LONG :
				// Types.DECIMAL, Types.NUMERIC
				return Types.BIGINT;
			case FLOAT :
				return Types.FLOAT;
			case DOUBLE :
				return Types.DOUBLE;
			case SERIALIZABLE :
				return Types.VARBINARY;
			case BLOB :
				return Types.BLOB;
			default :
				throw new IllegalArgumentException("No JDBC mapping for unknown SqlType " + sqlType);
		}
	}
}
