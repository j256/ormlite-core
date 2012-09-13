package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class LongStringTypeTest extends BaseTypeTest {

	private static final String STRING_COLUMN = "string";

	@Test
	public void testLongString() throws Exception {
		Class<LocalLongString> clazz = LocalLongString.class;
		Dao<LocalLongString, Object> dao = createDao(LocalLongString.class, true);
		String val = "str";
		String valStr = val;
		LocalLongString foo = new LocalLongString();
		foo.string = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.LONG_STRING, STRING_COLUMN, false, false, true,
				false, false, false, true, false);
	}

	@Test
	public void testCoverage() {
		new LongStringType(SqlType.LONG_STRING, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalLongString {
		@DatabaseField(columnName = STRING_COLUMN, dataType = DataType.LONG_STRING)
		String string;
	}
}
