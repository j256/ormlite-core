package android.database.sqlite;

import android.database.Cursor;

/**
 * Stub implementation of the Android Sqlite database object to stop compilation errors.
 */
public class SQLiteDatabase {

	public SQLiteStatement compileStatement(String sql) {
		return new SQLiteStatement();
	}

	public void close() {
	}

	public Cursor rawQuery(String sql, String[] args) {
		return new Cursor();
	}

	public void beginTransaction() {
	}

	public void setTransactionSuccessful() {
	}

	public void endTransaction() {
	}
}
