package com.j256.ormlite.android;

import java.sql.SQLException;

import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.support.ConnectionSource;

/**
 * Our source for connections to Android databases.
 * 
 * @author kevingalligan, graywatson
 */
public class AndroidConnectionSource implements ConnectionSource {

	private AndroidDatabaseConnection readableConnection = null;
	private AndroidDatabaseConnection readWriteConnection = null;

	public AndroidConnectionSource(SQLiteDatabase readWriteDb) {
		this(readWriteDb, readWriteDb);
	}

	public AndroidConnectionSource(SQLiteDatabase readableDb, SQLiteDatabase readWriteDb) {
		this.readableConnection = new AndroidDatabaseConnection(readableDb);
		this.readWriteConnection = new AndroidDatabaseConnection(readWriteDb);
	}

	public AndroidDatabaseConnection getReadOnlyConnection() throws SQLException {
		return readableConnection;
	}

	public AndroidDatabaseConnection getReadWriteConnection() throws SQLException {
		return readWriteConnection;
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
