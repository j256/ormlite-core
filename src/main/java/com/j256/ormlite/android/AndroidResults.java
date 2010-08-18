package com.j256.ormlite.android;

import static com.j256.ormlite.android.AndroidHelper.androidToJdbc;
import static com.j256.ormlite.android.AndroidHelper.jdbcToAndroid;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.sql.Timestamp;

import android.database.Cursor;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.Results;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 9:27:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidResults implements Results
{
    private Cursor cursor;
    private DateAdapter dateAdapter;

    public AndroidResults(Cursor cursor, DateAdapter dateAdapter)
    {
        this.cursor = cursor;
        this.dateAdapter = dateAdapter;
    }

    public int getColumnCount() throws SQLException {
		return cursor.getColumnCount();
	}

	public String getColumnName(int column) throws SQLException {
		return cursor.getColumnName(column);
	}

	public DataType getColumnDataType(int column) throws SQLException {
		// everything in sqlite-land is a string
		return DataType.STRING;
	}

	public boolean next() throws SQLException
    {
        return cursor.moveToNext();
    }

    public int findColumn(String columnName) throws SQLException
    {
        return androidToJdbc(cursor.getColumnIndex(columnName));
    }

    public String getString(int columnIndex) throws SQLException
    {
        return cursor.getString(jdbcToAndroid(columnIndex));
    }

    public boolean getBoolean(int columnIndex) throws SQLException
    {
        int col = jdbcToAndroid(columnIndex);
        if(cursor.isNull(col))
            return false;
        return cursor.getShort(col) != 0;
    }

    public byte getByte(int columnIndex) throws SQLException
    {
        return (byte) getShort(columnIndex);
    }

    public byte[] getBytes(int columnIndex) throws SQLException
    {
        return cursor.getBlob(jdbcToAndroid(columnIndex));
    }

    public short getShort(int columnIndex) throws SQLException
    {
        return cursor.getShort(jdbcToAndroid(columnIndex));
    }

    public int getInt(int columnIndex) throws SQLException
    {
        return cursor.getInt(jdbcToAndroid(columnIndex));
    }

    public long getLong(int columnIndex) throws SQLException
    {
        return cursor.getLong(jdbcToAndroid(columnIndex));
    }

    public float getFloat(int columnIndex) throws SQLException
    {
        return cursor.getFloat(jdbcToAndroid(columnIndex));
    }

    public double getDouble(int columnIndex) throws SQLException
    {
        return cursor.getDouble(jdbcToAndroid(columnIndex));
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException
    {
        try
        {
            return dateAdapter.fromDb(cursor, jdbcToAndroid(columnIndex));
        }
        catch (AdapterException e)
        {
            throw SqlExceptionUtil.create("Could not convert from db date", e);
        }
    }

    public InputStream getBlobStream(int columnIndex) throws SQLException
    {
        return new ByteArrayInputStream(cursor.getBlob(jdbcToAndroid(columnIndex)));
    }

    public boolean isNull(int columnIndex) throws SQLException
    {
        return cursor.isNull(jdbcToAndroid(columnIndex));
    }
}
