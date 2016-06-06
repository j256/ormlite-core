package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class ShortTypeTest extends BaseTypeTest {

	private static final String SHORT_COLUMN = "shortField";

	@Test
	public void testShort() throws Exception {
		Class<LocalShort> clazz = LocalShort.class;
		Dao<LocalShort, Object> dao = createDao(clazz, true);
		short val = 12312;
		String valStr = Short.toString(val);
		LocalShort foo = new LocalShort();
		foo.shortField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.SHORT, SHORT_COLUMN, false, true, false, true, false,
				false, true, true);
	}

	@Test
	public void testShortPrimitiveNull() throws Exception {
		Dao<LocalShortObj, Object> objDao = createDao(LocalShortObj.class, true);
		LocalShortObj foo = new LocalShortObj();
		foo.shortField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalShort, Object> dao = createDao(LocalShort.class, false);
		List<LocalShort> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).shortField);
	}

	@Test
	public void testCoverage() {
		new ShortType(SqlType.SHORT, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalShort {
		@DatabaseField(columnName = SHORT_COLUMN)
		short shortField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalShortObj {
		@DatabaseField(columnName = SHORT_COLUMN)
		Short shortField;
	}
}
