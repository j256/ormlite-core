package com.j256.ormlite.android.apptools;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.SqliteAndroidDatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * Wraps the TableUtils object and hard codes the @see com.j256.ormlite.db.SqliteAndroidDatabaseType SqliteAndroidDatabaseType  
 *
 * @author kevingalligan, graywatson
 */
public class AndroidTableUtils
{
    static SqliteAndroidDatabaseType dbType = new SqliteAndroidDatabaseType();
        
    public static <T> int createTable(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException
    {
        return TableUtils.createTable(dbType, connectionSource, dataClass);
    }
    public static <T> int createTable(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig) throws SQLException
    {
        return TableUtils.createTable(dbType, connectionSource, tableConfig);
    }

    public static <T> int dropTable(ConnectionSource connectionSource, Class<T> dataClass, boolean ignoreErrors) throws SQLException
    {
        return TableUtils.dropTable(dbType, connectionSource, dataClass, ignoreErrors);
    }

    public static <T> int dropTable(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws SQLException
    {
        return TableUtils.dropTable(dbType, connectionSource, tableConfig, ignoreErrors);
    }
}
