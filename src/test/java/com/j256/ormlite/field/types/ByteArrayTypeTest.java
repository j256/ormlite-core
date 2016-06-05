package com.j256.ormlite.field.types;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class ByteArrayTypeTest extends BaseTypeTest {

	private static final String BYTE_COLUMN = "byteField";

	@Test
	public void testByteArray() throws Exception {
		Class<LocalByteArray> clazz = LocalByteArray.class;
		Dao<LocalByteArray, Object> dao = createDao(clazz, true);
		byte[] val = new byte[] { 123, 4, 124, 1, 0, 72 };
		String valStr = new String(val);
		LocalByteArray foo = new LocalByteArray();
		foo.byteField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.BYTE_ARRAY, BYTE_COLUMN, false, true, true, false,
				true, false, true, false);
	}

	@Test
	public void testByteArrayNull() throws Exception {
		Class<LocalByteArray> clazz = LocalByteArray.class;
		Dao<LocalByteArray, Object> dao = createDao(clazz, true);
		LocalByteArray foo = new LocalByteArray();
		assertEquals(1, dao.create(new LocalByteArray()));
		testType(dao, foo, clazz, null, null, null, null, DataType.BYTE_ARRAY, BYTE_COLUMN, false, true, true, false,
				true, false, true, false);
	}

	@Test
	public void testByteArrayId() throws Exception {
		Class<ByteArrayId> clazz = ByteArrayId.class;
		Dao<ByteArrayId, Object> dao = createDao(clazz, true);
		ByteArrayId foo = new ByteArrayId();
		foo.id = new byte[] { 1, 2, 3, 4, 5 };
		assertEquals(1, dao.create(foo));
		ByteArrayId result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertArrayEquals(foo.id, result.id);
	}

	@Test
	public void testCoverage() {
		new ByteArrayType(SqlType.BYTE_ARRAY, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByteArray {
		@DatabaseField(columnName = BYTE_COLUMN, dataType = DataType.BYTE_ARRAY)
		byte[] byteField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class ByteArrayId {
		@DatabaseField(columnName = BYTE_COLUMN, dataType = DataType.BYTE_ARRAY, id = true)
		byte[] id;
	}
}
