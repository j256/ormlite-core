package com.j256.ormlite.db;

/**
 * Sqlite database type information used on the Android OS using the SqlDroid 3rd party database driver.
 * 
 * <p>
 * <b> NOTE: </b> Support for Android is now native. See the section on the manual about running with Android.
 * </p>
 * 
 * <p>
 * <b> NOTE:</b> internal JDBC support in Android is currently marked as <i>unsupported</i> which is why the SqlDroid
 * 3rd party driver was necessary. You may also want to try the {@link SqliteAndroidDatabaseType} if this driver does
 * not work for you.
 * </p>
 * 
 * @author graywatson
 */
public class SqlDroidDatabaseType extends SqliteDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "sqldroid";
	private final static String DRIVER_CLASS_NAME = "com.lemadi.storage.database.sqldroid.SqldroidDriver";

	@Override
	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	@Override
	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}
}
