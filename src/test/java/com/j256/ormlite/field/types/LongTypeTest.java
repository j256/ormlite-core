package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class LongTypeTest extends BaseTypeTest {

	private static final String LONG_COLUMN = "longField";

	@Test
	public void testLong() throws Exception {
		Class<LocalLong> clazz = LocalLong.class;
		Dao<LocalLong, Object> dao = createDao(clazz, true);
		long val = 13312321312312L;
		String valStr = Long.toString(val);
		LocalLong foo = new LocalLong();
		foo.longField = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.LONG, LONG_COLUMN, true, true, false, true, false,
				false, true, true);
	}

	@Test
	public void testLongPrimitiveNull() throws Exception {
		Dao<LocalLongObj, Object> objDao = createDao(LocalLongObj.class, true);
		LocalLongObj foo = new LocalLongObj();
		foo.longField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalLong, Object> dao = createDao(LocalLong.class, false);
		List<LocalLong> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).longField);
	}

	@Test
	public void testLongConvertId() {
		long longId = 1312313123131L;
		assertEquals(longId, DataType.LONG.getDataPersister().convertIdNumber(longId));
	}

	@Test
	public void testCoverage() {
		new LongType(SqlType.LONG, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalLong {
		@DatabaseField(columnName = LONG_COLUMN)
		long longField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalLongObj {
		@DatabaseField(columnName = LONG_COLUMN)
		Long longField;
	}
}
