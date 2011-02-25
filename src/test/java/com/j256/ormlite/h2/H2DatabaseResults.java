package com.j256.ormlite.h2;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.j256.ormlite.support.DatabaseResults;

/**
 * H2 database results.
 * 
 * @author graywatson
 */
public class H2DatabaseResults implements DatabaseResults {

	private ResultSet resultSet;

	public H2DatabaseResults(ResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public int getColumnCount() throws SQLException {
		return resultSet.getMetaData().getColumnCount();
	}

	public String getColumnName(int column) throws SQLException {
		return resultSet.getMetaData().getColumnName(column);
	}

	public boolean next() throws SQLException {
		return resultSet.next();
	}

	public int findColumn(String columnName) throws SQLException {
		return resultSet.findColumn(columnName);
	}

	public String getString(int columnIndex) throws SQLException {
		return resultSet.getString(columnIndex);
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

	public short getShort(int columnIndex) throws SQLException {
		return resultSet.getShort(columnIndex);
	}

	public int getInt(int columnIndex) throws SQLException {
		return resultSet.getInt(columnIndex);
	}

	public long getLong(int columnIndex) throws SQLException {
		return resultSet.getLong(columnIndex);
	}

	public float getFloat(int columnIndex) throws SQLException {
		return resultSet.getFloat(columnIndex);
	}

	public double getDouble(int columnIndex) throws SQLException {
		return resultSet.getDouble(columnIndex);
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return resultSet.getTimestamp(columnIndex);
	}

	public InputStream getBlobStream(int columnIndex) throws SQLException {
		Blob blob = resultSet.getBlob(columnIndex);
		if (blob == null) {
			return null;
		} else {
			return blob.getBinaryStream();
		}
	}

	public boolean isNull(int columnIndex) throws SQLException {
		return (resultSet.getObject(columnIndex) == null);
	}
}
