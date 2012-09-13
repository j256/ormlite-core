package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class DoubleTypeTest extends BaseTypeTest {

	private static final String DOUBLE_COLUMN = "doubleField";

	@Test
	public void testDouble() throws Exception {
		Class<LocalDouble> clazz = LocalDouble.class;
		Dao<LocalDouble, Object> dao = createDao(clazz, true);
		double val = 13313323131.221;
		String valStr = Double.toString(val);
		LocalDouble foo = new LocalDouble();
		foo.doubleField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.DOUBLE, DOUBLE_COLUMN, false, true, false, true,
				false, false, true, false);
	}

	@Test
	public void testDoublePrimitiveNull() throws Exception {
		Dao<LocalDoubleObj, Object> objDao = createDao(LocalDoubleObj.class, true);
		LocalDoubleObj foo = new LocalDoubleObj();
		foo.doubleField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalDouble, Object> dao = createDao(LocalDouble.class, false);
		List<LocalDouble> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0.0F, all.get(0).doubleField, 0.0F);
	}

	@Test
	public void testCoverage() {
		new DoubleType(SqlType.DOUBLE, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDouble {
		@DatabaseField(columnName = DOUBLE_COLUMN)
		double doubleField;;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDoubleObj {
		@DatabaseField(columnName = DOUBLE_COLUMN)
		Double doubleField;;
	}
}
