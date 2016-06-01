package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class StringBytesTypeTest extends BaseTypeTest {

	private static final String STRING_COLUMN = "string";

	@Test
	public void testStringBytes() throws Exception {
		Class<LocalStringBytes> clazz = LocalStringBytes.class;
		Dao<LocalStringBytes, Object> dao = createDao(clazz, true);
		String val = "string with \u0185";
		LocalStringBytes foo = new LocalStringBytes();
		foo.string = val;
		assertEquals(1, dao.create(foo));
		byte[] valBytes = val.getBytes("Unicode");
		testType(dao, foo, clazz, val, valBytes, valBytes, val, DataType.STRING_BYTES, STRING_COLUMN, false, true, true,
				false, true, false, true, false);
	}

	@Test
	public void testStringBytesFormat() throws Exception {
		Class<LocalStringBytesUtf8> clazz = LocalStringBytesUtf8.class;
		Dao<LocalStringBytesUtf8, Object> dao = createDao(clazz, true);
		String val = "string with \u0185";
		LocalStringBytesUtf8 foo = new LocalStringBytesUtf8();
		foo.string = val;
		assertEquals(1, dao.create(foo));
		byte[] valBytes = val.getBytes("UTF-8");
		testType(dao, foo, clazz, val, valBytes, valBytes, val, DataType.STRING_BYTES, STRING_COLUMN, false, true, true,
				false, true, false, true, false);
	}

	@Test
	public void testStringBytesNull() throws Exception {
		Class<LocalStringBytes> clazz = LocalStringBytes.class;
		Dao<LocalStringBytes, Object> dao = createDao(clazz, true);
		LocalStringBytes foo = new LocalStringBytes();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.STRING_BYTES, STRING_COLUMN, false, true, true,
				false, true, false, true, false);
	}

	@Test
	public void testCoverage() {
		new StringBytesType(SqlType.BYTE_ARRAY, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalStringBytes {
		@DatabaseField(columnName = STRING_COLUMN, dataType = DataType.STRING_BYTES)
		String string;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalStringBytesUtf8 {
		@DatabaseField(columnName = STRING_COLUMN, dataType = DataType.STRING_BYTES, format = "UTF-8")
		String string;
	}
}
