package android.database.sqlite;

/**
 * Stub implementation of the Android Sqlite database helper object to stop compilation errors.
 */
public class SQLiteOpenHelper {

	public SQLiteDatabase getWritableDatabase() {
		return new SQLiteDatabase();
	}

	public SQLiteDatabase getReadableDatabase() {
		return new SQLiteDatabase();
	}
}