package com.j256.ormlite.android;

import java.sql.SQLException;

import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.support.DatabaseAccess;
import com.j256.ormlite.support.GeneratedKeyHolder;
import com.j256.ormlite.support.PreparedStmt;

/**
 * Android database access class.
 * 
 * @author ...
 */
public class AndroidDatabaseAccess implements DatabaseAccess {

	public int insert(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int insert(String statement, Object[] args, int[] argFieldTypeVals, GeneratedKeyHolder keyHolder)
			throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int update(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public int delete(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public <T> Object queryForOne(String statement, Object[] args, int[] argFieldTypeVals, GenericRowMapper<T> rowMapper)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public long queryForLong(String statement) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	public PreparedStmt prepareStatement(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
