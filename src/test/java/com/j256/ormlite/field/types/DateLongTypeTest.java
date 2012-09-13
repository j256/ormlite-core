package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class DateLongTypeTest extends BaseTypeTest {

	private static final String DATE_COLUMN = "date";

	@Test
	public void testDateLong() throws Exception {
		Class<LocalDateLong> clazz = LocalDateLong.class;
		Dao<LocalDateLong, Object> dao = createDao(clazz, true);
		Date val = new Date();
		long sqlVal = val.getTime();
		String valStr = Long.toString(val.getTime());
		LocalDateLong foo = new LocalDateLong();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, sqlVal, sqlVal, valStr, DataType.DATE_LONG, DATE_COLUMN, false, true, false,
				false, false, false, true, false);
	}

	@Test
	public void testDateLongNull() throws Exception {
		Class<LocalDateLong> clazz = LocalDateLong.class;
		Dao<LocalDateLong, Object> dao = createDao(clazz, true);
		LocalDateLong foo = new LocalDateLong();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.DATE_LONG, DATE_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testDateLongParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalDateLong.class.getDeclaredField(DATE_COLUMN), LocalDateLong.class);
		DataType.DATE_LONG.getDataPersister().parseDefaultString(fieldType, "not valid long number");
	}

	@Test
	public void testCoverage() {
		new DateLongType(SqlType.LONG, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDateLong {
		@DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE_LONG)
		Date date;
	}
}
