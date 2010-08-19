package com.j256.ormlite.android;

import java.sql.SQLException;
import java.sql.Savepoint;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.GeneratedKeyHolder;
import com.j256.ormlite.support.PreparedStmt;

/**
 * Created by IntelliJ IDEA. User: kevin Date: Aug 17, 2010 Time: 8:00:42 PM To change this template use File | Settings
 * | File Templates.
 */
public class AndroidDatabaseConnection implements DatabaseConnection {

	private final SQLiteDatabase db;
	private final DateAdapter dateAdapter;

	public AndroidDatabaseConnection(SQLiteDatabase db, DateAdapter dateAdapter) {
		this.db = db;
		this.dateAdapter = dateAdapter;
	}

	public boolean getAutoCommit() throws SQLException {
		// always in auto-commit mode?
		return true;
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		// always in auto-commit mode?
	}

	public boolean isSupportsSavepoints() throws SQLException {
		return true;
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		db.beginTransaction();
		return null;
	}

	public void commit() throws SQLException {
		// always in auto-commit mode?
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void rollback() throws SQLException {
		db.endTransaction();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		db.endTransaction();
	}

	/**
	 * Android doesn't return the number of rows inserted.
	 */
	public int insert(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException {
		SQLiteStatement stmt = db.compileStatement(statement);

		AndroidHelper.bindArgs(stmt, args, argFieldTypeVals, dateAdapter);

		stmt.executeInsert();

		return 1;
	}

	public int insert(String statement, Object[] args, int[] argFieldTypeVals, GeneratedKeyHolder keyHolder)
			throws SQLException {
		SQLiteStatement stmt = db.compileStatement(statement);

		AndroidHelper.bindArgs(stmt, args, argFieldTypeVals, dateAdapter);

		long rowId = stmt.executeInsert();
		keyHolder.addKey(rowId);

		return 1;
	}

	public int update(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException {
		executeUpdate(statement, args, argFieldTypeVals);
		return 1;
	}

	private void executeUpdate(String statement, Object[] args, int[] argFieldTypeVals) {
		SQLiteStatement stmt = db.compileStatement(statement);

		AndroidHelper.bindArgs(stmt, args, argFieldTypeVals, dateAdapter);

		stmt.execute();
	}

	public int delete(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException {
		executeUpdate(statement, args, argFieldTypeVals);
		return 1;
	}

	public <T> Object queryForOne(String statement, Object[] args, int[] argFieldTypeVals, GenericRowMapper<T> rowMapper)
			throws SQLException {
		Cursor cursor = db.rawQuery(statement, AndroidHelper.toStrings(args, dateAdapter));
		cursor.moveToFirst();
		AndroidResults results = new AndroidResults(cursor, dateAdapter);

		if (!results.next()) {
			return null;
		} else {
			T first = rowMapper.mapRow(results);

			if (results.next()) {
				return MORE_THAN_ONE;
			} else {
				return first;
			}
		}
	}

	public long queryForLong(String statement) throws SQLException {
		SQLiteStatement stmt = db.compileStatement(statement);
		long l = stmt.simpleQueryForLong();
		return l;
	}

	public PreparedStmt prepareStatement(String sql) throws SQLException {
		PreparedStmt stmt = new AndroidPreparedStmt(sql, db, dateAdapter);
		return stmt;
	}

	public void close() throws SQLException {
		db.close();
	}
}
