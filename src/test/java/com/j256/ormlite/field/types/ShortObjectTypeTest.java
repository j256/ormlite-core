package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class ShortObjectTypeTest extends BaseTypeTest {

	private static final String SHORT_COLUMN = "shortField";

	@Test
	public void testShortObj() throws Exception {
		Class<LocalShortObj> clazz = LocalShortObj.class;
		Dao<LocalShortObj, Object> dao = createDao(clazz, true);
		Short val = 12312;
		String valStr = val.toString();
		LocalShortObj foo = new LocalShortObj();
		foo.shortField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.SHORT_OBJ, SHORT_COLUMN, false, true, false, false,
				false, false, true, true);
	}

	@Test
	public void testShortObjNull() throws Exception {
		Class<LocalShortObj> clazz = LocalShortObj.class;
		Dao<LocalShortObj, Object> dao = createDao(clazz, true);
		LocalShortObj foo = new LocalShortObj();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.SHORT_OBJ, SHORT_COLUMN, false, true, false, false,
				false, false, true, true);
	}

	@Test
	public void testVersion() throws Exception {
		Dao<ShortVersion, Integer> dao = createDao(ShortVersion.class, true);
		ShortVersion foo = new ShortVersion();
		assertNull(foo.version);
		assertEquals(1, dao.create(foo));
		assertEquals(Short.valueOf((short) 1), foo.version);
		assertEquals(1, dao.update(foo));
		assertEquals(Short.valueOf((short) 2), foo.version);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalShortObj {
		@DatabaseField(columnName = SHORT_COLUMN)
		Short shortField;
	}

	protected static class ShortVersion {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(version = true)
		Short version;
	}
}
