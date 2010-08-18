package com.j256.ormlite.android;

import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.j256.ormlite.support.Results;

/**
 * Implementation of the Results interface for Android systems.
 * 
 * @author ...
 */
public class AndroidResults implements Results {

	public boolean next() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public int findColumn(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getString(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public byte getByte(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public short getShort(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getInt(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLong(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getFloat(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getDouble(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream getBlobStream(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isNull(int columnIndex) throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}
}
