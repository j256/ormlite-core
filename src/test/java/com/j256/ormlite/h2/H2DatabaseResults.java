package com.j256.ormlite.h2;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.support.DatabaseResults;

/**
 * H2 database results.
 * 
 * @author graywatson
 */
public class H2DatabaseResults implements DatabaseResults {

	private final ResultSet resultSet;
	private final ResultSetMetaData metaData;
	private final ObjectCache objectCache;

	public H2DatabaseResults(ResultSet resultSet, ObjectCache objectCache) throws SQLException {
		this.resultSet = resultSet;
		this.metaData = resultSet.getMetaData();
		this.objectCache = objectCache;
	}

    public ResultSet getResultSet() throws SQLException {
        return resultSet;
    }

    public int getColumnCount() throws SQLException {
		return metaData.getColumnCount();
	}

	public String[] getColumnNames() throws SQLException {
		int colN = getColumnCount();
		String[] columnNames = new String[colN];
		for (int colC = 0; colC < colN; colC++) {
			columnNames[colC] = metaData.getColumnLabel(colC + 1);
		}
		return columnNames;
	}

	public boolean first() throws SQLException {
		return resultSet.first();
	}

	public boolean next() throws SQLException {
		return resultSet.next();
	}

	public boolean last() throws SQLException {
		return resultSet.last();
	}

	public boolean previous() throws SQLException {
		return resultSet.previous();
	}

	public boolean moveRelative(int num) throws SQLException {
		return resultSet.relative(num);
	}

	public boolean moveAbsolute(int position) throws SQLException {
		return resultSet.absolute(position);
	}

	public int findColumn(String columnName) throws SQLException {
		return resultSet.findColumn(columnName) - 1;
	}

	public String getString(int columnIndex) throws SQLException {
		return resultSet.getString(columnIndex + 1);
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		return resultSet.getBoolean(columnIndex + 1);
	}

	public char getChar(int columnIndex) throws SQLException {
		String string = resultSet.getString(columnIndex + 1);
		if (string == null || string.length() == 0) {
			return 0;
		} else if (string.length() == 1) {
			return string.charAt(columnIndex);
		} else {
			throw new SQLException("More than 1 character stored in database column: " + columnIndex);
		}
	}

	public byte getByte(int columnIndex) throws SQLException {
		return resultSet.getByte(columnIndex + 1);
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		return resultSet.getBytes(columnIndex + 1);
	}

	public short getShort(int columnIndex) throws SQLException {
		return resultSet.getShort(columnIndex + 1);
	}

	public int getInt(int columnIndex) throws SQLException {
		return resultSet.getInt(columnIndex + 1);
	}

	public long getLong(int columnIndex) throws SQLException {
		return resultSet.getLong(columnIndex + 1);
	}

	public float getFloat(int columnIndex) throws SQLException {
		return resultSet.getFloat(columnIndex + 1);
	}

	public double getDouble(int columnIndex) throws SQLException {
		return resultSet.getDouble(columnIndex + 1);
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return resultSet.getBigDecimal(columnIndex + 1);
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return resultSet.getTimestamp(columnIndex + 1);
	}

	public InputStream getBlobStream(int columnIndex) throws SQLException {
		Blob blob = resultSet.getBlob(columnIndex + 1);
		if (blob == null) {
			return null;
		} else {
			return blob.getBinaryStream();
		}
	}

	public boolean wasNull(int columnIndex) throws SQLException {
		return resultSet.wasNull();
	}

	public ObjectCache getObjectCache() {
		return objectCache;
	}

	public void close() throws IOException {
		try {
			resultSet.close();
		} catch (SQLException e) {
			throw new IOException("could not close result set", e);
		}
	}

	public void closeQuietly() {
		IOUtils.closeQuietly(this);
	}
}
