package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class CharObjectTypeTest extends BaseTypeTest {

	private static final String CHAR_COLUMN = "charField";

	@Test
	public void testCharObj() throws Exception {
		Class<LocalCharObj> clazz = LocalCharObj.class;
		Dao<LocalCharObj, Object> dao = createDao(clazz, true);
		Character val = 'w';
		String valStr = val.toString();
		LocalCharObj foo = new LocalCharObj();
		foo.charField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.CHAR_OBJ, CHAR_COLUMN, false, true, true, false,
				false, false, true, false);
	}

	@Test
	public void testCharObjNull() throws Exception {
		Class<LocalCharObj> clazz = LocalCharObj.class;
		Dao<LocalCharObj, Object> dao = createDao(clazz, true);
		LocalCharObj foo = new LocalCharObj();
		assertEquals(1, dao.create(new LocalCharObj()));
		testType(dao, foo, clazz, null, null, null, null, DataType.CHAR_OBJ, CHAR_COLUMN, false, true, true, false,
				false, false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testInvalidDefault() throws Exception {
		createDao(InvalidDefault.class, true);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalCharObj {
		@DatabaseField(columnName = CHAR_COLUMN)
		Character charField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class InvalidDefault {
		@DatabaseField(columnName = CHAR_COLUMN, defaultValue = "not one char")
		Character charField;
	}
}
