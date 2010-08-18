package com.j256.ormlite.android;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 10:18:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class NumericDateAdapter implements DateAdapter
{

    public Timestamp fromDb(Cursor c, int col)
    {
        if(c.isNull(col))
            return null;

        long timestamp = c.getLong(col);

        return new Timestamp(timestamp);
    }

    public void bindDate(SQLiteStatement stmt, int i, Object arg)
    {
        Date date = (Date) arg;
        stmt.bindLong(i, date.getTime());
    }

    public String toDbFormat(Date date)
    {
        if(date == null)
            return null;
        return Long.toString(date.getTime());
    }
}
