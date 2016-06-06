package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class ByteObjectTypeTest extends BaseTypeTest {

	private static final String BYTE_COLUMN = "byteField";

	@Test
	public void testByteObj() throws Exception {
		Class<LocalByteObj> clazz = LocalByteObj.class;
		Dao<LocalByteObj, Object> dao = createDao(clazz, true);
		byte val = 123;
		String valStr = Byte.toString(val);
		LocalByteObj foo = new LocalByteObj();
		foo.byteField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.BYTE_OBJ, BYTE_COLUMN, false, true, false, false,
				false, false, true, true);
	}

	@Test
	public void testByteObjNull() throws Exception {
		Class<LocalByteObj> clazz = LocalByteObj.class;
		Dao<LocalByteObj, Object> dao = createDao(clazz, true);
		LocalByteObj foo = new LocalByteObj();
		assertEquals(1, dao.create(new LocalByteObj()));
		testType(dao, foo, clazz, null, null, null, null, DataType.BYTE_OBJ, BYTE_COLUMN, false, true, false, false,
				false, false, true, true);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByteObj {
		@DatabaseField(columnName = BYTE_COLUMN)
		Byte byteField;
	}
}
