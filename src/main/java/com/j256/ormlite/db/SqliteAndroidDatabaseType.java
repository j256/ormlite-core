package com.j256.ormlite.db;

import com.j256.ormlite.android.DateAdapter;

/**
 * Sqlite database type information for the Android OS. This has a difference driver class name.
 * 
 * <p>
 * <b> NOTE: </b> Support for Android is now native. See the section on the manual about running with Android.
 * </p>
 * 
 * <p>
 * <b> WARNING:</b> If you do endeavor to use JDBC under Android, understand that as of 8/2010, JDBC is currently
 * <i>unsupported</i>. This may change in the future but until that time, you should use this with caution. You may want
 * to try the {@link SqlDroidDatabaseType} if this driver does not work for you.
 * </p>
 * 
 * @author graywatson
 */
public class SqliteAndroidDatabaseType extends SqliteDatabaseType implements DatabaseType {

	private DateAdapter dateAdapter;

	public SqliteAndroidDatabaseType(DateAdapter dateAdapter) {
		this.dateAdapter = dateAdapter;
	}

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

	@Override
	protected void appendDateType(StringBuilder sb) {
		if (dateAdapter.isNumeric()) {
			appendLongType(sb);
		} else {
			appendStringType(sb, 50);
		}
	}

	@Override
	protected void appendBooleanType(StringBuilder sb) {
		appendShortType(sb);
	}
}
