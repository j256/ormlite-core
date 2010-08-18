package com.j256.ormlite.android;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 10:30:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidConfiguration
{
    public DateAdapter dateAdapter;
    public SQLiteOpenHelper dbHelper;

    public AndroidConfiguration(SQLiteOpenHelper dbHelper, DateAdapter dateAdapter)
    {
        this.dbHelper = dbHelper;
        this.dateAdapter = dateAdapter == null ? new NumericDateAdapter() : dateAdapter;
    }

    public SQLiteDatabase getWriteableDb()
    {
        return dbHelper.getWritableDatabase();
    }

    public SQLiteDatabase getReadableDb()
    {
        return dbHelper.getReadableDatabase();
    }
}
