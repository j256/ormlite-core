package com.j256.ormlite.db;

import javax.sql.DataSource;

import com.j256.ormlite.android.AndroidDatabaseAccess;
import com.j256.ormlite.support.DatabaseAccess;

/**
 * Sqlite database type information for the Android OS. This has a difference driver class name.
 * 
 * <p>
 * <b> WARNING:</b> JDBC support in Android is currently marked as <i>unsupported</i>. I bet this will change in the
 * future but until that time, you should use this with caution. You may want to try the {@link SqlDroidDatabaseType} if
 * this driver does not work for you.
 * </p>
 * 
 * @author graywatson
 */
public class SqliteAndroidDatabaseType extends SqliteDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "sqlite";
	private final static String DRIVER_CLASS_NAME = "SQLite.JDBCDriver";

	@Override
	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	@Override
	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	public DatabaseAccess buildDatabaseAccess(DataSource dataSource) {
		return new AndroidDatabaseAccess();
	}
}
