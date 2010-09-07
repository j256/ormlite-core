package android.database.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * Stub implementation of the Android Sqlite database helper object to stop compilation errors.
 */
public class SQLiteOpenHelper {

	public SQLiteOpenHelper(Context context, String name, CursorFactory factory, int version) {
	}

	public SQLiteDatabase getWritableDatabase() {
		return new SQLiteDatabase();
	}

	public SQLiteDatabase getReadableDatabase() {
		return new SQLiteDatabase();
	}

	public void onCreate(SQLiteDatabase db) {
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public void close() {
	}
}