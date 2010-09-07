package com.j256.ormlite.android;

import android.database.sqlite.SQLiteDatabase;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

import java.sql.SQLException;

/**
 * Abstract connection source. Supports concrete implementations.
 * 
 * @author kevingalligan
 */
public abstract class BaseAndroidConnectionSource implements ConnectionSource {

	abstract SQLiteDatabase getReadOnlyDatabase();
	abstract SQLiteDatabase getReadWriteDatabase();

	public DatabaseConnection getReadOnlyConnection() throws SQLException {
		return new AndroidDatabaseConnection(getReadOnlyDatabase(), false);
	}

	public DatabaseConnection getReadWriteConnection() throws SQLException {
		return new AndroidDatabaseConnection(getReadWriteDatabase(), true);
	}

	public void releaseConnection(DatabaseConnection connection) throws SQLException {
	}

	public void close() throws SQLException {
	}
}
