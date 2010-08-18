package com.j256.ormlite.db;

import javax.sql.DataSource;

import android.database.sqlite.SQLiteOpenHelper;
import com.j256.ormlite.android.AndroidConfiguration;
import com.j256.ormlite.android.AndroidDatabaseAccess;
import com.j256.ormlite.android.DateAdapter;
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

	private SQLiteOpenHelper dbHelper;
    private DateAdapter dateAdapter;

    @Override
    public void loadDriver() throws ClassNotFoundException
    {
        //Hang out.  Nothing to do.
    }

    public SqliteAndroidDatabaseType(SQLiteOpenHelper dbHelper)
    {
        this.dbHelper = dbHelper;
    }

    public SqliteAndroidDatabaseType(SQLiteOpenHelper dbHelper, DateAdapter dateAdapter)
    {
        this.dateAdapter = dateAdapter;
        this.dbHelper = dbHelper;
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
	public DatabaseAccess buildDatabaseAccess(DataSource dataSource) {
		return new AndroidDatabaseAccess(new AndroidConfiguration(dbHelper, dateAdapter));
	}
}
