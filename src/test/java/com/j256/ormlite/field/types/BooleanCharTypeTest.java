package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class BooleanCharTypeTest extends BaseTypeTest {

	private static final String BOOLEAN_COLUMN = "bool";

	@Test
	public void testBooleanObj() throws Exception {
		Class<LocalBooleanChar> clazz = LocalBooleanChar.class;
		Dao<LocalBooleanChar, Object> dao = createDao(clazz, true);
		boolean val = true;
		String valStr = Boolean.toString(val);
		LocalBooleanChar foo = new LocalBooleanChar();
		foo.bool = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, '1', '1', valStr, DataType.BOOLEAN_CHAR, BOOLEAN_COLUMN, false, false, false,
				true, false, false, true, false);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBooleanChar {
		@DatabaseField(columnName = BOOLEAN_COLUMN, dataType = DataType.BOOLEAN_CHAR)
		boolean bool;
	}
}
