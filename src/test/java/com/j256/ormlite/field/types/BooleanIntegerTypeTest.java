package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class BooleanIntegerTypeTest extends BaseTypeTest {

	private static final String BOOLEAN_COLUMN = "bool";

	@Test
	public void testBooleanInteger() throws Exception {
		Class<LocalBooleanInteger> clazz = LocalBooleanInteger.class;
		Dao<LocalBooleanInteger, Object> dao = createDao(clazz, true);
		boolean val = true;
		String valStr = Boolean.toString(val);
		LocalBooleanInteger foo = new LocalBooleanInteger();
		foo.bool = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, 1, 1, valStr, DataType.BOOLEAN_INTEGER, BOOLEAN_COLUMN, false, false, false,
				true, false, false, true, false);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBooleanInteger {
		@DatabaseField(columnName = BOOLEAN_COLUMN, dataType = DataType.BOOLEAN_INTEGER)
		boolean bool;
	}
}
