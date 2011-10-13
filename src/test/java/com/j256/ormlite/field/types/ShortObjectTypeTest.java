package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

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
				false, false, true, false);
	}

	@Test
	public void testShortObjNull() throws Exception {
		Class<LocalShortObj> clazz = LocalShortObj.class;
		Dao<LocalShortObj, Object> dao = createDao(clazz, true);
		LocalShortObj foo = new LocalShortObj();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.SHORT_OBJ, SHORT_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalShortObj {
		@DatabaseField(columnName = SHORT_COLUMN)
		Short shortField;
	}
}
