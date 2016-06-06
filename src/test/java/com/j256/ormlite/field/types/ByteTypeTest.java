package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class ByteTypeTest extends BaseTypeTest {

	private static final String BYTE_COLUMN = "byteField";

	@Test
	public void testByte() throws Exception {
		Class<LocalByte> clazz = LocalByte.class;
		Dao<LocalByte, Object> dao = createDao(clazz, true);
		byte val = 123;
		String valStr = Byte.toString(val);
		LocalByte foo = new LocalByte();
		foo.byteField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.BYTE, BYTE_COLUMN, false, true, false, true, false,
				false, true, true);
	}

	@Test
	public void testBytePrimitiveNull() throws Exception {
		Dao<LocalByteObj, Object> objDao = createDao(LocalByteObj.class, true);
		LocalByteObj foo = new LocalByteObj();
		foo.byteField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalByte, Object> dao = createDao(LocalByte.class, false);
		List<LocalByte> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).byteField);
	}

	@Test
	public void testCoverage() {
		new ByteType(SqlType.BYTE, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByte {
		@DatabaseField(columnName = BYTE_COLUMN)
		byte byteField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByteObj {
		@DatabaseField(columnName = BYTE_COLUMN)
		Byte byteField;
	}
}
