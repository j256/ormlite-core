package android.database;

/**
 * Stub implementation of the Android cursor object to stop compilation errors.
 */
public class Cursor {

	public boolean moveToFirst() {
		return false;
	}

	public int getColumnCount() {
		return 0;
	}

	public String getColumnName(int column) {
		return null;
	}

	public void close() {
	}

	public boolean moveToNext() {
		return false;
	}

	public int getColumnIndex(String columnName) {
		return 0;
	}

	public String getString(int columnIndex) {
		return null;
	}

	public boolean isNull(int columnIndex) {
		return false;
	}

	public short getShort(int columnIndex) {
		return 0;
	}

	public byte[] getBlob(int columnIndex) {
		return null;
	}

	public int getInt(int columnIndex) {
		return 0;
	}

	public long getLong(int columnIndex) {
		return 0L;
	}

	public float getFloat(int columnIndex) {
		return 0.0F;
	}

	public double getDouble(int columnIndex) {
		return 0.0D;
	}
}
