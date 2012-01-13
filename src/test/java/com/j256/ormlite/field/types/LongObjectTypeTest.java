package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class LongObjectTypeTest extends BaseTypeTest {

	private static final String LONG_COLUMN = "longField";

	@Test
	public void testLongObj() throws Exception {
		Class<LocalLongObj> clazz = LocalLongObj.class;
		Dao<LocalLongObj, Object> dao = createDao(clazz, true);
		Long val = 13312321312312L;
		String valStr = val.toString();
		LocalLongObj foo = new LocalLongObj();
		foo.longField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.LONG_OBJ, LONG_COLUMN, true, true, false, false,
				false, false, true, true);
	}

	@Test
	public void testLongObjNull() throws Exception {
		Class<LocalLongObj> clazz = LocalLongObj.class;
		Dao<LocalLongObj, Object> dao = createDao(clazz, true);
		LocalLongObj foo = new LocalLongObj();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.LONG_OBJ, LONG_COLUMN, true, true, false, false,
				false, false, true, true);
	}

	@Test
	public void testVersion() throws Exception {
		Dao<LongVersion, Integer> dao = createDao(LongVersion.class, true);
		LongVersion foo = new LongVersion();
		assertNull(foo.version);
		assertEquals(1, dao.create(foo));
		assertEquals(Long.valueOf(1), foo.version);
		assertEquals(1, dao.update(foo));
		assertEquals(Long.valueOf(2), foo.version);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalLongObj {
		@DatabaseField(columnName = LONG_COLUMN)
		Long longField;
	}

	protected static class LongVersion {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(version = true)
		Long version;
	}
}
