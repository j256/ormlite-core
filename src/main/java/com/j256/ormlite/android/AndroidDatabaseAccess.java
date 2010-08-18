package com.j256.ormlite.android;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.support.DatabaseAccess;
import com.j256.ormlite.support.GeneratedKeyHolder;
import com.j256.ormlite.support.PreparedStmt;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Aug 17, 2010
 * Time: 8:00:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class AndroidDatabaseAccess implements DatabaseAccess
{
//    private SQLiteDatabase db;
    private AndroidConfiguration config;

    public AndroidDatabaseAccess(AndroidConfiguration config)
    {
        this.config = config;
//        this.db = db;
    }


    /**
     * Android doesn't return the number of rows inserted.
     */
    public int insert(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException
    {
        SQLiteDatabase db = config.getWriteableDb();
        SQLiteStatement stmt = db.compileStatement(statement);
        
        AndroidHelper.bindArgs(stmt, args, argFieldTypeVals, config);
        
        stmt.executeInsert();

        db.close();
        return 1;
    }

    public int insert(String statement, Object[] args, int[] argFieldTypeVals, GeneratedKeyHolder keyHolder) throws SQLException
    {
        SQLiteDatabase db = config.getWriteableDb();
        SQLiteStatement stmt = db.compileStatement(statement);

        AndroidHelper.bindArgs(stmt, args, argFieldTypeVals, config);

        long rowId = stmt.executeInsert();
        keyHolder.addKey(rowId);

        db.close();
        return 1;
    }

    public int update(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException
    {
        executeUpdate(statement, args, argFieldTypeVals);
        return 1;
    }

    private void executeUpdate(String statement, Object[] args, int[] argFieldTypeVals)
    {
        SQLiteDatabase db = config.getWriteableDb();
        SQLiteStatement stmt = db.compileStatement(statement);

        AndroidHelper.bindArgs(stmt, args, argFieldTypeVals, config);

        stmt.execute();
        db.close();
    }

    public int delete(String statement, Object[] args, int[] argFieldTypeVals) throws SQLException
    {
        executeUpdate(statement, args, argFieldTypeVals);
        return 1;
    }

    public <T> Object queryForOne(String statement, Object[] args, int[] argFieldTypeVals, GenericRowMapper<T> rowMapper)throws SQLException
    {
        SQLiteDatabase db = config.getReadableDb();
        Cursor cursor = db.rawQuery(statement, AndroidHelper.toStrings(args, config));
        cursor.moveToFirst();
        AndroidResults results = new AndroidResults(cursor, config);

        if(!results.next())
        {
            db.close();
            return null;
        }
        else
        {
            T first = rowMapper.mapRow(results);
            db.close();

            if (results.next()) {
                return MORE_THAN_ONE;
            } else {
                return first;
            }
        }
    }

    public long queryForLong(String statement) throws SQLException
    {
        SQLiteDatabase db = config.getReadableDb();
        SQLiteStatement stmt = db.compileStatement(statement);
        long l = stmt.simpleQueryForLong();
        db.close();
        return l;
    }

    public PreparedStmt prepareStatement(String sql) throws SQLException
    {
        PreparedStmt stmt = new AndroidPreparedStmt(sql, config);
        return stmt;
    }
}
