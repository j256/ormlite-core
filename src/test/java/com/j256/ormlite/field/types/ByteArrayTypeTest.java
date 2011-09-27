package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Arrays;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class ByteArrayTypeTest extends BaseTypeTest {

	private static final String BYTE_COLUMN = "byteField";

	@Test
	public void testByteArray() throws Exception {
		Class<LocalByteArray> clazz = LocalByteArray.class;
		Dao<LocalByteArray, Object> dao = createDao(clazz, true);
		byte[] val = new byte[] { 123, 4, 124, 1, 0, 72 };
		String valStr = Arrays.toString(val);
		LocalByteArray foo = new LocalByteArray();
		foo.byteField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.BYTE_ARRAY, BYTE_COLUMN, false, false, true,
				false, true, false, true, false);
	}

	@Test
	public void testByteArrayNull() throws Exception {
		Class<LocalByteArray> clazz = LocalByteArray.class;
		Dao<LocalByteArray, Object> dao = createDao(clazz, true);
		LocalByteArray foo = new LocalByteArray();
		assertEquals(1, dao.create(new LocalByteArray()));
		testType(dao, foo, clazz, null, null, null, null, DataType.BYTE_ARRAY, BYTE_COLUMN, false, false, true,
				false, true, false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testByteArrayParseDefault() throws Exception {
		DataType.BYTE_ARRAY.getDataPersister().parseDefaultString(null, null);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByteArray {
		@DatabaseField(columnName = BYTE_COLUMN, dataType = DataType.BYTE_ARRAY)
		byte[] byteField;
	}
}
