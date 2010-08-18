package com.j256.ormlite.android;

import java.sql.SQLException;

import com.j256.ormlite.support.PreparedStmt;
import com.j256.ormlite.support.Results;

/**
 * Implementation of the PreparedStmt interface for Android systems.
 * 
 * @author ...
 */
public class AndroidPreparedStmt implements PreparedStmt {

	public int getColumnCount() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getColumnName(int column) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean execute() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public String getWarning() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Results getResults() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getMoreResults() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public void close() throws SQLException {
		// TODO Auto-generated method stub
	}

	public void setNull(int parameterIndex, int sqlType) throws SQLException {
		// TODO Auto-generated method stub
	}

	public void setObject(int parameterIndex, Object obj, int sqlType) throws SQLException {
		// TODO Auto-generated method stub
	}

	public void setMaxRows(int max) throws SQLException {
		// TODO Auto-generated method stub
	}
}
