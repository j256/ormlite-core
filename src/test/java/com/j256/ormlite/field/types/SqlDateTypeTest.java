package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class SqlDateTypeTest extends BaseTypeTest {

	private static final String DATE_COLUMN = "date";
	private DataType dataType = DataType.SQL_DATE;

	@Test
	public void testSqlDate() throws Exception {
		Class<LocalDate> clazz = LocalDate.class;
		Dao<LocalDate, Object> dao = createDao(clazz, true);

		GregorianCalendar c = new GregorianCalendar();
		c.set(GregorianCalendar.HOUR_OF_DAY, 0);
		c.set(GregorianCalendar.MINUTE, 0);
		c.set(GregorianCalendar.SECOND, 0);
		c.set(GregorianCalendar.MILLISECOND, 0);
		long millis = c.getTimeInMillis();

		java.sql.Date val = new java.sql.Date(millis);
		String format = "yyyy-MM-dd HH:mm:ss.S";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		LocalDate foo = new LocalDate();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		Timestamp timestamp = new Timestamp(val.getTime());
		testType(dao, foo, clazz, val, timestamp, timestamp, valStr, dataType, DATE_COLUMN, false, true, true, false,
				true, false, true, false);
	}

	@Test
	public void testSqlDateNull() throws Exception {
		Class<LocalDate> clazz = LocalDate.class;
		Dao<LocalDate, Object> dao = createDao(clazz, true);
		LocalDate foo = new LocalDate();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, dataType, DATE_COLUMN, false, true, true, false, true, false,
				true, false);
	}

	@Test(expected = SQLException.class)
	public void testSqlDateParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME, LocalDate.class.getDeclaredField(DATE_COLUMN),
						LocalDate.class);
		dataType.getDataPersister().parseDefaultString(fieldType, "not valid date string");
	}

	@Test
	public void testCoverage() {
		new SqlDateType(SqlType.DATE, new Class[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidDateField() throws Exception {
		FieldType.createFieldType(connectionSource, TABLE_NAME, InvalidDate.class.getDeclaredField("notDate"),
				LocalDate.class);
	}

	@DatabaseTable
	protected static class InvalidDate {
		@DatabaseField(dataType = DataType.SQL_DATE)
		String notDate;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDate {
		@DatabaseField(columnName = DATE_COLUMN)
		java.sql.Date date;
	}
}
