package com.j256.ormlite.jdbc;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.support.Results;

/**
 * Wrapper around a {@link ResultSet} object which we delegate to.
 * 
 * @author graywatson
 */
public class JdbcResults implements Results {

	private final PreparedStatement preparedStmt;
	private final ResultSet resultSet;
	private ResultSetMetaData metaData = null;

	public JdbcResults(PreparedStatement preparedStmt, ResultSet resultSet) {
		this.preparedStmt = preparedStmt;
		this.resultSet = resultSet;
	}

	public int getColumnCount() throws SQLException {
		if (metaData == null) {
			metaData = resultSet.getMetaData();
		}
		return metaData.getColumnCount();
	}

	public String getColumnName(int column) throws SQLException {
		if (metaData == null) {
			metaData = resultSet.getMetaData();
		}
		return metaData.getColumnName(column);
	}

	DataType getColumnDataType(int column) throws SQLException {
		if (metaData == null) {
			metaData = resultSet.getMetaData();
		}
		int typeVal = metaData.getColumnType(column);
		return DataType.lookupIdTypeVal(typeVal);
	}

	public int findColumn(String columnName) throws SQLException {
		return resultSet.findColumn(columnName);
	}

	public InputStream getBlobStream(int columnIndex) throws SQLException {
		Blob blob = resultSet.getBlob(columnIndex);
		if (blob == null) {
			return null;
		} else {
			return blob.getBinaryStream();
		}
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
		// NOTE: we should not auto-close here, even if there are no more results
		if (resultSet.next()) {
			return true;
		} else if (!preparedStmt.getMoreResults()) {
			return false;
		} else {
			return resultSet.next();
		}
	}

	public boolean isNull(int columnIndex) throws SQLException {
		return (resultSet.getObject(columnIndex) == null);
	}
}
