package com.j256.ormlite.android;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.PreparedStmt;
import com.j256.ormlite.support.Results;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 8:23:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidPreparedStmt implements PreparedStmt
{
    private SQLiteDatabase db;
    private Cursor cursor;
    private String sql;
    private List<String> args;
    private Integer max;
    private AndroidConfiguration config;

    public AndroidPreparedStmt(String sql, AndroidConfiguration config)
    {
        this.sql = sql;
        args = new ArrayList<String>();
        this.config = config;
    }

    private SQLiteDatabase getDb()
    {
        if(db == null)
        {
            db = config.getWriteableDb();
        }
        return db;
    }

    /**
     * Not thread safe.  Not sure if we need it, but keep that in mind.
     */
    private Cursor getCursor() throws SQLException
    {
        if(cursor == null)
        {
            try
            {
                if(max != null)
                    sql = sql + " " + max;
                cursor = getDb().rawQuery(sql, args.toArray(new String[args.size()]));
                cursor.moveToFirst();
            }
            catch (Exception e)
            {
                throw SqlExceptionUtil.create("Problem with Android query", e);
            }
        }

        return cursor;
    }

    public int getColumnCount() throws SQLException
    {
        return getCursor().getColumnCount();
    }

    public String getColumnName(int column) throws SQLException
    {
        return getCursor().getColumnName(AndroidHelper.jdbcToAndroid(column));
    }

    public boolean execute() throws SQLException
    {
        getCursor();
        return true;
    }

    /**
     * Nothing equivalent in Android
     */
    public String getWarning()
    {
        return "";
    }

    public Results getResults() throws SQLException
    {
        return new AndroidResults(getCursor(), config.dateAdapter);
    }

    /**
     * Nothing equivalent in Android
     */
    public boolean getMoreResults()
    {
        return false;
    }

    public void close() throws SQLException
    {
        cursor.close();
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException
    {
        setObject(parameterIndex, null, sqlType);
    }

    public void setObject(int parameterIndex, Object obj, int sqlType) throws SQLException
    {
        isInPrep();
        args.add(AndroidHelper.jdbcToAndroid(parameterIndex), obj == null ? null : obj.toString());
    }

    public void setMaxRows(int max) throws SQLException
    {
        isInPrep();
        this.max = max;
    }

    private void isInPrep() throws SQLException
    {
        if(cursor != null)
        {
            throw new SQLException("Query already run. Cannot add argument values.");
        }
    }
}
