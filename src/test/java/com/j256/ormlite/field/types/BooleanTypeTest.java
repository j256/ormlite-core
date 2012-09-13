package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class BooleanTypeTest extends BaseTypeTest {

	private static final String BOOLEAN_COLUMN = "bool";

	@Test
	public void testBoolean() throws Exception {
		Class<LocalBoolean> clazz = LocalBoolean.class;
		Dao<LocalBoolean, Object> dao = createDao(clazz, true);
		boolean val = true;
		String valStr = Boolean.toString(val);
		LocalBoolean foo = new LocalBoolean();
		foo.bool = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.BOOLEAN, BOOLEAN_COLUMN, false, false, false, true,
				false, false, true, false);
	}

	@Test
	public void testBooleanPrimitiveNull() throws Exception {
		Dao<LocalBooleanObj, Object> objDao = createDao(LocalBooleanObj.class, true);
		LocalBooleanObj foo = new LocalBooleanObj();
		foo.bool = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalBoolean, Object> dao = createDao(LocalBoolean.class, false);
		List<LocalBoolean> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertFalse(all.get(0).bool);
	}

	@Test
	public void testCoverage() {
		new BooleanType(SqlType.BOOLEAN, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBoolean {
		@DatabaseField(columnName = BOOLEAN_COLUMN)
		boolean bool;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBooleanObj {
		@DatabaseField(columnName = BOOLEAN_COLUMN)
		Boolean bool;
	}
}
