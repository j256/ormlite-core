package com.j256.ormlite.android;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Main connection source.  Uses the standrd android SQLiteOpenHelper.  For best results, use our helper, 
 * @see com.j256.ormlite.android.apptools.OrmLiteSQLiteOpenHelper OrmLiteSLiteOpenHelper
 *
 * @author kevingalligan, graywatson
 */
public class AndroidConnectionSource extends BaseAndroidConnectionSource
{
    private SQLiteOpenHelper helper;

    public AndroidConnectionSource(SQLiteOpenHelper helper)
    {
        this.helper = helper;
    }

    @Override
    SQLiteDatabase getReadOnlyDatabase()
    {
        return helper.getReadableDatabase();
    }

    @Override
    SQLiteDatabase getReadWriteDatabase()
    {
        return helper.getWritableDatabase();
    }
}
