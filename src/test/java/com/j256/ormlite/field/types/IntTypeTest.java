package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class IntTypeTest extends BaseTypeTest {

	private static final String INT_COLUMN = "intField";

	@Test
	public void testInt() throws Exception {
		Class<LocalInt> clazz = LocalInt.class;
		Dao<LocalInt, Object> dao = createDao(clazz, true);
		int val = 313213123;
		String valStr = Integer.toString(val);
		LocalInt foo = new LocalInt();
		foo.intField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.INTEGER, INT_COLUMN, true, true, false, true, false, false,
				true, true);
	}

	@Test
	public void testIntPrimitiveNull() throws Exception {
		Dao<LocalIntObj, Object> objDao = createDao(LocalIntObj.class, true);
		LocalIntObj foo = new LocalIntObj();
		foo.intField = null;
		assertEquals(1, objDao.create(foo));
		// overlapping table
		Dao<LocalInt, Object> dao = createDao(LocalInt.class, false);
		List<LocalInt> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).intField);
	}

	@Test
	public void testIntConvertId() throws Exception {
		int intId = 213123123;
		long longId = new Long(intId);
		assertEquals(intId, DataType.INTEGER.getDataPersister().convertIdNumber(longId));
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalIntObj {
		@DatabaseField(columnName = INT_COLUMN)
		Integer intField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalInt {
		@DatabaseField(columnName = INT_COLUMN)
		int intField;
	}
}
