package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class StringTypeTest extends BaseTypeTest {

	private static final String STRING_COLUMN = "string";

	@Test
	public void testString() throws Exception {
		Class<LocalString> clazz = LocalString.class;
		Dao<LocalString, Object> dao = createDao(clazz, true);
		String val = "str";
		String valStr = val;
		LocalString foo = new LocalString();
		foo.string = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.STRING, STRING_COLUMN, false, true, true, false,
				false, false, true, false);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalString {
		@DatabaseField(columnName = STRING_COLUMN)
		String string;
	}
}
