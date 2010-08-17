package com.j256.ormlite.jdbc;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.j256.ormlite.support.Results;

/**
 * Wrapper around a {@link ResultSet} object which we delegate to.
 * 
 * @author graywatson
 */
public class JdbcResults implements Results {

	private ResultSet resultSet;

	public JdbcResults(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public int findColumn(String columnName) throws SQLException {
		return resultSet.findColumn(columnName);
	}

	public Blob getBlob(int columnIndex) throws SQLException {
		return resultSet.getBlob(columnIndex);
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		return resultSet.getBoolean(columnIndex);
	}

	public byte getByte(int columnIndex) throws SQLException {
		return resultSet.getByte(columnIndex);
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		return resultSet.getBytes(columnIndex);
	}

	public double getDouble(int columnIndex) throws SQLException {
		return resultSet.getDouble(columnIndex);
	}

	public float getFloat(int columnIndex) throws SQLException {
		return resultSet.getFloat(columnIndex);
	}

	public int getInt(int columnIndex) throws SQLException {
		return resultSet.getInt(columnIndex);
	}

	public long getLong(int columnIndex) throws SQLException {
		return resultSet.getLong(columnIndex);
	}

	public short getShort(int columnIndex) throws SQLException {
		return resultSet.getShort(columnIndex);
	}

	public String getString(int columnIndex) throws SQLException {
		return resultSet.getString(columnIndex);
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return resultSet.getTimestamp(columnIndex);
	}

	public boolean next() throws SQLException {
		return resultSet.next();
	}

	public boolean isNull(int columnIndex) throws SQLException {
		return (resultSet.getObject(columnIndex) == null);
	}
}
