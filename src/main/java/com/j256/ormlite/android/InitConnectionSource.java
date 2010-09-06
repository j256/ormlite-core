package com.j256.ormlite.android;

import android.database.sqlite.SQLiteDatabase;

/**
 * Connection source to be used during onCreate and onUpdate for SQLiteOpenHelper class.  The helper class
 * cannot access its own get db methods during initialization.
 *
 * @author kevingalligan, graywatson
 */
public class InitConnectionSource extends BaseAndroidConnectionSource
{
    private SQLiteDatabase db;

    public InitConnectionSource(SQLiteDatabase db)
    {
        this.db = db;
    }

    @Override
    SQLiteDatabase getReadOnlyDatabase()
    {
        return db;
    }

    @Override
    SQLiteDatabase getReadWriteDatabase()
    {
        return db;
    }
}
