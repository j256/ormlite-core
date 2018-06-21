package com.j256.ormlite.core.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.core.dao.Dao;
import com.j256.ormlite.core.field.DataType;
import com.j256.ormlite.core.field.DatabaseField;
import com.j256.ormlite.core.table.DatabaseTable;

public class IntegerObjectTypeTest extends BaseTypeTest {

	private static final String INT_COLUMN = "intField";

	@Test
	public void testIntObj() throws Exception {
		Class<LocalIntObj> clazz = LocalIntObj.class;
		Dao<LocalIntObj, Object> dao = createDao(clazz, true);
		Integer val = 313213123;
		String valStr = val.toString();
		LocalIntObj foo = new LocalIntObj();
		foo.intField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.INTEGER_OBJ, INT_COLUMN, true, true, false, false,
				false, false, true, true);
	}

	@Test
	public void testIntObjNull() throws Exception {
		Class<LocalIntObj> clazz = LocalIntObj.class;
		Dao<LocalIntObj, Object> dao = createDao(clazz, true);
		LocalIntObj foo = new LocalIntObj();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.INTEGER_OBJ, INT_COLUMN, true, true, false, false,
				false, false, true, true);
	}

	/* ============================================================================================ */

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalIntObj {
		@DatabaseField(columnName = INT_COLUMN)
		Integer intField;
	}
}
