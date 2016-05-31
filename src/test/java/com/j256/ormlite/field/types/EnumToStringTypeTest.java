package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.table.DatabaseTable;

public class EnumToStringTypeTest extends BaseTypeTest {

	private static final String ENUM_COLUMN = "ourEnum";

	@Test
	public void testEnumString() throws Exception {
		Class<LocalEnumToString> clazz = LocalEnumToString.class;
		Dao<LocalEnumToString, Object> dao = createDao(clazz, true);
		OurEnum val = OurEnum.SECOND;
		String valStr = val.toString();
		String sqlVal = valStr;
		LocalEnumToString foo = new LocalEnumToString();
		foo.ourEnum = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, sqlVal, sqlVal, valStr, DataType.ENUM_TO_STRING, ENUM_COLUMN, false, true, true,
				false, false, false, true, false);
	}

	@Test
	public void testEnumStringNull() throws Exception {
		Class<LocalEnumToString> clazz = LocalEnumToString.class;
		Dao<LocalEnumToString, Object> dao = createDao(clazz, true);
		LocalEnumToString foo = new LocalEnumToString();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.ENUM_TO_STRING, ENUM_COLUMN, false, true, true,
				false, false, false, true, false);
	}

	@Test
	public void testEnumStringCreateGet() throws Exception {
		Class<LocalEnumToString> clazz = LocalEnumToString.class;
		Dao<LocalEnumToString, Object> dao = createDao(clazz, true);
		LocalEnumToString foo1 = new LocalEnumToString();
		foo1.ourEnum = OurEnum.FIRST;
		assertEquals(1, dao.create(foo1));
		LocalEnumToString foo2 = new LocalEnumToString();
		foo2.ourEnum = OurEnum.SECOND;
		assertEquals(1, dao.create(foo2));
		List<LocalEnumToString> results = dao.queryForAll();
		assertEquals(2, results.size());
		assertEquals(foo1.ourEnum, results.get(0).ourEnum);
		assertEquals(foo2.ourEnum, results.get(1).ourEnum);
	}

	@Test
	public void testEnumToStringValue() throws Exception {
		Class<LocalEnumToString> clazz = LocalEnumToString.class;
		Dao<LocalEnumToString, Object> dao = createDao(clazz, true);
		LocalEnumToString foo = new LocalEnumToString();
		foo.ourEnum = OurEnum.SECOND;
		assertEquals(1, dao.create(foo));
		GenericRawResults<String[]> results = dao.queryRaw("select * from " + TABLE_NAME);
		CloseableIterator<String[]> iterator = results.closeableIterator();
		try {
			assertTrue(iterator.hasNext());
			String[] result = iterator.next();
			assertNotNull(result);
			assertEquals(1, result.length);
			assertFalse(OurEnum.SECOND.name().equals(result[0]));
			assertTrue(OurEnum.SECOND.toString().equals(result[0]));
		} finally {
			IOUtils.closeQuietly(iterator);
		}
	}

	@Test
	public void testCoverage() {
		new EnumToStringType(SqlType.STRING, new Class[0]);
	}

	/* ================================================================================ */

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalEnumToString {
		@DatabaseField(columnName = ENUM_COLUMN, dataType = DataType.ENUM_TO_STRING)
		OurEnum ourEnum;
	}

	private enum OurEnum {
		FIRST,
		SECOND,
		// end
		;

		@Override
		public String toString() {
			return name() + " and other stuff";
		}
	}
}
