package com.j256.ormlite.android;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Date;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.GeneratedKeyHolder;
import com.j256.ormlite.support.PreparedStmt;

/**
 * Database connection for Android.
 * 
 * @author kevingalligan, graywatson
 */
public class AndroidDatabaseConnection implements DatabaseConnection {

	private final SQLiteDatabase db;
	private final DateAdapter dateAdapter;

	public AndroidDatabaseConnection(SQLiteDatabase db, DateAdapter dateAdapter) {
		this.db = db;
		this.dateAdapter = dateAdapter;
	}

	public boolean getAutoCommit() throws SQLException {
		// always in auto-commit mode?
		return true;
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		// always in auto-commit mode?
		if (!autoCommit) {
			throw new UnsupportedOperationException("autoCommit = false is not suppported by Android");
		}
	}

	public Savepoint setSavePoint(String name) throws SQLException {
		db.beginTransaction();
		return null;
	}

	public void commit(Savepoint savepoint) throws SQLException {
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		// no setTransactionSuccessful() means it is a rollback
		db.endTransaction();
	}

	public PreparedStmt prepareStatement(String statement) throws SQLException {
		PreparedStmt stmt = new AndroidPreparedStmt(statement, db, dateAdapter);
		return stmt;
	}

	/**
	 * Android doesn't return the number of rows inserted.
	 */
	public int insert(String statement, Object[] args, SqlType[] argFieldTypes) throws SQLException {
		SQLiteStatement stmt = db.compileStatement(statement);

        try
        {
            bindArgs(stmt, args, argFieldTypes);

            stmt.executeInsert();
        }
        finally
        {
            if(stmt != null)
                stmt.close();
        }

        return 1;
	}

	public int insert(String statement, Object[] args, SqlType[] argFieldTypes, GeneratedKeyHolder keyHolder)
			throws SQLException {
		SQLiteStatement stmt = db.compileStatement(statement);

        try
        {
            bindArgs(stmt, args, argFieldTypes);

            long rowId = stmt.executeInsert();
            keyHolder.addKey(rowId);
        }
        finally
        {
            if(stmt != null)
                stmt.close();
        }

        return 1;
	}

	public int update(String statement, Object[] args, SqlType[] argFieldTypes) throws SQLException {
		SQLiteStatement stmt = db.compileStatement(statement);
        try
        {
            bindArgs(stmt, args, argFieldTypes);

            stmt.execute();
        }
        finally
        {
            if(stmt != null)
                stmt.close();
        }
        return 1;
	}

	public int delete(String statement, Object[] args, SqlType[] argFieldTypes) throws SQLException {
		// delete is the same as update
		return update(statement, args, argFieldTypes);
	}

	public <T> Object queryForOne(String statement, Object[] args, SqlType[] argFieldTypes,
			GenericRowMapper<T> rowMapper) throws SQLException {
		Cursor cursor = db.rawQuery(statement, toStrings(args));
		AndroidResults results = new AndroidResults(cursor, dateAdapter);

        try
        {
            if (!results.next()) {
                return null;
            } else {
                T first = rowMapper.mapRow(results);

                if (results.next()) {
                    return MORE_THAN_ONE;
                } else {
                    return first;
                }
            }
        }
        finally
        {
            if(cursor != null)
                cursor.close();
        }
    }

	public long queryForLong(String statement) throws SQLException {
		SQLiteStatement stmt = db.compileStatement(statement);
        try
        {
            return stmt.simpleQueryForLong();
        }
        finally
        {
            if(stmt != null)
                stmt.close();
        }
    }

	public void close() throws SQLException {
		db.close();
	}

	private void bindArgs(SQLiteStatement stmt, Object[] args, SqlType[] argFieldTypes) {
		if (args == null) {
			return;
		}
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			int argIndex = AndroidHelper.androidToJdbc(i);// Android API's are a bit inconsistent
			if (arg == null) {
				stmt.bindNull(argIndex);
			} else {
				switch (argFieldTypes[i]) {
					case BOOLEAN :
                        stmt.bindLong(argIndex, ((Boolean) arg) ? 1 : 0);
                        break;
					case BYTE :
					case SHORT :
					case INTEGER :
					case LONG :
						stmt.bindLong(argIndex, ((Number) arg).longValue());
						break;
					case FLOAT :
					case DOUBLE :
						stmt.bindDouble(argIndex, ((Number) arg).doubleValue());
						break;
					case STRING :
					case SERIALIZABLE :
						stmt.bindString(argIndex, (arg instanceof String) ? (String) arg : arg.toString());
						break;
					case DATE :
						dateAdapter.bindDate(stmt, argIndex, arg);
						break;
				}
			}
		}
	}

	private String[] toStrings(Object[] args) {
		if (args == null)
			return null;
		String[] strings = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			Object arg = args[i];
			if (arg == null)
				strings[i] = null;
			else if (arg instanceof Date)
				strings[i] = dateAdapter.toDbFormat((Date) arg);
			else
				strings[i] = arg.toString();
		}

		return strings;
	}
}
