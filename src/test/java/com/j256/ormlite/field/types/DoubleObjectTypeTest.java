package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class DoubleObjectTypeTest extends BaseTypeTest {

	private static final String DOUBLE_COLUMN = "doubleField";

	@Test
	public void testDoubleObj() throws Exception {
		Class<LocalDoubleObj> clazz = LocalDoubleObj.class;
		Dao<LocalDoubleObj, Object> dao = createDao(clazz, true);
		Double val = 13313323131.221;
		String valStr = val.toString();
		LocalDoubleObj foo = new LocalDoubleObj();
		foo.doubleField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.DOUBLE_OBJ, DOUBLE_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@Test
	public void testDoubleObjNull() throws Exception {
		Class<LocalDoubleObj> clazz = LocalDoubleObj.class;
		Dao<LocalDoubleObj, Object> dao = createDao(clazz, true);
		LocalDoubleObj foo = new LocalDoubleObj();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.DOUBLE_OBJ, DOUBLE_COLUMN, false, true, false,
				false, false, false, true, false);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDoubleObj {
		@DatabaseField(columnName = DOUBLE_COLUMN)
		Double doubleField;;
	}
}
