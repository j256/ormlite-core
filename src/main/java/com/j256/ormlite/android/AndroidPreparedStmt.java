package com.j256.ormlite.android;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.PreparedStmt;
import com.j256.ormlite.support.Results;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 8:23:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidPreparedStmt implements PreparedStmt
{
    private final String sql;
    private final SQLiteDatabase db;
    private final DateAdapter dateAdapter;

    private Cursor cursor;
    private final List<String> args = new ArrayList<String>();
    private Integer max;

    public AndroidPreparedStmt(String sql, SQLiteDatabase db, DateAdapter dateAdapter)
    {
        this.sql = sql;
        this.db = db;
        this.dateAdapter = dateAdapter;
    }

    public int getColumnCount() throws SQLException
    {
        return getCursor().getColumnCount();
    }

    public String getColumnName(int column) throws SQLException
    {
        return getCursor().getColumnName(AndroidHelper.jdbcToAndroid(column));
    }

    public Results executeQuery() throws SQLException {
        return new AndroidResults(getCursor(), dateAdapter);
	}

	public int executeUpdate() throws SQLException {
        try
        {
            String finalSql;
            if(max == null)
                finalSql = sql;
            else
                finalSql = sql + " " + max;
            db.execSQL(finalSql, args.toArray(new String[args.size()]));
        }
        catch (Exception e)
        {
            throw SqlExceptionUtil.create("Problem with Android query", e);
        }
		return 1;
	}

    public Results getGeneratedKeys() throws SQLException {
    	throw new UnsupportedOperationException("Unsupported operation to getGeneratedKeys");
	}

	public void close() throws SQLException
    {
        if (cursor != null) {
        	cursor.close();
        }
    }

    public void setNull(int parameterIndex, SqlType sqlType) throws SQLException
    {
        isInPrep();
        args.add(AndroidHelper.jdbcToAndroid(parameterIndex), null);
    }

    public void setObject(int parameterIndex, Object obj, SqlType sqlType) throws SQLException
    {
        isInPrep();
        args.add(AndroidHelper.jdbcToAndroid(parameterIndex), obj.toString());
    }

    public void setMaxRows(int max) throws SQLException
    {
        isInPrep();
        this.max = max;
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
	            String finalSql;
	            if(max == null)
	            	finalSql = sql;
	            else
	                finalSql = sql + " " + max;
	            cursor = db.rawQuery(finalSql, args.toArray(new String[args.size()]));
	            cursor.moveToFirst();
	        }
	        catch (Exception e)
	        {
	            throw SqlExceptionUtil.create("Problem with Android query", e);
	        }
	    }
	
	    return cursor;
	}

	private void isInPrep() throws SQLException
    {
        if(cursor != null)
        {
            throw new SQLException("Query already run. Cannot add argument values.");
        }
    }
}
