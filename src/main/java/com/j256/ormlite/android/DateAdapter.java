package com.j256.ormlite.android;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 10:16:21 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DateAdapter
{
    Timestamp fromDb(Cursor c, int col) throws AdapterException;

    void bindDate(SQLiteStatement stmt, int i, Object arg);

    String toDbFormat(Date date);
}
