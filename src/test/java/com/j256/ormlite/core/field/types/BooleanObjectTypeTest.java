package com.j256.ormlite.core.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.core.dao.Dao;
import com.j256.ormlite.core.field.DataType;
import com.j256.ormlite.core.field.DatabaseField;
import com.j256.ormlite.core.table.DatabaseTable;

public class BooleanObjectTypeTest extends BaseTypeTest {

	private static final String BOOLEAN_COLUMN = "bool";

	@Test
	public void testBooleanObj() throws Exception {
		Class<LocalBooleanObj> clazz = LocalBooleanObj.class;
		Dao<LocalBooleanObj, Object> dao = createDao(clazz, true);
		Boolean val = true;
		String valStr = val.toString();
		LocalBooleanObj foo = new LocalBooleanObj();
		foo.bool = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.BOOLEAN_OBJ, BOOLEAN_COLUMN, false, false, false,
				false, false, false, true, false);
	}

	@Test
	public void testBooleanObjNull() throws Exception {
		Class<LocalBooleanObj> clazz = LocalBooleanObj.class;
		Dao<LocalBooleanObj, Object> dao = createDao(clazz, true);
		LocalBooleanObj foo = new LocalBooleanObj();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.BOOLEAN_OBJ, BOOLEAN_COLUMN, false, false, false,
				false, false, false, true, false);
	}

	/* ============================================================================================ */

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBooleanObj {
		@DatabaseField(columnName = BOOLEAN_COLUMN)
		Boolean bool;
	}
}
