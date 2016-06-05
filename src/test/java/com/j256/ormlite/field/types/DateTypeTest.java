package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.DatabaseTable;

public class DateTypeTest extends BaseTypeTest {

	private static final String DATE_COLUMN = "date";

	@Test
	public void testDate() throws Exception {
		Class<LocalDate> clazz = LocalDate.class;
		Dao<LocalDate, Object> dao = createDao(clazz, true);
		// we have to round to 0 millis
		long millis = System.currentTimeMillis();
		millis -= millis % 1000;
		java.util.Date val = new java.util.Date(millis);
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		LocalDate foo = new LocalDate();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		Timestamp timestamp = new Timestamp(val.getTime());
		testType(dao, foo, clazz, val, timestamp, timestamp, valStr, DataType.DATE, DATE_COLUMN, false, true, true,
				false, true, false, true, false);
	}

	@Test
	public void testDateNull() throws Exception {
		Class<LocalDate> clazz = LocalDate.class;
		Dao<LocalDate, Object> dao = createDao(clazz, true);
		LocalDate foo = new LocalDate();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.DATE, DATE_COLUMN, false, true, true, false, true,
				false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testDateParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME, LocalDate.class.getDeclaredField(DATE_COLUMN),
						LocalDate.class);
		DataType.DATE.getDataPersister().parseDefaultString(fieldType, "not valid date string");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidDateField() throws Exception {
		FieldType.createFieldType(connectionSource, TABLE_NAME, InvalidDate.class.getDeclaredField("notDate"),
				LocalDate.class);
	}

	@DatabaseTable
	protected static class InvalidDate {
		@DatabaseField(dataType = DataType.DATE)
		String notDate;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDate {
		@DatabaseField(columnName = DATE_COLUMN)
		java.util.Date date;
	}
}
