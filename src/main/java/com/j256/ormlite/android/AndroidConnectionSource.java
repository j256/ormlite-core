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

	private SQLiteDatabase readableDb = null;
	private SQLiteDatabase readWriteDb = null;

	public AndroidConnectionSource(SQLiteDatabase readWriteDb) {
		this(readWriteDb, readWriteDb);
	}

	public AndroidConnectionSource(SQLiteDatabase readableDb, SQLiteDatabase readWriteDb) {
		this.readableDb = readableDb;
		this.readWriteDb = readWriteDb;
	}

	public AndroidDatabaseConnection getReadOnlyConnection() throws SQLException {
		return new AndroidDatabaseConnection(readableDb);
	}

	public AndroidDatabaseConnection getReadWriteConnection() throws SQLException {
		return new AndroidDatabaseConnection(readWriteDb);
	}

	/**
	 * Close any open connections. This will cause any future transactions to re-open the databases.
	 */
	public void close() throws SQLException {
		if (readableDb != null) {
			readableDb.close();
			readableDb = null;
		}
		if (readWriteDb != null) {
			readWriteDb.close();
			readWriteDb = null;
		}
	}
}
