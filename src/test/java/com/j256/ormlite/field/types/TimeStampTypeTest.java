package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.DateTypeTest.LocalDate;
import com.j256.ormlite.table.DatabaseTable;

public class TimeStampTypeTest extends BaseTypeTest {

	private static final String TIME_STAMP_COLUMN = "timestamp";
	private DataType dataType = DataType.TIME_STAMP;

	@Test
	public void testTimeStamp() throws Exception {
		Class<LocalTimeStamp> clazz = LocalTimeStamp.class;
		Dao<LocalTimeStamp, Object> dao = createDao(clazz, true);
		GregorianCalendar c = new GregorianCalendar();
		c.set(GregorianCalendar.MILLISECOND, 0);
		long millis = c.getTimeInMillis();
		java.sql.Timestamp val = new java.sql.Timestamp(millis);
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		LocalTimeStamp foo = new LocalTimeStamp();
		foo.timestamp = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, dataType, TIME_STAMP_COLUMN, false, true, true, false, true,
				false, true, false);
	}

	@Test
	public void testTimeStampNull() throws Exception {
		Class<LocalTimeStamp> clazz = LocalTimeStamp.class;
		Dao<LocalTimeStamp, Object> dao = createDao(clazz, true);
		LocalTimeStamp foo = new LocalTimeStamp();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, dataType, TIME_STAMP_COLUMN, false, true, true, false, true,
				false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testTimeStampParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalTimeStamp.class.getDeclaredField(TIME_STAMP_COLUMN), LocalTimeStamp.class);
		dataType.getDataPersister().parseDefaultString(fieldType, "not valid date string");
	}

	@Test
	public void testCoverage() {
		new TimeStampType(SqlType.DATE, new Class[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidDateField() throws Exception {
		FieldType.createFieldType(connectionSource, TABLE_NAME, InvalidDate.class.getDeclaredField("invalidType"),
				LocalDate.class);
	}

	@DatabaseTable
	protected static class InvalidDate {
		@DatabaseField(dataType = DataType.TIME_STAMP)
		String invalidType;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalTimeStamp {
		@DatabaseField(columnName = TIME_STAMP_COLUMN)
		java.sql.Timestamp timestamp;
	}
}
