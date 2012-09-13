package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class FloatTypeTest extends BaseTypeTest {

	private static final String FLOAT_COLUMN = "floatField";

	@Test
	public void testFloat() throws Exception {
		Class<LocalFloat> clazz = LocalFloat.class;
		Dao<LocalFloat, Object> dao = createDao(clazz, true);
		float val = 1331.221F;
		String valStr = Float.toString(val);
		LocalFloat foo = new LocalFloat();
		foo.floatField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.FLOAT, FLOAT_COLUMN, false, true, false, true, false,
				false, true, false);
	}

	@Test
	public void testFloatPrimitiveNull() throws Exception {
		Dao<LocalFloatObj, Object> objDao = createDao(LocalFloatObj.class, true);
		LocalFloatObj foo = new LocalFloatObj();
		foo.floatField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalFloat, Object> dao = createDao(LocalFloat.class, false);
		List<LocalFloat> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0.0F, all.get(0).floatField, 0.0F);
	}

	@Test
	public void testCoverage() {
		new FloatType(SqlType.FLOAT, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalFloat {
		@DatabaseField(columnName = FLOAT_COLUMN)
		float floatField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalFloatObj {
		@DatabaseField(columnName = FLOAT_COLUMN)
		Float floatField;
	}
}
