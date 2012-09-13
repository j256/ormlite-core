package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class CharTypeTest extends BaseTypeTest {

	private static final String CHAR_COLUMN = "charField";

	@Test
	public void testChar() throws Exception {
		Class<LocalChar> clazz = LocalChar.class;
		Dao<LocalChar, Object> dao = createDao(clazz, true);
		char val = 'w';
		String valStr = Character.toString(val);
		LocalChar foo = new LocalChar();
		foo.charField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.CHAR, CHAR_COLUMN, false, true, true, true, false,
				false, true, false);
	}

	@Test
	public void testPostgresChar() throws Exception {
		Dao<PostgresCharNull, Integer> dao = createDao(PostgresCharNull.class, true);
		PostgresCharNull nullChar = new PostgresCharNull();
		nullChar.charField = '\0';
		assertEquals(1, dao.create(nullChar));
	}

	@Test
	public void testCoverage() {
		new CharType(SqlType.CHAR, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalChar {
		@DatabaseField(columnName = CHAR_COLUMN)
		char charField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class PostgresCharNull {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = CHAR_COLUMN)
		char charField;
		PostgresCharNull() {
		}
	}
}
