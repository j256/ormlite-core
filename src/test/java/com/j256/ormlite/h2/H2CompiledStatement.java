package com.j256.ormlite.h2;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;

/**
 * H2 compiled statement.
 * 
 * @author graywatson
 */
public class H2CompiledStatement implements CompiledStatement {

	private final PreparedStatement preparedStatement;
	private final boolean cacheStore;

	public H2CompiledStatement(PreparedStatement preparedStatement, boolean cacheStore) {
		this.preparedStatement = preparedStatement;
		this.cacheStore = cacheStore;
	}

	@Override
	public int getColumnCount() throws SQLException {
		return preparedStatement.getMetaData().getColumnCount();
	}

	@Override
	public String getColumnName(int column) throws SQLException {
		return preparedStatement.getMetaData().getColumnName(column + 1);
	}

	@Override
	public int runUpdate() throws SQLException {
		return preparedStatement.executeUpdate();
	}

	@Override
	public DatabaseResults runQuery(ObjectCache objectCache) throws SQLException {
		return new H2DatabaseResults(preparedStatement.executeQuery(), objectCache, cacheStore);
	}

	@Override
	public int runExecute() throws SQLException {
		preparedStatement.execute();
		return preparedStatement.getUpdateCount();
	}

	@Override
	public void close() throws IOException {
		try {
			preparedStatement.close();
		} catch (SQLException e) {
			throw new IOException("could not close prepared statement", e);
		}
	}

	@Override
	public void closeQuietly() {
		IOUtils.closeQuietly(this);
	}

	@Override
	public void cancel() throws SQLException {
		preparedStatement.cancel();
	}

	@Override
	public void setObject(int parameterIndex, Object obj, SqlType sqlType) throws SQLException {
		if (obj == null) {
			preparedStatement.setNull(parameterIndex + 1, sqlTypeToJdbcInt(sqlType));
		} else {
			preparedStatement.setObject(parameterIndex + 1, obj, sqlTypeToJdbcInt(sqlType));
		}
	}

	@Override
	public void setMaxRows(int max) throws SQLException {
		preparedStatement.setMaxRows(max);
	}

	@Override
	public void setQueryTimeout(long millis) throws SQLException {
		preparedStatement.setQueryTimeout(Long.valueOf(millis).intValue() / 1000);
	}

	public static int sqlTypeToJdbcInt(SqlType sqlType) {
		switch (sqlType) {
			case STRING:
				return Types.VARCHAR;
			case LONG_STRING:
				return Types.LONGVARCHAR;
			case DATE:
				return Types.TIMESTAMP;
			case BOOLEAN:
				return Types.BOOLEAN;
			case CHAR:
				return Types.CHAR;
			case BYTE:
				return Types.TINYINT;
			case BYTE_ARRAY:
				return Types.VARBINARY;
			case SHORT:
				return Types.SMALLINT;
			case INTEGER:
				return Types.INTEGER;
			case LONG:
				// Types.DECIMAL, Types.NUMERIC
				return Types.BIGINT;
			case FLOAT:
				return Types.FLOAT;
			case DOUBLE:
				return Types.DOUBLE;
			case SERIALIZABLE:
				return Types.VARBINARY;
			case BLOB:
				return Types.BLOB;
			case BIG_DECIMAL:
				return Types.NUMERIC;
			default:
				throw new IllegalArgumentException("No JDBC mapping for unknown SqlType " + sqlType);
		}
	}
}
