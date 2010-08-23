package com.j256.ormlite.android;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 10:21:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class StringDateAdapter implements DateAdapter
{
    private String dateFormat;

    //This is probably overkill, but I've seen threading issues with DateFormat in my time. We'll see how this shakes out.
    private ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>(); 

    public StringDateAdapter(String dateFormat)
    {
        this.dateFormat = dateFormat;
    }

    private DateFormat getFormat()
    {
        DateFormat format = df.get();
        if(format == null)
        {
            format = new SimpleDateFormat(dateFormat);
            df.set(format);
        }

        return format;
    }

    public Timestamp fromDb(Cursor c, int argIndex) throws AdapterException
    {
        String s = c.getString(argIndex);
        if(s == null)
            return null;

        try
        {
            Date date = getFormat().parse(s);
            return new Timestamp(date.getTime());
        }
        catch (ParseException e)
        {
            throw new AdapterException("Bad date format: "+ s, e);
        }
    }

    public void bindDate(SQLiteStatement stmt, int argIndex, Object arg)
    {
        Date date = (Date) arg;
        stmt.bindString(argIndex, getFormat().format(date));        
    }

    public String toDbFormat(Date date)
    {
        if(date == null)
            return null;
        return getFormat().format(date);
    }

    public boolean isNumeric()
    {
        return false;
    }
}
