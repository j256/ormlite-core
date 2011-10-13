package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class FloatObjectTypeTest extends BaseTypeTest {

	private static final String FLOAT_COLUMN = "floatField";

	@Test
	public void testFloatObj() throws Exception {
		Class<LocalFloatObj> clazz = LocalFloatObj.class;
		Dao<LocalFloatObj, Object> dao = createDao(clazz, true);
		Float val = 1331.221F;
		String valStr = val.toString();
		LocalFloatObj foo = new LocalFloatObj();
		foo.floatField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.FLOAT_OBJ, FLOAT_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@Test
	public void testFloatObjNull() throws Exception {
		Class<LocalFloatObj> clazz = LocalFloatObj.class;
		Dao<LocalFloatObj, Object> dao = createDao(clazz, true);
		LocalFloatObj foo = new LocalFloatObj();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.FLOAT_OBJ, FLOAT_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalFloatObj {
		@DatabaseField(columnName = FLOAT_COLUMN)
		Float floatField;
	}
}
