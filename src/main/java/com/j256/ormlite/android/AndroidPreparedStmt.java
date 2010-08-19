package com.j256.ormlite.android;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

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
    private final SQLiteDatabase db;
    private final DateAdapter dateAdapter;
    private Cursor cursor;
    private String sql;
    private List<String> args;
    private Integer max;

    public AndroidPreparedStmt(String sql, SQLiteDatabase db, DateAdapter dateAdapter)
    {
        this.sql = sql;
        this.args = new ArrayList<String>();
        this.db = db;
        this.dateAdapter = dateAdapter;
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
                cursor = db.rawQuery(sql, args.toArray(new String[args.size()]));
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

    public Results executeQuery() throws SQLException {
        return new AndroidResults(getCursor(), dateAdapter);
	}

	public int executeUpdate() throws SQLException {
		throw new RuntimeException("Not implemented yet");
	}

    public Results getGeneratedKeys() throws SQLException {
    	throw new UnsupportedOperationException("Unsupported operation to getGeneratedKeys");
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
