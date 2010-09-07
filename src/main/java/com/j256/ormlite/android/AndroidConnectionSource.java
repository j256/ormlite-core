package com.j256.ormlite.android;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Our source for connections to Android databases.
 * 
 * @author kevingalligan, graywatson
 */
public class AndroidConnectionSource implements ConnectionSource {

	private AndroidDatabaseConnection readableConnection = null;
	private AndroidDatabaseConnection readWriteConnection = null;
	private SQLiteOpenHelper databaseHelper = null;

	public AndroidConnectionSource(SQLiteOpenHelper databaseHelper) {
		this.databaseHelper = databaseHelper;
	}

	public AndroidConnectionSource(SQLiteDatabase readWriteDb) {
		this(readWriteDb, readWriteDb);
	}

	public AndroidConnectionSource(SQLiteDatabase readableDb, SQLiteDatabase readWriteDb) {
		this.readableConnection = new AndroidDatabaseConnection(readableDb, false);
		this.readWriteConnection = new AndroidDatabaseConnection(readWriteDb, true);
	}

	public DatabaseConnection getReadOnlyConnection() throws SQLException {
		if (readableConnection == null) {
			synchronized (databaseHelper) {
				if (readableConnection == null) {
					readableConnection = new AndroidDatabaseConnection(databaseHelper.getReadableDatabase(), false);
				}
			}
		}
		return readableConnection;
	}

	public DatabaseConnection getReadWriteConnection() throws SQLException {
		if (readableConnection == null) {
			synchronized (databaseHelper) {
				if (readWriteConnection == null) {
					readWriteConnection = new AndroidDatabaseConnection(databaseHelper.getWritableDatabase(), true);
				}
			}
		}
		return readWriteConnection;
	}

	public void releaseConnection(DatabaseConnection connection) throws SQLException {
		// noop right now
	}

	/**
	 * Close any open connections. This will cause any future transactions to re-open the databases.
	 */
	public void close() throws SQLException {
		if (readableConnection != null) {
			readableConnection.close();
			readableConnection = null;
		}
		if (readWriteConnection != null) {
			readWriteConnection.close();
			readWriteConnection = null;
		}
	}
}
