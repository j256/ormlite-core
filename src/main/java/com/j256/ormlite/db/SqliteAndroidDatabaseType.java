package com.j256.ormlite.db;

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

	@Override
	public void loadDriver() throws ClassNotFoundException {
		// Hang out. Nothing to do.
	}

	@Override
	public String getDriverUrlPart() {
		return null;
	}

	@Override
	public String getDriverClassName() {
		return null;
	}
}
